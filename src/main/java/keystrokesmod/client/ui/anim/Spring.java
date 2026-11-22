package keystrokesmod.client.ui.anim;

import keystrokesmod.client.ui.UiSettings;

/**
 * Damped spring, the "iOS feel": fast start, soft settle, slight overshoot.
 * Reads the clock itself, so it can be queried any number of times per frame.
 */
public final class Spring {
    /** Higher = snappier. */
    public float stiffness = 280f;
    /** Higher = less bounce. 2*sqrt(stiffness) is critically damped (about 33). */
    public float damping = 26f;

    private float value;
    private float velocity;
    private float target;
    private long lastNs;

    public Spring(float initial) {
        this.value = initial;
        this.target = initial;
    }

    public void setTarget(float t) {
        this.target = t;
    }

    public float target() {
        return target;
    }

    public void snap(float v) {
        value = target = v;
        velocity = 0f;
    }

    public boolean isSettled() {
        return value == target && velocity == 0f;
    }

    public float get() {
        long now = System.nanoTime();
        if (lastNs == 0L) lastNs = now;
        float dt = (now - lastNs) / 1_000_000_000f;
        lastNs = now;

        if (!UiSettings.animations) {
            value = target;
            velocity = 0f;
            return value;
        }
        if (isSettled()) return value;
        if (dt > 0.05f) dt = 0.05f;

        int steps = (int) Math.ceil(dt / 0.008f);
        if (steps < 1) steps = 1;
        float h = dt / steps;
        for (int i = 0; i < steps; i++) {
            float accel = stiffness * (target - value) - damping * velocity;
            velocity += accel * h;
            value += velocity * h;
        }
        if (Math.abs(target - value) < 0.0005f && Math.abs(velocity) < 0.0005f) {
            value = target;
            velocity = 0f;
        }
        return value;
    }
}
