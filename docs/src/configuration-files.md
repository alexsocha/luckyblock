# Configuration files

The Lucky Block configuration files can be found in the `.minecrat/config/lucky/version-{...}`, after you run the mod at least once.

## Properties

-   File: `properties.txt`

{{> properties-table properties=properties }}

## Luck crafting

-   File: `luck_crafting.txt`

Certain items can affect the 'luck' of a Lucky Block or Lucky Potion when combined in a crafting table. This skews the probability of positive/negative outcomes, with values ranging from -100 to 100. These items are specified here, using the format.

```
[item]=[-100..100]
```

For example:

```
diamond=12
spider_eye=-10
pufferfish=-15
```

## Natural generation

-   File: `natural_gen.txt`

The Lucky Block can generate naturally in the Minecraft world, and across all dimensions. Every entry in this file is a standard Lucky Block [outcome](outcomes), which will occur naturally within the world. Dimensions are separated by `>{dimension_id}`

While not a requirement, the file should only contain outcomes that place Lucky Blocks in the world. This includes either directly placing a Lucky Block or using a custom structure. Lastly, the outcomes should not use any properties related to the player, as they are not initiated by a player.

### Spawnrate

Each outcome has a one in `spawnrate` chance of ocuring per chunk (16x16 block region). Therefore, higher spawnrates mean that the outcome is less likely to occur. To specify the spawnrate of a outcome, use the [@chance](combined_outcomes#chance) property. Note that this usage of the chance property is not related to the regular usage (e.g. in `drops.txt`). Also note that `@luck` has no effect.

> If two outcomes have the same spawnrate, the overall chance of generation will double. If a spawnrate of 1/200 (@chance=200) is desired, and there are two possible outcomes, it would be appropriate to give each a spawnrate of 1/400 (@chance=400).

The location of the spawn will usually be on the surface layer of the world, as long as a valid surface is found. If this is not possible, e.g. in an ocean biome, the spawn may occur in an underground cave.

### Example

```
>minecraft:overworld
type=structure,ID=gen_struct_1@chance=400
type=structure,ID=gen_struct_2@chance=400

>minecraft:the_nether
type=block,ID=lucky:lucky_block@chance=35

>minecraft:the_end
type=block,ID=lucky:lucky_block@chance=25
```

In the overworld, this spawn one of two structures. The overall surface spawnrate is 1/200. In the Nether and the End, only single Lucky Blocks will spawn. Note the much higher spawnrates (1/35 and 1/25), due to the difficulty of navigating these dimensions.

## Structures

-   Files: `structures.txt`, `*.luckystruct`, `*.schematic`

Structures structures are first configured in a separate `/structures`, which contains all structures as either `.schematic` or `.luckystruct` files. In the main directory, the file `structures.txt` is used to specify additional properties for each structure.

You can export `.schematic` structures from Minecraft directly.

### .luckystruct structures

**Advantages**

-   Easier to edit
-   Support Lucky Block [hash variables](property-values#hash_variables)

You can also use these structures as an overlay on top of `.schematic` structures, to configure detailed elements such as entities and block entities.

**Format**

The file is divided into three sections, `>properties`, `>blocks` and `>entities`. Firstly, the length, width and height of the structure are specified under the `>properties` section.

```
>properties
length=5
height=2
width=3
```

Then, every block in the structure is listed under the `>blocks` section. Air blocks do not need to be listed, as any position in the structure without a block will default to air.

```
x,y,z,id,state*,tile entity*
```

_\* Optional_

These properties are the same as in the [block outcome](outcomes#block), though the XYZ coordinates are relative to the structure's internal structure coordinate system.

Below is an example of a structure containing a custom Lucky Block which, when opened, will display the message `Hello there, {name of the player}`.

```
>blocks
4,0,1,lucky:lucky_block,,(Drops=["type=message,ID=\"Hello there, #pName\""])
```

Finally, entities are specified below the `>entities` section.

```
x,y,z,id,data*
```

_\* Optional_

These properties are the same as in the [entity outcome](outcomes#entity), but again with relative coordinates.

### structures.txt

This file lists all of the available structures, and configures additional properties for each.

{{> properties-table properties=outcomes.structure_definition }}

**Block mode**

| Mode      | Descriptions                                                                                                                                                      |
| --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `replace` | Blocks in the world are replaced by those added to form the structure. This applies for all blocks.                                                               |
| `overlay` | Blocks in the world are replaced by those added form the structure, with the exception of air blocks. Air blocks in the structure will not be added to the world. |
| `air`     | Every block in the structure, except for air, will be added to the world as air. Therefore, the structure define a 'hole' that will be made in the world.         |

**Structure coordinates**

To make working with structures easier, it's recommended that you:

-   Build the structure facing north (in the negative z direction).
-   If the structure's rotation is relative to the player's direction, build it the way it will appear when the player is facing north.

When the structure is generated without rotation, the structure coordinate `(0,0,0)` will appear in the bottom, east-most, north-most corner. The coordinate `(centerX,centerY,centerZ)` will always appear in the position where the outcome was initiated.

**Example**

Imagine a container ship structure with a width of 50, height of 50, and length of 100, which appears to be traveling in the north direction. When the structure is generated, we want the player to be on the ship. To achieve this we will:

-   Set `centerX` to `50 / 2 = 25` and `centerZ` to `100 / 2 = 50`, so that the player is in the middle of the ship.
-   Set `centerY` to `21`, so that the ship is submerged (assuming the deck is at level `20`).

```
(structures.txt)
file=ship.shematic,ID=ship,centerX=25,centerY=20,centerZ=50
```

To generate the structure, we will include a rotation relative to the direction that the player is facing:

```
(drops.txt)
type=structure,ID=ship,pos=#pPos,rotation=#pDirect
```

Now let's say that we instead want to generate the ship so that it's travelling towards the player. Below is an example configuration which uses the same structure file:

```
(structures.txt)
file=ship.shematic,ID=ship,centerX=25,centerY=5,centerZ=-10

(drops.txt)
type=structure,ID=ship,pos=#pPos,rotation=#pDirect+2
```

The ship is now gently submerged, and the center point has been moved in front of it. When generated, a 180 degree rotation is applied relative to the player, so that two are facing each other.

## Lucky Block outcomes

-   File: `drops.txt`

Defines the outcomes that the Lucky Block performs, one per line. See [outcomes](outcomes), [property values](property-values), and [combined outcomes](combined-outcomes) for detailed syntax.

## Lucky Sword outcomes

-   File: `sword_drops.txt`

Defines the outcomes that the Lucky Sword performs, in the same format as `drops.txt`. The Lucky Sword will always cause damage to entities, regardless of its additional effects. It's attack damage is the same as an iron sword, with twice the duration of a diamond sword. Additional actions should be rare, which can be enforced by including a 'nothing' action:

```
type=nothing@luck=0@chance=20
```

> The majority of the outcomes should be positive (for the player using it). The `type=nothing` outcome should have the lowest luck, so that when the luck of the sword is increased through crafting, the frequency of additional effects is also increased.

## Lucky Bow outcomes

-   File: `bow_drops.txt`

Defines the outcomes that the Lucky Bow performs, in the same format as `drops.txt`. The Lucky Bow should always shoot a projectile, and its position/motion should be set using the `#bowPos`/`#bowMotion` hash variables. For example, a regular arrow:

```
type=entity,ID=Arrow,pos=#bowPos,NBTTag=(Motion=#bowMotion,Color=-1)@luck=0@chance=20
```

To perform custom outcomes on impact, you can use the [lucky projectile](block-items-entities#lucky-projectile). For example:

type=entity,ID=LuckyProjectile,pos=#bowPos,NBTTag=(Motion=#bowMotion,item=(id=tnt),drops=["type=explosion"])

## Lucky Potion outcomes

-   File: `potion_drops.txt`

Defines the outcomes that the Lucky Potion performs, in the same format as `drops.txt`. The Lucky Potion should always perform a custom outcome. You should also include a splash potion animation each time, such as:

```
type=particle,ID=splashpotion,potion=strength
```

> Since a Lucky Potion can be used both as a weapon and a defense, make sure to clearly mark outcomes using `@luck=-2..2`. This way, when the its luck is modified through crafting, it will serve a different purpose.

## Common

-   File: all

**Comments**

Any line beginning with a forward slash `/` will be ignored, and treated as a comment.

```
/ items
type=item,ID=diamond
ID=emerald

/ entities
type=entity,ID=Pig
type=entity,ID=Cow
```

**Multiline**

You can split a long line into multiple lines using a backslash `\`.

```
group(type=entity,ID=Zombie,amount=40,posOffset=#circleOffset(8,10); \
type=entity,ID=Creeper,amount=10,posOffset=#circleOffset(8,10); \
type=entity,ID=Skeleton,amount=30,posOffset=#circleOffset(8,10); \
type=entity,ID=Spider,amount=20,posOffset=#circleOffset(8,10))
```
