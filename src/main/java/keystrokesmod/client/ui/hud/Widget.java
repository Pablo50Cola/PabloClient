package keystrokesmod.client.ui.hud;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import keystrokesmod.client.ui.gfx.Colors;

/**
 * Base class of every HUD element (Phase 2).
 *
 * A widget has a position described by an {@link Anchor} plus an offset from it, a scale, an
 * opacity and an enabled flag. The {@link WidgetManager} calls {@link #measure()} and then
 * {@link #render(float)} once per frame for every visible widget.
 *
 * Rules for subclasses (see ROADMAP.md, performance rules):
 * <ul>
 * <li>{@code measure()} only sets {@link #width} and {@link #height} (GUI units, unscaled).</li>
 * <li>{@code render()} draws in local coordinates: (0, 0) is the top-left corner of the widget.
 * The manager has already moved and scaled the drawing, so the widget does not need to care
 * about position or scale.</li>
 * <li>No {@code new}, string concatenation or formatting inside {@code measure()} / {@code render()}.
 * Cache text and rebuild it only when the value changes.</li>
 * </ul>
 */
public abstract class Widget {
    public static final float MIN_SCALE = 0.5f;
    public static final float MAX_SCALE = 3.0f;
    public static final float MIN_OPACITY = 0.1f;

    /** Stable key used in the saved file. Never change it once released. */
    public final String id;
    /** Name shown in the editor. */
    public final String title;

    private final Anchor defaultAnchor;
    private final float defaultOffsetX;
    private final float defaultOffsetY;
    private final boolean defaultEnabled;

    protected boolean enabled;
    protected Anchor anchor;
    protected float offsetX;
    protected float offsetY;
    protected float scale = 1f;
    protected float opacity = 1f;

    /** Content size in GUI units before scaling. Set by {@link #measure()}. */
    protected float width;
    protected float height;

    /** True while the editor screen is open: widgets should show sample content instead of nothing. */
    protected boolean editing;

    private float screenX;
    private float screenY;

    protected Widget(String id, String title, Anchor defaultAnchor, float defaultOffsetX, float defaultOffsetY,
            boolean defaultEnabled) {
        this.id = id;
        this.title = title;
        this.defaultAnchor = defaultAnchor;
        this.defaultOffsetX = defaultOffsetX;
        this.defaultOffsetY = defaultOffsetY;
        this.defaultEnabled = defaultEnabled;
        resetToDefaults();
    }

    // ------------------------------------------------------------------ to implement

    /** Updates {@link #width} and {@link #height}. Called every frame before {@link #render(float)}. */
    public abstract void measure();

    /** Draws the widget at local (0, 0). */
    public abstract void render(float partialTicks);

    // ------------------------------------------------------------------ to override (optional)

    /**
     * Whether the widget is drawn in the game right now. The default is the enabled flag; widgets
     * that depend on something else (for example the old HUD module being switched on) add to it.
     */
    public boolean isVisible() {
        return enabled;
    }

    /**
     * When true (default) the manager translates and scales the drawing, so {@link #render(float)}
     * draws at (0, 0). Return false for widgets that draw {@code Gfx.glass(...)}: the glass shader
     * needs real screen coordinates, so such a widget must use {@link #screenX()}, {@link #screenY()}
     * and {@link #scale()} itself.
     */
    public boolean usesMatrixTransform() {
        return true;
    }

    /** One short line shown under the title in the editor (for example when the widget depends on a module). May be null. */
    public String hint() {
        return null;
    }

    /** Extra values to save (for widgets with their own settings). */
    protected void saveExtra(JsonObject out) {
    }

    /** Reads the extra values back. Missing fields must keep their defaults. */
    protected void loadExtra(JsonObject in) {
    }

    /** Resets the extra values to their defaults. */
    protected void resetExtra() {
    }

    // ------------------------------------------------------------------ state

    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public final Anchor anchor() {
        return anchor;
    }

    public final float scale() {
        return scale;
    }

    public final void setScale(float scale) {
        this.scale = clamp(scale, MIN_SCALE, MAX_SCALE);
    }

    public final float opacity() {
        return opacity;
    }

    public final void setOpacity(float opacity) {
        this.opacity = clamp(opacity, MIN_OPACITY, 1f);
    }

    public final void setEditing(boolean editing) {
        this.editing = editing;
    }

    /** Top-left corner on the screen after the last {@link #layout(float, float)}. */
    public final float screenX() {
        return screenX;
    }

    public final float screenY() {
        return screenY;
    }

    /** Size on the screen (content size times scale). */
    public final float scaledWidth() {
        return width * scale;
    }

    public final float scaledHeight() {
        return height * scale;
    }

    /** Applies the widget's opacity to a colour. */
    protected final int fade(int argb) {
        return opacity >= 1f ? argb : Colors.scaleAlpha(argb, opacity);
    }

    // ------------------------------------------------------------------ layout

    /**
     * Works out where the widget is on the screen from its anchor and offset. Allocation free.
     * The result is kept inside the screen, so a widget can never end up out of reach after the
     * window got smaller.
     */
    public final void layout(float screenW, float screenH) {
        float w = width * scale;
        float h = height * scale;
        float dirX = anchor.fx == 1f ? -1f : 1f;
        float dirY = anchor.fy == 1f ? -1f : 1f;
        float x = anchor.fx * (screenW - w) + dirX * offsetX;
        float y = anchor.fy * (screenH - h) + dirY * offsetY;
        float maxX = screenW - w;
        float maxY = screenH - h;
        screenX = x > maxX ? (maxX < 0f ? 0f : maxX) : (x < 0f ? 0f : x);
        screenY = y > maxY ? (maxY < 0f ? 0f : maxY) : (y < 0f ? 0f : y);
    }

    /**
     * Moves the widget so its top-left corner is at (x, y) on the screen. The anchor is chosen from
     * the widget's centre, and the offset is stored relative to it, so the widget keeps its place
     * relative to that corner or edge when the window is resized.
     */
    public final void moveTo(float x, float y, float screenW, float screenH) {
        float w = width * scale;
        float h = height * scale;
        anchor = Anchor.fromPoint(x + w / 2f, y + h / 2f, screenW, screenH);
        float dirX = anchor.fx == 1f ? -1f : 1f;
        float dirY = anchor.fy == 1f ? -1f : 1f;
        offsetX = dirX * (x - anchor.fx * (screenW - w));
        offsetY = dirY * (y - anchor.fy * (screenH - h));
        layout(screenW, screenH);
    }

    /**
     * True for the two elements that still draw themselves (Target HUD, Radar) and keep their
     * position in the old modules. They only appear in the editor so they can still be moved;
     * Phase 3 turns them into real widgets.
     */
    public boolean isLegacyPositioned() {
        return false;
    }

    /** Sets the on-screen position directly (used for legacy-positioned elements). */
    public final void placeAt(float x, float y) {
        screenX = x;
        screenY = y;
    }

    public final boolean contains(float mouseX, float mouseY) {
        return mouseX >= screenX && mouseX <= screenX + width * scale
                && mouseY >= screenY && mouseY <= screenY + height * scale;
    }

    // ------------------------------------------------------------------ defaults and saving

    public final void resetToDefaults() {
        enabled = defaultEnabled;
        anchor = defaultAnchor;
        offsetX = defaultOffsetX;
        offsetY = defaultOffsetY;
        scale = 1f;
        opacity = 1f;
        resetExtra();
    }

    public final JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("enabled", enabled);
        o.addProperty("anchor", anchor.name());
        o.addProperty("offsetX", offsetX);
        o.addProperty("offsetY", offsetY);
        o.addProperty("scale", scale);
        o.addProperty("opacity", opacity);
        saveExtra(o);
        return o;
    }

    /** Loads saved values. Anything missing, wrong or from an older version falls back to the default. */
    public final void fromJson(JsonObject in) {
        resetToDefaults();
        if (in == null) return;

        Boolean en = readBoolean(in, "enabled");
        if (en != null) enabled = en;

        JsonElement a = in.get("anchor");
        if (a != null && a.isJsonPrimitive()) {
            anchor = Anchor.parse(a.getAsString(), anchor);
        }

        Float ox = readFloat(in, "offsetX");
        if (ox != null) offsetX = ox;
        Float oy = readFloat(in, "offsetY");
        if (oy != null) offsetY = oy;

        Float sc = readFloat(in, "scale");
        if (sc != null) scale = clamp(sc, MIN_SCALE, MAX_SCALE);
        Float op = readFloat(in, "opacity");
        if (op != null) opacity = clamp(op, MIN_OPACITY, 1f);

        try {
            loadExtra(in);
        } catch (RuntimeException ignored) {
            // a damaged extra value must never stop the client from starting
        }
    }

    protected static Boolean readBoolean(JsonObject o, String key) {
        try {
            JsonElement e = o.get(key);
            if (e != null && e.isJsonPrimitive()) return e.getAsBoolean();
        } catch (RuntimeException ignored) {
        }
        return null;
    }

    protected static Float readFloat(JsonObject o, String key) {
        try {
            JsonElement e = o.get(key);
            if (e != null && e.isJsonPrimitive()) {
                float v = e.getAsFloat();
                if (!Float.isNaN(v) && !Float.isInfinite(v)) return v;
            }
        } catch (RuntimeException ignored) {
        }
        return null;
    }

    protected static float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }
}
