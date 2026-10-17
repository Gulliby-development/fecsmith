package io.github.gulliby.fecsmith.core;

/** A user-facing usage error. */
public final class Failure {
    private Failure() {
    }

    public static final class Usage extends RuntimeException {
        public Usage(String message) {
            super(message);
        }
    }
}
