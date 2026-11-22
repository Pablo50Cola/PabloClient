# Pablo Client: mission and roadmap

_Last updated after the Phase 2 code was written (awaiting the owner's build and test). Whoever finishes a phase updates the status table and the "Next action" section below._

## Mission

Turn Pablo Client into a **full Minecraft 1.8.9 client in the vein of Feather and Badlion, with an iOS-style interface**: rounded cards, frosted glass, smooth spring motion, grouped settings lists. HUD and visual elements come first; the module set inherited from RavenB++ stays working underneath.

Three things matter, in this order:
1. **Performance.** The client must feel lighter than it looks. See the performance rules.
2. **Look and feel.** Consistent iOS design language everywhere (HUD, menus, notifications).
3. **Stability.** It must always build and launch on Forge 1.8.9 with JDK 8. Never leave `main` in a state that does not launch.

**Non-goals:** copying Apple assets (no SF Pro, no SF Symbols; use Inter and an open icon set such as Lucide or Phosphor), telemetry or data collection, remote code or config execution.

## Status

| Phase | What | Status |
|---|---|---|
| 0 | Design language and HTML mockup | **Done**, approved by the owner (mockup: dark iOS look, glass cards, Dynamic-Island-style pill) |
| 1 | Rendering toolkit | **Done**, verified in game by the owner (blur, shadows, springs, components; 0.35 to 0.52 ms CPU) |
| 2 | HUD widget system | **Builds and runs** (owner screenshot: watermark, keybinds and `perf` overlay correct, 0.19 ms client frame time, 60 FPS). Branch `phase2/hud-widgets`, jar `PabloClient-1.3.0-phase2.jar`. Still to confirm: editor, saved positions after restart and resize |
| 3 | Widgets | **Done except the owner's test of 3c.** Branch `phase3/widgets`, jar `PabloClient-1.3.0-phase3c.jar` |
| 4 | Notifications | **Written, not yet built or tested.** Branch `phase4/notifications` (built on `phase3/widgets`), jar `PabloClient-1.4.2-phase4.jar` |
| 5 | Menus | Planned |
| 6 | Polish (themes, font, icons, presets) | Planned |
| 7 | Release | Planned |

## Next action: verify Phase 3c and Phase 4

Phase 3c (branch `phase3/widgets`) and Phase 4 (branch `phase4/notifications`, which contains 3c) are written but not yet built or run. Test the Phase 4 jar `PabloClient-1.4.2-phase4.jar`, it covers both.

Phase 4 (iOS-style notifications), reworking the old `notifications` package; banners always work; the Gui module tick "Toggle island" (default on) only controls the island:
- Island: a black pill at the top centre when a module is switched on or off. Grows out of a dot on a springy width, holds about 2 s, shrinks away.
- Banners: rounded cards sliding in from the right on a spring, stacked top right, up to 4 at once, rest queued. Types: info, success, warning, error (coloured dot).
- API: `NotificationManager.info/success/warning/error(title, message)`, `island(name, on)`. Command `notify [info|success|warning|error] <text>` (no arguments = demo).

Owner test: build, type `notify` (demo of all), toggle a module with its key and watch the island. Check client frame time with `perf overlay` while a notification shows.

Phase 3c test: Radar and Target HUD in `hudedit` (movable, scalable, show where they were before), Background switch and colour swatches on pills.

Open items:
- Phase 2 report "some modules stayed where I left them, others moved" is still not understood.
- Phases 2, 3, 4 are not merged into `main`; merge after the owner confirms.
- Banners are only used by the `notify` command so far; real events (config loaded, update available, ...) come with the menus in Phase 5.
- Opacity does not apply to Target HUD and Radar; module list, watermark and keybinds keep their old module settings until Phase 5.

## Phases in detail

0. **Design language.** Dark-first iOS palette (`ui/gfx/Colors`), radii around 12 to 18 for cards, spacing on a 4/8 grid, spring timings, Inter font (open licence), open icon set. Done as an HTML mockup first so look decisions are cheap.
1. **Rendering toolkit.** SDF-shader rounded rectangles with gradient, outline and soft shadow; low-resolution frosted-glass blur; spring/tween animation; clipping; text helpers; quality presets; demo screen (`uidemo`). Done.
2. **HUD widget system.** Anchored widgets, manager, saved layout, drag-and-drop editor, `perf` command, old HUD migrated. Built and running; one position report open.
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
