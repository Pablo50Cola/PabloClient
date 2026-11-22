package keystrokesmod.client.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import keystrokesmod.client.ui.anim.Spring;
import keystrokesmod.client.ui.gfx.Colors;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.gfx.UiText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

/**
 * iOS-style notifications (Phase 4).
 *
 * <ul>
 * <li><b>Banners</b>: rounded cards that slide in from the right edge on a spring and stack in the
 * top right corner. At most {@value #MAX_VISIBLE} at once, the rest wait in a queue.</li>
 * <li><b>Island</b>: a black pill at the top centre, shown when a module is switched on or off.
 * It grows out of a small dot on a springy width, holds for about two seconds and shrinks away.</li>
 * </ul>
 *
 * Nothing is drawn (and nothing allocated) while no notification is active. Both parts are only
 * used while the "Notifications" switch of the Gui module is on.
 */
public class NotificationManager {
    private static final int MAX_VISIBLE = 4;
    private static final float BANNER_H = 34f;
    private static final float GAP = 6f;
    private static final float EDGE = 8f;

    private static final LinkedBlockingQueue<Notification> PENDING = new LinkedBlockingQueue<>();
    private static final List<Notification> ACTIVE = new ArrayList<>();

    // island
    private static final float ISLAND_H = 26f;
    private static final float ISLAND_MIN = 28f;
    private static final float ISLAND_PAD = 11f;
    private static final long ISLAND_HOLD_MS = 1900L;
    private static final Spring ISLAND_W = new Spring(ISLAND_MIN);
    private static boolean islandActive;
    private static boolean islandOn;
    private static String islandName = "";
    private static String islandState = "";
    private static float islandFullW = ISLAND_MIN;
    private static long islandUntil;

    static {
        ISLAND_W.stiffness = 330f;
        ISLAND_W.damping = 19f; // a little bounce
    }

    public static void show(Notification notification) {
        PENDING.add(notification);
    }

    public static void info(String title, String message) {
        show(new Notification(NotificationType.INFO, title, message, 1));
    }

    public static void success(String title, String message) {
        show(new Notification(NotificationType.SUCCESS, title, message, 1));
    }

    public static void warning(String title, String message) {
        show(new Notification(NotificationType.WARNING, title, message, 1));
    }

    public static void error(String title, String message) {
        show(new Notification(NotificationType.ERROR, title, message, 2));
    }

    /** Shows the island for a module that was switched on or off. */
    public static void island(String name, boolean on) {
        islandName = name;
        islandOn = on;
        islandState = on ? "On" : "Off";
        islandFullW = ISLAND_PAD + 14f + UiText.width(name) + 14f + UiText.width(islandState) + ISLAND_PAD;
        islandUntil = System.currentTimeMillis() + ISLAND_HOLD_MS;
        if (!islandActive) {
            islandActive = true;
            ISLAND_W.snap(ISLAND_MIN);
        }
        ISLAND_W.setTarget(islandFullW);
    }

    public static void render() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!islandActive && ACTIVE.isEmpty() && PENDING.isEmpty()) return;

        ScaledResolution sr = Gfx.scaled();
        float sw = sr.getScaledWidth();
        long now = System.currentTimeMillis();

        renderIsland(sw, now);
        renderBanners(sw, now);
        net.minecraft.client.renderer.GlStateManager.color(1f, 1f, 1f, 1f);
    }

    // ------------------------------------------------------------------ island

    private static void renderIsland(float sw, long now) {
        if (!islandActive) return;
        if (now > islandUntil) ISLAND_W.setTarget(ISLAND_MIN);
        float w = ISLAND_W.get();
        if (now > islandUntil && w <= ISLAND_MIN + 1.5f) {
            islandActive = false;
            return;
        }

        float x = (sw - w) / 2f;
        float y = 6f;
        Gfx.shadow(x, y, w, ISLAND_H, ISLAND_H / 2f, 14f, 4f, 0x55000000);
        Gfx.roundRect(x, y, w, ISLAND_H, ISLAND_H / 2f, 0xFF000000);

        float content = Colors.clamp01((w - ISLAND_MIN - 10f) / Math.max(1f, islandFullW - ISLAND_MIN - 10f));
        if (content <= 0.02f) return;
        int stateColor = islandOn ? Colors.GREEN : Colors.RED;
        float ty = y + (ISLAND_H - UiText.height()) / 2f;
        Gfx.circle(x + ISLAND_PAD + 3f, y + ISLAND_H / 2f, 3.5f, Colors.scaleAlpha(stateColor, content));
        UiText.draw(islandName, x + ISLAND_PAD + 14f, ty, Colors.scaleAlpha(Colors.LABEL, content));
        UiText.drawRight(islandState, x + w - ISLAND_PAD, ty, Colors.scaleAlpha(stateColor, content));
    }

    // ------------------------------------------------------------------ banners

    private static void renderBanners(float sw, long now) {
        while (ACTIVE.size() < MAX_VISIBLE && !PENDING.isEmpty()) {
            Notification n = PENDING.poll();
            n.show();
            ACTIVE.add(n);
        }

        float slotY = EDGE + (islandActive ? 0f : 0f);
        for (int i = 0; i < ACTIVE.size(); i++) {
            Notification n = ACTIVE.get(i);
            if (n.width <= 0f) {
                n.width = Math.max(Math.max(UiText.width(n.title), UiText.width(n.message)) + 44f, 150f);
            }
            if (!n.leaving && now - n.start > n.lifeMs) {
                n.leaving = true;
                n.slide.setTarget(1f);
            }
            if (!n.placed) {
                n.y.snap(slotY);
                n.placed = true;
            }
            n.y.setTarget(slotY);

            float s = n.slide.get();
            if (n.leaving && s > 0.98f) {
                ACTIVE.remove(i);
                i--;
                continue;
            }
            float y = n.y.get();
            float x = sw - n.width - EDGE + s * (n.width + EDGE * 2f);

            Gfx.shadow(x, y, n.width, BANNER_H, 14f, 16f, 6f, 0x55000000);
            Gfx.roundRect(x, y, n.width, BANNER_H, 14f, Colors.argb(240, 28, 28, 30));
            Gfx.circle(x + 16f, y + BANNER_H / 2f, 5f, n.type.color);
            UiText.draw(n.title, x + 30f, y + 6f, Colors.LABEL);
            UiText.draw(n.message, x + 30f, y + BANNER_H - UiText.height() - 6f, Colors.LABEL_SECONDARY);

            slotY += BANNER_H + GAP;
        }
    }
}
