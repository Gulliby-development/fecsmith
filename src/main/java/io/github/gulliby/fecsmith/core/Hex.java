package io.github.gulliby.fecsmith.core;

import java.util.Arrays;
import java.util.List;

/** Hexadecimal formatting and parsing contracts. */
public final class Hex {
    private Hex() {
    }

    public static String format(long value, int digits) {
        String hexadecimal = Long.toHexString(value);
        if (hexadecimal.length() >= digits) {
            return hexadecimal;
        }
        return "0".repeat(digits - hexadecimal.length()) + hexadecimal;
    }

    public static byte[] bytes(List<String> words) {
        byte[] parsed = new byte[4096];
        int count = 0;

        for (String word : words) {
            if (word.isEmpty() || (word.length() & 1) != 0 || !isHex(word)) {
                throw invalidHexWord(word);
            }

            int wordBytes = word.length() / 2;
            if (count + wordBytes > parsed.length) {
                throw new Failure.Usage("too many bytes (at most 4096)");
            }

            for (int i = 0; i < word.length(); i += 2) {
                parsed[count++] = (byte) ((nibble(word.charAt(i)) << 4) | nibble(word.charAt(i + 1)));
            }
        }
        return Arrays.copyOf(parsed, count);
    }

    public static int[] symbols(List<String> words, int m) {
        int digits = m <= 4 ? 1 : 2;
        int limit = 1 << m;
        int[] parsed = new int[words.size()];

        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (word.length() != digits || !isHex(word)) {
                throw invalidSymbol(word, digits, m);
            }

            int value = 0;
            for (int j = 0; j < word.length(); j++) {
                value = (value << 4) | nibble(word.charAt(j));
            }
            if (value >= limit) {
                throw invalidSymbol(word, digits, m);
            }
            parsed[i] = value;
        }
        return parsed;
    }

    private static Failure.Usage invalidHexWord(String word) {
        return new Failure.Usage("invalid hex word " + Text.show(word) + " (expected pairs of hex digits)");
    }

    private static Failure.Usage invalidSymbol(String word, int digits, int m) {
        String width = Text.plural(digits, "hex digit", "hex digits");
        return new Failure.Usage("invalid symbol " + Text.show(word)
                + " (expected " + width + " with a value below 2^" + m + ")");
    }

    private static boolean isHex(String text) {
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (!isHexDigit(character)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexDigit(char character) {
        return character >= '0' && character <= '9'
                || character >= 'a' && character <= 'f'
                || character >= 'A' && character <= 'F';
    }

    private static int nibble(char character) {
        if (character >= '0' && character <= '9') {
            return character - '0';
        }
        if (character >= 'a' && character <= 'f') {
            return character - 'a' + 10;
        }
        return character - 'A' + 10;
    }
}
