package io.github.gulliby.fecsmith;

import io.github.gulliby.fecsmith.commands.BurstCommand;
import io.github.gulliby.fecsmith.commands.ConvCommand;
import io.github.gulliby.fecsmith.commands.CrcCommand;
import io.github.gulliby.fecsmith.commands.DfreeCommand;
import io.github.gulliby.fecsmith.commands.OutcomesCommand;
import io.github.gulliby.fecsmith.commands.RsCommand;
import io.github.gulliby.fecsmith.commands.SecdedCommand;
import io.github.gulliby.fecsmith.commands.WeightsCommand;
import io.github.gulliby.fecsmith.core.Failure;
import io.github.gulliby.fecsmith.core.Io;
import io.github.gulliby.fecsmith.core.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;

/** Dispatches command-line arguments to their command adapters. */
public final class Cli {
    public static final String USAGE = "usage: fecsmith crc [--list] MODEL HEX...\n"
            + "       fecsmith secded encode|decode BITS\n"
            + "       fecsmith rs encode|decode [--m M] [--nsym N] SYMBOL...\n"
            + "       fecsmith conv encode|decode CODE BITS\n"
            + "       fecsmith weights K\n"
            + "       fecsmith outcomes [--errors N] K\n"
            + "       fecsmith burst [--max-len N] MODEL\n"
            + "       fecsmith dfree CODE";

    private static final Map<String, BiFunction<List<String>, Io, Integer>> COMMANDS = Map.of(
            "crc", CrcCommand::run,
            "secded", SecdedCommand::run,
            "rs", RsCommand::run,
            "conv", ConvCommand::run,
            "weights", WeightsCommand::run,
            "outcomes", OutcomesCommand::run,
            "burst", BurstCommand::run,
            "dfree", DfreeCommand::run);

    private Cli() {
    }

    public static int run(List<String> argv, Io io) {
        try {
            if (argv.isEmpty()) {
                throw new Failure.Usage(
                        "missing command (crc, secded, rs, conv, weights, outcomes, burst or dfree)");
            }
            if (argv.size() == 1 && argv.get(0).equals("--help")) {
                io.out(USAGE + "\n");
                return 0;
            }
            if (argv.size() == 1 && argv.get(0).equals("--version")) {
                Properties properties = new Properties();
                try (InputStream stream = Cli.class.getResourceAsStream("/fecsmith.properties")) {
                    if (stream == null) {
                        throw new Failure.Usage("cannot read the version");
                    }
                    properties.load(stream);
                } catch (IOException exception) {
                    throw new Failure.Usage("cannot read the version");
                }
                io.out("fecsmith " + properties.getProperty("version", "unknown") + "\n");
                return 0;
            }

            String command = argv.get(0);
            BiFunction<List<String>, Io, Integer> handler = COMMANDS.get(command);
            if (handler == null) {
                throw new Failure.Usage("unknown command " + Text.show(command));
            }
            return handler.apply(argv.subList(1, argv.size()), io);
        } catch (Failure.Usage failure) {
            io.err("fecsmith: " + failure.getMessage() + "\n");
            return 2;
        }
    }
}
