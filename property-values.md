# Property values

## Value types

| Type            | Examples                                        | Description                                                                                                                          |
| --------------- | ----------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `integer`       | `5`, `-7`, `200`                                | A positive or negative whole number.                                                                                                 |
| `float`         | `0.4`, `-180.0`, `-20F` `400f`                  | A positive or negative floating point number.                                                                                        |
| `true`\|`false` | `true`, `false`                                 | A conditional boolean value.                                                                                                         |
| `text`          | `lucky:lucky_block`, `"Hi, how are you?"`       | A text value.                                                                                                                        |
| `NBT Tag`       | `(title="My Book", pages=["page 1", "page 2"])` | Data in [Minecraft NBT format](https://minecraft.gamepedia.com/NBT_format), specified using [custom syntax](proprty-types#nbt-tags). |

> Numerical values can include at most one algebraic operation using the symbols `+`, `-`, `*`, `/`, e.g. `posY=#pPosY+10`.

## Quotes \& backslashes

When specifying a text value, quotes are usually not be needed. By convention, quotes should not be used for IDs and other one-word properties, but should be used for text containing multiple words. However, quotes are needed when:

-   The value of the text can be interpreted as any other data type, such as a number (e.g. "5"). A
-   The text contains any of the following symbols: `, ; ( ) [ ] { }`.

You can also use a backslash `\` to cancel special symbols, including quotes within quotes. The two examples below are equivalent:

```
type=block,ID=lucky:lucky_block,NBTTag=(Drops=["type=message,ID=\"Hello, how are you?\""])

type=block,ID=lucky:lucky_block,NBTTag=(Drops=["type=message,ID=Hello\, how are you?"])
```

## Hash variables

Hash variables allow properties to have dynamic values, which are either random or determined by the current state of the game.

### Standard variables

The variables can be used in most places.

<table>
    <thead>
        <th>Name</th>
        <th>Return Type</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#bPos, #bPosX, #bPosY, #bPosZ
</td>
<td class="no-p-margin">

`(integer,integer,integer)` \| `integer`

</td>
<td>
<div class="no-p-margin">

The position at which the outcome occured, rounded to the nearest block. `#bPos` returns an `(x,y,z)` tuple, while `bPos{X,Y,Z}` returns each component separately.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=Zombie,posX=#bPosX+5</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#pPos, #pPosX, #pPosY, #pPosZ
</td>
<td class="no-p-margin">

`(float,float,float)` \| `float`

</td>
<td>
<div class="no-p-margin">

The position of the player who initiated the outcome, rounded to the nearest block. `#pPos` returns an `(x,y,z)` tuple, while `pPos{X,Y,Z}` returns each component separately.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=block,ID=anvil,posY=#posY+10</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#ePos, #ePosX, #ePosY, #ePosZ
</td>
<td class="no-p-margin">

`(float,float,float)` \| `float`

</td>
<td>
<div class="no-p-margin">

The position of the entity hit (e.g. by the Lucky Sword or Lucky Projectile), rounded to the Nearest block. `#pPos` returns an `(x,y,z)` tuple, while `ePos{X,Y,Z}` returns each component separately.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=explosion,delay=5,pos=#ePos</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#{b,p,e}ExactPos, #{b,p,e}ExactPos{X,Y,Z}
</td>
<td class="no-p-margin">

`(float,float,float)` \| `float`

</td>
<td>
<div class="no-p-margin">

Same as the previous position variables, but without rounding.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=Zombie,pos=#pExactPos</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#bowPos
</td>
<td class="no-p-margin">

`(float,float,float)`

</td>
<td>
<div class="no-p-margin">

Returns the position which should be given to an entity shot from the Lucky Bow.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=Arrow,pos=#bowPos,NBTTag=(Motion=#bowMotion)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

#circeOffset(_min: `float`[, max: `float`]_)

</td>
<td class="no-p-margin">

`(float,0,float)`

</td>
<td>
<div class="no-p-margin">

Returns circular position offset. The points will always be evenly spaced, depending on the `amount` property, and will randomly lie within a (min, max) radius.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=Pig,amount=20,posOffset=#circleOffset(2,3)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#pName
</td>
<td class="no-p-margin">

`text`

</td>
<td>
<div class="no-p-margin">

The name of the player who initiated the outcome.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=message,ID="Hello there, #pName"</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#UUID
</td>
<td class="no-p-margin">

`UUID`

</td>
<td>
<div class="no-p-margin">

The UUID of the player who initiated the outcome.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=EntityHorse,NBTTag=(OwnerUUID=#pUUID)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#pPitch
</td>
<td class="no-p-margin">

`-90..90`

</td>
<td>
<div class="no-p-margin">

The pitch of the player who initiated the outcome. Pitch 90 = down, pitch -90 = up.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=arrow,NBTTag=(Motion=#motionFromDirection(#pYaw,#pPitch+#rand(-10,10),1.0)),amount=10</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#pYaw
</td>
<td class="no-p-margin">

`0..360`

</td>
<td>
<div class="no-p-margin">

The yaw of the player who initiated the outcome. Yaw 0 = south, yaw 180 = north.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>id=head,NBTTag=(Rotation=[#pYaw+180f,0f])</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#pDirect
</td>
<td class="no-p-margin">

`0` \| `1` \| `2` \| `3`

</td>
<td>
<div class="no-p-margin">

The direction the player is facing, rounded to the nearest X/Z axis. 0 = north, 1 = east, 2 = south, 3 = west.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=structure,ID=ship,rotation=#pDirect</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

#rand(_min: `integer`, max: `integer`_)

</td>
<td class="no-p-margin">

`integer`

</td>
<td>
<div class="no-p-margin">

Selects a random integer in the given range, inclusive.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=block,ID=anvil,posOffsetY=#rand(5,10)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

#rand(_min: `float`, max: `float`_)

</td>
<td class="no-p-margin">

`float`

</td>
<td>
<div class="no-p-margin">

Selects a random float in the given range, inclusive.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=bat,posOffsetY=#rand(0.0,5.0),amount=30</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

#randPosNeg(_min: `integer`, max: `integer`_)

</td>
<td class="no-p-margin">

`integer`

</td>
<td>
<div class="no-p-margin">

Selects a random integer in the given range, inclusive, with a 50% chance of being negative.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=block,ID=lava,posOffsetY=-1,posOffsetX=#randPosNeg(1,3),posOffsetZ=#randPosNeg(1,3),amount=10</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

#randPosNeg(_min: `float`, max: `float`_)

</td>
<td class="no-p-margin">

`float`

</td>
<td>
<div class="no-p-margin">

Selects a random float in the given range, inclusive, with a 50% chance of being negative.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=egg,NBTTag=(Motion=#motionFromDirection(#pYaw,#randPosNeg(10,20),1.0)),amount=20</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

#randList(_value1, value2, ..._)

</td>
<td class="no-p-margin">

One of the values in the list

</td>
<td>
<div class="no-p-margin">

Selects a random value from the given list.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=#randList(gold_ingot,diamond)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#time
</td>
<td class="no-p-margin">

`0..24000`

</td>
<td>
<div class="no-p-margin">

The current [world time](https://minecraft.gamepedia.com/Daylight_cycle).

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=time,ID=#time+1000</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#randColor
</td>
<td class="no-p-margin">

[Dye ID](https://minecraft.gamepedia.com/Dye#ID)

</td>
<td>
<div class="no-p-margin">

Selects a random color.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=block,ID=#randColor_terracotta</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#randPotion
</td>
<td class="no-p-margin">

[Status effect ID](https://minecraft.gamepedia.com/Status_effect)

</td>
<td>
<div class="no-p-margin">

Selects a random status effect.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=potion,NBTTag=(Potion=#randPotion)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#randSpawnEgg
</td>
<td class="no-p-margin">

[Spawn egg ID](https://minecraft.gamepedia.com/Spawn_Egg#ID)

</td>
<td>
<div class="no-p-margin">

Selects a random spawn egg.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=#randSpawnEgg</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

### NBT variables

The variables can be used in NBT tags.

<table>
    <thead>
        <th>Name</th>
        <th>Return Type</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#luckySwordEnchantments, #luckyAxeEnchantments, #luckyToolEnchantments, #luckyBowEnchantments, #luckyFishingRodEnchantments, #luckyCrossbowEnchantments, #luckyTridentEnchantments
</td>
<td class="no-p-margin">

`[(id=enchantment_id, lvl=integer), ...]`

</td>
<td>
<div class="no-p-margin">

Selects random enchantments for each item catagory.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=diamond_axe,NBTTag=(ench=#luckyAxeEnchantments)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#luckyHelmetEnchantments, #luckyChestplateEnchantments, #luckyLeggingsEnchantments, #luckyBootsEnchantments
</td>
<td class="no-p-margin">

`[(id=enchantment_id, lvl=integer), ...]`

</td>
<td>
<div class="no-p-margin">

Selects random enchantments for each armor catagory.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=iron_boots,NBTTag=(ench=#luckyBootsEnchantments)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

#chestLootTable(_name: [loot table name](https://minecraft.gamepedia.com/Loot_table#List_of_loot_tables)_)

</td>
<td class="no-p-margin">

[Loot table tag](https://minecraft.gamepedia.com/Loot_table#Tags)

</td>
<td>
<div class="no-p-margin">



</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=block,ID=chest,tileEntity=(Items=#chestLootTable("chests/spawn_bonus_chest"))</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#randEnchantment
</td>
<td class="no-p-margin">

`(id=enchantment_id, lvl=integer)`

</td>
<td>
<div class="no-p-margin">

Selects a single random enchantment. Most commonly used for enchanted books.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=enchanted_book,NBTTag=(StoredEnchantments=#randomEnchantment)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">
#randFireworksRocket
</td>
<td class="no-p-margin">

[Fireworks item tag](https://minecraft.gamepedia.com/Firework_Rocket#Item_data)

</td>
<td>
<div class="no-p-margin">

Creates a random fireworks rocket.

</div>
                <div style="word-break: break-all;">
                    <br />
                    <code>type=item,ID=fireworks,NBTTag=#randFireworksRocket</code>
                    <br />
                    <code>type=entity,ID=FireworksRocketEntity,NBTTag=(LifeTime=20,FireworksItem=#randFireworksRocket)</code>
                </div>
            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

#randLaunchMotion(_[power: `float`, pitch: `0..90`]_)

</td>
<td class="no-p-margin">

`[double,double,double]`

</td>
<td>
<div class="no-p-margin">

Returns an `[x,y,z]` motion tag which can be given to an entity, launching it upwards with the given power. The yaw angle is randomly chosen, while pitch is specified as an angle between the y axis (pitch 0 = up). The default inputs are `(0.9,15)`.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=tnt,amount=10,NBTTag=(Fuse=50,Motion=#randLaunchMotion(1.5,12)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

#motionFromDirection(_yaw: `0..360`, pitch: `-90..90`, power: `float`_)

</td>
<td class="no-p-margin">

`[double,double,double]`

</td>
<td>
<div class="no-p-margin">

Converts a direction and power into an `[x,y,z]` motion tag which can be given to an entity. Yaw 0 = south, yaw 180 = north, pitch -90 = down, pitch 90 = up.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=egg,posY=#pExactPosY+0.4,NBTTag=(Motion=#motionFromDirection(#pYaw,#pPitch,0.5))</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

#bowMotion(_[power: `float`, angleOffset: `0..90`]_)

</td>
<td class="no-p-margin">

`[double,double,double]`

</td>
<td>
<div class="no-p-margin">

Returns an `[x,y,z]` motion tag which should be given to an entity shot from the Lucky Bow. The power is a multiplier of the default bow power, and the offset is chosen randomly in both the pitch and yaw direction. The default inputs are `(1.0,0.0)`

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=tnt,pos=#bowPos,NBTTag=(Motion=#bowMotion(2.5))</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

### Structure variables

These variables can only be used within `.luckystruct` structure files. They provide useful information about the context in which the structure is generated.

<table>
    <thead>
        <th>Name</th>
        <th>Return Type</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

{#sPos, #sPosX, #sPosY, #sPosZ}(_x, y, z_)

</td>
<td class="no-p-margin">

An integer or float `(x,y,z)` tuple

</td>
<td>
<div class="no-p-margin">

Converts the given structure coordinates to the corresponding world coordinates of the generated structure. The input is an `(x,y,z)` tuple, in relative structure coordinates. `#sPos` will output the corresponding `(x,y,z)` tuple in world coordinates, while `#sPos{X,Y,Z}` will output each component separately. The output will use `float` values if one of the input coordinates is a `float`, and otherwise `integer` values.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>1,0,1,lucky:lucky_block,0,tileEntity=(Drops=["type=entity,ID=Zombie,pos=#sPos(2.5,0,2.5))"])</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
<td class="no-p-margin func-name-args">

#drop(_property name_)

</td>
<td class="no-p-margin">

`any`

</td>
<td>
<div class="no-p-margin">

Gets the value of a property that the structure was generated with. This is often used to create further structures with the same properties.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>1,0,1,lucky:lucky_block,0,tileEntity=(Drops=["type=structure,ID=inner,pos=#drop(pos),rotation=#drop(rotation))"])</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

### Cancelling

> As of version 1.8.0-2, hash variables are automatically cancelled within quotes and `.luckystruct` files. You can disable this behaviour using `[#]`. Previously, you needed to manually cancel the hash using `'#'`.

In certain cases, such as when using a hash variable in `.luckystruct` structures or in custom NBT tags, you don't want the variable to be evaluated immediately. Instead, you want to preserve it until the outcome is actually performed.

Fortunately, hash variables will not be evaluated in these cases by default. The example below will place a Lucky Block which, when opened, will spawn a pig above the player at their current location.

```
type=block,ID=lucky:lucky_block,NBTTag=(Drops=["type=entity,ID=Pig,posY=#pPosY+10"])
```

This behaviour can be disabled using `[#]`. The example below will spawn the pig above the location the player was in when opening the first block.

```
type=block,ID=lucky:lucky_block,NBTTag=(Drops=["type=entity,ID=Pig,posY=[#]pPosY+10"])
```

## NBT Tags

NBT tags are used for storing additional properties for items, blocks, entities, etc. When used in outcomes, the custom syntax is as follows:

| Tag type     | Format                                     | Example                            |
| ------------ | ------------------------------------------ | ---------------------------------- |
| `string`     | `name="value"`                             | `Name="Special Item"`              |
| `int`        | `name=integer`                             | `lvl=3`                            |
| `boolean`    | `name={true,false}`                        | `sparke=true`                      |
| `float`      | `name={float}F`                            | `size=3.29F`                       |
| `double`     | `name={double}D` \| `name=integer.integer` | `size=2D` \| `size=2.0`            |
| `byte`       | `name={byte}D`                             | `length=3B`                        |
| `short`      | `name={short}S`                            | `length=523S`                      |
| `long`       | `name={long}L`                             | `length=2876439L`                  |
| `int_array`  | `name=integer:integer:...`                 | `nums=372:35:97`                   |
| `byte_array` | `name={byte}B:{byte}B:...`                 | `nums=6B:4B:8B`                    |
| `compound`   | `name=(name=value,name=value,...)`         | `tag=(id="diamond_sword",count=2)` |
| `list`       | `name=[value,value,...]`                   | `Motion=[2.5,1.9,3.0]`             |
