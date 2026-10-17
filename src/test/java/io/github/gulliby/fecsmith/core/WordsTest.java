package io.github.gulliby.fecsmith.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class WordsTest {
    @Test
    void strictUtf8AcceptsMultibyteAndSupplementaryText() {
        FakeIo io = new FakeIo(new byte[] {
                0x63, 0x61, 0x66, (byte) 0xC3, (byte) 0xA9, 0x20,
                (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80
        });

        assertEquals(List.of("café", "😀"), Words.of(io, List.of("-")));
        assertEquals(1, io.reads);
    }

    @Test
    void splitsOnSpaceTabCarriageReturnAndLineFeed() {
        FakeIo io = new FakeIo(new byte[] {
                0x20, 0x61, 0x6C, 0x70, 0x68, 0x61, 0x09,
                0x0D, 0x0A, 0x62, 0x65, 0x74, 0x61, 0x0A,
                0x67, 0x61, 0x6D, 0x6D, 0x61, 0x09,
                0x64, 0x65, 0x6C, 0x74, 0x61, 0x0D, 0x20
        });

        assertEquals(List.of("alpha", "beta", "gamma", "delta"), Words.of(io, List.of("-")));
    }

    @Test
    void removesOneInitialBom() {
        FakeIo io = new FakeIo(new byte[] {
                (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 0x31, 0x32
        });

        assertEquals(List.of("12"), Words.of(io, List.of("-")));
    }

    @Test
    void removesExactlyOneInitialBomAndKeepsTheSecond() {
        FakeIo io = new FakeIo(new byte[] {
                (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 0x78,
                (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 0x79
        });

        assertEquals(List.of("x\uFEFFy"), Words.of(io, List.of("-")));
    }

    @Test
    void keepsNoninitialBomInsideAWord() {
        FakeIo io = new FakeIo(new byte[] {
                0x61, (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 0x62
        });

        assertEquals(List.of("a\uFEFFb"), Words.of(io, List.of("-")));
    }

    @Test
    void nulAndOtherWhitespaceAreNotSeparators() {
        FakeIo io = new FakeIo(new byte[] {
                0x61, 0x00, 0x62, (byte) 0xC2, (byte) 0xA0,
                0x63, 0x0B, 0x64, 0x0C, 0x65
        });

        assertEquals(List.of("a\0b\u00A0c\013d\fe"), Words.of(io, List.of("-")));
    }

    @Test
    void emptyStdinHasTheNoInputError() {
        assertUsage("<stdin>: no input", () -> Words.of(new FakeIo(new byte[] {}), List.of("-")));
    }

    @Test
    void bomOnlyStdinHasTheNoInputError() {
        assertUsage("<stdin>: no input", () -> Words.of(
                new FakeIo(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}), List.of("-")));
    }

    @Test
    void separatorOnlyStdinHasTheNoInputError() {
        assertUsage("<stdin>: no input", () -> Words.of(
                new FakeIo(new byte[] {0x20, 0x09, 0x0D, 0x0A, 0x20}), List.of("-")));
    }

    @Test
    void malformedUtf8ReportsTheExactError() {
        assertUsage("<stdin>: input is not valid UTF-8", () -> Words.of(
                new FakeIo(new byte[] {0x31, 0x32, (byte) 0xFF, 0x0A}), List.of("-")));
    }

    @Test
    void truncatedUtf8ReportsTheExactError() {
        assertUsage("<stdin>: input is not valid UTF-8", () -> Words.of(
                new FakeIo(new byte[] {(byte) 0xE2, (byte) 0x82}), List.of("-")));
    }

    @Test
    void overlongUtf8ReportsTheExactError() {
        assertUsage("<stdin>: input is not valid UTF-8", () -> Words.of(
                new FakeIo(new byte[] {(byte) 0xC0, (byte) 0xAF}), List.of("-")));
    }

    @Test
    void encodedSurrogateReportsTheExactError() {
        assertUsage("<stdin>: input is not valid UTF-8", () -> Words.of(
                new FakeIo(new byte[] {(byte) 0xED, (byte) 0xA0, (byte) 0x80}), List.of("-")));
    }

    @Test
    void codePointAboveUnicodeMaximumReportsTheExactError() {
        assertUsage("<stdin>: input is not valid UTF-8", () -> Words.of(
                new FakeIo(new byte[] {(byte) 0xF4, (byte) 0x90, (byte) 0x80, (byte) 0x80}),
                List.of("-")));
    }

    @Test
    void readFailureHasItsDistinctExactError() {
        FakeIo io = new FakeIo(new byte[] {}, new IOException("unavailable"));

        assertUsage("<stdin>: cannot read the input", () -> Words.of(io, List.of("-")));
        assertEquals(1, io.reads);
    }

    @Test
    void ordinaryArgumentsAreReturnedUnchangedWithoutReadingStdin() {
        List<String> args = List.of("12", "café", "😀");
        FakeIo io = new FakeIo(new byte[] {(byte) 0xFF});

        assertSame(args, Words.of(io, args));
        assertEquals(0, io.reads);
    }

    @Test
    void emptyArgumentsAreReturnedWithoutReadingStdin() {
        List<String> args = List.of();
        FakeIo io = new FakeIo(new byte[] {(byte) 0xFF});

        assertSame(args, Words.of(io, args));
        assertEquals(0, io.reads);
    }

    @Test
    void dashAmongOtherArgumentsIsOrdinaryAndDoesNotReadStdin() {
        List<String> args = List.of("-", "12");
        FakeIo io = new FakeIo(new byte[] {(byte) 0xFF});

        assertSame(args, Words.of(io, args));
        assertEquals(0, io.reads);
    }

    @Test
    void dashAfterAnotherArgumentIsOrdinaryAndDoesNotReadStdin() {
        List<String> args = List.of("12", "-");
        FakeIo io = new FakeIo(new byte[] {(byte) 0xFF});

        assertSame(args, Words.of(io, args));
        assertEquals(0, io.reads);
    }

    private static void assertUsage(String expectedMessage, Executable action) {
        Failure.Usage failure = assertThrows(Failure.Usage.class, action);
        assertEquals(expectedMessage, failure.getMessage());
    }

    private static final class FakeIo implements Io {
        private final byte[] input;
        private final IOException readFailure;
        private int reads;

        private FakeIo(byte[] input) {
            this(input, null);
        }

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
            throw new AssertionError("Words must not write standard output");
        }

        @Override
        public void err(String text) {
            throw new AssertionError("Words must not write standard error");
        }
    }
}
