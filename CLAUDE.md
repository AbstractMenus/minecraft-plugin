# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AbstractMenus is a GUI plugin for PaperMC/Folia Minecraft servers (26.1+) that allows creating custom inventory-based menus. Written in Java 25, it uses Gradle 9.9.1 with the Paper 26.3 API and Shadow plugin.

## Build Commands

```bash
./gradlew build          # Compile and run tests
./gradlew shadowJar      # Create fat JAR with bundled dependencies (output: build/dist/AbstractMenus-<version>.jar)
./gradlew test           # Run JUnit 6 tests only
```

Tests are in `plugin/src/test/java/` using JUnit 6 (Jupiter), Mockito, and MockBukkit 26.3. Run a single test with:
```bash
./gradlew test --tests "ru.abstractmenus.TestValueComparator"
```

## Architecture

### Plugin Entry Point
`ru.abstractmenus.AbstractMenus` extends `JavaPlugin`. Initialization order in `onEnable()`:
1. Load `MainConfig` from HOCON config
2. Initialize core services (VariableManager, BungeeManager, MenuManager, etc.)
3. Register external provider handlers (Vault, LuckPerms, PlaceholderAPI, SkinsRestorer)
4. Register type registries: `ItemProps` → `Activators` → `MenuActions` → `MenuRules` → `Catalogs`
5. Load menus from disk, register event listeners

### Type Registry System (Core Pattern)
The plugin uses a centralized type registry (`Types` from the API module) where actions, rules, item properties, activators, and catalogs are registered by name. Each type has a corresponding `NodeSerializer` for HOCON deserialization. Registration happens in static `init()` methods:
- `MenuActions.init()` — registers ~50 action types (e.g., `"openMenu"` → `ActionMenuOpen`)
- `MenuRules.init()` — registers ~25 rule types (e.g., `"permission"` → `RulePermission`)
- `ItemProps.init()` — registers ~25 item property types (e.g., `"material"` → `PropMaterial`)
- `Activators.init()` — registers ~15 activator types (e.g., `"command"` → `OpenCommand`)
- `Catalogs.init()` — registers catalog types

### Menu Hierarchy
```
Menu (API interface)
├── AbstractMenu (base with title, size, items, rules, actions)
│   ├── SimpleMenu (standard inventory GUI)
│   ├── AnimatedMenu (frame-based animations)
│   └── GeneratedMenu (dynamic layout via Matrix)
```

### Package Structure (`ru.abstractmenus`)
- **`data/`** — Largest package (~164 files). Contains all action, rule, activator, item property, and catalog implementations. Each has an inner `Serializer` class.
- **`services/`** — Core managers: `MenuManager` (menu lifecycle, 20-tick update loop), `BungeeManager`, `HeadAnimManager`, `ProfileStorage`
- **`handlers/`** — Abstraction layer over external plugins (Economy, Permissions, Levels, Placeholders, Skins). Accessed via `Handlers` static facade.
- **`serializers/`** — HOCON deserialization: `ItemSerializer`, `LocationSerializer`, type adapters
- **`commands/`** — `/am`, `/var`, `/varp` commands and subcommands
- **`command/`** — Command framework (parsing, arguments, context)
- **`variables/`** — Global/player variable system with SQLite persistence
- **`placeholders/`** — PlaceholderAPI integration and built-in placeholder handlers
- **`menu/`** — Menu classes, items, frames
- **`listeners/`** — Bukkit event listeners (inventory clicks, chat, player join, WorldGuard regions)
- **`nms/`** — NMS code for ActionBar, Title, Book via reflection
- **`extractors/`** — Context extractors (Block, Entity, NPC, ItemStack, Region, World)
- **`datatype/`** — Type wrappers (TypeBool, TypeInt, TypeLocation, TypeMaterial, etc.)

### Configuration Format
Menus and config use HOCON format (not YAML). The internal HOCON library is at `ru.abstractmenus.hocon`.

### Key Dependencies
- **API module**: Local `:api` subproject (`ru.abstractmenus:api`)
- **Lombok**: Used throughout for `@Getter`/`@Setter`/`@AllArgsConstructor`
- **Kyori Adventure + MiniMessage**: Rich text component API
- **FoliaLib**: Thread-safe task scheduling (Folia compatibility)
- **Paper API**: Compile-only Paper 26.3 API dependency

### Adding a New Action/Rule/Property/Activator
1. Create class in the appropriate `data/` subpackage
2. Add an inner `Serializer extends NodeSerializer` class for HOCON deserialization
3. Register it in the corresponding `init()` method (e.g., `MenuActions.init()`) via `Types.registerAction(name, class, serializer)`

### External Plugin Integration
External plugins are accessed through handler interfaces in `handlers/`. The `Handlers` class holds static references. Provider detection and registration happens in `AbstractMenus.registerProviders()`. Soft dependencies are declared in `plugin.yml`.
