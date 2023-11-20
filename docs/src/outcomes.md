# Outcomes

## Base properties

These properties apply to all outcome types.

{{> drop-properties-table properties=outcomes.common }}

## Item

-   `type=item`

Drops an item.

{{> drop-properties-table properties=outcomes.item }}

## Block

-   `type=block`

Places a block.

{{> drop-properties-table properties=outcomes.block }}

## Entity

-   `type=entity`

Spawns an entity.

{{> drop-properties-table properties=outcomes.entity }}

## Structure

-   `type=structure`

Generates a structure, which has been [preconfigured](configuration-files#structurestxt).

{{> drop-properties-table properties=outcomes.structure }}

## Command

-   `type=command`

Runs a Minecraft command.

{{> drop-properties-table properties=outcomes.command }}

## Difficulty

-   `type=difficulty`

Sets the difficulty level of the world. This can be used to ensure that monsters spawn.

{{> drop-properties-table properties=outcomes.difficulty }}

## Effect

-   `type=effect`

Gives a status effect to the player and/or surrounding entities.

{{> drop-properties-table properties=outcomes.effect }}

### Special effect

Special effects are non-standard effects added by the mod.

{{> special-id-table ids=outcomes.special_effect }}

## Explosion

-   `type=explosion`

Creates an explosion.

{{> drop-properties-table properties=outcomes.explosion }}

## Fill

-   `type=fill`

Fills an area blocks.

{{> drop-properties-table properties=outcomes.fill }}

## Message

-   `type=message`

Shows a message in the chat.

{{> drop-properties-table properties=outcomes.message }}

## Particle

-   `type=particle`

Creates one or more particles.

{{> drop-properties-table properties=outcomes.message }}

### Special particle

Special particles are particles/animations which exist game by default, but don't have standard IDs. They may also be accompanied by a sound.

{{> special-id-table ids=outcomes.special_particle }}

## Sound

-   `type=sound`

Plays a Minecraft sound.

{{> drop-properties-table properties=outcomes.sound }}

## Nothing

-   `type=nothing`

Does nothing. Mainly used for the Lucky Sword when no additional effect is desired.
