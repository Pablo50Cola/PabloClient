# UI toolkit (Phase 1)

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
- There is no `.perf` command or overlay yet (Phase 2).
