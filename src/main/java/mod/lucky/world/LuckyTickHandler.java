package mod.lucky.world;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LuckyTickHandler {
    private ConcurrentHashMap<Integer, Object> delayDrops;
    private boolean shownUpdateVersion = true;

    public LuckyTickHandler() {
        try {
            this.delayDrops = new ConcurrentHashMap<Integer, Object>();
        } catch (Exception e) {}
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        try {
            if (Minecraft.getInstance().player != null && this.shownUpdateVersion) {
                this.shownUpdateVersion = false;

                URL url = new URL("http://www.minecraftascending.com/projects/lucky_block/download/version/version_log.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                int curLuckyVersion = Integer.valueOf(Lucky.VERSION.replace(".", ""));
                int curMinecraftVersion = Integer.valueOf(Lucky.MC_VERSION.replace(".", ""));

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split("\\|");
                    int luckyVersion = Integer.valueOf(split[0].replace(".", ""));
                    int minecraftVersion = Integer.valueOf(split[1].replace(".", ""));

                    if (minecraftVersion >= curMinecraftVersion && luckyVersion > curLuckyVersion) {
                        String message = split[2];
                        ITextComponent textComponent = ITextComponent.Serializer.fromJson(message);
                        Minecraft.getInstance().player.sendMessage(textComponent);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
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
            System.err.println("Lucky Block: Error processing delay drop");
            e.printStackTrace();
            this.delayDrops.clear();
        }
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save event) {
        try {
            if (this.delayDrops.size() > 0) {
                boolean saved = false;
                NBTTagList dropTags = new NBTTagList();

                for (int i = 0; i > -1; i++) {
                    if (this.delayDrops.containsKey(i)
                    && this.delayDrops.get(i) instanceof DelayLuckyDrop) {
                        DelayLuckyDrop delayDrop = (DelayLuckyDrop) this.delayDrops.get(i);

                        BlockPos harvestPos = delayDrop.getProcessData().getHarvestBlockPos();
                        ChunkPos harvestChunkPos = event.getChunk().getWorldForge()
                            .getChunkDefault(harvestPos).getPos();

                        if (harvestChunkPos == event.getChunk().getPos()) {
                            dropTags.add(delayDrop.writeToNBT());
                            saved = true;
                        }
                    }
                    if (!this.delayDrops.containsKey(i)) break;
                }

                if (saved) event.getData().setTag("LuckyBlockDelayDrops", dropTags);
            }
        } catch (Exception e) {
            Lucky.LOGGER.error("Error saving chunk properties");
            this.delayDrops.clear();
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load event) {
        try {
            if (event.getData().hasKey("LuckyBlockDelayDrops")) {
                NBTTagList delayDropTags = event.getData().getList("LuckyBlockDelayDrops", 10);
                for (int i = 0; i < delayDropTags.size(); i++) {
                    DelayLuckyDrop delayDrop =
                        new DelayLuckyDrop(Lucky.luckyBlock.getDropProcessor(), null, 0);
                    delayDrop.readFromNBT(delayDropTags.getCompound(i),
                        event.getChunk().getWorldForge().getWorld());
                    this.addDelayDrop(delayDrop);
                }
            }
        } catch (Exception e) {
            System.err.println("Lucky Block: Error loading chunk properties");
            e.printStackTrace();
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
