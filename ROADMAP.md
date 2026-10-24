# RavenP roadmap: iOS-style client

Goal: turn RavenP into a full client (in the vein of Feather / Badlion) with an iOS-style interface: rounded cards, frosted glass, smooth motion, grouped settings lists.

## Performance rules (apply to every phase)
Performance is a requirement, not a polish step. Every change must respect these:

1. **Budget:** the whole client UI must cost well under 1 ms of frame time on a mid-range PC with all widgets on. Each PR states FPS with the client on and off (same world, same spot).
2. **No per-frame allocation:** no `new` objects, string concatenation or formatting in render code. Cache formatted text and only rebuild it when the value changes (FPS, ping, coordinates...). Allocation causes garbage-collection stutter in 1.8.9.
3. **Compile shaders once**, cache uniform locations, batch draw calls, and minimize GL state changes.
4. **Blur is the expensive part:** render it at reduced resolution, once per frame, only when a blurred element is visible, and only in the area behind it. Menus can use stronger blur than the in-game HUD. Quality setting: Off / Low / High.
5. **Pay only for what is visible:** disabled widgets cost nothing, off-screen widgets are skipped, animations only run while something is moving.
6. **Cache static content:** text widths, font atlases and widgets that rarely change are drawn from cached data, not recomputed.
7. **Graceful fallback:** if a shader fails to compile (old GPU), fall back to the simple renderer instead of crashing.
8. **Measure:** a `.perf` command / debug overlay shows the client's own render time per frame, so regressions are visible.
9. **Quality presets:** a Low / Medium / High setting (blur, shadows, animation detail) so weaker PCs can still use the client.

## Phases
0. **Design language**: colors (dark/light), radii, spacing, font (open font such as Inter, not Apple's SF), open icon set (Lucide/Phosphor), animation timings. Reviewed as an HTML mockup first.
1. **Rendering toolkit**: shader rounded rects (gradient, outline, shadow), low-res frosted-glass blur, animation/easing helpers, clipping, text and icon helpers, demo screen.
2. **HUD widget system**: `Widget` base class (position, anchor, scale, opacity, enabled), manager with save/load, drag-and-drop editor with snapping; migrate logo, watermark and module list out of the 620-line `HUD`.
3. **Widgets**: FPS, CPS, ping, coordinates, clock, armor/potion status, keystrokes, module list pill.
4. **Notifications**: iOS-style banners and a Dynamic-Island-style pill for module toggles.
5. **Menus**: Settings-app-style ClickGUI (grouped lists, animated switches, sliders, segmented controls), restyled main menu, remove the two old GUIs.
6. **Polish**: light/dark theme, accent color, performance pass and presets.
7. **Release**: version bump, GitHub release with jar, working update check.

## Workflow
Each phase is one or a few PRs. Code is written without a local Minecraft, so the owner builds each branch and sends screenshots / FPS numbers back.
