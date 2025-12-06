# Open World RPG Plugin – Design Plan
## Overview

This plan outlines a Paper 1.21.8 Minecraft plugin that creates an open-world RPG experience in designated worlds. The plugin will manage separate inventories for RPG worlds, provide a kit builder for custom loadouts, integrate with economy plugins for an item shop, and introduce custom mobs/bosses.

Key features include:

- **Multi-World RPG Mode**: RPG features only run in configured worlds (e.g., `config.yml::rpgWorlds`), leaving other worlds for vanilla gameplay.
- **Per-World Inventory Management**: The plugin swaps between a player’s normal inventory and their RPG inventory when entering or leaving an RPG world, with optional WorldGuard safe/danger zones to add finer inventory isolation similar to established multi-world inventory plugins ([SpigotMC](https://www.spigotmc.org)).
- **Kit Builder GUI**: Players customize loadouts through an in-game GUI, with the option to leverage [ExecutableItems](https://docs.ssomar.com) as a soft dependency for advanced item abilities.
- **Custom Items & Abilities**: Admins define items with unique names, lore, enchantments, and special effects in config, or defer to ExecutableItems for rich behavior while reusing the plugin’s kit/shop system.
- **Economy & Shop System**: Integrates with Vault and XConomy for earning/spending currency, spawns an NPC/GUI shop, and falls back to an internal currency system if no economy plugin is available; rewards come from combat.
- **Custom Mobs and Bosses**: Offers configurable mobs and bosses with custom gear, attributes, and drops, all built atop vanilla entities without extra textures or client mods.
- **Persistence & Safety**: Inventories, kits, player stats, and balances are stored persistently, with logic to handle logging out, region changes, and reloads so RPG items never leak into non-RPG worlds.

## World Selection and Multi-World Support

The plugin will target specific worlds defined in config.yml under a setting like rpgWorlds. Only players in those worlds experience the RPG features, allowing normal gameplay in other worlds. For example:

rpgWorlds: ["RPG_World1", "DungeonRealm"] in config would enable RPG mode in RPG_World1 and DungeonRealm while other worlds remain unaffected.

World Entry/Exit Detection: Use the Bukkit PlayerChangedWorldEvent to detect when a player moves between worlds. If the target world is an RPG world (or leaving one), trigger inventory and mode switches accordingly.

Grouping or Separate Inventories: By default, each listed RPG world will have its own separate inventory for players. This means if there are multiple RPG worlds, the player will maintain distinct gear/progress in each (similar to how multi-world inventory plugins work
[SpigotMC](https://www.spigotmc.org)
). (Optionally, the config could allow grouping certain worlds to share inventories, but by default isolation is safer to prevent cross-world item exploits.)

Environment Settings: In RPG worlds, you may want to disable certain vanilla mechanics (if desired) – for example, turning off natural mob spawning if using custom spawn systems, or adjusting difficulty. The plugin can provide config options or rely on server settings for each world (e.g. spawn-monsters: false in paper.yml for those worlds if we fully control spawning via the plugin).

## Inventory Management and Data Persistence

One of the core functionalities is swapping player inventories when they transition in or out of RPG worlds, and preserving those states. The system will:

Save Original Inventory on Entry: When a player is about to enter an RPG world, the plugin saves their current inventory (and optionally armor, offhand, XP, and ender chest contents) to a data store. This data can be kept in a per-player file (e.g. plugins/OpenWorldRPG/playerdata/<uuid>.yml) under a section for the originating world. This ensures we remember what they had before entering the RPG world
[SpigotMC](https://www.spigotmc.org)
.

Load/Assign RPG Inventory: If the player has visited this RPG world before, load their last known RPG inventory from storage. If it’s their first time or if a default kit is defined, initialize their inventory with the starter kit (see Kit Builder section) or leave it empty for them to fill via the kit builder. The player’s inventory is then replaced with this RPG inventory as they teleport into the world. Armor slots and offhand are also managed.

Restoring Inventory on Exit: When leaving an RPG world (teleporting to a non-RPG world), the plugin saves the player’s current RPG inventory state back to storage and then restores their original inventory that was saved on entry. This way, they get all their normal items back immediately on arriving in a safe/non-RPG world
[SpigotMC](https://www.spigotmc.org)
. All RPG items are kept separate and cannot be taken out.

Handling Logouts and Crashes: If a player disconnects while in an RPG world, the plugin will treat it similarly to leaving the world:

On PlayerQuitEvent: if the player is in an RPG world, save their RPG inventory and optionally restore their original inventory data to the player entity before they actually quit. This ensures that if they log back in (potentially into a lobby world), they won’t accidentally log in with RPG items. In case the server allows relogging at the same location, we might instead choose to not give original items on quit but rather simply save RPG state; then on PlayerJoinEvent, check if they are in an RPG world and reapply the RPG inventory. This detail can be configurable or based on server behavior. The goal is to prevent any item crossover even in edge cases.

On server reload or shutdown, ensure all online players’ current inventories are properly saved to the correct world slots to avoid rollback or duplication. The plugin will flush the inventory data cache to disk on disable.

Region-Based Inventory (Safe Zones): (Advanced/Optional feature) If WorldGuard is present and certain regions are defined as safe zones (e.g. towns) within an RPG world, the plugin can also swap inventories when players cross these region boundaries:

Danger Zone Inventory vs. Safe Zone Inventory: Admin can configure region names (or set a custom WG flag) for “safe” areas. When a player enters a safe region from a dangerous area, the plugin could temporarily store their combat inventory and give them either a safe-zone inventory or even their “original” inventory from outside world (depending on design). When they go back into the wild, their combat kit is restored. This ensures players aren’t running around towns with swords drawn, and also they cannot bring loot from the wild directly into the safe zone without control.

For simplicity, we might designate that inside safe zones, the player’s inventory is mostly empty or contains only non-combat items (or perhaps the original inventory from the main world if that makes sense in the server’s context). Implementing this means treating the safe zone like a mini-world in terms of inventory separation – conceptually similar to how we handle world switching
[SpigotMC](https://www.spigotmc.org)
.

All region-based swaps will also be saved persistently. If a player logs out inside a safe zone, the plugin should remember what items they had in the safe zone vs. what they left in the wild, and restore appropriately on login or region re-entry. (This feature can be toggled if not needed, as it adds complexity.)

Ender Chest and Other Storage: To prevent item leakage, the plugin will intercept interactions with Ender Chests (and possibly shulker boxes if necessary) in RPG worlds. By default, the Ender Chest should likely be separated per world just like inventories, or simply disabled in RPG worlds. Otherwise, a player could store RPG items in an Ender Chest and retrieve them in a non-RPG world. The simplest approach is to maintain a separate Ender Chest inventory for RPG worlds (saved with the player’s data) or block cross-world EnderChest usage. This will be clearly documented and configurable.

Player States: Optionally, the plugin can also handle other player state aspects if desired – e.g., health, hunger, potion effects – so that when switching between RPG and normal worlds, they don’t bring a high health or potion effects with them. For now, we focus on inventory and leave health/hunger as is, since the worlds are separate game modes. (Admins can configure keep-inventory gamerules in RPG worlds if they prefer to avoid dropping items on death, for example.)

All inventory data will be saved in a persistent format (YAML or JSON) keyed by player UUID and world. We will use safe serialization (including support for item meta, enchantments, etc.). Minecraft/Paper provides ItemStack serialization to YAML by default, which we can utilize for simplicity. Data example:

```yaml
# plugins/OpenWorldRPG/playerdata/<player uuid>.yml
RPG_World1:
  inventory: <serialized item list>
  armor: <serialized armor items>
  enderchest: <serialized enderchest items>
DungeonRealm:
  inventory: <...>
  armor: <...>
  enderchest: <...>
originalWorld: world  # last non-RPG world name saved, for reference
originalInventory: <serialized items from before entering any RPG world>
```

(The above is conceptual; actual format may differ. Each time a player transitions, we update these records.)

## Kit Builder System

The Kit Builder allows players to set up and customize the loadout they will use in RPG worlds. This is implemented via an interactive GUI and associated commands:

Accessing Kit Builder: Players can open the kit builder menu with a command (e.g. /rpg builder). This command can have permission requirements if we want to restrict it to certain ranks or only allow usage in safe zones (recommended so players aren’t editing kits mid-combat). Alternatively, an NPC or an item (like a “Kit Editor” book) in safe towns could open the GUI.

Builder GUI Layout: The GUI will mimic a full player inventory (typically 36 slots + 9 hotbar, and possibly armor slots displayed). However, since a chest GUI can’t show the armor/off-hand directly, we might handle armor via special slots or an alternate GUI. For simplicity, the first iteration will focus on the main inventory slots. The GUI will be a Chest-type inventory of size 54 (to also include some control buttons and possibly armor):

Slots 0-35 correspond to the player’s inventory hotbar+main slots.

We can use the bottom row (slots 45-53) for special buttons: e.g. a “Save & Exit” button, “Next Page”/“Previous Page” if needed, or a help/info item. Armor could be represented by icons here as well (e.g. an icon to set helmet, etc., which when clicked opens a sub-GUI for armor selection).

Selecting Items: Each slot in the kit builder initially shows either the item currently assigned to that slot in the kit or a placeholder (e.g. a Barrier block named “Empty Slot”). When a player clicks an empty slot, the plugin opens an Item Selection GUI. This selection menu will list all allowed items that can be put into kits, drawn from the configurable item list (see Custom Items below). We can categorize items in this menu with filter tabs (swords, armor, consumables, etc.) or just paginate through all items. For usability, we might implement categories as separate GUIs accessible via icons (e.g. click a diamond sword icon to view all weapons).

Adding an Item: In the item selection menu, clicking on an item will assign it to the previously clicked kit slot. The player is then returned to the main kit builder GUI, and that slot now displays the chosen item (with its custom name/lore). If the item has quantity (like potions), we could allow the player to specify an amount (possibly by repeated clicks or a prompt) – though by default, equipment will be 1 each, consumables could have a stack count set by config default.

Removing/Changing Items: If a player clicks an occupied slot in the kit GUI, we can either remove the item (return to empty) or open the selection menu again to choose a replacement. Perhaps left-click to change, right-click to remove – this can be indicated in the GUI item lore.

Saving the Kit: The kit is auto-saved as the player makes changes (every click can immediately update their saved kit data). However, to avoid confusion, we will also provide a “Save & Close” button in the GUI to finalize changes. When the player closes the GUI (manually or via this button), their kit configuration is stored persistently (e.g. in playerdata/<uuid>.yml or a separate kits.yml). This saved kit will be used to give them items when they enter the RPG world (if they have no existing RPG inventory yet or if the server resets kits each entry – depending on server config).

Using Kits In-Game: When a player enters an RPG world for the first time (or any time their RPG inventory is empty/reset), the plugin can populate their inventory with the saved kit items. If the server intends kits to be just a starting loadout and then players keep whatever they acquire afterwards, we will only apply the kit on first entry or on some reset condition (not every time they enter if they have existing items). This behavior can be configurable:

By default: The kit acts as a starting loadout. Players will keep any items they pick up in RPG world until they leave. If they exit and re-enter later, they continue with whatever they had (persistent inventory).

Optional mode: Always reset to the saved kit on each entry (like a fixed kit per visit). This is less RPG-like (since it would erase progress), so likely the default is persistence. But an admin could enable a “Reset kit on each entry” mode for a roguelike experience.

Multiple Kits or Profiles: Not in initial scope, but in future the plugin could allow players to save multiple kit profiles (e.g. “Knight build”, “Archer build”) and choose one when entering the RPG world or at a loadout station. For now, one kit per player is sufficient unless otherwise requested.

Permissions & Restrictions: The kit builder will respect any item permission or availability rules:

For example, if certain powerful items should only be usable by certain ranks, the plugin can check permission before allowing it in the kit (and either hide it from the selection menu or show it but with a lock icon and not allow pick).

If ExecutableItems is integrated, we might also check if the player has access to that ExecutableItem (some servers use permissions for certain custom items). ExecutableItems API provides a permission check for items
[docs.ssomar.com](https://docs.ssomar.com)
.

It’s also wise to disable kit editing while in combat or in dangerous areas to prevent abuse (this can be enforced by only allowing the command in safe zones or when no hostile mobs are nearby).

The Kit Builder’s configuration (what items are available, any slot limitations, etc.) will be defined by the admin. Below in ## Example Configurations we illustrate how items and default kits can be configured.

## Custom Items and Abilities

A rich RPG experience requires custom items with unique lore and effects. Our plugin will support two levels of custom items:

Built-in Custom Items: Defined via the plugin’s config files, these include custom names, lore text, enchantments, attributes, and simple special abilities. For example, a sword that poisons enemies or an armor piece that increases max health. We will provide a set of common effects that can be toggled via config.

ExecutableItems Integration: If the server has the ExecutableItems (EI) plugin installed (detected via soft-dependency), our plugin will integrate with it to take advantage of its powerful item system
[SpigotMC](https://www.spigotmc.org)
. EI allows fully customizable items with “activators” (triggers for abilities on click, hit, etc.), so rather than reinventing complex abilities, we can let admins use EI to create items and simply reference them in our kit/shop configs. If an item in our config is marked as coming from EI, we will use EI’s API to give that item to players
[docs.ssomar.com](https://docs.ssomar.com)
.

### Key details for custom items

Configuring Items: Admins define items in an items.yml (or a section of config.yml). Each item has a unique ID/key name. For example: sword_of_ice, healing_potion, flame_bow, etc. Under each item, various properties can be set:

name: The display name (color codes supported).

material: Base Minecraft material (e.g. DIAMOND_SWORD).

lore: A list of lore lines (supports color, and can describe effects or story flavor).

enchantments: List of enchantments to apply (with levels), if any.

attributes: Custom attribute modifiers (like +10% speed or +5 attack damage) if needed – though these can also be achieved via enchantments or potion effects.

amount: Default stack size (for consumables like arrows or potions).

abilities: Special effects/behaviors tied to the item. We will support a set of predefined ability types, such as:

On-Hit Effects: e.g. apply Slowness 2 for 3 seconds to target (slow_on_hit: 3s), ignite target (ignite_on_hit: 4s), lifesteal (lifesteal_on_hit: 20% to heal player for 20% of damage dealt), etc.

On-Use/Right-Click: e.g. a teleportation orb that when right-clicked teleports the player a short distance (teleport_on_use: 5 blocks), or a healing potion that gives regeneration effect when consumed (though if it’s a consumable we could just use a standard potion).

Passive: e.g. wearing a “Flame Chestplate” might give the player fire resistance (passive_effect: FIRE_RESISTANCE) as long as worn.
These abilities would be implemented in our code (listening to events like EntityDamageByEntity, PlayerInteract, etc., and checking if the player’s item has that ability configured). We will keep this system somewhat simple to avoid needing an entire scripting engine – for complex abilities, we rely on ExecutableItems.

executableItem: Instead of the above details, an item entry can simply specify it’s provided by ExecutableItems plugin. For example: executable: true and an ei_id: MyCustomSword. In this case, the plugin will not apply its own lore or enchants (it will defer to the EI item definition); it will use the EI API to fetch the item stack. This allows server owners to use the in-game EI editor to create fancy items, and just plug them into our system by ID. (If EI is not present or the item ID not found, the plugin will log a warning and skip that item.)

Examples: (See Example Configurations for a sample items.yml snippet defining a custom sword, an armor, and a consumable.)

Balance & Restrictions: Admins have full control over what items are available in the RPG world via the kit builder and shops. If certain items should only be obtainable through progression (like a boss drop or purchase), the admin simply wouldn’t list it in the kit builder’s allowed items. Our plugin can support that by having separate lists for “kit builder items” vs “shop items” vs “mob drops” if needed. For simplicity, we might use one unified list and just mark or separate where each can appear:

e.g. an item config could have flags like obtainable_in_kit: true/false, obtainable_in_shop: true/false, drop_from: [undead_king] etc. This ensures some items (like powerful ones) can’t be freely chosen in kits and must be earned.

However, by default we expect the kit builder list to be all items the admin configures, and they will exercise judgement in not including overpowered items as free kit options.

ExecutableItems abilities: When using EI items, all ability logic is handled by that plugin (e.g. if the item casts a spell on right-click, etc.). Our integration is mainly giving the item and perhaps checking permission with hasItemPerm as needed
[docs.ssomar.com](https://docs.ssomar.com)
. We will note in documentation that to create/edit those items the admin should use the EI plugin’s commands (e.g. /ei editor).

## Economy Integration and Item Shop

Economy: The plugin will integrate with server economies to allow players to earn and spend money within the RPG world. We will support:

Vault API (Primary): Vault is the standard interface for economy plugins
[dev.bukkit.org](https://dev.bukkit.org)
. If Vault is installed, our plugin will use it to interact with whatever economy is hooked (be it EssentialsX Economy, XConomy, etc.). For example, when a player earns money from killing a mob, we’ll call something like economy.depositPlayer(player, amount). When they purchase an item from the shop, we check economy.has(player, price) and then withdraw the cost. Vault ensures compatibility with many economy plugins seamlessly.

XConomy (Soft-Dependency): XConomy itself is Vault-compatible
[SpigotMC](https://www.spigotmc.org)
, meaning if Vault is present, XConomy will be accessed through it. If for some reason Vault is not present but XConomy is, we can attempt to hook XConomy’s API directly. XConomy’s API appears to store balances and has commands; since XConomy requires Vault by design, this scenario is unlikely, but our plugin will detect if the XConomy plugin is loaded and try to use its public methods if available (or log a warning to install Vault).

Internal Economy Fallback: If neither Vault nor XConomy is installed, the plugin will fall back to a simple built-in currency system. In this mode, we maintain a balance for each player in a YAML or database. Players can still earn money from mobs and spend at the shop, but this currency will be separate from any server-wide currency. We will provide basic commands like /rpgmoney to check balance, but it won’t integrate with other plugins. (This is mainly to ensure the RPG features are playable even without an economy plugin.)

Earning Money: Players will earn currency primarily by fighting mobs in RPG worlds:

Each custom mob can have a money drop defined (either a fixed amount or random range). When that mob dies, the plugin credits the killer’s account accordingly. Example: a “Zombie Knight” might give 5-10 coins, while a boss could reward 100 coins.

We might also allow other ways to earn: quests or events are possible future additions, but not in this basic scope.

The plugin should ensure money earned is stored in the correct system (Vault/other). If using internal, update our stored balance.

## Item Shop (NPC and GUI)

The RPG world will have a shop where players can spend their hard-earned currency on items (weapons, armor, consumables, etc.). Details:

Shop NPC: We’ll implement the shop as an NPC that players can interact with to open a GUI. There are two ways to handle the NPC:

Via External NPC Plugin: If Citizens or a similar NPC plugin is available (could be a soft-dependency), the admin can create an NPC and assign it the “RPGShop” trait from our plugin. That NPC, when right-clicked, will trigger our shop GUI to open. We can also provide a command like /rpg shopnpc create to spawn a preconfigured villager NPC at the player’s location as a shop (using internal code if Citizens not present).

Internal Entity: Without any external NPC plugin, we can spawn a vanilla entity (e.g. a Villager or custom Villager-like entity) at configured coordinates. The plugin will keep this entity from wandering (perhaps by constantly setting its position or giving it NoAI). When a player right-clicks this entity, we cancel any default trading and instead open our GUI. We can mark it with a custom name (e.g. “§aItem Shop”) and perhaps glowing effect to distinguish it. This approach ensures no extra dependencies, though it might be less flexible than Citizens for moving or styling the NPC.

Shop GUI: Upon interacting with the shop NPC, a chest GUI opens listing items for sale. The items available and their prices are defined in a shop config (could be part of config.yml or a separate shop.yml). Each item display will show the item icon, name, and lore, plus a lore line for price (e.g. “Price: 50 coins”). We will likely make one GUI page per category or have multiple pages if items are numerous:

E.g. Page 1 – Weapons, Page 2 – Armor, Page 3 – Consumables. Or we can have category selectors in the GUI. The exact layout can be configured or auto-generated based on item list size.

Purchasing: When a player clicks an item in the shop GUI, the plugin will attempt the purchase:

Check player’s balance (Vault or internal). If insufficient funds, play a sound and perhaps flash the price in red. If sufficient, deduct the cost and give the item to the player’s inventory (if inventory is full, we can drop it at their feet or refuse the purchase with a message).

If the item is one of our custom items (ID from items.yml), we construct the ItemStack with the specified attributes (or fetch via EI API if applicable). If it’s a vanilla item (the plugin could also allow selling some basic blocks or foods if configured), we just create it directly.

We will also fire a custom event (like RPGPlayerPurchaseEvent) in case other plugins or addons want to listen (e.g. for logging or achievements).

Selling: The plan doesn’t specifically mention selling items back, but we could include an option for the shop to buy items from players (perhaps not all items). This would be more complex (need GUI for player to select what to sell, or detect holding item, etc.), so in this initial plan we assume shop is for buying only. Players acquire money only from combat, not by selling loot (unless configured later).

Price Configuration: Prices for each item are fully configurable. Admin can set some items as very expensive (like powerful gear) and others cheap (consumables). This balancing is up to the server’s economy. If using Vault with a server-wide currency, consider how RPG rewards compare to other ways of earning money on the server (to prevent economy imbalance). If using internal economy, it’s self-contained, so it’s fine.

Multi-Currency Consideration: Vault supports a single currency. XConomy also basically deals in one currency. We won’t complicate with multiple currency types – just assume one currency (coins, gold, whatever — configurable name perhaps). The plugin’s messages can refer to it as “coins” by default, or pick up the name from Vault if available (Vault API can sometimes get currency name). Otherwise, configurable in a messages file.

## Custom Mobs and Bosses

To enrich the RPG worlds, this plugin will introduce custom mobs and bosses that provide challenging combat and thematic flavor. No custom textures or models are used – all mobs will use vanilla Minecraft entities but with modified attributes, equipment, and behaviors to appear unique. They will be defined in config so server owners can tweak stats or add new ones. Key aspects:

Mob Definitions: In mobs.yml, admins can list custom mob types. Each mob has:

ID/Name: A key used internally (e.g. zombie_knight, forest_spider). Also possibly used for spawn references and drops.

Base Entity Type: The underlying Minecraft entity (e.g. ZOMBIE, SKELETON, SPIDER, WITHER_SKELETON, etc.). This determines the fundamental behavior (zombie-type melee vs skeleton ranged, etc.).

Display Name: Custom nameplate for the mob (e.g. "&2Zombie Knight"). We will show this name over their head and in death messages. Typically bosses will have a name and mobs might or might not (maybe we give all custom mobs a name for flavor).

Stats: Custom health, damage, movement speed, etc. We can set these via attributes:

Health: using GenericAttributes.MAX_HEALTH and then actually setting their health to that value on spawn.

Damage: for melee mobs, GenericAttributes.ATTACK_DAMAGE; for others, maybe not directly needed if using vanilla damage, but we can adjust if necessary.

Speed: GenericAttributes.MOVEMENT_SPEED (to make some mobs faster/slower).

Other attributes: armor, knockback resistance, etc., configurable if desired.

Equipment: We can equip mobs with specific gear. For example, give a Zombie Knight a full iron armor set and an iron sword. We specify each slot: helmet, chestplate, leggings, boots, main-hand weapon, off-hand item (if any). The plugin will apply these on mob spawn. We should also consider disabling equipment drop if we don’t want players to always get that gear – by default, the plugin can set the equipment drop chance to 0 for all custom gear unless a drop is explicitly defined in the drops section.

Drops: A list of items or rewards the mob can drop on death. This can include:

Vanilla materials (e.g. "IRON_INGOT: 0.2" meaning 20% chance to drop an iron ingot).

Custom items (referenced by our item IDs, e.g. "health_potion: 0.5" 50% chance for a health potion item).

Money drop: Since money isn’t a physical item, we’ll represent it with a special drop entry like "money: 5-10" meaning give 5 to 10 coins to the killer. (This is how players earn currency as mentioned.) We’ll parse this and not actually drop anything on ground but directly credit the player.

Experience: We can also have an exp: 20 to give extra XP points on death if desired, or we could just use vanilla XP orbs. Possibly we let vanilla XP orbs drop normally unless we want custom control.

Abilities/AI: For standard mobs, we typically rely on their base Minecraft AI. However, we can offer some simple custom behaviors:

Potion Effects: e.g. a mob could have a constant potion effect applied (like a Poisonous Spider might have a poison aura, or a Fast Zombie has Speed II). Config could allow giving the mob certain potion effects on spawn.

Special Skills: For bosses especially, we might implement scripted abilities. For example, a boss could “leap” at the target, “summon minions”, “shoot fireballs”, or “heal itself”. These would require timers or event checks in code:

We can define triggers like every X seconds or at certain health percentages. E.g. abilities: ["summon_minions:zombie_knight,3, every:30s"] meaning every 30 seconds the boss summons 3 Zombie Knight adds. Or `"rage_phase: 20%" meaning when health <20%, boss gains strength or new behavior.

This is complex but we can at least plan for a few common ability types. Initially, to keep things manageable, we might include one example like summoning minions or area knockback.

If using a library helps (for pathfinding or easier custom AI), we will consider it. For instance, there are open-source libraries for custom entity control, but we can likely manage with Bukkit/Paper events and attributes for simpler needs. We explicitly avoid any paid libraries or adding heavy dependencies. All custom AI will be coded or optionally integrated from free sources and shaded into our plugin if needed (so the server owner doesn’t have to add more jars).

Bosses: These are special mobs typically with higher stats and unique drops:

They will be defined similarly in config, perhaps in a separate section bosses: or just flagged with boss: true. Bosses might have a spawn trigger (not spawning as part of normal random spawns). For example, a boss could be set to spawn in a specific location or when a specific condition is met (e.g. a player enters a certain dungeon area). In config we could specify coordinates or region for bosses.

Alternatively, some bosses could spawn randomly but very rarely, or as final waves in an event. Initially, we might provide a command to manually spawn a boss (for testing/admin events) and leave automated boss spawning for later expansion or simple timers. E.g. "A world boss spawns every hour at location X" could be configured.

Bosses will have more abilities than normal mobs. We can script a basic ability cycle for each boss as needed (via our code reading the config for abilities).

Ensure bosses have a visible health bar if possible (we could integrate with a bossbar API or simply broadcast boss health in the mob name using the team health display trick, or require an external boss bar plugin – but that may be out of scope for now).

Mob Spawning Control: We have to decide how custom mobs appear in the world:

We can override natural spawns: e.g. listen to CreatureSpawnEvent in the RPG world and for each spawn, decide if we want to replace a vanilla mob with a custom one. For instance, if a Zombie spawns naturally, we could cancel it and instead spawn our zombie_knight with some probability. This is a quick way to integrate with the normal spawning system while injecting custom mobs. We’d likely turn down vanilla spawn rates and handle spawn replacement logic carefully (to avoid infinite loops of spawn events).

Alternatively, custom spawn system: disable all vanilla spawning in RPG worlds (peaceful or via gamerule) and have our own task that spawns mobs around players. For example, every X seconds, for each player (or each chunk), if the mob count is below a threshold, spawn a random custom mob based on a configured table of spawn chances. This approach is more deterministic and gives full control (like MythicMobs does). It’s a bigger implementation but offers better scaling control. Given time constraints, we might do a hybrid: keep vanilla spawning for ambient mobs, but allow config to e.g. replace 100% of zombies with zombie_knights, etc., as a simpler mapping.

### Config approach could be

spawn_rules:
  ZOMBIE: zombie_knight  # replace all vanilla zombie spawns with zombie_knight
  SKELETON: skeleton_archer
  # or percentages:
  HUSK:
    - mummy: 70%
    - husk: 30%  # 70% chance replace husk with custom mummy, else let normal spawn


Or we simply instruct the admin to set world to peaceful and rely on plugin spawners. We can provide a basic random spawner config:

random_spawns:
  enabled: true
  spawn_interval: 20  # seconds
  max_mobs_per_player: 10
  mobs:
    zombie_knight:
      weight: 5    # relative chance
      min_group: 1
      max_group: 3
    forest_spider:
      weight: 3
      min_group: 2
      max_group: 4


etc. This allows a mix of mobs. For simplicity of plan, we won’t go deep into the algorithm, but we will note that the plugin provides controlled spawning to ensure the RPG world is populated with the configured mobs appropriately.

Region-specific spawns: If using WorldGuard regions, we could also allow certain mobs only in certain regions/biomes. E.g. forest mobs in forest area, undead in graveyard region. This might be overkill for initial release, but we can design the config to potentially include region or biome conditions for spawns.

Combat and AI Adjustments: While using vanilla AI, there are a few tweaks we might do:

Ensure mobs don’t target each other incorrectly (like zombie knights shouldn’t attack normal zombies if on same team – usually they won’t since they’re same base type faction).

Possibly use Paper’s pathfinding API or Navigator to make certain bosses move differently (like charge at player or avoid water, etc.). Could also give bosses increased awareness (Minecraft AI is limited but attributes like follow range can be increased).

If needed, we can turn off certain behaviors (like Enderman teleport or Creeper explosion block damage if that matters – though we might not use creeper as base if it could grief). These details can be configured or handled via events (e.g. cancel an explosion in RPG world or set creeper explosion to no block damage via gamerule or our plugin).

### Rewards and Drops

Custom mobs and bosses are the source of rare items and money. For instance, a boss might have a guaranteed drop of a unique item (defined in items.yml) at 100% and some extra loot at lower chances. Our drop system will handle giving those to the player or dropping on ground. If multiple players fight a boss, by default only the player who deals the final hit (or most damage) might get the loot. We might consider a fairness system (like all participants get something or use experience orbs as proxy), but initial implementation can keep it simple (last-hit gets the drop, so players might party and then share loot manually).

We’ll ensure that any custom item drop uses our item creation logic (or EI if applicable) so that the item has all its special attributes when obtained.

## Safe Zones and Danger Zones (WorldGuard Integration)

WorldGuard can be leveraged to create distinct areas in an RPG world with differing rules, as touched on earlier with inventory. Here’s how the plugin will utilize it:

Region Definitions: The server admin can define WorldGuard regions using WorldEdit as usual. For our plugin, we care about identifying which regions are “safe” (towns, no combat) and possibly which are “dungeon” or “danger” areas. The simplest setup: any region listed as safe in our config will be considered non-combat zones, and everything else in the world is treated as dangerous by default.

### Config example

safeRegions:
  - SpawnTown
  - GuildHall


We may also allow a region flag approach: e.g., admin could set a WorldGuard custom flag rpg-safe=true on regions, and our plugin can query that. Using WG’s API, we can check a player’s current region membership on movement.

Enforcing Rules in Safe Zones: In safe regions, the plugin will:

Prevent hostile mob spawns (we can either not spawn them via our system or explicitly remove any that wander in). WorldGuard by default can prevent mob spawning in regions if configured, but our plugin can double ensure by not spawning custom mobs there.

Possibly disable PvP if not already (again WG flag pvp: deny can handle it, but we can also check combat events and cancel if both players in safe region).

Allow usage of certain commands or features: e.g. kit editing, trading, etc., can be allowed only in safe zone. We’ll incorporate checks for region in commands like /rpg builder (only let it open if player is in a safe region, to avoid combat abuse).

If region-based inventory separation is enabled, perform the inventory swap on entering/leaving these regions as described before. This is complex, so it might be disabled by default unless the server explicitly wants it. Many servers might choose to keep the same inventory but just rely on the fact that you can’t fight in town. We will document how to enable the swap if needed (maybe via useRegionInventorySwitch: true in config).

Danger Zone Mechanics: In dangerous areas (i.e., not in a safe region), all RPG gameplay elements are active:

Custom mobs will spawn here. If a player is in a safe region, perhaps mobs despawn or can’t enter it, creating a natural boundary. We might give mobs a AI rule not to path into safe regions if possible (though WG can also block mob entry if configured with entry deny for monsters).

PvP could be enabled if the server wants (WG flag or a plugin config allowPVP: true/false for RPG worlds). That’s up to the design of the server’s RPG (some want co-op PvE only, others might have PvP zones). We will not override PVP rules unless specified; we defer to server/WorldGuard settings.

When a player is in a danger zone, they should have their RPG inventory equipped (ensured by world enter or region enter logic). We’ll monitor region transitions via events (WorldGuard has region events in its API or we can use a PlayerMoveEvent and check if region membership changed by comparing WG queries). When leaving a danger zone into safe, if inventory switching is on, we swap to safe inventory (likely empty or original). When entering back to danger, swap back to RPG gear. All these transitions must be smooth and not allow item duping or loss. We’ll thoroughly test that (e.g., what if a player tries to drop items right as a swap happens – we might need to cancel drops during region transitions, etc.).

WorldGuard as Soft-Dependency: We will mark WorldGuard as a soft-depend in plugin.yml. If WG is not present, the plugin will simply not do region-based checks and assume the entire world is one uniform “danger zone”. The config’s safeRegions list would be irrelevant then. But the plugin’s core (inventory management, mobs, etc.) will still function. We might include a simple alternative if no WG: e.g., allow defining a cuboid in our config as safe area (with coordinates) just so there is some safe hub possible without WG. That’s optional.

Utilizing WorldGuard for regions allows server admins to visually create and adjust safe zones without hardcoding coordinates in our config, so it’s the preferred method. We will include instructions for setting up such regions and the required flags.

## Commands and Permissions

We will implement several commands for both players and admins, along with permission nodes to control them. Here’s a summary:

### Player Commands

/rpg builder – Open the kit builder GUI (as detailed above).

Permission: openworldrpg.builder (default true for players, though perhaps restrict to RPG worlds only).

/rpg shop – (If we want a command to open shop GUI directly, in case an NPC is not used or for convenience). This would open the item shop GUI. Could be disabled if NPC is the preferred interaction.

Permission: openworldrpg.shop (default true, or maybe false to force NPC use).

/rpg stats – Show the player’s RPG stats, like how much money they have (if using internal economy), maybe kills, etc. This is a nicety. Could integrate with a placeholder/scoreboard as well.

Permission: openworldrpg.stats (default true).

/rpg spawn <mob/boss> <id> – (Admin/OP only) Manually spawn a custom mob or boss at your location. Useful for testing or events.

Permission: openworldrpg.admin.spawn.

/rpg setshop – (Admin) Possibly used to designate the entity you are looking at as the shop NPC. Alternatively, /rpg createshop [type] to spawn a shopkeeper.

Permission: openworldrpg.admin.setshop.

/rpg reload – (Admin) Reload configuration files (items, mobs, etc.) without restarting server. Will need to carefully reinitialize internal data and possibly respawn mobs if spawn rules changed.

Permission: openworldrpg.admin.reload.

### Permissions

openworldrpg.use – Perhaps a general permission to participate in RPG worlds (if server wants to restrict access to certain ranks). If set, plugin could prevent unauthorized players from entering those worlds or using the kit builder. By default, all players have access.

openworldrpg.builder – as above, to use kit builder.

openworldrpg.shop – to use the shop GUI.

openworldrpg.bypass – (Admin) allows bypassing inventory restrictions (maybe to test or move items freely if needed). Possibly not needed but could let admins carry items out for debugging.

openworldrpg.admin.* – covers all admin commands.

We will also ensure that if a player somehow gets teleported into an RPG world without going through normal means (like an admin tp), the plugin still triggers the inventory swap and such (we can handle this by also checking on PlayerTeleportEvent and PlayerJoin if location is in one of those worlds). So even admin TP obeys rules (unless they have bypass perm).

## Data Storage and Persistence

All critical data will be stored persistently to survive server restarts and crashes:

Player Data: As mentioned, each player’s kit setup and inventories per world will be stored, likely in YAML under playerdata. We might store all in one file per player (which is easier to manage for deletion if player leaves) or have separate directories (one for inventories, one for kits) – but one file per player containing everything is fine given modern hardware and not too many worlds.

Economy Data: If using internal economy, we’ll have a file (or part of the player data) storing balances. Possibly a simple balances.yml mapping UUID -> balance for quick lookup. Alternatively, in each player’s file, store a money: field. Either works. If lots of players, a single large balances file could be a bottleneck, so per-player data might scale better.

### Config Files

config.yml for global settings (world list, toggles like useRegionSwap, default starting kit, etc., plus perhaps some default values).

items.yml for custom item definitions.

mobs.yml for mob and boss definitions (including drops, abilities, spawn settings).

shop.yml for shop inventory and prices (or we merge that into items by adding a price field, but separating might be cleaner).

kits.yml – if we want to predefine kit templates or default kit, but since kit is per player, that’s in player data. We might not need a kits.yml unless providing pre-made kit examples for players to choose from. Possibly an idea for future (players could pick a template kit to start from).

Logging: The plugin can log major events (like a player’s inventory being saved/loaded, purchases made, boss spawns) at least in debug mode to help track any issues. We’ll also watch for any errors in saving data and handle exceptions to avoid corrupting files.

### Edge Case Handling

Prevent dupes: e.g., if a player somehow triggers two world change events quickly (maybe through a portal and command), we’ll lock their data during a transition so one event doesn’t override another improperly.

If a player dies in the RPG world, depending on server keep-inventory, items might drop. If keep-inventory is off (so items drop), we must ensure that if they respawn in a non-RPG world (like some servers set respawn to hub), they don’t get their RPG items back in hand and also have dropped copies. Ideally, if respawn sends them out of the RPG world, we treat it as an exit: save nothing (because they died, maybe their items were dropped and gone) and restore original. That could mean if they died and lost items, those items remain in RPG world ground. We might want to force respawn within the RPG world to let them pick up or lose items normally. This is a server config consideration but we will highlight it.

Combat logging: If a player logs out during boss fight to avoid death, what do we do? Without a full combat-tag system, we might just let it be – they’ll log back in likely in same world with same gear (since we save it). The boss might still be there to fight. We won’t implement heavy punishments, but server owners can handle that socially or with a separate plugin if needed.

Exploits with dropping items through world borders or shulker shenanigans: We will double-check scenarios where a player could attempt to smuggle RPG items out. With strict separation (no same world portal to normal world, etc.) it should be fine. EnderChest we already plan for. Shulker boxes: If a player somehow got a shulker in RPG world and filled it with items, then carried that shulker to a normal world inventory via a loophole, that’s problematic. Since we swap whole inventories, they shouldn’t carry any container item out because it stays in RPG inventory. If they try to circumvent by placing a shulker and then leaving (to pick it up later in normal world), we could handle by not allowing placement of shulkers or containers in RPG world or not allowing break if placed (or just note this edge case). Likely not needed if inventory always swaps at world boundary – because they can’t place it across worlds. We’ll still be mindful.

## Example Configurations

Below are examples of how the plugin’s configuration files might look. These demonstrate defining worlds, items, mobs, bosses, kits, and shop entries.

config.yml
```yaml
# OpenWorldRPG main configuration
rpgWorlds:
  - RPG_World1         # Name of the world(s) where RPG mode is active
  - DungeonRealm

useWorldGuard: true    # Whether to integrate with WorldGuard for safe/danger zones
safeRegions:
  - SpawnCity          # Region names marked as safe (no combat, optional inventory swap)
  - CityMarket

allowPVP: false        # Whether players can PvP in RPG worlds (can also use WorldGuard for finer control)
regionInventorySwitch: true   # Enable inventory swapping on safe zone enter/exit (if WorldGuard is used)

# Economy settings
useVault: true         # Use Vault economy if available (if false or not present, fallback to internal)
startingBalance: 0     # Default money for new players in RPG (if internal economy)
currencyName: "Coins"  # Name of the currency (for display in messages)

# Shop settings
shopNPC:
  enabled: true
  npcName: "&aItem Shop"    # Name to display on the shop NPC (if using internal NPC)
  npcType: VILLAGER         # Entity type for shop NPC (e.g. Villager)
  location: "RPG_World1,100,65,-30,0,0"  # XYZ coords and yaw/pitch for the shop NPC spawn, if using internal

# Default kit (given on first entry to RPG world, or if kit builder not used yet)
defaultKit:
  hotbar:
    0: iron_sword         # item IDs defined in items.yml
    1: wooden_shield
    2: health_potion
  main:
    9: bread              # give some food in inventory slot 9 (first slot of second row)
    10: bread
    11: bread
  armor:
    helmet: leather_helmet
    chestplate: leather_chestplate
    leggings: leather_leggings
    boots: leather_boots

# Spawn settings for mobs (simplified example)
spawnRules:
  replaceVanilla: true    # if true, intercept vanilla spawns
  replacements:
    ZOMBIE: zombie_knight      # replace all zombies with zombie_knight
    SKELETON: skeleton_archer  # replace skeletons with skeleton_archer
  # Alternatively, a custom spawn system could be configured here
```

items.yml
```yaml
items:
  iron_sword:
    name: "&fIron Longsword"
    material: IRON_SWORD
    lore:
      - "&7A reliable iron sword for new adventurers."
      - "&7Damage: 6"
    enchantments:
      - DAMAGE_ALL: 1      # Sharpness I
    attributes:
      damage: 6            # base damage (if we choose to use attribute modifiers)
    abilities: []          # no special ability for this basic item

  wooden_shield:
    name: "&6Wooden Shield"
    material: SHIELD
    lore:
      - "&7A makeshift shield. Blocks attacks."
      - "&8(Right-click to use)"
    abilities: []
    # Shields might not need special config; using vanilla mechanics

  health_potion:
    name: "&dHealing Potion"
    material: POTION
    lore:
      - "&7Potion of Healing"
      - "&8Instantly restores 4 hearts on use"
    potionEffect: INSTANT_HEALTH    # We can specify a pre-made potion effect for this drinkable
    amount: 3                       # Give 3 potions in a stack
    abilities:
      - consume_heal: 8            # Our custom ability: on consume, heal 8 health (4 hearts)

  sword_of_ice:
    name: "&bSword of Ice"
    material: DIAMOND_SWORD
    lore:
      - "&7A blade forged from eternal ice."
      - "&7On hit, has a chance to slow enemies."
    enchantments:
      - DAMAGE_ALL: 3             # Sharpness III
    abilities:
      - slow_on_hit: 3s           # Custom ability: apply Slowness for 3 seconds on hit
      - slow_chance: 30%          # 30% chance to trigger (if not specified, assume 100%)

  thunder_axe:
    name: "&eAxe of Thunder"
    material: IRON_AXE
    lore:
      - "&7Unleashes thunder on strike."
    enchantments:
      - DAMAGE_ALL: 4
    abilities:
      - lightning_on_hit: true    # Summon a lightning bolt on hit (could be rare or cooldown-based)
      - cooldown: 5s             # e.g., at most one lightning per 5 seconds per player

  # Example of an ExecutableItems integration:
  fireball_spell:
    executable: true
    ei_id: "FireballSpell"       # This ID corresponds to an item defined in ExecutableItems plugin
    # No other attributes needed; we rely on EI's config for name/lore/ability.
```


(The above items are just examples. The abilities like slow_on_hit, lightning_on_hit etc., would be handled in code. We’d document exactly which ability keywords are available.)

mobs.yml
```yaml
mobs:
  zombie_knight:
    type: ZOMBIE
    displayName: "&2Zombie Knight"
    health: 40        # 20 hearts
    damage: 6         # 3 hearts per hit (we adjust attribute)
    armor: 5          # points of armor (we can simulate via gear or attribute)
    speed: 0.25       # slightly faster than normal zombie
    equipment:
      helmet: IRON_HELMET
      chestplate: IRON_CHESTPLATE
      leggings: IRON_LEGGINGS
      boots: IRON_BOOTS
      main_hand: IRON_SWORD
    drops:
      - iron_ingot: 0.2      # 20% chance 1 iron ingot
      - health_potion: 0.1   # 10% chance a health potion (from items.yml)
      - money: 5-10          # 5 to 10 Coins to player
    spawn:
      weight: 5             # relative weight for random spawn (if using custom spawner)
      minLevel: 1           # maybe level scaling if we add that (not now)

  skeleton_archer:
    type: SKELETON
    displayName: "&7Skeleton Archer"
    health: 30
    damage: 5         # affects arrow damage possibly
    equipment:
      helmet: CHAINMAIL_HELMET
      chestplate: LEATHER_CHESTPLATE
      main_hand: BOW
    drops:
      - arrow: 1.0 (2-4)    # Always drop 2-4 arrows
      - money: 3-7
    abilities:
      - poison_arrows: true  # custom ability: its arrows inflict poison
    spawn:
      weight: 4

bosses:
  undead_king:
    type: ZOMBIE
    displayName: "&4&lUndead King"
    health: 300        # 150 hearts, a formidable boss
    damage: 15         # 7.5 hearts per hit
    armor: 15          # heavy armor
    equipment:
      helmet: DIAMOND_HELMET
      chestplate: DIAMOND_CHESTPLATE
      leggings: DIAMOND_LEGGINGS
      boots: DIAMOND_BOOTS
      main_hand: DIAMOND_SWORD
    drops:
      - undead_crown: 1.0    # 100% drop a custom item 'undead_crown'
      - gold_ingot: 1.0 (5-8) # 100% drop 5-8 gold ingots
      - money: 100-200       # 100 to 200 Coins
    abilities:
      - summon_minions: zombie_knight,3,30s   # Every 30s, summon 3 Zombie Knights
      - ground_slam: 15s        # Every 15s, perform ground slam (AoE damage/knockback around boss)
      - rage_phase: 20%         # When <20% health, gains Strength and Speed
    spawn:
      trigger: "region:UndeadThrone"  # spawns when a player enters region "UndeadThrone"
      announcement: "&cThe Undead King has arisen!"
      respawn_delay: 1800        # 30 minutes respawn after killed
```

(The config above illustrates both regular mobs and a boss. The boss has a more complex ability set. The plugin will interpret these settings to give the boss those behaviors: e.g., scheduling tasks for summon_minions and ground_slam, and listening to its health to trigger rage_phase.)

shop.yml
```yaml
shop:
  title: "&8RPG Item Shop"
  items:
    - item: iron_sword
      price: 50
      slot: 10        # slot in GUI (optional, otherwise auto-place)
    - item: wooden_shield
      price: 30
      slot: 11
    - item: health_potion
      price: 15
      slot: 12
    - item: sword_of_ice
      price: 200
      slot: 13
    - item: thunder_axe
      price: 250
      slot: 14
    # Could have multiple pages; this is one page example
  categories:          # If we wanted categories, e.g. separate GUIs or icons
    weapons:
      icon: IRON_SWORD
      items: [iron_sword, sword_of_ice, thunder_axe]
    armor:
      icon: IRON_CHESTPLATE
      items: [leather_helmet, leather_chestplate]   # assume those defined in items.yml
    consumables:
      icon: POTION
      items: [health_potion, mana_potion]           # mana_potion maybe another item
```

(In this example, the shop GUI will display an Iron Sword for 50 coins, etc. The slot is where to place it; if not given, the plugin can auto-layout. We also show an optional way to group categories with icons if we implement that. The actual config structure can be adjusted based on how we code the GUI.)

## Additional Implementation Notes

### Soft-Dependencies Recap

The plugin will list Vault, XConomy, WorldGuard, ExecutableItems, and Citizens (if we use it) as soft-dependencies. We’ll perform checks like Bukkit.getPluginManager().isPluginEnabled("Vault") before hooking. For example, to hook Vault economy:

RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
if (rsp != null) {
    economy = rsp.getProvider();
    getLogger().info("[OpenWorldRPG] Hooked into Vault economy: " + economy.getName());
}


If not found, we check XConomy specifically (it may register a service too). If still none, enable internal econ. Similar checks for WG and EI (using their API as needed).

### Performance Considerations

We will use async operations where possible for file I/O (e.g. saving/loading player data can be done async to not lag the server, then schedule sync inventory apply). Spawning lots of mobs will be done carefully to avoid spikes (maybe spread over ticks). We’ll also provide config to limit mob counts to prevent overwhelming the server or players.

## Testing

- Moving items between worlds (ensuring no loss/duplication).
- Combat scenarios with multiple players.
- Kit editing workflow and persistence.
- Economy transactions and shop purchases.
- With and without each soft dependency present to ensure graceful degradation.

By following this plan, we will implement a comprehensive open-world RPG plugin that is configurable and extensible, while keeping it within the confines of Paper 1.21.8 API and avoiding unwanted external dependencies. All the example configs above would serve as a starting point for server owners to customize their RPG experience.
