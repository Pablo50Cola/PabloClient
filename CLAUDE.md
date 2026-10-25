# RavenP – notes for working in this repo

Fork of K-ov/Raven (RavenB++), GPL-3.0. Forge 1.8.9 client mod, Java 8, Mixin.

## Build
- Needs **JDK 8** (set JAVA_HOME) and network access to Forge/Sponge/Mojang maven hosts.
- `./gradlew clean build` (Gradle 4.7, ForgeGradle 2.1-SNAPSHOT). Output: `build/libs/`.
- `build.gradle` embeds `libs/discord-rpc.jar` via `fileTree`; that jar is not in `libs/`, so check whether anything still needs it before relying on it.
- **Mixin must stay on 0.7.11-SNAPSHOT** (embedded via Sponge maven). 0.6 is too old (`@Accessor`), and 0.8.x needs ASM 6+, which 1.8.9 lacks (crashes with `NoClassDefFoundError ... ClassRemapper`). `libs/mixin-0.8.5.jar` is not used.
- Verified by the owner: this build compiles on JDK 8, launches on Forge 1.8.9, and the modules work in singleplayer.
- The cloud sandbox cannot reach those hosts, so code changes there are unbuilt: the owner compiles and tests locally.

## Layout
- `src/main/java/keystrokesmod/client/module/modules/<category>/` – one class per module, extends `Module`, registers `Setting`s (`SliderSetting`, `TickSetting`, `ComboSetting`, ...). Modules are registered in `ModuleManager`.
- `client/mixin/mixins/` – Mixin classes (`mixins.keystrokesmod.json`). Mappings: stable_20 / searge.
- `client/clickgui/raven` and `client/clickgui/kv` – the two ClickGUIs. `client/command/commands` – chat commands.
- Update checking: `Raven.sourceLocation/downloadLocation`, `VersionManager`, `UpdateCheck` (all point at this fork, branch `main`). Version lives in `src/main/resources/assets/keystrokesmod/version` and `branch`.

## Rules
- Keep GPL-3.0 and existing credits/notices.
- No telemetry, no data collection, no remote code or config execution.
- All Creative and Functionality- Decisions are to be made or run by Pablo50Cola 
