# Changelog

## v9

- fix: register addon crafting recipes after registering items
- fix: correctly resolve structure IDs from multiple addons
- refactor: separate NBT structures and drop structures

## v8

- fix: 'sPos' template variable edge case with negative positions
- fix: effect drops which act on entities in a range
- feat: make adjustY a common attribute
- fix: resolving structure IDs starting with lucky:

## v7

- fix: type casting for template variables which return nested structures

## v6

- Added the following drop properties:
    - `type=entity,randomizeMob=true/false`
        - Defaults to `true`
        - If true, the mob entity will be given random properties just like when spawning naturally, e.g. Zombie armor/weapons, sheep wool color
    - `type=entity,adjustY=(min: int, max: int)`
        - Defaults to `[0, 10]`
        - Adjusts the vertical position of the entity by the given amount, so that ideally it spawns on top of a block
    - `type=block/fill,blockMode=replace/overlay/air`
        - Same as `structure.blockMode`
        - Determines if new blocks should replace existing blocks
    - `type=structure,size=(x: int, y: int, z: int)`
        - Automatically detected in `.nbt` structures
        - Equivalent to the `length/height/width` properties in `.luckystruct` files
        - Used to initialize `centerOffset=(x/2,0,z/2)`
        - Used to determine the fill area when `blockMode=replace`
    - `type=block/fill,facing=0..3` and `type=entity,facing=0.0..4.0`
        - Defaults to `0` for blocks and `2.0` for entities (since Minecraft entities face south by default)
        - 0=north, 1=east, 2=south, 3=west, other values will be wrapped to this range
        - Sets the direction the block/entity is facing
    - `type=any,rotation=0..3`
        - Defaults to `0`
        - 0=north, 1=east, 2=south, 3=west, other values will be wrapped to this range
        - Together with `facing`, determines the final direction of blocks and entities
        - Together with `posOffset` and `centerOffset`, determines the final position of the drop
        - Provides context to the `#sPos(x,y,z)` template variable
        - See the structure example below
    - `type=any,centerOffset=(x: double, y: double, z: double),centerOffsetX=double,etc.`:
      - For structures, defaults to the `centerX,centerY,centerZ` properties in `structures.txt` or otherwise `lenght/2,0,width/2`
      - For other drops, defaults to `(0,0,0)`
      - Together with `posOffset` and `rotation`, determines the final position of the drop
      - Provides context to the `#sPos(x,y,z)` template variable

- Added the following template variables:
    - `#pUUIDArray`, which gives the UUID of the player as an array of 4 integers (most significant first). This is now Minecraft's preferred UUID format.
- Config is now located in `.minecraft/config/lucky/x.x.x-x-{forge/fabric}`.
- All config entries ending with one of `\ ( [ , ;` or starting with one of `) ]` are automatically joined together.
- You can now reference structures using their relative path
  within `structures/`, and set properties such as `centerOffset` or `blockMode` directly within the drop, e.g.
  e.g. `type=structure,id=sub_folder/my_structure.nbt,centerOffset=(3,0,3),blockMode=overlay`.
- Structure IDs will now search within the corresponding addon first. You can also explicitly add a namespace prefix using the ID of any block/item, e.g. `type=structure,id=red_lucky_block:house.nbt`

- As a consequence of the new properties, all the features of a `.luckystruct` file can now be implemented directly within drops. The
  following example creates a Zombie on top of a chest on top of a log, in front of the player:
```
group(
    type=block,posOffset=(0,0,0),id=oak_log;
    type=block,posOffset=(0,1,0),id=chest,facing=2,nbttag=(Items=#chestLootTable("chests/simple_dungeon"));
    type=entity,posOffset=(0.5,2,0.5),id=zombie,facing=2
),pos=#pPos,centerOffset=(0,0,2),rotation=#pDirect
```

## v5

- Added `#luckyCrossbowEnchantments` and `#luckyTridentEnchantments`.

## v4

- Removed custom loot tables (sorry!).

## v3

- Add-ons should now be placed in `.minecraft/addons/lucky`. However, the old `addons/luckyBlock`
  folder will continue to work.
- Config is now located in `.minecraft/config/lucky/version-x.x.x`.

### Assets

To update an addon named 'Lucky Block Red':

- `assets/blockstates/red_lucky_block.json`
    - Change variant 'normal' to ''.
    - Change `lucky:red_lucky_block` to `lucky:block/red_lucky_block`.
- `assets/lang/en_US.lang`
    - Change to json format (en_us.json).
    - Change block and item name formats, e.g.
        - `tile.redLuckyBlock.name` -> `block.lucky.red_lucky_block`
        - `item.redLuckySword.name` -> `item.lucky.red_lucky_sword`

### Drops

- Changed IDs to 1.13 format - no more 'damage' values.
- Changed NBT tags to 1.13 format where necessary (e.g. display names, books, signs).
- Use `#randSpawnEgg` to get a random spawn egg ID.
- Use a dot `.` to specify particle arguments (e.g. `block.dirt`).
- Use `#json(...)` to easily convert Lucky Block NBT syntax to JSON (and `#jsonStr(...)` to escape
  all quotes, useful for custom display names).
- Use the 'state' property to set block states (in Lucky Block NBT format).
- The 5th propety for each block in .luckystruct files is now an NBT tag in the same format as
  'state'. Numbers can still be provided, but will have no effect.
- Changed structures to Minecraft's new .nbt format, using structure blocks. Old .schematic
  structures will continue work, but some block states may by invalid.
- Renamed `lucky:LuckyProjectile` to `lucky:lucky_projectile`.

### Other

- Use `showUpdateMessage=false` in properties.txt to disable update messages.

## v2

- feat: add lucky items
