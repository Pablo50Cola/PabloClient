package keystrokesmod.client.ui.demo;

import java.io.IOException;
import java.util.Locale;

import keystrokesmod.client.ui.UiSettings;
import keystrokesmod.client.ui.anim.Spring;
import keystrokesmod.client.ui.components.UiSegmented;
import keystrokesmod.client.ui.components.UiSwitch;
import keystrokesmod.client.ui.gfx.BlurBuffer;
import keystrokesmod.client.ui.gfx.Colors;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.gfx.UiText;
import keystrokesmod.client.ui.perf.FrameCost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

/**
 * Phase 1 demo: shows every building block of the toolkit and lets you change the quality
 * settings live while watching the client's own frame cost. Open it with the command "uidemo".
 */
public class UiDemoScreen extends GuiScreen {
    private static final float PANEL_W = 300f;
    private static final float PANEL_H = 206f;
    private static final float PAD = 16f;
    private static final float ROW = 26f;
    private static final float SEG_W = 34f;

    private final UiSegmented quality = new UiSegmented(new String[] { "Off", "Low", "High" },
            UiSettings.quality.ordinal());
    private final UiSwitch animations = new UiSwitch(UiSettings.animations);
    private final UiSwitch shadows = new UiSwitch(UiSettings.shadows);
    private final Spring toast = new Spring(-40f);

    // cached strings: rebuilt only when the number changes (no per-frame allocation)
    private int costKey = -1;
    private String costText = "";
    private int fpsKey = -1;
    private String fpsText = "";
    private int callsKey = -1;
    private String callsText = "";

    private float panelX;
    private float panelY;

    @Override
    public void initGui() {
        toast.setTarget(10f);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private float rowTop(int row) {
        return panelY + 38f + row * ROW;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        FrameCost.begin();

        float w = this.width;
        float h = this.height;
        float t = (System.currentTimeMillis() % 600000L) / 1000f;

        // --- something colourful behind the glass, so the blur has something to show
        Gfx.circle(w * 0.28f + (float) Math.sin(t * 0.8) * 40f, h * 0.35f + (float) Math.cos(t * 0.6) * 30f, 70f, Colors.ACCENT);
        Gfx.circle(w * 0.72f + (float) Math.cos(t * 0.7) * 40f, h * 0.55f + (float) Math.sin(t * 0.9) * 30f, 80f, 0xFFBF5AF2);
        Gfx.circle(w * 0.50f + (float) Math.sin(t * 0.5) * 60f, h * 0.80f, 60f, Colors.GREEN);
        for (int i = 1; i < 10; i++) {
            Gfx.roundRect(i * w / 10f, 0f, 2f, h, 0f, 0x55FFFFFF);
        }

        // --- shapes card (top left)
        Gfx.shadow(12f, 12f, 150f, 70f, 14f, 14f, 6f, 0x55000000);
        Gfx.roundRect(12f, 12f, 150f, 70f, 14f, Colors.SURFACE);
        UiText.draw("Shapes", 22f, 19f, Colors.LABEL_SECONDARY);
        Gfx.roundRect(22f, 34f, 26f, 26f, 4f, Colors.ACCENT);
        Gfx.roundRect(54f, 34f, 26f, 26f, 10f, Colors.GREEN);
        Gfx.roundRect(86f, 34f, 26f, 26f, 13f, Colors.ORANGE);
        Gfx.outlined(118f, 34f, 34f, 26f, 8f, 1.5f, Colors.ELEVATED, Colors.RED);
        Gfx.gradientH(22f, 66f, 130f, 8f, 4f, Colors.ACCENT, 0xFFBF5AF2);

        // --- toast pill that springs in from the top (Dynamic-Island style)
        float pillW = 150f;
        Gfx.shadow((w - pillW) / 2f, toast.get(), pillW, 24f, 12f, 12f, 4f, 0x66000000);
        Gfx.glass((w - pillW) / 2f, toast.get(), pillW, 24f, 12f);
        Gfx.circle((w - pillW) / 2f + 14f, toast.get() + 12f, 6f, Colors.GREEN);
        UiText.draw("Sprint enabled", (w - pillW) / 2f + 26f, toast.get() + 8f, Colors.LABEL);

        // --- main glass panel
        panelX = (w - PANEL_W) / 2f;
        panelY = (h - PANEL_H) / 2f + 10f;
        Gfx.shadow(panelX, panelY, PANEL_W, PANEL_H, 18f, 26f, 12f, 0x77000000);
        Gfx.glass(panelX, panelY, PANEL_W, PANEL_H, 18f);

        UiText.draw("Performance", panelX + PAD, panelY + 14f, Colors.LABEL);

        float textOff = (ROW - UiText.height()) / 2f;
        UiText.draw("Blur quality", panelX + PAD, rowTop(0) + textOff, Colors.LABEL);
        quality.draw(panelX + PANEL_W - PAD - SEG_W * 3f, rowTop(0) + (ROW - UiSegmented.HEIGHT) / 2f, SEG_W);
        divider(rowTop(1));

        UiText.draw("Animations", panelX + PAD, rowTop(1) + textOff, Colors.LABEL);
        animations.draw(panelX + PANEL_W - PAD - UiSwitch.WIDTH, rowTop(1) + (ROW - UiSwitch.HEIGHT) / 2f);
        divider(rowTop(2));

        UiText.draw("Shadows", panelX + PAD, rowTop(2) + textOff, Colors.LABEL);
        shadows.draw(panelX + PANEL_W - PAD - UiSwitch.WIDTH, rowTop(2) + (ROW - UiSwitch.HEIGHT) / 2f);
        divider(rowTop(3));

        float avg = FrameCost.avgMs();
        int key = (int) (avg * 100f);
        if (key != costKey) {
            costKey = key;
            costText = String.format(Locale.ROOT, "%.2f ms", avg);
        }
        UiText.draw("Client frame time", panelX + PAD, rowTop(3) + textOff, Colors.LABEL);
        UiText.drawRight(costText, panelX + PANEL_W - PAD, rowTop(3) + textOff, avg < 1f ? Colors.GREEN : Colors.ORANGE);
        divider(rowTop(4));

        int fps = Minecraft.getDebugFPS();
        if (fps != fpsKey) {
            fpsKey = fps;
            fpsText = Integer.toString(fps);
        }
        UiText.draw("FPS", panelX + PAD, rowTop(4) + textOff, Colors.LABEL);
        UiText.drawRight(fpsText, panelX + PANEL_W - PAD, rowTop(4) + textOff, Colors.LABEL_SECONDARY);
        divider(rowTop(5));

        int calls = Gfx.drawCallsLastFrame();
        if (calls != callsKey) {
            callsKey = calls;
            callsText = Integer.toString(calls);
        }
        UiText.draw("Draw calls", panelX + PAD, rowTop(5) + textOff, Colors.LABEL);
        UiText.drawRight(callsText, panelX + PANEL_W - PAD, rowTop(5) + textOff, Colors.LABEL_SECONDARY);

        UiText.drawCentered("Esc to close", w / 2f, panelY + PANEL_H + 14f, Colors.LABEL_SECONDARY);

        super.drawScreen(mouseX, mouseY, partialTicks);
        FrameCost.end();
    }

    private void divider(float y) {
        Gfx.roundRect(panelX + PAD, y, PANEL_W - PAD * 2f, 0.5f, 0f, 0x33FFFFFF);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            float segX = panelX + PANEL_W - PAD - SEG_W * 3f;
            float segY = rowTop(0) + (ROW - UiSegmented.HEIGHT) / 2f;
            int index = quality.hit(segX, segY, SEG_W, mouseX, mouseY);
            if (index >= 0) {
                quality.select(index);
                UiSettings.quality = UiSettings.Quality.values()[index];
                if (index == 0) BlurBuffer.release();
                return;
            }

            float swX = panelX + PANEL_W - PAD - UiSwitch.WIDTH;
            if (animations.hit(swX, rowTop(1) + (ROW - UiSwitch.HEIGHT) / 2f, mouseX, mouseY)) {
                animations.toggle();
                UiSettings.animations = animations.isOn();
                return;
            }
            if (shadows.hit(swX, rowTop(2) + (ROW - UiSwitch.HEIGHT) / 2f, mouseX, mouseY)) {
                shadows.toggle();
                UiSettings.shadows = shadows.isOn();
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
