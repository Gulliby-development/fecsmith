package io.github.gulliby.fecsmith.codes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Convolutional coding, terminated encoding, and Viterbi decoding. */
public final class Conv {
    private static final List<Code> CATALOG = List.of(
            new Code("k3", 3, List.of(07, 05)),
            new Code("k5", 5, List.of(023, 035)),
            new Code("k7", 7, List.of(0171, 0133)));
    private static final List<String> NAMES = List.of("k3", "k5", "k7");

    private Conv() {
    }

    public record Code(String name, int constraint, List<Integer> generators) {
    }

    public record Decoded(int[] message, int errors) {
    }

    public static Optional<Code> byName(String name) {
        for (Code code : CATALOG) {
            if (code.name().equals(name)) {
                return Optional.of(code);
            }
        }
        return Optional.empty();
    }

    public static List<String> names() {
        return NAMES;
    }

    /**
     * Advances one input through the encoder.
     *
     * @return the next state followed by the output of each generator
     */
    public static int[] step(Code c, int state, int input) {
        int constraint = c.constraint();
        int register = (input << (constraint - 1)) | state;
        int output0 = Integer.bitCount(register & c.generators().get(0)) & 1;
        int output1 = Integer.bitCount(register & c.generators().get(1)) & 1;
        int nextState = (input << (constraint - 2)) | (state >> 1);
        return new int[]{nextState, output0, output1};
    }

    public static List<int[]> encode(Code c, int[] bits) {
        List<int[]> encoded = new ArrayList<>(bits.length + c.constraint() - 1);
        int state = 0;
        for (int bit : bits) {
            int[] result = step(c, state, bit);
            state = result[0];
            encoded.add(new int[]{result[1], result[2]});
        }
        for (int i = 0; i < c.constraint() - 1; i++) {
            int[] result = step(c, state, 0);
            state = result[0];
            encoded.add(new int[]{result[1], result[2]});
        }
        return encoded;
    }

    public static Decoded decode(Code c, List<int[]> received) {
        int stateCount = 1 << (c.constraint() - 1);
        int stateMask = stateCount - 1;
        int steps = received.size();
        int unreachable = Integer.MAX_VALUE / 4;
        int[] metrics = new int[stateCount];
        Arrays.fill(metrics, unreachable);
        metrics[0] = 0;
        int[][] predecessors = new int[steps][stateCount];

        for (int time = 0; time < steps; time++) {
            int[] pair = received.get(time);
            int[] nextMetrics = new int[stateCount];
            Arrays.fill(nextMetrics, unreachable);
            for (int destination = 0; destination < stateCount; destination++) {
                int input = destination >> (c.constraint() - 2);
                int predecessor0 = (destination << 1) & stateMask;
                int predecessor1 = predecessor0 + 1;
                int[] output0 = step(c, predecessor0, input);
                int[] output1 = step(c, predecessor1, input);
                int distance0 = (output0[1] == pair[0] ? 0 : 1)
                        + (output0[2] == pair[1] ? 0 : 1);
                int distance1 = (output1[1] == pair[0] ? 0 : 1)
                        + (output1[2] == pair[1] ? 0 : 1);
                int metric0 = metrics[predecessor0] == unreachable
                        ? unreachable : metrics[predecessor0] + distance0;
                int metric1 = metrics[predecessor1] == unreachable
                        ? unreachable : metrics[predecessor1] + distance1;

                if (metric0 <= metric1) {
                    nextMetrics[destination] = metric0;
                    predecessors[time][destination] = predecessor0;
                } else {
                    nextMetrics[destination] = metric1;
                    predecessors[time][destination] = predecessor1;
                }
            }
            metrics = nextMetrics;
        }

        int[] pathInputs = new int[steps];
        int state = 0;
        for (int time = steps - 1; time >= 0; time--) {
            pathInputs[time] = state >> (c.constraint() - 2);
            state = predecessors[time][state];
        }
        int messageLength = steps - (c.constraint() - 1);
        return new Decoded(Arrays.copyOf(pathInputs, messageLength), metrics[0]);
    }
}
