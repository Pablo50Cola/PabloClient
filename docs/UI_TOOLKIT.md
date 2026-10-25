# UI toolkit (Phase 1) and HUD widget system (Phase 2)

Everything lives in `src/main/java/keystrokesmod/client/ui/`. Sizes are in **GUI units** (the scaled coordinates Minecraft's GuiScreen uses), not pixels. All drawing goes through `Gfx`.

## Map

| File | Purpose |
|---|---|
| `UiSettings` | Global switches: `quality` (OFF / LOW / HIGH), `animations`, `shadows`, `blurStrength`. Read by the rest of the toolkit. |
| `UiEvents` | Forge event hook. Calls `Gfx.newFrame()` at the start of every render tick (resets the once-per-frame blur capture). Registered in `Raven.init()`. |
| `gfx/Gfx` | The public drawing API (below). |
| `gfx/Shaders`, `gfx/ShaderProgram` | GLSL 1.20 sources and a compile/link/uniform wrapper. Shaders are compiled once, lazily. If anything fails the toolkit marks itself unavailable and falls back. |
| `gfx/BlurBuffer` | Captures the screen into a small FBO (1/2 or 1/4 resolution), blurs it with a separable gaussian, at most once per frame, only when a glass element is drawn. |
| `gfx/Colors` | iOS dark palette constants and colour helpers (`argb`, `withAlpha`, `lerp`, ...). |
| `gfx/UiText` | Text helpers (`draw`, `drawCentered`, `drawRight`, `width`, `height`) over the existing font. |
| `anim/Spring`, `anim/Tween`, `anim/Ease` | Motion. `Spring` is time-based and settles by itself; `Tween` plays a timed curve. Both respect `UiSettings.animations`. |
| `perf/FrameCost` | `begin()` / `end()` around UI drawing; `avgMs()` gives the client's own CPU cost per frame. |
| `components/UiSwitch`, `components/UiSegmented` | iOS switch and segmented control. |
| `demo/UiDemoScreen` + `command/commands/UiDemo` | The `uidemo` terminal command. Use it as a living example. |

## Drawing API (`Gfx`)

`roundRect`, `gradientV`, `gradientH`, `gradient4`, `outlined`, `circle`, `shadow`, `glass`, `scissorBegin` / `scissorEnd`, plus `drawCallsLastFrame()` for diagnostics. Colours are `0xAARRGGBB` ints.

## Rules for using it

1. No `new`, `String.format` or string concatenation in per-frame code. Cache text and rebuild it only when the value changes (see `UiDemoScreen` for the pattern).
2. Draw glass only for things that are visible. Each glass draw after the first costs almost nothing; the first one triggers the blur capture.
3. Wrap UI drawing in `FrameCost.begin()` / `FrameCost.end()` when you want it measured.
4. Never crash on a missing GPU feature: `Gfx` falls back to the old `RenderUtils` shapes and a flat translucent fill.

## Measured on the owner's PC (singleplayer, demo screen)

Client frame time (CPU side): Off 0.35 ms, Low about 0.40 ms, High 0.52 ms. FPS stayed the same across the settings. The owner's FPS is capped, so GPU cost is not visible in this number.

## Known gaps

- The font is still the old Gilroy one; Inter is planned (Phase 6).
- Icons are not implemented yet (plan: Lucide or Phosphor, never Apple's SF Symbols).
- No light theme yet.

# HUD widget system (Phase 2)

Everything lives in `src/main/java/keystrokesmod/client/ui/hud/`.

| File | Purpose |
|---|---|
| `Widget` | Base class. Has `id` (saved key, never rename), `title`, anchor + offset, `scale`, `opacity`, `enabled`. Subclasses implement `measure()` (sets `width`/`height`) and `render(partialTicks)` (draws at local 0,0; the manager already moved and scaled). Optional: `isVisible()`, `hint()`, `saveExtra/loadExtra/resetExtra` for own settings. |
| `Anchor` | Nine anchor points (corners, edges, centre). A widget stores its distance to the anchor, so it stays put when the window or GUI scale changes. |
| `WidgetManager` | Registry (`register`, `get`, `all`), one render pass per frame from `Render2DEvent` inside one `FrameCost.begin()/end()`, saving/loading (`toJson`/`fromJson`), `resetAll`. Skips disabled widgets before measuring them. |
| `HudEditorScreen` | Drag-and-drop editor. Snaps to screen edges/centre and other widgets (Shift = no snap), arrow keys nudge, mouse wheel scales, bottom panel: enable switch, scale, opacity, reset, reset all, done. |
| `widgets/ModuleListWidget`, `WatermarkWidget`, `KeybindsWidget` | The old HUD module's drawing, moved out. They read the HUD module's settings and only show while it is on. |
| `widgets/PerfWidget` | Frame cost / FPS / draw calls overlay. Off by default (`perf overlay`). |
| `widgets/LegacyBoxWidget` | Editor-only stand-in for Target HUD and Radar, which still draw themselves and keep their position in `hudpos.json`. Replaced by real widgets in Phase 3. |

Commands: `hudedit` (alias `widgets`) opens the editor; `perf` prints the numbers; `perf overlay [on|off]` toggles the overlay.

## Adding a widget

1. Create `ui/hud/widgets/MyWidget.java` extending `Widget`, call `super(id, title, defaultAnchor, offsetX, offsetY, enabledByDefault)`.
2. In `measure()` set `width`/`height`. In `render()` draw at (0, 0) with `Gfx` / `UiText`, wrap colours in `fade(...)`.
3. Cache text: rebuild a string only when its source value changed (see `PerfWidget`). No `new`, concatenation or `String.format` per frame.
4. If `editing` is true show sample content so the editor has something to draw.
5. Register it in `WidgetManager.init()`.

Widgets that draw `Gfx.glass(...)` need real screen coordinates: return `false` from `usesMatrixTransform()` and use `screenX()`, `screenY()`, `scale()`.

## Saved data

`clientconfig.kv` key `"widgets"`: `{"version":1,"items":{"<id>":{"enabled","anchor","offsetX","offsetY","scale","opacity", ...extra}}}`. Anything missing or damaged falls back to the widget's defaults. With no `"widgets"` key (first start after the update) the old HUD positions from `hudpos.json` are carried over once.
