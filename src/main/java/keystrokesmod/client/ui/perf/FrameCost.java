package keystrokesmod.client.ui.perf;

/**
 * Measures how long the client's own drawing code takes per frame (CPU side: time spent
 * issuing the draw calls). It does not measure GPU time, but for this toolkit the CPU side is
 * where regressions show up first. Not nest-safe: wrap one whole UI pass in begin()/end().
 */
public final class FrameCost {
    private static long startNs;
    private static float avgMs;
    private static float lastMs;

    private FrameCost() {
    }

    public static void begin() {
        startNs = System.nanoTime();
    }

    public static void end() {
        lastMs = (System.nanoTime() - startNs) / 1_000_000f;
        avgMs = avgMs == 0f ? lastMs : avgMs * 0.92f + lastMs * 0.08f;
    }

    /** Smoothed milliseconds per frame. */
    public static float avgMs() {
        return avgMs;
    }

    public static float lastMs() {
        return lastMs;
    }
}
