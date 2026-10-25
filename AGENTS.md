# Handoff guide for collaborators and AI assistants

You are picking up **Pablo Client**, an iOS-style Minecraft 1.8.9 Forge client. Do this before anything else:

1. Read `ROADMAP.md`: the mission, the status table and the **Next action** section tell you exactly where the project stands.
2. Read `CLAUDE.md`: how to build, repository layout, hard rules.
3. Read `docs/UI_TOOLKIT.md` if you touch anything visual.
4. Look at `git log` and open branches to see unmerged work.

## The owner

Pablo owns the repository and the product decisions. He is not an experienced programmer and builds on Windows with JDK 8. He wants to be walked through anything he has to do (what to click, what to type, what to send back). He downloads branch ZIPs from GitHub instead of using git. Do not ask him to run git commands or read stack traces; ask for screenshots, the build output, or `.minecraft/logs/latest.log`.

## How work flows

1. Write the code on a branch (`phaseN/<topic>`).
2. If you cannot build (no access to the Forge hosts), say so clearly and hand Pablo a short build-and-test recipe.
3. He builds, tests and sends back results (screenshots, FPS, logs). Fix based on those.
4. After he confirms it works, merge into `main` through a pull request and update `ROADMAP.md`.

## Non-negotiable

- Performance rules in `ROADMAP.md` apply to every change.
- No evasion of anti-cheat, no telemetry, no remote code. Keep GPL-3.0 and the credits.
- Never leave `main` in a state that does not build or launch.
