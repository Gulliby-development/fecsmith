package io.github.gulliby.fecsmith.commands;

import io.github.gulliby.fecsmith.codes.Crc;
import io.github.gulliby.fecsmith.core.Failure;
import io.github.gulliby.fecsmith.core.Hex;
import io.github.gulliby.fecsmith.core.Io;
import io.github.gulliby.fecsmith.core.Options;
import io.github.gulliby.fecsmith.core.Text;
import io.github.gulliby.fecsmith.core.Words;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Command-line adapter for CRC model listing and computation. */
public final class CrcCommand {
    private static final List<Options.Spec> SPECS = List.of(Options.Spec.flag("--list"));
    private static final List<String> CATALOG_HEADER =
            List.of("name", "width", "poly", "init", "refin", "refout", "xorout", "check");

    private CrcCommand() {
    }

    public static int run(List<String> argv, Io io) {
        Options.Parsed parsed = Options.parse(argv, SPECS);
        List<String> args = parsed.args();

        if (parsed.values().get("--list") == 1) {
            if (!args.isEmpty()) {
                throw new Failure.Usage("too many arguments");
            }
            io.out(catalogOutput());
            return 0;
        }

        if (args.isEmpty()) {
            throw new Failure.Usage("missing MODEL");
        }
        if (args.size() == 1) {
            throw new Failure.Usage("missing HEX");
        }

        String modelText = args.get(0);
        Crc.Model model = Crc.parse(modelText);
        List<String> words = Words.of(io, args.subList(1, args.size()));
        byte[] data = Hex.bytes(words);
        int digits = (model.width() + 3) / 4;
        String output = modelText + " over " + Text.plural(data.length, "byte", "bytes") + ": "
                + Hex.format(Crc.compute(model, data), digits) + "\n";
        io.out(output);
        return 0;
    }

    private static String catalogOutput() {
        List<List<String>> rows = new ArrayList<>();
        byte[] checkData = "123456789".getBytes(StandardCharsets.US_ASCII);
        for (Crc.Model model : Crc.catalog()) {
            int digits = (model.width() + 3) / 4;
            rows.add(List.of(
                    model.name(),
                    Integer.toString(model.width()),
                    Hex.format(model.poly(), digits),
                    Hex.format(model.init(), digits),
                    model.refIn() ? "yes" : "no",
                    model.refOut() ? "yes" : "no",
                    Hex.format(model.xorOut(), digits),
                    Hex.format(Crc.compute(model, checkData), digits)));
        }

        StringBuilder output = new StringBuilder();
        for (String line : Text.renderTable(CATALOG_HEADER, rows)) {
            output.append(line).append('\n');
        }
        return output.toString();
    }
}
