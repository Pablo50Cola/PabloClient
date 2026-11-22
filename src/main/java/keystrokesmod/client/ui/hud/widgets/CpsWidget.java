package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.ui.hud.Anchor;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Clicks per second for the left and right mouse button. Every click is time-stamped from the
 * Forge mouse event (so no click is missed between frames); the count is the number of clicks in
 * the last second.
 */
public final class CpsWidget extends TextPillWidget {
    private static final int RING = 64;

    private final long[] left = new long[RING];
    private final long[] right = new long[RING];
    private int leftPos;
    private int rightPos;

    public CpsWidget() {
        super("cps", "CPS", Anchor.TOP_RIGHT, 6f, 58f, 100L);
    }

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        if (!e.buttonstate) return;
        long now = System.currentTimeMillis();
        if (e.button == 0) {
            left[leftPos] = now;
            leftPos = (leftPos + 1) % RING;
        } else if (e.button == 1) {
            right[rightPos] = now;
            rightPos = (rightPos + 1) % RING;
        }
    }

    private static int count(long[] ring, long now) {
        int n = 0;
        for (int i = 0; i < RING; i++) {
            if (now - ring[i] < 1000L) n++;
        }
        return n;
    }

    @Override
    protected long sample() {
        long now = System.currentTimeMillis();
        return count(left, now) * 1000L + count(right, now);
    }

    @Override
    protected void build(StringBuilder out, long key) {
        out.append(key / 1000L).append(" | ").append(key % 1000L).append(" CPS");
    }
}
