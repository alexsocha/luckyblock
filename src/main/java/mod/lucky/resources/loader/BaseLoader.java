package mod.lucky.resources.loader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import mod.lucky.Lucky;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.item.*;
import mod.lucky.resources.BaseResource;
import mod.lucky.util.LuckyReader;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;

import javax.annotation.Nullable;

public abstract class BaseLoader {
    private BlockLuckyBlock luckyBlock;
    private ItemLuckySword luckySword;
    private ItemLuckyBow luckyBow;
    private ItemLuckyPotion luckyPotion;

    private ArrayList<IRecipe> recipes = new ArrayList<>();

    public abstract InputStream getResourceStream(BaseResource resource);

    public void loadResource(BaseResource resource) {
        try {
            InputStream stream = this.getResourceStream(resource);
            if (stream == null) return;
            LuckyReader reader = new LuckyReader(new InputStreamReader(stream));
            resource.process(reader, this);
        } catch (Exception e) {
            Lucky.error(e, "Lucky Block: Error loading resource: " + resource.getPath());
            Lucky.error(e, "Error loading resource: " + resource.getPath());
        }
    }

    public void setBlock(BlockLuckyBlock block) { this.luckyBlock = block; }
    public BlockLuckyBlock getBlock() { return this.luckyBlock; }
    public ItemLuckyBlock getBlockItem() {
        return (ItemLuckyBlock) Item.BLOCK_TO_ITEM.get(this.getBlock());
    }

    public void setSword(ItemLuckySword sword) { this.luckySword = sword; }
    @Nullable public ItemLuckySword getSword() { return this.luckySword; }

    public void setBow(ItemLuckyBow bow) { this.luckyBow = bow; }
    @Nullable public ItemLuckyBow getBow() { return this.luckyBow; }

    public void setPotion(ItemLuckyPotion potion) { this.luckyPotion = potion; }
    @Nullable public ItemLuckyPotion getPotion() { return this.luckyPotion; }

    public ArrayList<ILuckyItemContainer> getAllItems() {
        ArrayList<ILuckyItemContainer> result = new ArrayList<>();

        result.add(this.getBlockItem());
        if (this.getSword() != null) result.add(this.getSword());
        if (this.getBow() != null) result.add(this.getBow());
        if (this.getPotion() != null) result.add(this.getPotion());

        return result;
    }

    public void addRecipe(IRecipe recipe) { this.recipes.add(recipe); }
    public ArrayList<IRecipe> getRecipes() { return this.recipes; }
}
