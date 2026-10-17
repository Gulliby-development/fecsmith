package io.github.gulliby.fecsmith.analysis;

import io.github.gulliby.fecsmith.codes.Secded;

import java.util.SortedMap;
import java.util.TreeMap;

/** Weight distribution of the extended Hamming code for a data length. */
public final class Weights {
    private Weights() {
    }

    public static SortedMap<Integer, Long> distribution(int k) {
        SortedMap<Integer, Long> counts = new TreeMap<>();
        int wordCount = 1 << k;

        for (int value = 0; value < wordCount; value++) {
            String bits = Integer.toBinaryString(value);
            String data = "0".repeat(k - bits.length()) + bits;
            String codeword = Secded.encode(data);

            int weight = 0;
            for (int i = 0; i < codeword.length(); i++) {
                if (codeword.charAt(i) == '1') {
                    weight++;
                }
            }
            counts.merge(weight, 1L, Long::sum);
        }
        return counts;
    }
}
