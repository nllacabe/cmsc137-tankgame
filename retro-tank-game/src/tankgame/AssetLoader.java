package tankgame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

// loads image assets from the assets folder
// returns null pag di mahanap asset, but there are fallback elements
//  usage:
//  BufferedImage img = AssetLoader.get("tank");
//  looks for assets/tank.png, then assets/tank.jpg

public class AssetLoader {

    private static final String ASSETS_DIR = "assets";
    private static final String[] EXTENSIONS = { ".png", ".jpg", ".jpeg", ".gif", ".bmp" };

    // file is only loaded one time
    private static final Map<String, BufferedImage> cache = new HashMap<String, BufferedImage>();

    // Get an image by base name (e.g. "tank", "bullet").
    // @param name base name without extension
    // @return the loaded image, or null if not found

    public static BufferedImage get(String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }

        BufferedImage image = loadFromDisk(name);
        cache.put(name, image); // cache even if null to avoid repeated lookups
        return image;
    }

    // reload all cached assets from disk. USE PAG MAY BAGONG ASSETS NA DAGDAG
    public static void reloadAll() {
        for (String name : cache.keySet()) {
            cache.put(name, loadFromDisk(name));
        }
    }

    private static BufferedImage loadFromDisk(String name) {
        for (String ext : EXTENSIONS) {
            File file = new File(ASSETS_DIR + File.separator + name + ext);
            if (file.exists()) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    if (img != null) {
                        System.out.println("[AssetLoader] Loaded: " + file.getPath());
                        return img;
                    }
                } catch (Exception e) {
                    System.err.println("[AssetLoader] Failed to load " + file.getPath() + ": " + e.getMessage());
                }
            }
        }
        return null;
    }

    private AssetLoader() {
    }
}
