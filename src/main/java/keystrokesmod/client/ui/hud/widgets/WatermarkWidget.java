package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.module.modules.HUD;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.Widget;
import keystrokesmod.client.ui.hud.WidgetManager;
import keystrokesmod.client.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * "Pablo Client | name | FPS" box, moved out of the old {@code HUD} module with the same look.
 * Shown while the old HUD module is on and its "Watermark" switch is on. The text is rebuilt only
 * when the FPS number or the player name changes.
 */
public final class WatermarkWidget extends Widget {
    private int cachedFps = -1;
    private String cachedName;
    private String text = "";
    private int textWidth;

    public WatermarkWidget() {
        super("watermark", "Watermark", Anchor.TOP_LEFT, 5f, 5f, true);
    }

    @Override
    public String hint() {
        return "Shown while the HUD module is on.";
    }

    @Override
    public boolean isVisible() {
        return enabled && WidgetManager.legacyHudOn() && HUD.watermark.isToggled();
    }

    @Override
    public void measure() {
        Minecraft mc = Minecraft.getMinecraft();
        int fps = Minecraft.getDebugFPS();
        String name = mc.getSession().getUsername();
        if (fps != cachedFps || name != cachedName) {
            cachedFps = fps;
            cachedName = name;
            text = "Pablo Client | " + name + " | " + fps + " FPS";
            textWidth = mc.fontRendererObj.getStringWidth(text);
        }
        width = textWidth + 6;
        height = mc.fontRendererObj.FONT_HEIGHT + 6;
    }

    @Override
    public void render(float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int color = HUD.colourMode.getInput() == HUD.ColourModes.ORANGE.ordinal() + 1 ? 0xFFFF8C00 : -1;
        RenderUtils.drawBorderedRoundedRect(0, 0, width, height, 4, 1, fade(0xFFFF8C00), fade(0x90101010));
        fr.drawStringWithShadow(text, 3, 4, fade(color));
    }
}
