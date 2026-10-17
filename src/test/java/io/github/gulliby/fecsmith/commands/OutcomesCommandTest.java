package io.github.gulliby.fecsmith.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gulliby.fecsmith.Cli;
import io.github.gulliby.fecsmith.core.Io;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OutcomesCommandTest {
    @Test
    void checksMissingAndExcessiveArgumentsBeforeValidatingK() {
        assertUsage(List.of("outcomes"), "missing K");
        assertUsage(List.of("outcomes", "5", "6"), "too many arguments");
        assertUsage(List.of("outcomes", "not-a-number", "extra"), "too many arguments");
        assertUsage(List.of("outcomes", "0", "extra"), "too many arguments");
        assertUsage(List.of("outcomes", "5", "--errors=0"),
                "invalid --errors \"0\" (expected an integer from 1 to 3)");
    }

    @Test
    void rejectsUnknownOptionsBeforeDeferredIntegerValidation() {
        assertUsage(List.of("outcomes", "--unknown", "5"), "unknown option \"--unknown\"");
        assertUsage(List.of("outcomes", "5", "--unknown"), "unknown option \"--unknown\"");
        assertUsage(List.of("outcomes", "--errors=0", "--unknown", "5"),
                "unknown option \"--unknown\"");
        assertUsage(List.of("outcomes", "--help"), "unknown option \"--help\"");
        assertUsage(List.of("outcomes", "--"), "unknown option \"--\"");
    }

    @Test
    void parsesErrorsOptionAsEitherSeparateOrEqualsValue() {
        assertSuccess(List.of("outcomes", "--errors", "1", "5"),
                "secded k=5 r=4 n=10\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       10        10         0         0\n");
        assertSuccess(List.of("outcomes", "--errors=2", "5"),
                "secded k=5 r=4 n=10\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       10        10         0         0\n"
                        + "2       45        0          45        0\n");
    }

    @Test
    void consumesAnOptionValueEvenWhenItStartsWithDash() {
        assertUsage(List.of("outcomes", "--errors", "-1", "5"),
                "invalid --errors \"-1\" (expected an integer from 1 to 3)");
        assertUsage(List.of("outcomes", "--errors", "--unknown", "5"),
                "invalid --errors \"--unknown\" (expected an integer from 1 to 3)");
    }

    @Test
    void checksMissingOptionValueBeforeAnyCommandArguments() {
        assertUsage(List.of("outcomes", "--errors"), "--errors needs a value");
        assertUsage(List.of("outcomes", "--errors", "5"), "invalid --errors \"5\" (expected an integer from 1 to 3)");
        assertUsage(List.of("outcomes", "--errors=0"), "invalid --errors \"0\" (expected an integer from 1 to 3)");
    }

    @Test
    void validatesErrorsFromOneThroughThreeUsingCanonicalDecimalSyntax() {
        assertUsage(List.of("outcomes", "--errors", "0", "5"),
                "invalid --errors \"0\" (expected an integer from 1 to 3)");
        assertUsage(List.of("outcomes", "--errors", "4", "5"),
                "invalid --errors \"4\" (expected an integer from 1 to 3)");
        assertUsage(List.of("outcomes", "--errors", "01", "5"),
                "invalid --errors \"01\" (expected an integer from 1 to 3)");
        assertUsage(List.of("outcomes", "--errors", "+2", "5"),
                "invalid --errors \"+2\" (expected an integer from 1 to 3)");
        assertUsage(List.of("outcomes", "--errors", "", "5"),
                "invalid --errors \"\" (expected an integer from 1 to 3)");
        assertUsage(List.of("outcomes", "--errors", "1234567", "5"),
                "invalid --errors \"1234567\" (expected an integer from 1 to 3)");
        assertUsage(List.of("outcomes", "--errors", "1\n", "5"),
                "invalid --errors \"1?\" (expected an integer from 1 to 3)");
    }

    @Test
    void repeatedErrorsOptionUsesItsLastValue() {
        assertSuccess(List.of("outcomes", "--errors=3", "--errors", "1", "5"),
                "secded k=5 r=4 n=10\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       10        10         0         0\n");
        assertSuccess(List.of("outcomes", "5", "--errors=2", "--errors=1"),
                "secded k=5 r=4 n=10\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       10        10         0         0\n");
        assertUsage(List.of("outcomes", "--errors=1", "--errors=0", "5"),
                "invalid --errors \"0\" (expected an integer from 1 to 3)");
    }

    @Test
    void checksKInItsInclusiveOneThroughSixtyFourDomain() {
        assertUsage(List.of("outcomes", "0"), "invalid K \"0\" (expected an integer from 1 to 64)");
        assertUsage(List.of("outcomes", "65"), "invalid K \"65\" (expected an integer from 1 to 64)");
        assertUsage(List.of("outcomes", "-1"), "invalid K \"-1\" (expected an integer from 1 to 64)");
        assertUsage(List.of("outcomes", "+5"), "invalid K \"+5\" (expected an integer from 1 to 64)");
        assertUsage(List.of("outcomes", "05"), "invalid K \"05\" (expected an integer from 1 to 64)");
        assertUsage(List.of("outcomes", ""), "invalid K \"\" (expected an integer from 1 to 64)");
        assertUsage(List.of("outcomes", "1234567"),
                "invalid K \"1234567\" (expected an integer from 1 to 64)");
        assertUsage(List.of("outcomes", "5\n"), "invalid K \"5?\" (expected an integer from 1 to 64)");
    }

    @Test
    void printsDefaultThreeErrorWorkedTableExactly() {
        assertSuccess(List.of("outcomes", "5"),
                "secded k=5 r=4 n=10\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       10        10         0         0\n"
                        + "2       45        0          45        0\n"
                        + "3       120       0          48        72\n");
    }

    @Test
    void printsOneAndTwoErrorTablesWithOnlyRequestedRows() {
        assertSuccess(List.of("outcomes", "--errors=1", "5"),
                "secded k=5 r=4 n=10\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       10        10         0         0\n");
        assertSuccess(List.of("outcomes", "5", "--errors", "2"),
                "secded k=5 r=4 n=10\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       10        10         0         0\n"
                        + "2       45        0          45        0\n");
    }

    @Test
    void printsSmallestDataBoundaryAndPreservesZeroCounts() {
        assertSuccess(List.of("outcomes", "--errors=3", "1"),
                "secded k=1 r=2 n=4\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       4         4          0         0\n"
                        + "2       6         0          6         0\n"
                        + "3       4         0          0         4\n");
    }

    @Test
    void printsLargestDataBoundaryWithLiteralCountsAndAlignedColumns() {
        assertSuccess(List.of("outcomes", "--errors=3", "64"),
                "secded k=64 r=7 n=72\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       72        72         0         0\n"
                        + "2       2556      0          2556      0\n"
                        + "3       59640     0          14336     45304\n");
    }

    @Test
    @DisplayName("weights/outcomes with no K")
    void rejectsMissingOperandsForAnalysisCommands() {
        assertUsage(List.of("weights"), "missing K");
        assertUsage(List.of("burst"), "missing MODEL");
        assertUsage(List.of("dfree"), "missing CODE");
    }

    @Test
    @DisplayName("weights/outcomes with two operands")
    void rejectsExcessOperandsForWeightsAndOutcomes() {
        assertUsage(List.of("weights", "4", "5"), "too many arguments");
        assertUsage(List.of("outcomes", "4", "5"), "too many arguments");
    }

    @Test
    @DisplayName("weights 0")
    void rejectsWeightsBelowItsMinimum() {
        assertUsage(List.of("weights", "0"), "invalid K \"0\" (expected an integer from 1 to 12)");
    }

    @Test
    @DisplayName("burst bad extra")
    void checksBurstExcessArgumentsBeforeLookingUpModel() {
        assertUsage(List.of("burst", "bad", "extra"), "too many arguments");
    }

    @Test
    @DisplayName("burst bad or malformed crc: spec")
    void checksBurstModelErrorsInOrder() {
        assertUsage(List.of("burst", "bad"),
                "unknown model \"bad\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)");
        assertUsage(List.of("burst", "crc:8:10:00:0:0:00"),
                "invalid crc spec \"crc:8:10:00:0:0:00\" (POLY must be odd and below 2^WIDTH)");
    }

    @Test
    @DisplayName("dfree bad extra")
    void checksDfreeExcessArgumentsBeforeCodeLookup() {
        assertUsage(List.of("dfree", "bad", "extra"), "too many arguments");
    }

    @Test
    @DisplayName("dfree bad")
    void rejectsUnknownDfreeCode() {
        assertUsage(List.of("dfree", "bad"), "unknown code \"bad\" (expected k3, k5 or k7)");
    }

    @Test
    @DisplayName("weights 4")
    void printsTheWorkedWeightsTableExactly() {
        assertSuccess(List.of("weights", "4"),
                "secded k=4 r=3 n=8, minimum distance 4\n"
                        + "weight  codewords\n"
                        + "0       1\n"
                        + "4       14\n"
                        + "8       1\n");
    }

    @Test
    @DisplayName("burst --max-len 10 crc8")
    void printsTheWorkedBurstTableExactly() {
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
    @DisplayName("dfree k3")
    void printsTheK3FreeDistanceWitnessExactly() {
        assertSuccess(List.of("dfree", "k3"),
                "k3 (K=3, generators 7 and 5 octal): free distance 5\n"
                        + "input     100\n"
                        + "coded     11 10 11\n");
    }

    @Test
    @DisplayName("dfree k5")
    void printsTheK5FreeDistanceWitnessExactly() {
        assertSuccess(List.of("dfree", "k5"),
                "k5 (K=5, generators 23 and 35 octal): free distance 7\n"
                        + "input     10000\n"
                        + "coded     11 01 01 10 11\n");
    }

    @Test
    @DisplayName("dfree k7")
    void printsTheK7FreeDistanceWitnessExactly() {
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
            throw new AssertionError("outcomes must not read standard input");
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
