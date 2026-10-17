package io.github.gulliby.fecsmith.analysis;

import io.github.gulliby.fecsmith.codes.Conv;

import java.util.Comparator;
import java.util.PriorityQueue;

/** Finds minimum-weight first-return paths in convolutional encoders. */
public final class Dfree {
    private static final Comparator<Label> ORDER = Comparator
            .comparingInt(Label::weight)
            .thenComparingInt(Label::steps)
            .thenComparing(Label::input)
            .thenComparingInt(Label::state);

    private Dfree() {
    }

    public record Result(int weight, int steps, String input) {
    }

    private record Label(int state, int weight, int steps, String input) {
    }

    /**
     * Finds the best path that leaves zero on input 1 and first returns to zero.
     *
     * <p>For each nonzero state, only its best label is retained. Any continuation
     * from the same state adds the same weight and steps and appends the same
     * input suffix, so this ordering is preserved. A cycle cannot improve a
     * label: it adds steps and nonnegative weight. Thus the finite-state search
     * needs no arbitrary path-length cutoff.</p>
     */
    public static Result find(Conv.Code code) {
        int[] firstStep = Conv.step(code, 0, 1);
        Label first = new Label(
                firstStep[0],
                firstStep[1] + firstStep[2],
                1,
                "1");
        int stateCount = 1 << (code.constraint() - 1);
        Label[] bestByState = new Label[stateCount];
        bestByState[first.state()] = first;

        PriorityQueue<Label> pending = new PriorityQueue<>(ORDER);
        pending.add(first);
        Label bestReturn = null;

        while (!pending.isEmpty()) {
            Label current = pending.remove();
            if (!current.equals(bestByState[current.state()])) {
                continue;
            }

            for (int input = 0; input <= 1; input++) {
                int[] transition = Conv.step(code, current.state(), input);
                Label candidate = new Label(
                        transition[0],
                        current.weight() + transition[1] + transition[2],
                        current.steps() + 1,
                        current.input() + input);

                if (candidate.state() == 0) {
                    if (bestReturn == null || ORDER.compare(candidate, bestReturn) < 0) {
                        bestReturn = candidate;
                    }
                } else {
                    Label previous = bestByState[candidate.state()];
                    if (previous == null || ORDER.compare(candidate, previous) < 0) {
                        bestByState[candidate.state()] = candidate;
                        pending.add(candidate);
                    }
                }
            }
        }

        return new Result(bestReturn.weight(), bestReturn.steps(), bestReturn.input());
    }
}
