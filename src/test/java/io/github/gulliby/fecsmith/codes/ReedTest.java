package io.github.gulliby.fecsmith.codes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.gulliby.fecsmith.gf.Field;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class ReedTest {
    private static final int[] ZERO_PARITY_32 = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    private static final int[] ZERO_WORD_33 = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    @Test
    void tIsTheFloorOfTheParityCountForOddAndEvenCounts() {
        assertEquals(1, new Reed.Code(Field.of(3), 2).t());
        assertEquals(1, new Reed.Code(Field.of(3), 3).t());
        assertEquals(2, new Reed.Code(Field.of(8), 4).t());
        assertEquals(16, new Reed.Code(Field.of(6), 32).t());
    }

    @Test
    void parityUsesTheSpecifiedRootsAcrossEverySupportedField() {
        assertArrayEquals(new int[] {6, 3},
                Reed.parity(new Reed.Code(Field.of(3), 2), new int[] {1}));
        assertArrayEquals(new int[] {6, 8},
                Reed.parity(new Reed.Code(Field.of(4), 2), new int[] {1}));
        assertArrayEquals(new int[] {6, 8},
                Reed.parity(new Reed.Code(Field.of(5), 2), new int[] {1}));
        assertArrayEquals(new int[] {6, 8},
                Reed.parity(new Reed.Code(Field.of(6), 2), new int[] {1}));
        assertArrayEquals(new int[] {6, 8},
                Reed.parity(new Reed.Code(Field.of(7), 2), new int[] {1}));
        assertArrayEquals(new int[] {6, 8},
                Reed.parity(new Reed.Code(Field.of(8), 2), new int[] {1}));
    }

    @Test
    void parityKeepsLeadingZerosAndOddParityCounts() {
        assertArrayEquals(new int[] {0, 1},
                Reed.parity(new Reed.Code(Field.of(3), 2), new int[] {1, 0, 0, 0, 1}));
        assertArrayEquals(new int[] {5, 2, 5},
                Reed.parity(new Reed.Code(Field.of(3), 3), new int[] {1}));
        assertArrayEquals(ZERO_PARITY_32,
                Reed.parity(new Reed.Code(Field.of(6), 32), new int[] {0}));
    }

    @Test
    void syndromesMatchTheSuppliedCleanAndCorruptedGf256Words() {
        Reed.Code code = new Reed.Code(Field.of(8), 4);

        assertArrayEquals(new int[] {0, 0, 0, 0},
                Reed.syndromes(code, new int[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x08, 0x47, 0x90, 0xca}));
        assertArrayEquals(new int[] {0x05, 0x5a, 0xa0, 0x6c},
                Reed.syndromes(code, new int[] {0x48, 0x00, 0x6c, 0x6c, 0xff, 0x08, 0x47, 0x90, 0xca}));
    }

    @Test
    void suppliedGf256ParityIsFixedWidthAndHighestDegreeFirst() {
        assertArrayEquals(new int[] {0x08, 0x47, 0x90, 0xca},
                Reed.parity(new Reed.Code(Field.of(8), 4),
                        new int[] {0x48, 0x65, 0x6c, 0x6c, 0x6f}));
    }

    @Test
    void oddParityCountDecodesOneErrorInAShortenedWord() {
        Reed.Code code = new Reed.Code(Field.of(3), 3);
        int[] clean = {1, 5, 2, 5};
        int[] received = {1, 4, 2, 5};

        assertArrayEquals(new int[] {0, 0, 0}, Reed.syndromes(code, clean));
        Reed.Decoded decoded = Reed.decode(code, received);
        assertArrayEquals(new int[] {4, 6, 5}, decoded.syndromes());
        assertTrue(decoded.ok());
        assertEquals(new TreeMap<>(Map.of(1, 1)), decoded.errors());
        assertEquals(List.of(1), List.copyOf(decoded.errors().keySet()));
    }

    @Test
    void suppliedGf256DecoderFindsExactAscendingXorErrors() {
        Reed.Decoded decoded = Reed.decode(new Reed.Code(Field.of(8), 4),
                new int[] {0x48, 0x00, 0x6c, 0x6c, 0xff, 0x08, 0x47, 0x90, 0xca});

        assertArrayEquals(new int[] {0x05, 0x5a, 0xa0, 0x6c}, decoded.syndromes());
        assertTrue(decoded.ok());
        assertEquals(new TreeMap<>(Map.of(1, 0x65, 4, 0x90)), decoded.errors());
        assertEquals(List.of(1, 4), List.copyOf(decoded.errors().keySet()));
    }

    @Test
    void fullLengthCleanAndOneErrorWordsDecodeAtTheCorrectionBoundary() {
        Reed.Code code = new Reed.Code(Field.of(3), 2);
        int[] clean = {1, 0, 0, 0, 0, 6, 2};
        int[] received = {1, 0, 0, 0, 0, 7, 2};

        assertArrayEquals(new int[] {0, 0}, Reed.syndromes(code, clean));
        Reed.Decoded cleanDecoded = Reed.decode(code, clean);
        assertArrayEquals(new int[] {0, 0}, cleanDecoded.syndromes());
        assertTrue(cleanDecoded.ok());
        assertTrue(cleanDecoded.errors().isEmpty());

        Reed.Decoded decoded = Reed.decode(code, received);
        assertArrayEquals(new int[] {2, 4}, decoded.syndromes());
        assertTrue(decoded.ok());
        assertEquals(new TreeMap<>(Map.of(5, 1)), decoded.errors());
        assertEquals(List.of(5), List.copyOf(decoded.errors().keySet()));
    }

    @Test
    void aFullLengthTwoErrorCorruptionMayDecodeToAnAlternateCodeword() {
        Reed.Code code = new Reed.Code(Field.of(3), 2);
        int[] received = {0, 0, 0, 0, 0, 6, 2};

        assertArrayEquals(new int[] {5, 7}, Reed.syndromes(code, received));
        Reed.Decoded decoded = Reed.decode(code, received);
        assertArrayEquals(new int[] {5, 7}, decoded.syndromes());
        assertTrue(decoded.ok());
        assertEquals(new TreeMap<>(Map.of(0, 1)), decoded.errors());
        assertEquals(List.of(0), List.copyOf(decoded.errors().keySet()));
    }

    @Test
    void aShortenedTwoErrorCorruptionMayDecodeToAnAlternateCodeword() {
        Reed.Code code = new Reed.Code(Field.of(3), 2);
        int[] received = {0, 0, 0, 0, 7, 2};

        assertArrayEquals(new int[] {7, 3}, Reed.syndromes(code, received));
        Reed.Decoded decoded = Reed.decode(code, received);
        assertArrayEquals(new int[] {7, 3}, decoded.syndromes());
        assertTrue(decoded.ok());
        assertEquals(new TreeMap<>(Map.of(0, 1)), decoded.errors());
    }

    @Test
    void aFullLengthTwoErrorVectorOutsideEveryRadiusOneBallFailsWithNoErrors() {
        Reed.Code code = new Reed.Code(Field.of(3), 2);
        int[] received = {1, 2, 0, 0, 0, 0, 0};

        assertArrayEquals(new int[] {0, 1}, Reed.syndromes(code, received));
        Reed.Decoded decoded = Reed.decode(code, received);
        assertArrayEquals(new int[] {0, 1}, decoded.syndromes());
        assertFalse(decoded.ok());
        assertTrue(decoded.errors().isEmpty());
    }

    @Test
    void aShortenedTwoErrorVectorOutsideEveryRadiusOneBallFailsWithNoErrors() {
        Reed.Code code = new Reed.Code(Field.of(3), 2);
        int[] received = {1, 2, 0, 0, 0, 0};

        assertArrayEquals(new int[] {0, 7}, Reed.syndromes(code, received));
        Reed.Decoded decoded = Reed.decode(code, received);
        assertArrayEquals(new int[] {0, 7}, decoded.syndromes());
        assertFalse(decoded.ok());
        assertTrue(decoded.errors().isEmpty());
    }

    @Test
    void suppliedGf256UncorrectableVectorRetainsOriginalSyndromesAndEmptyErrors() {
        Reed.Decoded decoded = Reed.decode(new Reed.Code(Field.of(8), 4),
                new int[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x9b, 0x6e, 0x3a, 0x6c});

        assertArrayEquals(new int[] {0xa7, 0xad, 0x88, 0xfa}, decoded.syndromes());
        assertFalse(decoded.ok());
        assertTrue(decoded.errors().isEmpty());
    }

    @Test
    void maximumParityCountPreservesThirtyTwoZeroSymbolsAndDecodesTheZeroWord() {
        Reed.Code code = new Reed.Code(Field.of(6), 32);

        assertArrayEquals(ZERO_PARITY_32, Reed.syndromes(code, ZERO_WORD_33));
        Reed.Decoded decoded = Reed.decode(code, ZERO_WORD_33);
        assertArrayEquals(ZERO_PARITY_32, decoded.syndromes());
        assertTrue(decoded.ok());
        assertTrue(decoded.errors().isEmpty());
    }
}
