package keystrokesmod.client.module.modules.client;

import keystrokesmod.client.clickgui.raven.components.CategoryComponent;
import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.Setting;
import keystrokesmod.client.module.setting.impl.ComboSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.ColorM;
import keystrokesmod.client.utils.Utils;

public class GuiModule extends Module {

    private static ComboSetting preset;

    private static TickSetting cleanUp, reset, rainbowNotification, notifications, animeGirl;
    private static SliderSetting animeGirlSize, animeGirlOpacity;

    public static int guiScale;

    public GuiModule() {
        super("Gui", ModuleCategory.client);
        withKeycode(54);
        this.registerSetting(cleanUp = new TickSetting("Clean Up", false));
        this.registerSetting(reset = new TickSetting("Reset position", false));
        this.registerSetting(notifications = new TickSetting("Toggle island", true));
        this.registerSetting(rainbowNotification = new TickSetting("Reset position", false));
        this.registerSetting(animeGirl = new TickSetting("Anime Girl", false));
        this.registerSetting(animeGirlSize = new SliderSetting("Anime Girl Size", 202, 20, 500, 5));
        this.registerSetting(animeGirlOpacity = new SliderSetting("Anime Girl Opacity", 80, 10, 100, 5));
        this.registerSetting(preset = new ComboSetting("Preset", Preset.Orange));
    }

    @Override
    public void guiButtonToggled(Setting setting) {
        if (setting == cleanUp) {
            cleanUp.disable();
            for (CategoryComponent cc : Raven.clickGui.getCategoryList())
                cc.setCoords(((cc.getX() / 50) * 50) + ((cc.getX() % 50) > 25 ? 50 : 0), ((cc.getY() / 50) * 50) + ((cc.getY() % 50) > 25 ? 50 : 0));
        } else if (setting == reset) {
            reset.disable();
            Raven.clickGui.resetSort();
        }

    }

    @Override
    public void onEnable() {
        if (Utils.Player.isPlayerInGame() && ((mc.currentScreen != Raven.clickGui) || (mc.currentScreen != Raven.kvCompactGui))) {
            mc.displayGuiScreen(Raven.clickGui);
            Raven.clickGui.initMain();
        }
        this.disable();
    }


    private static Preset getPresetMode() {
        return (Preset) preset.getMode();
    }

    // sgimas going to tell me theres a better way to do this isnt he

    public static boolean isCategoryBackgroundToggled() {
        return getPresetMode().categoryBackground;
    }

    public static boolean showGradientEnabled() {
        return getPresetMode().showGradientEnabled;
    }

    public static boolean showGradientDisabled() {
        return  getPresetMode().showGradientDisabled;
    }

    public static boolean useCustomFont() {
        return  getPresetMode().useCustomFont;
    }

    public static int getEnabledTopRGB(int delay) {
        return  getPresetMode().enabledTopRGB.color(delay);
    }

    public static int getEnabledTopRGB() {
        return getEnabledTopRGB(0);
    }

    public static int getEnabledBottomRGB(int delay) {
        return  getPresetMode().enabledBottomRGB.color(delay);
    }

    public static int getEnabledBottomRGB() {
        return getEnabledBottomRGB(0);
    }

    public static int getEnabledTextRGB(int delay) {
        return  getPresetMode().enabledTextRGB.color(delay);
    }

    public static int getEnabledTextRGB() {
        return getEnabledTextRGB(0);
    }

    public static int getDisabledTopRGB(int delay) {
        return  getPresetMode().disabledTopRGB.color(delay);
    }

    public static int getDisabledTopRGB() {
        return getDisabledTopRGB(0);
    }

    public static int getDisabledBottomRGB(int delay) {
        return  getPresetMode().disabledBottomRGB.color(delay);
    }

    public static int getDisabledBottomRGB() {
        return getDisabledBottomRGB(0);
    }

    public static int getDisabledTextRGB(int delay) {
        return  getPresetMode().disabledTextRGB.color(0);
    }

    public static int getDisabledTextRGB() {
        return getDisabledTextRGB(0);
    }

    public static int getBackgroundRGB(int delay) {
        return  getPresetMode().backgroundRGB.color(delay);
    }

    public static int getBackgroundRGB() {
        return getBackgroundRGB(0);
    }

    public static int getSettingBackgroundRGB(int delay) {
        return  getPresetMode().settingBackgroundRGB.color(delay);
    }

    public static int getSettingBackgroundRGB() {
        return getSettingBackgroundRGB(0);
    }


    public static int getCategoryBackgroundRGB(int delay) {
        return  getPresetMode().categoryBackgroundRGB.color(delay);
    }

    public static int getCategoryBackgroundRGB() {
        return getCategoryBackgroundRGB(0);
    }

    public static int getCategoryNameRGB(int delay) {
        return  getPresetMode().categoryNameRGB.color(delay);
    }

    public static int getCategoryNameRGB() {
        return getCategoryNameRGB(0);
    }

    public static int getBoarderColour(int delay) {
        return  getPresetMode().boarderColor.color(delay);
    }

    public static int getBoarderColour() {
        return getBoarderColour(0);
    }

    public static int getCategoryOutlineColor1(int delay) {
        return  getPresetMode().categoryOutlineColor.color(delay);
    }

    public static int getCategoryOutlineColor1() {
        return getCategoryOutlineColor1(0);
    }

    public static int getCategoryOutlineColor2(int delay) {
        return  getPresetMode().categoryOutlineColor2.color(delay);
    }

    public static int getCategoryOutlineColor2() {
        return getCategoryOutlineColor2(0);
    }

    public static CNColor getCNColor() {
        return  getPresetMode().cnColor;
    }

    public static boolean isSwingToggled() {
        return  getPresetMode().swing;
    }

    public static boolean isRoundedToggled() {
        return  getPresetMode().roundedCorners;
    }

    public static boolean isBoarderToggled() {
        return  getPresetMode().boarder;
    }

    public static boolean rainbowNotification() {
        return rainbowNotification.isToggled();
    }

    public static boolean notifications() {
        return notifications.isToggled();
    }

    public static boolean animeGirlEnabled() {
        return animeGirl.isToggled();
    }

    public static float getAnimeGirlSize() {
        return (float) animeGirlSize.getInput();
    }

    public static float getAnimeGirlOpacity() {
        return (float) animeGirlOpacity.getInput();
    }

    public enum Preset {
        /* Vape(true, false, true, true, // showGradientEnabled - showGradientDisabled - useCustomFont -
                // categoryBackground
                CNColor.STATIC, // just leave this
                // new Color(red, green, blue, alpha (optional out of 255 default is 255))
                new Color(255, 255, 255), // categoryNameRGB
                new Color(27, 25, 26, 255), // settingBackgroundRGB
                new Color(27, 25, 26), // categoryBackgroundRGB
                new Color(59, 132, 107), // enabledTopRGB
                new Color(59, 132, 107), // enabledBottomRGB
                new Color(250, 250, 250), // enabledTextRGB
                new Color(27, 25, 26), // disabledTopRGB
                new Color(27, 25, 26), // disabledBottomRGB
                new Color(255, 255, 255), // disabledTextRGB
                new Color(27, 25, 26), // backgroundRGBW
                false, //rounded
                false //swing
                ), */
        Vape( // name
                        true, false, true, true, // showGradientEnabled - showGradientDisabled - useCustomFont -
                        CNColor.STATIC, // just leave this
                        in -> 0xFFFFFFFE, // categoryNameRGB
                        in -> 0x99808080, // settingBackgroundRGB
                        in -> 0x99808080, // categoryBackgroundRGB
                        in -> -12876693, // enabledTopRGB
                        in -> -12876693, // enabledBottomRGB
                        in -> 0xFFFFFFFE, // enabledTextRGB
                        in -> 0xFF000000, // disabledTopRGB
                        in -> 0xFF000000, // disabledBottomRGB
                        in -> 0xFFFFFFFE, // disabledTextRGB
                        in -> 0x99808080, // backgroundRGB
                        true, //rounded
                        true, //swing
                        false, //boarder
                        in -> -12876693,
                        in -> -12876693,
                        in -> Utils.Client.otherAstolfoColorsDraw(in, 10)
                        ),

        PlusPlus( // name
                        true, false, true, true, // showGradientEnabled - showGradientDisabled - useCustomFont -
                        CNColor.STATIC, // just leave this
                        in -> 0xFFFFFFFE, // categoryNameRGB
                        in -> -15001318, // settingBackgroundRGB
                        in -> -15001318, // categoryBackgroundRGB
                        in -> Utils.Client.rainbowDraw(2, in), // enabledTopRGB
                        in -> Utils.Client.rainbowDraw(2, in), // enabledBottomRGB
                        in -> 0xFF000000, // enabledTextRGB
                        in -> 0xFF000000, // disabledTopRGB
                        in -> 0xFF000000, // disabledBottomRGB
                        in -> 0xFFFFFFFE, // disabledTextRGB
                        in -> 0xFF808080, // backgroundRGB
                        true, //rounded
                        true, //swing
                        true, //boarder
                        in -> 0xFFFFFFFE,
                        in -> Utils.Client.astolfoColorsDraw(in, 10),
                        in -> Utils.Client.otherAstolfoColorsDraw(in, 10)
                        ),

        Orange( // name
                        true, false, true, true, // showGradientEnabled - showGradientDisabled - useCustomFont -
                        CNColor.STATIC, // just leave this
                        in -> 0xFFFFFFFE, // categoryNameRGB
                        in -> 0xFF1A1A1A, // settingBackgroundRGB
                        in -> 0xFF1A1A1A, // categoryBackgroundRGB
                        in -> 0xFFFF8C00, // enabledTopRGB
                        in -> 0xFFFF8C00, // enabledBottomRGB
                        in -> 0xFFFFFFFF, // enabledTextRGB
                        in -> 0xFF2A2A2A, // disabledTopRGB
                        in -> 0xFF2A2A2A, // disabledBottomRGB
                        in -> 0xFFCCCCCC, // disabledTextRGB
                        in -> 0xFF121212, // backgroundRGB
                        true, //rounded
                        true, //swing
                        true, //boarder
                        in -> 0xFFFF8C00,
                        in -> 0xFFFF8C00,
                        in -> 0xFF555555
                        ),

        Purple( // name
                        true, false, true, true, // showGradientEnabled - showGradientDisabled - useCustomFont -
                        CNColor.STATIC, // just leave this
                        in -> 0xFFFFFFFE, // categoryNameRGB
                        in -> 0xFF1A1A2A, // settingBackgroundRGB
                        in -> 0xFF1A1A2A, // categoryBackgroundRGB
                        in -> 0xFF9B59B6, // enabledTopRGB
                        in -> 0xFF8E44AD, // enabledBottomRGB
                        in -> 0xFFFFFFFF, // enabledTextRGB
                        in -> 0xFF2A2A3A, // disabledTopRGB
                        in -> 0xFF2A2A3A, // disabledBottomRGB
                        in -> 0xFFCCCCCC, // disabledTextRGB
                        in -> 0xFF0F0F1A, // backgroundRGB
                        true, //rounded
                        true, //swing
                        true, //boarder
                        in -> 0xFF9B59B6,
                        in -> 0xFF8E44AD,
                        in -> 0xFF555566
                        ),

        Blue( // name
                        true, false, true, true, // showGradientEnabled - showGradientDisabled - useCustomFont -
                        CNColor.STATIC, // just leave this
                        in -> 0xFFFFFFFE, // categoryNameRGB
                        in -> 0xFF1A1A3A, // settingBackgroundRGB
                        in -> 0xFF1A1A3A, // categoryBackgroundRGB
                        in -> 0xFF3498DB, // enabledTopRGB
                        in -> 0xFF2980B9, // enabledBottomRGB
                        in -> 0xFFFFFFFF, // enabledTextRGB
                        in -> 0xFF2A2A4A, // disabledTopRGB
                        in -> 0xFF2A2A4A, // disabledBottomRGB
                        in -> 0xFFCCCCCC, // disabledTextRGB
                        in -> 0xFF0F0F2A, // backgroundRGB
                        true, //rounded
                        true, //swing
                        true, //boarder
                        in -> 0xFF3498DB,
                        in -> 0xFF2980B9,
                        in -> 0xFF555577
                        ),

        Green( // name
                        true, false, true, true, // showGradientEnabled - showGradientDisabled - useCustomFont -
                        CNColor.STATIC, // just leave this
                        in -> 0xFFFFFFFE, // categoryNameRGB
                        in -> 0xFF1A2A1A, // settingBackgroundRGB
                        in -> 0xFF1A2A1A, // categoryBackgroundRGB
                        in -> 0xFF27AE60, // enabledTopRGB
                        in -> 0xFF229954, // enabledBottomRGB
                        in -> 0xFFFFFFFF, // enabledTextRGB
                        in -> 0xFF2A3A2A, // disabledTopRGB
                        in -> 0xFF2A3A2A, // disabledBottomRGB
                        in -> 0xFFCCCCCC, // disabledTextRGB
                        in -> 0xFF0F2A0F, // backgroundRGB
                        true, //rounded
                        true, //swing
                        true, //boarder
                        in -> 0xFF27AE60,
                        in -> 0xFF229954,
                        in -> 0xFF556655
                        ),

        Red( // name
                        true, false, true, true, // showGradientEnabled - showGradientDisabled - useCustomFont -
                        CNColor.STATIC, // just leave this
                        in -> 0xFFFFFFFE, // categoryNameRGB
                        in -> 0xFF2A1A1A, // settingBackgroundRGB
                        in -> 0xFF2A1A1A, // categoryBackgroundRGB
                        in -> 0xFFE74C3C, // enabledTopRGB
                        in -> 0xFFC0392B, // enabledBottomRGB
                        in -> 0xFFFFFFFF, // enabledTextRGB
                        in -> 0xFF3A2A2A, // disabledTopRGB
                        in -> 0xFF3A2A2A, // disabledBottomRGB
                        in -> 0xFFCCCCCC, // disabledTextRGB
                        in -> 0xFF2A0F0F, // backgroundRGB
                        true, //rounded
                        true, //swing
                        true, //boarder
                        in -> 0xFFE74C3C,
                        in -> 0xFFC0392B,
                        in -> 0xFF665555
                        ),

        Dark( // name
                        false, false, true, true, // showGradientEnabled - showGradientDisabled - useCustomFont -
                        CNColor.STATIC, // just leave this
                        in -> 0xFFFFFFFF, // categoryNameRGB
                        in -> 0xFF1E1E1E, // settingBackgroundRGB
                        in -> 0xFF252525, // categoryBackgroundRGB
                        in -> 0xFF424242, // enabledTopRGB
                        in -> 0xFF424242, // enabledBottomRGB
                        in -> 0xFFFFFFFF, // enabledTextRGB
                        in -> 0xFF2A2A2A, // disabledTopRGB
                        in -> 0xFF2A2A2A, // disabledBottomRGB
                        in -> 0xFFAAAAAA, // disabledTextRGB
                        in -> 0xFF121212, // backgroundRGB
                        true, //rounded
                        true, //swing
                        false, //boarder
                        in -> 0xFF424242,
                        in -> 0xFF424242,
                        in -> 0xFF555555
                        ),

        Rainbow( // name
                        true, true, true, true, // showGradientEnabled - showGradientDisabled - useCustomFont -
                        CNColor.RAINBOW, // rainbow colors
                        in -> Utils.Client.rainbowDraw(1, 0), // categoryNameRGB
                        in -> Utils.Client.rainbowDraw(2, 0), // settingBackgroundRGB
                        in -> Utils.Client.rainbowDraw(1, 0), // categoryBackgroundRGB
                        in -> Utils.Client.rainbowDraw(2, 0), // enabledTopRGB
                        in -> Utils.Client.rainbowDraw(2, 100), // enabledBottomRGB
                        in -> 0xFFFFFFFF, // enabledTextRGB
                        in -> Utils.Client.rainbowDraw(2, 200), // disabledTopRGB
                        in -> Utils.Client.rainbowDraw(2, 300), // disabledBottomRGB
                        in -> 0xFFCCCCCC, // disabledTextRGB
                        in -> 0xFF1A1A1A, // backgroundRGB
                        true, //rounded
                        true, //swing
                        true, //boarder
                        in -> Utils.Client.rainbowDraw(1, 0),
                        in -> Utils.Client.rainbowDraw(1, 50),
                        in -> Utils.Client.rainbowDraw(1, 100)
                        ),

        Neon( // name
                        true, true, true, true, // showGradientEnabled - showGradientDisabled - useCustomFont -
                        CNColor.RAINBOW, // rainbow colors
                        in -> 0xFF00FFFF, // categoryNameRGB (cyan)
                        in -> 0xFF1A0033, // settingBackgroundRGB (dark purple)
                        in -> 0xFF1A0033, // categoryBackgroundRGB
                        in -> 0xFFFF00FF, // enabledTopRGB (magenta)
                        in -> 0xFF00FFFF, // enabledBottomRGB (cyan)
                        in -> 0xFFFFFFFF, // enabledTextRGB
                        in -> 0xFF330066, // disabledTopRGB
                        in -> 0xFF330066, // disabledBottomRGB
                        in -> 0xFFAAAAFF, // disabledTextRGB
                        in -> 0xFF0A001A, // backgroundRGB
                        true, //rounded
                        true, //swing
                        true, //boarder
                        in -> 0xFFFF00FF,
                        in -> 0xFF00FFFF,
                        in -> Utils.Client.rainbowDraw(1, 0)
                        );

        public boolean showGradientEnabled, showGradientDisabled, useCustomFont, categoryBackground, roundedCorners, swing, boarder;
        public ColorM categoryNameRGB, settingBackgroundRGB, categoryBackgroundRGB, enabledTopRGB, enabledBottomRGB,
        enabledTextRGB, disabledTopRGB, disabledBottomRGB, disabledTextRGB, backgroundRGB, boarderColor, categoryOutlineColor, categoryOutlineColor2;
        public CNColor cnColor;

        private Preset(
                        boolean showGradientEnabled, boolean showGradientDisabled, boolean useCustomFont,
                        boolean categoryBackground, CNColor cnColor, ColorM categoryNameRGB, ColorM settingBackgroundRGB,
                        ColorM categoryBackgroundRGB, ColorM enabledTopRGB, ColorM enabledBottomRGB, ColorM enabledTextRGB,
                        ColorM disabledTopRGB, ColorM disabledBottomRGB, ColorM disabledTextRGB, ColorM backgroundRGB,
                        boolean roundedCorners, boolean swing, boolean boarder, ColorM boarderColor, ColorM categoryOutlineColor, ColorM categoryOutlineColor2) {
            this.showGradientEnabled = showGradientEnabled;
            this.showGradientDisabled = showGradientDisabled;
            this.useCustomFont = useCustomFont;
            this.categoryBackground = categoryBackground;
            this.categoryNameRGB = categoryNameRGB;
            this.settingBackgroundRGB = settingBackgroundRGB;
            this.categoryBackgroundRGB = categoryBackgroundRGB;
            this.enabledTopRGB = enabledTopRGB;
            this.enabledBottomRGB = enabledBottomRGB;
            this.enabledTextRGB = enabledTextRGB;
            this.disabledTopRGB = disabledTopRGB;
            this.disabledBottomRGB = disabledBottomRGB;
            this.disabledTextRGB = disabledTextRGB;
            this.backgroundRGB = backgroundRGB;
            this.cnColor = cnColor;
            this.roundedCorners = roundedCorners;
            this.swing = swing;
            this.boarder = boarder;
            this.boarderColor = boarderColor;
            this.categoryOutlineColor = categoryOutlineColor;
            this.categoryOutlineColor2 = categoryOutlineColor2;
        }

    }



    public enum CNColor {
        RAINBOW, STATIC
    }
}
