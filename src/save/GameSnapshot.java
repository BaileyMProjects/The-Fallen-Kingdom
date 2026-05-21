package save;

import characters.Player;
import core.Difficulty;
import world.LocationId;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * GameSnapshot — a fully serializable point-in-time snapshot of a game session.
 *
 * The Player (with inventory and equipment) is serialized directly.
 * World state is captured as lightweight maps/sets so the world graph itself
 * (which contains circular location references) does not need to be serialized.
 * On load the world is rebuilt fresh, then these deltas are applied.
 */
public class GameSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Core game state ───────────────────────────────────────────────────────

    /** Fully serialized player including inventory and all equipment. */
    public final Player player;
    public final Difficulty  difficulty;
    public final LocationId  locationId;

    // ── World deltas (applied on top of a freshly built world) ───────────────

    /** Remaining enemy count per location (every location is stored, 0 = all dead). */
    public final Map<String, Integer>      enemyCounts;

    /** Remaining ground-item names per location (only items still on the floor). */
    public final Map<String, List<String>> groundItems;

    /** LocationId names whose puzzle has been solved. */
    public final Set<String> solvedPuzzles;

    /** "LOCATION_ID:DIRECTION" pairs for exits that were locked at init but are now open. */
    public final Set<String> unlockedExits;

    // ── Menu display metadata (readable without full deserialization) ─────────

    public final String characterName;
    public final int    characterLevel;
    public final String locationDisplay;
    public final long   saveTimestamp;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public GameSnapshot(Player p, Difficulty d, LocationId locId,
                        Map<String, Integer>      enemyCounts,
                        Map<String, List<String>> groundItems,
                        Set<String>               solvedPuzzles,
                        Set<String>               unlockedExits) {
        this.player          = p;
        this.difficulty      = d;
        this.locationId      = locId;
        this.enemyCounts     = new HashMap<>(enemyCounts);
        this.groundItems     = new HashMap<>(groundItems);
        this.solvedPuzzles   = new HashSet<>(solvedPuzzles);
        this.unlockedExits   = new HashSet<>(unlockedExits);
        this.characterName   = p.getName();
        this.characterLevel  = p.getLevel();
        this.locationDisplay = locId.name().replace("_", " ").toLowerCase();
        this.saveTimestamp   = System.currentTimeMillis();
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    /** One-line summary shown in the save-slot button on the main menu. */
    public String getSummary() {
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(saveTimestamp));
        return characterName + "  Lv." + characterLevel
                + "  —  " + locationDisplay + "  [" + date + "]";
    }
}
