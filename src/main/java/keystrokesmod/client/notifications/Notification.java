package keystrokesmod.client.notifications;

import keystrokesmod.client.ui.anim.Spring;

/**
 * One banner (Phase 4). Holds its text, its lifetime and the two springs the manager animates:
 * {@link #slide} (1 = off the right edge, 0 = in place) and {@link #y} (the stack position).
 */
public class Notification {
    final NotificationType type;
    final String title;
    final String message;
    final long lifeMs;

    long start;
    boolean leaving;
    boolean placed;
    float width;
    final Spring slide = new Spring(1f);
    final Spring y = new Spring(0f);

    /** @param length lifetime multiplier: 1 = 3 seconds */
    public Notification(NotificationType type, String title, String message, int length) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.lifeMs = 3000L * Math.max(1, length);
        slide.stiffness = 240f;
        slide.damping = 22f;
    }

    public void show() {
        start = System.currentTimeMillis();
        slide.snap(1f);
        slide.setTarget(0f);
    }

    public boolean isShown() {
        return !leaving || slide.get() < 0.98f;
    }
}
