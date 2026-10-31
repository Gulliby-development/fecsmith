package io.github.gulliby.fecsmith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gulliby.fecsmith.core.Io;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CliTest {
    private static final String USAGE = "usage: fecsmith crc [--list] MODEL HEX...\n"
            + "       fecsmith secded encode|decode BITS\n"
            + "       fecsmith rs encode|decode [--m M] [--nsym N] SYMBOL...\n"
            + "       fecsmith conv encode|decode CODE BITS\n"
            + "       fecsmith weights [--cumulative] K\n"
            + "       fecsmith outcomes [--errors N] K\n"
            + "       fecsmith burst [--max-len N] MODEL\n"
            + "       fecsmith dfree CODE";

    @Test
    @org.junit.jupiter.api.DisplayName("[--help]")
    void standaloneHelpPrintsTheExactUsageOnce() {
        assertRun(List.of("--help"), 0, USAGE + "\n", "", 0);
    }

    @Test
    @org.junit.jupiter.api.DisplayName("[--version]")
    void standaloneVersionPrintsTheBuildVersionOnce() {
        assertRun(List.of("--version"), 0, "fecsmith 0.1.0\n", "", 0);
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Non-standalone --version")
    void versionIsNotSpecialWithOtherArgumentsOrAssignment() {
        assertRun(List.of("--version", "crc"), 2, "",
                "fecsmith: unknown command \"--version\"\n");
        assertRun(List.of("weights", "--version"), 2, "",
                "fecsmith: unknown option \"--version\"\n");
        assertRun(List.of("--version=1"), 2, "",
                "fecsmith: unknown command \"--version=1\"\n");
    }

    @Test
    void missingCommandHasTheExactDiagnosticAndNoOutput() {
        assertRun(List.of(), 2, "",
                "fecsmith: missing command (crc, secded, rs, conv, weights, outcomes, burst or dfree)\n", 0);
    }

    @Test
    @org.junit.jupiter.api.DisplayName("[unknown]")
    void unknownCommandIsQuotedAndHasNoOutput() {
        assertRun(List.of("unknown"), 2, "fecsmith: unknown command \"unknown\"\n");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Unknown Unicode/control text")
    void unknownCommandSanitizesEveryNonAsciiAndControlCodePoint() {
        assertRun(List.of("é😀\t"), 2, "fecsmith: unknown command \"???\"\n");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("[--help,crc]")
    void helpIsOnlySpecialWhenItIsTheSoleArgument() {
        assertRun(List.of("--help", "crc"), 2, "fecsmith: unknown command \"--help\"\n");
        assertRun(List.of("weights", "--help"), 2, "fecsmith: unknown option \"--help\"\n");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Each valid command: crc")
    void dispatchesCrcAndPreservesTheWorkedOutput() {
        assertRun(List.of("crc", "crc32", "313233343536373839"), 0,
                "crc32 over 9 bytes: cbf43926\n", "");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Each valid command: secded")
    void dispatchesSecdedAndPreservesCorrectedStatusAndOutput() {
        assertRun(List.of("secded", "decode", "0010001100"), 0,
                "secded k=5 r=4 n=10\n"
                        + "syndrome  3\n"
                        + "parity    odd\n"
                        + "status    corrected bit 3\n"
                        + "codeword  0011001100\n"
                        + "data      10110\n", "");
    }

    @Test
    void propagatesTheSecdedNegativeExitWithItsCompleteStatusOutput() {
        assertRun(List.of("secded", "decode", "0001001000"), 1,
                "secded k=5 r=4 n=10\n"
                        + "syndrome  5\n"
                        + "parity    even\n"
                        + "status    double error detected\n", "");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Each valid command: rs")
    void dispatchesRsAndPreservesTheWorkedEncoding() {
        assertRun(List.of("rs", "encode", "48", "65", "6c", "6c", "6f"), 0,
                "RS(9,5) over GF(2^8), t=2\n"
                        + "codeword  48 65 6c 6c 6f 08 47 90 ca\n"
                        + "parity    08 47 90 ca\n", "");
    }

    @Test
    void propagatesTheRsNegativeExitWithoutPrintingAfterStatus() {
        assertRun(List.of("rs", "decode", "48", "65", "6c", "6c", "6f", "9b", "6e", "3a", "6c"), 1,
                "RS(9,5) over GF(2^8), t=2\n"
                        + "syndrome  a7 ad 88 fa\n"
                        + "status    uncorrectable\n", "");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Each valid command: conv")
    void dispatchesConvAndPreservesItsTerminatedEncoding() {
        assertRun(List.of("conv", "encode", "k3", "1011"), 0,
                "conv k3 (K=3, generators 7 and 5 octal)\n"
                        + "input     1011\n"
                        + "coded     11 10 00 01 01 11\n", "");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Each valid command: weights")
    void dispatchesWeightsAndPreservesTheWorkedTable() {
        assertRun(List.of("weights", "4"), 0,
                "secded k=4 r=3 n=8, minimum distance 4\n"
                        + "weight  codewords\n"
                        + "0       1\n"
                        + "4       14\n"
                        + "8       1\n", "");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Each valid command: outcomes")
    void dispatchesOutcomesAndPreservesTheWorkedTable() {
        assertRun(List.of("outcomes", "5"), 0,
                "secded k=5 r=4 n=10\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       10        10         0         0\n"
                        + "2       45        0          45        0\n"
                        + "3       120       0          48        72\n", "");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Each valid command: burst")
    void dispatchesBurstAndPreservesTheWorkedTable() {
        assertRun(List.of("burst", "--max-len", "10", "crc8"), 0,
                "crc8, width 8, bursts up to length 10\n"
                        + "length  patterns  undetected\n"
                        + "1       1         0\n"
                        + "2       1         0\n"
                        + "3       2         0\n"
                        + "4       4         0\n"
                        + "5       8         0\n"
                        + "6       16        0\n"
                        + "7       32        0\n"
                        + "8       64        0\n"
                        + "9       128       1\n"
                        + "10      256       1\n", "");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Each valid command: dfree")
    void dispatchesDfreeAndPreservesItsFirstReturnWitness() {
        assertRun(List.of("dfree", "k3"), 0,
                "k3 (K=3, generators 7 and 5 octal): free distance 5\n"
                        + "input     100\n"
                        + "coded     11 10 11\n", "");
    }

    @Test
    void wrapsCommandUsageErrorsAndKeepsStdoutEmpty() {
        assertRun(List.of("weights", "0"), 2, "",
                "fecsmith: invalid K \"0\" (expected an integer from 1 to 12)\n", 0);
    }

    private static void assertRun(List<String> argv, int expectedStatus, String expectedOutput, String expectedError) {
        assertRun(argv, expectedStatus, expectedOutput, expectedError, 0);
    }

    private static void assertRun(List<String> argv, int expectedStatus, String expectedOutput, String expectedError,
            int expectedReads) {
        FakeIo io = new FakeIo();

        int status = Cli.run(argv, io);

        assertEquals(expectedStatus, status);
        assertEquals(expectedOutput.isEmpty() ? List.of() : List.of(expectedOutput), io.outputs);
        assertEquals(expectedError.isEmpty() ? List.of() : List.of(expectedError), io.errors);
        assertEquals(expectedReads, io.reads);
    }

    private static void assertRun(List<String> argv, int expectedStatus, String expectedError) {
        assertRun(argv, expectedStatus, "", expectedError);
    }

    private static final class FakeIo implements Io {
        private final ArrayList<String> outputs = new ArrayList<>();
        private final ArrayList<String> errors = new ArrayList<>();
        private int reads;

        @Override
        public byte[] readStdin() throws IOException {
            reads++;
            throw new AssertionError("Cli test commands must not read standard input");
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
