package world;

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
 *   - Constructing all locations and wiring their exits
 *   - Populating locations with enemies (via EnemyFactory), NPCs, items, and puzzles
 *   - Moving the player between locations, enforcing locked-door logic
 *
 * World is the single source of truth for where everything is in the game.
 * Game.java delegates all movement and location queries here.
 *
 * Map layout (10 locations):
 *
 *                    [Forgotten Battlefield]
 *                           |  E
 *   [Mystic Glade] --S-- [Dark Forest] --N-- [Ancient Ruins]
 *                           |  E
 *                   [Underground Dungeon]
 *                           |  N (locked: Ancient Key)
 *   [Cursed Archives] --W-- [Corrupted Castle] --E-- [Shadow Barracks]
 *                           |  N (locked: puzzle)
 *                    [Shadow Throne Room]
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

    private void initializeWorld() {

        // ================================================================
        // CREATE ALL LOCATIONS
        // ================================================================

        Location village = new Location(LocationId.VILLAGE, "Village",
            "A small village at the edge of the corrupted kingdom. Stone cottages\n" +
            "line a dusty road. Most inhabitants have fled, but a few brave souls remain.\n" +
            "The air carries the distant smell of shadow magic.");

        Location darkForest = new Location(LocationId.DARK_FOREST, "Dark Forest",
            "Twisted trees block out the sky, their branches clawing at the gloom.\n" +
            "Strange, guttural sounds echo from the undergrowth. A narrow trail\n" +
            "branches in every direction — something ancient stirs in the south.");

        Location ancientRuins = new Location(LocationId.ANCIENT_RUINS, "Ancient Ruins",
            "Crumbling stone columns rise from the earth, engraved with faded runes.\n" +
            "At the centre stands a weathered stone tablet, its inscription still\n" +
            "glowing faintly with ancient power. A ruined archway opens to the east.");

        Location dungeon = new Location(LocationId.UNDERGROUND_DUNGEON, "Underground Dungeon",
            "A damp underground chamber lit by sputtering torches. Three heavy iron\n" +
            "levers protrude from the far wall, and a locked iron door stands to the north.\n" +
            "Water drips steadily from the ceiling.");

        Location castle = new Location(LocationId.CORRUPTED_CASTLE, "Corrupted Castle",
            "The once-grand hall is warped by shadow energy. Dark crystals line the\n" +
            "cracked walls and a sickly purple light pulses from the ceiling.\n" +
            "Passages branch east and west into darker reaches of the castle.");

        Location shadowThrone = new Location(LocationId.SHADOW_THRONE, "Shadow Throne Room",
            "Absolute darkness presses in from every side. A throne of bone and obsidian\n" +
            "looms at the far end of the chamber. Upon it sits the Shadow Lord — a\n" +
            "towering figure of darkness whose eyes burn like dying stars.");

        // ── New locations ────────────────────────────────────────────────────

        Location mysticGlade = new Location(LocationId.MYSTIC_GLADE, "Mystic Glade",
            "A serene clearing bathed in soft emerald light. Ancient oaks form a\n" +
            "natural cathedral around a carpet of luminous moss. The air here feels\n" +
            "different — alive and watchful. At the centre stands a colossal oak,\n" +
            "its bark carved with symbols older than the kingdom itself. A great\n" +
            "guardian stands vigil beneath its boughs.");

        Location forgottenBattlefield = new Location(LocationId.FORGOTTEN_BATTLEFIELD, "Forgotten Battlefield",
            "A vast ruined field strewn with the remnants of a war long forgotten.\n" +
            "Rusted weapons half-buried in scorched earth stretch to the horizon.\n" +
            "Stone formations etched with glowing runes jut from the ground —\n" +
            "this place still holds ancient power. The wind carries the echo of\n" +
            "distant battle cries, and something crawls between the ruins.");

        Location shadowBarracks = new Location(LocationId.SHADOW_BARRACKS, "Shadow Barracks",
            "A grim stone barracks where the Shadow Lord's forces are quartered.\n" +
            "Iron cages hang from the ceiling, most empty. Shadow energy seeps from\n" +
            "the walls, and formless shapes drift between crumbling bunks — remnants\n" +
            "of soldiers long since consumed by darkness. A tattered shadow robe\n" +
            "hangs on the far wall.");

        Location cursedArchives = new Location(LocationId.CURSED_ARCHIVES, "Cursed Archives",
            "Row upon row of ancient bookshelves stretch into the darkness, their\n" +
            "tomes crackling with corrupted shadow energy. At the room's heart\n" +
            "stands a stone altar bearing four symbol-etched levers. Carved above\n" +
            "them in shadow-script: 'Knowledge yields to those who seek in order.'");

        // ================================================================
        // CONNECT LOCATIONS
        // ================================================================

        // Village ↔ Dark Forest
        village.setExit(Direction.EAST,  darkForest);
        darkForest.setExit(Direction.WEST,  village);

        // Dark Forest ↔ Ancient Ruins
        darkForest.setExit(Direction.NORTH, ancientRuins);
        ancientRuins.setExit(Direction.SOUTH, darkForest);

        // Dark Forest ↔ Underground Dungeon
        darkForest.setExit(Direction.EAST,  dungeon);
        dungeon.setExit(Direction.WEST,  darkForest);

        // Dark Forest ↔ Mystic Glade (new)
        darkForest.setExit(Direction.SOUTH, mysticGlade);
        mysticGlade.setExit(Direction.NORTH, darkForest);

        // Ancient Ruins ↔ Forgotten Battlefield (new)
        ancientRuins.setExit(Direction.EAST, forgottenBattlefield);
        forgottenBattlefield.setExit(Direction.WEST, ancientRuins);

        // Underground Dungeon ↔ Corrupted Castle (locked)
        dungeon.setExit(Direction.NORTH, castle);
        dungeon.lockExit(Direction.NORTH,
            "The iron door is locked solid. You need the Ancient Key to open it.");
        castle.setExit(Direction.SOUTH, dungeon);

        // Corrupted Castle ↔ Shadow Throne (locked by puzzle)
        castle.setExit(Direction.NORTH, shadowThrone);
        castle.lockExit(Direction.NORTH,
            "The sealed gate won't budge. A puzzle mechanism on the wall might hold the answer.");
        shadowThrone.setExit(Direction.SOUTH, castle);

        // Corrupted Castle ↔ Shadow Barracks (new)
        castle.setExit(Direction.EAST, shadowBarracks);
        shadowBarracks.setExit(Direction.WEST, castle);

        // Corrupted Castle ↔ Cursed Archives (new)
        castle.setExit(Direction.WEST, cursedArchives);
        cursedArchives.setExit(Direction.EAST, castle);

        // ================================================================
        // POPULATE — NPCs
        // ================================================================

        village.addNPC(new NPC(
            "Village Elder",
            "A weathered old man whose eyes carry the weight of the kingdom's grief.",
            new String[]{
                "Adventurer! You must help us. The Shadow Lord shattered the Ancient Relic " +
                "and now darkness spreads. Recover it from his throne room — it is our " +
                "only hope. Speak to Griswold the Merchant before you leave.",
                "Head east into the Dark Forest. In the Ancient Ruins to the north you'll " +
                "find an inscription — solve its riddle to claim the Ancient Key.",
                "The Ancient Key unlocks the dungeon passage to the Corrupted Castle. " +
                "Be brave, adventurer. The kingdom depends on you."
            }
        ));

        Merchant merchant = new Merchant();
        merchant.addShopItem(ItemFactory.create(ItemType.IRON_SWORD));
        merchant.addShopItem(ItemFactory.create(ItemType.LEATHER_ARMOUR));
        merchant.addShopItem(ItemFactory.create(ItemType.HEALTH_POTION));
        merchant.addShopItem(ItemFactory.create(ItemType.ELIXIR));
        village.addNPC(merchant);

        castle.addNPC(new NPC(
            "Imprisoned Knight",
            "A knight in tattered armour, chained to the wall. He raises his head with effort.",
            new String[]{
                "You came... thank the heavens. The Shadow Lord's power is tied to the dark " +
                "crystals on his throne. Weaken him by striking them first.",
                "The puzzle on the wall controls the gate north. Solve it and the way to " +
                "the throne room will open. Check the east and west wings too — they hold " +
                "powerful relics that once belonged to the castle's defenders.",
                "Go. End this nightmare. I will hold on a little longer."
            }
        ));

        // Tree Protector — in both NPC and enemy lists so the player can talk OR fight
        mysticGlade.addNPC(new NPC(
            "Tree Protector",
            "A towering figure woven from living bark and ancient roots. Two amber eyes\n" +
            "  glow warmly in a face shaped from gnarled wood. He radiates old magic.",
            new String[]{
                "I am the guardian of this grove — as old as the first oak. I sense the " +
                "shadow's corruption on your path. Will you prove yourself worthy of the " +
                "forest's blessing? Type 'befriend tree protector' to accept my friendship, " +
                "or 'attack tree protector' to take what you want by force.",
                "Choose peace and I will grant you the Evasive Boots — woven from root-fibre " +
                "and living wind, they will help you evade the darkness ahead.",
                "The ancient oak behind me has stood for a thousand years. Whatever happens " +
                "here today, its memory will endure."
            }
        ));
        mysticGlade.addEnemy(EnemyFactory.create(EnemyType.TREE_PROTECTOR_ENEMY));

        // ================================================================
        // POPULATE — Enemies (Factory pattern)
        // ================================================================

        darkForest.addEnemy(EnemyFactory.create(EnemyType.SHADOW_GOBLIN));
        darkForest.addEnemy(EnemyFactory.create(EnemyType.SHADOW_GOBLIN));
        darkForest.addEnemy(EnemyFactory.create(EnemyType.PLAGUE_RAT));

        forgottenBattlefield.addEnemy(EnemyFactory.create(EnemyType.PLAGUE_RAT));
        forgottenBattlefield.addEnemy(EnemyFactory.create(EnemyType.PLAGUE_RAT));
        forgottenBattlefield.addEnemy(EnemyFactory.create(EnemyType.STONE_SENTINEL));

        castle.addEnemy(EnemyFactory.create(EnemyType.DARK_KNIGHT));

        shadowBarracks.addEnemy(EnemyFactory.create(EnemyType.VOID_WRAITH));
        shadowBarracks.addEnemy(EnemyFactory.create(EnemyType.VOID_WRAITH));

        cursedArchives.addEnemy(EnemyFactory.create(EnemyType.VOID_WRAITH));

        shadowThrone.addEnemy(EnemyFactory.create(EnemyType.SHADOW_LORD));

        // ================================================================
        // POPULATE — Ground items
        // ================================================================

        forgottenBattlefield.addItem(ItemFactory.create(ItemType.HEALTH_POTION));
        shadowBarracks.addItem(ItemFactory.create(ItemType.SHADOW_ROBE));

        // ================================================================
        // POPULATE — Puzzles
        // ================================================================

        // Ancient Ruins — riddle rewards the Ancient Key
        ancientRuins.setPuzzle(new RiddlePuzzle(
            "I have cities but no houses, mountains but no trees, and water but no fish. " +
            "What am I?",
            "map",
            ItemFactory.create(ItemType.ANCIENT_KEY),
            null
        ));

        // Underground Dungeon — lever sequence rewards the Shadow Blade
        dungeon.setPuzzle(new LeverPuzzle(
            new int[]{1, 3, 2},
            ItemFactory.create(ItemType.SHADOW_BLADE),
            null
        ));

        // Corrupted Castle — riddle unlocks the gate to the Shadow Throne
        castle.setPuzzle(new RiddlePuzzle(
            "I speak without a mouth, am heard without ears, and have no body yet " +
            "come alive with wind. What am I?",
            "echo",
            null,
            Direction.NORTH
        ));

        // Forgotten Battlefield — ancient runic tablet, rewards Battle Axe
        forgottenBattlefield.setPuzzle(new RiddlePuzzle(
            "The more you take, the more you leave behind. What am I?",
            "footsteps",
            ItemFactory.create(ItemType.BATTLE_AXE),
            null
        ));

        // Cursed Archives — 4-lever shadow sequence, rewards Elixir
        cursedArchives.setPuzzle(new LeverPuzzle(
            "Shadow Archive Seals",
            "Four shadow runes are carved above the levers, each pulsing darkly.",
            "The runes decree: third eldest, then youngest, then eldest, then second eldest.",
            new int[]{3, 1, 4, 2},
            ItemFactory.create(ItemType.ELIXIR),
            null
        ));

        // ================================================================
        // REGISTER LOCATIONS AND SET START
        // ================================================================

        locations.put(LocationId.VILLAGE,              village);
        locations.put(LocationId.DARK_FOREST,          darkForest);
        locations.put(LocationId.ANCIENT_RUINS,        ancientRuins);
        locations.put(LocationId.UNDERGROUND_DUNGEON,  dungeon);
        locations.put(LocationId.CORRUPTED_CASTLE,     castle);
        locations.put(LocationId.SHADOW_THRONE,        shadowThrone);
        locations.put(LocationId.MYSTIC_GLADE,         mysticGlade);
        locations.put(LocationId.FORGOTTEN_BATTLEFIELD,forgottenBattlefield);
        locations.put(LocationId.SHADOW_BARRACKS,      shadowBarracks);
        locations.put(LocationId.CURSED_ARCHIVES,      cursedArchives);

        currentLocation = village;
    }

    // -------------------------------------------------------------------------
    // Movement
    // -------------------------------------------------------------------------

    /**
     * Attempts to move the player one step in the given direction.
     *
     * Locked exits:
     *   - Dungeon → Castle: requires the Ancient Key (consumed on use).
     *   - Castle  → Throne: locked by the castle puzzle.
     *
     * @return true if movement succeeded, false if blocked
     */
    public boolean movePlayer(Direction dir) {
        if (!currentLocation.hasExit(dir)) {
            System.out.println("You can't go " + dir.name().toLowerCase() + " from here.");
            return false;
        }

        if (currentLocation.isExitLocked(dir)) {
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

    public Location getCurrentLocation()       { return currentLocation; }
    public Location getLocation(LocationId id) { return locations.get(id); }
}
