# Pablo Client: notes for working in this repo

Pablo Client is a Forge 1.8.9 client mod (Java 8, Mixin), GPL-3.0, built on RavenB++. Read `ROADMAP.md` first (mission, status, next action), then `docs/UI_TOOLKIT.md`. Also read `AGENTS.md` for how to work with the owner.

## Build
- Needs **JDK 8** (set `JAVA_HOME`) and network access to the Forge/Sponge/Mojang maven hosts.
- `./gradlew clean build` (Gradle 4.7, ForgeGradle 2.1-SNAPSHOT). Output: `build/libs/PabloClient-<version>.jar`.
- `build.gradle` embeds `libs/discord-rpc.jar` via `fileTree`; that jar is not in `libs/`, so check whether anything still needs it before relying on it.
- **Mixin must stay on 0.7.11-SNAPSHOT** (embedded via Sponge maven). 0.6 is too old (`@Accessor`), and 0.8.x needs ASM 6+, which 1.8.9 lacks (crashes with `NoClassDefFoundError ... ClassRemapper`). `libs/mixin-0.8.5.jar` is not used.
- Verified by the owner: Phase 1 compiles on JDK 8, launches on Forge 1.8.9, the old modules work in singleplayer and the `uidemo` screen renders correctly.
- The cloud sandbox cannot reach the build hosts, so code changes written there are unbuilt: the owner compiles and tests locally. API names written from memory may need a fix after the first build; ask for the compile output.

## Layout
- `src/main/java/keystrokesmod/client/module/modules/<category>/`: one class per module, extends `Module`, registers `Setting`s (`SliderSetting`, `TickSetting`, `ComboSetting`, ...). Modules are registered in `ModuleManager`.
- `client/mixin/mixins/`: Mixin classes (`mixins.keystrokesmod.json`). Mappings: stable_20 / searge.
- `client/clickgui/raven` and `client/clickgui/kv`: the two old ClickGUIs (to be replaced in Phase 5). `client/command/commands`: terminal commands, registered in `CommandManager` (`addCommand(new X())`).
- `client/ui/`: the new UI toolkit (Phase 1), see `docs/UI_TOOLKIT.md`. It is hooked in via `UiEvents` (registered in `Raven.init()`).
- Render events: `Raven.eventBus` (Guava) posts `Render2DEvent` at the end of each render tick; `Raven.register(obj)` registers on the Forge bus.
- Update checking: `Raven.sourceLocation/downloadLocation`, `VersionManager`, `UpdateCheck` (point at this repository, branch `main`). Version lives in `src/main/resources/assets/keystrokesmod/version` and `branch`.
- Naming: user-visible name is "Pablo Client". Package names and the mod id `keystrokesmod` stay unchanged on purpose.

## Rules
- Keep GPL-3.0 and existing credits/notices (`CREDITS.md`, per-file comments).
- No telemetry, no data collection, no remote code or config execution.
- No features meant to evade server anti-cheat or hide the client from servers or staff (no mod-list/brand spoofing, staff-avoidance, etc.).
- Follow the performance rules in `ROADMAP.md` for every UI change.
- Commit to a branch, never straight to `main`. End commits with the attribution trailers required by the session.
