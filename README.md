  # Pablo Client

An open-source Minecraft **1.8.9** (Forge) client with an **iOS-style interface**: rounded cards, frosted glass, smooth spring animations and grouped settings lists, built to stay light on frame time.

> **Status: early development.** The rendering toolkit (Phase 1) is done and the HUD/menu redesign is in progress. There is no public release yet. See [ROADMAP.md](ROADMAP.md) for exactly what is done and what comes next.

## What it is

Pablo Client started from the open-source RavenB++ mod (GPL-3.0) and is being grown into a full client in the style of Feather or Badlion:

- **Today:** the complete module set inherited from RavenB++ (combat, movement, player, world, render, minigames and utility modules, a command terminal, configs), now running on a new toolkit for GPU-drawn rounded shapes, shadows, frosted-glass blur and animations.
- **Goal:** a redesigned HUD (FPS, CPS, ping, keystrokes, coordinates, ...), notifications, and a Settings-app-style menu, all measured against a strict performance budget.

Read [ROADMAP.md](ROADMAP.md) for the mission, the phases and the performance rules.

## Responsible use

Many inherited modules give gameplay advantages that most multiplayer servers forbid. Use them only in singleplayer or where the server allows them. This project does not contain any telemetry or remote code execution.

## Try the UI toolkit

Start a singleplayer world, press **Right Shift** to open the menu, open the terminal and type `uidemo`. The demo screen shows the new shapes, glass blur, switches and a live readout of the client's own frame cost. See [docs/UI_TOOLKIT.md](docs/UI_TOOLKIT.md).

## Install (once a release exists)

1. Install Minecraft Forge for 1.8.9 (version 11.15.1.2318) and start the Forge profile once.
2. Put the `PabloClient-<version>.jar` from the Releases page into your `.minecraft/mods` folder.
3. Start the Forge profile.

## Build from source

Requirements: **JDK 8** (set `JAVA_HOME`), internet access for Gradle to fetch Forge and dependencies.

```bash
./gradlew clean build        # Windows: gradlew.bat clean build
```

The jar appears in `build/libs/`. Details and pitfalls (Mixin version, mappings) are in [CLAUDE.md](CLAUDE.md).

## Contributing and handoff

Humans and AI assistants: start with [AGENTS.md](AGENTS.md), then [ROADMAP.md](ROADMAP.md).

## License and credits

Licensed under the **GNU General Public License v3.0** (see [LICENSE](LICENSE)). Any build or fork must stay open source under the same license. Pablo Client builds on the work of many people; see [CREDITS.md](CREDITS.md).
