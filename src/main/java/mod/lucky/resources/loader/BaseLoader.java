package mod.lucky.resources.loader;

import java.io.InputStream;
import java.io.InputStreamReader;

import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.item.ItemLuckyBow;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.item.ItemLuckySword;
import mod.lucky.resources.BaseResource;
import mod.lucky.util.LuckyReader;

public abstract class BaseLoader {
    private BlockLuckyBlock lucky_block;
    private ItemLuckySword lucky_sword;
    private ItemLuckyBow lucky_bow;
    private ItemLuckyPotion lucky_potion;

    public abstract InputStream getResourceStream(BaseResource resource);

    public void loadResource(BaseResource resource) {
        try {
            InputStream stream = this.getResourceStream(resource);
            if (stream == null) return;
            LuckyReader reader = new LuckyReader(new InputStreamReader(stream));
            resource.process(reader, this);
        } catch (Exception e) {
            System.err.println("Lucky Block: Error loading resource: " + resource.getDirectory());
            e.printStackTrace();
        }
    }

    public BlockLuckyBlock getBlock() {
        return this.lucky_block;
    }

    public ItemLuckySword getSword() {
        return this.lucky_sword;
    }

    public ItemLuckyBow getBow() {
        return this.lucky_bow;
    }

    public ItemLuckyPotion getPotion() {
        return this.lucky_potion;
    }

    public void setLuckyBlockItems(
        BlockLuckyBlock lucky_block,
        ItemLuckySword lucky_sword,
        ItemLuckyBow lucky_bow,
        ItemLuckyPotion lucky_potion) {
        this.lucky_block = lucky_block;
        this.lucky_sword = lucky_sword;
        this.lucky_bow = lucky_bow;
        this.lucky_potion = lucky_potion;
    }
}
