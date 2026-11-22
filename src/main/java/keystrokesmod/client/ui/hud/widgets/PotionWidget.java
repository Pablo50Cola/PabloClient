package keystrokesmod.client.ui.hud.widgets;

import java.util.Collection;

import keystrokesmod.client.ui.gfx.Colors;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.gfx.UiText;
import keystrokesmod.client.ui.hud.Anchor;
import keystrokesmod.client.ui.hud.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

/**
 * Active potion effects: the vanilla effect icon, the name (with level) and the time left.
 * Up to {@value #MAX} effects. The list is read four times a second; a name is rebuilt only when
 * the effect or its level changed, a time only when the whole seconds changed.
 */
public final class PotionWidget extends Widget {
    private static final int MAX = 6;
    private static final long REFRESH_MS = 250L;
    private static final float PAD = 6f;
    private static final float ROW = 22f;
    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/container/inventory.png");
    private static final Gui GUI = new Gui();

    private final Potion[] potions = new Potion[MAX];
    private final int[] nameKey = new int[MAX];
    private final int[] timeKey = new int[MAX];
    private final String[] names = new String[MAX];
    private final String[] times = new String[MAX];
    private final StringBuilder builder = new StringBuilder(16);
    private int count;
    private float textWidth;
    private long lastRefresh;
    private boolean lastEditing;
    private boolean fresh = true;

    public PotionWidget() {
        super("potions", "Potion effects", Anchor.MIDDLE_LEFT, 6f, 0f, false);
    }

    @Override
    public boolean isVisible() {
        Minecraft mc = Minecraft.getMinecraft();
        return enabled && mc.thePlayer != null && !mc.thePlayer.getActivePotionEffects().isEmpty();
    }

    private void refresh() {
        long now = System.currentTimeMillis();
        if (!fresh && editing == lastEditing && now - lastRefresh < REFRESH_MS) return;
        fresh = false;
        lastRefresh = now;
        lastEditing = editing;

        Minecraft mc = Minecraft.getMinecraft();
        int n = 0;
        if (mc.thePlayer != null) {
            Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
            for (PotionEffect e : effects) {
                if (n >= MAX) break;
                Potion p = e.getPotionID() >= 0 && e.getPotionID() < Potion.potionTypes.length
                        ? Potion.potionTypes[e.getPotionID()] : null;
                if (p == null || !p.hasStatusIcon()) continue;

                int nk = e.getPotionID() * 256 + e.getAmplifier() + 1;
                if (potions[n] != p || nameKey[n] != nk || names[n] == null) {
                    builder.setLength(0);
                    builder.append(I18n.format(e.getEffectName()));
                    if (e.getAmplifier() > 0) builder.append(' ').append(e.getAmplifier() + 1);
                    names[n] = builder.toString();
                    nameKey[n] = nk;
                }
                int sec = e.getDuration() / 20;
                if (timeKey[n] != sec + 1 || times[n] == null || potions[n] != p) {
                    if (sec > 3600) {
                        times[n] = "**";
                    } else {
                        builder.setLength(0);
                        builder.append(sec / 60).append(':');
                        if (sec % 60 < 10) builder.append('0');
                        builder.append(sec % 60);
                        times[n] = builder.toString();
                    }
                    timeKey[n] = sec + 1;
                }
                potions[n] = p;
                n++;
            }
        }
        if (n == 0 && editing) {
            potions[0] = Potion.moveSpeed;
            names[0] = "Speed 2";
            times[0] = "1:30";
            nameKey[0] = -1;
            timeKey[0] = -1;
            n = 1;
        }
        count = n;

        float w = 0f;
        for (int i = 0; i < n; i++) {
            w = Math.max(w, Math.max(UiText.width(names[i]), UiText.width(times[i])));
        }
        textWidth = w;
    }

    @Override
    public void measure() {
        refresh();
        width = PAD * 2f + 18f + 6f + textWidth;
        height = PAD * 2f + count * ROW - 2f;
        if (height < 1f) height = 1f;
    }

    @Override
    public void render(float partialTicks) {
        Gfx.roundRect(0f, 0f, width, height, 8f, fade(Colors.argb(200, 28, 28, 30)));
        Minecraft mc = Minecraft.getMinecraft();
        float y = PAD;
        for (int i = 0; i < count; i++) {
            int icon = potions[i].getStatusIconIndex();
            mc.getTextureManager().bindTexture(ICONS);
            GlStateManager.color(1f, 1f, 1f, opacity);
            GlStateManager.enableBlend();
            GUI.drawTexturedModalRect((int) PAD, (int) y, icon % 8 * 18, 198 + icon / 8 * 18, 18, 18);
            GlStateManager.disableBlend();
            GlStateManager.color(1f, 1f, 1f, 1f);

            UiText.draw(names[i], PAD + 24f, y + 1f, fade(Colors.LABEL));
            UiText.draw(times[i], PAD + 24f, y + 11f, fade(Colors.LABEL_SECONDARY));
            y += ROW;
        }
    }
}
