# Yet Another Thirst - Minecraft 1.21

Development documentation and technical specifications for the Minecraft 1.21 branch of Yet Another Thirst.

## Version Specifications

| Property | Value |
|---|---|
| Minecraft Version | 1.21 (`[1.21, 1.21.2)`) |
| Mod Version | 1.21-1.5.2 |
| Mod ID | `yet_another_thirst` |
| Group | `dev.minhnh.yetanotherthirst` |
| Java Version | 21 |
| Forge | 51.0.33 (`[51,)`) |
| NeoForge | 21.0.167 (`[21.0.167,)`) |
| Fabric Loader | 0.16.9 |
| Fabric API | 0.102.0+1.21 (`>=1.21 <1.22`) |
| Parchment Mappings | 2024.06.23 (MC 1.21) |
| NeoForm | 1.21-20240613.152323 |

## Architecture

Multi-loader architecture using a shared `common` module with loader-specific implementations:

```
Yet-Another-Thirst-1.21/
├── common/      # Loader-agnostic game logic, data, screens, blocks, items, mixins
├── forge/       # Forge entry point and platform service implementation
├── neoforge/    # NeoForge entry point and platform service implementation
├── fabric/      # Fabric entry point and platform service implementation
└── buildSrc/    # Shared Gradle multi-loader conventions (multiloader-common.gradle)
```

### Platform Abstraction

Platform-specific logic uses Java `ServiceLoader`:
- Interface: `dev.minhnh.yetanotherthirst.platform.services.IPlatformHelper`
- Entry point: `dev.minhnh.yetanotherthirst.platform.Services.PLATFORM`
- Service provider configuration: `META-INF/services/dev.minhnh.yetanotherthirst.platform.services.IPlatformHelper` in each subproject (`forge`, `neoforge`, `fabric`).

## Development Commands

### Build Tasks

```bash
# Build all loaders
./gradlew build

# Build a specific loader
./gradlew :fabric:build
./gradlew :neoforge:build
./gradlew :forge:build

# Clean build outputs
./gradlew clean
```

### Run Environment

```bash
# Client
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
./gradlew :forge:runClient

# Dedicated Server
./gradlew :fabric:runServer
./gradlew :neoforge:runServer
./gradlew :forge:runServer
```

## Testing Suite

Unit tests reside in `common/src/test` using JUnit Jupiter 5.10.2 and Mockito 5.11.0. Tests use `TestPlatformHelper` registered via `ServiceLoader` in `common/src/test/resources/META-INF/services/` to execute in isolation without launching a Minecraft instance.

### Test Execution

```bash
# Run all tests
./gradlew test

# Run common module tests only
./gradlew :common:test

# Run a specific test class
./gradlew :common:test --tests "dev.minhnh.yetanotherthirst.ThirstStateTest"
```

### Test Suites

| Test Class | Scope / Verification |
|---|---|
| `ThirstDataTest` | `ThirstData` record defaults, immutability, and NBT codec serialization/deserialization. |
| `ThirstStateTest` | Atomic state mutations, drink mechanics, exhaustion accumulation, and snapshot synchronization. |
| `ThirstStorageTest` | Multi-player UUID state caching, thread safety, data retention, and lifecycle resets. |
| `ThirstConfigTest` | Boundary constraints, default values, clamp behavior, and exhaustion/sprint thresholds. |
| `ThirstValuesTest` | Item drink value parsing, hydration/quenched mappings, and fallback defaults. |
| `WaterPurityTest` | Purity levels (`DIRTY`, `PURIFIED`), item tooltips, formatting components, and container handling. |
| `FilterCoreTest` | Filter core damage, durability degradation, and block entity processing logic. |
| `ThirstCompatTest` | Third-party mod reflection delegates and config flags. |
| `YetAnotherThirstAPITest` | Public API methods (`YetAnotherThirstAPI`) for external mod access and thirst querying. |
| `TestPlatformHelper` | Mock `IPlatformHelper` provider used for headless test execution. |

## Mod Compatibility (1.21)

Compatibility layers are managed through `dev.minhnh.yetanotherthirst.compat.ThirstCompat` via reflection:

| Mod | Feature | Supported Loaders (1.21) |
|---|---|---|
| AppleSkin | Thirst HUD preview and drinkable item tooltips | Fabric, NeoForge |
| Jade | Water boiler and filter frame HUD tooltips | Fabric, NeoForge |
| Cold Sweat | Body temperature influence on dehydration | NeoForge |
| Tough as Nails | Automatic conflict avoidance / coexistence handling | NeoForge |
| Vampirism | Vampire player thirst suspension | NeoForge |
| Supernatural | Vampire player thirst suspension (NBT fallback) | NeoForge |

See `COMPATIBLE_MODS_VERSIONS.md` for specific tested jar files and versions.

## Development Constraints

- **Common Module Independence**: Never import loader-specific classes (`net.minecraftforge.*`, `net.neoforged.*`, `net.fabricmc.*`) into `common`. Abstract via `IPlatformHelper`.
- **Property Expansion**: Any property added to `gradle.properties` must be added to the `expandProps` map in `buildSrc/src/main/groovy/multiloader-common.gradle`.
- **Registries**: Block, item, and menu registries must be initialized in each loader's subproject.
- **Compatibility Boundaries**: Implement mod compat only for loaders supporting that mod on Minecraft 1.21.
