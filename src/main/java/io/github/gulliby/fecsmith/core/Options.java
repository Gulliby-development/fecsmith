package io.github.gulliby.fecsmith.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Hand-written command-line option parsing shared by commands. */
public final class Options {
    private Options() {
    }

    public record Spec(String name, boolean flag, int lo, int hi, int fallback) {
        public static Spec flag(String name) {
            return new Spec(name, true, 0, 0, 0);
        }

        public static Spec integer(String name, int lo, int hi, int fallback) {
            return new Spec(name, false, lo, hi, fallback);
        }
    }

    public record Parsed(Map<String, Integer> values, List<String> args) {
        public Parsed {
            values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
            args = List.copyOf(args);
        }
    }

    public static Parsed parse(List<String> argv, List<Spec> specs) {
        Map<String, Spec> byName = new LinkedHashMap<>();
        for (Spec spec : specs) {
            byName.put(spec.name(), spec);
        }

        Map<String, String> rawValues = new LinkedHashMap<>();
        List<String> args = new ArrayList<>();

        for (int i = 0; i < argv.size(); i++) {
            String arg = argv.get(i);
            if (!arg.startsWith("--")) {
                args.add(arg);
                continue;
            }

            int equals = arg.indexOf('=');
            String name = equals < 0 ? arg : arg.substring(0, equals);
            Spec spec = byName.get(name);
            if (spec == null) {
                throw new Failure.Usage("unknown option " + Text.show(arg));
            }

            if (spec.flag()) {
                if (equals >= 0) {
                    throw new Failure.Usage(name + " takes no value");
                }
                rawValues.put(name, null);
            } else if (equals >= 0) {
                rawValues.put(name, arg.substring(equals + 1));
            } else if (i + 1 < argv.size()) {
                rawValues.put(name, argv.get(++i));
            } else {
                throw new Failure.Usage(name + " needs a value");
            }
        }

        Map<String, Integer> values = new LinkedHashMap<>();
        for (Spec spec : specs) {
            String raw = rawValues.get(spec.name());
            if (spec.flag()) {
                values.put(spec.name(), rawValues.containsKey(spec.name()) ? 1 : 0);
            } else if (rawValues.containsKey(spec.name())) {
                values.put(spec.name(), integer(spec.name(), raw, spec.lo(), spec.hi()));
            } else {
                values.put(spec.name(), spec.fallback());
            }
        }

        return new Parsed(values, args);
    }

    public static int integer(String name, String text, int lo, int hi) {
        boolean validShape = "0".equals(text);
        if (!validShape && text != null && !text.isEmpty() && text.length() <= 6
                && text.charAt(0) >= '1' && text.charAt(0) <= '9') {
            validShape = true;
            for (int i = 1; i < text.length(); i++) {
                char digit = text.charAt(i);
                if (digit < '0' || digit > '9') {
                    validShape = false;
                    break;
                }
            }
        }

        if (validShape) {
            int value = Integer.parseInt(text);
            if (value >= lo && value <= hi) {
                return value;
            }
        }

        throw new Failure.Usage("invalid " + name + " " + Text.show(text)
                + " (expected an integer from " + lo + " to " + hi + ")");
    }
}
