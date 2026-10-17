package io.github.gulliby.fecsmith.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;

class HexTest {
    private static final String ZEROES_64_HEX =
            "0000" + "0000" + "0000" + "0000" + "0000" + "0000" + "0000" + "0000"
                    + "0000" + "0000" + "0000" + "0000" + "0000" + "0000" + "0000" + "0000";
    private static final String ZEROES_256_BYTES_HEX =
            ZEROES_64_HEX + ZEROES_64_HEX + ZEROES_64_HEX + ZEROES_64_HEX
                    + ZEROES_64_HEX + ZEROES_64_HEX + ZEROES_64_HEX + ZEROES_64_HEX;
    private static final String FOUR_KIB_HEX =
            ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX
                    + ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX
                    + ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX
                    + ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX + ZEROES_256_BYTES_HEX;

    @Test
    void formatUsesLowercaseAndPadsWithoutTruncating() {
        assertEquals("0", Hex.format(0, 1));
        assertEquals("0000", Hex.format(0, 4));
        assertEquals("f", Hex.format(15, 1));
        assertEquals("0a", Hex.format(10, 2));
        assertEquals("00ff", Hex.format(255, 4));
        assertEquals("1234", Hex.format(0x1234, 2));
        assertEquals("fedcba", Hex.format(0xFEDCBA, 4));
        assertEquals("7fffffffffffffff", Hex.format(Long.MAX_VALUE, 16));
    }

    @Test
    void bytesAcceptsMixedCaseMultiPairWordsAndKeepsTheirOrder() {
        assertArrayEquals(new byte[] {(byte) 0x0A, (byte) 0x0A, (byte) 0x1B, (byte) 0xFF, (byte) 0xFF},
                Hex.bytes(List.of("0A", "0a1B", "FFff")));
        assertArrayEquals(new byte[] {}, Hex.bytes(List.of()));
        assertArrayEquals(new byte[] {(byte) 0x00, (byte) 0x80, (byte) 0xFF},
                Hex.bytes(List.of("00", "80FF")));
    }

    @Test
    void bytesRejectsEmptyOddPrefixedWhitespaceAndNonHexWordsExactly() {
        assertBytesError("invalid hex word \"\" (expected pairs of hex digits)", List.of(""));
        assertBytesError("invalid hex word \"0\" (expected pairs of hex digits)", List.of("0"));
        assertBytesError("invalid hex word \"ABC\" (expected pairs of hex digits)", List.of("ABC"));
        assertBytesError("invalid hex word \"0x01\" (expected pairs of hex digits)", List.of("0x01"));
        assertBytesError("invalid hex word \"0x\" (expected pairs of hex digits)", List.of("0x"));
        assertBytesError("invalid hex word \"AA?\" (expected pairs of hex digits)", List.of("AA "));
        assertBytesError("invalid hex word \"?1\" (expected pairs of hex digits)", List.of("Ａ1"));
        assertBytesError("invalid hex word \"?0\" (expected pairs of hex digits)", List.of("\t0"));
    }

    @Test
    void bytesReportsTheFirstMalformedWord() {
        assertBytesError("invalid hex word \"0x\" (expected pairs of hex digits)", List.of("0x", "gg"));
    }

    @Test
    void bytesAcceptsExactly4096BytesAndRejects4097() {
        byte[] bytes = Hex.bytes(List.of(FOUR_KIB_HEX));

        assertEquals(4096, bytes.length);
        assertEquals(0, bytes[0]);
        assertEquals(0, bytes[4095]);

        assertBytesError("too many bytes (at most 4096)", List.of(FOUR_KIB_HEX + "00"));
    }

    @Test
    void bytesReportsOversizedValidPrefixBeforeALaterMalformedWord() {
        assertBytesError("too many bytes (at most 4096)", List.of(FOUR_KIB_HEX + "00", "not-hex"));
    }

    @Test
    void symbolsAcceptsEmptyAndMaximumValueForEverySupportedWidth() {
        assertArrayEquals(new int[] {}, Hex.symbols(List.of(), 3));
        // m=3, 7 | [7]
        assertArrayEquals(new int[] {7}, Hex.symbols(List.of("7"), 3));
        // m=4, f | [15]
        assertArrayEquals(new int[] {15}, Hex.symbols(List.of("f"), 4));
        // m=5, 1f | [31]
        assertArrayEquals(new int[] {31}, Hex.symbols(List.of("1f"), 5));
        assertArrayEquals(new int[] {63}, Hex.symbols(List.of("3F"), 6));
        assertArrayEquals(new int[] {127}, Hex.symbols(List.of("7f"), 7));
        // m=8, FF | [255]
        assertArrayEquals(new int[] {255}, Hex.symbols(List.of("FF"), 8));
        assertArrayEquals(new int[] {10, 191, 28}, Hex.symbols(List.of("0a", "bF", "1C"), 8));
    }

    @Test
    void symbolsRejectsFirstValueOutsideEachFieldWithExactWidthMessage() {
        // m=3, 8 | invalid symbol "8" (expected 1 hex digit with a value below 2^3)
        assertSymbolError("invalid symbol \"8\" (expected 1 hex digit with a value below 2^3)", List.of("8"), 3);
        assertSymbolError("invalid symbol \"10\" (expected 1 hex digit with a value below 2^4)", List.of("10"), 4);
        // m=5, 20 | invalid symbol "20" (expected 2 hex digits with a value below 2^5)
        assertSymbolError("invalid symbol \"20\" (expected 2 hex digits with a value below 2^5)", List.of("20"), 5);
        assertSymbolError("invalid symbol \"40\" (expected 2 hex digits with a value below 2^6)", List.of("40"), 6);
        assertSymbolError("invalid symbol \"80\" (expected 2 hex digits with a value below 2^7)", List.of("80"), 7);
        // m=8, 100 | invalid symbol "100" (expected 2 hex digits with a value below 2^8)
        assertSymbolError("invalid symbol \"100\" (expected 2 hex digits with a value below 2^8)", List.of("100"), 8);
    }

    @Test
    void symbolsRejectsWrongWidthsAndInvalidCharactersExactly() {
        // m=4, 0f | invalid symbol "0f" (expected 1 hex digit with a value below 2^4)
        assertSymbolError("invalid symbol \"0f\" (expected 1 hex digit with a value below 2^4)", List.of("0f"), 4);
        assertSymbolError("invalid symbol \"1\" (expected 2 hex digits with a value below 2^5)", List.of("1"), 5);
        assertSymbolError("invalid symbol \"F\" (expected 2 hex digits with a value below 2^8)", List.of("F"), 8);
        assertSymbolError("invalid symbol \"g\" (expected 1 hex digit with a value below 2^4)", List.of("g"), 4);
        assertSymbolError("invalid symbol \"G0\" (expected 2 hex digits with a value below 2^5)", List.of("G0"), 5);
        assertSymbolError("invalid symbol \"0x\" (expected 2 hex digits with a value below 2^8)", List.of("0x"), 8);
        assertSymbolError("invalid symbol \"?\" (expected 1 hex digit with a value below 2^4)", List.of("é"), 4);
    }

    @Test
    void symbolsReportsTheFirstInvalidWord() {
        assertSymbolError("invalid symbol \"g0\" (expected 2 hex digits with a value below 2^8)",
                List.of("0A", "g0", "GG"), 8);
    }

    private static void assertBytesError(String expectedMessage, List<String> words) {
        Failure.Usage failure = assertThrows(Failure.Usage.class, () -> Hex.bytes(words));
        assertEquals(expectedMessage, failure.getMessage());
    }

    private static void assertSymbolError(String expectedMessage, List<String> words, int m) {
        Failure.Usage failure = assertThrows(Failure.Usage.class, () -> Hex.symbols(words, m));
        assertEquals(expectedMessage, failure.getMessage());
    }
}
