package io.github.gulliby.fecsmith.core;

import java.io.IOException;

/** The byte input and text output boundary for commands. */
public interface Io {
    byte[] readStdin() throws IOException;

    void out(String text);

    void err(String text);
}
