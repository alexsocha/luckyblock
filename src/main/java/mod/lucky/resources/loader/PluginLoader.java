package mod.lucky.resources.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import mod.lucky.Lucky;
import mod.lucky.resources.BaseResource;
import mod.lucky.resources.ResourcePluginInit;
import net.minecraft.resources.FilePack;
import net.minecraft.resources.FolderPack;
import net.minecraft.resources.IResourcePack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PluginLoader extends BaseLoader {
    private File pluginFile;
    private String pluginName;

    public PluginLoader(File pluginFile) {
        this.pluginFile = pluginFile;
        this.pluginName = pluginFile.getName();
    }

    public void registerPlugin() {
        this.loadResource(new ResourcePluginInit());
    }

    /*
    public void initializePlugin() {
        try {
            GameRegistry.registerWorldGenerator(this.getBlock().getWorldGenerator(), 1);
        } catch (Exception e) {
            System.err.println(
                "Lucky Block Addons: Error initializing generation for add-on: " + this.pluginFile != null
                    ? this.pluginFile.toString()
                    : "unknown");
        }
    }*/

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
                ZipEntry entry = file.getEntry(resource.getPath());
                if (entry == null || entry.isDirectory()) return null;
                InputStream stream = file.getInputStream(entry);
                return stream;
            }
        } catch (Exception e) {
            if (!resource.isOptional()) {
                Lucky.LOGGER.error("Error getting resource '" + resource.getPath()
                    + "' from plugin '" + this.pluginFile.getName() + "'");
            }
        }
        return null;
    }

    public File getFile(BaseResource resource) {
        return new File(this.pluginFile.getPath() + "/" + resource.getPath());
    }

    @OnlyIn(Dist.CLIENT)
    public IResourcePack getResourcePack() {
        IResourcePack pack = null;
        if (this.pluginFile.isDirectory()) return new FolderPack(this.pluginFile);
        else return new FilePack(this.pluginFile);
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
