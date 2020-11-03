package mod.lucky.world;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import mod.lucky.drop.func.DropProcessor;
import net.minecraft.client.Minecraft;
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
    private static boolean showUpdate = true;
    private static boolean didShowUpdate = false;
    private HashMap<String, Boolean> didShowUpdateCache = new HashMap<>();

    public LuckyTickHandler() {
        try {
            this.delayDrops = new ConcurrentHashMap<Integer, Object>();
            readDidShowUpdate();
        } catch (Exception e) {}
    }

    public static void setShowUpdateMessage(boolean showUpdateMessage) {
        LuckyTickHandler.showUpdate = showUpdateMessage;
    }

    private static int getVersionNumber() {
        String[] mcv = Lucky.MC_VERSION.toString().split("\\.");
        int luckySubversion = Integer.parseInt(Lucky.VERSION.toString().split("-")[1]);
        return Integer.parseInt(mcv[0]) * 1000000
            + Integer.parseInt(mcv[1]) * 10000
            + Integer.parseInt(mcv[2]) * 100
            + luckySubversion;
    }

    private void readDidShowUpdate() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                Lucky.resourceManager.getDefaultLoader().getFile(".showupdate.cache")));

            String line;
            while ((line = br.readLine()) != null) {
                didShowUpdateCache.put(line, true);
            }
            br.close();
        } catch (Exception e) {}
    }

    private static void writeDidShowUpdate(int version) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(
                Lucky.resourceManager.getDefaultLoader().getFile(".showupdate.cache"), true));

            bw.newLine();
            bw.append(String.valueOf(version));
            bw.close();
        } catch (Exception e) {}
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        try {
            if (LuckyTickHandler.showUpdate
                && Minecraft.getInstance().player != null
                && !LuckyTickHandler.didShowUpdate
            ) {
                LuckyTickHandler.didShowUpdate = true;

                int curVersionNumber = getVersionNumber();

                URL url = new URL("https://www.luckyblockmod.com/version-log");
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split("\\|");
                    if (split.length < 3) continue;
                    int logVersionNumber = Integer.parseInt(split[0]);

                    // log version numbers <= 0 are reserved for special announcements, and should not be cached
                    if (logVersionNumber > 0 && this.didShowUpdateCache.containsKey(String.valueOf(logVersionNumber))) {
                        continue;
                    }

                    if (logVersionNumber > curVersionNumber) {
                        String message = split[2];
                        ITextComponent textComponent = ITextComponent.Serializer.getComponentFromJson(message);
                        Minecraft.getInstance().player.sendMessage(textComponent, Minecraft.getInstance().player.getUniqueID());
                        writeDidShowUpdate(logVersionNumber);
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
                        event.getChunk().getWorldForge());
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
