# AbstractMenus

<a href="https://github.com/AbstractMenus/minecraft-plugin/blob/master/LICENSE"><img src="https://img.shields.io/badge/License-MIT-red.svg" alt="license"/></a>
<a href="https://github.com/AbstractMenus/minecraft-plugin/releases"><img src="https://img.shields.io/badge/version-2.0.0--alpha.2-blue" alt="version"/></a>
<img src="https://img.shields.io/badge/minecraft-1.20.6+-brightgreen" alt="minecraft"/>
<img src="https://img.shields.io/badge/paper-1.21.11-brightgreen" alt="paper"/>
<img src="https://img.shields.io/badge/java-21-orange" alt="java"/>
<img src="https://img.shields.io/badge/folia-supported-8a2be2" alt="folia"/>

AbstractMenus is a GUI plugin for Paper / Folia Minecraft servers that lets
server owners and developers build inventory-based menus declaratively through
HOCON configuration. Menus, rules, actions, activators, and item properties
are all first-class, extensible via an open type registry, and render through
a cached, MiniMessage-aware pipeline.

The plugin supports **Minecraft 1.20.6 onwards**. Older versions were dropped
to keep the codebase free of backwards-compatibility wrappers and to track the
current Paper API surface.

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

## 🔗 Links
- [SpigotMC](https://www.spigotmc.org/resources/abstract-menus-an-advanced-gui-plugin.75107/)
- [Discord](https://discord.gg/kt4P9Cgw)
- [Documentation](https://abstractmenus.github.io/docs/index.html)

## 🧰 Requirements
- **JDK 21+** (target and source)
- **Gradle 9.x** (wrapper included)
- **Paper** or Folia, **1.20.6 – 1.21.11+** (built against 1.21.11 dev bundle)

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
./gradlew test                          # JUnit 5 + Mockito tests
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
~190 unit and integration tests under `src/test/java/`, covering pure-logic
utilities, the HOCON type deserializers, the copy-on-write item contract, the
MiniMessage legacy color path, and Mockito-based regression pinning for
Bukkit API fixes.

```bash
./gradlew test
```

MockBukkit-backed integration tests are scaffolded but currently `@Disabled`
due to an upstream MockBukkit ↔ Paper 1.21.11 registry-bootstrap collision.

## 👪 Contributions and feedback
If you have suggestions for improvement or want to report a bug, feel free
to create an issue or a pull request in the project repository. The
codebase follows standard Java style, uses Lombok for boilerplate, and
targets Java 21 (pattern matching, records, switch expressions welcome).

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
