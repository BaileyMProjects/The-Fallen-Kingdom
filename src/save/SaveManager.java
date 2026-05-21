package save;

import java.io.*;
import java.nio.file.*;

/**
 * SaveManager — reads and writes the three save slots to disk.
 *
 * Files are stored in a "saves/" directory next to the working directory.
 * Each slot is a serialized GameSnapshot object written with ObjectOutputStream.
 *
 * All I/O errors are caught and reported via System.out; callers receive null
 * on a failed load and a no-op on a failed save rather than crashing.
 */
public class SaveManager {

    private static final String SAVE_DIR = "saves";
    private static final String SAVE_EXT = ".sav";
    public  static final int    NUM_SLOTS = 3;

    private SaveManager() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Serializes the snapshot to saves/saveN.sav. */
    public static void save(int slot, GameSnapshot snapshot) {
        checkSlot(slot);
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
            Path file = slotPath(slot);
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(file)))) {
                oos.writeObject(snapshot);
            }
        } catch (IOException e) {
            System.out.println("  [Warning] Save failed: " + e.getMessage());
        }
    }

    /**
     * Loads and returns the snapshot from saves/saveN.sav.
     * Returns null if the slot is empty or the file cannot be read.
     */
    public static GameSnapshot load(int slot) {
        checkSlot(slot);
        Path file = slotPath(slot);
        if (!Files.exists(file)) return null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            return (GameSnapshot) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("  [Warning] Load failed: " + e.getMessage());
            return null;
        }
    }

    /** Deletes the save file for the given slot (no-op if it does not exist). */
    public static void delete(int slot) {
        checkSlot(slot);
        try { Files.deleteIfExists(slotPath(slot)); }
        catch (IOException ignored) {}
    }

    /** Returns true if a save file exists for the given slot. */
    public static boolean exists(int slot) {
        checkSlot(slot);
        return Files.exists(slotPath(slot));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Path slotPath(int slot) {
        return Paths.get(SAVE_DIR, "save" + slot + SAVE_EXT);
    }

    private static void checkSlot(int slot) {
        if (slot < 1 || slot > NUM_SLOTS)
            throw new IllegalArgumentException("Slot must be 1–" + NUM_SLOTS);
    }
}
