package util;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Loads enemy sprite images from resources/images/.
 * File naming matches AsciiArtLoader: "Shadow Goblin" → shadow_goblin.png
 * Returns null if no image file exists so CombatPanel can fall back to text art.
 */
public class EnemyImageLoader {

    private static final String IMAGE_DIR = "resources/images/";

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
        File file = new File(IMAGE_DIR + toFilename(enemyName));
        if (!file.exists()) return null;

        ImageIcon raw = new ImageIcon(file.getAbsolutePath());
        if (raw.getIconWidth() <= 0) return null;

        return scaledToFit(raw, maxWidth, maxHeight);
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
