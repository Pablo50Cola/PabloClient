package keystrokesmod.client.ui.anim;

/** Easing curves, input and output are 0..1. No allocation. */
public enum Ease {
    LINEAR,
    OUT_CUBIC,
    IN_OUT_CUBIC,
    OUT_BACK,
    OUT_EXPO;

    public float apply(float t) {
        if (t <= 0f) return 0f;
        if (t >= 1f) return 1f;
        switch (this) {
            case OUT_CUBIC: {
                float u = 1f - t;
                return 1f - u * u * u;
            }
            case IN_OUT_CUBIC: {
                if (t < 0.5f) return 4f * t * t * t;
                float u = -2f * t + 2f;
                return 1f - u * u * u / 2f;
            }
            case OUT_BACK: {
                final float c1 = 1.70158f;
                final float c3 = c1 + 1f;
                float u = t - 1f;
                return 1f + c3 * u * u * u + c1 * u * u;
            }
            case OUT_EXPO:
                return 1f - (float) Math.pow(2.0, -10.0 * t);
            default:
                return t;
        }
    }
}
