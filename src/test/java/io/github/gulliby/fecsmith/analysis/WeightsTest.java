package io.github.gulliby.fecsmith.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class WeightsTest {
    @Test
    void kOneHasOnlyTheZeroAndFullWeightCodewords() {
        SortedMap<Integer, Long> actual = Weights.distribution(1);

        assertEquals(new TreeMap<>(Map.of(0, 1L, 4, 1L)), actual);
        assertEquals(List.of(0, 4), List.copyOf(actual.keySet()));
        assertFalse(actual.containsKey(1));
        assertFalse(actual.containsKey(2));
        assertFalse(actual.containsKey(3));
    }

    @Test
    void kTwoHasThreeWeightFourCodewords() {
        SortedMap<Integer, Long> actual = Weights.distribution(2);

        assertEquals(new TreeMap<>(Map.of(0, 1L, 4, 3L)), actual);
        assertEquals(List.of(0, 4), List.copyOf(actual.keySet()));
        assertFalse(actual.containsKey(1));
        assertFalse(actual.containsKey(2));
        assertFalse(actual.containsKey(3));
        assertFalse(actual.containsKey(5));
    }

    @Test
    void kFourMatchesTheCompleteLiteralDistribution() {
        SortedMap<Integer, Long> actual = Weights.distribution(4);

        assertEquals(new TreeMap<>(Map.of(0, 1L, 4, 14L, 8, 1L)), actual);
        assertEquals(List.of(0, 4, 8), List.copyOf(actual.keySet()));
        assertFalse(actual.containsKey(1));
        assertFalse(actual.containsKey(2));
        assertFalse(actual.containsKey(3));
        assertFalse(actual.containsKey(5));
        assertFalse(actual.containsKey(6));
        assertFalse(actual.containsKey(7));
    }

    @Test
    void kTwelveMatchesTheCompleteLiteralDistributionAtTheUpperBoundary() {
        SortedMap<Integer, Long> actual = Weights.distribution(12);

        assertEquals(new TreeMap<>(Map.of(
                0, 1L,
                4, 148L,
                6, 560L,
                8, 1374L,
                10, 1248L,
                12, 644L,
                14, 112L,
                16, 9L)), actual);
        assertEquals(List.of(0, 4, 6, 8, 10, 12, 14, 16), List.copyOf(actual.keySet()));
        assertFalse(actual.containsKey(1));
        assertFalse(actual.containsKey(2));
        assertFalse(actual.containsKey(3));
        assertFalse(actual.containsKey(5));
        assertFalse(actual.containsKey(7));
        assertFalse(actual.containsKey(9));
        assertFalse(actual.containsKey(11));
        assertFalse(actual.containsKey(13));
        assertFalse(actual.containsKey(15));
        assertFalse(actual.containsKey(17));
        assertFalse(actual.containsKey(18));
    }
}
