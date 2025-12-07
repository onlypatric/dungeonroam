# TODO

## Core RPG Mode setup
- [x] Define `config.yml` entries (`rpgWorlds`, `useWorldGuard`, environment overrides) and ensure defaults cover multi-world support plus optional safe/danger tags.
- [x] Detect world entry/exit via `PlayerChangedWorldEvent`/`PlayerTeleportEvent` and gate RPG behavior to configured worlds.
- [x] Provide toggles for disabling vanilla systems (mob spawning, PvP) inside RPG worlds and document how servers should align paper/server settings.
- [x] Consolidate core services so each test harness or integration run can reuse the same bootstrapping helpers (plugin loader, mock vault, registry patching) without duplicating setup code.
- [x] Outline the runtime responsibilities within the core module:
  - Inventory swap service persists both normal and RPG inventories per player/world, capturing armor/offhand/enderchest states before transitions.
  - Player data manager handles YAML-backed persistence (`playerdata.yml`) and ensures async flushes happen through Bukkit schedulers.
  - Kit, economy, and shop managers are responsible for exposing context-aware commands and GUI entry points, even when the actual GUI is not yet implemented.
  - Core listeners respond to world changes, teleport events, and optional region switches to keep inventories and RPG state consistent.
- [x] Document configuration/command expectations in TODO so the next round of work can reference the desired command tree, economy hooks, and safe-zone flags.
  - Core `/rpg` command currently exposes `reload`, `builder`, `shop`, `stats`, and `where`; only `reload` and `where` perform actions, and builder/shop/stats remain placeholders for upcoming UI-driven features.
  - `config.yml` now carries `vanillaOverrides.disableVanillaMobSpawns` and `vanillaOverrides.enforceNoPvp` flags alongside world lists and safe-region placeholders, forming the baseline for safe/danger zoning once WorldGuard hooks arrive.

## Inventory & Persistence
- [ ] Save a player’s original inventory/armor/offhand/ender chest when they enter an RPG world, storing data per player UUID + world.
- [ ] Load/assign the player’s RPG inventory (or starter kit) when they arrive, applying armor/offhand slots.
- [ ] Persist RPG inventory state on exit, quit, reload, or shutdown and restore the original inventory when leaving the RPG world.
- [ ] Introduce flag-controlled region-based swaps (safe vs danger zones) for WorldGuard-backed safe hubs, including Ender Chest separation and optional region inventory caches.
- [ ] Handle logout/crash edge cases (restore data on quit, reapply inventory on join, prevent rollback/duplication).
- [ ] Serialize inventory data to YAML/JSON, keeping ability to flush caches async then resync on the main thread.
- [ ] Track per-item metadata (inventory origins, assigned kits, cooldown tags) via Item-NBT-API when storing/loading to avoid losing ability references during serialization.

## Kit Builder GUI
- [ ] Create `/rpg builder` command (permission gated) that opens a chest-style GUI reflecting player slots plus buttons for save/help.
- [ ] Implement item selection menus for each slot, allow selecting, removing, or swapping items, and auto-save kit data on every interaction.
- [ ] Decide whether to allow editing only in safe zones or via specific items/locations and enforce that restriction.
- [ ] Persist player kits (per UUID) and use them to populate inventory on first entry or reset condition based on config (configurable reset modes).
- [ ] Respect item permissions and ExecutableItems access checks before showing or granting items through the builder.

## Custom Items & Abilities
- [ ] Load admin-defined items from `items.yml`, including name, material, lore, enchantments, attributes, abilities, and stack size.
- [ ] Implement ability handlers (e.g., slow_on_hit, ignite_on_hit, consume_heal, lightning_on_hit) by listening to combat/use events and applying effects with chance/cooldown support.
- [ ] Add an `executable` flag path that, when true, fetches compiled items from ExecutableItems by ID (with permission checks).
- [ ] Categorize each item for kit/shop/drop availability and document flags like `obtainable_in_kit`, `obtainable_in_shop`, and `drop_from`.
- [ ] Ensure ability logic defaults to other plugins when ExecutableItems provides the item.
- [ ] Use Item-NBT-API to persist ability/cooldown tags and custom identifiers so we can detect special gear after player deaths, drops, or configuration reloads.

## Economy & Shop
- [ ] Hook into Vault (primary) and XConomy (fallback/soft dependency) to manage depositing/withdrawing coins; fall back to an internal economy if neither is found.
- [ ] Track player balances per UUID (or via a dedicated file) and expose commands like `/rpgmoney` when using the internal economy.
- [ ] Implement NPC-based shop (Citizens or in-house villager) with configurable name and location that opens a GUI.
- [ ] Design a paginated/sectioned shop GUI, display prices, build items via custom item logic, and handle inventory overflow.
- [ ] Deduct currency on purchase, fire a custom event, and optionally play feedback (sound, title) for success/failure.

## Custom Mobs & Bosses
- [ ] Load mob/boss definitions (`mobs.yml`/`bosses` section) with stats (health, damage, speed, armor), equipment, and drops.
- [ ] Apply equipment drops/gear, suppress undesired vanilla drops, and reward custom loot (items, money, XP) on death.
- [ ] Spawn mobs via vanilla spawn overrides or a custom spawner (configurable weights, groups, region restrictions).
- [ ] Add boss abilities (summon minions, ground slam, rage phases) via schedulers/events and announce when triggered.
- [ ] Give bosses respawn timers/triggers (region entry) and broadcast accordingly, ensuring players joining mid-fight don’t break logic.
- [ ] Ensure multi-player loot: define final-hit or shared reward policies, log boss spawns/kills for debugging.

## WorldGuard & Safe-Zone Integration
- [ ] Detect WorldGuard presence (soft-dependency) and query region membership on movement to determine safe/danger zones.
- [ ] Enforce safe-zone rules: disable mob spawning, PvP, and unsafe commands while within flagged regions.
- [ ] Optionally switch inventories when crossing safe-region boundaries when `regionInventorySwitch` is enabled.
- [ ] Document fallback behavior when WG is missing (e.g., global danger, optional cube-based safe zone config).

## Commands & Permissions
- [ ] Register `/rpg builder`, `/rpg shop`, `/rpg stats`, `/rpg spawn <mob|boss>`, `/rpg setshop`, and `/rpg reload` commands with appropriate permission nodes.
- [ ] Provide broad `openworldrpg.use`, builder/shop toggles, `openworldrpg.bypass`, and `openworldrpg.admin.*` permissions.
- [ ] Ensure teleport events and direct server transfers respect inventory rules unless bypassed.

## Data Files & Logging
- [ ] Define file layout for `playerdata/<uuid>.yml`, `items.yml`, `mobs.yml`, `shop.yml`, and optional `kits.yml`.
- [ ] Log major lifecycle events (inventory swaps, purchases, boss spawns) in debug mode.
- [ ] Guard file I/O with async scheduling, catch serialization errors, and warn/admin-notify on issues.

## Edge Cases & Testing
- [ ] Prevent duplicate transitions (e.g., teleport + portal) by locking per-player swap operations or queuing actions.
- [ ] Handle death/respawn logic so RPG items aren’t restored incorrectly after a non-RPG respawn.
- [ ] Mitigate exploits (shulkers, Ender chests) by ensuring inventories are globally swapped and optionally blocking container placement in RPG worlds.
- [ ] Validate scenarios: moving items between worlds, combat with multiple players, kit persistence, economy/shop flows, and optional dependencies being present/absent.

## Example Config/Demos
- [ ] Populate TODO references with full working configs (worlds, items, mobs, shop) so admins can copy/paste working examples during testing.
- [ ] Update documentation to describe how to add new items, mobs, shops, and safe regions.
