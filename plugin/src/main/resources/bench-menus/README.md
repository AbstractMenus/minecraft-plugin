# Bench Menus

Reference menu configurations used as a perf benchmark. Each file targets a
specific code path so before/after measurements (Spark profiler, JMH, GC logs)
have a stable workload to attach to.

## Install

These files are NOT auto-extracted by the plugin (only `menu.conf` and
`menu_anim.conf` are). Copy them into the live menu folder:

```bash
mkdir -p plugins/AbstractMenus/menus/bench
cp src/main/resources/bench-menus/* plugins/AbstractMenus/menus/bench/
mv plugins/AbstractMenus/menus/bench/_templates.conf plugins/AbstractMenus/menus/_templates.conf
```

`_templates.conf` lives at the menus root because the bench files include it
via `./plugins/AbstractMenus/menus/_templates.conf`.

## Bench files

| File | Open command | What it stresses |
|------|--------------|------------------|
| `bench_static_grid.conf` | `/am open bench_static_grid` | 54-slot fully-static menu — exercises PropName/PropLore pre-compute (zero MiniMessage parses on refresh after first build) |
| `bench_dynamic_lore.conf` | `/am open bench_dynamic_lore` | Heavy `%placeholder%` usage in name + lore — exercises the dynamic Handlers.replace + parseToLegacy path |
| `bench_animated_carousel.conf` | `/am open bench_animated_carousel` | 8-frame loop animation, delay=1 — exercises AnimatedMenu/Frame.play (verifies the double-build fix) |
| `bench_generated_pagination.conf` | `/am open bench_generated_pagination` | GeneratedMenu over the online players catalog with paging — exercises catalog snapshot + matrix layout |
| `bench_skull_grid.conf` | `/am open bench_skull_grid` | Mix of static texture skulls + live player skins — exercises Skulls.skullCache + Skulls.playerSkullCache |
| `bench_complex_rules.conf` | `/am open bench_complex_rules` | Items gated by permission/level/money/world/and/or/oneof/chance/if rules — exercises rule dispatch |
| `bench_actions_rich.conf` | `/am open bench_actions_rich` | Multi-step click chains, randActions, bulk, setProperty (CoW), variable mutations — exercises Actions.activate path |
| `bench_high_refresh.conf` | `/am open bench_high_refresh` | 9-slot menu with `updateInterval: 1` — exercises MenuManager.UpdateTask end-to-end at every tick |
| `bench_minimessage_heavy.conf` | `/am open bench_minimessage_heavy` | Heavy mix of legacy color codes + MiniMessage tags — exercises the LegacyColorTagReplacer rewrite |
| `bench_variables.conf` | `/am open bench_variables` | Global + personal variable reads/writes — exercises VariableManagerImpl + cache |

## Suggested workflow

1. Open enough alt accounts (or use a stress-test client) to reach 30–50
   simultaneous menus.
2. `/spark profiler open --thread "main"` for a few minutes.
3. Open the `bench_*` menus across players in proportion to your real
   usage. For pure stress, bench D, E, H, I are the most active.
4. Check `Spark > Bottom-up > findHottest` — frames in
   `MenuManager.UpdateTask`, `SimpleMenu.placeItems`, `SimpleItem.applyProperties`,
   `MiniMessageUtil$ActiveReplacer.parseToLegacy` indicate where time goes.
5. Compare against the baseline measurement to verify the perf wave is
   actually paying off in your environment (player count, plugin mix).

## Notes

- No BungeeCord placeholders or actions — the bench is single-server.
- The grids assume a vanilla server with `world` as the default world; tweak
  the `RuleWorld` entries in `bench_complex_rules.conf` if your overworld
  is named differently.
- `bench_dynamic_lore.conf` and `bench_high_refresh.conf` use placeholders
  that PlaceholderAPI typically expands. Without PAPI installed they will
  render as raw `%name%` strings, which is fine for measuring the plugin's
  own dispatch cost but misses the PAPI hop.
