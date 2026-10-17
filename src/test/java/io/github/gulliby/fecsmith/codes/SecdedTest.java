package io.github.gulliby.fecsmith.codes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class SecdedTest {
    private static final String MAX_DATA =
            "0000000000" + "0000000000" + "0000000000" + "0000000000"
                    + "0000000000" + "0000000000" + "0000000000" + "0000000000"
                    + "0000000000" + "0000000000" + "0000000000" + "0000000001";

    private static final String MAX_CODEWORD =
            "01101000" + "10000000" + "10000000" + "00000000"
                    + "10000000" + "00000000" + "00000000" + "00000000"
                    + "10000000" + "00000000" + "00000000" + "00000000"
                    + "00000000" + "00000000" + "00000000" + "00000001";

    private static final String MAX_RECEIVED =
            "01101000" + "10000000" + "10000000" + "00000000"
                    + "10000000" + "00000000" + "00000000" + "00000000"
                    + "10000000" + "00000000" + "00000000" + "00000000"
                    + "00000000" + "00000000" + "00000000" + "00000000";

    @Test
    void literalLayoutsAndMatchingLengthsCoverParityTransitions() {
        assertEquals(new Secded.Layout(1, 2, 4), Secded.forData(1));
        assertEquals(Optional.of(new Secded.Layout(1, 2, 4)), Secded.forLength(4));
        assertEquals(new Secded.Layout(2, 3, 6), Secded.forData(2));
        assertEquals(Optional.of(new Secded.Layout(2, 3, 6)), Secded.forLength(6));
        assertEquals(new Secded.Layout(4, 3, 8), Secded.forData(4));
        assertEquals(Optional.of(new Secded.Layout(4, 3, 8)), Secded.forLength(8));
        assertEquals(new Secded.Layout(5, 4, 10), Secded.forData(5));
        assertEquals(Optional.of(new Secded.Layout(5, 4, 10)), Secded.forLength(10));
        assertEquals(new Secded.Layout(11, 4, 16), Secded.forData(11));
        assertEquals(Optional.of(new Secded.Layout(11, 4, 16)), Secded.forLength(16));
        assertEquals(new Secded.Layout(12, 5, 18), Secded.forData(12));
        assertEquals(Optional.of(new Secded.Layout(12, 5, 18)), Secded.forLength(18));
        assertEquals(new Secded.Layout(26, 5, 32), Secded.forData(26));
        assertEquals(Optional.of(new Secded.Layout(26, 5, 32)), Secded.forLength(32));
        assertEquals(new Secded.Layout(27, 6, 34), Secded.forData(27));
        assertEquals(Optional.of(new Secded.Layout(27, 6, 34)), Secded.forLength(34));
        assertEquals(new Secded.Layout(57, 6, 64), Secded.forData(57));
        assertEquals(Optional.of(new Secded.Layout(57, 6, 64)), Secded.forLength(64));
        assertEquals(new Secded.Layout(58, 7, 66), Secded.forData(58));
        assertEquals(Optional.of(new Secded.Layout(58, 7, 66)), Secded.forLength(66));
        assertEquals(new Secded.Layout(120, 7, 128), Secded.forData(120));
        assertEquals(Optional.of(new Secded.Layout(120, 7, 128)), Secded.forLength(128));
    }

    @Test
    void lengthLookupRejectsEveryLiteralGapAndIntegerExtreme() {
        assertEquals(Optional.empty(), Secded.forLength(3));
        assertEquals(Optional.empty(), Secded.forLength(5));
        assertEquals(Optional.empty(), Secded.forLength(9));
        assertEquals(Optional.empty(), Secded.forLength(17));
        assertEquals(Optional.empty(), Secded.forLength(33));
        assertEquals(Optional.empty(), Secded.forLength(65));
        assertEquals(Optional.empty(), Secded.forLength(129));
        assertEquals(Optional.empty(), Secded.forLength(Integer.MIN_VALUE));
        assertEquals(Optional.empty(), Secded.forLength(Integer.MAX_VALUE));
    }

    @Test
    void dataPositionsAreIncreasingAndExcludeParityIndexes() {
        assertArrayEquals(new int[] {3}, Secded.dataPositions(1));
        assertArrayEquals(new int[] {3, 5, 6, 7, 9}, Secded.dataPositions(5));
    }

    @Test
    void literalEncodingsCoverZeroOneExampleAndMaximumDataLength() {
        assertEquals("0000", Secded.encode("0"));
        assertEquals("1111", Secded.encode("1"));
        assertEquals("0011001100", Secded.encode("10110"));
        assertEquals(MAX_CODEWORD, Secded.encode(MAX_DATA));
    }

    @Test
    void literalDecodedRecordsCoverAllStatusesAndKFiveCorrections() {
        assertEquals(new Secded.Decoded(Secded.Status.OK, 0, false, -1,
                        "0011001100", "10110"),
                Secded.decode("0011001100"));
        assertEquals(new Secded.Decoded(Secded.Status.CORRECTED, 0, true, 0,
                        "0011001100", "10110"),
                Secded.decode("1011001100"));
        assertEquals(new Secded.Decoded(Secded.Status.CORRECTED, 1, true, 1,
                        "0011001100", "10110"),
                Secded.decode("0111001100"));
        assertEquals(new Secded.Decoded(Secded.Status.CORRECTED, 3, true, 3,
                        "0011001100", "10110"),
                Secded.decode("0010001100"));
        assertEquals(new Secded.Decoded(Secded.Status.DOUBLE, 5, false, -1, null, null),
                Secded.decode("0001001000"));
        assertEquals(new Secded.Decoded(Secded.Status.OUTSIDE, 10, true, -1, null, null),
                Secded.decode("1001001110"));
    }

    @Test
    void minimumLengthWordDecodesAndCorrectsItsLastIndex() {
        assertEquals(new Secded.Decoded(Secded.Status.OK, 0, false, -1, "0000", "0"),
                Secded.decode("0000"));
        assertEquals(new Secded.Decoded(Secded.Status.CORRECTED, 3, true, 3, "0000", "0"),
                Secded.decode("0001"));
    }

    @Test
    void maximumLengthWordCorrectsItsLastValidIndex() {
        assertEquals(new Secded.Decoded(Secded.Status.CORRECTED, 127, true, 127,
                        MAX_CODEWORD, MAX_DATA),
                Secded.decode(MAX_RECEIVED));
    }
}
