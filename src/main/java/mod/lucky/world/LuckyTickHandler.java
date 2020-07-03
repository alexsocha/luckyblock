package mod.lucky.world;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LuckyTickHandler {
    private ConcurrentHashMap<Integer, Object> delayDrops;
    private boolean alreadyShownMessage = false;
    private static boolean showUpdateMessage = true;

    public LuckyTickHandler() {
        try {
            this.delayDrops = new ConcurrentHashMap<Integer, Object>();
        } catch (Exception e) {}
    }

    public static void setShowUpdateMessage(boolean showUpdateMessage) {
        LuckyTickHandler.showUpdateMessage = showUpdateMessage;
    }

    private static int compareVersions(String v1, String v2) {
        String splitChar = v1.contains("-") || v2.contains("-") ? "-"
            : v1.contains(".") || v2.contains(".") ? "."
            : "";

        if (!splitChar.equals("")) {
            String[] v1Parts = v1.split(Pattern.quote(splitChar));
            String[] v2Parts = v2.split(Pattern.quote(splitChar));
            for (int i = 0; i < Math.max(v1Parts.length, v2Parts.length); i++) {
                if (i >= v1Parts.length) return -1;
                else if (i >= v2Parts.length) return 1;
                else {
                    int c = compareVersions(v1Parts[i], v2Parts[i]);
                    if (c != 0) return c;
                }
            }
            return 0;

        } else {
            return Integer.valueOf(v1).compareTo(Integer.valueOf(v2));
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        try {
            if (LuckyTickHandler.showUpdateMessage
                && Minecraft.getInstance().player != null
                && !this.alreadyShownMessage) {

                this.alreadyShownMessage = true;

                URL url = new URL("http://www.minecraftascending.com/projects/lucky_block/download/version/version_log.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split("\\|");
                    String luckyVersion = split[0];
                    String minecraftVersion = split[1];

                    if (compareVersions(Lucky.MC_VERSION, minecraftVersion) >= 0 && compareVersions(Lucky.VERSION, luckyVersion) > 0) {
                        String message = split[2];
                        ITextComponent textComponent = ITextComponent.Serializer.func_240643_a_(message); // fromJson
                        Minecraft.getInstance().player.sendMessage(textComponent, Minecraft.getInstance().player.getUniqueID());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Lucky.error(e, "Error showing update message");
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onServerTick(TickEvent.ServerTickEvent event) {
        try {
            for (int i = 0; i > -1; i++) {
                if (this.delayDrops.containsKey(i) && this.delayDrops.get(i) instanceof DelayLuckyDrop) {
                    DelayLuckyDrop delayDrop = (DelayLuckyDrop) this.delayDrops.get(i);
                    delayDrop.update();
                    if (delayDrop.finished()) {
                        this.delayDrops.remove(i);
                        if (this.delayDrops.containsKey(i + 1)) this.delayDrops.put(i, 0);
                    }
                }
                if (!this.delayDrops.containsKey(i)) break;
            }
        } catch (Exception e) {
            Lucky.error(e, "Error processing delay drop");
            this.delayDrops.clear();
        }
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save event) {
        try {
            if (this.delayDrops.size() > 0) {
                boolean saved = false;
                ListNBT dropTags = new ListNBT();

                for (int i = 0; i > -1; i++) {
                    if (this.delayDrops.containsKey(i)
                    && this.delayDrops.get(i) instanceof DelayLuckyDrop) {
                        DelayLuckyDrop delayDrop = (DelayLuckyDrop) this.delayDrops.get(i);

                        BlockPos harvestPos = delayDrop.getProcessData().getHarvestBlockPos();
                        ChunkPos harvestChunkPos = event.getWorld().getChunk(harvestPos).getPos();

                        if (harvestChunkPos == event.getChunk().getPos()) {
                            dropTags.add(delayDrop.writeToNBT());
                            saved = true;
                        }
                    }
                    if (!this.delayDrops.containsKey(i)) break;
                }

                if (saved) event.getData().put("LuckyBlockDelayDrops", dropTags);
            }
        } catch (Exception e) {
            Lucky.error(e, "Error saving chunk properties");
            this.delayDrops.clear();
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load event) {
        try {
            if (event.getData().contains("LuckyBlockDelayDrops")) {
                ListNBT delayDropTags = event.getData().getList("LuckyBlockDelayDrops", 10);
                for (int i = 0; i < delayDropTags.size(); i++) {
                    DelayLuckyDrop delayDrop =
                        new DelayLuckyDrop(Lucky.luckyBlock.getDropProcessor(), null, 0);
                    delayDrop.readFromNBT(delayDropTags.getCompound(i),
                        event.getChunk().getWorldForge().getWorld());
                    this.addDelayDrop(delayDrop);
                }
            }
        } catch (Exception e) {
            Lucky.error(e, "Error loading chunk properties");
            this.delayDrops.clear();
        }
    }

    public void addDelayDrop(DelayLuckyDrop delayDrop) {
        for (int i = 0; i > -1; i++) {
            if (!this.delayDrops.containsKey(i)
                || !(this.delayDrops.get(i) instanceof DelayLuckyDrop)) {

                this.delayDrops.put(i, delayDrop);
                break;
            }
        }
    }

    public void addDelayDrop(DropProcessor dropProcessor, DropProcessData processData, float delay) {
        this.addDelayDrop(new DelayLuckyDrop(dropProcessor, processData, (long) (delay * 20)));
    }
}
