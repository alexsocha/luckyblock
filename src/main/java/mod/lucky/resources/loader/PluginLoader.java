package mod.lucky.resources.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import mod.lucky.resources.BaseResource;
import mod.lucky.resources.ResourcePluginInit;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LegacyV2Adapter;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PluginLoader extends BaseLoader {
    private File pluginFile;
    private String pluginName = "random_block";

    public PluginLoader(File pluginFile) {
        try {
            this.pluginFile = pluginFile;
            this.pluginName = pluginFile.getName();
        } catch (Exception e) {
            System.err.println(
                "Lucky Block: Failed to load plugin loader for file " + pluginFile.toString());
            e.printStackTrace();
        }
    }

    public void registerPlugin() {
        this.loadResource(new ResourcePluginInit());
    }

    public void initializePlugin() {
        try {
            GameRegistry.registerWorldGenerator(this.getBlock().getWorldGenerator(), 1);
        } catch (Exception e) {
            System.err.println(
                "Lucky Block Addons: Error initializing generation for add-on: " + this.pluginFile != null
                    ? this.pluginFile.toString()
                    : "unknown");
        }
    }

    @Override
    public InputStream getResourceStream(BaseResource resource) {
        try {
            if (this.pluginFile.isDirectory()) {
                File file = this.getFile(resource);
                if (file == null || file.isDirectory()) return null;
                return new FileInputStream(file);
            } else {
                @SuppressWarnings("resource")
                ZipFile file = new ZipFile(this.pluginFile);
                ZipEntry entry = file.getEntry(resource.getDirectory());
                if (entry == null || entry.isDirectory()) return null;
                InputStream stream = file.getInputStream(entry);
                return stream;
            }
        } catch (Exception e) {
            if (!resource.isOptional()) {
                System.err.println(
                    "Lucky Block: Error getting resource '"
                        + resource.getDirectory()
                        + "' from plugin '"
                        + this.pluginFile.getName()
                        + "'");
                e.printStackTrace();
            }
        }
        return null;
    }

    public File getFile(BaseResource resource) {
        return new File(this.pluginFile.getPath() + "/" + resource.getDirectory());
    }

    @SideOnly(Side.CLIENT)
    public IResourcePack getResourcePack() {
        IResourcePack pack = null;
        if (this.pluginFile.isDirectory()) pack = new FolderResourcePack(this.pluginFile);
        else pack = new FileResourcePack(this.pluginFile);

        return new LegacyV2Adapter(pack);
    }

    public File getPluginFile() {
        return this.pluginFile;
    }

    public String getPluginName() {
        return this.pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public boolean hasResource(BaseResource resource) {
        return this.getResourceStream(resource) != null;
    }

    @Override
    public void loadResource(BaseResource resource) {
        super.loadResource(resource);
    }
}
