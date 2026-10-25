package keystrokesmod.client.module.modules;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.ui.hud.HudEditorScreen;

/** Switch that opens the HUD widget editor (the same screen as the {@code hudedit} command). */
public class HUDEditor extends Module {

    public HUDEditor() {
        super("HUDEditor", ModuleCategory.config);
        this.registerSetting(new DescriptionSetting("Click to open HUD editor"));
        showInHud = false;
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            mc.displayGuiScreen(new HudEditorScreen());
        }
        this.disable();
    }
}
