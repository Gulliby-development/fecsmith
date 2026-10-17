package io.github.gulliby.fecsmith.commands;

import io.github.gulliby.fecsmith.analysis.Dfree;
import io.github.gulliby.fecsmith.codes.Conv;
import io.github.gulliby.fecsmith.core.Failure;
import io.github.gulliby.fecsmith.core.Io;
import io.github.gulliby.fecsmith.core.Options;
import io.github.gulliby.fecsmith.core.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Command-line adapter for convolutional free-distance analysis. */
public final class DfreeCommand {
    private DfreeCommand() {
    }

    public static int run(List<String> argv, Io io) {
        List<String> args = Options.parse(argv, List.of()).args();
        if (args.isEmpty()) {
            throw new Failure.Usage("missing CODE");
        }
        if (args.size() > 1) {
            throw new Failure.Usage("too many arguments");
        }

        String name = args.get(0);
        Optional<Conv.Code> matchingCode = Conv.byName(name);
        if (matchingCode.isEmpty()) {
            throw new Failure.Usage("unknown code " + Text.show(name) + " (expected k3, k5 or k7)");
        }
        Conv.Code code = matchingCode.orElseThrow();
        Dfree.Result result = Dfree.find(code);

        String input = result.input();
        List<String> pairs = new ArrayList<>(result.steps());
        int state = 0;
        for (int i = 0; i < result.input().length(); i++) {
            int bit = result.input().charAt(i) - '0';
            int[] transition = Conv.step(code, state, bit);
            state = transition[0];
            pairs.add(Integer.toString(transition[1]) + transition[2]);
        }

        String output = code.name() + " (K=" + code.constraint() + ", generators "
                + Integer.toOctalString(code.generators().get(0)) + " and "
                + Integer.toOctalString(code.generators().get(1)) + " octal): free distance "
                + result.weight() + "\n"
                + line("input", input)
                + line("coded", String.join(" ", pairs));
        io.out(output);
        return 0;
    }

    private static String line(String key, String value) {
        return Text.line(key, value) + "\n";
    }
}
