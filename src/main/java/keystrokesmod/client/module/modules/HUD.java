package keystrokesmod.client.module.modules;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;



import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.Setting;
import keystrokesmod.client.ui.hud.HudEditorScreen;
import keystrokesmod.client.module.setting.impl.ComboSetting;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.RenderUtils;
import keystrokesmod.client.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class HUD extends Module {
    public static TickSetting editPosition, dropShadow, logo, watermark, keybinds, arrayListBackground, arrayListBorder;
    public static ComboSetting logoMode;
    public static SliderSetting colourMode, logoScaleh, logoScalew, arrayListOpacity;
    public static DescriptionSetting colourModeDesc, logoDesc1, logoDesc2;
    public static int hudX = 5;
    public static int hudY = 70;
    public static int watermarkX = 5;
    public static int watermarkY = 5;
    public static int keybindsX = 5;
    public static int keybindsY = 50;
    public static int targetHUDX = 100;
    public static int targetHUDY = 100;
    private InputStream inputStream;
    private ResourceLocation ravenLogo;

    public static Utils.HUD.PositionMode positionMode;
    public static boolean showedError;
    public static final String HUDX_prefix = "HUDX~ ";
    public static final String HUDY_prefix = "HUDY~ ";

    public enum lmv {
        l1, l2, l3, l4, l5, l6, l7, CD
    }

    public HUD() {
        super("HUD", ModuleCategory.render);
        this.registerSetting(editPosition = new TickSetting("Edit position", false));
        this.registerSetting(dropShadow = new TickSetting("Drop shadow", true));
        this.registerSetting(logo = new TickSetting("Logo", true));
        this.registerSetting(watermark = new TickSetting("Watermark", true));
        this.registerSetting(keybinds = new TickSetting("Keybinds", true));
        this.registerSetting(arrayListBackground = new TickSetting("Array List Background", false));
        this.registerSetting(arrayListBorder = new TickSetting("Array List Border", true));
        this.registerSetting(colourMode = new SliderSetting("Value: ", 1, 1, 7, 1));
        this.registerSetting(colourModeDesc = new DescriptionSetting("Mode: RAVEN"));
        this.registerSetting(logoScaleh = new SliderSetting("Logo Scale height ", 1, 0, 10, 0.01));
        this.registerSetting(logoScalew = new SliderSetting("Logo Scale width ", 2, 0, 10, 0.01));
        this.registerSetting(arrayListOpacity = new SliderSetting("Array List Opacity", 0.8, 0.1, 1.0, 0.1));
        this.registerSetting(logoMode = new ComboSetting("Logo Mode:", lmv.l7));
        this.registerSetting(logoDesc1 = new DescriptionSetting("cd logomode put an image logo.png"));
        this.registerSetting(logoDesc1 = new DescriptionSetting("in the keystrokes folder"));
        showedError = false;
        showInHud = false;
    }

    private void setUpLogo() {
        RenderUtils.getResourcePath("/assets/keystrokes/logohud/" + logoMode.getMode().toString() + ".png");
    }

    @Override
	public void postApplyConfig() {
        setUpLogo();
    }

    @Override
	public void guiButtonToggled(Setting b) {
        if (b == logoMode)
            setUpLogo();
        else if (b == editPosition) {
            editPosition.disable();
            mc.displayGuiScreen(new HudEditorScreen());
        }
    }

    public boolean logoLoaded() {
        return (ravenLogo != null) && logo.isToggled();
    }

    @Override
	public void guiUpdate() {
        colourModeDesc.setDesc(Utils.md + ColourModes.values()[(int) colourMode.getInput() - 1]);
    }

    @Override
	public void onEnable() {
        Raven.moduleManager.sort();
        loadLegacyPositions();
    }

    public enum ColourModes {
        RAVEN, RAVEN2, ASTOLFO, ASTOLFO2, ASTOLFO3, KV, ORANGE
    }

    public static int getHudX() {
        return hudX;
    }

    public static int getHudY() {
        return hudY;
    }

    public static void setHudX(int hudX) {
        HUD.hudX = hudX;
    }

    public static void setHudY(int hudY) {
        HUD.hudY = hudY;
    }
    
    public static void setTargetHUDPosition(int x, int y) {
        targetHUDX = x;
        targetHUDY = y;
    }
    
    public static void resetTargetHUDPosition() {
        targetHUDX = 100;
        targetHUDY = 100;
    }
    
        
    private static File getHudPositionsFile() {
        return new File(Minecraft.getMinecraft().mcDataDir + File.separator + "keystrokes" + File.separator + "hudpos.json");
    }

    private static void ensureDirExists(File f) {
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    @Override
    public void onDisable() {
        saveHUDPositions();
        super.onDisable();
    }

    private static String buildHudDataString() {
        StringBuilder hudData = new StringBuilder();
        hudData.append("hudX,").append(hudX).append(",").append(hudY).append(";");
        hudData.append("watermarkX,").append(watermarkX).append(",").append(watermarkY).append(";");
        hudData.append("keybindsX,").append(keybindsX).append(",").append(keybindsY).append(";");
        hudData.append("targetHUDX,").append(targetHUDX).append(",").append(targetHUDY).append(";");
        hudData.append("radarX,").append(keystrokesmod.client.module.modules.render.Radar.radarX).append(",").append(keystrokesmod.client.module.modules.render.Radar.radarY).append(";");
        return hudData.toString();
    }

    /**
     * Reads {@code keystrokes/hudpos.json}. The module list, watermark and keybinds are widgets now
     * and keep their position in the client config; this file still holds the Target HUD and Radar
     * positions, and the old positions of the three widgets (used once to carry them over).
     */
    public static void loadLegacyPositions() {
        File hudFile = getHudPositionsFile();
        if (!hudFile.exists()) return;

        try (FileReader reader = new FileReader(hudFile)) {
            JsonParser jsonParser = new JsonParser();
            JsonObject data = jsonParser.parse(reader).getAsJsonObject();
            if (data == null || !data.has("hud_positions")) return;

            String hudData = data.get("hud_positions").getAsString();
            if (hudData == null || hudData.isEmpty()) return;

            String[] positions = hudData.split(";");
            for (String pos : positions) {
                String[] parts = pos.split(",");
                if (parts.length == 3) {
                    String element = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    switch (element) {
                        case "hudX": hudX = x; break;
                        case "hudY": hudY = y; break;
                        case "watermarkX": watermarkX = x; break;
                        case "watermarkY": watermarkY = y; break;
                        case "keybindsX": keybindsX = x; break;
                        case "keybindsY": keybindsY = y; break;
                        case "targetHUDX": targetHUDX = x; break;
                        case "targetHUDY": targetHUDY = y; break;
                        case "radarX": keystrokesmod.client.module.modules.render.Radar.radarX = x; break;
                        case "radarY": keystrokesmod.client.module.modules.render.Radar.radarY = y; break;
                    }
                }
            }
        } catch (JsonSyntaxException | ClassCastException | IllegalStateException | NumberFormatException | IOException e) {
            resetAllPositions();
        }
    }

    private static void writeHudPositions(String hudData) {
        File hudFile = getHudPositionsFile();
        ensureDirExists(hudFile);

        try {
            JsonObject data;
            if (hudFile.exists()) {
                try (FileReader reader = new FileReader(hudFile)) {
                    JsonParser jsonParser = new JsonParser();
                    data = jsonParser.parse(reader).getAsJsonObject();
                } catch (Exception e) {
                    data = new JsonObject();
                }
            } else {
                data = new JsonObject();
            }

            data.addProperty("hud_positions", hudData);
            try (PrintWriter out = new PrintWriter(new FileWriter(hudFile))) {
                out.write(data.toString());
            }
        } catch (Exception ignored) {}
    }

    private static void saveHUDPositions() {
        writeHudPositions(buildHudDataString());
    }

    public static void savePositions() {
        writeHudPositions(buildHudDataString());
    }

    private static void resetAllPositions() {
        hudX = 5;
        hudY = 70;
        watermarkX = 5;
        watermarkY = 5;
        keybindsX = 5;
        keybindsY = 50;
        targetHUDX = 100;
        targetHUDY = 100;
    }
}
