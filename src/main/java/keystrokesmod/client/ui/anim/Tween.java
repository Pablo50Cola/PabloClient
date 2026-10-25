package keystrokesmod.client.ui.anim;

import keystrokesmod.client.ui.UiSettings;

/**
 * A value that moves from its current position to a target over a fixed time.
 * Costs nothing while idle; reads the clock only when asked for its value.
 */
public final class Tween {
    private float from;
    private float to;
    private long startNs;
    private long durationNs;
    private Ease ease = Ease.OUT_CUBIC;
    private boolean running;

    public Tween(float initial) {
        this.from = initial;
        this.to = initial;
    }

    public void animateTo(float target, int millis, Ease easing) {
        if (target == to && !running) return;
        if (!UiSettings.animations || millis <= 0) {
            from = to = target;
            running = false;
            return;
        }
        from = get();
        to = target;
        ease = easing;
        startNs = System.nanoTime();
        durationNs = millis * 1_000_000L;
        running = true;
    }

    public void snap(float value) {
        from = to = value;
        running = false;
    }

    public float get() {
        if (!running) return to;
        float t = (System.nanoTime() - startNs) / (float) durationNs;
        if (t >= 1f) {
            running = false;
            from = to;
            return to;
        }
        return from + (to - from) * ease.apply(t);
    }

    public float target() {
        return to;
    }

    public boolean isRunning() {
        return running;
    }
}
