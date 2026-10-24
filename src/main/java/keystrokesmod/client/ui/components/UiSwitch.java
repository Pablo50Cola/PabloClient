package keystrokesmod.client.ui.components;

import keystrokesmod.client.ui.anim.Spring;
import keystrokesmod.client.ui.gfx.Colors;
import keystrokesmod.client.ui.gfx.Gfx;

/** iOS-style toggle switch with a spring-animated knob. Sizes are in GUI units. */
public final class UiSwitch {
    public static final float WIDTH = 38f;
    public static final float HEIGHT = 23f;
    private static final float KNOB = 20f;
    private static final float INSET = 1.5f;
    private static final float TRAVEL = WIDTH - KNOB - INSET * 2f;

    private final Spring knob;
    private boolean on;

    public UiSwitch(boolean on) {
        this.on = on;
        this.knob = new Spring(on ? 1f : 0f);
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean value) {
        on = value;
        knob.setTarget(value ? 1f : 0f);
    }

    public void toggle() {
        setOn(!on);
    }

    public boolean hit(float x, float y, float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    public void draw(float x, float y) {
        float t = knob.get();
        Gfx.roundRect(x, y, WIDTH, HEIGHT, HEIGHT / 2f, Colors.lerp(Colors.SWITCH_OFF, Colors.GREEN, t));

        float kx = x + INSET + t * TRAVEL;
        float ky = y + INSET;
        Gfx.shadow(kx, ky, KNOB, KNOB, KNOB / 2f, 4f, 1.5f, 0x55000000);
        Gfx.circle(kx + KNOB / 2f, ky + KNOB / 2f, KNOB / 2f, 0xFFFFFFFF);
    }
}
