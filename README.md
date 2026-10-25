# RavenP

RavenP is a fork of [RavenB++ (K-ov/Raven)](https://github.com/K-ov/Raven), a PvP and utility mod for Minecraft 1.8.9 (Forge).
This fork is maintained by [Pablo50Cola](https://github.com/Pablo50Cola) and adds improvements and new features on top of upstream.

**Status:** early setup. Releases from this fork will appear under [Releases](https://github.com/Pablo50Cola/RavenP/releases).
The download links further down this page still point to the upstream project.

## Credits and license

RavenP is licensed under the **GNU GPL v3** (see `LICENSE`), the same as upstream, so this fork and any build of it must stay open source under that license.
It builds on the work of K-ov (RavenB++), Kopamed (Raven B+), the Raven B3/B4 authors, OFP and spiderfrog (KeystrokesMod), Yario and Extazz (KeystrokesMod V5), and the other contributors in the git history.
Upstream's Discord and links are left in place where they credit the original project.

---

# RavenB++ (upstream readme)

Raven b++ is a pvp and utility mod for minecraft 1.8.x. It supports Forge and Feather adding quality of life improvements, bug fixes, and so much more.<br>
Not related to Raven B3/B4 in any way.

## Community
Coming Soon...

# Installation & Download

* Download the latest version [here](https://github.com/K-ov/Raven/releases/download/v1.0.0/1.8.9.BetterKeystrokes.V-1.2.jar)
* Download Essential [here](https://cdn.essential.gg/mods/60ecf53d6b26c76a26d49e5b/62c746c961a27f691be98531/Essential-forge_1-8-9.jar)
* Download Forge for Minecraft 1.8.9 [here](https://maven.minecraftforge.net/net/minecraftforge/forge/1.8.9-11.15.1.2318-1.8.9/forge-1.8.9-11.15.1.2318-1.8.9-installer.jar) and run the installer.
* If you cannot find the Forge version, fully close the Minecraft Launcher using Task Manager and reopen it. If you still need help, join the Discord server above.
* After launching the Forge profile once:
  * On Windows: go to `%appdata%\.minecraft\mods`
  * On macOS: go to `~/Library/Application Support/minecraft/mods/`
* Put both:
  * `1.8.9.BetterKeystrokes.V-1.2.jar`
  * `Essential-forge_1-8-9.jar`
  into the `mods` folder.
* Launch Minecraft using the Forge profile and you're good to go.
(Upstream from RavenB++) 

Alternatively, this [YouTube tutorial](https://www.youtube.com/watch?v=CuV4GfLDubk) explains the installation process step-by-step.

---

## Download

[Latest](https://github.com/K-ov/Raven/releases/download/v1.0.0/1.8.9.BetterKeystrokes.V-1.2.jar)

---

# Building Ravenb++ Yourself

1. [Download](https://gradle.org/next-steps/?version=2.7&format=bin) and [install](https://docs.gradle.org/current/userguide/installation.html) Gradle.
2. Clone this repository to your machine.
3. Open a terminal or command prompt in the project folder.
4. Run:

```bash
./gradlew build
```

5. The compiled build will be located in:

```text
build/libs
```
