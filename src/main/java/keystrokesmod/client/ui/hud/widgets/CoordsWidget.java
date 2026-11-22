package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.ui.hud.Anchor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

/** Block position as "X 12  Y 64  Z -30". */
public final class CoordsWidget extends TextPillWidget {
    public CoordsWidget() {
        super("coords", "Coordinates", Anchor.TOP_RIGHT, 6f, 110f, 100L);
    }

    @Override
    protected long sample() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return 0L;
        long x = MathHelper.floor_double(mc.thePlayer.posX);
        long y = MathHelper.floor_double(mc.thePlayer.posY);
        long z = MathHelper.floor_double(mc.thePlayer.posZ);
        return x * 73856093L ^ y * 19349663L ^ z * 83492791L;
    }

    @Override
    protected void build(StringBuilder out, long key) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            out.append("X 0  Y 0  Z 0");
            return;
        }
        out.append("X ").append(MathHelper.floor_double(mc.thePlayer.posX))
                .append("  Y ").append(MathHelper.floor_double(mc.thePlayer.posY))
                .append("  Z ").append(MathHelper.floor_double(mc.thePlayer.posZ));
    }
}
