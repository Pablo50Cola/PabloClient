package keystrokesmod.client.command.commands;

import keystrokesmod.client.clickgui.raven.Terminal;
import keystrokesmod.client.command.Command;
import keystrokesmod.client.ui.demo.UiDemoScreen;
import net.minecraft.client.Minecraft;

public class UiDemo extends Command {
    public UiDemo() {
        super("uidemo", "Opens the UI toolkit demo screen", 0, 0, new String[] {}, new String[] { "ui" });
    }

    @Override
    public void onCall(String[] args) {
        Terminal.print("Opening UI demo...");
        // open on the next tick: the screen that ran this command closes itself right after it returns
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                Minecraft.getMinecraft().displayGuiScreen(new UiDemoScreen());
            }
        });
    }
}
