package keystrokesmod.client.ui;

/**
 * Global switches for the iOS-style UI toolkit. Performance presets live here so every
 * widget respects the same quality level (see ROADMAP.md, "Performance rules").
 */
public final class UiSettings {

    /**
     * Blur quality. {@code divisor} is how much smaller than the screen the blur is rendered
     * (bigger = cheaper), {@code passes} is the number of horizontal+vertical gaussian passes.
     */
    public enum Quality {
        OFF("Off", 1, 0),
        LOW("Low", 4, 1),
        HIGH("High", 2, 2);

        public final String label;
        public final int divisor;
        public final int passes;

        Quality(String label, int divisor, int passes) {
            this.label = label;
            this.divisor = divisor;
            this.passes = passes;
        }
    }

    public static volatile Quality quality = Quality.HIGH;
    /** When false every animation snaps to its target instantly (no per-frame cost). */
    public static volatile boolean animations = true;
    /** When false soft shadows are skipped. */
    public static volatile boolean shadows = true;
    /** Blur spread multiplier, 0.5 .. 2.0. */
    public static volatile float blurStrength = 1.0f;

    private UiSettings() {
    }
}
