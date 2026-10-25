package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.module.modules.HUD;
import keystrokesmod.client.module.modules.render.Radar;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.Widget;
import keystrokesmod.client.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

/**
 * Stand-in for the two elements that still draw themselves and keep their position in the old
 * modules: the Target HUD and the Radar. It is never drawn in the game (the modules do that), it
 * only appears in the editor so these two can be moved as before. Phase 3 replaces it with real
 * widgets.
 */
public final class LegacyBoxWidget extends Widget {
    public enum Kind {
        TARGET_HUD, RADAR
    }

    private final Kind kind;

    public LegacyBoxWidget(Kind kind) {
        super(kind == Kind.TARGET_HUD ? "legacy_targethud" : "legacy_radar",
                kind == Kind.TARGET_HUD ? "Target HUD" : "Radar", Anchor.TOP_LEFT, 0f, 0f, true);
        this.kind = kind;
        this.width = kind == Kind.TARGET_HUD ? 150f : 100f;
        this.height = kind == Kind.TARGET_HUD ? 50f : 100f;
    }

    @Override
    public boolean isLegacyPositioned() {
        return true;
    }

    /** Never drawn by the game pass. */
    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void measure() {
        width = kind == Kind.TARGET_HUD ? 150f : 100f;
        height = kind == Kind.TARGET_HUD ? 50f : 100f;
    }

    public int legacyX() {
        return kind == Kind.TARGET_HUD ? HUD.targetHUDX : Radar.radarX;
    }

    public int legacyY() {
        return kind == Kind.TARGET_HUD ? HUD.targetHUDY : Radar.radarY;
    }

    public void setLegacy(int x, int y) {
        if (kind == Kind.TARGET_HUD) {
            HUD.setTargetHUDPosition(x, y);
        } else {
            Radar.radarX = x;
            Radar.radarY = y;
        }
    }

    public void resetLegacyPosition() {
        if (kind == Kind.TARGET_HUD) {
            HUD.resetTargetHUDPosition();
        } else {
            Radar.radarX = 10;
            Radar.radarY = 120;
        }
    }

    @Override
    public void render(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (kind == Kind.TARGET_HUD) {
            RenderUtils.drawRoundedRect(0, 0, width, height, 8, 0x801A1A1A);
            RenderUtils.drawBorderedRoundedRect(0, 0, width, height, 8, 2, 0xFFFF8C00, 0x00000000);
            mc.fontRendererObj.drawStringWithShadow("TargetPlayer", 35, 8, 0xFFFFFF);
            mc.fontRendererObj.drawStringWithShadow("20.0/20.0", 35, 15, 0xCCCCCC);
            RenderUtils.drawRoundedRect(35, 25, 35 + 105, 33, 4, 0xFF404040);
            RenderUtils.drawRoundedRect(35, 25, 35 + 84, 33, 4, 0xFF00FF00);
        } else {
            RenderUtils.drawRoundedRect(0, 0, width, height, 8, 0x801A1A1A);
            RenderUtils.drawBorderedRoundedRect(0, 0, width, height, 8, 2, 0xFFFF8C00, 0x00000000);
            Gui.drawRect(0, 49, 100, 51, 0x20FFFFFF);
            Gui.drawRect(49, 0, 51, 100, 0x20FFFFFF);
            mc.fontRendererObj.drawString("Radar", 5, 5, 0xFFFFFFFF);
            RenderUtils.drawRoundedRect(48, 48, 52, 52, 2, 0xFFFF8C00);
        }
    }
}
