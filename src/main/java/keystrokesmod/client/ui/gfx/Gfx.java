package keystrokesmod.client.ui.gfx;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.ui.UiSettings;
import keystrokesmod.client.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Drawing API of the iOS-style UI toolkit. All coordinates are in Minecraft GUI units
 * (the same units vanilla GUI code uses). Colours are ARGB ints, see {@link Colors}.
 *
 * Every shape is a single quad run through a small shader, so edges are anti-aliased and
 * corner radii are exact at any GUI scale. If shaders are unavailable, every call falls back to
 * the old (slower, jaggier) {@link RenderUtils} code instead of crashing.
 */
public final class Gfx {
    private static ScaledResolution scaled;
    private static int cachedW = -1;
    private static int cachedH = -1;
    private static int cachedGui = -1;

    private static int drawCalls;
    private static int lastDrawCalls;
    private static boolean cullWas;

    private Gfx() {
    }

    // ------------------------------------------------------------------ frame / scale

    /** Called once per frame by {@link keystrokesmod.client.ui.UiEvents}. */
    public static void newFrame() {
        BlurBuffer.newFrame();
        lastDrawCalls = drawCalls;
        drawCalls = 0;
    }

    /** Number of toolkit draw calls issued in the previous frame (for the perf overlay). */
    public static int drawCallsLastFrame() {
        return lastDrawCalls;
    }

    /** Cached: a new ScaledResolution is only built when the window size or GUI scale changes. */
    public static ScaledResolution scaled() {
        Minecraft mc = Minecraft.getMinecraft();
        int gui = mc.gameSettings.guiScale;
        if (scaled == null || cachedW != mc.displayWidth || cachedH != mc.displayHeight || cachedGui != gui) {
            scaled = new ScaledResolution(mc);
            cachedW = mc.displayWidth;
            cachedH = mc.displayHeight;
            cachedGui = gui;
        }
        return scaled;
    }

    public static float scaleFactor() {
        return scaled().getScaleFactor();
    }

    // ------------------------------------------------------------------ shapes

    public static void roundRect(float x, float y, float w, float h, float radius, int color) {
        drawRound(x, y, w, h, radius, color, color, color, color, 0f, 0, 0f, 0f, false);
    }

    /** Vertical gradient: {@code top} colour fading into {@code bottom}. */
    public static void gradientV(float x, float y, float w, float h, float radius, int top, int bottom) {
        drawRound(x, y, w, h, radius, top, top, bottom, bottom, 0f, 0, 0f, 0f, false);
    }

    /** Horizontal gradient: {@code left} colour fading into {@code right}. */
    public static void gradientH(float x, float y, float w, float h, float radius, int left, int right) {
        drawRound(x, y, w, h, radius, left, right, left, right, 0f, 0, 0f, 0f, false);
    }

    /** One colour per corner. */
    public static void gradient4(float x, float y, float w, float h, float radius,
                                 int topLeft, int topRight, int bottomLeft, int bottomRight) {
        drawRound(x, y, w, h, radius, topLeft, topRight, bottomLeft, bottomRight, 0f, 0, 0f, 0f, false);
    }

    /** Filled rounded rectangle with a border drawn inside its edge. */
    public static void outlined(float x, float y, float w, float h, float radius,
                                float borderWidth, int fill, int border) {
        drawRound(x, y, w, h, radius, fill, fill, fill, fill, borderWidth, border, 0f, 0f, false);
    }

    public static void circle(float centerX, float centerY, float radius, int color) {
        roundRect(centerX - radius, centerY - radius, radius * 2f, radius * 2f, radius, color);
    }

    /**
     * Soft drop shadow around a rounded rectangle. Only the part outside the shape is drawn, so it
     * never darkens a translucent panel. Skipped entirely when shadows are switched off.
     */
    public static void shadow(float x, float y, float w, float h, float radius,
                              float blur, float offsetY, int color) {
        if (!UiSettings.shadows || blur <= 0f) return;
        drawRound(x, y + offsetY, w, h, radius, color, color, color, color, 0f, 0, blur, blur, true);
    }

    /**
     * Frosted glass panel: a blurred copy of what is behind it, tinted, with a hairline border.
     * With blur switched off (or unavailable) it becomes a flat, slightly more opaque panel.
     */
    public static void glass(float x, float y, float w, float h, float radius, int tint, int border, float opacity) {
        if (w <= 0f || h <= 0f || opacity <= 0f) return;

        int tex = BlurBuffer.texture();
        ShaderProgram p = tex >= 0 ? Shaders.glass() : null;
        if (p == null) {
            int a = Math.min(255, ((tint >>> 24) & 255) + 70);
            int flat = Colors.scaleAlpha(Colors.withAlpha(tint, a), opacity);
            float bw = (border >>> 24) != 0 ? 1f : 0f;
            drawRound(x, y, w, h, radius, flat, flat, flat, flat, bw, Colors.scaleAlpha(border, opacity), 0f, 0f, false);
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        float scale = scaleFactor();
        float maxR = Math.min(w, h) * 0.5f;

        begin(true);
        GlStateManager.bindTexture(tex);
        p.use();
        p.set1i("blurTex", 0);
        p.set2f("rectPos", x, y);
        p.set2f("rectSize", w, h);
        p.set2f("screen", mc.displayWidth / scale, mc.displayHeight / scale);
        p.set1f("radius", radius > maxR ? maxR : radius);
        p.set1f("pixel", 1f / scale);
        p.set1f("saturation", 1.6f);
        p.set1f("opacity", opacity);
        p.set1f("borderWidth", (border >>> 24) != 0 ? 1.5f / scale : 0f);
        p.setColor("tint", tint);
        p.setColor("borderColor", border);
        quad(x, y, w, h);
        end();
    }

    public static void glass(float x, float y, float w, float h, float radius) {
        glass(x, y, w, h, radius, Colors.GLASS, Colors.GLASS_BORDER, 1f);
    }

    // ------------------------------------------------------------------ clipping

    /** Clips drawing to a rectangle (GUI units). Not nestable: always pair with {@link #scissorEnd()}. */
    public static void scissorBegin(float x, float y, float w, float h) {
        Minecraft mc = Minecraft.getMinecraft();
        float s = scaleFactor();
        int px = Math.round(x * s);
        int py = mc.displayHeight - Math.round((y + h) * s);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(px, py, Math.max(0, Math.round(w * s)), Math.max(0, Math.round(h * s)));
    }

    public static void scissorEnd() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    // ------------------------------------------------------------------ internals

    private static void drawRound(float x, float y, float w, float h, float radius,
                                  int tl, int tr, int bl, int br,
                                  float borderWidth, int borderColor,
                                  float soft, float expand, boolean hole) {
        if (w <= 0f || h <= 0f) return;

        ShaderProgram p = Shaders.round();
        if (p == null) {
            // simple renderer fallback (no gradients / shadows)
            if (soft > 0f) return;
            if (borderWidth > 0f) {
                RenderUtils.drawBorderedRoundedRect(x, y, x + w, y + h, radius, borderWidth, borderColor, tl);
            } else {
                RenderUtils.drawRoundedRect(x, y, x + w, y + h, radius, tl);
            }
            return;
        }

        float maxR = Math.min(w, h) * 0.5f;
        float qw = w + expand * 2f;
        float qh = h + expand * 2f;

        begin(false);
        p.use();
        p.set2f("quadSize", qw, qh);
        p.set2f("rectSize", w, h);
        p.set1f("radius", radius > maxR ? maxR : radius);
        p.set1f("pixel", 1f / scaleFactor());
        p.set1f("soft", soft);
        p.set1f("hole", hole ? 1f : 0f);
        p.set1f("borderWidth", borderWidth);
        p.setColor("cTL", tl);
        p.setColor("cTR", tr);
        p.setColor("cBL", bl);
        p.setColor("cBR", br);
        p.setColor("borderColor", borderColor);
        quad(x - expand, y - expand, qw, qh);
        end();
    }

    private static void begin(boolean textured) {
        cullWas = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        if (cullWas) GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        if (textured) {
            GlStateManager.enableTexture2D();
        } else {
            GlStateManager.disableTexture2D();
        }
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private static void end() {
        ShaderProgram.stop();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        if (cullWas) GlStateManager.enableCull();
        drawCalls++;
    }

    /** Quad with texture coordinates (0,0) top-left to (1,1) bottom-right, which the shaders use as local space. */
    private static void quad(float x, float y, float w, float h) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0f, 1f);
        GL11.glVertex2f(x, y + h);
        GL11.glTexCoord2f(1f, 1f);
        GL11.glVertex2f(x + w, y + h);
        GL11.glTexCoord2f(1f, 0f);
        GL11.glVertex2f(x + w, y);
        GL11.glTexCoord2f(0f, 0f);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
    }
}
