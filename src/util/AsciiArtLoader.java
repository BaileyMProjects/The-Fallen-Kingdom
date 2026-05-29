package util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * AsciiArtLoader — reads enemy ASCII art for display in combat.
 *
 * File naming: the enemy's display name is lowercased and spaces replaced with
 * underscores, e.g. "Shadow Goblin" → shadow_goblin.txt.
 *
 * Art is first looked up on the classpath (so it loads when bundled inside the
 * executable jar), then falls back to resources/ascii/ on the filesystem (so it
 * still loads when run from the project root as described in README.md).  A
 * missing file returns a placeholder string instead of throwing, so the GUI
 * degrades gracefully if an art file has not been added yet.
 */
public class AsciiArtLoader {

    private static final String ART_DIR       = "resources/ascii/"; // filesystem (dev runs)
    private static final String ART_CLASSPATH = "/ascii/";          // inside the jar

    private AsciiArtLoader() {}

    /**
     * Loads and returns the ASCII art for the named enemy.
     *
     * @param enemyName display name of the enemy (e.g. "Shadow Goblin")
     * @return multi-line ASCII art string, or a placeholder on failure
     */
    public static String load(String enemyName) {
        String filename = toFilename(enemyName);

        try (InputStream in = AsciiArtLoader.class.getResourceAsStream(ART_CLASSPATH + filename)) {
            if (in != null) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
            // fall through to the filesystem
        }

        File file = new File(ART_DIR + filename);
        if (file.exists()) {
            try {
                return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            } catch (IOException ignored) {
                // fall through to the placeholder
            }
        }

        return buildPlaceholder(enemyName);
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
