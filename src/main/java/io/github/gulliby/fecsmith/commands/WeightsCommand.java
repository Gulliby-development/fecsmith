package io.github.gulliby.fecsmith.commands;

import io.github.gulliby.fecsmith.analysis.Weights;
import io.github.gulliby.fecsmith.codes.Secded;
import io.github.gulliby.fecsmith.core.Failure;
import io.github.gulliby.fecsmith.core.Io;
import io.github.gulliby.fecsmith.core.Options;
import io.github.gulliby.fecsmith.core.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Command-line adapter for SECDED weight distributions. */
public final class WeightsCommand {
    private WeightsCommand() {
    }

    public static int run(List<String> argv, Io io) {
        Options.Parsed parsed = Options.parse(argv, List.of(Options.Spec.flag("--cumulative")));
        List<String> args = parsed.args();
        boolean cumulative = parsed.values().get("--cumulative") == 1;
        if (args.isEmpty()) {
            throw new Failure.Usage("missing K");
        }
        if (args.size() > 1) {
            throw new Failure.Usage("too many arguments");
        }

        int k = Options.integer("K", args.get(0), 1, 12);
        Secded.Layout layout = Secded.forData(k);
        var distribution = Weights.distribution(k);
        int minimumDistance = distribution.keySet().stream()
                .filter(weight -> weight > 0)
                .findFirst()
                .orElseThrow();

        List<List<String>> rows = new ArrayList<>(distribution.size());
        long running = 0;
        for (Map.Entry<Integer, Long> entry : distribution.entrySet()) {
            running += entry.getValue();
            if (cumulative) {
                rows.add(List.of(Integer.toString(entry.getKey()), Long.toString(entry.getValue()), Long.toString(running)));
            } else {
                rows.add(List.of(Integer.toString(entry.getKey()), Long.toString(entry.getValue())));
            }
        }
        List<String> header = cumulative ? List.of("weight", "codewords", "cumulative") : List.of("weight", "codewords");

        String output = "secded k=" + layout.k() + " r=" + layout.r() + " n=" + layout.n()
                + ", minimum distance " + minimumDistance + "\n"
                + table(header, rows);
        io.out(output);
        return 0;
    }

    private static String table(List<String> header, List<List<String>> rows) {
        return String.join("\n", Text.renderTable(header, rows)) + "\n";
    }
}
