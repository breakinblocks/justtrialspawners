# Individual spawner and vault appearances

Each placed trial spawner or vault can select its own resource-pack model. This changes its textures and geometry while keeping its spawning, rewards, collision, light emission, and interaction behavior. The spinning mob and vault reward displays remain in their usual positions.

## Set an appearance

These commands require permission level 2 and work on both block types:

```mcfunction
/trialspawner appearance <pos> <namespace>:justtrialspawners/<name>
/trialspawner appearance <pos> reset
```

Two examples are included with the mod. For a block directly below you:

```mcfunction
/trialspawner appearance ~ ~-1 ~ justtrialspawners:justtrialspawners/emerald_spawner
/trialspawner appearance ~ ~-1 ~ justtrialspawners:justtrialspawners/emerald_vault
```

Use the example matching the block type. Their emerald sides distinguish them from ordinary blocks, and their top textures follow the normal and ominous states.

The setting is saved on that block and synchronized to clients. If a client's resource pack lacks the selected model, that client sees the default model. The server accepts model IDs without checking client resources, so check the spelling and the client log if an appearance does not show up.

## Supply a model in a resource pack

For the ID `mypack:justtrialspawners/dangerous_spawner`, create:

```text
assets/mypack/models/justtrialspawners/dangerous_spawner.json
```

The mod discovers JSON models under `assets/<namespace>/models/justtrialspawners/`, including subdirectories, whenever client resources reload. Reload with F3+T after changing your pack.

For a texture variant, inherit a shipped model and override its texture slots:

```json
{
  "parent": "justtrialspawners:block/trial_spawner",
  "render_type": "minecraft:cutout",
  "textures": {
    "side": "mypack:block/dangerous_spawner_side",
    "top": "mypack:block/dangerous_spawner_top"
  }
}
```

Those textures belong at `assets/mypack/textures/block/dangerous_spawner_side.png` and `dangerous_spawner_top.png`. The shipped block models use `side`, `top`, and `bottom` texture slots.

For different geometry, supply an ordinary block-model JSON with your own `elements` and textures. Use `minecraft:cutout` for opaque textures with transparent holes or `minecraft:translucent` for partial transparency. Avoid item-only models such as `builtin/entity`; the appearance must provide baked block geometry.

Vault models should face north. The mod rotates the geometry, face culling, and normals to match the placed vault's facing. Custom appearances do not change collision shapes or move the internal mob/reward displays.

## State-specific models

A single base model works for every state. Optionally add models underneath a directory with the same name:

```text
models/justtrialspawners/dangerous_spawner.json
models/justtrialspawners/dangerous_spawner/active.json
models/justtrialspawners/dangerous_spawner/active_ominous.json
models/justtrialspawners/dangerous_spawner/ominous.json
```

For an ominous block, the lookup order is `<name>/<state>_ominous`, `<name>/ominous`, `<name>/<state>`, then `<name>`. Normal blocks try `<name>/<state>` and then `<name>`. If none exists, the default model for the block's current state is used.

| Block | State names |
| --- | --- |
| Trial spawner | `inactive`, `waiting_for_players`, `active`, `waiting_for_reward_ejection`, `ejecting_reward`, `cooldown` |
| Vault | `inactive`, `active`, `unlocking`, `ejecting` |

Each variant is a complete model JSON. For example, an active spawner variant can inherit `justtrialspawners:block/trial_spawner_active` and override only its side texture, preserving the active top texture.

## NBT and configured items

The model ID is stored as a root block-entity string named `appearance`. It can also be changed through `/data` or the spawner's NBT editor:

```mcfunction
/data merge block <pos> {appearance:"mypack:justtrialspawners/dangerous_spawner"}
/data remove block <pos> appearance
```

Configured block items can carry the same field in `BlockEntityTag`:

```snbt
{BlockEntityTag:{appearance:"mypack:justtrialspawners/dangerous_spawner"}}
```

The appearance transfers when the item is placed and is preserved when copying block-entity data into structures or items. Trial spawner item previews also show the custom model. Vault inventory icons retain their standard appearance; placed vaults use the configured model.
