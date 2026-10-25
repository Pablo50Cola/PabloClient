# Pablo Client: mission and roadmap

_Last updated after the Phase 2 code was written (awaiting the owner's build and test). Whoever finishes a phase updates the status table and the "Next action" section below._

## Mission

Turn Pablo Client into a **full Minecraft 1.8.9 client in the vein of Feather and Badlion, with an iOS-style interface**: rounded cards, frosted glass, smooth spring motion, grouped settings lists. HUD and visual elements come first; the module set inherited from RavenB++ stays working underneath.

Three things matter, in this order:
1. **Performance.** The client must feel lighter than it looks. See the performance rules.
2. **Look and feel.** Consistent iOS design language everywhere (HUD, menus, notifications).
3. **Stability.** It must always build and launch on Forge 1.8.9 with JDK 8. Never leave `main` in a state that does not launch.

**Non-goals:** copying Apple assets (no SF Pro, no SF Symbols; use Inter and an open icon set such as Lucide or Phosphor), telemetry or data collection, remote code or config execution, and any feature meant to evade server anti-cheat or hide the client from servers or staff.

## Status

| Phase | What | Status |
|---|---|---|
| 0 | Design language and HTML mockup | **Done**, approved by the owner (mockup: dark iOS look, glass cards, Dynamic-Island-style pill) |
| 1 | Rendering toolkit | **Done**, verified in game by the owner (blur, shadows, springs, components; 0.35 to 0.52 ms CPU) |
| 2 | HUD widget system | **Builds and runs** (owner screenshot: watermark, keybinds and `perf` overlay correct, 0.19 ms client frame time, 60 FPS). Branch `phase2/hud-widgets`, jar `PabloClient-1.3.0-phase2.jar`. Still to confirm: editor, saved positions after restart and resize |
| 3 | Widgets | Planned |
| 4 | Notifications | Planned |
| 5 | Menus | Planned |
| 6 | Polish (themes, font, icons, presets) | Planned |
| 7 | Release | Planned |

## Next action: verify Phase 2, then Phase 3

Phase 2 is on branch `phase2/hud-widgets`. The owner built it and it launches; the HUD widgets and the perf overlay were seen working (0.19 ms, 60 FPS). Remaining checks:

1. ~~Build and launch~~ done.
2. The owner launches the game and checks, in the same world and spot:
   - the HUD module on: module list, watermark and keybinds box look and sit where they did before;
   - `hudedit` opens the editor; widgets can be dragged (snapping), switched on/off, scaled and faded; Esc closes it;
   - positions survive restarting the game and resizing the window;
   - `perf` prints the numbers, `perf overlay` shows them live;
   - client frame time and FPS with the HUD off and on (screenshot of the overlay is enough).
3. After the owner confirms, merge into `main` through a pull request, mark Phase 2 done here and start **Phase 3 (widgets)**: FPS, CPS, ping, coordinates, clock, armor and potion status, keystrokes, and real widgets for the Target HUD and the Radar (replacing `LegacyBoxWidget`).

What Phase 2 contains (see `docs/UI_TOOLKIT.md`, section "HUD widget system"):
- `ui/hud/Widget`, `Anchor`, `WidgetManager`: anchor + offset positions that survive window resizes, scale, opacity, one render pass per frame inside one `FrameCost` pair.
- Saved in `clientconfig.kv` under `"widgets"`; missing or damaged fields fall back to defaults; the old positions are carried over once.
- `HudEditorScreen`: drag and drop with snapping, selection panel, mouse wheel scale, arrow-key nudging. Opened by `hudedit`, the HUD module's "Edit position" switch, or the HUDEditor module.
- `perf` command and a `PerfWidget` overlay (off by default).
- The old HUD module no longer draws anything itself: module list, watermark and keybinds are widgets that use the module's settings and still only show while it is on. Target HUD and Radar still draw themselves (and keep their saved position in `hudpos.json`); the editor can move them.

Acceptance (owner reports): widgets can be moved, enabled and disabled; positions survive a restart and a window resize; all old HUD features still work; client frame time and FPS with the HUD on and off.

Known gaps carried into later phases:
- The old HUD "Logo" setting did nothing even before Phase 2 (the logo image was never loaded), so no logo widget exists. The settings are still there, untouched.
- The module list keeps the old settings (colour mode, shadow, background); they move into per-widget settings in Phase 5 when the old GUIs go.

## Phases in detail

0. **Design language.** Dark-first iOS palette (`ui/gfx/Colors`), radii around 12 to 18 for cards, spacing on a 4/8 grid, spring timings, Inter font (open licence), open icon set. Done as an HTML mockup first so look decisions are cheap.
1. **Rendering toolkit.** SDF-shader rounded rectangles with gradient, outline and soft shadow; low-resolution frosted-glass blur; spring/tween animation; clipping; text helpers; quality presets; demo screen (`uidemo`). Done.
2. **HUD widget system.** Anchored widgets, manager, saved layout, drag-and-drop editor, `perf` command, old HUD migrated. Code written; awaiting the owner's test.
3. **Widgets.** FPS, CPS, ping, coordinates, clock, armor and potion status, keystrokes, module-list pill. Each is a `Widget` subclass with its own settings, text cached until the value changes.
4. **Notifications.** iOS-style banners and a Dynamic-Island-style pill for module toggles, queued and animated with springs.
5. **Menus.** Settings-app-style ClickGUI (grouped lists, animated switches, sliders, segmented controls), restyled main menu, and removal of the two old GUIs (`clickgui/raven`, `clickgui/kv`) once the new one covers everything.
6. **Polish.** Light/dark theme, accent colour, Inter font, icons, performance pass and Low/Medium/High presets.
7. **Release.** Version bump, GitHub release with the jar attached, working update check (`VersionManager`, `UpdateCheck`).

## Performance rules (apply to every phase)

1. **Budget:** the whole client UI must cost well under 1 ms of frame time on a mid-range PC with all widgets on. Every PR states FPS with the client on and off (same world, same spot).
2. **No per-frame allocation:** no `new`, string concatenation or formatting in render code. Cache formatted text and rebuild it only when the value changes. Allocation causes garbage-collection stutter in 1.8.9.
3. **Compile shaders once**, cache uniform locations, batch draw calls, minimise GL state changes.
4. **Blur is the expensive part:** render it at reduced resolution, once per frame, only when a blurred element is visible. Menus may use stronger blur than the in-game HUD. Quality setting: Off / Low / High.
5. **Pay only for what is visible:** disabled widgets cost nothing, off-screen widgets are skipped, animations only run while something is moving.
6. **Cache static content:** text widths, font atlases and rarely changing widgets are drawn from cached data.
7. **Graceful fallback:** if a shader fails to compile (old GPU), fall back to the simple renderer instead of crashing.
8. **Measure:** the client's own render time per frame is always visible (`FrameCost`, and the `.perf` command from Phase 2).
9. **Quality presets:** Low / Medium / High (blur, shadows, animation detail) so weaker PCs can still use the client.

## Working method

- The cloud environment that writes most of the code cannot reach the Forge/Mojang hosts, so **code is written but unbuilt there.** The owner builds each branch locally (JDK 8, `gradlew clean build`) and reports: the build output, `latest.log` on a crash, screenshots, and FPS numbers.
- Work on a branch per phase (`phaseN/<topic>`), merge into `main` only after the owner confirmed it builds and launches. Never merge unverified changes into `main`.
- The owner is not an experienced programmer: instructions to him must be step by step (what to click, what to type, what to send back). He downloads branch ZIPs rather than using git.
- Build jars are named `PabloClient-<version>.jar`; the version changes per phase so an old jar cannot be mistaken for a new one. Make sure only one jar is in the `mods` folder.
- Update this file, `docs/UI_TOOLKIT.md` and `CLAUDE.md` whenever a phase finishes or a decision changes.

## Decisions already made

- Fork base: RavenB++ (GPL-3.0). Licence stays GPL-3.0; credits stay (see `CREDITS.md`).
- Mixin stays on **0.7.11-SNAPSHOT** (0.6 is too old, 0.8.x needs ASM 6+ which 1.8.9 lacks).
- Package names (`keystrokesmod.*`) and the mod id `keystrokesmod` stay as they are, so configs and mixins keep working. Only user-visible names say "Pablo Client".
- Dark theme first, light theme in Phase 6.
- Rendering uses GLSL 1.20 shaders with a fallback path, not a new rendering library.
