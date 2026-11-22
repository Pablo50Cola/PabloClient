package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.ui.hud.Anchor;
import net.minecraft.client.Minecraft;

public final class FpsWidget extends TextPillWidget {
    public FpsWidget() {
        super("fps", "FPS", Anchor.TOP_RIGHT, 6f, 32f, 250L);
    }

    @Override
    protected long sample() {
        return Minecraft.getDebugFPS();
    }

    @Override
    protected void build(StringBuilder out, long key) {
        out.append(key).append(" FPS");
    }
}
