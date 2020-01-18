# Changelog

## Lucky Block 7.9.1 for Minecraft 1.14.4

- Removed custom loot tables (sorry!)

## Lucky Block 7.7.1 for Minecraft 1.13.2

- Add-ons should now be placed in `.minecraft/addons/lucky`. However, the old `addons/luckyBlock` folder will continue to work
- Config is now located in `.minecraft/config/lucky/version-x.x.x`

The following assumes you have an add-on named 'Lucky Block Red'

### Assets

- `assets/blockstates/red_lucky_block.json`
  - Change variant 'normal' to ''
  - Change `lucky:red_lucky_block` to `lucky:block/red_lucky_block`
- `assets/lang/en_US.lang`
  - Change to json format (en_us.json)
  - Change block and item name formats, e.g.
    - `tile.redLuckyBlock.name` -> `block.lucky.red_lucky_block`
    - `item.redLuckySword.name` -> `item.lucky.red_lucky_sword`

### Drops

- Change IDs to 1.13 format - no more 'damage' value
- Change NBT tags to 1.13 format where necessary (e.g. display names, books, signs)
- Use `#randSpawnEgg` to get a random spawn egg ID
- Use a dot `.` to specify particle arguments (e.g. `block.dirt`)
- Use `#json(...)` to easily convert Lucky Block NBT syntax to JSON (and
`#jsonStr(...)` to escape all quotes, useful for custom display names)
- Use the 'state' property to set block states (in Lucky Block NBT format)
- The 5th propety for each block in .luckystruct files is now an NBT tag in the same format as 'state'. Numbers can still be provided, but will have no effect
- Change structures to Minecraft's new .nbt format, using structure blocks. Old .shematic structures will continue work, but some block states may by invalid
- Rename `lucky:LuckyProjectile` to `lucky:lucky_projectile`

### Other

Use `showUpdateMessage=false` in the properties.txt file to disable update messages.
