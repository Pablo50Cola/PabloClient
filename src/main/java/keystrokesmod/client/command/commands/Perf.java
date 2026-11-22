package keystrokesmod.client.command.commands;

import java.util.Locale;

import keystrokesmod.client.clickgui.raven.Terminal;
import keystrokesmod.client.command.Command;
import keystrokesmod.client.ui.UiSettings;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.hud.Widget;
import keystrokesmod.client.ui.hud.WidgetManager;
import keystrokesmod.client.ui.perf.FrameCost;
import net.minecraft.client.Minecraft;

/**
 * {@code perf} prints the client's own frame cost, the FPS and the toolkit draw calls.
 * {@code perf overlay} (or {@code perf overlay on|off}) shows the same numbers as a small HUD widget.
 */
public class Perf extends Command {
    public Perf() {
        super("perf", "Shows client frame time, FPS and draw calls. 'perf overlay [on|off]' toggles a live overlay", 0,
                2, new String[] { "overlay", "on|off" }, new String[] { "fps" });
    }

    @Override
    public void onCall(String[] args) {
        if (args.length == 0) {
            Terminal.print(String.format(Locale.ROOT,
                    "Client frame time %.2f ms (last %.2f ms) | %d FPS | %d draw calls | blur %s",
                    FrameCost.avgMs(), FrameCost.lastMs(), Minecraft.getDebugFPS(), Gfx.drawCallsLastFrame(),
                    UiSettings.quality.name().toLowerCase(Locale.ROOT)));
            Terminal.print("Tip: 'perf overlay' shows this live on screen.");
            return;
        }

        if (!args[0].equalsIgnoreCase("overlay")) {
            this.incorrectArgs();
            return;
        }

        Widget overlay = WidgetManager.get("perf");
        if (overlay == null) {
            Terminal.print("The overlay is not available.");
            return;
        }
        boolean on;
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("on")) {
                on = true;
            } else if (args[1].equalsIgnoreCase("off")) {
                on = false;
            } else {
                this.incorrectArgs();
                return;
            }
        } else {
            on = !overlay.isEnabled();
        }
        overlay.setEnabled(on);
        WidgetManager.save();
        Terminal.print("Performance overlay " + (on ? "on" : "off") + ".");
    }
}
