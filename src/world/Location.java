package world;

import characters.Enemy;
import characters.NPC;
import items.Item;
import puzzles.Puzzle;

import java.util.*;

/**
 * Location — a single room or area in the game world.
 *
 * Each location holds:
 *   - A map of exits (Direction → neighbouring Location)
 *   - Lists of items, enemies, and NPCs currently present
 *   - An optional Puzzle
 *   - A set of locked exits with explanatory messages
 *
 * Locations are connected by World.initializeWorld() after construction;
 * this class is purely a data container with formatting helpers.
 */
public class Location {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final LocationId id;
    private final String     name;
    private final String     description;

    private final Map<Direction, Location> exits;
    private final Set<Direction>           lockedExits;
    private final Map<Direction, String>   lockMessages;

    private final List<Item>  items;
    private final List<Enemy> enemies;
    private final List<NPC>   npcs;

    private Puzzle puzzle;
    private int    levelRequirement = 1;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Location(LocationId id, String name, String description) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.exits        = new EnumMap<>(Direction.class);
        this.lockedExits  = new HashSet<>();
        this.lockMessages = new EnumMap<>(Direction.class);
        this.items    = new ArrayList<>();
        this.enemies  = new ArrayList<>();
        this.npcs     = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Exit management
    // -------------------------------------------------------------------------

    /** Registers a one-way connection from this location in the given direction. */
    public void setExit(Direction dir, Location target) {
        exits.put(dir, target);
    }

    /**
     * Marks an existing exit as locked.
     *
     * @param dir     the direction of the locked exit
     * @param message the reason shown to the player when they try to pass
     */
    public void lockExit(Direction dir, String message) {
        lockedExits.add(dir);
        lockMessages.put(dir, message);
    }

    /** Removes the lock on an exit so the player can pass freely. */
    public void unlockExit(Direction dir) {
        lockedExits.remove(dir);
        lockMessages.remove(dir);
    }

    public boolean isExitLocked(Direction dir)  { return lockedExits.contains(dir); }
    public boolean hasExit(Direction dir)        { return exits.containsKey(dir); }
    public Location getExit(Direction dir)       { return exits.get(dir); }
    public String getLockMessage(Direction dir)  {
        return lockMessages.getOrDefault(dir, "The way is blocked.");
    }

    // -------------------------------------------------------------------------
    // Item management
    // -------------------------------------------------------------------------

    public void addItem(Item item)    { items.add(item); }
    public void removeItem(Item item) { items.remove(item); }
    public List<Item> getItems()      { return Collections.unmodifiableList(items); }

    /**
     * Case-insensitive partial-match search so "sword", "iron sword", and
     * "Iron Sword" all find the same item.
     */
    public Item findItem(String name) {
        String search = name.toLowerCase().trim();
        return items.stream()
                    .filter(i -> i.getName().toLowerCase().contains(search))
                    .findFirst()
                    .orElse(null);
    }

    // -------------------------------------------------------------------------
    // Enemy management
    // -------------------------------------------------------------------------

    public void addEnemy(Enemy e)    { enemies.add(e); }
    public void removeEnemy(Enemy e) { enemies.remove(e); }
    public List<Enemy> getEnemies()  { return Collections.unmodifiableList(enemies); }

    public Enemy findEnemy(String name) {
        String search = name.toLowerCase().trim();
        return enemies.stream()
                      .filter(e -> e.getName().toLowerCase().contains(search))
                      .findFirst()
                      .orElse(null);
    }

    // -------------------------------------------------------------------------
    // NPC management
    // -------------------------------------------------------------------------

    public void addNPC(NPC npc)    { npcs.add(npc); }
    public void removeNPC(NPC npc) { npcs.remove(npc); }
    public List<NPC> getNpcs()     { return Collections.unmodifiableList(npcs); }

    public NPC findNPC(String name) {
        String search = name.toLowerCase().trim();
        return npcs.stream()
                   .filter(n -> n.getName().toLowerCase().contains(search))
                   .findFirst()
                   .orElse(null);
    }

    /**
     * Finds the first NPC in this location that is an instance of the given type.
     * Used by Game to locate a Merchant without knowing its name.
     */
    @SuppressWarnings("unchecked")
    public <T extends NPC> T findNPCOfType(Class<T> type) {
        return (T) npcs.stream()
                       .filter(type::isInstance)
                       .findFirst()
                       .orElse(null);
    }

    // -------------------------------------------------------------------------
    // Puzzle management
    // -------------------------------------------------------------------------

    public void setPuzzle(Puzzle p)  { this.puzzle = p; }
    public boolean hasPuzzle()       { return puzzle != null; }
    public Puzzle getPuzzle()        { return puzzle; }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    /**
     * Returns a fully-formatted description of this location, including exits,
     * enemies, NPCs, items on the ground, and a puzzle hint if one is present.
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(name).append(" ===\n");
        sb.append(description);

        // Exits
        sb.append("\n\nExits: ");
        List<String> exitNames = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (exits.containsKey(dir)) {
                String label = dir.name().toLowerCase();
                if (lockedExits.contains(dir)) label += " [locked]";
                exitNames.add(label);
            }
        }
        sb.append(exitNames.isEmpty() ? "none" : String.join(", ", exitNames));

        // Enemies
        if (!enemies.isEmpty()) {
            sb.append("\n\nEnemies:");
            for (Enemy e : enemies) {
                sb.append("\n  - ").append(e.getName())
                  .append("  (HP: ").append(e.getHealth()).append(")");
            }
        }

        // NPCs
        if (!npcs.isEmpty()) {
            sb.append("\n\nPeople here:");
            for (NPC n : npcs) {
                sb.append("\n  - ").append(n.getName());
            }
        }

        // Ground items
        if (!items.isEmpty()) {
            sb.append("\n\nItems on the ground:");
            for (Item i : items) {
                sb.append("\n  - ").append(i.getName());
            }
        }

        // Puzzle hint
        if (puzzle != null && !puzzle.isSolved()) {
            sb.append("\n\n[There is a puzzle here — type 'solve' to attempt it]");
        }

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public LocationId getId()          { return id; }
    public String     getName()        { return name; }
    public String     getDescription() { return description; }
    public int  getLevelRequirement()          { return levelRequirement; }
    public void setLevelRequirement(int level) { this.levelRequirement = level; }
}
