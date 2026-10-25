package keystrokesmod.client.ui.hud;

/**
 * Where on the screen a widget is attached. The widget keeps its distance to this point, so it
 * stays at the same corner or edge when the window size or GUI scale changes.
 *
 * {@code fx} / {@code fy} are the horizontal / vertical position of the anchor as a fraction of
 * the screen (0 = left / top, 0.5 = middle, 1 = right / bottom).
 */
public enum Anchor {
    TOP_LEFT(0f, 0f),
    TOP_CENTER(0.5f, 0f),
    TOP_RIGHT(1f, 0f),
    MIDDLE_LEFT(0f, 0.5f),
    CENTER(0.5f, 0.5f),
    MIDDLE_RIGHT(1f, 0.5f),
    BOTTOM_LEFT(0f, 1f),
    BOTTOM_CENTER(0.5f, 1f),
    BOTTOM_RIGHT(1f, 1f);

    /** Cached so nobody calls values() (which allocates a new array) in per-frame code. */
    public static final Anchor[] ALL = values();

    public final float fx;
    public final float fy;

    Anchor(float fx, float fy) {
        this.fx = fx;
        this.fy = fy;
    }

    public boolean isRight() {
        return fx == 1f;
    }

    public boolean isBottom() {
        return fy == 1f;
    }

    /** Picks the anchor for a point on the screen by dividing the screen into a 3x3 grid. */
    public static Anchor fromPoint(float x, float y, float screenW, float screenH) {
        int col = x < screenW / 3f ? 0 : (x < screenW * 2f / 3f ? 1 : 2);
        int row = y < screenH / 3f ? 0 : (y < screenH * 2f / 3f ? 1 : 2);
        return ALL[row * 3 + col];
    }

    /** Parses a saved name; unknown or missing names give {@code fallback}. */
    public static Anchor parse(String name, Anchor fallback) {
        if (name == null) return fallback;
        for (int i = 0; i < ALL.length; i++) {
            if (ALL[i].name().equals(name)) return ALL[i];
        }
        return fallback;
    }
}
