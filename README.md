# AbstractMenus

<a href="https://github.com/AbstractMenus/minecraft-plugin/blob/master/LICENSE"><img src="https://img.shields.io/badge/License-MIT-red.svg" alt="license"/></a>
<a href="https://github.com/AbstractMenus/minecraft-plugin/releases"><img src="https://img.shields.io/badge/version-2.0.0--alpha.2-blue" alt="version"/></a>
<img src="https://img.shields.io/badge/minecraft-26.1+-brightgreen" alt="minecraft"/>
<img src="https://img.shields.io/badge/paper-26.3-brightgreen" alt="paper"/>
<img src="https://img.shields.io/badge/java-25-orange" alt="java"/>
<img src="https://img.shields.io/badge/folia-supported-8a2be2" alt="folia"/>

AbstractMenus is a GUI plugin for Paper / Folia Minecraft servers that lets
server owners and developers build inventory-based menus declaratively through
HOCON configuration. Menus, rules, actions, activators, and item properties
are all first-class, extensible via an open type registry, and render through
a cached, MiniMessage-aware pipeline.

The plugin supports **Minecraft 26.1 onwards** and requires **Java 25**. The build targets the Paper 26.3 API.

## 🔭 Table of contents
- [Features](#-features)
- [Links](#-links)
- [Requirements](#-requirements)
- [Build](#-build)
- [Running locally](#-running-locally)
- [Architecture at a glance](#-architecture-at-a-glance)
- [Benchmarks](#-benchmarks)
- [Tests](#-tests)
- [Contributions and feedback](#-contributions-and-feedback)
- [Contributors](#-contributors)
- [Licence](#-licence)

---

## ✨ Features

- **HOCON-driven menus** — declare `SimpleMenu`, `AnimatedMenu`, and
  paginated `GeneratedMenu` from a config file, with `include` + templating
  support.
- **Type registry** — ~50 actions, ~25 rules, ~25 item properties, ~15
  activators, and 5 catalog types; third parties can register their own via
  `Types.register*()`.
- **MiniMessage + legacy colors** — single-pass `§a → <green>` conversion,
  with pre-computation for fully-static names and lore (zero parse cost on
  refresh for typical menus).
- **Placeholder integration** — PlaceholderAPI-compatible, plus a set of
  built-in hooks: `%var_*%`, `%varp_*%`, `%ctg_*%`, `%hanim_*%`, `%server_*%`,
  `%placed_*%`, `%taken_*%`, `%changed_*%`.
- **Variables with persistence** — global (`%var_*%`) and personal
  (`%varp_*%`) variables, backed by SQLite, with cross-server sync over the
  BungeeCord channel.
- **Folia-compatible scheduling** — all Bukkit scheduler calls go through a
  `BukkitTasks` wrapper backed by FoliaLib.
- **External integrations (soft-deps)** — Vault, LuckPerms, WorldGuard,
  SkinsRestorer, MMOItems, HeadDatabase, ItemsAdder, Oraxen, Citizens,
  NBT-API.
- **Performance-first** — MiniMessage rewrite, copy-on-write item cloning,
  skull / HDB / player-profile caches, single-build animated frames,
  per-tick player cache (see
  [`PR_DESCRIPTION.md`](PR_DESCRIPTION.md) for the 1.18.0 profile-backed
  numbers).

## 🧰 Item properties

Item properties are configured per menu item on Minecraft 26.x. Namespaced keys containing `:` must be quoted in HOCON; keys without a namespace use `minecraft`.

`tooltipDisplay` can hide an entire tooltip or selected data components, alongside normal name and lore properties:

```hocon
name: "Bundle"
tooltipDisplay {
    hideTooltip: false
    hiddenComponents: [BUNDLE_CONTENTS, "minecraft:enchantments"]
}
lore: ["Contents stay hidden"]
```

Set `hideTooltip: true` to hide the whole tooltip. Component names accept uppercase, lowercase and namespaced keys. Unknown components fail at menu load. Hidden components are merged with existing flags, so `tooltipDisplay` and `flags` can appear in either order.

`itemModel` selects a resource-pack item model; `tooltipStyle` selects its tooltip sprites:

```hocon
itemModel: "example:menu/cake"
tooltipStyle: "example:menu/gold"
```

`maxStackSize` accepts integers from 1 through 99, with invalid constants rejected at menu load. It also accepts integer placeholders; results outside that range log a warning and leave the existing stack limit unchanged. Stack sizes above one cannot be combined with the `max_damage` component, so use them on items without durability:

```hocon
material: CAKE
maxStackSize: 99
count: 64
```

`rarity` accepts `COMMON`, `UNCOMMON`, `RARE` or `EPIC`, including enum placeholders:

```hocon
rarity: RARE
```

`model` keeps the integer format used by existing menus, mapping it to one float in the custom model data component:

```hocon
model: 123
```

It also accepts an object containing any of the four custom model data lists. Floats, flags, strings and colors support placeholders; colors accept hex, RGB strings or dye names:

```hocon
model {
    floats: [1.0, 2.5]
    flags: [true, false]
    strings: ["cake"]
    colors: ["#ff8000"]
}
```

`glow: true` forces the enchantment glint without adding an enchantment; `false` leaves the item's glint unchanged:

```hocon
glow: true
```

`flags` hides the matching tooltip components and preserves attribute modifiers, including those configured with `attributeModifier`:

```hocon
material: DIAMOND_SWORD
flags: HIDE_ATTRIBUTES
```

`count` is passed through unchanged, including amounts above the item's maximum stack size:

```hocon
material: DIAMOND_SWORD
count: 64
```

`data` remains accepted as a byte-valued alias for item damage through `Damageable`. It no longer reads or writes legacy material data; use `damage` for new menus.

## Upgrading

This version supports Minecraft 26.1+ and Java 25 only.

`glow` no longer adds an Unbreaking enchantment, and `flags` no longer writes default attribute modifiers. Items handed to players by older versions, such as menu opener items and quest tokens, that carry those old enchantments or modifiers no longer match updated item definitions in `clickItem` activators, `heldItem` and `inventoryItem` rules, or `itemRemove`. Reissue those affected items after updating.

The built-in placeholder values have changed:

- `block_data` returns the full block state string, such as `minecraft:oak_log[axis=y]`, instead of a legacy numeric data byte.
- `item_data` returns the item's damage value, or `0` for items without damage.
- `block_biome` returns the registry key path in uppercase, such as `PLAINS`.

`count` is passed through unchanged.

## 🔗 Links
- [SpigotMC](https://www.spigotmc.org/resources/abstract-menus-an-advanced-gui-plugin.75107/)
- [Discord](https://discord.gg/kt4P9Cgw)
- [Documentation](https://abstractmenus.github.io/docs/index.html)

## 🧰 Requirements
- **JDK 25+** (target and source)
- **Gradle 9.9.1** (wrapper included)
- **Paper** or Folia, **26.1+** (built against the Paper 26.3 API)

## 🔨 Build
Clone and build a distributable shaded JAR:

```bash
git clone https://github.com/AbstractMenus/minecraft-plugin.git AbstractMenus
cd AbstractMenus
./gradlew shadowJar
```

Output: `build/dist/AbstractMenus-<version>.jar` (the aggregator also drops the
api jar + sources + javadoc next to it; per-module jars stay in
`plugin/build/libs/` and `api/build/libs/`).

Other useful tasks:

```bash
./gradlew build                         # compile + tests
./gradlew test                          # JUnit 6 + Mockito tests
./gradlew test --tests "ru.abstractmenus.util.TestStringUtil"
./gradlew jmh -Pbench=PlaceholderReplace # JMH micro-benchmarks
```

## 🚀 Running locally
Recommended soft-dep matrix for a realistic dev server:

| Plugin | Why |
|---|---|
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | `%player_*%`, `%server_*%`, `%vault_*%` resolution |
| [Vault](https://www.spigotmc.org/resources/vault.34315/) + an economy plugin (e.g. EssentialsX) | `%vault_eco_balance%`, economy-backed rules |
| [LuckPerms](https://luckperms.net/) | `rules { group: "..." }`, `%luckperms_*%` |

None are hard dependencies — the plugin starts on a clean Paper server, and
missing hooks simply short-circuit.

Drop the shaded JAR into `plugins/`, start the server once to generate the
menu folder (`plugins/AbstractMenus/menus/`), then edit the sample configs or
copy from `src/main/resources/bench-menus/` for a ready workload.

## 🧩 Architecture at a glance

```
AbstractMenus (JavaPlugin)
 ├─ MainConfig                 → HOCON config.conf
 ├─ Core services (onEnable order-sensitive)
 │   ├─ VariableManager        → vars + SQLite persistence
 │   ├─ BungeeManager          → cross-server plugin messages
 │   ├─ MenuManager            → open/close/update loop (20 TPS timer)
 │   ├─ HeadAnimManager        → head-animation ticking
 │   └─ ProfileStorage         → player skin profile cache
 ├─ Handlers (Vault/LuckPerms/PAPI/SkinsRestorer/...)  → facade
 ├─ Type registry
 │   ├─ ItemProps              → ~25 properties (material, name, lore, ...)
 │   ├─ Activators             → ~15 activators (command, block, region, ...)
 │   ├─ MenuActions            → ~50 actions (openMenu, giveMoney, setVar, ...)
 │   ├─ MenuRules              → ~25 rules (permission, level, if, and/or, ...)
 │   └─ Catalogs               → players / entities / worlds / iterator / slice
 ├─ Menu hierarchy
 │   └─ AbstractMenu
 │        ├─ SimpleMenu        → fixed-slot GUI
 │        ├─ AnimatedMenu      → frame-based animation
 │        └─ GeneratedMenu     → matrix layout over a catalog, paginated
 └─ Listeners                  → InventoryListener, PlayerListener, ...
```

More detail on the rendering pipeline, caches, and performance trade-offs
lives in [`PR_DESCRIPTION.md`](PR_DESCRIPTION.md).

## 📊 Benchmarks
A pack of 10 reference menus (`src/main/resources/bench-menus/`) stresses
every hot path (static / dynamic / animated / paginated / skull-heavy /
rule-heavy / action-heavy / high-refresh / MiniMessage-heavy / variables).
The pack is accompanied by its own
[README](src/main/resources/bench-menus/README.md) with install steps and a
Spark profiler workflow.

JMH micro-benchmarks live under `src/test/java/ru/abstractmenus/bench/`,
covering: placeholder replacement, string splitting, slot containment, item
build, and concurrent map access.

```bash
./gradlew jmh                       # run all benchmarks
./gradlew jmh -Pbench=ItemBuild     # filter by name
```

## 🧪 Tests
Unit and integration tests under `plugin/src/test/java/`, covering pure-logic utilities, the HOCON type deserializers, the copy-on-write item contract, the MiniMessage legacy color path, and Mockito-based regression pinning for Bukkit API fixes.

```bash
./gradlew test
```

MockBukkit 26.3 integration tests verify that the plugin enables, initializes its menu manager, and disables cleanly.

## 👪 Contributions and feedback
If you have suggestions for improvement or want to report a bug, feel free to create an issue or a pull request in the project repository. The codebase follows standard Java style, uses Lombok for boilerplate, and targets Java 25 (pattern matching, records, switch expressions welcome).

## 🙌 Contributors

The plugin is developed by:

- [@Nanit](https://github.com/Nan1t) — original author
- [@BrainRTP](https://github.com/BrainRTP) — maintainer, performance work
- [@WhyZerVellasskx](https://github.com/WhyZerVellasskx) — contributor
  (modern MiniMessage/colorize path, keyed enchantments, PAPI tag
  resolver, 1.21.4 support, CI / release automation, and more)

See the [full contributor graph on GitHub](https://github.com/AbstractMenus/minecraft-plugin/graphs/contributors)
for everyone who has ever touched the code.

## 📜 Licence
This project is distributed under the **MIT** licence. You are free to use,
modify, and redistribute this code under the terms of the licence.
