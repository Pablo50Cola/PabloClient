package keystrokesmod.client.ui.hud;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import keystrokesmod.client.module.modules.HUD;
import keystrokesmod.client.ui.components.UiSwitch;
import keystrokesmod.client.ui.gfx.Colors;
import keystrokesmod.client.ui.gfx.Gfx;
import keystrokesmod.client.ui.gfx.UiText;
import keystrokesmod.client.ui.hud.widgets.LegacyBoxWidget;
import net.minecraft.client.gui.GuiScreen;

/**
 * Drag-and-drop editor for the HUD widgets (Phase 2). Open it with the {@code hudedit} command, the
 * "Edit position" switch of the HUD module or the HUDEditor module.
 *
 * <ul>
 * <li>Drag a widget to move it. It snaps to the screen edges and centre and to other widgets; hold
 * Shift to move freely. Arrow keys nudge the selected widget by one unit.</li>
 * <li>Disabled widgets show as dashed-looking outlines so they can still be found and switched on.</li>
 * <li>The panel at the bottom switches the selected widget on or off and changes its scale and
 * opacity (mouse wheel changes the scale too).</li>
 * </ul>
 *
 * Everything is saved when the screen closes (and after each drop), so positions survive a restart.
 */
public class HudEditorScreen extends GuiScreen {
    private static final float SNAP = 5f;
    private static final float PANEL_W = 330f;
    private static final float PANEL_H = 104f;
    private static final float PAD = 14f;
    private static final float BTN_H = 20f;
    private static final float STEP_W = 20f;

    private Widget selected;
    private Widget dragging;
    private float grabDX;
    private float grabDY;
    private boolean moved;

    private boolean guideV;
    private boolean guideH;
    private float guideX;
    private float guideY;

    private UiSwitch enableSwitch = new UiSwitch(false);

    // cached strings: rebuilt only when the number changes
    private int scaleKey = -1;
    private String scaleText = "";
    private int opacityKey = -1;
    private String opacityText = "";

    // panel geometry, recomputed every frame
    private float panelX;
    private float panelY;

    // snapping scratch values
    private float bestDelta;
    private float bestLine;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        WidgetManager.applyPendingMigration();
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        Widget[] all = WidgetManager.all();
        for (int i = 0; i < all.length; i++) all[i].setEditing(false);
        persist();
    }

    private void persist() {
        HUD.savePositions();
        WidgetManager.save();
    }

    private void select(Widget w) {
        selected = w;
        if (w != null) enableSwitch = new UiSwitch(w.isEnabled());
    }

    // ------------------------------------------------------------------ drawing

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float sw = this.width;
        float sh = this.height;
        panelX = (sw - PANEL_W) / 2f;
        panelY = sh - PANEL_H - 12f;

        Gfx.roundRect(0f, 0f, sw, sh, 0f, 0x99000000);
        // centre lines, always faint
        Gfx.roundRect(sw / 2f, 0f, 1f, sh, 0f, 0x1AFFFFFF);
        Gfx.roundRect(0f, sh / 2f, sw, 1f, 0f, 0x1AFFFFFF);

        Widget[] all = WidgetManager.all();
        for (int i = 0; i < all.length; i++) {
            Widget w = all[i];
            if (w.isLegacyPositioned()) {
                w.measure();
                LegacyBoxWidget lw = (LegacyBoxWidget) w;
                float lx = lw.legacyX();
                float ly = lw.legacyY();
                float maxX = sw - w.scaledWidth();
                float maxY = sh - w.scaledHeight();
                w.placeAt(lx > maxX ? Math.max(0f, maxX) : Math.max(0f, lx),
                        ly > maxY ? Math.max(0f, maxY) : Math.max(0f, ly));
            } else {
                w.setEditing(true);
                w.measure();
                if (w != dragging) w.layout(sw, sh);
            }
        }

        for (int i = 0; i < all.length; i++) {
            Widget w = all[i];
            if (w.width <= 0f || w.height <= 0f) continue;
            boolean live = w.isLegacyPositioned() || w.isEnabled();
            float x = w.screenX();
            float y = w.screenY();
            float ww = w.scaledWidth();
            float hh = w.scaledHeight();
            if (live) {
                WidgetManager.draw(w, partialTicks);
            } else {
                Gfx.outlined(x, y, ww, hh, 6f, 1f, 0x22FFFFFF, 0x77FFFFFF);
                UiText.drawCentered(w.title, x + ww / 2f, y + (hh - UiText.height()) / 2f, Colors.LABEL_SECONDARY);
            }
            if (w == selected) {
                Gfx.outlined(x - 2f, y - 2f, ww + 4f, hh + 4f, 6f, 1.5f, 0x00000000, Colors.ACCENT);
            } else if (w.contains(mouseX, mouseY) && dragging == null) {
                Gfx.outlined(x - 2f, y - 2f, ww + 4f, hh + 4f, 6f, 1f, 0x00000000, 0x88FFFFFF);
            }
        }

        if (dragging != null) {
            if (guideV) Gfx.roundRect(guideX, 0f, 1f, sh, 0f, Colors.ORANGE);
            if (guideH) Gfx.roundRect(0f, guideY, sw, 1f, 0f, Colors.ORANGE);
        }

        drawPanel(mouseX, mouseY);
        UiText.drawCentered("Drag widgets to move them. Hold Shift to move freely. Esc to finish.", sw / 2f, 8f,
                Colors.LABEL_SECONDARY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawPanel(int mouseX, int mouseY) {
        Gfx.shadow(panelX, panelY, PANEL_W, PANEL_H, 16f, 22f, 10f, 0x77000000);
        Gfx.roundRect(panelX, panelY, PANEL_W, PANEL_H, 16f, Colors.SURFACE);

        Widget w = selected;
        float rowY = panelY + 44f;
        if (w == null) {
            UiText.draw("No widget selected", panelX + PAD, panelY + 14f, Colors.LABEL);
            UiText.draw("Click a widget to edit it.", panelX + PAD, panelY + 28f, Colors.LABEL_SECONDARY);
        } else {
            UiText.draw(w.title, panelX + PAD, panelY + 11f, Colors.LABEL);
            String hint = w.isLegacyPositioned() ? "Position only. Everything else is set in its module."
                    : w.hint();
            if (hint != null) UiText.draw(hint, panelX + PAD, panelY + 25f, Colors.LABEL_SECONDARY);

            if (!w.isLegacyPositioned()) {
                enableSwitch.draw(panelX + PANEL_W - PAD - UiSwitch.WIDTH, panelY + 10f);

                int sp = (int) (w.scale() * 100f + 0.5f);
                if (sp != scaleKey) {
                    scaleKey = sp;
                    scaleText = Integer.toString(sp) + "%";
                }
                int op = (int) (w.opacity() * 100f + 0.5f);
                if (op != opacityKey) {
                    opacityKey = op;
                    opacityText = Integer.toString(op) + "%";
                }
                float ty = rowY + (BTN_H - UiText.height()) / 2f;
                UiText.draw("Scale", panelX + PAD, ty, Colors.LABEL_SECONDARY);
                stepper(panelX + 62f, rowY, scaleText, mouseX, mouseY);
                UiText.draw("Opacity", panelX + 176f, ty, Colors.LABEL_SECONDARY);
                stepper(panelX + 232f, rowY, opacityText, mouseX, mouseY);
            }
        }

        float by = panelY + PANEL_H - PAD - BTN_H + 2f;
        button(resetX(), by, 70f, "Reset", w != null, mouseX, mouseY);
        button(resetAllX(), by, 82f, "Reset all", true, mouseX, mouseY);
        button(doneX(), by, 70f, "Done", true, mouseX, mouseY);
    }

    private float resetX() {
        return panelX + PAD;
    }

    private float resetAllX() {
        return panelX + PAD + 78f;
    }

    private float doneX() {
        return panelX + PANEL_W - PAD - 70f;
    }

    /** "-  value  +" control. Layout: minus button, 52 wide value, plus button. */
    private void stepper(float x, float y, String value, int mouseX, int mouseY) {
        boolean hoverMinus = inside(mouseX, mouseY, x, y, STEP_W, BTN_H);
        boolean hoverPlus = inside(mouseX, mouseY, x + STEP_W + 52f, y, STEP_W, BTN_H);
        Gfx.roundRect(x, y, STEP_W, BTN_H, 10f, hoverMinus ? 0xFF3A3A3C : Colors.ELEVATED);
        Gfx.roundRect(x + STEP_W + 52f, y, STEP_W, BTN_H, 10f, hoverPlus ? 0xFF3A3A3C : Colors.ELEVATED);
        float ty = y + (BTN_H - UiText.height()) / 2f;
        UiText.drawCentered("-", x + STEP_W / 2f, ty, Colors.LABEL);
        UiText.drawCentered("+", x + STEP_W + 52f + STEP_W / 2f, ty, Colors.LABEL);
        UiText.drawCentered(value, x + STEP_W + 26f, ty, Colors.LABEL);
    }

    private void button(float x, float y, float w, String label, boolean active, int mouseX, int mouseY) {
        boolean hover = active && inside(mouseX, mouseY, x, y, w, BTN_H);
        Gfx.roundRect(x, y, w, BTN_H, 10f, hover ? 0xFF3A3A3C : Colors.ELEVATED);
        UiText.drawCentered(label, x + w / 2f, y + (BTN_H - UiText.height()) / 2f,
                active ? Colors.LABEL : Colors.LABEL_SECONDARY);
    }

    private static boolean inside(float mx, float my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    // ------------------------------------------------------------------ input

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (inside(mouseX, mouseY, panelX, panelY, PANEL_W, PANEL_H)) {
            clickPanel(mouseX, mouseY);
            return;
        }

        Widget[] all = WidgetManager.all();
        for (int i = all.length - 1; i >= 0; i--) {
            Widget w = all[i];
            if (w.width <= 0f || w.height <= 0f) continue;
            if (w.contains(mouseX, mouseY)) {
                if (w != selected) select(w);
                dragging = w;
                grabDX = mouseX - w.screenX();
                grabDY = mouseY - w.screenY();
                moved = false;
                return;
            }
        }
        selected = null;
    }

    private void clickPanel(int mouseX, int mouseY) {
        Widget w = selected;
        float by = panelY + PANEL_H - PAD - BTN_H + 2f;

        if (inside(mouseX, mouseY, doneX(), by, 70f, BTN_H)) {
            mc.displayGuiScreen(null);
            return;
        }
        if (inside(mouseX, mouseY, resetAllX(), by, 82f, BTN_H)) {
            WidgetManager.resetAll();
            if (selected != null) select(selected);
            persist();
            return;
        }
        if (w == null) return;

        if (inside(mouseX, mouseY, resetX(), by, 70f, BTN_H)) {
            if (w instanceof LegacyBoxWidget) {
                ((LegacyBoxWidget) w).resetLegacyPosition();
            } else {
                w.resetToDefaults();
            }
            select(w);
            persist();
            return;
        }
        if (w.isLegacyPositioned()) return;

        if (enableSwitch.hit(panelX + PANEL_W - PAD - UiSwitch.WIDTH, panelY + 10f, mouseX, mouseY)) {
            enableSwitch.toggle();
            w.setEnabled(enableSwitch.isOn());
            persist();
            return;
        }

        float rowY = panelY + 44f;
        if (inside(mouseX, mouseY, panelX + 62f, rowY, STEP_W, BTN_H)) {
            w.setScale(roundTo(w.scale() - 0.1f, 0.05f));
            persist();
        } else if (inside(mouseX, mouseY, panelX + 62f + STEP_W + 52f, rowY, STEP_W, BTN_H)) {
            w.setScale(roundTo(w.scale() + 0.1f, 0.05f));
            persist();
        } else if (inside(mouseX, mouseY, panelX + 232f, rowY, STEP_W, BTN_H)) {
            w.setOpacity(roundTo(w.opacity() - 0.1f, 0.05f));
            persist();
        } else if (inside(mouseX, mouseY, panelX + 232f + STEP_W + 52f, rowY, STEP_W, BTN_H)) {
            w.setOpacity(roundTo(w.opacity() + 0.1f, 0.05f));
            persist();
        }
    }

    private static float roundTo(float v, float step) {
        return Math.round(v / step) * step;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging == null || clickedMouseButton != 0) return;
        Widget w = dragging;
        float sw = this.width;
        float sh = this.height;
        float ww = w.scaledWidth();
        float hh = w.scaledHeight();

        float x = mouseX - grabDX;
        float y = mouseY - grabDY;

        guideV = false;
        guideH = false;
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            float nx = snapAxis(w, x, ww, sw, true);
            float ny = snapAxis(w, y, hh, sh, false);
            x = nx;
            y = ny;
        }

        float maxX = sw - ww;
        float maxY = sh - hh;
        x = x > maxX ? Math.max(0f, maxX) : Math.max(0f, x);
        y = y > maxY ? Math.max(0f, maxY) : Math.max(0f, y);
        moveWidget(w, x, y);
        moved = true;
    }

    private void moveWidget(Widget w, float x, float y) {
        if (w instanceof LegacyBoxWidget) {
            ((LegacyBoxWidget) w).setLegacy(Math.round(x), Math.round(y));
            w.placeAt(x, y);
        } else {
            w.moveTo(x, y, this.width, this.height);
        }
    }

    /**
     * Snaps one axis. The widget's start, centre and end are compared with the screen's start,
     * centre and end and with the same three lines of every other widget; the closest line within
     * {@link #SNAP} units wins. Sets the guide line when something snapped.
     */
    private float snapAxis(Widget self, float pos, float size, float screen, boolean horizontal) {
        bestDelta = SNAP + 1f;
        bestLine = 0f;

        float half = size / 2f;
        // screen lines
        consider(0f, pos, half, size);
        consider(screen / 2f, pos, half, size);
        consider(screen, pos, half, size);

        Widget[] all = WidgetManager.all();
        for (int i = 0; i < all.length; i++) {
            Widget o = all[i];
            if (o == self || o.width <= 0f || o.height <= 0f) continue;
            float start = horizontal ? o.screenX() : o.screenY();
            float len = horizontal ? o.scaledWidth() : o.scaledHeight();
            consider(start, pos, half, size);
            consider(start + len / 2f, pos, half, size);
            consider(start + len, pos, half, size);
        }

        if (Math.abs(bestDelta) > SNAP) return pos;
        if (horizontal) {
            guideV = true;
            guideX = bestLine;
        } else {
            guideH = true;
            guideY = bestLine;
        }
        return pos + bestDelta;
    }

    /** Tests one target line against the widget's start, centre and end. */
    private void consider(float line, float pos, float half, float size) {
        test(line, line - pos);
        test(line, line - (pos + half));
        test(line, line - (pos + size));
    }

    private void test(float line, float delta) {
        if (Math.abs(delta) < Math.abs(bestDelta)) {
            bestDelta = delta;
            bestLine = line;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (state == 0 && dragging != null) {
            dragging = null;
            guideV = false;
            guideH = false;
            if (moved) persist();
            moved = false;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && selected != null && !selected.isLegacyPositioned()) {
            selected.setScale(roundTo(selected.scale() + (wheel > 0 ? 0.05f : -0.05f), 0.05f));
            persist();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        Widget w = selected;
        if (w != null && dragging == null) {
            int dx = keyCode == Keyboard.KEY_LEFT ? -1 : (keyCode == Keyboard.KEY_RIGHT ? 1 : 0);
            int dy = keyCode == Keyboard.KEY_UP ? -1 : (keyCode == Keyboard.KEY_DOWN ? 1 : 0);
            if (dx != 0 || dy != 0) {
                float x = Math.max(0f, Math.min(this.width - w.scaledWidth(), w.screenX() + dx));
                float y = Math.max(0f, Math.min(this.height - w.scaledHeight(), w.screenY() + dy));
                moveWidget(w, x, y);
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }
}
