package keystrokesmod.client.ui.gfx;

/** ARGB colour helpers (ints, 0xAARRGGBB). No allocation. The iOS dark palette lives here. */
public final class Colors {
    public static final int BACKGROUND = 0xFF000000;
    public static final int SURFACE = 0xFF1C1C1E;
    public static final int ELEVATED = 0xFF2C2C2E;
    public static final int GLASS = argb(140, 28, 28, 30);
    public static final int GLASS_BORDER = argb(36, 255, 255, 255);
    public static final int LABEL = 0xFFFFFFFF;
    public static final int LABEL_SECONDARY = argb(153, 235, 235, 245);
    public static final int ACCENT = 0xFF0A84FF;
    public static final int GREEN = 0xFF30D158;
    public static final int ORANGE = 0xFFFF9F0A;
    public static final int RED = 0xFFFF453A;
    public static final int SWITCH_OFF = argb(92, 120, 120, 128);

    private Colors() {
    }

    public static int argb(int a, int r, int g, int b) {
        return ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
    }

    /** Replaces the alpha channel. */
    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 255) << 24);
    }

    /** Multiplies the existing alpha by a 0..1 factor (for fade in / out). */
    public static int scaleAlpha(int color, float factor) {
        int a = (int) (((color >>> 24) & 255) * clamp01(factor));
        return (color & 0x00FFFFFF) | (a << 24);
    }

    /** Linear blend between two colours, t = 0..1. */
    public static int lerp(int from, int to, float t) {
        t = clamp01(t);
        int a = (int) (((from >>> 24) & 255) + (((to >>> 24) & 255) - ((from >>> 24) & 255)) * t);
        int r = (int) (((from >> 16) & 255) + (((to >> 16) & 255) - ((from >> 16) & 255)) * t);
        int g = (int) (((from >> 8) & 255) + (((to >> 8) & 255) - ((from >> 8) & 255)) * t);
        int bl = (int) ((from & 255) + ((to & 255) - (from & 255)) * t);
        return argb(a, r, g, bl);
    }

    public static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }
}
