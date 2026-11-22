package keystrokesmod.client.ui.hud.widgets;

import java.util.TimeZone;

import keystrokesmod.client.ui.hud.Anchor;

/** Real-world time of day (24 hour), checked once a second. */
public final class ClockWidget extends TextPillWidget {
    public ClockWidget() {
        super("clock", "Clock", Anchor.TOP_RIGHT, 6f, 136f, 1000L);
    }

    @Override
    protected long sample() {
        long now = System.currentTimeMillis();
        return (now + TimeZone.getDefault().getOffset(now)) / 60000L;
    }

    @Override
    protected void build(StringBuilder out, long key) {
        int minuteOfDay = (int) (key % 1440L);
        int h = minuteOfDay / 60;
        int m = minuteOfDay % 60;
        if (h < 10) out.append('0');
        out.append(h).append(':');
        if (m < 10) out.append('0');
        out.append(m);
    }
}
