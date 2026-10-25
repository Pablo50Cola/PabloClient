package keystrokesmod.client.ui.hud.widgets;

import java.util.List;

import org.lwjgl.input.Keyboard;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.HUD;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.Widget;
import keystrokesmod.client.ui.hud.WidgetManager;
import keystrokesmod.client.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Box listing every module that has a key bound, moved out of the old {@code HUD} module with the
 * same look. Shown while the old HUD module is on and its "Keybinds" switch is on. The bind list is
 * refreshed four times a second; each line's text is only rebuilt when its module or key changes.
 */
public final class KeybindsWidget extends Widget {
    private static final long REFRESH_MS = 250L;
    private static final String HEADER = "Keybinds";
    private static final String[] SAMPLE = { "KillAura [R]", "Sprint [V]", "Fly [F]" };

    private Module[] mods = new Module[16];
    private int[] codes = new int[16];
    private String[] texts = new String[16];
    private int count;
    private int boxWidth;

    private long lastRefresh;
    private boolean lastEditing;
    private boolean refreshed;

    public KeybindsWidget() {
        super("keybinds", "Keybinds", Anchor.TOP_LEFT, 5f, 50f, true);
    }

    @Override
    public String hint() {
        return "Shown while the HUD module is on.";
    }

    @Override
    public boolean isVisible() {
        return enabled && WidgetManager.legacyHudOn() && HUD.keybinds.isToggled();
    }

    private void refresh() {
        long now = System.currentTimeMillis();
        if (refreshed && now - lastRefresh < REFRESH_MS && editing == lastEditing) return;
        refreshed = true;
        lastRefresh = now;
        lastEditing = editing;

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        List<Module> all = Raven.moduleManager.getModules();
        int n = 0;
        for (int i = 0, size = all.size(); i < size; i++) {
            if (all.get(i).getKeycode() != 0) n++;
        }

        boolean sample = n == 0 && editing;
        int total = sample ? SAMPLE.length : n;
        if (total > mods.length) {
            mods = new Module[total + 8];
            codes = new int[total + 8];
            texts = new String[total + 8];
        }

        int idx = 0;
        int widest = 80;
        if (sample) {
            for (int i = 0; i < total; i++) {
                mods[i] = null;
                codes[i] = 0;
                texts[i] = SAMPLE[i];
                int w = fr.getStringWidth(texts[i]) + 6;
                if (w > widest) widest = w;
            }
            idx = total;
        } else {
            for (int i = 0, size = all.size(); i < size; i++) {
                Module m = all.get(i);
                int code = m.getKeycode();
                if (code == 0) continue;
                if (mods[idx] != m || codes[idx] != code || texts[idx] == null) {
                    mods[idx] = m;
                    codes[idx] = code;
                    texts[idx] = m.getName() + " [" + Keyboard.getKeyName(code) + "]";
                }
                int w = fr.getStringWidth(texts[idx]) + 6;
                if (w > widest) widest = w;
                idx++;
            }
        }
        count = idx;
        boxWidth = widest;
    }

    @Override
    public void measure() {
        refresh();
        if (count == 0) {
            width = 0f;
            height = 0f;
            return;
        }
        width = boxWidth;
        height = 15 + count * (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 2) + 4;
    }

    @Override
    public void render(float partialTicks) {
        if (count == 0) return;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        boolean orange = HUD.colourMode.getInput() == HUD.ColourModes.ORANGE.ordinal() + 1;

        RenderUtils.drawBorderedRoundedRect(0, 0, width, height, 4, 1, fade(0xFFFF8C00), fade(0x90101010));
        fr.drawStringWithShadow(HEADER, (width / 2f) - (fr.getStringWidth(HEADER) / 2f), 4, fade(-1));

        int yOffset = 18;
        for (int i = 0; i < count; i++) {
            Module m = mods[i];
            boolean on = m != null ? m.isEnabled() : (i % 2 == 0);
            int color = on ? (orange ? 0xFFFF8C00 : 0xFF00FF00) : 0xFFFFFFFF;
            fr.drawStringWithShadow(texts[i], 3, yOffset, fade(color));
            yOffset += fr.FONT_HEIGHT + 2;
        }
    }
}
