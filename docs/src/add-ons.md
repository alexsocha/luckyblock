# Add-ons

Add-ons add new blocks and items similar to the standard ones, but with custom outcomes.

## Installing

1. Run Minecraft at least once with the Lucky Block mod installed.
2. Place the add-on file/folder in `.minecraft/addons/lucky/`
3. Run Minecraft again, now with the add-on installed.

When using an add-on on a server, the add-on must installed on both the server and the client due to the custom textures.

## Creating

The file structure of an add-on is very similar to the standard [configuration files](configuration-files), but with additional assets. Add-ons can be distributed as a folder, `.zip` archive (preferred), or `.jar` archive.

```
{add-on name}{.zip,.jar,folder}
plugin_init.txt
drops.txt
sword_drops.txt
bow_drops.txt
potion_drops.txt
properties.txt
recipes.txt
natural_gen.txt
luck_crafting.txt
structures.txt
structures/...
assets/lucky/textures/blocks
assets/lucky/textures/blocks/custom_lucky_block.png
assets/lucky/textures/items/custom_lucky_bow_standby.png
assets/lucky/textures/items/custom_lucky_bow_pulling_0.png
assets/lucky/textures/items/custom_lucky_bow_pulling_1.png
assets/lucky/textures/items/custom_lucky_bow_pulling_2.png
assets/lucky/textures/items/custom_lucky_potion.png
assets/lucky/textures/items/custom_lucky_sword.png
assets/lucky/models/item/custom_lucky_bow_pulling_0.json
assets/lucky/models/item/custom_lucky_bow_pulling_1.json
assets/lucky/models/item/custom_lucky_bow_pulling_2.json
assets/lucky/models/item/custom_lucky_sword.json
assets/lucky/models/item/custom_lucky_block.json
assets/lucky/models/item/custom_lucky_bow.json
assets/lucky/models/item/custom_lucky_potion.json
assets/lucky/models/block/custom_lucky_block.json
assets/lucky/blockstates/custom_lucky_block.json
assets/lucky/lang/en_us.json
pack.mcmeta
```

**Instructions**

-   Download the template add-on (below) to get started.
-   Edit `plugin_init.txt` to specify the ID of the new block and (optionally) items. If an item is omitted, it won't be added to the game and you can ignore all configuration files related to it.
    ```
    block_id=custom_lucky_block
    sword_id=custom_lucky_sword
    bow_id=custom_lucky_bow
    potion_id=custom_lucky_potion
    ```
-   Rename all of the files containing the word `custom` to match the new IDs.
-   Edit each `.json` in `blockstates`/`models` and update the IDs within. You could potentially do a find-and-replace on the word `custom`.
    -   These files can also be used to create custom models.
-   Open `en_us.json` and update both the IDs and in-game display names.
    ```
    {
        "block.lucky.custom_lucky_block": "Custom Lucky Block",
        "item.lucky.custom_lucky_sword": "Custom Lucky Sword",
        "item.lucky.custom_lucky_bow": "Custom Lucky Bow",
        "item.lucky.custom_lucky_potion": "Custom Lucky Potion"
    }
    ```
    -   You can also add translations for other languages.
-   Update the texture `.png` files.
-   Leave the file `pack.mcmeta` unchanged. Add-ons are loaded in a similar way to resource packs, and an error message will appear if this file doesn't exist. Otherwise it serves no purpose.
-   Use the remaining configuration files customize the add-on as you like.
-   When referencing the ID of the new block/items, use the prefix `lucky:`.

## Template add-on

{{!-- server-side rendering --}}

<table>
    <thead>
        <th>Version</th>
        <th>Minecraft Version</th>
        <th>Lucky Block Version</th>
        <th>Link</th>
    </thead>
    <tbody>
        \{{#each projects.custom-lucky-block-java}}
        <!-- prettier-ignore -->
        <tr>
            <td>\{{{this.version}}}</td>
            <td>\{{{this.dependencies.minecraft.formattedVersionRange}}}</td>
            <td>\{{{this.dependencies.lucky-block.formattedVersionRange}}}</td>
            <td><a href="/instant-download/custom-lucky-block-java/\{{{this.version}}}">Download</td>
        </tr>
        \{{/each}}
    </tbody>
</table>
