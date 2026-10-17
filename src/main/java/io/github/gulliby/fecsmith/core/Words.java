package io.github.gulliby.fecsmith.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Resolves command words, including the strict standard-input form. */
public final class Words {
    private Words() {
    }

    public static List<String> of(Io io, List<String> args) {
        if (args.size() != 1 || !args.get(0).equals("-")) {
            return args;
        }

        byte[] bytes;
        try {
            bytes = io.readStdin();
        } catch (IOException exception) {
            throw new Failure.Usage("<stdin>: cannot read the input");
        }

        String input;
        try {
            input = decode(bytes);
        } catch (CharacterCodingException exception) {
            throw new Failure.Usage("<stdin>: input is not valid UTF-8");
        }

        if (input.startsWith("\uFEFF")) {
            input = input.substring(1);
        }

        List<String> words = new ArrayList<>();
        int start = -1;
        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);
            if (isSeparator(character)) {
                if (start >= 0) {
                    words.add(input.substring(start, i));
                    start = -1;
                }
            } else if (start < 0) {
                start = i;
            }
        }
        if (start >= 0) {
            words.add(input.substring(start));
        }

        if (words.isEmpty()) {
            throw new Failure.Usage("<stdin>: no input");
        }
        return List.copyOf(words);
    }

    private static String decode(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
    }

    private static boolean isSeparator(char character) {
        return character == ' ' || character == '\t' || character == '\r' || character == '\n';
    }
}
