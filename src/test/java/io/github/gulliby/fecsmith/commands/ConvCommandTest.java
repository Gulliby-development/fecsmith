package io.github.gulliby.fecsmith.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gulliby.fecsmith.Cli;
import io.github.gulliby.fecsmith.core.Io;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConvCommandTest {
    private static final String ZERO_20 = "00000000000000000000";
    private static final String ZERO_200 =
            ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20
                    + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20;
    private static final String ZERO_400 =
            ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20
                    + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20
                    + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20
                    + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20;
    private static final String ZERO_198 =
            ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20
                    + ZERO_20 + ZERO_20 + ZERO_20 + ZERO_20
                    + "000000000000000000";
    private static final String ZERO_PAIRS_20 =
            "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ";
    private static final String ZERO_PAIRS_202 =
            ZERO_PAIRS_20 + ZERO_PAIRS_20 + ZERO_PAIRS_20 + ZERO_PAIRS_20 + ZERO_PAIRS_20
                    + ZERO_PAIRS_20 + ZERO_PAIRS_20 + ZERO_PAIRS_20 + ZERO_PAIRS_20 + ZERO_PAIRS_20
                    + "00 00";

    @Test
    void checksModeAndOptionErrorsBeforeCommandArguments() {
        assertUsage(List.of("conv"), "missing mode (encode or decode)");
        assertUsage(List.of("conv", "ENCODE"), "unknown mode \"ENCODE\" (expected encode or decode)");
        assertUsage(List.of("conv", "déc"), "unknown mode \"d?c\" (expected encode or decode)");
        assertUsage(List.of("conv", "--help"), "unknown option \"--help\"");
        assertUsage(List.of("conv", "--", "encode"), "unknown option \"--\"");
        assertUsage(List.of("conv", "--bad", "encode", "k3", "1"), "unknown option \"--bad\"");
    }

    @Test
    void checksModeArityAndCodeBeforeBitSyntax() {
        assertUsage(List.of("conv", "encode"), "missing CODE");
        assertUsage(List.of("conv", "decode"), "missing CODE");
        assertUsage(List.of("conv", "encode", "k3"), "missing BITS");
        assertUsage(List.of("conv", "decode", "k3"), "missing BITS");
        assertUsage(List.of("conv", "encode", "bad", "x", "extra"), "too many arguments");
        assertUsage(List.of("conv", "encode", "bad", "x"), "unknown code \"bad\" (expected k3, k5 or k7)");
        assertUsage(List.of("conv", "decode", "k4", "x"), "unknown code \"k4\" (expected k3, k5 or k7)");
        assertUsage(List.of("conv", "encode", "ké", "x"), "unknown code \"k?\" (expected k3, k5 or k7)");
    }

    @Test
    void validatesBitSyntaxAndLengthBoundariesInOrder() {
        assertUsage(List.of("conv", "encode", "k3", ""), "invalid bit string \"\" (expected 1 to 200 digits 0 or 1)");
        assertUsage(List.of("conv", "encode", "k3", "0x"), "invalid bit string \"0x\" (expected 1 to 200 digits 0 or 1)");
        assertUsage(List.of("conv", "encode", "k3", "1\n0"),
                "invalid bit string \"1?0\" (expected 1 to 200 digits 0 or 1)");
        assertUsage(List.of("conv", "encode", "k3", ZERO_200 + "0"),
                "invalid bit string \"" + ZERO_200 + "0\" (expected 1 to 200 digits 0 or 1)");
        assertUsage(List.of("conv", "decode", "k3", ""),
                "invalid bit string \"\" (expected 2 to 400 digits 0 or 1)");
        assertUsage(List.of("conv", "decode", "k3", "0x"),
                "invalid bit string \"0x\" (expected 2 to 400 digits 0 or 1)");
        assertUsage(List.of("conv", "decode", "k3", ZERO_400 + "00"),
                "invalid bit string \"" + ZERO_400 + "00\" (expected 2 to 400 digits 0 or 1)");
        assertUsage(List.of("conv", "decode", "k3", "00"),
                "invalid received length 2 for k3 (expected an even number from 6 to 400)");
        assertUsage(List.of("conv", "decode", "k3", "0000001"),
                "invalid received length 7 for k3 (expected an even number from 6 to 400)");
        assertUsage(List.of("conv", "decode", "k5", "00000000"),
                "invalid received length 8 for k5 (expected an even number from 10 to 400)");
        assertUsage(List.of("conv", "decode", "k7", "000000000000"),
                "invalid received length 12 for k7 (expected an even number from 14 to 400)");
    }

    @Test
    void encodesTheWorkedExampleAndOneBitInputsForEveryCode() {
        assertSuccess(List.of("conv", "encode", "k3", "1011"),
                "conv k3 (K=3, generators 7 and 5 octal)\n"
                        + "input     1011\n"
                        + "coded     11 10 00 01 01 11\n");
        assertSuccess(List.of("conv", "encode", "k3", "1"),
                "conv k3 (K=3, generators 7 and 5 octal)\n"
                        + "input     1\n"
                        + "coded     11 10 11\n");
        assertSuccess(List.of("conv", "encode", "k5", "1"),
                "conv k5 (K=5, generators 23 and 35 octal)\n"
                        + "input     1\n"
                        + "coded     11 01 01 10 11\n");
        assertSuccess(List.of("conv", "encode", "k7", "1"),
                "conv k7 (K=7, generators 171 and 133 octal)\n"
                        + "input     1\n"
                        + "coded     11 10 11 11 00 01 11\n");
    }

    @Test
    void acceptsMaximumEncodeLengthEvenThoughTheTailMakesMoreThanFourHundredBits() {
        assertSuccess(List.of("conv", "encode", "k3", ZERO_200),
                "conv k3 (K=3, generators 7 and 5 octal)\n"
                        + "input     " + ZERO_200 + "\n"
                        + "coded     " + ZERO_PAIRS_202 + "\n");
    }

    @Test
    void decodesTheMinimumValidReceivedLengthForEachCode() {
        assertSuccess(List.of("conv", "decode", "k3", "000000"),
                "conv k3 (K=3, generators 7 and 5 octal)\n"
                        + "status    ok\n"
                        + "message   0\n");
        assertSuccess(List.of("conv", "decode", "k5", "0000000000"),
                "conv k5 (K=5, generators 23 and 35 octal)\n"
                        + "status    ok\n"
                        + "message   0\n");
        assertSuccess(List.of("conv", "decode", "k7", "00000000000000"),
                "conv k7 (K=7, generators 171 and 133 octal)\n"
                        + "status    ok\n"
                        + "message   0\n");
    }

    @Test
    void acceptsMaximumDecodeLengthAndRejectsTheNextLengthAtSyntaxValidation() {
        assertSuccess(List.of("conv", "decode", "k3", ZERO_400),
                "conv k3 (K=3, generators 7 and 5 octal)\n"
                        + "status    ok\n"
                        + "message   " + ZERO_198 + "\n");
    }

    @Test
    void decodesCleanAndCorrectedWorkedExamplesIncludingThePredecessorTie() {
        assertSuccess(List.of("conv", "decode", "k3", "111100010111"),
                "conv k3 (K=3, generators 7 and 5 octal)\n"
                        + "status    corrected 1 channel bit error\n"
                        + "message   1011\n");
        assertSuccess(List.of("conv", "decode", "k3", "00000111"),
                "conv k3 (K=3, generators 7 and 5 octal)\n"
                        + "status    corrected 3 channel bit errors\n"
                        + "message   00\n");
        assertSuccess(List.of("conv", "decode", "k3", "000000"),
                "conv k3 (K=3, generators 7 and 5 octal)\n"
                        + "status    ok\n"
                        + "message   0\n");
    }

    @Test
    void cleanDecodeHasTheExactHeadersForK5AndK7() {
        assertSuccess(List.of("conv", "decode", "k5", "1101011011"),
                "conv k5 (K=5, generators 23 and 35 octal)\n"
                        + "status    ok\n"
                        + "message   1\n");
        assertSuccess(List.of("conv", "decode", "k7", "11101111000111"),
                "conv k7 (K=7, generators 171 and 133 octal)\n"
                        + "status    ok\n"
                        + "message   1\n");
    }

    private static void assertSuccess(List<String> argv, String expectedOutput) {
        assertRun(argv, 0, expectedOutput, "");
    }

    private static void assertUsage(List<String> argv, String message) {
        assertRun(argv, 2, "", "fecsmith: " + message + "\n");
    }

    private static void assertRun(List<String> argv, int expectedStatus, String expectedOutput,
            String expectedError) {
        FakeIo io = new FakeIo();

        int status = Cli.run(argv, io);

        assertEquals(expectedStatus, status);
        assertEquals(expectedOutput.isEmpty() ? List.of() : List.of(expectedOutput), io.outputs);
        assertEquals(expectedError.isEmpty() ? List.of() : List.of(expectedError), io.errors);
        assertEquals(0, io.reads);
    }

    private static final class FakeIo implements Io {
        private final ArrayList<String> outputs = new ArrayList<>();
        private final ArrayList<String> errors = new ArrayList<>();
        private int reads;

        @Override
        public byte[] readStdin() throws IOException {
            reads++;
            return new byte[0];
        }

        @Override
        public void out(String text) {
            outputs.add(text);
        }

        @Override
        public void err(String text) {
            errors.add(text);
        }
    }
}
