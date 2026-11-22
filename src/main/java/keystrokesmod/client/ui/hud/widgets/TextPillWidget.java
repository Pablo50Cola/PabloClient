package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.ui.gfx.UiText;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.Widget;

/**
 * Base of the small one-line widgets (FPS, CPS, ping, coordinates, clock): a rounded pill with one
 * text. The value is read at most every {@code refreshMs} milliseconds, and the text is only
 * rebuilt when the value actually changed, so nothing is allocated in a normal frame.
 */
abstract class TextPillWidget extends Widget {
    private static final float PAD_X = 8f;
    private static final float PAD_Y = 5f;

    private final long refreshMs;
    private final StringBuilder builder = new StringBuilder(40);
    private String text = "";
    private float textWidth = -1f;
    private long lastRefresh;
    private long lastKey;

    /** Off by default: the owner switches widgets on in the HUD editor. */
    protected TextPillWidget(String id, String title, Anchor anchor, float offsetX, float offsetY, long refreshMs) {
        super(id, title, anchor, offsetX, offsetY, false);
        this.refreshMs = refreshMs;
    }

    /** Reads the current value. Must be cheap and must not allocate. */
    protected abstract long sample();

    /** Writes the text for the value into {@code out}. Only called when the value changed. */
    protected abstract void build(StringBuilder out, long key);

    @Override
    public boolean hasBackgroundOption() {
        return true;
    }

    @Override
    public boolean hasColorOption() {
        return true;
    }

    @Override
    public final void measure() {
        long now = System.currentTimeMillis();
        if (textWidth < 0f || now - lastRefresh >= refreshMs) {
            lastRefresh = now;
            long key = sample();
            if (textWidth < 0f || key != lastKey) {
                lastKey = key;
                builder.setLength(0);
                build(builder, key);
                text = builder.toString();
                textWidth = UiText.width(text);
            }
        }
        width = textWidth + PAD_X * 2f;
        height = UiText.height() + PAD_Y * 2f;
    }

    @Override
    public final void render(float partialTicks) {
        drawBackground(width, height, 8f);
        UiText.draw(text, PAD_X, PAD_Y, textColor());
    }
}
