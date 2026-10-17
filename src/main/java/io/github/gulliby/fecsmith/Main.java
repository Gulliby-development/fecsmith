package io.github.gulliby.fecsmith;

import io.github.gulliby.fecsmith.core.Io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Portable process entry point for fecsmith. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Io io = new Io() {
            @Override
            public byte[] readStdin() throws IOException {
                return System.in.readAllBytes();
            }

            @Override
            public void out(String text) {
                try {
                    System.out.write(text.getBytes(StandardCharsets.UTF_8));
                    System.out.flush();
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            }

            @Override
            public void err(String text) {
                try {
                    System.err.write(text.getBytes(StandardCharsets.UTF_8));
                    System.err.flush();
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            }
        };
        System.exit(Cli.run(List.of(args), io));
    }
}
