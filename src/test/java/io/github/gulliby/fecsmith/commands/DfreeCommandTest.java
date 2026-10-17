package io.github.gulliby.fecsmith.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gulliby.fecsmith.Cli;
import io.github.gulliby.fecsmith.core.Io;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DfreeCommandTest {
    @Test
    void rejectsMissingCode() {
        assertUsage(List.of("dfree"), "missing CODE");
    }

    @Test
    void rejectsExcessArgumentsBeforeLookingUpCode() {
        assertUsage(List.of("dfree", "bad", "extra"), "too many arguments");
        assertUsage(List.of("dfree", "unknown", "extra"), "too many arguments");
        assertUsage(List.of("dfree", "k3", "extra"), "too many arguments");
    }

    @Test
    void rejectsEveryOptionBecauseDfreeHasNoOptions() {
        assertUsage(List.of("dfree", "--unknown"), "unknown option \"--unknown\"");
        assertUsage(List.of("dfree", "k3", "--help"), "unknown option \"--help\"");
        assertUsage(List.of("dfree", "--", "k3"), "unknown option \"--\"");
    }

    @Test
    void optionParsingPrecedesMissingAndExcessOperandChecks() {
        assertUsage(List.of("dfree", "--unknown", "k3", "extra"), "unknown option \"--unknown\"");
        assertUsage(List.of("dfree", "--unknown", "extra"), "unknown option \"--unknown\"");
    }

    @Test
    void sanitizesUnknownUnicodeAndControlTextInTheDiagnostic() {
        assertUsage(List.of("dfree", "é😀\n"), "unknown code \"???\" (expected k3, k5 or k7)");
    }

    @Test
    void rejectsUnknownCodeWithTheExactExpectedNames() {
        assertUsage(List.of("dfree", "bad"), "unknown code \"bad\" (expected k3, k5 or k7)");
        assertUsage(List.of("dfree", "K3"), "unknown code \"K3\" (expected k3, k5 or k7)");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("weights/outcomes with no K")
    void rejectsMissingWeightsAndOutcomesOperands() {
        assertUsage(List.of("weights"), "missing K");
        assertUsage(List.of("outcomes"), "missing K");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("weights/outcomes with two operands")
    void rejectsExcessWeightsAndOutcomesOperands() {
        assertUsage(List.of("weights", "4", "5"), "too many arguments");
        assertUsage(List.of("outcomes", "4", "5"), "too many arguments");
    }

    @Test
    void rejectsOutOfRangeAnalysisKValues() {
        assertUsage(List.of("weights", "0"), "invalid K \"0\" (expected an integer from 1 to 12)");
        assertUsage(List.of("outcomes", "65"), "invalid K \"65\" (expected an integer from 1 to 64)");
    }

    @Test
    void rejectsMissingAndExcessiveBurstOperands() {
        assertUsage(List.of("burst"), "missing MODEL");
        assertUsage(List.of("burst", "bad", "extra"), "too many arguments");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("burst bad or malformed crc: spec")
    void rejectsUnknownAndMalformedBurstModels() {
        assertUsage(List.of("burst", "bad"),
                "unknown model \"bad\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)");
        assertUsage(List.of("burst", "crc:8:10:00:0:0:00"),
                "invalid crc spec \"crc:8:10:00:0:0:00\" (POLY must be odd and below 2^WIDTH)");
    }

    @Test
    void preservesTheOtherAnalysisCommandsNamedWorkedOutputs() {
        assertSuccess(List.of("weights", "4"),
                "secded k=4 r=3 n=8, minimum distance 4\n"
                        + "weight  codewords\n"
                        + "0       1\n"
                        + "4       14\n"
                        + "8       1\n");
        assertSuccess(List.of("outcomes", "5"),
                "secded k=5 r=4 n=10\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       10        10         0         0\n"
                        + "2       45        0          45        0\n"
                        + "3       120       0          48        72\n");
        assertSuccess(List.of("burst", "--max-len", "10", "crc8"),
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
                        + "10      256       1\n");
    }

    @Test
    void printsTheCompleteK3FirstReturnWitness() {
        assertSuccess(List.of("dfree", "k3"),
                "k3 (K=3, generators 7 and 5 octal): free distance 5\n"
                        + "input     100\n"
                        + "coded     11 10 11\n");
    }

    @Test
    void rejectsTheLiteralDfreeUnknownCodeAfterArity() {
        assertUsage(List.of("dfree", "bad", "extra"), "too many arguments");
        assertUsage(List.of("dfree", "bad"), "unknown code \"bad\" (expected k3, k5 or k7)");
    }

    @Test
    void printsTheCompleteK5FirstReturnWitness() {
        assertSuccess(List.of("dfree", "k5"),
                "k5 (K=5, generators 23 and 35 octal): free distance 7\n"
                        + "input     10000\n"
                        + "coded     11 01 01 10 11\n");
    }

    @Test
    void printsTheCompleteK7FirstReturnWitness() {
        assertSuccess(List.of("dfree", "k7"),
                "k7 (K=7, generators 171 and 133 octal): free distance 10\n"
                        + "input     1000000\n"
                        + "coded     11 10 11 11 00 01 11\n");
    }

    private static void assertSuccess(List<String> argv, String expectedOutput) {
        assertRun(argv, 0, expectedOutput, "");
    }

    private static void assertUsage(List<String> argv, String message) {
        assertRun(argv, 2, "", "fecsmith: " + message + "\n");
    }

    private static void assertRun(List<String> argv, int expectedStatus, String expectedOutput, String expectedError) {
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
            throw new AssertionError("dfree must not read standard input");
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
