package io.github.gulliby.fecsmith.commands;

import io.github.gulliby.fecsmith.analysis.Burst;
import io.github.gulliby.fecsmith.codes.Crc;
import io.github.gulliby.fecsmith.core.Failure;
import io.github.gulliby.fecsmith.core.Io;
import io.github.gulliby.fecsmith.core.Options;
import io.github.gulliby.fecsmith.core.Text;

import java.util.ArrayList;
import java.util.List;

/** Command-line adapter for CRC burst analysis. */
public final class BurstCommand {
    private BurstCommand() {
    }

    public static int run(List<String> argv, Io io) {
        Options.Parsed parsed = Options.parse(argv,
                List.of(Options.Spec.integer("--max-len", 1, 18, 12)));
        List<String> args = parsed.args();
        if (args.isEmpty()) {
            throw new Failure.Usage("missing MODEL");
        }
        if (args.size() > 1) {
            throw new Failure.Usage("too many arguments");
        }

        String name = args.get(0);
        Crc.Model model = Crc.parse(name);
        int maxLength = parsed.values().get("--max-len");
        List<Burst.Row> burstRows = Burst.run(model, maxLength);
        List<List<String>> rows = new ArrayList<>(burstRows.size());
        for (Burst.Row row : burstRows) {
            rows.add(List.of(
                    Integer.toString(row.length()),
                    Long.toString(row.patterns()),
                    Long.toString(row.undetected())));
        }

        String output = name + ", width " + model.width() + ", bursts up to length " + maxLength + "\n"
                + table(List.of("length", "patterns", "undetected"), rows);
        io.out(output);
        return 0;
    }

    private static String table(List<String> header, List<List<String>> rows) {
        return String.join("\n", Text.renderTable(header, rows)) + "\n";
    }
}
