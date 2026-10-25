# Pablo Client: mission and roadmap

_Last updated at the end of Phase 1. Whoever finishes a phase updates the status table and the "Next action" section below._

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
| 2 | HUD widget system | **Next** |
| 3 | Widgets | Planned |
| 4 | Notifications | Planned |
| 5 | Menus | Planned |
| 6 | Polish (themes, font, icons, presets) | Planned |
| 7 | Release | Planned |

## Next action: Phase 2

Build the HUD widget system on top of the Phase 1 toolkit (`docs/UI_TOOLKIT.md`).

Deliverables:
- `ui/hud/Widget`: base class with id, position, anchor (corner/edge + offset so it survives window resizes), scale, opacity, enabled flag, `measure()` and `render(partialTicks)`.
- `ui/hud/WidgetManager`: registry, one render pass per frame on `Render2DEvent`, skipping disabled widgets, drawing inside a single `FrameCost.begin()/end()`.
- Persistence: save/load widget positions and settings to the existing config system (`ConfigManager` / `ClientConfig`). Unknown or missing fields fall back to defaults.
- Drag-and-drop editor screen with snapping to edges/centre and other widgets; opened by a command and from the menu.
- A `.perf` command (and optional small overlay) showing `FrameCost.avgMs()`, FPS and `Gfx.drawCallsLastFrame()`.
- Migrate the existing logo, watermark and module list out of `module/modules/HUD.java` (about 620 lines) into widgets without changing their behaviour.

Acceptance:
- Widgets can be moved, enabled and disabled; positions survive a restart and a window resize.
- All old HUD features still work.
- No per-frame allocation in widget render code.
- The owner reports the client frame time and FPS with the HUD on and off (same world, same spot).

## Phases in detail

0. **Design language.** Dark-first iOS palette (`ui/gfx/Colors`), radii around 12 to 18 for cards, spacing on a 4/8 grid, spring timings, Inter font (open licence), open icon set. Done as an HTML mockup first so look decisions are cheap.
1. **Rendering toolkit.** SDF-shader rounded rectangles with gradient, outline and soft shadow; low-resolution frosted-glass blur; spring/tween animation; clipping; text helpers; quality presets; demo screen (`uidemo`). Done.
2. **HUD widget system.** See "Next action".
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

## Future expansion(unconfirmed)
1. Quality of life features: Keystrokes, Zoom, Music Player(Sigmaclient Inspo), Visuals.
2. Further simple features: HUD Skidding, Render improvements. 
3. Client-Bypasses and new feats.
