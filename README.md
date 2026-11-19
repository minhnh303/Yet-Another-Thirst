# Yet Another Thirst - Minecraft 1.20.1

Yet Another Thirst adds a configurable thirst system designed for modpacks, with water purity, compatibility integrations, and an in-game configuration screen.

## Version specifications

| Property | Value |
| --- | --- |
| Minecraft | 1.20.1 (`[1.20.1, 1.22)`) |
| Mod | 1.6.0 |
| Java | 17 |
| Forge | 47.2.30 (`[47,)`) |
| NeoForge | 47.1.99 (`[47,)`) |
| Fabric Loader | 0.15.11 |
| Fabric API | 0.92.2+1.20.1 (`>=1.20.1 <1.22`) |
| Parchment mappings | 2023.09.03 for 1.20.1 |

## Architecture

The project uses shared `common` gameplay and client code, with Forge, NeoForge, and Fabric loader modules. Loader-specific services are selected through `Services.PLATFORM`; configuration backends are selected through `IConfigBackend`.

The in-game configuration screen is shared client code. Changes are staged in `ConfigSession` and saved through the active loader's configuration backend.

## Development

```bash
./gradlew build
./gradlew :common:test
./gradlew :common:checkSeams
./gradlew :forge:build
./gradlew :neoforge:build
./gradlew :fabric:build
```

## Compatibility

Compatibility is routed through `ThirstCompat` and runtime loader checks.

| Mod | Integration |
| --- | --- |
| Create | Preserves water purity during fluid transfer and supports filter frames. |
| Immersive Engineering | Preserves fluid purity and supports the water boiler on Forge and NeoForge. |
| Jade | Shows block information. |
| AppleSkin | Shows thirst HUD and tooltip previews. |
| Cold Sweat, Environmentz | Adjust dehydration based on temperature. |
| Vampirism, Supernatural | Suspends thirst depletion for vampire players. |
| Tough as Nails | Can disable Yet Another Thirst in favor of external thirst. |

Dependency versions used for compilation are listed in [COMPATIBLE_MODS_VERSIONS.md](COMPATIBLE_MODS_VERSIONS.md).
