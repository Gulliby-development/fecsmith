package io.github.gulliby.fecsmith.analysis;

import io.github.gulliby.fecsmith.codes.Secded;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Enumerates how SECDED handles error patterns of each requested weight. */
public final class Outcomes {
    private Outcomes() {
    }

    public record Row(int errors, long patterns, long corrected, long detected, long wrong) {
    }

    public static List<Row> run(int k, int maxErrors) {
        int n = Secded.forData(k).n();
        List<Row> rows = new ArrayList<>(maxErrors);
        for (int errors = 1; errors <= maxErrors; errors++) {
            char[] received = new char[n];
            Arrays.fill(received, '0');
            Counts counts = new Counts();
            enumerate(received, 0, errors, counts);
            rows.add(new Row(errors, counts.patterns, counts.corrected, counts.detected, counts.wrong));
        }
        return rows;
    }

    private static void enumerate(char[] word, int firstPosition, int remaining, Counts counts) {
        if (remaining == 0) {
            Secded.Decoded decoded = Secded.decode(new String(word));
            counts.patterns++;
            switch (decoded.status()) {
                case DOUBLE, OUTSIDE -> counts.detected++;
                case CORRECTED -> {
                    if (isZeroCodeword(decoded.codeword())) {
                        counts.corrected++;
                    } else {
                        counts.wrong++;
                    }
                }
                case OK -> counts.wrong++;
            }
            return;
        }

        int lastPosition = word.length - remaining;
        for (int position = firstPosition; position <= lastPosition; position++) {
            word[position] = '1';
            enumerate(word, position + 1, remaining - 1, counts);
            word[position] = '0';
        }
    }

    private static boolean isZeroCodeword(String codeword) {
        for (int i = 0; i < codeword.length(); i++) {
            if (codeword.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }

    private static final class Counts {
        private long patterns;
        private long corrected;
        private long detected;
        private long wrong;
    }
}
