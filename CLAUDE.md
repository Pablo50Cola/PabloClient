# RavenP – notes for working in this repo

Fork of K-ov/Raven (RavenB++), GPL-3.0. Forge 1.8.9 client mod, Java 8, Mixin.

## Build
- Needs **JDK 8** (set JAVA_HOME) and network access to Forge/Sponge/Mojang maven hosts.
- `./gradlew clean build` (Gradle 4.7, ForgeGradle 2.1-SNAPSHOT). Output: `build/libs/`.
- `build.gradle` embeds `libs/discord-rpc.jar` via `fileTree`; that jar is not in `libs/`, so check whether anything still needs it before relying on it.
- The cloud sandbox cannot reach those hosts, so code changes there are unbuilt: the owner compiles and tests locally.

## Layout
- `src/main/java/keystrokesmod/client/module/modules/<category>/` – one class per module, extends `Module`, registers `Setting`s (`SliderSetting`, `TickSetting`, `ComboSetting`, ...). Modules are registered in `ModuleManager`.
- `client/mixin/mixins/` – Mixin classes (`mixins.keystrokesmod.json`). Mappings: stable_20 / searge.
- `client/clickgui/raven` and `client/clickgui/kv` – the two ClickGUIs. `client/command/commands` – chat commands.
- Update checking: `Raven.sourceLocation/downloadLocation`, `VersionManager`, `UpdateCheck` (all point at this fork, branch `main`). Version lives in `src/main/resources/assets/keystrokesmod/version` and `branch`.

## Rules
- Keep GPL-3.0 and existing credits/notices.
- No telemetry, no data collection, no remote code or config execution.
- No features meant to evade server anti-cheat or hide the client from servers or staff (no mod-list/brand spoofing, staff-avoidance, etc.).
