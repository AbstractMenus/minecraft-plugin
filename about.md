# AbstractMenus - Architecture Overview

## Project Summary

AbstractMenus is a GUI plugin for PaperMC/Folia Minecraft servers that allows creating custom inventory-based menus configured via HOCON files. Players interact with these menus through various activators (commands, items, events), and server admins define menu layouts, rules, and actions declaratively.

- **Target MC version:** 26.3
- **Minimum MC version:** 26.1
- **Java version:** 25
- **Build system:** Gradle 9.9.1 with Paper API + Shadow plugin
- **Configuration format:** HOCON (bundled library at `ru.abstractmenus.hocon`)

## Architecture Layers

```
Plugin Entry Point (AbstractMenus.java)
    |
    v
Services Layer (MenuManager, BungeeManager, HeadAnimManager, ProfileStorage, VariableManagerImpl)
    |
    v
Data Layer (Actions, Rules, Activators, ItemProps, Catalogs - ~164 files)
    |
    v
API Module (:api - local Gradle subproject)
```

## Plugin Lifecycle

1. **`onLoad()`**: Initialize `AbstractMenusProvider`, register service in Bukkit `ServicesManager`
2. **`onEnable()`**:
   - Load `MainConfig` from HOCON config
   - Initialize core services: `FoliaLib`, `HeadAnimManager`, `VariableManagerImpl`, `BungeeManager`, `ActionBar`, `Title`, `MenuManager`
   - Register external providers: Vault (economy), LuckPerms (permissions), PlaceholderAPI, SkinsRestorer
   - Register type registries in order: `ItemProps` -> `Activators` -> `MenuActions` -> `MenuRules` -> `Catalogs`
   - Load menus from disk (`MenuManager.loadMenus()`)
   - Register Bukkit event listeners: `InventoryListener`, `ProfileStorage`, `ChatListener`, `PlayerListener`, optionally `WGHandlers`
3. **`onDisable()`**: Unload all menus, stop BungeeManager timer, shutdown variables DAO, unregister channels

## Menu System

```
Menu (API interface)
  +-- AbstractMenu (base: title, size, items, rules, actions, inventory management)
       +-- SimpleMenu (standard inventory GUI, static layout)
       +-- AnimatedMenu (frame-based animations with timed transitions)
       +-- GeneratedMenu (dynamic layout via Matrix pattern)
```

**Menu open flow:** Activator triggers -> clone menu -> check openRules -> preOpenActions -> createInventory -> openActions -> refresh (placeItems) -> player.openInventory -> postOpenActions

**Menu update loop:** `MenuManager` runs a 20-tick scheduler. For each open menu, calls `menu.update()` which checks `updateInterval` and refreshes items.

## Type Registry System

Central pattern: `Types` class (from API module) holds maps of registered type names to classes and serializers.

| Registry | Init method | Example types | Count |
|---|---|---|---|
| Actions | `MenuActions.init()` | `openMenu`, `message`, `sound`, `closeMenu`, `setVar` | ~50 |
| Rules | `MenuRules.init()` | `permission`, `money`, `if`, `and`, `or`, `level` | ~25 |
| Item Properties | `ItemProps.init()` | `material`, `name`, `lore`, `enchant`, `nbt`, `skull` | ~25 |
| Activators | `Activators.init()` | `command`, `clickItem`, `joinServer`, `shiftClick` | ~15 |
| Catalogs | `Catalogs.init()` | Various catalog types | ~5 |

Each type has:
- Implementation class (e.g., `ActionMenuOpen`)
- Inner `Serializer extends NodeSerializer` for HOCON deserialization
- Registration via `Types.registerXxx(name, class, serializer)`

## Serialization

- **Format:** HOCON (not YAML). Bundled HOCON library at `ru.abstractmenus.hocon`
- **Entry point:** `NodeSerializer` implementations per type
- **Item serialization:** `ItemSerializer` handles full item deserialization from HOCON nodes
- **Type adapters:** `LocationSerializer`, `NbtCompoundSerializer`, etc.

## Variables System

- **Storage:** SQLite database (`variables.db`) via `VariablesDao`
- **Cache:** In-memory `ConcurrentHashMap` in `VariableManagerImpl`
- **Types:** Global variables (`/var`) and per-player variables (`/varp`)
- **Sync:** Optional BungeeCord variable sync via plugin messaging
- **Expiry:** Variables can have TTL (expiry timestamp)

## External Plugin Handlers

Abstracted through interfaces in `handlers/` package, accessed via static `Handlers` facade:

| Handler | Interface | Implementations |
|---|---|---|
| Economy | `EconomyHandler` | `EconomyVaultHandler` (Vault) |
| Permissions | `PermissionsHandler` | `LuckPermsHandler`, `PermissionDefaultHandler` |
| Levels | `LevelHandler` | `LevelDefaultHandler` |
| Placeholders | `PlaceholderHandler` | `PlaceholderCustomHandler` (PAPI), `PlaceholderDefaultHandler` |
| Skins | `SkinHandler` | `SkinsRestorerHandler` |

## NMS Layer

Wrappers use the current server APIs without Minecraft version checks.

- **Book:** `Book_1_15` uses `player.openBook()`.
- **Title:** `SenderModern` uses `player.sendTitle()`.
- **ActionBar:** `ActionBar_1_9` uses Spigot `sendMessage(ChatMessageType.ACTION_BAR)`.

## Key Dependencies

| Dependency | Role |
|---|---|
| `paper-api` | Compile-only Paper 26.3 server API |
| `:api` | Core API interfaces (Menu, Item, Rule, Action, etc.) |
| `Lombok` | Boilerplate reduction (@Getter, @Setter, @AllArgsConstructor) |
| `Kyori Adventure + MiniMessage` | Rich text component API, MiniMessage format support |
| `FoliaLib` | Thread-safe task scheduling (Folia compatibility) |
| `javaluator` | Mathematical expression evaluation |
| `item-nbt-api` | NBT manipulation abstraction |

## Technical Debt and Risk Areas

### Thread Safety (Critical)
- `ProfileStorage.profiles`: `HashMap` accessed from async thread (PlayerJoinEvent handler) and main thread
- `VariablesDao.connection`: Single SQLite `Connection` used from multiple threads without synchronization
- `AbstractMenu.placedItems`: Lazy init `if (null) new HashMap<>()` is not thread-safe
- `BungeeManager.servers`: `HashSet` accessed from message handler and scheduler threads

### Memory Leaks
- `HeadAnimManager.currentFrames`: ConcurrentHashMap entries accumulate indefinitely
- `AbstractMenu.placedItems`: Not cleared after `dropPlaced()`

### Deprecated/Legacy Code
- `Skulls.createSkullItem()`: Uses deprecated `ItemStack(Material, int, short)` constructor

### Removed: RuleJS (Nashorn)
The `js` rule type (JavaScript evaluation via Nashorn engine) has been **removed**. It was a security risk (arbitrary code execution) and Nashorn was removed from JDK in Java 15+. Use the `if` rule with `BooleanEvaluator` as an alternative for conditional logic.
