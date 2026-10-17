package io.github.gulliby.fecsmith.analysis;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.github.gulliby.fecsmith.codes.Conv;
import org.junit.jupiter.api.Test;

class DfreeTest {
    @Test
    void k3FindsTheLiteralMinimumFirstReturnWitness() {
        assertWitness(
                "k3",
                5,
                3,
                "100",
                new int[]{2, 1, 0},
                new int[][]{{1, 1}, {1, 0}, {1, 1}});
    }

    @Test
    void k5FindsTheLiteralMinimumFirstReturnWitness() {
        assertWitness(
                "k5",
                7,
                5,
                "10000",
                new int[]{8, 4, 2, 1, 0},
                new int[][]{{1, 1}, {0, 1}, {0, 1}, {1, 0}, {1, 1}});
    }

    @Test
    void k7FindsTheLiteralMinimumFirstReturnWitness() {
        assertWitness(
                "k7",
                10,
                7,
                "1000000",
                new int[]{32, 16, 8, 4, 2, 1, 0},
                new int[][]{{1, 1}, {1, 0}, {1, 1}, {1, 1}, {0, 0}, {0, 1}, {1, 1}});
    }

    private static void assertWitness(
            String codeName,
            int expectedWeight,
            int expectedSteps,
            String expectedInput,
            int[] expectedStates,
            int[][] expectedPairs) {
        Conv.Code code = Conv.byName(codeName).orElseThrow();
        Dfree.Result result = Dfree.find(code);

        assertEquals(expectedWeight, result.weight(), codeName + " free distance");
        assertEquals(expectedSteps, result.steps(), codeName + " witness steps");
        assertEquals(expectedInput, result.input(), codeName + " first-return input");
        assertEquals(expectedSteps, result.input().length(), "steps equal input length");
        assertEquals(expectedInput.length(), expectedStates.length, "literal state count");
        assertEquals(expectedInput.length(), expectedPairs.length, "one output pair per input bit");

        int state = 0;
        int observedWeight = 0;
        for (int index = 0; index < result.input().length(); index++) {
            int input = result.input().charAt(index) - '0';
            int[] transition = Conv.step(code, state, input);

            assertEquals(expectedStates[index], transition[0],
                    codeName + " state after input index " + index);
            assertArrayEquals(expectedPairs[index],
                    new int[]{transition[1], transition[2]},
                    codeName + " output pair at input index " + index);
            if (index < result.input().length() - 1) {
                assertNotEquals(0, transition[0],
                        codeName + " must not return before the final input");
            }

            observedWeight += transition[1] + transition[2];
            state = transition[0];
        }

        assertEquals(0, state, codeName + " ends in state zero");
        assertEquals(expectedWeight, observedWeight, codeName + " replayed output weight");
    }
}
