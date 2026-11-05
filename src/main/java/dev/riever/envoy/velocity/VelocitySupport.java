package dev.riever.envoy.velocity;

public final class VelocitySupport {

    public static void verify() {
        try {
            Handles.init();
        } catch (ExceptionInInitializerError e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("Unsupported Velocity version: " + cause.getMessage() + "." +
                    " If you think this is a bug, feel free to report to the issue tracker.", cause);
        }
    }
}
