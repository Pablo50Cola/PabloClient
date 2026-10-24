package keystrokesmod.client.ui.gfx;

import keystrokesmod.client.utils.font.FontUtil;
import net.minecraft.client.Minecraft;

/**
 * Text drawing for the UI toolkit. Uses the client's smooth TTF font (Gilroy) once it has loaded
 * and falls back to Minecraft's own font otherwise. Swapping in Inter later only touches this class.
 */
public final class UiText {
    private UiText() {
    }

    private static boolean custom() {
        return FontUtil.normal != null;
    }

    public static void draw(String text, float x, float y, int color) {
        if (custom()) {
            FontUtil.normal.drawSmoothString(text, x, y, color);
        } else {
            Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color, false);
        }
    }

    public static void drawCentered(String text, float centerX, float y, int color) {
        draw(text, centerX - width(text) / 2f, y, color);
    }

    public static void drawRight(String text, float rightX, float y, int color) {
        draw(text, rightX - width(text), y, color);
    }

    public static float width(String text) {
        if (custom()) return (float) FontUtil.normal.getStringWidth(text);
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
    }

    /** Approximate visible height of a line, for vertical centering. */
    public static float height() {
        if (custom()) return FontUtil.normal.getHeight();
        return 8f;
    }
}
