package io.github.gulliby.fecsmith.commands;

import io.github.gulliby.fecsmith.analysis.Outcomes;
import io.github.gulliby.fecsmith.codes.Secded;
import io.github.gulliby.fecsmith.core.Failure;
import io.github.gulliby.fecsmith.core.Io;
import io.github.gulliby.fecsmith.core.Options;
import io.github.gulliby.fecsmith.core.Text;

import java.util.ArrayList;
import java.util.List;

/** Command-line adapter for SECDED decoder outcome analysis. */
public final class OutcomesCommand {
    private OutcomesCommand() {
    }

    public static int run(List<String> argv, Io io) {
        Options.Parsed parsed = Options.parse(argv,
                List.of(Options.Spec.integer("--errors", 1, 3, 3)));
        List<String> args = parsed.args();
        if (args.isEmpty()) {
            throw new Failure.Usage("missing K");
        }
        if (args.size() > 1) {
            throw new Failure.Usage("too many arguments");
        }

        int k = Options.integer("K", args.get(0), 1, 64);
        int maxErrors = parsed.values().get("--errors");
        Secded.Layout layout = Secded.forData(k);
        List<Outcomes.Row> outcomes = Outcomes.run(k, maxErrors);
        List<List<String>> rows = new ArrayList<>(outcomes.size());
        for (Outcomes.Row row : outcomes) {
            rows.add(List.of(
                    Integer.toString(row.errors()),
                    Long.toString(row.patterns()),
                    Long.toString(row.corrected()),
                    Long.toString(row.detected()),
                    Long.toString(row.wrong())));
        }

        String output = "secded k=" + layout.k() + " r=" + layout.r() + " n=" + layout.n() + "\n"
                + table(List.of("errors", "patterns", "corrected", "detected", "wrong"), rows);
        io.out(output);
        return 0;
    }

    private static String table(List<String> header, List<List<String>> rows) {
        return String.join("\n", Text.renderTable(header, rows)) + "\n";
    }
}
