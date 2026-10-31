package io.github.gulliby.fecsmith.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.gulliby.fecsmith.Cli;
import io.github.gulliby.fecsmith.core.Io;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WeightsCumulativeTest {
    @Test
    void cumulativeRows() {
        row(List.of("weights", "--cumulative", "1"),
                0, "secded k=1 r=2 n=4, minimum distance 4\nweight  codewords  cumulative\n0       1          1\n4       1          2\n", "");
        row(List.of("weights", "--cumulative", "2"),
                0, "secded k=2 r=3 n=6, minimum distance 4\nweight  codewords  cumulative\n0       1          1\n4       3          4\n", "");
        row(List.of("weights", "--cumulative", "4"),
                0, "secded k=4 r=3 n=8, minimum distance 4\nweight  codewords  cumulative\n0       1          1\n4       14         15\n8       1          16\n", "");
        row(List.of("weights", "4", "--cumulative"),
                0, "secded k=4 r=3 n=8, minimum distance 4\nweight  codewords  cumulative\n0       1          1\n4       14         15\n8       1          16\n", "");
        row(List.of("weights", "--cumulative", "--cumulative", "3"),
                0, "secded k=3 r=3 n=7, minimum distance 4\nweight  codewords  cumulative\n0       1          1\n4       7          8\n", "");
        row(List.of("weights", "--cumulative", "6"),
                0, "secded k=6 r=4 n=11, minimum distance 4\nweight  codewords  cumulative\n0       1          1\n4       26         27\n6       24         51\n8       13         64\n", "");
        row(List.of("weights", "--cumulative", "12"),
                0, "secded k=12 r=5 n=18, minimum distance 4\nweight  codewords  cumulative\n0       1          1\n4       148        149\n6       560        709\n8       1374       2083\n10      1248       3331\n12      644        3975\n14      112        4087\n16      9          4096\n", "");
        row(List.of("weights", "3"),
                0, "secded k=3 r=3 n=7, minimum distance 4\nweight  codewords\n0       1\n4       7\n", "");
        row(List.of("weights", "--cumulative=1", "4"),
                2, "", "fecsmith: --cumulative takes no value\n");
        row(List.of("weights", "--cumulative"),
                2, "", "fecsmith: missing K\n");
        row(List.of("weights", "--cumulative", "4", "5"),
                2, "", "fecsmith: too many arguments\n");
        row(List.of("weights", "--cumulative", "13"),
                2, "", "fecsmith: invalid K \"13\" (expected an integer from 1 to 12)\n");
        row(List.of("weights", "--cumulative", "0"),
                2, "", "fecsmith: invalid K \"0\" (expected an integer from 1 to 12)\n");
        row(List.of("weights", "--cumulative", "--unknown", "4"),
                2, "", "fecsmith: unknown option \"--unknown\"\n");
        row(List.of("weights", "--cum", "4"),
                2, "", "fecsmith: unknown option \"--cum\"\n");
        row(List.of("weights", "--cumulative", "--", "4"),
                2, "", "fecsmith: unknown option \"--\"\n");
        row(List.of("outcomes", "--cumulative", "5"),
                2, "", "fecsmith: unknown option \"--cumulative\"\n");
    }

    private static void row(List<String> argv, int status, String out, String err) {
        FakeIo io = new FakeIo();

        int got = Cli.run(argv, io);

        assertEquals(status, got, argv.toString());
        assertEquals(out.isEmpty() ? List.of() : List.of(out), io.outputs, argv.toString());
        assertEquals(err.isEmpty() ? List.of() : List.of(err), io.errors, argv.toString());
    }

    private static final class FakeIo implements Io {
        private final ArrayList<String> outputs = new ArrayList<>();
        private final ArrayList<String> errors = new ArrayList<>();

        @Override
        public byte[] readStdin() throws IOException {
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
