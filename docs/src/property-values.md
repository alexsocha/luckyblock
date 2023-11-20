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

{{> template-var-table templateVars=hash_vars.standard }}

### NBT variables

The variables can be used in NBT tags.

{{> template-var-table templateVars=hash_vars.nbt }}

### Structure variables

These variables can only be used within `.luckystruct` structure files. They provide useful information about the context in which the structure is generated.

{{> template-var-table templateVars=hash_vars.structure }}

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
