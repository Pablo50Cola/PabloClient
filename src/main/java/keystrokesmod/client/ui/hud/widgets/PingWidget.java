package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.ui.hud.Anchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

/** Round-trip time to the server as the tab list reports it. Shows "-- ms" in singleplayer. */
public final class PingWidget extends TextPillWidget {
    public PingWidget() {
        super("ping", "Ping", Anchor.TOP_RIGHT, 6f, 84f, 1000L);
    }

    @Override
    protected long sample() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.getNetHandler() == null) return -1L;
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        if (info == null || info.getResponseTime() <= 0) return -1L;
        return info.getResponseTime();
    }

    @Override
    protected void build(StringBuilder out, long key) {
        if (key < 0L) {
            out.append("-- ms");
        } else {
            out.append(key).append(" ms");
        }
    }
}
