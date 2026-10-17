package io.github.gulliby.fecsmith;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.gulliby.fecsmith.core.Io;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class MainTest {
    private static final Pattern FENCED_BLOCK = Pattern.compile(
            "(?m)^\\x60{3}([^\\r\\n]*)\\n(.*?)^\\x60{3}[ \\t]*(?:\\n|$)",
            Pattern.DOTALL);

    @Test
    void readmeExamplesMatchCliAndUsage() throws IOException {
        String markdown = java.nio.file.Files.readString(Path.of("README.md"), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        List<CodeBlock> blocks = fencedBlocks(markdown);

        List<Integer> usageIndexes = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            if (block.language().equals("text") && block.body().startsWith("usage: fecsmith ")) {
                usageIndexes.add(i);
            }
        }
        assertEquals(1, usageIndexes.size(), "README must contain one usage block");
        int usageIndex = usageIndexes.get(0);
        assertEquals(Cli.USAGE + "\n", blocks.get(usageIndex).body());

        List<Example> examples = new ArrayList<>();
        List<Integer> pairedOutputIndexes = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock commandBlock = blocks.get(i);
            if (!commandBlock.language().equals("sh") || !commandBlock.body().startsWith("fecsmith ")) {
                continue;
            }

            assertTrue(i + 1 < blocks.size(), "README example command is missing its output block");
            CodeBlock outputBlock = blocks.get(i + 1);
            assertEquals("text", outputBlock.language(), "README example command must be followed by text output");
            String command = commandBlock.body();
            if (command.endsWith("\n")) {
                command = command.substring(0, command.length() - 1);
            }
            assertFalse(command.contains("\n"), "README example commands must be one line");

            List<String> arguments = new ArrayList<>(List.of(command.split("\\s+")));
            assertEquals("fecsmith", arguments.remove(0), "remove only the documented launcher shorthand");
            examples.add(new Example(List.copyOf(arguments), outputBlock.body()));
            pairedOutputIndexes.add(i + 1);
        }

        assertEquals(List.of(
                List.of("crc", "crc32", "313233343536373839"),
                List.of("secded", "decode", "0010001100"),
                List.of("rs", "decode", "48", "00", "6c", "6c", "ff", "08", "47", "90", "ca")),
                examples.stream().map(Example::arguments).toList());
        assertEquals(3, examples.size(), "README must contain exactly the three prescribed examples");
        assertEquals(3, pairedOutputIndexes.size());
        assertEquals(4, blocks.stream().filter(block -> block.language().equals("text")).count(),
                "README must contain usage plus exactly three output blocks");
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).language().equals("text") && i != usageIndex) {
                assertTrue(pairedOutputIndexes.contains(i), "README contains an unpaired text output block");
            }
        }

        for (Example example : examples) {
            FakeIo io = new FakeIo();
            assertEquals(0, Cli.run(example.arguments(), io));
            assertEquals(example.output(), io.stdout.toString());
            assertEquals("", io.stderr.toString());
        }
    }

    @Test
    void malformedUtf8OnStandardInputIsRejectedByTheProcessEntryPoint() throws Exception {
        Path java = Path.of(System.getProperty("java.home"), "bin", "java");
        Process process = new ProcessBuilder(
                java.toString(),
                "-cp",
                "target/classes",
                "io.github.gulliby.fecsmith.Main",
                "crc",
                "crc32",
                "-")
                .directory(Path.of(System.getProperty("user.dir")).toFile())
                .start();
        CompletableFuture<byte[]> stdout = readBytesAsync(process.getInputStream());
        CompletableFuture<byte[]> stderr = readBytesAsync(process.getErrorStream());

        try {
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(new byte[] {0x31, 0x32, (byte) 0xFF, 0x0A});
            }

            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "fecsmith process did not exit within 30 seconds");
            assertEquals(2, process.exitValue());
            assertArrayEquals(new byte[0], stdout.get(30, TimeUnit.SECONDS));
            assertArrayEquals(
                    "fecsmith: <stdin>: input is not valid UTF-8\n".getBytes(StandardCharsets.UTF_8),
                    stderr.get(30, TimeUnit.SECONDS));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static List<CodeBlock> fencedBlocks(String markdown) {
        List<CodeBlock> blocks = new ArrayList<>();
        Matcher matcher = FENCED_BLOCK.matcher(markdown);
        while (matcher.find()) {
            blocks.add(new CodeBlock(matcher.group(1), matcher.group(2)));
        }
        return List.copyOf(blocks);
    }

    private static CompletableFuture<byte[]> readBytesAsync(InputStream stream) {
        return CompletableFuture.supplyAsync(() -> {
            try (stream) {
                return stream.readAllBytes();
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        });
    }

    private record CodeBlock(String language, String body) {
    }

    private record Example(List<String> arguments, String output) {
    }

    private static final class FakeIo implements Io {
        private final StringBuilder stdout = new StringBuilder();
        private final StringBuilder stderr = new StringBuilder();

        @Override
        public byte[] readStdin() {
            throw new AssertionError("README examples must not read standard input");
        }

        @Override
        public void out(String text) {
            stdout.append(text);
        }

        @Override
        public void err(String text) {
            stderr.append(text);
        }
    }
}
