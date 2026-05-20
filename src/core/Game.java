package core;

// NOTE: This file references classes from all other packages.
// The project will compile once every batch has been added.
// See README.md for full compilation instructions.

import characters.Enemy;
import characters.Merchant;
import characters.NPC;
import characters.Player;
import combat.CombatSystem;
import events.EventManager;
import events.GameObserver;
import events.PlayerObserver;
import events.QuestObserver;
import events.GameEvent;
import events.GameEventType;
import items.Item;
import quests.QuestManager;
import util.Command;
import util.CommandParser;
import util.InputHandler;
import world.Direction;
import world.Location;
import world.World;

/**
 * Game — central game orchestrator.
 *
 * Owns the main game loop and delegates to specialised subsystems:
 *
 *   World         — location graph, movement, world initialisation
 *   CombatSystem  — turn-based combat (uses Strategy pattern internally)
 *   QuestManager  — quest tracking (registered as Observer)
 *   EventManager  — event bus (Observer pattern subject)
 *   InputHandler  — raw console input with error handling
 *   CommandParser — converts raw text into typed Command objects
 *
 * Game itself is responsible only for the loop and command dispatch.
 * Business logic lives in the dedicated subsystems, keeping this class
 * readable and each subsystem independently testable.
 */
public class Game {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private Player        player;
    private World         world;
    private QuestManager  questManager;
    private EventManager  eventManager;
    private CombatSystem  combatSystem;
    private InputHandler  inputHandler;
    private CommandParser commandParser;
    private boolean       running;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Wires together all subsystems.
     *
     * Dependencies are satisfied here via constructor injection (composition),
     * so no subsystem needs to know about any other at construction time.
     */
    public Game() {
        this.eventManager  = new EventManager();
        this.questManager  = new QuestManager(eventManager);
        this.combatSystem  = new CombatSystem(eventManager);
        this.inputHandler  = new InputHandler();
        this.commandParser = new CommandParser();
        this.running       = false;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /** Called by GameManager.newGame() to begin the game session. */
    public void start() {
        displayWelcome();

        String name   = inputHandler.promptName();
        this.player   = new Player(name, 100, 10, 5, 0);

        // Register observers — must happen after player exists
        eventManager.subscribe(questManager);
        eventManager.subscribe(new QuestObserver());
        eventManager.subscribe(new PlayerObserver(player));

        this.world = new World(player, eventManager);
        initializeQuests();

        System.out.println("\nWelcome, " + name + ". Your adventure begins in the Village.");
        System.out.println("Type 'help' for a list of commands.\n");
        displayLocation();

        running = true;
        gameLoop();
    }

    // -------------------------------------------------------------------------
    // Main loop
    // -------------------------------------------------------------------------

    private void gameLoop() {
        while (running) {
            String input = inputHandler.readInput();
            if (input == null) {
                continue;
            }

            Command command = commandParser.parse(input);
            processCommand(command);

            if (running && player.getHealth() <= 0) {
                displayDefeat();
                running = false;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Command dispatch
    // -------------------------------------------------------------------------

    private void processCommand(Command command) {
        switch (command.getType()) {
            case GO:         handleMove(command);    break;
            case LOOK:       displayLocation();      break;
            case LOOK_AT:    handleLookAt(command);  break;
            case TAKE:       handleTake(command);    break;
            case DROP:       handleDrop(command);    break;
            case INVENTORY:  handleInventory();      break;
            case USE:        handleUse(command);     break;
            case EQUIP:      handleEquip(command);   break;
            case TALK:       handleTalk(command);    break;
            case ATTACK:     handleAttack(command);  break;
            case SOLVE:      handleSolve();          break;
            case BUY:        handleBuy(command);     break;
            case SELL:       handleSell(command);    break;
            case STATS:      handleStats();          break;
            case QUESTS:     handleQuests();         break;
            case HELP:       handleHelp();           break;
            case QUIT:       handleQuit();           break;
            default:
                System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
    }

    // -------------------------------------------------------------------------
    // Command handlers
    // -------------------------------------------------------------------------

    private void handleMove(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Go where? (north, south, east, west)");
            return;
        }
        Direction dir = Direction.fromString(command.getArgString());
        if (dir == null) {
            System.out.println("That's not a valid direction. Try: north, south, east, west.");
            return;
        }
        boolean moved = world.movePlayer(dir);
        if (moved) {
            displayLocation();
        }
        // world.movePlayer prints the reason if movement is blocked
    }

    private void handleLookAt(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Look at what?");
            return;
        }
        String target  = command.getArgString();
        Location current = world.getCurrentLocation();

        // Search ground items first
        Item item = current.findItem(target);
        if (item != null) { System.out.println(item.getDescription()); return; }

        // Then inventory
        item = player.getInventory().findItem(target);
        if (item != null) { System.out.println(item.getDescription()); return; }

        // Then enemies
        Enemy enemy = current.findEnemy(target);
        if (enemy != null) { System.out.println(enemy.getDescription()); return; }

        // Then NPCs
        NPC npc = current.findNPC(target);
        if (npc != null) { System.out.println(npc.getDescription()); return; }

        System.out.println("You don't see a '" + target + "' here.");
    }

    private void handleTake(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Take what?");
            return;
        }
        Location current = world.getCurrentLocation();
        Item item = current.findItem(command.getArgString());
        if (item == null) {
            System.out.println("There's no '" + command.getArgString() + "' here.");
            return;
        }
        player.getInventory().addItem(item);
        current.removeItem(item);
        System.out.println("You picked up: " + item.getName() + ".");
    }

    private void handleDrop(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Drop what?");
            return;
        }
        Item item = player.getInventory().findItem(command.getArgString());
        if (item == null) {
            System.out.println("You don't have a '" + command.getArgString() + "'.");
            return;
        }
        player.getInventory().removeItem(item);
        world.getCurrentLocation().addItem(item);
        System.out.println("You dropped: " + item.getName() + ".");
    }

    private void handleInventory() {
        if (player.getInventory().isEmpty()) {
            System.out.println("Your inventory is empty.");
            return;
        }
        System.out.println("\n--- Inventory ---");
        player.getInventory().listItems();
        System.out.println("Gold: " + player.getGold());
        System.out.println("-----------------\n");
    }

    private void handleUse(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Use what?");
            return;
        }
        Item item = player.getInventory().findItem(command.getArgString());
        if (item == null) {
            System.out.println("You don't have a '" + command.getArgString() + "'.");
            return;
        }
        item.use(player);
    }

    private void handleEquip(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Equip what?");
            return;
        }
        Item item = player.getInventory().findItem(command.getArgString());
        if (item == null) {
            System.out.println("You don't have a '" + command.getArgString() + "'.");
            return;
        }
        player.equip(item);
    }

    private void handleTalk(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Talk to whom?");
            return;
        }
        NPC npc = world.getCurrentLocation().findNPC(command.getArgString());
        if (npc == null) {
            System.out.println("There's no one called '" + command.getArgString() + "' here.");
            return;
        }
        String dialogue = npc.talk(player, questManager);
        System.out.println("\n" + npc.getName() + ": \"" + dialogue + "\"\n");

        // OBSERVER PATTERN: lets QuestManager detect "Elder's Plea" quest trigger
        eventManager.notify(new GameEvent(
                GameEventType.TALKED_TO_NPC, npc.getName().toLowerCase()));
    }

    private void handleAttack(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Attack what?");
            return;
        }
        Enemy enemy = world.getCurrentLocation().findEnemy(command.getArgString());
        if (enemy == null) {
            System.out.println("There's no '" + command.getArgString() + "' here to attack.");
            return;
        }

        boolean playerSurvived = combatSystem.executeCombat(player, enemy, inputHandler);
        if (!playerSurvived) {
            displayDefeat();
            running = false;
            return;
        }

        if (enemy.getHealth() <= 0) {
            world.getCurrentLocation().removeEnemy(enemy);
            System.out.println("You defeated " + enemy.getName() + "!");

            if (enemy.getGoldDrop() > 0) {
                player.addGold(enemy.getGoldDrop());
                System.out.println("You found " + enemy.getGoldDrop() + " gold.");
            }
            for (Item loot : enemy.getLootItems()) {
                world.getCurrentLocation().addItem(loot);
                System.out.println(enemy.getName() + " dropped: " + loot.getName() + ".");
            }

            if (enemy.isBoss()) {
                displayVictory();
                running = false;
            }
        }
    }

    private void handleSolve() {
        Location current = world.getCurrentLocation();
        if (!current.hasPuzzle()) {
            System.out.println("There's no puzzle to solve here.");
            return;
        }
        if (current.getPuzzle().isSolved()) {
            System.out.println("You've already solved the puzzle here.");
            return;
        }
        current.getPuzzle().attempt(player, inputHandler, current);

        // OBSERVER PATTERN: notify after attempt so QuestManager can track
        // the "Dungeon Delver" side quest (two puzzles solved)
        if (current.getPuzzle().isSolved()) {
            eventManager.notify(new GameEvent(
                    GameEventType.PUZZLE_SOLVED, current.getPuzzle().getName()));
        }
    }

    private void handleBuy(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Buy what?");
            return;
        }
        NPC npc = world.getCurrentLocation().findNPCOfType(Merchant.class);
        if (npc == null) {
            System.out.println("There's no merchant here.");
            return;
        }
        ((Merchant) npc).buyItem(command.getArgString(), player);
    }

    private void handleSell(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Sell what?");
            return;
        }
        NPC npc = world.getCurrentLocation().findNPCOfType(Merchant.class);
        if (npc == null) {
            System.out.println("There's no merchant here.");
            return;
        }
        Item item = player.getInventory().findItem(command.getArgString());
        if (item == null) {
            System.out.println("You don't have a '" + command.getArgString() + "'.");
            return;
        }
        ((Merchant) npc).sellItem(item, player);
    }

    private void handleStats() {
        System.out.println(player.getStatsDisplay());
    }

    private void handleQuests() {
        System.out.println(questManager.getQuestLog());
    }

    private void handleHelp() {
        System.out.println("\n--- Commands -----------------------------------------------");
        System.out.println("  go <direction>       Move: north, south, east, west");
        System.out.println("  look                 Describe current location");
        System.out.println("  look at <target>     Examine an item, NPC, or enemy");
        System.out.println("  take <item>          Pick up an item");
        System.out.println("  drop <item>          Drop an item");
        System.out.println("  inventory / inv      View your inventory");
        System.out.println("  use <item>           Use an item (e.g. health potion)");
        System.out.println("  equip <item>         Equip a weapon or armour");
        System.out.println("  talk <npc>           Talk to an NPC");
        System.out.println("  attack <enemy>       Attack an enemy");
        System.out.println("  solve                Attempt to solve a puzzle");
        System.out.println("  buy <item>           Buy from a merchant");
        System.out.println("  sell <item>          Sell to a merchant");
        System.out.println("  stats                View your character statistics");
        System.out.println("  quests               View your quest log");
        System.out.println("  help                 Show this list");
        System.out.println("  quit                 Exit the game");
        System.out.println("------------------------------------------------------------\n");
    }

    private void handleQuit() {
        System.out.println("Thank you for playing The Fallen Kingdom. Farewell!");
        running = false;
    }

    // -------------------------------------------------------------------------
    // Display helpers
    // -------------------------------------------------------------------------

    private void displayWelcome() {
        System.out.println("========================================================");
        System.out.println("         T H E   F A L L E N   K I N G D O M          ");
        System.out.println("========================================================");
        System.out.println("  A darkness has consumed the kingdom. The Shadow Lord  ");
        System.out.println("  has shattered the Ancient Relic and cursed the land.  ");
        System.out.println("  You are the only adventurer brave enough to stop him. ");
        System.out.println("========================================================\n");
    }

    private void displayLocation() {
        System.out.println("\n" + world.getCurrentLocation().getFullDescription());
    }

    private void displayVictory() {
        System.out.println("\n========================================================");
        System.out.println("                  V I C T O R Y !                      ");
        System.out.println("========================================================");
        System.out.println("  The Shadow Lord has fallen! The Ancient Relic is      ");
        System.out.println("  restored and the kingdom is saved. You are a hero.    ");
        System.out.println("========================================================\n");
    }

    private void displayDefeat() {
        System.out.println("\n========================================================");
        System.out.println("               G A M E   O V E R                       ");
        System.out.println("========================================================");
        System.out.println("  You have fallen in battle. The Shadow Lord's darkness ");
        System.out.println("  consumes the kingdom. Perhaps another hero will rise. ");
        System.out.println("========================================================\n");
    }

    // -------------------------------------------------------------------------
    // Quest initialisation
    // -------------------------------------------------------------------------

    private void initializeQuests() {
        questManager.initializeQuests(player);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** Returns the active player, or {@code null} before {@link #start()} creates one. */
    public Player       getPlayer()       { return player; }
    public QuestManager getQuestManager() { return questManager; }
    public InputHandler getInputHandler() { return inputHandler; }

    /** Subscribes an additional observer — used by GameWindow to attach GuiObserver. */
    public void registerObserver(GameObserver observer) {
        eventManager.subscribe(observer);
    }

    // Package-private — used by unit tests
    World   getWorld()    { return world; }
    boolean isRunning()   { return running; }

    /** Allows the combat system (or tests) to stop the main loop externally. */
    public void setRunning(boolean running) {
        this.running = running;
    }
}
