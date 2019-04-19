package mod.lucky.resources.loader;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import mod.lucky.Lucky;
import mod.lucky.resources.BaseResource;

public class DefaultLoader extends BaseLoader {
  private File resourceDir;

  public DefaultLoader(File minecraftDirectory) {
    this.resourceDir =
        new File(minecraftDirectory.getPath() + "/config/lucky_block/version-" + Lucky.VERSION);
    this.setLuckyBlockItems(
        Lucky.getInstance().lucky_block,
        Lucky.getInstance().lucky_sword,
        Lucky.getInstance().lucky_bow,
        Lucky.getInstance().lucky_potion);
  }

  public void extractDefaultResources() {
    try {
      InputStream stream = Lucky.class.getResourceAsStream("default_config.zip");
      ZipInputStream inputStream = new ZipInputStream(stream);

      ZipEntry entry;
      while ((entry = inputStream.getNextEntry()) != null) {
        FileOutputStream outputStream = null;
        File dest = new File(this.resourceDir.getPath() + "/" + entry.getName());
        if (!entry.isDirectory() && !dest.exists()) {
          if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
          dest.createNewFile();
          outputStream = new FileOutputStream(dest);
        }

        int data;
        while ((data = inputStream.read()) != -1)
          if (outputStream != null) outputStream.write(data);
        if (outputStream != null) outputStream.close();
      }
      inputStream.close();
    } catch (Exception e) {
      System.err.println("Lucky Block: Error extracting default resources");
      e.printStackTrace();
    }
  }

  public File getFile(BaseResource resource) {
    File defaultFile = new File(this.resourceDir.getPath() + "/" + resource.getDirectory());
    return defaultFile;
  }

  @Override
  public InputStream getResourceStream(BaseResource resource) {
    try {
      File file = this.getFile(resource);
      if (file.isDirectory()) return null;
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
      System.err.println(
          "Lucky Block: Error getting default resource '" + resource.getDirectory() + "'");
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void loadResource(BaseResource resource) {
    super.loadResource(resource);
  }

  public File getResourceDir() {
    return this.resourceDir;
  }
}
