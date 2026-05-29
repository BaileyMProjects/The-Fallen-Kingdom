package util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

/**
 * Loads enemy sprite images for display in combat.
 * File naming matches AsciiArtLoader: "Shadow Goblin" → shadow_goblin.png
 *
 * Images are first looked up on the classpath (so they load when bundled inside
 * the executable jar), then fall back to resources/images/ on the filesystem
 * (so they still load when run from the project root).  Returns null if no
 * image is found so CombatPanel can fall back to text art.
 */
public class EnemyImageLoader {

    private static final String IMAGE_DIR       = "resources/images/"; // filesystem (dev runs)
    private static final String IMAGE_CLASSPATH = "/images/";          // inside the jar

    private EnemyImageLoader() {}

    /**
     * Loads and scales the sprite for the named enemy.
     *
     * @param enemyName display name (e.g. "Shadow Goblin")
     * @param maxWidth  maximum display width in pixels
     * @param maxHeight maximum display height in pixels
     * @return scaled ImageIcon, or null if no image file exists
     */
    public static ImageIcon load(String enemyName, int maxWidth, int maxHeight) {
        String filename = toFilename(enemyName);

        URL url = EnemyImageLoader.class.getResource(IMAGE_CLASSPATH + filename);
        if (url != null) {
            ImageIcon raw = new ImageIcon(url);
            if (raw.getIconWidth() > 0) return scaledToFit(raw, maxWidth, maxHeight);
        }

        File file = new File(IMAGE_DIR + filename);
        if (file.exists()) {
            ImageIcon raw = new ImageIcon(file.getAbsolutePath());
            if (raw.getIconWidth() > 0) return scaledToFit(raw, maxWidth, maxHeight);
        }

        return null;
    }

    private static ImageIcon scaledToFit(ImageIcon icon, int maxW, int maxH) {
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        double scale = Math.min((double) maxW / w, (double) maxH / h);
        int newW = (int) (w * scale);
        int newH = (int) (h * scale);
        Image scaled = icon.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static String toFilename(String name) {
        return name.toLowerCase().replace(" ", "_").replace("'", "") + ".png";
    }
}
