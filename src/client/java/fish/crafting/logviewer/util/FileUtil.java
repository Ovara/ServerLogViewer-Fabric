package fish.crafting.logviewer.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class FileUtil {

    public static File getDataDir(){
        File dir = FabricLoader.getInstance().getGameDir().toFile();
        return new File(dir, "serverlogviewer");
    }

}
