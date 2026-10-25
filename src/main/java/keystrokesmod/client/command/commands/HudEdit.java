package keystrokesmod.client.command.commands;

import keystrokesmod.client.clickgui.raven.Terminal;
import keystrokesmod.client.command.Command;
import keystrokesmod.client.ui.hud.HudEditorScreen;
import net.minecraft.client.Minecraft;

public class HudEdit extends Command {
    public HudEdit() {
        super("hudedit", "Opens the HUD editor: drag, enable, scale and fade the HUD widgets", 0, 0,
                new String[] {}, new String[] { "widgets" });
    }

    @Override
    public void onCall(String[] args) {
        Terminal.print("Opening HUD editor...");
        // open on the next tick: the screen that ran this command closes itself right after it returns
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                Minecraft.getMinecraft().displayGuiScreen(new HudEditorScreen());
            }
        });
    }
}
