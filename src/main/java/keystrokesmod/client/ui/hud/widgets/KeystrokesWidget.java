package keystrokesmod.client.ui.hud.widgets;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import keystrokesmod.client.ui.gfx.Colors;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.gfx.UiText;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.ClickStats;
import keystrokesmod.client.ui.hud.Widget;
import net.minecraft.client.Minecraft;

/**
 * W A S D, both mouse buttons with their live CPS, and the space bar, in a wide layout.
 * Keys follow the game's key bindings. A pressed key fades to white quickly and back slowly and
 * "sinks" slightly; the mouse keys also flash on every click the game performs, so clicks from
 * the Left Clicker / Right Clicker show up even though no physical button is held.
 */
public final class KeystrokesWidget extends Widget {
    private static final float KW = 34f;
    private static final float KH = 26f;
    private static final float G = 4f;
    private static final float MOUSE_H = 30f;
    private static final float SPACE_H = 16f;
    private static final int W = 0, A = 1, S = 2, D = 3, LMB = 4, RMB = 5, SPACE = 6;

    private final float[] glow = new float[7];
    private float pulseLeft;
    private float pulseRight;
    private long seenLeft;
    private long seenRight;
    private long lastNs = System.nanoTime();
    private float dtSeconds;

    public KeystrokesWidget() {
        super("keystrokes", "Keystrokes", Anchor.BOTTOM_LEFT, 10f, 60f, false);
    }

    @Override
    public void measure() {
        width = KW * 3f + G * 2f;
        height = (KH + G) * 2f + MOUSE_H + G + SPACE_H;

        long ns = System.nanoTime();
        dtSeconds = (ns - lastNs) / 1.0E9f;
        lastNs = ns;
        if (dtSeconds > 0.1f) dtSeconds = 0.1f;

        long tl = ClickStats.leftTotal();
        long tr = ClickStats.rightTotal();
        if (tl != seenLeft) {
            seenLeft = tl;
            pulseLeft = 1f;
        }
        if (tr != seenRight) {
            seenRight = tr;
            pulseRight = 1f;
        }
        pulseLeft = Math.max(0f, pulseLeft - dtSeconds * 7f);
        pulseRight = Math.max(0f, pulseRight - dtSeconds * 7f);

        for (int i = 0; i < glow.length; i++) {
            float target = down(i) ? 1f : 0f;
            float rate = target > glow[i] ? 32f : 9f; // quick press, slow release
            glow[i] += (target - glow[i]) * Math.min(1f, dtSeconds * rate);
        }
    }

    private static boolean down(int key) {
        Minecraft mc = Minecraft.getMinecraft();
        switch (key) {
        case W: return Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        case A: return Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        case S: return Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        case D: return Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
        case LMB: return Mouse.isButtonDown(0);
        case RMB: return Mouse.isButtonDown(1);
        default: return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
        }
    }

    private void plate(float x, float y, float w, float h, float g) {
        float in = 1.5f * g; // the key sinks a little while pressed
        int color = Colors.lerp(Colors.argb(170, 28, 28, 30), Colors.argb(235, 255, 255, 255), g);
        Gfx.roundRect(x + in, y + in, w - in * 2f, h - in * 2f, 6f, fade(color));
    }

    private void key(int id, float x, float y, String label) {
        float g = glow[id];
        plate(x, y, KW, KH, g);
        UiText.drawCentered(label, x + KW / 2f, y + (KH - UiText.height()) / 2f,
                fade(Colors.lerp(Colors.LABEL, 0xFF000000, g)));
    }

    private void mouseKey(int id, float x, float y, float w, String label, int cps, float pulse) {
        float g = Math.max(glow[id], pulse);
        plate(x, y, w, MOUSE_H, g);
        int text = Colors.lerp(Colors.LABEL, 0xFF000000, g);
        int sub = Colors.lerp(Colors.LABEL_SECONDARY, 0xFF000000, g);
        UiText.drawCentered(label, x + w / 2f, y + 4f, fade(text));
        UiText.drawCentered(ClickStats.number(cps), x + w / 2f, y + MOUSE_H - UiText.height() - 4f, fade(sub));
    }

    @Override
    public void render(float partialTicks) {
        long ms = System.currentTimeMillis();
        float row1 = KH + G;
        float row2 = (KH + G) * 2f;
        float half = (width - G) / 2f;

        key(W, KW + G, 0f, "W");
        key(A, 0f, row1, "A");
        key(S, KW + G, row1, "S");
        key(D, (KW + G) * 2f, row1, "D");
        mouseKey(LMB, 0f, row2, half, "LMB", ClickStats.leftCps(ms), pulseLeft);
        mouseKey(RMB, half + G, row2, half, "RMB", ClickStats.rightCps(ms), pulseRight);

        float sy = row2 + MOUSE_H + G;
        plate(0f, sy, width, SPACE_H, glow[SPACE]);
        Gfx.roundRect(width / 2f - 10f, sy + SPACE_H / 2f - 1f, 20f, 2f, 1f,
                fade(Colors.lerp(Colors.LABEL_SECONDARY, 0xFF000000, glow[SPACE])));
    }
}
