package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.ui.gfx.Colors;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.gfx.UiText;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.Widget;
import keystrokesmod.client.ui.perf.FrameCost;
import net.minecraft.client.Minecraft;

/**
 * Small overlay with the client's own frame cost, the FPS and the number of toolkit draw calls.
 * Off by default; switch it on with the {@code perf overlay} command or in the HUD editor.
 * The text is rebuilt at most four times a second, and only when a number changed.
 */
public final class PerfWidget extends Widget {
    private static final long REFRESH_MS = 250L;
    private static final float PAD_X = 8f;
    private static final float PAD_Y = 5f;

    private final StringBuilder builder = new StringBuilder(48);
    private String text = "0.00 ms | 0 FPS | 0 draws";
    private float textWidth = -1f;
    private long lastRefresh;
    private int keyHundredths = -1;
    private int keyFps = -1;
    private int keyCalls = -1;

    public PerfWidget() {
        super("perf", "Performance", Anchor.TOP_RIGHT, 6f, 6f, false);
    }

    private void refresh() {
        long now = System.currentTimeMillis();
        if (textWidth >= 0f && now - lastRefresh < REFRESH_MS) return;
        lastRefresh = now;

        int hundredths = (int) (FrameCost.avgMs() * 100f + 0.5f);
        int fps = Minecraft.getDebugFPS();
        int calls = Gfx.drawCallsLastFrame();
        if (textWidth >= 0f && hundredths == keyHundredths && fps == keyFps && calls == keyCalls) return;
        keyHundredths = hundredths;
        keyFps = fps;
        keyCalls = calls;

        StringBuilder b = builder;
        b.setLength(0);
        b.append(hundredths / 100).append('.');
        int frac = hundredths % 100;
        if (frac < 10) b.append('0');
        b.append(frac).append(" ms | ").append(fps).append(" FPS | ").append(calls).append(" draws");
        text = b.toString();
        textWidth = UiText.width(text);
    }

    @Override
    public void measure() {
        refresh();
        width = textWidth + PAD_X * 2f;
        height = UiText.height() + PAD_Y * 2f;
    }

    @Override
    public void render(float partialTicks) {
        Gfx.roundRect(0f, 0f, width, height, 8f, fade(Colors.argb(200, 28, 28, 30)));
        UiText.draw(text, PAD_X, PAD_Y, fade(Colors.LABEL));
    }
}
