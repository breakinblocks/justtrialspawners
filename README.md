# Just Trial Spawners

A Forge 1.20.1 backport of Minecraft 1.21's Trial Chamber features: trial spawners, vaults, breeze, bogged, wind charges, ominous mode, mace enchantments, and trial-themed mob effects.

## Features

### Blocks

- **Trial Spawner** — Full state machine matching 1.21 behavior. Detects players, spawns mobs in waves, ejects rewards on completion, and transforms into ominous mode when a player with Bad Omen approaches.
- **Vault** — Key-activated reward block. Insert a Trial Key to claim loot; each player gets one reward per vault.

### Items

- **Trial Key** / **Ominous Trial Key** — Unlock vaults
- **Breeze Rod** — Dropped by Breeze mobs, crafts into wind charges
- **Wind Charge** — Throwable projectile that creates a wind burst explosion
- **Ominous Bottle** — Drink to gain Bad Omen (levels I-V, scales duration)
- **Breeze / Bogged Spawn Eggs**

### Entities

- **Breeze** — Aerial mob that fires wind charges
- **Bogged** — Skeleton variant that shoots poison arrows; can be sheared for mushrooms
- **Wind Charge** — Projectile entity with authentic 1.21 knockback physics

### Mob Effects

- **Trial Omen** — Converted from Bad Omen near trial spawners; activates ominous mode
- **Wind Charged** — Creates a wind burst on death
- **Weaving** — Spawns cobwebs on death
- **Oozing** — Spawns slimes on death
- **Infested** — Chance to spawn silverfish when hit

### Enchantments (mace / weapon)

- **Density** — +1 damage per level (max V)
- **Breach** — Bypasses armor effectiveness (max IV)
- **Wind Burst** — Triggers a wind explosion on hit (max III, treasure-only)

### Ominous Mode

When a player with Bad Omen enters a trial spawner's detection range, their Bad Omen is converted into Trial Omen and the spawner becomes ominous. Ominous spawners:

- Spawn tougher mob variants
- Drop items periodically during the fight (configurable via loot table)
- Eject ominous rewards (ominous trial keys, ominous consumables)
- Unlock ominous vaults for upgraded loot

### Commands

```
/trialspawner edit                         - Edit held trial spawner item (NBT editor)
/trialspawner edit <pos>                   - Edit placed trial spawner (NBT editor)
/trialspawner give <entity>                - Give a pre-configured spawner item
/trialspawner addmob <pos> <entity>        - Add a mob to a placed spawner
/trialspawner setconfig <pos> <key> <val>  - Set a config value
/trialspawner info <pos>                   - Show current configuration
/trialspawner clearmobs <pos>              - Clear all spawn potentials
```

## Dependencies

- **Forge** 1.20.1 (47.3+)
- **FTB Library** 2001.2.12+ (required - provides the NBT editor used by `/trialspawner edit`)
- **Architectury API** 9.2.14+ (transitive dependency of FTB Library)

## Migrating from the Trials Mod

Just Trial Spawners is designed as a drop-in replacement for the Trials mod by Salju. Automatic migration handles:

- **Registry remapping**: `trials:trial_spawner`, `trials:trial_vault`, `trials:trial_vault_ominous`, `trials:breeze`, `trials:bogged`, `trials:wind_charge`, and most items/effects/enchantments are automatically mapped to their Just Trial Spawners equivalents
- **NBT conversion**: Old trial spawner NBT (`SpawnEgg`, `LootTable`, `isActive`, etc.) is lazily converted to the new format on chunk load
- **Vault ominous fixup**: Ominous vaults recover their blockstate on the first server tick after migration
- **Mob effects**: `trials:winded`/`weaving`/`oozing`/`infested`/`trial_curse` remap to Just Trial Spawners equivalents
- **Enchantments**: `trials:density`/`breach`/`wind_burst` remap to Just Trial Spawners equivalents

### Known Migration Limitations

- The Trials mod's ~50 decorative blocks (tuff variants, copper grates/doors/bulbs/trapdoors, crafter, heavy core) are NOT provided by Just Trial Spawners. These blocks will be lost (turned to air) when migrating. Forge will show a "missing registry entries" confirmation on first load.
- Vault blocks lose their facing direction — the Trials mod's vault had no facing property, so all migrated vaults default to `facing=north`.
- Trial spawner items in player inventories will keep their old NBT format until placed and picked up again. They work correctly when placed, but tooltips and mob rendering won't show until re-picked-up.
- Trials mod potions, banner patterns, paintings, sherds, and music discs are not remapped and will be lost.
- The `trials:mace` item has no Just Trial Spawners equivalent.

Disable migration via the server config `enable_trials_migration = false` once migration is complete.

## Building from Source

```bash
./gradlew build          # Build the mod jar (output: build/libs/justtrialspawners-1.20.1-1.0.0.jar)
./gradlew runClient      # Launch a dev client
./gradlew runServer      # Launch a dev server
./gradlew runData        # Run data generation
```

## License

MIT License — see [LICENSE.md](LICENSE.md).
