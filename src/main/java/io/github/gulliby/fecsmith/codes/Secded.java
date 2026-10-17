package io.github.gulliby.fecsmith.codes;

import java.util.Optional;

/** Extended Hamming (SECDED) layout, encoder, and decoder. */
public final class Secded {
    private Secded() {
    }

    public record Layout(int k, int r, int n) {
    }

    public static Layout forData(int k) {
        int r = 0;
        while ((1 << r) < k + r + 1) {
            r++;
        }
        return new Layout(k, r, k + r + 1);
    }

    public static Optional<Layout> forLength(int n) {
        for (int k = 1; k <= 120; k++) {
            Layout layout = forData(k);
            if (layout.n() == n) {
                return Optional.of(layout);
            }
        }
        return Optional.empty();
    }

    public static int[] dataPositions(int k) {
        Layout layout = forData(k);
        int[] positions = new int[k];
        int count = 0;
        for (int position = 1; position < layout.n(); position++) {
            if ((position & (position - 1)) != 0) {
                positions[count++] = position;
            }
        }
        return positions;
    }

    public static String encode(String data) {
        int k = data.length();
        Layout layout = forData(k);
        char[] codeword = new char[layout.n()];
        java.util.Arrays.fill(codeword, '0');

        int[] positions = dataPositions(k);
        for (int i = 0; i < k; i++) {
            codeword[positions[i]] = data.charAt(i);
        }

        for (int parityPosition = 1; parityPosition < layout.n(); parityPosition <<= 1) {
            int parity = 0;
            for (int position = 1; position < layout.n(); position++) {
                if ((position & parityPosition) != 0 && position != parityPosition
                        && codeword[position] == '1') {
                    parity ^= 1;
                }
            }
            codeword[parityPosition] = parity == 0 ? '0' : '1';
        }

        int overall = 0;
        for (int position = 1; position < layout.n(); position++) {
            if (codeword[position] == '1') {
                overall ^= 1;
            }
        }
        codeword[0] = overall == 0 ? '0' : '1';
        return new String(codeword);
    }

    public enum Status {
        OK, CORRECTED, DOUBLE, OUTSIDE
    }

    public record Decoded(Status status, int syndrome, boolean odd, int position,
                          String codeword, String data) {
    }

    public static Decoded decode(String codeword) {
        int n = codeword.length();
        Layout layout = forLength(n).orElseThrow();
        int syndrome = 0;
        int parity = 0;
        for (int position = 0; position < n; position++) {
            if (codeword.charAt(position) == '1') {
                parity ^= 1;
                if (position > 0) {
                    syndrome ^= position;
                }
            }
        }
        boolean odd = parity != 0;

        if (!odd && syndrome != 0) {
            return new Decoded(Status.DOUBLE, syndrome, false, -1, null, null);
        }
        if (odd && syndrome > n - 1) {
            return new Decoded(Status.OUTSIDE, syndrome, true, -1, null, null);
        }

        Status status = odd ? Status.CORRECTED : Status.OK;
        int correctedPosition = odd ? syndrome : -1;
        char[] corrected = codeword.toCharArray();
        if (odd) {
            corrected[syndrome] = corrected[syndrome] == '1' ? '0' : '1';
        }

        int[] positions = dataPositions(layout.k());
        StringBuilder data = new StringBuilder(layout.k());
        for (int position : positions) {
            data.append(corrected[position]);
        }
        return new Decoded(status, syndrome, odd, correctedPosition,
                new String(corrected), data.toString());
    }
}
