package io.github.gulliby.fecsmith.codes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ConvTest {
    private static final String MAX_ZERO_BITS =
            "00000000000000000000"
                    + "00000000000000000000"
                    + "00000000000000000000"
                    + "00000000000000000000"
                    + "00000000000000000000"
                    + "00000000000000000000"
                    + "00000000000000000000"
                    + "00000000000000000000"
                    + "00000000000000000000"
                    + "00000000000000000000";

    @Test
    void catalogHasTheSpecifiedNamesConstraintsAndOctalGenerators() {
        assertEquals(List.of("k3", "k5", "k7"), Conv.names());
        assertEquals(new Conv.Code("k3", 3, List.of(7, 5)), Conv.byName("k3").orElseThrow());
        assertEquals(new Conv.Code("k5", 5, List.of(19, 29)), Conv.byName("k5").orElseThrow());
        assertEquals(new Conv.Code("k7", 7, List.of(121, 91)), Conv.byName("k7").orElseThrow());
    }

    @Test
    void lookupIsCaseSensitiveAndRejectsUnknownNames() {
        assertTrue(Conv.byName("").isEmpty());
        assertTrue(Conv.byName("K3").isEmpty());
        assertTrue(Conv.byName("k4").isEmpty());
        assertFalse(Conv.byName("k3").isEmpty());
    }

    @Test
    void k3TransitionsCoverZeroAndHighestStateForBothInputs() {
        assertStep("k3", 0, 0, 0, 0, 0);
        assertStep("k3", 0, 1, 2, 1, 1);
        assertStep("k3", 3, 0, 1, 0, 1);
        assertStep("k3", 3, 1, 3, 1, 0);
    }

    @Test
    void k5TransitionsCoverZeroAndHighestStateForBothInputs() {
        assertStep("k5", 0, 0, 0, 0, 0);
        assertStep("k5", 0, 1, 8, 1, 1);
        assertStep("k5", 15, 0, 7, 0, 1);
        assertStep("k5", 15, 1, 15, 1, 0);
    }

    @Test
    void k7TransitionsCoverZeroAndHighestStateForBothInputs() {
        assertStep("k7", 0, 0, 0, 0, 0);
        assertStep("k7", 0, 1, 32, 1, 1);
        assertStep("k7", 63, 0, 31, 0, 0);
        assertStep("k7", 63, 1, 63, 1, 1);
    }

    @Test
    void encodingIncludesTheExactTailForTheDocumentedK3Message() {
        assertPairs("11 10 00 01 01 11", Conv.encode(code("k3"), bits("1011")));
    }

    @Test
    void encodingOneBitAddsExactlyTheRequiredTailForEveryCode() {
        assertPairs("11 10 11", Conv.encode(code("k3"), bits("1")));
        assertPairs("11 01 01 10 11", Conv.encode(code("k5"), bits("1")));
        assertPairs("11 10 11 11 00 01 11", Conv.encode(code("k7"), bits("1")));
    }

    @Test
    void maximumLengthLiteralMessageEncodesWithoutDroppingItsTail() {
        int[] message = bits(MAX_ZERO_BITS);
        assertEquals(200, message.length);

        List<int[]> encoded = Conv.encode(code("k3"), message);
        assertEquals(202, encoded.size());
        assertArrayEquals(new int[]{0, 0}, encoded.get(0));
        assertArrayEquals(new int[]{0, 0}, encoded.get(201));
    }

    @Test
    void decodingTheShortestLegalReceivedSequenceForEachCodeRemovesOnlyTheTail() {
        assertDecoded("k3", "111011", "1", 0);
        assertDecoded("k5", "1101011011", "1", 0);
        assertDecoded("k7", "11101111000111", "1", 0);
    }

    @Test
    void decodingCleanAndCorruptedK3ExamplesReturnsMessageAndChannelMetric() {
        assertDecoded("k3", "111000010111", "1011", 0);
        assertDecoded("k3", "111100010111", "1011", 1);
    }

    @Test
    void decodingCorrectsOneChannelBitForK5AndK7() {
        assertDecoded("k5", "0101011011", "1", 1);
        assertDecoded("k7", "01101111000111", "1", 1);
    }

    @Test
    void equalMetricPathsUseTheLowerPredecessorAndReturnTheSpecifiedMessage() {
        assertDecoded("k3", "00000111", "00", 3);
    }

    @Test
    void maximumLengthLiteralReceivedSequenceDecodesAtTheUpperBoundary() {
        String receivedBits = MAX_ZERO_BITS + MAX_ZERO_BITS;
        assertEquals(400, receivedBits.length());

        Conv.Decoded decoded = Conv.decode(code("k3"), receivedPairs(receivedBits));
        assertEquals(198, decoded.message().length);
        assertEquals(0, decoded.errors());
    }

    private static Conv.Code code(String name) {
        return Conv.byName(name).orElseThrow();
    }

    private static void assertStep(
            String codeName, int state, int input, int nextState, int output0, int output1) {
        assertArrayEquals(
                new int[]{nextState, output0, output1},
                Conv.step(code(codeName), state, input),
                codeName + " state " + state + " input " + input);
    }

    private static void assertDecoded(
            String codeName, String received, String expectedMessage, int expectedErrors) {
        Conv.Decoded decoded = Conv.decode(code(codeName), receivedPairs(received));
        assertArrayEquals(bits(expectedMessage), decoded.message(),
                codeName + " decoded message from " + received);
        assertEquals(expectedErrors, decoded.errors(),
                codeName + " channel errors from " + received);
    }

    private static void assertPairs(String expected, List<int[]> actual) {
        List<int[]> expectedPairs = receivedPairs(expected);
        assertEquals(expectedPairs.size(), actual.size(), "pair count for " + expected);
        for (int index = 0; index < expectedPairs.size(); index++) {
            assertArrayEquals(expectedPairs.get(index), actual.get(index),
                    "pair " + index + " for " + expected);
        }
    }

    private static List<int[]> receivedPairs(String pairText) {
        String compact = pairText.replace(" ", "");
        if ((compact.length() & 1) != 0) {
            throw new IllegalArgumentException("literal pair fixture must have an even number of bits");
        }

        java.util.ArrayList<int[]> pairs = new java.util.ArrayList<>(compact.length() / 2);
        for (int index = 0; index < compact.length(); index += 2) {
            pairs.add(new int[]{
                    compact.charAt(index) - '0',
                    compact.charAt(index + 1) - '0'});
        }
        return pairs;
    }

    private static int[] bits(String value) {
        int[] result = new int[value.length()];
        for (int index = 0; index < value.length(); index++) {
            result[index] = value.charAt(index) - '0';
        }
        return result;
    }
}
