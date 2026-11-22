package keystrokesmod.client.ui.hud.widgets;

import keystrokesmod.client.ui.gfx.Colors;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.gfx.UiText;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

/**
 * The worn armor (helmet to boots) as item icons with the remaining durability in percent.
 * Only pieces that are worn take a row; the widget hides itself when nothing is worn.
 */
public final class ArmorWidget extends Widget {
    private static final float PAD = 6f;
    private static final float ROW = 18f;
    private static final String[] PERCENT = new String[101];

    static {
        for (int i = 0; i <= 100; i++) PERCENT[i] = i + "%";
    }

    private float textWidth = -1f;
    private int rows;
    private boolean placeholders;

    public ArmorWidget() {
        super("armor", "Armor", Anchor.MIDDLE_RIGHT, 6f, 0f, false);
    }

    private static ItemStack piece(int i) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return null;
        return mc.thePlayer.inventory.armorInventory[3 - i]; // 0 = helmet
    }

    private static boolean wearsAnything() {
        for (int i = 0; i < 4; i++) {
            if (piece(i) != null) return true;
        }
        return false;
    }

    @Override
    public boolean isVisible() {
        return enabled && wearsAnything();
    }

    @Override
    public void measure() {
        if (textWidth < 0f) textWidth = UiText.width("100%");
        int n = 0;
        for (int i = 0; i < 4; i++) {
            if (piece(i) != null) n++;
        }
        placeholders = n == 0 && editing;
        rows = placeholders ? 4 : n;
        width = PAD * 2f + 16f + 4f + textWidth;
        height = PAD * 2f + rows * ROW - 2f;
        if (height < 1f) height = 1f;
    }

    @Override
    public void render(float partialTicks) {
        Gfx.roundRect(0f, 0f, width, height, 8f, fade(Colors.argb(200, 28, 28, 30)));
        Minecraft mc = Minecraft.getMinecraft();
        float y = PAD;
        for (int i = 0; i < 4; i++) {
            ItemStack s = piece(i);
            if (s == null) {
                if (placeholders) {
                    Gfx.roundRect(PAD, y, 16f, 16f, 4f, fade(Colors.argb(60, 255, 255, 255)));
                    y += ROW;
                }
                continue;
            }

            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(s, (int) PAD, (int) y);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();

            if (s.isItemStackDamageable() && s.getMaxDamage() > 0) {
                int pct = (s.getMaxDamage() - s.getItemDamage()) * 100 / s.getMaxDamage();
                if (pct < 0) pct = 0;
                if (pct > 100) pct = 100;
                int color = pct > 50 ? Colors.GREEN : (pct > 25 ? Colors.ORANGE : Colors.RED);
                UiText.draw(PERCENT[pct], PAD + 20f, y + (16f - UiText.height()) / 2f, fade(color));
            }
            y += ROW;
        }
    }
}
