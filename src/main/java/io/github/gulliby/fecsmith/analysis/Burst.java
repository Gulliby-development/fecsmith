package io.github.gulliby.fecsmith.analysis;

import io.github.gulliby.fecsmith.codes.Crc;

import java.util.ArrayList;
import java.util.List;

/** Counts CRC-undetected bursts by polynomial divisibility. */
public final class Burst {
    private Burst() {
    }

    /** Counts for one burst length. */
    public record Row(int length, long patterns, long undetected) {
    }

    public static List<Row> run(Crc.Model model, int maxLen) {
        long generator = Crc.generator(model);
        List<Row> rows = new ArrayList<>(maxLen);

        for (int length = 1; length <= maxLen; length++) {
            long patternCount = length == 1 ? 1L : 1L << (length - 2);
            long undetected = 0L;
            for (long interior = 0L; interior < patternCount; interior++) {
                long pattern = length == 1
                        ? 1L
                        : (1L << (length - 1)) | (interior << 1) | 1L;
                if (remainder(pattern, generator) == 0L) {
                    undetected++;
                }
            }
            rows.add(new Row(length, patternCount, undetected));
        }

        return List.copyOf(rows);
    }

    private static long remainder(long dividend, long divisor) {
        int divisorDegree = degree(divisor);
        int dividendDegree = degree(dividend);
        while (dividendDegree >= divisorDegree) {
            dividend ^= divisor << (dividendDegree - divisorDegree);
            dividendDegree = degree(dividend);
        }
        return dividend;
    }

    private static int degree(long polynomial) {
        return 63 - Long.numberOfLeadingZeros(polynomial);
    }
}
