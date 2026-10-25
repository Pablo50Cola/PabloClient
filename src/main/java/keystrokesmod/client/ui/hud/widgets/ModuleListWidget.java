package keystrokesmod.client.ui.hud.widgets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.HUD;
import keystrokesmod.client.module.modules.client.FakeHud;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.Widget;
import keystrokesmod.client.ui.hud.WidgetManager;
import keystrokesmod.client.utils.RenderUtils;
import keystrokesmod.client.utils.Utils;
import keystrokesmod.client.utils.font.FontUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * The list of enabled modules, moved out of the old {@code HUD} module unchanged in look and
 * behaviour (colour modes, drop shadow, background, Fake Hud). It uses the settings of the old HUD
 * module and only shows while that module is switched on.
 *
 * What changed: where the list sits is now decided by the widget's anchor instead of the old
 * quadrant guess. Right-hand anchors right-align the text, bottom anchors sort shortest-first.
 * The list of names is rebuilt ten times a second instead of every frame.
 */
public final class ModuleListWidget extends Widget {
    private static final HUD.ColourModes[] MODES = HUD.ColourModes.values();
    private static final int MARGIN = 2;
    private static final long REFRESH_MS = 100L;
    private static final String[] SAMPLE = { "KillAura", "Velocity", "Sprint", "AutoClicker" };

    private static final Comparator<Module> LONGEST_FIRST = new Comparator<Module>() {
        @Override
        public int compare(Module a, Module b) {
            FontRenderer fr = Utils.mc.fontRendererObj;
            return fr.getStringWidth(b.getName()) - fr.getStringWidth(a.getName());
        }
    };
    private static final Comparator<Module> SHORTEST_FIRST = new Comparator<Module>() {
        @Override
        public int compare(Module a, Module b) {
            FontRenderer fr = Utils.mc.fontRendererObj;
            return fr.getStringWidth(a.getName()) - fr.getStringWidth(b.getName());
        }
    };

    private final ArrayList<Module> scratch = new ArrayList<>();
    private String[] names = new String[32];
    private int[] widths = new int[32];
    private int count;
    private int maxWidth;

    private long lastRefresh;
    private boolean lastBottom;
    private boolean lastEditing;
    private boolean refreshed;

    private int rowAdvance;
    private int backgroundHeight;

    public ModuleListWidget() {
        super("modulelist", "Module list", Anchor.TOP_LEFT, 5f, 70f, true);
    }

    @Override
    public String hint() {
        return "Shown while the HUD module is on.";
    }

    @Override
    public boolean isVisible() {
        return enabled && WidgetManager.legacyHudOn();
    }

    private static HUD.ColourModes mode() {
        int i = (int) HUD.colourMode.getInput() - 1;
        return MODES[i < 0 ? 0 : (i >= MODES.length ? MODES.length - 1 : i)];
    }

    private void refresh() {
        long now = System.currentTimeMillis();
        boolean bottom = anchor.isBottom();
        if (refreshed && now - lastRefresh < REFRESH_MS && bottom == lastBottom && editing == lastEditing) return;
        refreshed = true;
        lastRefresh = now;
        lastBottom = bottom;
        lastEditing = editing;
        // Fake Hud still sorts its own list by this value
        HUD.positionMode = anchor.isRight()
                ? (bottom ? Utils.HUD.PositionMode.DOWNRIGHT : Utils.HUD.PositionMode.UPRIGHT)
                : (bottom ? Utils.HUD.PositionMode.DOWNLEFT : Utils.HUD.PositionMode.UPLEFT);

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        Module fake = Raven.moduleManager.getModuleByClazz(FakeHud.class);
        List<Module> source = fake != null && fake.isEnabled() ? FakeHud.getModules() : Raven.moduleManager.getModules();

        scratch.clear();
        for (int i = 0, n = source.size(); i < n; i++) {
            Module m = source.get(i);
            if (m.isEnabled() && m.showInHud()) scratch.add(m);
        }
        scratch.sort(bottom ? SHORTEST_FIRST : LONGEST_FIRST);

        int n = scratch.size();
        boolean sample = n == 0 && editing;
        int total = sample ? SAMPLE.length : n;
        if (total > names.length) {
            names = new String[total + 16];
            widths = new int[total + 16];
        }
        maxWidth = 0;
        for (int i = 0; i < total; i++) {
            String name = sample ? SAMPLE[i] : scratch.get(i).getName();
            int w = fr.getStringWidth(name);
            names[i] = name;
            widths[i] = w;
            if (w > maxWidth) maxWidth = w;
        }
        count = total;
    }

    @Override
    public void measure() {
        refresh();
        if (count == 0) {
            width = 0f;
            height = 0f;
            return;
        }
        int fh = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        boolean right = anchor.isRight();
        boolean kvLeft = mode() == HUD.ColourModes.KV && !right;
        rowAdvance = kvLeft ? fh - 2 : fh + MARGIN;
        backgroundHeight = count * (fh + MARGIN);
        width = maxWidth;
        height = count * rowAdvance;
    }

    @Override
    public void render(float partialTicks) {
        if (count == 0) return;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        boolean right = anchor.isRight();
        boolean shadow = HUD.dropShadow.isToggled();
        HUD.ColourModes mode = mode();

        if (HUD.arrayListBackground.isToggled()) {
            int bgOpacity = (int) (HUD.arrayListOpacity.getInput() * 255);
            int bgColor = (bgOpacity << 24) | 0x101010;
            if (HUD.arrayListBorder.isToggled()) {
                RenderUtils.drawBorderedRoundedRect(-2, -2, maxWidth + 4, backgroundHeight + 2, 4, 1, 0x00000000,
                        fade(0xFFFF8C00));
            } else {
                RenderUtils.drawRoundedRect(-2, -2, maxWidth + 4, backgroundHeight + 2, 4, fade(bgColor));
            }
        }

        int y = 0;
        int del = 0;
        for (int i = 0; i < count; i++) {
            int color = 0;
            switch (mode) {
            case RAVEN:
            case RAVEN2:
                color = Utils.Client.rainbowDraw(2L, (long) del);
                break;
            case ASTOLFO:
                color = Utils.Client.astolfoColorsDraw(10, 14);
                break;
            case ASTOLFO2:
            case ASTOLFO3:
                color = Utils.Client.astolfoColorsDraw(10, del);
                break;
            case KV:
                color = Utils.Client.customDraw(del);
                break;
            case ORANGE:
                color = 0xFFFF8C00;
                break;
            default:
                break;
            }
            if (color == 0) continue;
            color = fade(color);

            String name = names[i];
            if (right) {
                float x = maxWidth - widths[i];
                if (mode == HUD.ColourModes.KV) {
                    FontUtil.two.drawString(name, (double) x, y, color, shadow, 10);
                } else {
                    fr.drawString(name, x, (float) y, color, shadow);
                }
            } else if (mode == HUD.ColourModes.KV) {
                FontUtil.two.drawString(name, 0f, (float) y, color);
            } else {
                fr.drawString(name, 0f, (float) y, color, shadow);
            }
            y += rowAdvance;
            del -= mode == HUD.ColourModes.RAVEN ? 120 : 10;
        }
    }
}
