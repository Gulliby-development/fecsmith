package io.github.gulliby.fecsmith.commands;

import io.github.gulliby.fecsmith.codes.Secded;
import io.github.gulliby.fecsmith.core.Failure;
import io.github.gulliby.fecsmith.core.Io;
import io.github.gulliby.fecsmith.core.Options;
import io.github.gulliby.fecsmith.core.Text;

import java.util.List;

/** Command-line adapter for extended Hamming codes. */
public final class SecdedCommand {
    private SecdedCommand() {
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
            throw new Failure.Usage("missing BITS");
        }
        if (args.size() > 2) {
            throw new Failure.Usage("too many arguments");
        }

        String bits = args.get(1);
        int lo = mode.equals("encode") ? 1 : 4;
        int hi = mode.equals("encode") ? 120 : 128;
        validateBits(bits, lo, hi);

        String output;
        int status = 0;
        if (mode.equals("encode")) {
            Secded.Layout layout = Secded.forData(bits.length());
            String codeword = Secded.encode(bits);
            output = header(layout)
                    + line("data", bits)
                    + line("codeword", codeword);
        } else {
            Secded.Layout layout = Secded.forLength(bits.length())
                    .orElseThrow(() -> new Failure.Usage("invalid codeword length " + bits.length()
                            + " (no data size has this length)"));
            Secded.Decoded decoded = Secded.decode(bits);
            StringBuilder result = new StringBuilder(header(layout));
            result.append(line("syndrome", Integer.toString(decoded.syndrome())));
            result.append(line("parity", decoded.odd() ? "odd" : "even"));
            switch (decoded.status()) {
                case OK -> {
                    result.append(line("status", "ok"));
                    result.append(line("codeword", decoded.codeword()));
                    result.append(line("data", decoded.data()));
                }
                case CORRECTED -> {
                    result.append(line("status", "corrected bit " + decoded.position()));
                    result.append(line("codeword", decoded.codeword()));
                    result.append(line("data", decoded.data()));
                }
                case DOUBLE -> {
                    result.append(line("status", "double error detected"));
                    status = 1;
                }
                case OUTSIDE -> {
                    result.append(line("status", "uncorrectable (syndrome " + decoded.syndrome()
                            + " is outside the word)"));
                    status = 1;
                }
            }
            output = result.toString();
        }

        io.out(output);
        return status;
    }

    private static String header(Secded.Layout layout) {
        return "secded k=" + layout.k() + " r=" + layout.r() + " n=" + layout.n() + "\n";
    }

    private static String line(String key, String value) {
        return Text.line(key, value) + "\n";
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

}
