package io.github.gulliby.fecsmith.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gulliby.fecsmith.Cli;
import io.github.gulliby.fecsmith.core.Io;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RsCommandTest {
    private static final byte[] NO_STDIN = {};

    @Test
    void encodesTheWorkedGf256MessageAndUsesLowercaseFixedWidthSymbols() {
        assertSuccess(List.of("rs", "encode", "48", "65", "6c", "6c", "6f"),
                "RS(9,5) over GF(2^8), t=2\n"
                        + "codeword  48 65 6c 6c 6f 08 47 90 ca\n"
                        + "parity    08 47 90 ca\n");
    }

    @Test
    void encodeCoversAllSixFieldsAndBothSymbolWidths() {
        assertSuccess(List.of("rs", "encode", "--m", "3", "--nsym", "2", "7"),
                "RS(3,1) over GF(2^3), t=1\n"
                        + "codeword  7 4 2\n"
                        + "parity    4 2\n");
        assertSuccess(List.of("rs", "encode", "--m", "4", "--nsym", "2", "F"),
                "RS(3,1) over GF(2^4), t=1\n"
                        + "codeword  f 4 1\n"
                        + "parity    4 1\n");
        assertSuccess(List.of("rs", "encode", "--m", "5", "--nsym", "2", "01"),
                "RS(3,1) over GF(2^5), t=1\n"
                        + "codeword  01 06 08\n"
                        + "parity    06 08\n");
        assertSuccess(List.of("rs", "encode", "--m", "6", "--nsym", "2", "01"),
                "RS(3,1) over GF(2^6), t=1\n"
                        + "codeword  01 06 08\n"
                        + "parity    06 08\n");
        assertSuccess(List.of("rs", "encode", "--m", "7", "--nsym", "2", "01"),
                "RS(3,1) over GF(2^7), t=1\n"
                        + "codeword  01 06 08\n"
                        + "parity    06 08\n");
        assertSuccess(List.of("rs", "encode", "--m", "8", "--nsym", "2", "01"),
                "RS(3,1) over GF(2^8), t=1\n"
                        + "codeword  01 06 08\n"
                        + "parity    06 08\n");
    }

    @Test
    void encodesOddParityCountsAndTheMaximumShortenedFieldWord() {
        assertSuccess(List.of("rs", "encode", "--m", "3", "--nsym", "3", "1"),
                "RS(4,1) over GF(2^3), t=1\n"
                        + "codeword  1 5 2 5\n"
                        + "parity    5 2 5\n");
        assertSuccess(List.of("rs", "encode", "--m", "3", "--nsym", "2", "1", "0", "0", "0", "1"),
                "RS(7,5) over GF(2^3), t=1\n"
                        + "codeword  1 0 0 0 1 0 1\n"
                        + "parity    0 1\n");
    }

    @Test
    void decodesAFullLengthCleanWordAndTheOneErrorBoundary() {
        assertSuccess(List.of("rs", "decode", "--m", "3", "--nsym", "2",
                        "1", "0", "0", "0", "0", "6", "2"),
                "RS(7,5) over GF(2^3), t=1\n"
                        + "syndrome  0 0\n"
                        + "status    ok\n"
                        + "codeword  1 0 0 0 0 6 2\n"
                        + "message   1 0 0 0 0\n");
        assertSuccess(List.of("rs", "decode", "--m", "3", "--nsym", "2",
                        "1", "0", "0", "0", "0", "7", "2"),
                "RS(7,5) over GF(2^3), t=1\n"
                        + "syndrome  2 4\n"
                        + "status    corrected 1 error\n"
                        + "errors    5:1\n"
                        + "codeword  1 0 0 0 0 6 2\n"
                        + "message   1 0 0 0 0\n");
    }

    @Test
    void decodesAShortenedWordWithAnOddParityCount() {
        assertSuccess(List.of("rs", "decode", "--m", "3", "--nsym", "3", "1", "4", "2", "5"),
                "RS(4,1) over GF(2^3), t=1\n"
                        + "syndrome  4 6 5\n"
                        + "status    corrected 1 error\n"
                        + "errors    1:1\n"
                        + "codeword  1 5 2 5\n"
                        + "message   1\n");
    }

    @Test
    void decodesTheWorkedCleanGf256Word() {
        assertSuccess(List.of("rs", "decode", "48", "65", "6c", "6c", "6f", "08", "47", "90", "ca"),
                "RS(9,5) over GF(2^8), t=2\n"
                        + "syndrome  00 00 00 00\n"
                        + "status    ok\n"
                        + "codeword  48 65 6c 6c 6f 08 47 90 ca\n"
                        + "message   48 65 6c 6c 6f\n");
    }

    @Test
    void decodesTheWorkedTwoErrorCorrectionWithAscendingXorErrors() {
        assertSuccess(List.of("rs", "decode", "48", "00", "6c", "6c", "ff", "08", "47", "90", "ca"),
                "RS(9,5) over GF(2^8), t=2\n"
                        + "syndrome  05 5a a0 6c\n"
                        + "status    corrected 2 errors\n"
                        + "errors    1:65 4:90\n"
                        + "codeword  48 65 6c 6c 6f 08 47 90 ca\n"
                        + "message   48 65 6c 6c 6f\n");
    }

    @Test
    void suppliedUncorrectableGf256VectorStopsAfterItsStatus() {
        assertRun(List.of("rs", "decode", "48", "65", "6c", "6c", "6f", "9b", "6e", "3a", "6c"),
                NO_STDIN, null, 1,
                "RS(9,5) over GF(2^8), t=2\n"
                        + "syndrome  a7 ad 88 fa\n"
                        + "status    uncorrectable\n",
                "", 0);
    }

    @Test
    void optionBoundariesAndValuesAfterOperandsAreAccepted() {
        assertSuccess(List.of("rs", "encode", "--m", "3", "--nsym", "2", "1"),
                "RS(3,1) over GF(2^3), t=1\n"
                        + "codeword  1 6 3\n"
                        + "parity    6 3\n");
        assertSuccess(List.of("rs", "encode", "1", "--m", "3", "--nsym", "2"),
                "RS(3,1) over GF(2^3), t=1\n"
                        + "codeword  1 6 3\n"
                        + "parity    6 3\n");
        assertUsage(List.of("rs", "encode", "--m", "8"), "missing SYMBOL", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--nsym", "32"), "missing SYMBOL", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--nsym=2"), "missing SYMBOL", NO_STDIN, 0);
    }

    @Test
    void repeatedOptionsUseTheLastValue() {
        assertSuccess(List.of("rs", "encode", "--m", "3", "--m=4", "--nsym", "3", "--nsym=2", "1"),
                "RS(3,1) over GF(2^4), t=1\n"
                        + "codeword  1 6 8\n"
                        + "parity    6 8\n");
    }

    @Test
    void optionSyntaxErrorsPrecedeModeAndRangeValidation() {
        assertUsage(List.of("rs"), "missing mode (encode or decode)", NO_STDIN, 0);
        assertUsage(List.of("rs", "bad"), "unknown mode \"bad\" (expected encode or decode)", NO_STDIN, 0);
        assertUsage(List.of("rs", "--m"), "--m needs a value", NO_STDIN, 0);
        assertUsage(List.of("rs", "--bad", "encode"), "unknown option \"--bad\"", NO_STDIN, 0);
        assertUsage(List.of("rs", "--m", "2", "--bad"), "unknown option \"--bad\"", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "--wat"),
                "invalid --m \"--wat\" (expected an integer from 3 to 8)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--help"), "unknown option \"--help\"", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "2"), "invalid --m \"2\" (expected an integer from 3 to 8)",
                NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "9"), "invalid --m \"9\" (expected an integer from 3 to 8)",
                NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--nsym", "1"), "invalid --nsym \"1\" (expected an integer from 2 to 32)",
                NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--nsym", "33"), "invalid --nsym \"33\" (expected an integer from 2 to 32)",
                NO_STDIN, 0);
        assertUsage(List.of("rs", "--nsym", "1", "--m", "2", "encode", "1"),
                "invalid --m \"2\" (expected an integer from 3 to 8)", NO_STDIN, 0);
        assertUsage(List.of("rs", "--m", "2", "--nsym", "1", "encode", "1"),
                "invalid --m \"2\" (expected an integer from 3 to 8)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m=007", "1"),
                "invalid --m \"007\" (expected an integer from 3 to 8)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "1000000", "1"),
                "invalid --m \"1000000\" (expected an integer from 3 to 8)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "", "1"),
                "invalid --m \"\" (expected an integer from 3 to 8)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "-1", "1"),
                "invalid --m \"-1\" (expected an integer from 3 to 8)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "+3", "1"),
                "invalid --m \"+3\" (expected an integer from 3 to 8)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m=3", "--nsym=2=ignored", "1"),
                "invalid --nsym \"2=ignored\" (expected an integer from 2 to 32)", NO_STDIN, 0);
    }

    @Test
    void symbolValidationUsesTheSelectedFieldWidthAndValueRange() {
        assertUsage(List.of("rs", "encode", "--m", "3", "01"),
                "invalid symbol \"01\" (expected 1 hex digit with a value below 2^3)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "3", "8"),
                "invalid symbol \"8\" (expected 1 hex digit with a value below 2^3)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "3", "1", "f"),
                "invalid symbol \"f\" (expected 1 hex digit with a value below 2^3)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "5", "1"),
                "invalid symbol \"1\" (expected 2 hex digits with a value below 2^5)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "5", "20"),
                "invalid symbol \"20\" (expected 2 hex digits with a value below 2^5)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "8", "0x"),
                "invalid symbol \"0x\" (expected 2 hex digits with a value below 2^8)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "3", "8", "x"),
                "invalid symbol \"8\" (expected 1 hex digit with a value below 2^3)", NO_STDIN, 0);
    }

    @Test
    void symbolChecksPrecedeEncodeAndDecodeCapacityChecks() {
        assertUsage(List.of("rs", "encode", "--m", "3", "--nsym", "2", "0", "0", "0", "0", "0", "0"),
                "codeword of 8 symbols does not fit in GF(2^3) (at most 7)", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "3", "--nsym", "2", "0", "0", "0", "0", "0", "x"),
                "invalid symbol \"x\" (expected 1 hex digit with a value below 2^3)", NO_STDIN, 0);
        assertUsage(List.of("rs", "decode", "--m", "3", "--nsym", "8",
                        "0", "0", "0", "0", "0", "0", "0", "0"),
                "received word of 8 symbols does not fit in GF(2^3) (at most 7)", NO_STDIN, 0);
        assertUsage(List.of("rs", "decode", "--m", "3", "--nsym", "2", "0", "0"),
                "received word of 2 symbols is not longer than --nsym 2", NO_STDIN, 0);
        assertUsage(List.of("rs", "encode", "--m", "4", "--nsym", "4",
                        "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c"),
                "codeword of 16 symbols does not fit in GF(2^4) (at most 15)", NO_STDIN, 0);
        assertUsage(List.of("rs", "decode", "00"),
                "received word of 1 symbols is not longer than --nsym 4", NO_STDIN, 0);
    }

    @Test
    void shortestValidDecodeWordIsAcceptedAtTheNsymBoundary() {
        assertSuccess(List.of("rs", "decode", "--m", "3", "--nsym", "2", "0", "0", "0"),
                "RS(3,1) over GF(2^3), t=1\n"
                        + "syndrome  0 0\n"
                        + "status    ok\n"
                        + "codeword  0 0 0\n"
                        + "message   0\n");
    }

    @Test
    void stdinResolvesOnlyAnExactlySingleDashAndAcceptsBomWhitespace() {
        assertRun(List.of("rs", "encode", "--m", "3", "--nsym", "2", "-"),
                new byte[] {
                        (byte) 0xef, (byte) 0xbb, (byte) 0xbf, 0x31, 0x09, 0x30, 0x20,
                        0x30, 0x0d, 0x0a, 0x30, 0x09, 0x31, 0x0a
                },
                null, 0,
                "RS(7,5) over GF(2^3), t=1\n"
                        + "codeword  1 0 0 0 1 0 1\n"
                        + "parity    0 1\n",
                "", 1);
        assertRun(List.of("rs", "encode", "-"),
                new byte[] {
                        (byte) 0xef, (byte) 0xbb, (byte) 0xbf,
                        0x34, 0x38, 0x09, 0x36, 0x35, 0x20, 0x36, 0x63, 0x0d, 0x0a,
                        0x36, 0x63, 0x20, 0x36, 0x66, 0x0a
                },
                null, 0,
                "RS(9,5) over GF(2^8), t=2\n"
                        + "codeword  48 65 6c 6c 6f 08 47 90 ca\n"
                        + "parity    08 47 90 ca\n",
                "", 1);
        assertRun(List.of("rs", "encode", "--m", "3", "--nsym", "2", "-"),
                new byte[] {0x31, 0x09, 0x32, 0x0d, 0x0a, 0x33, 0x0a},
                null, 0,
                "RS(5,3) over GF(2^3), t=1\n"
                        + "codeword  1 2 3 4 4\n"
                        + "parity    4 4\n",
                "", 1);
    }

    @Test
    void malformedUtf8AndNulStdinReachTheStrictWordDecoder() {
        assertUsage(List.of("rs", "encode", "-"), "<stdin>: input is not valid UTF-8",
                new byte[] {0x31, 0x32, (byte) 0xff, 0x0a}, 1);
        assertUsage(List.of("rs", "encode", "-"),
                "invalid symbol \"1?2\" (expected 2 hex digits with a value below 2^8)",
                new byte[] {0x31, 0x00, 0x32}, 1);
    }

    @Test
    void emptyAndSeparatorOnlyStdinHaveTheNoInputDiagnostic() {
        assertUsage(List.of("rs", "decode", "-"), "<stdin>: no input", new byte[] {}, 1);
        assertUsage(List.of("rs", "decode", "-"), "<stdin>: no input",
                new byte[] {0x20, 0x09, 0x0d, 0x0a}, 1);
        assertUsage(List.of("rs", "decode", "-"), "<stdin>: no input",
                new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0xbf}, 1);
    }

    @Test
    void stdinReadFailureHasItsDistinctDiagnostic() {
        assertRun(List.of("rs", "encode", "-"), NO_STDIN, new IOException("unavailable"), 2, "",
                "fecsmith: <stdin>: cannot read the input\n", 1);
    }

    @Test
    void earlierFailuresAndDashMixedWithSymbolsDoNotReadStdin() {
        assertUsage(List.of("rs", "unknown", "-"),
                "unknown mode \"unknown\" (expected encode or decode)", new byte[] {(byte) 0xff}, 0);
        assertUsage(List.of("rs", "encode", "--m", "2", "-"),
                "invalid --m \"2\" (expected an integer from 3 to 8)", new byte[] {(byte) 0xff}, 0);
        assertUsage(List.of("rs", "encode", "--m", "3", "-", "1"),
                "invalid symbol \"-\" (expected 1 hex digit with a value below 2^3)",
                new byte[] {(byte) 0xff}, 0);
        assertUsage(List.of("rs", "encode", "--m", "3", "1", "-"),
                "invalid symbol \"-\" (expected 1 hex digit with a value below 2^3)",
                new byte[] {(byte) 0xff}, 0);
        assertUsage(List.of("rs", "encode", "--bad", "-"), "unknown option \"--bad\"",
                new byte[] {(byte) 0xff}, 0);
    }

    private static void assertSuccess(List<String> argv, String expectedOutput) {
        assertRun(argv, NO_STDIN, null, 0, expectedOutput, "", 0);
    }

    private static void assertUsage(List<String> argv, String message, byte[] input, int expectedReads) {
        assertRun(argv, input, null, 2, "", "fecsmith: " + message + "\n", expectedReads);
    }

    private static void assertRun(List<String> argv, byte[] input, IOException readFailure,
            int expectedStatus, String expectedOutput, String expectedError, int expectedReads) {
        FakeIo io = new FakeIo(input, readFailure);

        int status = Cli.run(argv, io);

        assertEquals(expectedStatus, status);
        assertEquals(expectedOutput.isEmpty() ? List.of() : List.of(expectedOutput), io.outputs);
        assertEquals(expectedError.isEmpty() ? List.of() : List.of(expectedError), io.errors);
        assertEquals(expectedReads, io.reads);
    }

    private static final class FakeIo implements Io {
        private final byte[] input;
        private final IOException readFailure;
        private final ArrayList<String> outputs = new ArrayList<>();
        private final ArrayList<String> errors = new ArrayList<>();
        private int reads;

        private FakeIo(byte[] input, IOException readFailure) {
            this.input = input;
            this.readFailure = readFailure;
        }

        @Override
        public byte[] readStdin() throws IOException {
            reads++;
            if (readFailure != null) {
                throw readFailure;
            }
            return input;
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
