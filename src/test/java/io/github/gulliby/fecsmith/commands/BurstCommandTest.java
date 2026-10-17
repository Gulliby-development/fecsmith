package io.github.gulliby.fecsmith.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gulliby.fecsmith.Cli;
import io.github.gulliby.fecsmith.core.Io;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BurstCommandTest {
    @Test
    void checksMissingAndExcessiveArgumentsInOrder() {
        assertUsage(List.of("burst"), "missing MODEL");
        assertUsage(List.of("burst", "crc8", "extra"), "too many arguments");
        assertUsage(List.of("burst", "not-a-model", "extra"), "too many arguments");
    }

    @Test
    void parsesMaxLengthBeforePositionalValidation() {
        assertUsage(List.of("burst", "--unknown"), "unknown option \"--unknown\"");
        assertUsage(List.of("burst", "--max-len"), "--max-len needs a value");
        assertUsage(List.of("burst", "--max-len=0"), "invalid --max-len \"0\" (expected an integer from 1 to 18)");
        assertUsage(List.of("burst", "--max-len=19"), "invalid --max-len \"19\" (expected an integer from 1 to 18)");
        assertUsage(List.of("burst", "--max-len=01"), "invalid --max-len \"01\" (expected an integer from 1 to 18)");
        assertUsage(List.of("burst", "--max-len=+1"), "invalid --max-len \"+1\" (expected an integer from 1 to 18)");
        assertUsage(List.of("burst", "--max-len=1234567"),
                "invalid --max-len \"1234567\" (expected an integer from 1 to 18)");
        assertUsage(List.of("burst", "--max-len=1\n"), "invalid --max-len \"1?\" (expected an integer from 1 to 18)");
        assertUsage(List.of("burst", "--max-len", "--unknown"),
                "invalid --max-len \"--unknown\" (expected an integer from 1 to 18)");
        assertUsage(List.of("burst", "--max-len=0", "--unknown"),
                "unknown option \"--unknown\"");
        assertUsage(List.of("burst", "--max-len=0"),
                "invalid --max-len \"0\" (expected an integer from 1 to 18)");
    }

    @Test
    void repeatedMaxLengthUsesLastValueAndAnOverwrittenInvalidValueIsNotChecked() {
        assertSuccess(List.of("burst", "--max-len=0", "--max-len=1", "crc8"),
                "crc8, width 8, bursts up to length 1\n"
                        + "length  patterns  undetected\n"
                        + "1       1         0\n");
        assertSuccess(List.of("burst", "crc8", "--max-len=10", "--max-len", "1"),
                "crc8, width 8, bursts up to length 1\n"
                        + "length  patterns  undetected\n"
                        + "1       1         0\n");
    }

    @Test
    void looksUpModelAfterArityAndRejectsUnknownAndMalformedModels() {
        assertUsage(List.of("burst", "unknown"),
                "unknown model \"unknown\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)");
        assertUsage(List.of("burst", "crc:8:10:00:0:0:00"),
                "invalid crc spec \"crc:8:10:00:0:0:00\" (POLY must be odd and below 2^WIDTH)");
        assertUsage(List.of("burst", "crc:8:7:00:0:0:00:extra"),
                "invalid crc spec \"crc:8:7:00:0:0:00:extra\" "
                        + "(expected crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)");
        assertUsage(List.of("burst", "crc:2:1:0:0:0:0"),
                "invalid crc spec \"crc:2:1:0:0:0:0\" (WIDTH must be from 3 to 32)");
        assertUsage(List.of("burst", "crc:8:07:100:0:0:00"),
                "invalid crc spec \"crc:8:07:100:0:0:00\" (INIT must be below 2^WIDTH)");
        assertUsage(List.of("burst", "crc:8:07:00:0:0:100"),
                "invalid crc spec \"crc:8:07:00:0:0:100\" (XOROUT must be below 2^WIDTH)");
    }

    @Test
    void printsTheWorkedCrc8BurstTableExactly() {
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
    void defaultMaximumLengthIsTwelveAndIncludesEveryLiteralRow() {
        assertSuccess(List.of("burst", "crc8"),
                "crc8, width 8, bursts up to length 12\n"
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
                        + "10      256       1\n"
                        + "11      512       2\n"
                        + "12      1024      4\n");
    }

    @Test
    void maximumLengthOneHasOneRowAndPreservesTheTypedModelName() {
        assertUsage(List.of("burst", "--max-len=1", "CRC8"),
                "unknown model \"CRC8\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)");
        assertSuccess(List.of("burst", "--max-len=1", "crc8"),
                "crc8, width 8, bursts up to length 1\n"
                        + "length  patterns  undetected\n"
                        + "1       1         0\n");
    }

    @Test
    void maximumLengthEighteenIncludesAllLiteralCrc3RowsAndAlignedColumns() {
        assertSuccess(List.of("burst", "--max-len=18", "crc3-gsm"),
                "crc3-gsm, width 3, bursts up to length 18\n"
                        + "length  patterns  undetected\n"
                        + "1       1         0\n"
                        + "2       1         0\n"
                        + "3       2         0\n"
                        + "4       4         1\n"
                        + "5       8         1\n"
                        + "6       16        2\n"
                        + "7       32        4\n"
                        + "8       64        8\n"
                        + "9       128       16\n"
                        + "10      256       32\n"
                        + "11      512       64\n"
                        + "12      1024      128\n"
                        + "13      2048      256\n"
                        + "14      4096      512\n"
                        + "15      8192      1024\n"
                        + "16      16384     2048\n"
                        + "17      32768     4096\n"
                        + "18      65536     8192\n");
    }

    @Test
    void countsIgnoreCrcInitReflectionAndXorOutButKeepModelSpellingInHeader() {
        assertSuccess(List.of("burst", "--max-len=10", "crc:8:07:00:0:0:00"),
                "crc:8:07:00:0:0:00, width 8, bursts up to length 10\n"
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
        assertSuccess(List.of("burst", "--max-len=10", "crc:8:07:01:1:1:01"),
                "crc:8:07:01:1:1:01, width 8, bursts up to length 10\n"
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
    @DisplayName("bad")
    void rejectsTheLiteralBadBurstModel() {
        assertUsage(List.of("burst", "bad"),
                "unknown model \"bad\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)");
    }

    @Test
    @DisplayName("weights/outcomes with no K")
    void otherAnalysisCommandsRejectMissingOperands() {
        assertUsage(List.of("weights"), "missing K");
        assertUsage(List.of("outcomes"), "missing K");
    }

    @Test
    @DisplayName("weights/outcomes with two operands")
    void otherAnalysisCommandsRejectExcessOperands() {
        assertUsage(List.of("weights", "4", "5"), "too many arguments");
        assertUsage(List.of("outcomes", "4", "5"), "too many arguments");
    }

    @Test
    @DisplayName("weights 0")
    void weightsRejectsItsLowerOutOfRangeBoundary() {
        assertUsage(List.of("weights", "0"), "invalid K \"0\" (expected an integer from 1 to 12)");
    }

    @Test
    @DisplayName("outcomes 65")
    void outcomesRejectsItsUpperOutOfRangeBoundary() {
        assertUsage(List.of("outcomes", "65"), "invalid K \"65\" (expected an integer from 1 to 64)");
    }

    @Test
    @DisplayName("burst bad extra")
    void burstChecksExcessBeforeModelLookup() {
        assertUsage(List.of("burst", "bad", "extra"), "too many arguments");
    }

    @Test
    @DisplayName("burst bad or malformed crc: spec")
    void burstRejectsBadAndMalformedModelInputs() {
        assertUsage(List.of("burst", "bad"),
                "unknown model \"bad\" (see crc --list, or write crc:WIDTH:POLY:INIT:REFIN:REFOUT:XOROUT)");
        assertUsage(List.of("burst", "crc:8:10:00:0:0:00"),
                "invalid crc spec \"crc:8:10:00:0:0:00\" (POLY must be odd and below 2^WIDTH)");
    }

    @Test
    @DisplayName("dfree")
    void dfreeRejectsMissingCode() {
        assertUsage(List.of("dfree"), "missing CODE");
    }

    @Test
    @DisplayName("dfree bad extra")
    void dfreeChecksExcessBeforeCodeLookup() {
        assertUsage(List.of("dfree", "bad", "extra"), "too many arguments");
    }

    @Test
    @DisplayName("dfree bad")
    void dfreeRejectsUnknownCode() {
        assertUsage(List.of("dfree", "bad"), "unknown code \"bad\" (expected k3, k5 or k7)");
    }

    @Test
    @DisplayName("weights 4")
    void weightsPrintsTheCompleteWorkedTable() {
        assertSuccess(List.of("weights", "4"),
                "secded k=4 r=3 n=8, minimum distance 4\n"
                        + "weight  codewords\n"
                        + "0       1\n"
                        + "4       14\n"
                        + "8       1\n");
    }

    @Test
    @DisplayName("outcomes 5")
    void outcomesPrintsTheCompleteWorkedTable() {
        assertSuccess(List.of("outcomes", "5"),
                "secded k=5 r=4 n=10\n"
                        + "errors  patterns  corrected  detected  wrong\n"
                        + "1       10        10         0         0\n"
                        + "2       45        0          45        0\n"
                        + "3       120       0          48        72\n");
    }

    @Test
    @DisplayName("dfree k3")
    void dfreeK3PrintsTheCompleteWitness() {
        assertSuccess(List.of("dfree", "k3"),
                "k3 (K=3, generators 7 and 5 octal): free distance 5\n"
                        + "input     100\n"
                        + "coded     11 10 11\n");
    }

    @Test
    @DisplayName("dfree k5")
    void dfreeK5PrintsTheCompleteWitness() {
        assertSuccess(List.of("dfree", "k5"),
                "k5 (K=5, generators 23 and 35 octal): free distance 7\n"
                        + "input     10000\n"
                        + "coded     11 01 01 10 11\n");
    }

    @Test
    @DisplayName("dfree k7")
    void dfreeK7PrintsTheCompleteWitness() {
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
            throw new AssertionError("burst must not read standard input");
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
