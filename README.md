# Yet Another Thirst - Minecraft 26.2

Development documentation and technical specifications for the Minecraft 26.2 branch of Yet Another Thirst.

## Version Specifications

| Property | Value |
|---|---|
| Minecraft Version | 26.2 (`[26.2, 27)`) |
| Mod Version | 1.5.3 |
| Mod ID | `yet_another_thirst` |
| Group | `dev.minhnh.yetanotherthirst` |
| Java Version | 25 |
| Forge | 65.1.5 (`[65,)`) |
| NeoForge | 26.2.0.93 (`[26.2,)`) |
| Fabric Loader | 0.20.1 (`>=0.18.4`) |
| Fabric API | 0.161.0+26.2 (`>=26.2 <27`) |
| Parchment Mappings | 2026.06.16 (MC 26.2) |
| NeoForm | 26.2-2 |

## Architecture

Multi-loader architecture using a shared `common` module with loader-specific implementations:

```
Yet-Another-Thirst/
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

## Mod Compatibility (26.2)

Compatibility layers are managed through `dev.minhnh.yetanotherthirst.compat.ThirstCompat`:

| Mod | Feature | Supported Loaders (26.2) |
|---|---|---|
| AppleSkin | Thirst HUD preview and drinkable item tooltips | Fabric, Forge, NeoForge |
| Jade | Water boiler and filter frame HUD tooltips | Forge, NeoForge |
| Cold Sweat | Body temperature influence on dehydration | Forge, NeoForge |
| EnvironmentZ | Body temperature influence on dehydration | Fabric, NeoForge |
| Tough as Nails | Automatic conflict avoidance / coexistence handling | Forge, NeoForge |
| Vampirism | Vampire player thirst suspension | Forge, NeoForge |
| Supernatural | Vampire player thirst suspension (NBT fallback) | Forge, NeoForge |

## Development Constraints

- **Common Module Independence**: Never import loader-specific classes (`net.minecraftforge.*`, `net.neoforged.*`, `net.fabricmc.*`) into `common`. Abstract via `IPlatformHelper`.
- **Property Expansion**: Any property added to `gradle.properties` must be added to the `expandProps` map in `buildSrc/src/main/groovy/multiloader-common.gradle`.
- **Registries**: Block, item, and menu registries must be initialized in each loader's subproject.
- **Compatibility Boundaries**: Implement mod compat only for loaders supporting that mod on Minecraft 26.2.
