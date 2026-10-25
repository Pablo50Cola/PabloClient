package keystrokesmod.client.ui;

import keystrokesmod.client.ui.gfx.Gfx;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Frame bookkeeping for the UI toolkit: resets the once-per-frame blur capture. */
public class UiEvents {

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            Gfx.newFrame();
        }
    }
}
