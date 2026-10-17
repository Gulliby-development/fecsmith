package io.github.gulliby.fecsmith.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gulliby.fecsmith.Cli;
import io.github.gulliby.fecsmith.core.Io;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SecdedCommandTest {
    private static final byte[] NO_STDIN = {};
    private static final String ZERO_64 =
            "00000000" + "00000000" + "00000000" + "00000000"
                    + "00000000" + "00000000" + "00000000" + "00000000";
    private static final String ZERO_120 =
            "000000000000" + "000000000000" + "000000000000" + "000000000000"
                    + "000000000000" + "000000000000" + "000000000000" + "000000000000"
                    + "000000000000" + "000000000000";
    private static final String ZERO_128 = ZERO_120 + "00000000";

    @Test
    void reportsMissingAndUnknownModesBeforeCheckingBits() {
        assertUsage(List.of("secded"), "missing mode (encode or decode)");
        assertUsage(List.of("secded", "ENCODE"), "unknown mode \"ENCODE\" (expected encode or decode)");
        assertUsage(List.of("secded", "déc"), "unknown mode \"d?c\" (expected encode or decode)");
        assertUsage(List.of("secded", "--help"), "unknown option \"--help\"");
        assertUsage(List.of("secded", "--", "encode"), "unknown option \"--\"");
    }

    @Test
    void checksModeArityBeforeBitSyntax() {
        assertUsage(List.of("secded", "encode"), "missing BITS");
        assertUsage(List.of("secded", "decode"), "missing BITS");
        assertUsage(List.of("secded", "encode", "bad", "extra"), "too many arguments");
        assertUsage(List.of("secded", "encode", "x", "extra"), "too many arguments");
        assertUsage(List.of("secded", "decode", "bad", "extra"), "too many arguments");
        assertUsage(List.of("secded", "encode", "bad", "extra", "more"), "too many arguments");
    }

    @Test
    void validatesEncodeAndDecodeBitStringDomainsAndPrecedesLengthLookup() {
        assertUsage(List.of("secded", "encode", ""), "invalid bit string \"\" (expected 1 to 120 digits 0 or 1)");
        assertUsage(List.of("secded", "encode", "x"), "invalid bit string \"x\" (expected 1 to 120 digits 0 or 1)");
        assertUsage(List.of("secded", "decode", "x"), "invalid bit string \"x\" (expected 4 to 128 digits 0 or 1)");
        assertUsage(List.of("secded", "encode", "2"), "invalid bit string \"2\" (expected 1 to 120 digits 0 or 1)");
        assertUsage(List.of("secded", "encode", "10x1"), "invalid bit string \"10x1\" (expected 1 to 120 digits 0 or 1)");
        assertUsage(List.of("secded", "encode", "1\n0"), "invalid bit string \"1?0\" (expected 1 to 120 digits 0 or 1)");
        assertUsage(List.of("secded", "encode", ZERO_120 + "0"),
                "invalid bit string \"" + ZERO_120 + "0\" (expected 1 to 120 digits 0 or 1)");
        assertUsage(List.of("secded", "decode", "000"), "invalid bit string \"000\" (expected 4 to 128 digits 0 or 1)");
        assertUsage(List.of("secded", "decode", "00x0"), "invalid bit string \"00x0\" (expected 4 to 128 digits 0 or 1)");
        assertUsage(List.of("secded", "decode", ZERO_128 + "0"),
                "invalid bit string \"" + ZERO_128 + "0\" (expected 4 to 128 digits 0 or 1)");
        assertUsage(List.of("secded", "decode", "00000"),
                "invalid codeword length 5 (no data size has this length)");
        assertUsage(List.of("secded", "decode", "01101"),
                "invalid codeword length 5 (no data size has this length)");
        assertSuccess(List.of("secded", "decode", "0000000"),
                "secded k=3 r=3 n=7\n"
                        + "syndrome  0\n"
                        + "parity    even\n"
                        + "status    ok\n"
                        + "codeword  0000000\n"
                        + "data      000\n");
        assertUsage(List.of("secded", "decode", "000000000"),
                "invalid codeword length 9 (no data size has this length)");
        assertUsage(List.of("secded", "decode", ZERO_64 + "0"),
                "invalid codeword length 65 (no data size has this length)");
    }

    @Test
    void encodesTheWorkedExampleAndTheOneBitBoundary() {
        assertSuccess(List.of("secded", "encode", "10110"),
                "secded k=5 r=4 n=10\n"
                        + "data      10110\n"
                        + "codeword  0011001100\n");
        assertSuccess(List.of("secded", "encode", "0"),
                "secded k=1 r=2 n=4\n"
                        + "data      0\n"
                        + "codeword  0000\n");
        assertSuccess(List.of("secded", "encode", "1"),
                "secded k=1 r=2 n=4\n"
                        + "data      1\n"
                        + "codeword  1111\n");
    }

    @Test
    void acceptsTheMaximumEncodeBoundaryAndKeepsTheWholeOutputAtomic() {
        assertSuccess(List.of("secded", "encode", ZERO_120),
                "secded k=120 r=7 n=128\n"
                        + "data      " + ZERO_120 + "\n"
                        + "codeword  " + ZERO_128 + "\n");
    }

    @Test
    void decodesTheMinimumAndIntermediateSupportedLayouts() {
        assertSuccess(List.of("secded", "decode", "0000"),
                "secded k=1 r=2 n=4\n"
                        + "syndrome  0\n"
                        + "parity    even\n"
                        + "status    ok\n"
                        + "codeword  0000\n"
                        + "data      0\n");
        assertSuccess(List.of("secded", "decode", "000000"),
                "secded k=2 r=3 n=6\n"
                        + "syndrome  0\n"
                        + "parity    even\n"
                        + "status    ok\n"
                        + "codeword  000000\n"
                        + "data      00\n");
        assertSuccess(List.of("secded", "decode", "00000000"),
                "secded k=4 r=3 n=8\n"
                        + "syndrome  0\n"
                        + "parity    even\n"
                        + "status    ok\n"
                        + "codeword  00000000\n"
                        + "data      0000\n");
    }

    @Test
    void acceptsTheMaximumDecodeBoundaryWithExactResultFields() {
        assertSuccess(List.of("secded", "decode", ZERO_128),
                "secded k=120 r=7 n=128\n"
                        + "syndrome  0\n"
                        + "parity    even\n"
                        + "status    ok\n"
                        + "codeword  " + ZERO_128 + "\n"
                        + "data      " + ZERO_120 + "\n");
    }

    @Test
    void printsTheWorkedCleanAndCorrectedDecodeExamples() {
        assertSuccess(List.of("secded", "decode", "0011001100"),
                "secded k=5 r=4 n=10\n"
                        + "syndrome  0\n"
                        + "parity    even\n"
                        + "status    ok\n"
                        + "codeword  0011001100\n"
                        + "data      10110\n");
        assertSuccess(List.of("secded", "decode", "0010001100"),
                "secded k=5 r=4 n=10\n"
                        + "syndrome  3\n"
                        + "parity    odd\n"
                        + "status    corrected bit 3\n"
                        + "codeword  0011001100\n"
                        + "data      10110\n");
    }

    @Test
    void correctsTheOverallParityBitAtPositionZero() {
        assertSuccess(List.of("secded", "decode", "1011001100"),
                "secded k=5 r=4 n=10\n"
                        + "syndrome  0\n"
                        + "parity    odd\n"
                        + "status    corrected bit 0\n"
                        + "codeword  0011001100\n"
                        + "data      10110\n");
    }

    @Test
    void correctsAParityPositionAndADataPosition() {
        assertSuccess(List.of("secded", "decode", "0111001100"),
                "secded k=5 r=4 n=10\n"
                        + "syndrome  1\n"
                        + "parity    odd\n"
                        + "status    corrected bit 1\n"
                        + "codeword  0011001100\n"
                        + "data      10110\n");
        assertSuccess(List.of("secded", "decode", "0011101100"),
                "secded k=5 r=4 n=10\n"
                        + "syndrome  4\n"
                        + "parity    odd\n"
                        + "status    corrected bit 4\n"
                        + "codeword  0011001100\n"
                        + "data      10110\n");
    }

    @Test
    void doubleErrorsEmitOnlyTheNegativeStatusAndReturnOne() {
        assertNegative(List.of("secded", "decode", "0001001000"),
                "secded k=5 r=4 n=10\n"
                        + "syndrome  5\n"
                        + "parity    even\n"
                        + "status    double error detected\n");
        assertNegative(List.of("secded", "decode", "0110000000"),
                "secded k=5 r=4 n=10\n"
                        + "syndrome  3\n"
                        + "parity    even\n"
                        + "status    double error detected\n");
    }

    @Test
    void convolutionalCommandFixturesCoverTheSpecifiedDispatchAndWorkedExamples() {
        assertUsage(List.of("conv"), "missing mode (encode or decode)");
        assertUsage(List.of("conv", "bad"), "unknown mode \"bad\" (expected encode or decode)");
        assertUsage(List.of("conv", "encode"), "missing CODE");
        assertUsage(List.of("conv", "encode", "bad"), "missing BITS");
        assertUsage(List.of("conv", "encode", "bad", "x", "extra"), "too many arguments");
        assertUsage(List.of("conv", "encode", "bad", "x"),
                "unknown code \"bad\" (expected k3, k5 or k7)");
        assertUsage(List.of("conv", "encode", "k3", "x"),
                "invalid bit string \"x\" (expected 1 to 200 digits 0 or 1)");
        assertUsage(List.of("conv", "decode", "k3", "x"),
                "invalid bit string \"x\" (expected 2 to 400 digits 0 or 1)");
        assertUsage(List.of("conv", "decode", "k3", "00"),
                "invalid received length 2 for k3 (expected an even number from 6 to 400)");
        assertUsage(List.of("conv", "decode", "k3", "0000001"),
                "invalid received length 7 for k3 (expected an even number from 6 to 400)");
        assertSuccess(List.of("conv", "encode", "k3", "1011"),
                "conv k3 (K=3, generators 7 and 5 octal)\n"
                        + "input     1011\n"
                        + "coded     11 10 00 01 01 11\n");
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
    void oddDecodeLengthHasTheExactUsageResult() {
        assertUsage(List.of("conv", "decode", "k3", "0000001"),
                "invalid received length 7 for k3 (expected an even number from 6 to 400)");
    }

    @Test
    void cleanDecodeShowsOkForK3K5AndK7() {
        assertSuccess(List.of("conv", "decode", "k3", "000000"),
                "conv k3 (K=3, generators 7 and 5 octal)\n"
                        + "status    ok\n"
                        + "message   0\n");
        assertSuccess(List.of("conv", "decode", "k5", "1101011011"),
                "conv k5 (K=5, generators 23 and 35 octal)\n"
                        + "status    ok\n"
                        + "message   1\n");
        assertSuccess(List.of("conv", "decode", "k7", "11101111000111"),
                "conv k7 (K=7, generators 171 and 133 octal)\n"
                        + "status    ok\n"
                        + "message   1\n");
    }

    @Test
    void outsideSyndromesEmitOnlyTheNegativeStatusAndReturnOne() {
        assertNegative(List.of("secded", "decode", "1001001110"),
                "secded k=5 r=4 n=10\n"
                        + "syndrome  10\n"
                        + "parity    odd\n"
                        + "status    uncorrectable (syndrome 10 is outside the word)\n");
    }

    private static void assertSuccess(List<String> argv, String expectedOutput) {
        assertRun(argv, 0, expectedOutput, "", 0);
    }

    private static void assertNegative(List<String> argv, String expectedOutput) {
        assertRun(argv, 1, expectedOutput, "", 0);
    }

    private static void assertUsage(List<String> argv, String message) {
        assertRun(argv, 2, "", "fecsmith: " + message + "\n", 0);
    }

    private static void assertRun(List<String> argv, int expectedStatus, String expectedOutput,
            String expectedError, int expectedReads) {
        FakeIo io = new FakeIo();

        int status = Cli.run(argv, io);

        assertEquals(expectedStatus, status);
        assertEquals(expectedOutput.isEmpty() ? List.of() : List.of(expectedOutput), io.outputs);
        assertEquals(expectedError.isEmpty() ? List.of() : List.of(expectedError), io.errors);
        assertEquals(expectedReads, io.reads);
    }

    private static final class FakeIo implements Io {
        private final ArrayList<String> outputs = new ArrayList<>();
        private final ArrayList<String> errors = new ArrayList<>();
        private int reads;

        @Override
        public byte[] readStdin() throws IOException {
            reads++;
            return NO_STDIN;
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
