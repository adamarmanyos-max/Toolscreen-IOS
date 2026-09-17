# Toolscreen

A Fabric mod for **Minecraft Java Edition 1.16.1** (the long-standing speedrun category
version), built to run under [Amethyst-iOS](https://github.com/AngelAuraMC/Amethyst-iOS) — a
PojavLauncher-based Java Edition launcher for iOS/iPadOS with Forge/Fabric/Quilt support.

## Why this exists / what changed from the original

The desktop "Toolscreen" speedrunning utility works by **DLL-injecting** into the Windows
Minecraft process to add: zoom/magnifier overlays (eye-spy), preset alignment overlays
("Mapless" nether alignment, "Preemptive" end alignment), instant borderless window resizing,
global key rebinding, and a virtual camera for stream-splitting.

None of that is possible on iOS — there's no DLL injection, no OS-level window manager to
resize, and no virtual camera driver inside the app sandbox. So this mod reimplements the parts
that *are* achievable as normal, legal, client-side game code:

| Original Toolscreen feature   | Here                                                              |
|--------------------------------|--------------------------------------------------------------------|
| Magnifier / eye-spy zoom       | ✅ Hold-to-zoom keybind (temporarily lowers FOV, same technique as the classic "Zoom" mod) |
| Mapless / Preemptive overlays  | ✅ Toggleable on-screen alignment guide, cycled between a cross (mapless) and box (preemptive) preset |
| Key rebinding                  | ⚠️ Partial — exposed as normal Fabric keybinds in Minecraft's own Controls menu, not global OS rebinding |
| Instant borderless resize      | ❌ N/A — the app is already fullscreen on iOS |
| Virtual camera                 | ❌ Not possible inside the iOS app sandbox |

## Features

- **Hold-to-zoom** (`key.toolscreen.zoom`, default `C`): temporarily sets FOV to a low value
  (configurable, default `5`) while held, for precise eye-spy pixel reading, then restores your
  normal FOV on release.
- **Alignment overlay toggle** (`key.toolscreen.toggle_overlay`, default `V`): shows/hides a
  center-screen guide.
- **Cycle overlay preset** (`key.toolscreen.cycle_preset`, default `B`): switches between
  `mapless` (crosshair cross, for nether portal alignment) and `preemptive` (box, for end portal
  room alignment) presets.
- Config persisted to `.minecraft/config/toolscreen.json` (zoom FOV, overlay enabled/preset/color).

All three actions are ordinary Fabric `KeyBinding`s, so on Amethyst-iOS you map them to
on-screen touch buttons using the launcher's own **custom on-screen controls** editor — the mod
doesn't draw its own touch buttons, so behaviour matches every other keybind in the game.

## Building

Requires JDK 17.

```
./gradlew build
```

The output jar is written to `build/libs/toolscreen-0.1.0.jar`.

Build status is verified by CI (`.github/workflows/build.yml`) on every push — see the
[latest build's release assets](https://github.com/adamarmanyos-max/Toolscreen-IOS/releases/tag/latest-build)
for a ready-to-use jar without building locally.

## Installing on Amethyst-iOS

1. Build the jar (above), or grab it from a release.
2. In Amethyst-iOS, create/select a Fabric 1.16.1 profile.
3. Copy `toolscreen-0.1.0.jar` into that profile's `mods` folder (via the Files app / on-device
   file manager, same as any other Fabric mod).
4. Launch, then bind the three `Toolscreen` keybinds (Options → Controls → Toolscreen category)
   to on-screen touch buttons via Amethyst's custom controls editor.

## Roadmap / not yet done

- In-game config screen (currently hand-edit the JSON file).
- Tuning the alignment overlay's exact geometry/position against current community
  route notes for mapless/preemptive alignment, rather than the generic placeholder
  cross/box shapes here.
- Optional multi-version support (this targets 1.16.1 only, per the speedrun category version).
