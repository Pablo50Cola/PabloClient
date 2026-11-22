package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.render.TargetHUD;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.Widget;

/**
 * The Target HUD as a real widget (Phase 3b): position, scale and on/off come from the HUD
 * editor, the mode and fade time stay in the Target HUD module. Shown while that module is on; the
 * editor shows a sample.
 */
public final class TargetHudWidget extends Widget {
    private TargetHUD module;

    public TargetHudWidget() {
        super("targethud", "Target HUD", Anchor.TOP_LEFT, 100f, 100f, true);
    }

    private TargetHUD module() {
        if (module == null && Raven.moduleManager != null) {
            Module m = Raven.moduleManager.getModuleByName("Target HUD");
            if (m instanceof TargetHUD) module = (TargetHUD) m;
        }
        return module;
    }

    @Override
    public boolean isVisible() {
        TargetHUD t = module();
        return enabled && t != null && t.isEnabled();
    }

    @Override
    public String hint() {
        return "Needs the Target HUD module on.";
    }

    @Override
    public void measure() {
        TargetHUD t = module();
        width = t != null ? t.widgetWidth() : 150f;
        height = t != null ? t.widgetHeight() : 50f;
    }

    @Override
    public void render(float partialTicks) {
        TargetHUD t = module();
        if (t == null) return;
        if (editing) {
            t.drawSample();
        } else {
            t.drawWidget();
        }
    }
}
