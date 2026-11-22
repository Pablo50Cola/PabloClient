package keystrokesmod.client.ui.gfx;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.ui.UiSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;

/**
 * Produces the blurred copy of the screen that frosted-glass panels show.
 *
 * Performance design:
 *  - the blur is computed at most ONCE per frame, and only when the first glass panel is drawn
 *    (no glass on screen = zero cost);
 *  - it runs at 1/2 or 1/4 of the screen resolution, depending on the quality preset;
 *  - the two small framebuffers are reused every frame (no per-frame allocation).
 * If anything goes wrong it switches itself off and glass falls back to a flat translucent panel.
 */
public final class BlurBuffer {
    private static Framebuffer a;
    private static Framebuffer b;
    private static int bufW;
    private static int bufH;
    private static boolean capturedThisFrame;
    private static boolean lastCaptureOk;
    private static boolean broken;

    private BlurBuffer() {
    }

    /** Called once at the start of every frame. */
    static void newFrame() {
        capturedThisFrame = false;
    }

    /** @return the GL texture id of the blurred screen, or -1 if blur is off or unavailable. */
    static int texture() {
        if (broken) return -1;
        UiSettings.Quality q = UiSettings.quality;
        if (q == UiSettings.Quality.OFF) return -1;

        if (!capturedThisFrame) {
            capturedThisFrame = true;
            try {
                lastCaptureOk = capture(q);
            } catch (Throwable t) {
                broken = true;
                lastCaptureOk = false;
                System.err.println("[PabloClient/ui] blur disabled after error: " + t);
                try {
                    Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
                } catch (Throwable ignored) {
                    // nothing more we can do
                }
            }
        }
        return lastCaptureOk && a != null ? a.framebufferTexture : -1;
    }

    private static boolean capture(UiSettings.Quality q) {
        Minecraft mc = Minecraft.getMinecraft();
        Framebuffer main = mc.getFramebuffer();
        if (main == null || !OpenGlHelper.isFramebufferEnabled()) return false;
        ShaderProgram blur = Shaders.blur();
        if (blur == null) return false;

        int w = Math.max(16, main.framebufferWidth / q.divisor);
        int h = Math.max(16, main.framebufferHeight / q.divisor);
        ensureBuffers(w, h);

        // remember GL state we are about to change
        boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        if (scissor) GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.disableDepth();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        // 1) shrink the screen into buffer A with bilinear filtering
        a.bindFramebuffer(true);
        GlStateManager.bindTexture(main.framebufferTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        fullscreenQuad();
        // vanilla keeps the main buffer on NEAREST filtering; put it back
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // 2) gaussian passes: A -> B horizontal, B -> A vertical
        float spread = UiSettings.blurStrength;
        blur.use();
        blur.set1i("tex", 0);
        for (int i = 0; i < q.passes; i++) {
            b.bindFramebuffer(true);
            GlStateManager.bindTexture(a.framebufferTexture);
            blur.set2f("dir", spread / w, 0f);
            fullscreenQuad();

            a.bindFramebuffer(true);
            GlStateManager.bindTexture(b.framebufferTexture);
            blur.set2f("dir", 0f, spread / h);
            fullscreenQuad();
        }
        ShaderProgram.stop();

        // restore everything
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        main.bindFramebuffer(true);

        if (scissor) GL11.glEnable(GL11.GL_SCISSOR_TEST);
        if (depth) GlStateManager.enableDepth();
        if (cull) GlStateManager.enableCull();
        if (alpha) GlStateManager.enableAlpha();
        if (blend) GlStateManager.enableBlend();
        return true;
    }

    private static void ensureBuffers(int w, int h) {
        if (a == null) {
            a = new Framebuffer(w, h, false);
            b = new Framebuffer(w, h, false);
            a.setFramebufferFilter(GL11.GL_LINEAR);
            b.setFramebufferFilter(GL11.GL_LINEAR);
            bufW = w;
            bufH = h;
        } else if (bufW != w || bufH != h) {
            a.createBindFramebuffer(w, h);
            b.createBindFramebuffer(w, h);
            a.setFramebufferFilter(GL11.GL_LINEAR);
            b.setFramebufferFilter(GL11.GL_LINEAR);
            bufW = w;
            bufH = h;
        }
    }

    private static void fullscreenQuad() {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0f, 0f);
        GL11.glVertex2f(-1f, -1f);
        GL11.glTexCoord2f(1f, 0f);
        GL11.glVertex2f(1f, -1f);
        GL11.glTexCoord2f(1f, 1f);
        GL11.glVertex2f(1f, 1f);
        GL11.glTexCoord2f(0f, 1f);
        GL11.glVertex2f(-1f, 1f);
        GL11.glEnd();
    }

    /** Frees the GPU buffers (e.g. when the quality is switched off for a long time). */
    public static void release() {
        if (a != null) {
            a.deleteFramebuffer();
            b.deleteFramebuffer();
            a = null;
            b = null;
        }
    }
}
