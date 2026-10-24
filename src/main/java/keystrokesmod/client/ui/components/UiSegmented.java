package keystrokesmod.client.ui.components;

import keystrokesmod.client.ui.anim.Spring;
import keystrokesmod.client.ui.gfx.Colors;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.gfx.UiText;

/** iOS-style segmented control: a sliding thumb behind equal-width labels. Sizes are in GUI units. */
public final class UiSegmented {
    public static final float HEIGHT = 21f;
    private static final float INSET = 1.5f;

    private final String[] labels;
    private final Spring thumb;
    private int selected;

    public UiSegmented(String[] labels, int selected) {
        this.labels = labels;
        this.selected = selected;
        this.thumb = new Spring(selected);
    }

    public int selected() {
        return selected;
    }

    public void select(int index) {
        selected = index;
        thumb.setTarget(index);
    }

    /** @return the clicked segment index, or -1 when the mouse is outside the control. */
    public int hit(float x, float y, float segmentWidth, float mouseX, float mouseY) {
        if (mouseY < y || mouseY > y + HEIGHT || mouseX < x) return -1;
        int index = (int) ((mouseX - x) / segmentWidth);
        return index >= 0 && index < labels.length ? index : -1;
    }

    public void draw(float x, float y, float segmentWidth) {
        Gfx.roundRect(x, y, segmentWidth * labels.length, HEIGHT, 7f, Colors.argb(70, 118, 118, 128));

        float t = thumb.get();
        float thumbX = x + INSET + t * segmentWidth;
        Gfx.shadow(thumbX, y + INSET, segmentWidth - INSET * 2f, HEIGHT - INSET * 2f, 5.5f, 3f, 1f, 0x44000000);
        Gfx.roundRect(thumbX, y + INSET, segmentWidth - INSET * 2f, HEIGHT - INSET * 2f, 5.5f, 0xFF636366);

        float textY = y + (HEIGHT - UiText.height()) / 2f;
        for (int i = 0; i < labels.length; i++) {
            UiText.drawCentered(labels[i], x + segmentWidth * (i + 0.5f), textY, Colors.LABEL);
        }
    }
}
