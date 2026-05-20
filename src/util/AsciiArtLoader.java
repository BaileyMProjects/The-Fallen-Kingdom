package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * AsciiArtLoader — reads enemy ASCII art from the resources/ascii/ directory.
 *
 * File naming: the enemy's display name is lowercased and spaces replaced with
 * underscores, e.g. "Shadow Goblin" → resources/ascii/shadow_goblin.txt.
 *
 * Paths are resolved relative to the working directory, which is the project
 * root when the game is launched as described in README.md.  A missing file
 * returns a placeholder string instead of throwing, so the GUI degrades
 * gracefully if an art file has not been added yet.
 */
public class AsciiArtLoader {

    private static final String ART_DIR = "resources/ascii/";

    private AsciiArtLoader() {}

    /**
     * Loads and returns the ASCII art for the named enemy.
     *
     * @param enemyName display name of the enemy (e.g. "Shadow Goblin")
     * @return multi-line ASCII art string, or a placeholder on failure
     */
    public static String load(String enemyName) {
        String filename = toFilename(enemyName);
        File file = new File(ART_DIR + filename);
        if (!file.exists()) {
            return buildPlaceholder(enemyName);
        }
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            return buildPlaceholder(enemyName);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String toFilename(String name) {
        return name.toLowerCase().replace(" ", "_").replace("'", "") + ".txt";
    }

    private static String buildPlaceholder(String name) {
        return "  ???\n  [ " + name.toUpperCase() + " ]\n  ???\n";
    }
}
