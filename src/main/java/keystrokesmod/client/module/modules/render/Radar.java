package keystrokesmod.client.module.modules.render;

import com.google.common.eventbus.Subscribe;
import keystrokesmod.client.event.impl.Render2DEvent;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.utils.RenderUtils;
import keystrokesmod.client.utils.font.FontUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.Color;

public class Radar extends Module {

    public static int radarX = 10;
    public static int radarY = 120;
    
    private SliderSetting size;
    private SliderSetting scale;

    public Radar() {
        super("Radar", ModuleCategory.render);
        this.registerSetting(size = new SliderSetting("Size", 100, 50, 200, 5));
        this.registerSetting(scale = new SliderSetting("Scale", 2.0, 0.5, 5.0, 0.1));
    }

    /** Edge length of the (square) radar in GUI units. */
    public int sizePx() {
        return (int) size.getInput();
    }

    /**
     * Draws the radar with its top-left corner at (0, 0). Called by {@code RadarWidget}, which
     * moves and scales the drawing; the module no longer draws on its own.
     */
    public void drawRadar() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // local origin; shadows the old saved position fields (kept only to carry old positions over)
        final int radarX = 0;
        final int radarY = 0;
        int s = (int) size.getInput();
        int halfS = s / 2;
        int cx = radarX + halfS;
        int cy = radarY + halfS;
        
        // Background
        RenderUtils.drawRoundedRect(radarX, radarY, radarX + s, radarY + s, 8, 0x801A1A1A);
        
        // Border
        RenderUtils.drawBorderedRoundedRect(radarX, radarY, radarX + s, radarY + s, 8, 2, 0xFFFF8C00, 0x00000000);
        
        // Center lines
        Gui.drawRect(cx - halfS, cy - 1, cx + halfS, cy + 1, 0x20FFFFFF);
        Gui.drawRect(cx - 1, cy - halfS, cx + 1, cy + halfS, 0x20FFFFFF);
        
        // Title
        FontUtil.two.drawString("Radar", radarX + 5, radarY + 5, 0xFFFFFFFF);
        
        // Self
        RenderUtils.drawRoundedRect(cx - 2, cy - 2, cx + 2, cy + 2, 2, 0xFFFF8C00);

        // Players
        double radarScale = scale.getInput();
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer || player.isDead || player.isInvisible()) continue;
            
            double diffX = player.posX - mc.thePlayer.posX;
            double diffZ = player.posZ - mc.thePlayer.posZ;
            
            float yaw = mc.thePlayer.rotationYaw;
            double cos = Math.cos(Math.toRadians(yaw));
            double sin = Math.sin(Math.toRadians(yaw));
            
            double rotX = -(diffX * cos + diffZ * sin);
            double rotY = -(diffZ * cos - diffX * sin);
            
            int pointX = (int) (cx + rotX * radarScale);
            int pointY = (int) (cy + rotY * radarScale);
            
            // Constrain to radar bounds
            if (pointX < radarX + 4) pointX = radarX + 4;
            if (pointX > radarX + s - 4) pointX = radarX + s - 4;
            if (pointY < radarY + 4) pointY = radarY + 4;
            if (pointY > radarY + s - 4) pointY = radarY + s - 4;
            
            // Color based on Y level difference
            double diffY = player.posY - mc.thePlayer.posY;
            int blipColor = 0xFFFF0000; // Default Red
            if (diffY > 3) blipColor = 0xFF00FF00; // Above = Green
            else if (diffY < -3) blipColor = 0xFF0000FF; // Below = Blue
            
            RenderUtils.drawRoundedRect(pointX - 2, pointY - 2, pointX + 2, pointY + 2, 2, blipColor);
        }
    }
}
