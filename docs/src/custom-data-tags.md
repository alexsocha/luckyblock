# Custom data tags

## Lucky Block

The Lucky Block includes a custom block entity, in the format:

-   Luck: `integer` (optional). The luck of the block, which is usually modified through crafting.
-   Drops: `["outcome 1", "outcome 2", ...]` (optional). A list of custom outcomes. If specified, this will override the default outcomes. Only one is chosen each time.

With Minecraft commands, you can create custom Lucky Blocks:

```
/setblock ~ ~ ~ lucky:lucky_block{Luck:50,Drops:["type=item,ID=stick","type=item,ID=diamond"]}
```

## Lucky items

The Lucky Sword, Lucky Bow, and Lucky Potion have the same data tag as the Lucky Block.

```
/give @a lucky:lucky_sword{Luck:10,Drops:["type=nothing@chance=10","type=effect,ID=special_fire,target=hitEntity,duration=10"]}
```

## Lucky Projectile

The Lucky Projectile is a special entity which performs outcomes on impact. It is most commonly used by the Lucky Bow, and its appearance can be customised to any any item. Its data is in the format:

-   item: The item used for the appearance of the projectile.
    -   id: `text`. The item ID.
    -   tag: `Compound NBT tag` (optional). Additional item data.
-   trail (optional): The outcomes that will occur while the projectile is alive.
    -   frequency: `float`. How frequently the outcomes will occur, in [game ticks](https://minecraft.gamepedia.com/Tick) (0.05 seconds). A value less than one will cause multiple outcomes per tick.
    -   drops: `["outcome 1", "outcome 2", ...]`. A list of trail outcomes. Only one is chosen each time.
-   drops: `["outcome 1", "outcome 2", ...]`. A list of outcomes that will occur on impact. Only one is chosen each time.

An example projectile that the Lucky Bow might shoot:

```
type=entity,ID=LuckyProjectile,pos=#bowPos,NBTTag=(Motion=#bowMotion,item=(id=water_bucket),trail=(frequency=0.5,drops=["type=particle,ID=splash"]),impact=["type=block,ID=flowing_water"]
```
