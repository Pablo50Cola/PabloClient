package keystrokesmod.client.ui.hud.widgets;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import keystrokesmod.client.ui.gfx.Colors;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.gfx.UiText;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.Widget;
import net.minecraft.client.Minecraft;

/**
 * W A S D, both mouse buttons and the space bar. A key fades to white while it is held and fades
 * back when released. Uses the game's own key bindings, so rebinding is followed automatically.
 */
public final class KeystrokesWidget extends Widget {
    private static final float K = 24f;
    private static final float G = 3f;
    private static final float SPACE_H = 14f;
    private static final int W = 0, A = 1, S = 2, D = 3, LMB = 4, RMB = 5, SPACE = 6;

    private final float[] glow = new float[7];
    private long last = System.nanoTime();
    private float step;

    public KeystrokesWidget() {
        super("keystrokes", "Keystrokes", Anchor.BOTTOM_LEFT, 10f, 60f, false);
    }

    @Override
    public void measure() {
        width = K * 3f + G * 2f;
        height = K * 3f + G * 3f + SPACE_H;
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

    private void key(int id, float x, float y, float w, float h, String label) {
        float t = down(id) ? 1f : 0f;
        glow[id] += (t - glow[id]) * step;
        float g = glow[id];
        Gfx.roundRect(x, y, w, h, 6f, fade(Colors.lerp(Colors.argb(170, 28, 28, 30), Colors.argb(235, 255, 255, 255), g)));
        if (label != null) {
            UiText.drawCentered(label, x + w / 2f, y + (h - UiText.height()) / 2f,
                    fade(Colors.lerp(Colors.LABEL, 0xFF000000, g)));
        }
    }

    @Override
    public void render(float partialTicks) {
        long now = System.nanoTime();
        float dt = (now - last) / 1.0E9f;
        last = now;
        if (dt > 0.1f) dt = 0.1f;
        step = Math.min(1f, dt * 16f);

        float row1 = K + G;
        float row2 = (K + G) * 2f;
        float half = (width - G) / 2f;
        key(W, K + G, 0f, K, K, "W");
        key(A, 0f, row1, K, K, "A");
        key(S, K + G, row1, K, K, "S");
        key(D, (K + G) * 2f, row1, K, K, "D");
        key(LMB, 0f, row2, half, K, "LMB");
        key(RMB, half + G, row2, half, K, "RMB");
        key(SPACE, 0f, row2 + K + G, width, SPACE_H, null);
    }
}
