# Outcomes

## Base properties

These properties apply to all outcome types.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>type
                
                
            </td>
<td class="no-p-margin">

`text`

</td>
<td class="no-p-margin">

`item`

</td>
<td>
<div class="no-p-margin">

The type of the outcome. All other properties, except for base properties, will depend on the type.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=message,ID="Hello"</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>pos, posX, posY, posZ
                
                
            </td>
<td class="no-p-margin">

`(integer, integer, integer)` \| `integer`

</td>
<td class="no-p-margin">

The position at which the outcome was initiated

</td>
<td>
<div class="no-p-margin">

Sets the position at which the outcome will occur. Use `pos` to set all coordinates at once, or `pos{X,Y,Z}` to set each separately.

</div>
                <div style="word-break: break-all;">
                    <br />
                    <code>type=entity,ID=Zombie,posY=255</code>
                    <br />
                    <code>type=entity,ID=Zombie,pos=(40,64,-90)</code>
                </div>
            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>posOffset, posOffsetX, posOffsetY, posOffsetZ
                
                
            </td>
<td class="no-p-margin">

`(integer, integer, integer)` \| `integer`

</td>
<td class="no-p-margin">

`0`

</td>
<td>
<div class="no-p-margin">

Offsets the position of the outcome. Use `posOffset` to set all coordinates at once, or `posOffset{X,Y,Z}` to set each separately. Note that in many cases you can use e.g. `posY=#bPosY+10` to acheive the same result.

</div>
                <div style="word-break: break-all;">
                    <br />
                    <code>type=entity,ID=Sheep,posOffsetY=10</code>
                    <br />
                    <code>type=entity,ID=Sheep,amount=20,posOffset=#circleOffset(5)</code>
                </div>
            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>amount
                
                
            </td>
<td class="no-p-margin">

`integer`

</td>
<td class="no-p-margin">

`1`

</td>
<td>
<div class="no-p-margin">

Specifies the number of times the outcome will be repeated. All properties will remain the same for each instance of the outcome, unless the properties are randomized (see `reinitialize`).

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=diamond,amount=10</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>reinitialize
                
                
            </td>
<td class="no-p-margin">

`true`\|`false`

</td>
<td class="no-p-margin">

`true`

</td>
<td>
<div class="no-p-margin">

Used when the amount property is greater than one. This will specify whether each instance of the outcome should have its properties re-randomized. If set to `false`, random properties will only be chosen for the first instance of the outcome, and subsequent outcomes will use those properties.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=dye,damage=#rand(0,15),amount=10,reinitialize=false</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>delay
                
                
            </td>
<td class="no-p-margin">

`float`

</td>
<td class="no-p-margin">

`0`

</td>
<td>
<div class="no-p-margin">

Delays the occurrence of the outcome. The delay is specified in seconds, and the outcome will take place once the time has passed.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=gold_block,delay=5</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>postDelayInit
                
                
            </td>
<td class="no-p-margin">

`true`\|`false`

</td>
<td class="no-p-margin">

`true`

</td>
<td>
<div class="no-p-margin">

Whether a delayed outcome will be initialized after the delay (`true`), or immediately (`false). This will affect when [hash variables](property-values#hash-variables) are evaluated.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=Zombie,pos=#pPos,delay=5,postDelayInit=false</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

## Item

-   `type=item`

Drops an item.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>ID
                
                
            </td>
<td class="no-p-margin">

[Item ID](https://minecraft.gamepedia.com/Java_Edition_data_value#Items)

</td>
<td class="no-p-margin">

`text`

</td>
<td>
<div class="no-p-margin">

The ID of the item.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=diamond_sword</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>NBTTag
                
                
            </td>
<td class="no-p-margin">

[NBT Tag](property-values#nbt-tags)

</td>
<td class="no-p-margin">

`()`

</td>
<td>
<div class="no-p-margin">

Used to specify the NBT Tag of an item. This property stores additional data about the item, a full list of item NBT Tags can be found here.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=potion,NBTTag=(Potion="fire_resistance")</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

## Block

-   `type=block`

Places a block.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>ID
                
                
            </td>
<td class="no-p-margin">

[Block ID](https://minecraft.gamepedia.com/Java_Edition_data_value#Blocks)

</td>
<td class="no-p-margin">



</td>
<td>
<div class="no-p-margin">

The ID of the block.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=block,ID=pumpkin</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>state
                
                
            </td>
<td class="no-p-margin">

[NBT Tag](property-values#nbt-tags)

</td>
<td class="no-p-margin">

`()`

</td>
<td>
<div class="no-p-margin">

The [block state](https://minecraft.gamepedia.com/Block_states).

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=block,ID=cauldron,state=(level=3)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>tileEntity
                
                
            </td>
<td class="no-p-margin">

[NBT Tag](property-values#nbt-tags)

</td>
<td class="no-p-margin">

`()`

</td>
<td>
<div class="no-p-margin">

Sets the [block entity](https://minecraft.gamepedia.com/Block_entity).

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=block,ID=sign,tileEntity=(Text2="{\"text\":\"Hello\",\"color\":\"blue\"}")</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>blockUpdate
                
                
            </td>
<td class="no-p-margin">

`true`\|`false`

</td>
<td class="no-p-margin">

`true`

</td>
<td>
<div class="no-p-margin">

Whether the block should be updated once placed. If enabled, water will flow, blocks that cannot stay (e.g. torches in air) will be dropped, and other updates will take place.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=block,ID=water,blockUpdate=false</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

## Entity

-   `type=entity`

Spawns an entity.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>ID
                
                
            </td>
<td class="no-p-margin">

[Entity ID](https://minecraft.gamepedia.com/Java_Edition_data_value#Entities)

</td>
<td class="no-p-margin">

`text`

</td>
<td>
<div class="no-p-margin">

The ID of the entity.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=entity,ID=Zombie</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>NBTTag
                
                
            </td>
<td class="no-p-margin">

[NBT Tag](property-values#nbt-tags)

</td>
<td class="no-p-margin">

`()`

</td>
<td>
<div class="no-p-margin">

Sets additional entity properties

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=item,ID=Pig,NBTTag=(CustomName="Sam")</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

## Structure

-   `type=structure`

Generates a structure, which has been [preconfigured](configuration-files#structurestxt).

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>ID
                
                
            </td>
<td class="no-p-margin">

[Structure ID](config_files#structures)

</td>
<td class="no-p-margin">



</td>
<td>
<div class="no-p-margin">

The ID of a structure defined in `structures.txt`.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=structure,ID=ship</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>rotation
                
                
            </td>
<td class="no-p-margin">

`integer`

</td>
<td class="no-p-margin">

`0`

</td>
<td>
<div class="no-p-margin">

The number of times the structure should be rotated by 90 degrees, clockwise. The rotation will occur around the structure's centerX and centerZ.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=structure,ID=ship,rotation=3</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

## Command

-   `type=command`

Runs a Minecraft command.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>command
                
                
            </td>
<td class="no-p-margin">

[Command](https://minecraft.gamepedia.com/Commands)

</td>
<td class="no-p-margin">



</td>
<td>
<div class="no-p-margin">

The full command text.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=command,ID="/say Hello"</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>commandSender
                
                
            </td>
<td class="no-p-margin">

The name of a player, or a selector

</td>
<td class="no-p-margin">

`@a`

</td>
<td>
<div class="no-p-margin">

The sender of the command.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=command,commandSender=PlayerInDistress,ID="/tell @a Here is a message from PlayerInDistress"</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>displayOutput
                
                
            </td>
<td class="no-p-margin">

`true`\|`false`

</td>
<td class="no-p-margin">

`false`

</td>
<td>
<div class="no-p-margin">

Whether the output of the command should be displayed in the chat.

</div>
            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

## Difficulty

-   `type=difficulty`

Sets the difficulty level of the world. This can be used to ensure that monsters spawn.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>ID
                
                
            </td>
<td class="no-p-margin">

`peaceful`\|`easy`\|`normal`\|`hard`

</td>
<td class="no-p-margin">



</td>
<td>
<div class="no-p-margin">

The world [difficulty level](https://minecraft.gamepedia.com/Difficulty).

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=difficulty,ID=hard</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

## Effect

-   `type=effect`

Gives a status effect to the player and/or surrounding entities.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>ID
                
                
            </td>
<td class="no-p-margin">

[Status effect ID](https://minecraft.gamepedia.com/Status_effect)

</td>
<td class="no-p-margin">



</td>
<td>
<div class="no-p-margin">

The ID of the status effect

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=effect,ID=slowness</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>duration
                
                
            </td>
<td class="no-p-margin">

`integer`

</td>
<td class="no-p-margin">

`30`

</td>
<td>
<div class="no-p-margin">

The number of seconds the effect will last for.

</div>
            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>amplifier
                
                
            </td>
<td class="no-p-margin">

`0..255`

</td>
<td class="no-p-margin">

`0`

</td>
<td>
<div class="no-p-margin">

The effect amplifier, which is one less than the number displayed (e.g. Strength III has an amplifier of 2).

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=effect,ID=strength,amplifier=2</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>target
                
                
            </td>
<td class="no-p-margin">

`none`\|`player`\|`hitEntity`

</td>
<td class="no-p-margin">

`none`

</td>
<td>
<div class="no-p-margin">

Whether the effect should be given to the player who initiated it, or to the entity that was hit (only applies to the Lucky Sword and [Lucky Projectile](custom-data-tags#lucky-projectile)).

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=effect,ID=blindness,target=hitEntity</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>range
                
                
            </td>
<td class="no-p-margin">

`integer`

</td>
<td class="no-p-margin">

`4`

</td>
<td>
<div class="no-p-margin">

If `target=none`, the effect will be applied all entities within this range.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=effect,ID=instant_damage,range=10</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>excludePlayer
                
                
            </td>
<td class="no-p-margin">

`true`\|`false`

</td>
<td class="no-p-margin">

`false`

</td>
<td>
<div class="no-p-margin">

If `target=none`, this determines whether the player who initiated the effect should be excluded.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=effect,ID=poison,exludePlayer=true</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

### Special effect

Special effects are non-standard effects added by the mod.

<table>
    <thead>
        <th>ID</th>
        <th>Extra Properies</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <tr>
            <td><code>special_fire</code></td>
            <td>
                <!-- prettier-ignore -->
                <ul>
<li class="no-p-margin">

duration: `integer`

</li>
                </ul>
            </td>
            <!-- prettier-ignore -->
            <td>
<div class="no-p-margin">

Sets an entity on fire for the given number of seconds.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=effect,ID=special_fire,duration=5</code>
                </div>
            </td>
        </tr>
        <tr>
            <td><code>special_knockback</code></td>
            <td>
                <!-- prettier-ignore -->
                <ul>
<li class="no-p-margin">

power: `float`

</li>
<li class="no-p-margin">

directionYaw: `0..360`

</li>
<li class="no-p-margin">

directionPitch: `-90..90`

</li>
                </ul>
            </td>
            <!-- prettier-ignore -->
            <td>
<div class="no-p-margin">

Pushes the entity away in a given direction (yaw 0 = south, pitch 90 = down) with a given power. By default, the angles are calculated away from the source of the effect.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=effect,ID=special_knockback,ragne=4,power=2.5</code>
                </div>
            </td>
        </tr>
    </tbody>
</table>
<!-- prettier-ignore -->

<!-- -->

## Explosion

-   `type=explosion`

Creates an explosion.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>radius
                
                
            </td>
<td class="no-p-margin">

`integer`

</td>
<td class="no-p-margin">

`4`

</td>
<td>
<div class="no-p-margin">

The size of the explosion. For reference, a creeper explosion has radius 3, TNT has 4, and a charged creeper has 6

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=explosion,radius=7</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>fire
                
                
            </td>
<td class="no-p-margin">

`true`\|`false`

</td>
<td class="no-p-margin">

`false`

</td>
<td>
<div class="no-p-margin">

Whether the explosion will set blocks on fire, in the same way as a charged creeper.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=explosion,fire=true</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

## Fill

-   `type=fill`

Fills an area blocks.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>ID
                
                
            </td>
<td class="no-p-margin">

[Block ID](https://minecraft.gamepedia.com/Java_Edition_data_value#Blocks)

</td>
<td class="no-p-margin">



</td>
<td>
<div class="no-p-margin">

The ID of the block.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=fill,ID=lava,size=(3,3,3)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>state
                
                
            </td>
<td class="no-p-margin">

[NBT Tag](property-values#nbt-tags)

</td>
<td class="no-p-margin">

`()`

</td>
<td>
<div class="no-p-margin">

The [block state](https://minecraft.gamepedia.com/Block_states).

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=fill,ID=cake,state=(bites=4),size=(3,1,3)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>size, width, height, length
                
                
            </td>
<td class="no-p-margin">

`(integer, integer, integer)` \| `integer`

</td>
<td class="no-p-margin">

`(1,1,1)`

</td>
<td>
<div class="no-p-margin">

The size of the fill area. Use `size` to set all dimensions at once, or `length`/`width`/`height` to set each separately. When facing north, the initial outcome position will appear in the bottom, north-most, west-most corner of the fill area.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=fill,ID=air,size=(5,3,5)</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>pos2, pos2X, pos2Y, pos2Z
                
                
            </td>
<td class="no-p-margin">

`(integer, integer, integer)` \| `integer`

</td>
<td class="no-p-margin">



</td>
<td>
<div class="no-p-margin">

An alternative way of setting the fill area, so that each dimension is defined by two points: the inital outcome position and the one given here. Use `pos2` to set all coordinates at once, or `pos2{X,Y,Z}` to set each separately.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=fill,ID=air,posOffset=(-1,0,-1),pos2=(#pPosX+1,#pPosY-10,#pPosZ+1)</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

## Message

-   `type=message`

Shows a message in the chat.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>ID
                
                
            </td>
<td class="no-p-margin">

`text`

</td>
<td class="no-p-margin">



</td>
<td>
<div class="no-p-margin">

The message text.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=message,ID="Hello, #pName"</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

## Particle

-   `type=particle`

Creates one or more particles.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>ID
                
                
            </td>
<td class="no-p-margin">

`text`

</td>
<td class="no-p-margin">



</td>
<td>
<div class="no-p-margin">

The message text.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=message,ID="Hello, #pName"</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

### Special particle

Special particles are particles/animations which exist game by default, but don't have standard IDs. They may also be accompanied by a sound.

<table>
    <thead>
        <th>ID</th>
        <th>Extra Properies</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <tr>
            <td><code>splashpotion</code></td>
            <td>
                <!-- prettier-ignore -->
                <ul>
<li class="no-p-margin">

potion: [Status effect ID](https://minecraft.gamepedia.com/Status_effect)

</li>
                </ul>
            </td>
            <!-- prettier-ignore -->
            <td>
<div class="no-p-margin">

The breaking animation of a splash potion (with sound).

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=particle,ID=splashpotion,potion=night_vision</code>
                </div>
            </td>
        </tr>
    </tbody>
</table>
<!-- prettier-ignore -->

<!-- -->

## Sound

-   `type=sound`

Plays a Minecraft sound.

<table>
    <thead>
        <th>Name</th>
        <th>Type</th>
        <th>Default</th>
        <th>Description/Examples</th>
    </thead>
    <tbody>
        <!-- prettier-ignore -->
        <tr>
            <td>ID
                
                
            </td>
<td class="no-p-margin">

[Sound ID](https://minecraft.gamepedia.com/Sounds.json#Sound_events)

</td>
<td class="no-p-margin">



</td>
<td>
<div class="no-p-margin">

The sound resource ID.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=sound,ID=mob.pig.say</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>volume
                
                
            </td>
<td class="no-p-margin">

`float`

</td>
<td class="no-p-margin">

`1.0`

</td>
<td>
<div class="no-p-margin">

The volume at which the sound will be played.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=sound,ID=block.bell.use,volume=3.0</code>
                </div>
                            </td>
        </tr>
        <!-- prettier-ignore -->
        <tr>
            <td>pitch
                
                
            </td>
<td class="no-p-margin">

`1.0..2.0`

</td>
<td class="no-p-margin">

`1.0`

</td>
<td>
<div class="no-p-margin">

The pitch at which the sound will be played.

</div>
                <br />
                <div style="word-break: break-all;">
                    <code>type=sound,ID=block.note_block.harp,pitch=1.5</code>
                </div>
                            </td>
        </tr>
    </tbody>
</table>
<br />
<!-- prettier-ignore -->

<!-- -->

## Nothing

-   `type=nothing`

Does nothing. Mainly used for the Lucky Sword when no additional effect is desired.
