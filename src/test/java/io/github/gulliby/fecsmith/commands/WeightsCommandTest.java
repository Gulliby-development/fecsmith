package io.github.gulliby.fecsmith.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gulliby.fecsmith.Cli;
import io.github.gulliby.fecsmith.core.Io;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WeightsCommandTest {
    @Test
    @DisplayName("weights/outcomes with two operands")
    void checksMissingAndExcessiveArgumentsBeforeParsingK() {
        assertUsage(List.of("weights"), "missing K");
        assertUsage(List.of("weights", "4", "5"), "too many arguments");
        assertUsage(List.of("weights", "not-a-number", "extra"), "too many arguments");
        assertUsage(List.of("weights", "4", "5", "--unknown"), "unknown option \"--unknown\"");
    }

    @Test
    void rejectsOptionsBecauseWeightsHasNoOptionSpecs() {
        assertUsage(List.of("weights", "--help"), "unknown option \"--help\"");
        assertUsage(List.of("weights", "--"), "unknown option \"--\"");
        assertUsage(List.of("weights", "--unknown", "1"), "unknown option \"--unknown\"");
        assertUsage(List.of("weights", "1", "--unknown"), "unknown option \"--unknown\"");
    }

    @Test
    void validatesKWithinTheInclusiveDomainUsingCanonicalDecimalSyntax() {
        assertUsage(List.of("weights", "0"), "invalid K \"0\" (expected an integer from 1 to 12)");
        assertUsage(List.of("weights", "13"), "invalid K \"13\" (expected an integer from 1 to 12)");
        assertUsage(List.of("weights", "-1"), "invalid K \"-1\" (expected an integer from 1 to 12)");
        assertUsage(List.of("weights", "+1"), "invalid K \"+1\" (expected an integer from 1 to 12)");
        assertUsage(List.of("weights", "01"), "invalid K \"01\" (expected an integer from 1 to 12)");
        assertUsage(List.of("weights", ""), "invalid K \"\" (expected an integer from 1 to 12)");
        assertUsage(List.of("weights", "1234567"),
                "invalid K \"1234567\" (expected an integer from 1 to 12)");
        assertUsage(List.of("weights", "1\n"), "invalid K \"1?\" (expected an integer from 1 to 12)");
        assertUsage(List.of("weights", "1.0"),
                "invalid K \"1.0\" (expected an integer from 1 to 12)");
        assertUsage(List.of("weights", "-"), "invalid K \"-\" (expected an integer from 1 to 12)");
    }

    @Test
    void printsTheCompleteKOneBoundaryDistribution() {
        assertSuccess(List.of("weights", "1"),
                "secded k=1 r=2 n=4, minimum distance 4\n"
                        + "weight  codewords\n"
                        + "0       1\n"
                        + "4       1\n");
    }

    @Test
    void printsTheCompleteKTwoDistribution() {
        assertSuccess(List.of("weights", "2"),
                "secded k=2 r=3 n=6, minimum distance 4\n"
                        + "weight  codewords\n"
                        + "0       1\n"
                        + "4       3\n");
    }

    @Test
    void printsTheWorkedKFourDistributionWithSortedWeightsAndMinimumDistance() {
        assertSuccess(List.of("weights", "4"),
                "secded k=4 r=3 n=8, minimum distance 4\n"
                        + "weight  codewords\n"
                        + "0       1\n"
                        + "4       14\n"
                        + "8       1\n");
    }

    @Test
    void printsEveryPresentWeightInAscendingOrderAtTheKwelveBoundary() {
        assertSuccess(List.of("weights", "12"),
                "secded k=12 r=5 n=18, minimum distance 4\n"
                        + "weight  codewords\n"
                        + "0       1\n"
                        + "4       148\n"
                        + "6       560\n"
                        + "8       1374\n"
                        + "10      1248\n"
                        + "12      644\n"
                        + "14      112\n"
                        + "16      9\n");
    }

    @Test
    @DisplayName("weights/outcomes with no K")
    void checksMissingAndExcessiveKForWeightsAndOutcomes() {
        assertUsage(List.of("weights"), "missing K");
        assertUsage(List.of("outcomes"), "missing K");
        assertUsage(List.of("outcomes", "5", "6"), "too many arguments");
        assertUsage(List.of("outcomes", "not-a-number", "extra"), "too many arguments");
        assertUsage(List.of("outcomes", "65"), "invalid K \"65\" (expected an integer from 1 to 64)");
    }

    @Test
    @DisplayName("burst bad or malformed crc: spec")
    void checksBurstAndDfreeMissingArgumentsExcessAndUnknownNames() {
        assertUsage(List.of("burst"), "missing MODEL");
        assertUsage(List.of("burst", "bad", "extra"), "too many arguments");
        assertUsage(List.of("burst", "bad"), "unknown model \"bad\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)");
        assertUsage(List.of("burst", "crc:8:10:00:0:0:00"),
                "invalid crc spec \"crc:8:10:00:0:0:00\" (POLY must be odd and below 2^WIDTH)");
        assertUsage(List.of("dfree"), "missing CODE");
        assertUsage(List.of("dfree", "bad", "extra"), "too many arguments");
        assertUsage(List.of("dfree", "bad"), "unknown code \"bad\" (expected k3, k5 or k7)");
    }

    @Test
    void printsTheCompleteWorkedOutcomesExample() {
        assertSuccess(List.of("outcomes", "5"),
                "secded k=5 r=4 n=10\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       10        10         0         0\n"
                        + "2       45        0          45        0\n"
                        + "3       120       0          48        72\n");
    }

    @Test
    void printsTheCompleteWorkedBurstExample() {
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
    void printsTheCompleteFirstReturnPathsForK3K5AndK7() {
        assertSuccess(List.of("dfree", "k3"),
                "k3 (K=3, generators 7 and 5 octal): free distance 5\n"
                        + "input     100\n"
                        + "coded     11 10 11\n");
        assertSuccess(List.of("dfree", "k5"),
                "k5 (K=5, generators 23 and 35 octal): free distance 7\n"
                        + "input     10000\n"
                        + "coded     11 01 01 10 11\n");
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
            throw new AssertionError("weights must not read standard input");
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
