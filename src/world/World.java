package world;

// NOTE: initializeWorld() references classes from later batches (EnemyFactory,
// ItemFactory, NPC, Merchant, RiddlePuzzle, LeverPuzzle).
// This file compiles fully once all batches have been added.

import characters.EnemyFactory;
import characters.EnemyType;
import characters.Merchant;
import characters.NPC;
import characters.Player;
import events.EventManager;
import items.Item;
import items.ItemFactory;
import items.ItemType;
import puzzles.LeverPuzzle;
import puzzles.RiddlePuzzle;

import java.util.HashMap;
import java.util.Map;

/**
 * World — builds and manages the entire location graph.
 *
 * Responsibilities:
 *   - Constructing all six locations and wiring their exits
 *   - Populating locations with enemies (via EnemyFactory), NPCs, and puzzles
 *   - Moving the player between locations, enforcing locked-door logic
 *
 * World is the single source of truth for where everything is in the game.
 * Game.java delegates all movement and location queries here.
 */
public class World {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final Map<LocationId, Location> locations;
    private       Location                  currentLocation;
    private final Player                    player;
    private final EventManager              eventManager;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public World(Player player, EventManager eventManager) {
        this.player       = player;
        this.eventManager = eventManager;
        this.locations    = new HashMap<>();
        initializeWorld();
    }

    // -------------------------------------------------------------------------
    // World initialisation
    // -------------------------------------------------------------------------

    /**
     * Creates every location, connects them, and populates them with enemies,
     * NPCs, items, and puzzles.
     *
     * This is called once at game start.  Separating world-building from the
     * Game class keeps Game responsible only for the game loop.
     */
    private void initializeWorld() {

        // ----- Create locations -----

        Location village = new Location(LocationId.VILLAGE, "Village",
            "A small village at the edge of the corrupted kingdom. Stone cottages\n" +
            "line a dusty road. Most inhabitants have fled, but a few brave souls remain.\n" +
            "The air carries the distant smell of shadow magic.");

        Location darkForest = new Location(LocationId.DARK_FOREST, "Dark Forest",
            "Twisted trees block out the sky, their branches clawing at the gloom.\n" +
            "Strange, guttural sounds echo from the undergrowth.\n" +
            "The path forks north toward crumbling ruins and east toward an old dungeon.");

        Location ancientRuins = new Location(LocationId.ANCIENT_RUINS, "Ancient Ruins",
            "Crumbling stone columns rise from the earth, engraved with faded runes.\n" +
            "At the centre stands a weathered stone tablet, its inscription still\n" +
            "glowing faintly with ancient power.");

        Location dungeon = new Location(LocationId.UNDERGROUND_DUNGEON, "Underground Dungeon",
            "A damp underground chamber lit by sputtering torches. Three heavy iron\n" +
            "levers protrude from the far wall, and a locked iron door stands to the north.\n" +
            "Water drips steadily from the ceiling.");

        Location castle = new Location(LocationId.CORRUPTED_CASTLE, "Corrupted Castle",
            "The once-grand hall is warped by shadow energy. Dark crystals line the\n" +
            "cracked walls and a sickly purple light pulses from the ceiling.\n" +
            "A massive sealed gate blocks the passage north.");

        Location shadowThrone = new Location(LocationId.SHADOW_THRONE, "Shadow Throne Room",
            "Absolute darkness presses in from every side. A throne of bone and obsidian\n" +
            "looms at the far end of the chamber. Upon it sits the Shadow Lord — a\n" +
            "towering figure of darkness whose eyes burn like dying stars.");

        // ----- Connect locations -----

        village.setExit(Direction.EAST,  darkForest);

        darkForest.setExit(Direction.WEST,  village);
        darkForest.setExit(Direction.NORTH, ancientRuins);
        darkForest.setExit(Direction.EAST,  dungeon);

        ancientRuins.setExit(Direction.SOUTH, darkForest);

        dungeon.setExit(Direction.WEST,  darkForest);
        dungeon.setExit(Direction.NORTH, castle);
        dungeon.lockExit(Direction.NORTH,
            "The iron door is locked solid. You need the Ancient Key to open it.");

        castle.setExit(Direction.SOUTH, dungeon);
        castle.setExit(Direction.NORTH, shadowThrone);
        castle.lockExit(Direction.NORTH,
            "The sealed gate won't budge. A puzzle mechanism on the wall might hold the answer.");

        shadowThrone.setExit(Direction.SOUTH, castle);

        // ----- Populate with NPCs -----

        village.addNPC(new NPC(
            "Village Elder",
            "A weathered old man whose eyes carry the weight of the kingdom's grief.",
            new String[]{
                "Adventurer! You must help us. The Shadow Lord shattered the Ancient Relic" +
                " and now darkness spreads. Recover it from his throne room — it is our" +
                " only hope. Speak to Griswold the Merchant before you leave.",
                "Head east into the Dark Forest. In the Ancient Ruins to the north you'll" +
                " find an inscription — solve its riddle to claim the Ancient Key.",
                "The Ancient Key unlocks the dungeon passage to the Corrupted Castle." +
                " Be brave, adventurer. The kingdom depends on you."
            }
        ));

        Merchant merchant = new Merchant();
        merchant.addShopItem(ItemFactory.create(ItemType.IRON_SWORD));
        merchant.addShopItem(ItemFactory.create(ItemType.LEATHER_ARMOUR));
        merchant.addShopItem(ItemFactory.create(ItemType.HEALTH_POTION));
        village.addNPC(merchant);

        castle.addNPC(new NPC(
            "Imprisoned Knight",
            "A knight in tattered armour, chained to the wall. He raises his head with effort.",
            new String[]{
                "You came... thank the heavens. The Shadow Lord's power is tied to the dark" +
                " crystals on his throne. Weaken him by striking them first.",
                "The puzzle on the wall controls the gate north. Solve it and the way to" +
                " the throne room will open.",
                "Go. End this nightmare. I will hold on a little longer."
            }
        ));

        // ----- Populate with enemies (Factory pattern) -----

        darkForest.addEnemy(EnemyFactory.create(EnemyType.SHADOW_GOBLIN));
        darkForest.addEnemy(EnemyFactory.create(EnemyType.SHADOW_GOBLIN));
        castle.addEnemy(EnemyFactory.create(EnemyType.DARK_KNIGHT));
        shadowThrone.addEnemy(EnemyFactory.create(EnemyType.SHADOW_LORD));

        // ----- Add puzzles -----
        // RiddlePuzzle(riddle, answer, rewardItem, directionToUnlock)
        // LeverPuzzle(correctSequence, rewardItem, directionToUnlock)

        ancientRuins.setPuzzle(new RiddlePuzzle(
            "I have cities but no houses, mountains but no trees, and water but no fish." +
            " What am I?",
            "map",
            ItemFactory.create(ItemType.ANCIENT_KEY),
            null   // no exit to unlock here — the key itself unlocks the dungeon door
        ));

        dungeon.setPuzzle(new LeverPuzzle(
            new int[]{1, 3, 2},                         // correct pull sequence
            ItemFactory.create(ItemType.SHADOW_BLADE),  // reward
            null                                        // no exit to unlock here
        ));

        castle.setPuzzle(new RiddlePuzzle(
            "I speak without a mouth, am heard without ears, and have no body yet" +
            " come alive with wind. What am I?",
            "echo",
            null,             // no item reward
            Direction.NORTH   // solving unlocks the gate to the Shadow Throne
        ));

        // ----- Register and set start location -----

        locations.put(LocationId.VILLAGE,              village);
        locations.put(LocationId.DARK_FOREST,          darkForest);
        locations.put(LocationId.ANCIENT_RUINS,        ancientRuins);
        locations.put(LocationId.UNDERGROUND_DUNGEON,  dungeon);
        locations.put(LocationId.CORRUPTED_CASTLE,     castle);
        locations.put(LocationId.SHADOW_THRONE,        shadowThrone);

        currentLocation = village;
    }

    // -------------------------------------------------------------------------
    // Movement
    // -------------------------------------------------------------------------

    /**
     * Attempts to move the player one step in the given direction.
     *
     * Locked exits are handled here:
     *   - Dungeon → Castle: requires the Ancient Key (consumed on use).
     *   - Castle → Throne:  locked by puzzle; the puzzle itself unlocks it.
     *
     * @return true if movement succeeded, false if blocked
     */
    public boolean movePlayer(Direction dir) {
        if (!currentLocation.hasExit(dir)) {
            System.out.println("You can't go " + dir.name().toLowerCase() + " from here.");
            return false;
        }

        if (currentLocation.isExitLocked(dir)) {
            // Special case: Ancient Key unlocks the dungeon north door
            if (currentLocation.getId() == LocationId.UNDERGROUND_DUNGEON
                    && dir == Direction.NORTH) {
                Item key = player.getInventory().findItem("ancient key");
                if (key != null) {
                    currentLocation.unlockExit(dir);
                    player.getInventory().removeItem(key);
                    System.out.println("You insert the Ancient Key into the iron lock.");
                    System.out.println("The door swings open with a heavy groan.");
                } else {
                    System.out.println(currentLocation.getLockMessage(dir));
                    return false;
                }
            } else {
                System.out.println(currentLocation.getLockMessage(dir));
                return false;
            }
        }

        currentLocation = currentLocation.getExit(dir);
        return true;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Location getCurrentLocation()    { return currentLocation; }
    public Location getLocation(LocationId id) { return locations.get(id); }
}
