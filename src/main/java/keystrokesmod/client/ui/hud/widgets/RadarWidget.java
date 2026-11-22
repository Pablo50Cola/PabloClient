package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.render.Radar;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.Widget;
import net.minecraft.client.Minecraft;

/**
 * The Radar as a real widget (Phase 3b): position, scale and on/off come from the HUD editor, the
 * Size and Scale settings stay in the Radar module. Shown while the Radar module is on.
 */
public final class RadarWidget extends Widget {
    private Radar module;

    public RadarWidget() {
        super("radar", "Radar", Anchor.TOP_LEFT, 10f, 120f, true);
    }

    private Radar module() {
        if (module == null && Raven.moduleManager != null) {
            Module m = Raven.moduleManager.getModuleByName("Radar");
            if (m instanceof Radar) module = (Radar) m;
        }
        return module;
    }

    @Override
    public boolean isVisible() {
        Radar r = module();
        return enabled && r != null && r.isEnabled();
    }

    @Override
    public String hint() {
        return "Needs the Radar module on.";
    }

    @Override
    public void measure() {
        Radar r = module();
        width = r != null ? r.sizePx() : 100f;
        height = width;
    }

    @Override
    public void render(float partialTicks) {
        Radar r = module();
        if (r == null || Minecraft.getMinecraft().thePlayer == null) return;
        r.drawRadar();
    }
}
