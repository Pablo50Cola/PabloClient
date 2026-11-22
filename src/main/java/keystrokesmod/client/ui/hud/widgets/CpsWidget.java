package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.ui.gfx.Colors;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.gfx.UiText;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.ClickStats;
import keystrokesmod.client.ui.hud.Widget;

/**
 * Clicks per second (left | right), counted from the clicks the game really performs (see
 * {@link ClickStats}), so the Left Clicker and Right Clicker show up. The numbers ease toward the
 * real value, the pill flashes on every click and a thin bar shows the speed (full at 20 CPS).
 * The pill has a fixed width so it does not jitter while the numbers change.
 */
public final class CpsWidget extends Widget {
    private static final float PAD_X = 8f;
    private static final float PAD_Y = 5f;
    private static final float BAR_H = 2f;

    private final StringBuilder builder = new StringBuilder(16);
    private String text = "0 | 0 CPS";
    private float fixedWidth = -1f;
    private float dispLeft;
    private float dispRight;
    private float pulse;
    private long seenLeft;
    private long seenRight;
    private int keyLeft = -1;
    private int keyRight = -1;
    private long lastNs = System.nanoTime();

    public CpsWidget() {
        super("cps", "CPS", Anchor.TOP_RIGHT, 6f, 58f, false);
    }

    @Override
    public void measure() {
        if (fixedWidth < 0f) fixedWidth = UiText.width("99+ | 99+ CPS") + PAD_X * 2f;

        long ns = System.nanoTime();
        float dt = (ns - lastNs) / 1.0E9f;
        lastNs = ns;
        if (dt > 0.1f) dt = 0.1f;
        float ease = Math.min(1f, dt * 14f);

        long ms = System.currentTimeMillis();
        dispLeft += (ClickStats.leftCps(ms) - dispLeft) * ease;
        dispRight += (ClickStats.rightCps(ms) - dispRight) * ease;

        int l = Math.round(dispLeft);
        int r = Math.round(dispRight);
        if (l != keyLeft || r != keyRight) {
            keyLeft = l;
            keyRight = r;
            builder.setLength(0);
            builder.append(ClickStats.number(l)).append(" | ").append(ClickStats.number(r)).append(" CPS");
            text = builder.toString();
        }

        long tl = ClickStats.leftTotal();
        long tr = ClickStats.rightTotal();
        if (tl != seenLeft || tr != seenRight) {
            seenLeft = tl;
            seenRight = tr;
            pulse = 1f;
        }
        pulse = Math.max(0f, pulse - dt * 5f);

        width = fixedWidth;
        height = UiText.height() + PAD_Y * 2f + BAR_H + 3f;
    }

    @Override
    public void render(float partialTicks) {
        Gfx.roundRect(0f, 0f, width, height, 8f, fade(Colors.argb(200, 28, 28, 30)));
        if (pulse > 0.02f) {
            Gfx.roundRect(0f, 0f, width, height, 8f, fade(Colors.withAlpha(Colors.ACCENT, (int) (pulse * 90f))));
        }
        UiText.drawCentered(text, width / 2f, PAD_Y, fade(Colors.LABEL));

        float trackW = width - PAD_X * 2f;
        float by = height - PAD_Y + 1f;
        Gfx.roundRect(PAD_X, by, trackW, BAR_H, 1f, fade(Colors.argb(50, 255, 255, 255)));
        float fill = trackW * Colors.clamp01(Math.max(dispLeft, dispRight) / 20f);
        if (fill >= 2f) {
            Gfx.roundRect(PAD_X, by, fill, BAR_H, 1f, fade(Colors.lerp(Colors.ACCENT, Colors.GREEN, Colors.clamp01(fill / trackW))));
        }
    }
}
