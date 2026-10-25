package io.github.gulliby.fecsmith.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class HexPrefixTest {
    @Test
    void bytesAcceptsALowerCaseOrUpperCasePrefix() {
        assertArrayEquals(new byte[] {(byte) 0x0A}, Hex.bytes(List.of("0x0A")));
        assertArrayEquals(new byte[] {(byte) 0xAB, (byte) 0xCD}, Hex.bytes(List.of("0Xabcd")));
    }

    @Test
    void bytesReadsPrefixedAndPlainWordsInOrder() {
        assertArrayEquals(new byte[] {0x31, 0x32, 0x33, 0x34, 0x35},
                Hex.bytes(List.of("0x31", "32", "0X3334", "35")));
    }

    @Test
    void aPrefixWithoutDigitsIsRejected() {
        assertBytesError("invalid hex word \"0x\" (expected pairs of hex digits)", List.of("0x"));
        assertBytesError("invalid hex word \"0X\" (expected pairs of hex digits)", List.of("0X"));
    }

    @Test
    void anOddOrNonHexRemainderAfterThePrefixIsRejectedWithTheWholeWord() {
        assertBytesError("invalid hex word \"0x011\" (expected pairs of hex digits)", List.of("0x011"));
        assertBytesError("invalid hex word \"0xgg\" (expected pairs of hex digits)", List.of("0xgg"));
    }

    @Test
    void onlyOneLeadingPrefixIsAllowed() {
        assertBytesError("invalid hex word \"0x0x01\" (expected pairs of hex digits)", List.of("0x0x01"));
        assertBytesError("invalid hex word \"00x01\" (expected pairs of hex digits)", List.of("00x01"));
        assertBytesError("invalid hex word \"x01\" (expected pairs of hex digits)", List.of("x01"));
    }

    private static void assertBytesError(String expectedMessage, List<String> words) {
        Failure.Usage failure = assertThrows(Failure.Usage.class, () -> Hex.bytes(words));
        assertEquals(expectedMessage, failure.getMessage());
    }
}
