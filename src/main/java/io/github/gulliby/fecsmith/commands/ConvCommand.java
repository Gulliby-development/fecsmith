package io.github.gulliby.fecsmith.commands;

import io.github.gulliby.fecsmith.codes.Conv;
import io.github.gulliby.fecsmith.core.Failure;
import io.github.gulliby.fecsmith.core.Io;
import io.github.gulliby.fecsmith.core.Options;
import io.github.gulliby.fecsmith.core.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Command-line adapter for convolutional coding. */
public final class ConvCommand {
    private ConvCommand() {
    }

    public static int run(List<String> argv, Io io) {
        List<String> args = Options.parse(argv, List.of()).args();
        if (args.isEmpty()) {
            throw new Failure.Usage("missing mode (encode or decode)");
        }
        String mode = args.get(0);
        if (!mode.equals("encode") && !mode.equals("decode")) {
            throw new Failure.Usage("unknown mode " + Text.show(mode) + " (expected encode or decode)");
        }
        if (args.size() == 1) {
            throw new Failure.Usage("missing CODE");
        }
        if (args.size() == 2) {
            throw new Failure.Usage("missing BITS");
        }
        if (args.size() > 3) {
            throw new Failure.Usage("too many arguments");
        }

        String name = args.get(1);
        String bits = args.get(2);
        Optional<Conv.Code> matchingCode = Conv.byName(name);
        if (matchingCode.isEmpty()) {
            throw new Failure.Usage("unknown code " + Text.show(name) + " (expected k3, k5 or k7)");
        }
        Conv.Code code = matchingCode.orElseThrow();

        int lo = mode.equals("encode") ? 1 : 2;
        int hi = mode.equals("encode") ? 200 : 400;
        validateBits(bits, lo, hi);
        if (mode.equals("decode")) {
            int length = bits.length();
            int min = 2 * code.constraint();
            if ((length & 1) != 0 || length < min || length > 400) {
                throw new Failure.Usage("invalid received length " + length + " for " + name
                        + " (expected an even number from " + min + " to 400)");
            }
        }

        String output;
        if (mode.equals("encode")) {
            int[] input = parseBits(bits);
            List<int[]> encoded = Conv.encode(code, input);
            List<String> pairs = new ArrayList<>(encoded.size());
            for (int[] pair : encoded) {
                pairs.add(pairText(pair));
            }
            output = header(code)
                    + line("input", bits)
                    + line("coded", String.join(" ", pairs));
        } else {
            List<int[]> received = new ArrayList<>(bits.length() / 2);
            for (int i = 0; i < bits.length(); i += 2) {
                received.add(new int[]{bits.charAt(i) - '0', bits.charAt(i + 1) - '0'});
            }
            Conv.Decoded decoded = Conv.decode(code, received);
            String message = bitsText(decoded.message());
            String result = decoded.errors() == 0
                    ? "ok"
                    : "corrected " + decoded.errors() + " channel bit "
                            + (decoded.errors() == 1 ? "error" : "errors");
            output = header(code)
                    + line("status", result)
                    + line("message", message);
        }

        io.out(output);
        return 0;
    }

    private static String header(Conv.Code code) {
        return "conv " + code.name() + " (K=" + code.constraint() + ", generators "
                + Integer.toOctalString(code.generators().get(0)) + " and "
                + Integer.toOctalString(code.generators().get(1)) + " octal)\n";
    }

    private static String line(String key, String value) {
        return Text.line(key, value) + "\n";
    }

    private static String pairText(int[] pair) {
        return Integer.toString(pair[0]) + pair[1];
    }

    private static void validateBits(String bits, int lo, int hi) {
        boolean valid = bits.length() >= lo && bits.length() <= hi;
        for (int i = 0; valid && i < bits.length(); i++) {
            valid = bits.charAt(i) == '0' || bits.charAt(i) == '1';
        }
        if (!valid) {
            throw new Failure.Usage("invalid bit string " + Text.show(bits)
                    + " (expected " + lo + " to " + hi + " digits 0 or 1)");
        }
    }

    private static int[] parseBits(String bits) {
        int[] parsed = new int[bits.length()];
        for (int i = 0; i < bits.length(); i++) {
            parsed[i] = bits.charAt(i) - '0';
        }
        return parsed;
    }

    private static String bitsText(int[] bits) {
        StringBuilder text = new StringBuilder(bits.length);
        for (int bit : bits) {
            text.append(bit);
        }
        return text.toString();
    }
}
