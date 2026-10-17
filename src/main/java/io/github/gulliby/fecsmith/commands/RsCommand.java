package io.github.gulliby.fecsmith.commands;

import io.github.gulliby.fecsmith.codes.Reed;
import io.github.gulliby.fecsmith.core.Failure;
import io.github.gulliby.fecsmith.core.Hex;
import io.github.gulliby.fecsmith.core.Io;
import io.github.gulliby.fecsmith.core.Options;
import io.github.gulliby.fecsmith.core.Text;
import io.github.gulliby.fecsmith.core.Words;
import io.github.gulliby.fecsmith.gf.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/** Command-line adapter for systematic Reed-Solomon encoding and decoding. */
public final class RsCommand {
    private static final List<Options.Spec> SPECS = List.of(
            Options.Spec.integer("--m", 3, 8, 8),
            Options.Spec.integer("--nsym", 2, 32, 4));

    private RsCommand() {
    }

    public static int run(List<String> argv, Io io) {
        Options.Parsed parsed = Options.parse(argv, SPECS);
        List<String> args = parsed.args();

        if (args.isEmpty()) {
            throw new Failure.Usage("missing mode (encode or decode)");
        }
        String mode = args.get(0);
        if (!mode.equals("encode") && !mode.equals("decode")) {
            throw new Failure.Usage("unknown mode " + Text.show(mode) + " (expected encode or decode)");
        }
        if (args.size() == 1) {
            throw new Failure.Usage("missing SYMBOL");
        }

        int m = parsed.values().get("--m");
        int nsym = parsed.values().get("--nsym");
        List<String> words = Words.of(io, args.subList(1, args.size()));
        int[] symbols = Hex.symbols(words, m);
        int limit = (1 << m) - 1;
        int n = symbols.length;
        if (mode.equals("encode")) {
            n += nsym;
            if (n > limit) {
                throw new Failure.Usage("codeword of " + n + " symbols does not fit in GF(2^" + m
                        + ") (at most " + limit + ")");
            }
        } else {
            if (n > limit) {
                throw new Failure.Usage("received word of " + n + " symbols does not fit in GF(2^" + m
                        + ") (at most " + limit + ")");
            }
            if (n <= nsym) {
                throw new Failure.Usage("received word of " + n + " symbols is not longer than --nsym " + nsym);
            }
        }

        Reed.Code code = new Reed.Code(Field.of(m), nsym);
        String output;
        int status = 0;
        if (mode.equals("encode")) {
            int[] parity = Reed.parity(code, symbols);
            int[] codeword = new int[symbols.length + parity.length];
            System.arraycopy(symbols, 0, codeword, 0, symbols.length);
            System.arraycopy(parity, 0, codeword, symbols.length, parity.length);

            output = header(codeword.length, symbols.length, m, code.t())
                    + line("codeword", formatSymbols(codeword, m))
                    + line("parity", formatSymbols(parity, m));
        } else {
            Reed.Decoded decoded = Reed.decode(code, symbols);
            StringBuilder result = new StringBuilder(header(n, n - nsym, m, code.t()));
            result.append(line("syndrome", formatSymbols(decoded.syndromes(), m)));
            if (!decoded.ok()) {
                result.append(line("status", "uncorrectable"));
                output = result.toString();
                status = 1;
            } else {
                SortedMap<Integer, Integer> errors = decoded.errors();
                if (errors.isEmpty()) {
                    result.append(line("status", "ok"));
                } else {
                    result.append(line("status", "corrected " + errors.size() + " "
                            + (errors.size() == 1 ? "error" : "errors")));
                    result.append(line("errors", formatErrors(errors, m)));
                }

                int[] codeword = symbols.clone();
                for (Map.Entry<Integer, Integer> error : errors.entrySet()) {
                    codeword[error.getKey()] ^= error.getValue();
                }
                result.append(line("codeword", formatSymbols(codeword, m)));
                result.append(line("message", formatSymbols(copyPrefix(codeword, n - nsym), m)));
                output = result.toString();
            }
        }

        io.out(output);
        return status;
    }

    private static String header(int n, int k, int m, int t) {
        return "RS(" + n + "," + k + ") over GF(2^" + m + "), t=" + t + "\n";
    }

    private static String line(String key, String value) {
        return Text.line(key, value) + "\n";
    }

    private static String formatSymbols(int[] symbols, int m) {
        int digits = m <= 4 ? 1 : 2;
        List<String> formatted = new ArrayList<>(symbols.length);
        for (int symbol : symbols) {
            formatted.add(Hex.format(symbol, digits));
        }
        return String.join(" ", formatted);
    }

    private static String formatErrors(SortedMap<Integer, Integer> errors, int m) {
        int digits = m <= 4 ? 1 : 2;
        List<String> formatted = new ArrayList<>(errors.size());
        for (Map.Entry<Integer, Integer> error : errors.entrySet()) {
            formatted.add(error.getKey() + ":" + Hex.format(error.getValue(), digits));
        }
        return String.join(" ", formatted);
    }

    private static int[] copyPrefix(int[] values, int length) {
        int[] prefix = new int[length];
        System.arraycopy(values, 0, prefix, 0, length);
        return prefix;
    }
}
