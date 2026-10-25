package keystrokesmod.client.ui.hud;

import java.util.ArrayList;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import keystrokesmod.client.event.impl.Render2DEvent;
import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.HUD;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.hud.widgets.KeybindsWidget;
import keystrokesmod.client.ui.hud.widgets.LegacyBoxWidget;
import keystrokesmod.client.ui.hud.widgets.ModuleListWidget;
import keystrokesmod.client.ui.hud.widgets.PerfWidget;
import keystrokesmod.client.ui.hud.widgets.WatermarkWidget;
import keystrokesmod.client.ui.perf.FrameCost;
import keystrokesmod.client.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Registry and renderer of all HUD widgets (Phase 2).
 *
 * One render pass per frame, driven by {@link Render2DEvent}. Disabled widgets cost nothing:
 * they are skipped before anything is measured. The whole pass is wrapped in a single
 * {@link FrameCost#begin()} / {@link FrameCost#end()} pair so the {@code perf} command can show it.
 */
public final class WidgetManager {
    private static final WidgetManager INSTANCE = new WidgetManager();
    private static final ArrayList<Widget> LIST = new ArrayList<>();

    /** Plain array copy of {@link #LIST}: iterating it with an index creates no iterator objects. */
    private static Widget[] widgets = new Widget[0];

    private static boolean initialized;
    /** True when no saved widget data was found, so the old HUD positions should be carried over. */
    private static boolean pendingMigration;

    private static Module hudModule;
    private static int hudLookupCooldown;
    private static net.minecraft.util.Timer timer;

    private WidgetManager() {
    }

    // ------------------------------------------------------------------ setup

    /** Creates the built-in widgets and starts listening for render events. Call once from {@code Raven.init()}. */
    public static void init() {
        if (initialized) return;
        initialized = true;
        try {
            // Target HUD and Radar still keep their position in hudpos.json; load it now, not only
            // when the old HUD module is switched on, so the editor never shows (and saves) defaults.
            HUD.loadLegacyPositions();
        } catch (RuntimeException ignored) {
        }
        register(new ModuleListWidget());
        register(new WatermarkWidget());
        register(new KeybindsWidget());
        register(new PerfWidget());
        register(new LegacyBoxWidget(LegacyBoxWidget.Kind.TARGET_HUD));
        register(new LegacyBoxWidget(LegacyBoxWidget.Kind.RADAR));
        Raven.eventBus.register(INSTANCE);
    }

    public static void register(Widget w) {
        LIST.add(w);
        widgets = LIST.toArray(new Widget[0]);
    }

    /** All widgets in drawing order. Treat as read-only. */
    public static Widget[] all() {
        return widgets;
    }

    public static Widget get(String id) {
        for (int i = 0; i < widgets.length; i++) {
            if (widgets[i].id.equals(id)) return widgets[i];
        }
        return null;
    }

    /**
     * The migrated old HUD widgets (module list, watermark, keybinds) only show while the old
     * "HUD" module is switched on, exactly as before.
     */
    public static boolean legacyHudOn() {
        if (hudModule == null) {
            if (hudLookupCooldown-- > 0 || Raven.moduleManager == null) return false;
            hudLookupCooldown = 120;
            hudModule = Raven.moduleManager.getModuleByName("HUD");
            if (hudModule == null) return false;
        }
        return hudModule.isEnabled();
    }

    // ------------------------------------------------------------------ rendering

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!Utils.Player.isPlayerInGame()) return;
        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) return;
        renderGame(partialTicks());
    }

    /** The timer is looked up by reflection, so it is fetched once and kept. */
    private static float partialTicks() {
        if (timer == null) {
            try {
                timer = Utils.Client.getTimer();
            } catch (RuntimeException ignored) {
                return 0f;
            }
        }
        return timer == null ? 0f : timer.renderPartialTicks;
    }

    private static void renderGame(float partialTicks) {
        ScaledResolution sr = Gfx.scaled();
        float sw = sr.getScaledWidth();
        float sh = sr.getScaledHeight();
        if (pendingMigration) migrateLegacyPositions(sw, sh);

        FrameCost.begin();
        for (int i = 0; i < widgets.length; i++) {
            Widget w = widgets[i];
            if (!w.isVisible()) continue;
            w.setEditing(false);
            w.measure();
            if (w.width <= 0f || w.height <= 0f) continue;
            w.layout(sw, sh);
            if (w.screenX() >= sw || w.screenY() >= sh) continue;
            draw(w, partialTicks);
        }
        FrameCost.end();
    }

    /** Draws one widget with its position, scale and opacity applied. Used by the game pass and the editor. */
    static void draw(Widget w, float partialTicks) {
        if (w.usesMatrixTransform()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(w.screenX(), w.screenY(), 0f);
            float s = w.scale();
            if (s != 1f) GlStateManager.scale(s, s, 1f);
            w.render(partialTicks);
            GlStateManager.popMatrix();
        } else {
            w.render(partialTicks);
        }
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    // ------------------------------------------------------------------ saving and loading

    /** The widget part of the client config file ({@code clientconfig.kv}, key "widgets"). */
    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);
        JsonObject items = new JsonObject();
        for (int i = 0; i < widgets.length; i++) {
            if (widgets[i].isLegacyPositioned()) continue; // these keep their position in the old files
            items.add(widgets[i].id, widgets[i].toJson());
        }
        root.add("items", items);
        return root;
    }

    /**
     * Loads the saved widget data. {@code null} (no data yet) keeps the defaults and carries the old
     * HUD positions over on the first frame. Unknown widgets are ignored, missing widgets and fields
     * fall back to their defaults.
     */
    public static void fromJson(JsonObject root) {
        JsonObject items = null;
        try {
            if (root != null && root.has("items")) {
                JsonElement e = root.get("items");
                if (e.isJsonObject()) items = e.getAsJsonObject();
            }
        } catch (RuntimeException ignored) {
        }

        if (items == null) {
            // Only the real widgets go back to defaults. Target HUD and Radar keep the position that
            // was just read from hudpos.json.
            for (int i = 0; i < widgets.length; i++) {
                if (!widgets[i].isLegacyPositioned()) widgets[i].resetToDefaults();
            }
            pendingMigration = true;
            return;
        }

        pendingMigration = false;
        for (int i = 0; i < widgets.length; i++) {
            Widget w = widgets[i];
            if (w.isLegacyPositioned()) continue;
            JsonElement e = items.get(w.id);
            w.fromJson(e != null && e.isJsonObject() ? e.getAsJsonObject() : null);
        }
    }

    public static void resetAll() {
        for (int i = 0; i < widgets.length; i++) {
            Widget w = widgets[i];
            if (w instanceof LegacyBoxWidget) {
                ((LegacyBoxWidget) w).resetLegacyPosition();
            } else {
                w.resetToDefaults();
            }
        }
    }

    /** Writes the client config file, including the widget data. */
    public static void save() {
        if (Raven.clientConfig != null) Raven.clientConfig.saveConfig();
    }

    /**
     * Carries the old HUD positions over right away if that has not happened yet. The editor calls
     * this when it opens, so a migration can never overwrite positions the user just chose.
     */
    public static void applyPendingMigration() {
        if (!pendingMigration) return;
        ScaledResolution sr = Gfx.scaled();
        migrateLegacyPositions(sr.getScaledWidth(), sr.getScaledHeight());
    }

    /** First start after the update: take the positions the old HUD saved. */
    private static void migrateLegacyPositions(float sw, float sh) {
        pendingMigration = false;
        try {
            HUD.loadLegacyPositions();
            moveLegacy("modulelist", HUD.hudX, HUD.hudY, sw, sh);
            moveLegacy("watermark", HUD.watermarkX, HUD.watermarkY, sw, sh);
            moveLegacy("keybinds", HUD.keybindsX, HUD.keybindsY, sw, sh);
        } catch (RuntimeException ignored) {
            // keep the default positions
        }
    }

    private static void moveLegacy(String id, int x, int y, float sw, float sh) {
        Widget w = get(id);
        if (w == null) return;
        w.setEditing(true); // sample content gives a realistic width
        w.measure();
        w.setEditing(false);
        w.moveTo(x, y, sw, sh);
    }
}
