package core;

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
import characters.ArenaMaster;
import characters.Enchanter;
import enchantments.WeaponEnchantment;
import items.Item;
import items.ItemFactory;
import items.ItemType;
import quests.QuestManager;
import util.Command;
import util.CommandParser;
import util.InputHandler;
import world.Direction;
import world.Location;
import world.World;

import save.GameSnapshot;
import save.SaveManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private Difficulty    difficulty = Difficulty.MEDIUM;

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

    /**
     * Sets the chosen difficulty before the game thread calls start().
     * Also propagates the enemy damage multiplier to CombatSystem.
     */
    public void setDifficulty(Difficulty d) {
        this.difficulty = d;
        this.combatSystem.setDamageMultiplier(d.enemyDamageMultiplier);
    }

    /** Called on the game thread once a difficulty has been selected. */
    public void start() {
        displayWelcome();

        String name = inputHandler.promptName();
        this.player = new Player(
                name,
                difficulty.startHp,
                difficulty.startAttack,
                difficulty.startDefense,
                difficulty.startGold);

        // Register observers — must happen after player exists
        eventManager.subscribe(questManager);
        eventManager.subscribe(new QuestObserver());
        eventManager.subscribe(new PlayerObserver(player));

        this.world = new World(player, eventManager);
        initializeQuests();

        System.out.println("\nWelcome, " + name + ". Your adventure begins in the Village.");
        System.out.println("Difficulty: " + difficulty.label);
        System.out.println("Type 'help' for a list of commands.\n");
        displayLocation();

        running = true;
        gameLoop();
    }

    /**
     * Starts the game from a previously saved snapshot instead of a new game.
     * Called by GameWindow when the player clicks a Continue slot button.
     */
    public void startFromSnapshot(GameSnapshot snapshot) {
        this.difficulty = snapshot.difficulty;
        this.combatSystem.setDamageMultiplier(difficulty.enemyDamageMultiplier);
        this.player = snapshot.player;

        eventManager.subscribe(questManager);
        eventManager.subscribe(new QuestObserver());
        eventManager.subscribe(new PlayerObserver(player));

        this.world = new World(player, eventManager);
        this.world.applyState(snapshot);
        initializeQuests();

        System.out.println("\n========================================================");
        System.out.println("             G A M E   L O A D E D                    ");
        System.out.println("========================================================");
        System.out.println("  Welcome back, " + player.getName() + ".");
        System.out.println("  Difficulty: " + difficulty.label
                + "  |  Level: " + player.getLevel()
                + "  |  Gold: " + player.getGold() + "g");
        System.out.println("========================================================\n");

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
            case TALK:       handleTalk(command);     break;
            case BEFRIEND:   handleBefriend(command); break;
            case ATTACK:     handleAttack(command);  break;
            case SOLVE:      handleSolve();          break;
            case BUY:        handleBuy(command);     break;
            case SELL:       handleSell(command);    break;
            case ENCHANT:    handleEnchant(command); break;
            case RESPAWN:    handleRespawn();         break;
            case SAVE:       handleSave(command);    break;
            case MAP:        handleMap();            break;
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
            eventManager.notify(new GameEvent(GameEventType.LOCATION_CHANGED, null));
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
                double enchantMult = (player.getEquippedWeapon() instanceof WeaponEnchantment)
                        ? ((WeaponEnchantment) player.getEquippedWeapon()).getGoldMultiplier()
                        : 1.0;
                int gold = (int)(enemy.getGoldDrop() * enchantMult * difficulty.goldDropMultiplier);
                player.addGold(gold);
                if (enchantMult > 1.0)
                    System.out.println("Your Lucky enchantment glints — you found " + gold + " gold!");
                else
                    System.out.println("You found " + gold + " gold.");
            }

            boolean isTreeProtector = enemy.getName().equalsIgnoreCase("Tree Protector");
            for (Item loot : enemy.getLootItems()) {
                world.getCurrentLocation().addItem(loot);
                if (isTreeProtector) {
                    System.out.println("\nAs the Tree Protector crumbles into bark and ash,");
                    System.out.println("the ancient oak behind him shudders and sways.");
                    System.out.println("A faint emerald glow pulses deep within its hollow.");
                    System.out.println("You notice a " + loot.getName() + " nestled in the tree.");
                } else {
                    System.out.println(enemy.getName() + " dropped: " + loot.getName() + ".");
                }
            }

            for (Item chanceDrop : enemy.rollChanceLoot()) {
                world.getCurrentLocation().addItem(chanceDrop);
                System.out.println(enemy.getName() + " also dropped: " + chanceDrop.getName() + ".");
            }

            if (isTreeProtector) {
                // Remove the Tree Protector NPC version as well
                NPC npc = world.getCurrentLocation().findNPC("Tree Protector");
                if (npc != null) world.getCurrentLocation().removeNPC(npc);
            }

            // Wait for the typewriter to finish printing the gold/loot lines,
            // then push a sidebar refresh so the gold counter updates visibly.
            pause(700);
            eventManager.notify(new GameEvent(GameEventType.PLAYER_STATS_CHANGED, null));

            if (enemy.isBoss()) {
                displayVictory();
                running = false;
            }
        }
    }

    private void handleBefriend(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Befriend whom?");
            return;
        }
        NPC npc = world.getCurrentLocation().findNPC(command.getArgString());
        if (npc == null) {
            System.out.println("There's no '" + command.getArgString() + "' to befriend here.");
            return;
        }
        if (!npc.getName().equalsIgnoreCase("Tree Protector")) {
            System.out.println(npc.getName() + " looks at you curiously. That doesn't seem right here.");
            return;
        }
        // The enemy version must still be present — can't befriend after already fighting
        Enemy treeEnemy = world.getCurrentLocation().findEnemy("Tree Protector");
        if (treeEnemy == null) {
            System.out.println("The Tree Protector watches you silently. The choice has already been made.");
            return;
        }
        world.getCurrentLocation().removeEnemy(treeEnemy);
        Item boots = ItemFactory.create(ItemType.EVASIVE_BOOTS);
        player.getInventory().addItem(boots);
        System.out.println("\nThe Tree Protector's amber eyes warm with ancient light.");
        System.out.println("\"Wise choice, adventurer. You carry no shadow-taint in your heart.\"");
        System.out.println("\"Take these boots — crafted from root-weave and living wind.\"");
        System.out.println("\"They shall make you fleet and hard to strike down.\"\n");
        System.out.println("You received: Evasive Boots.");
        System.out.println("  [+1 defense | enemies have +20% miss chance when attacking you]");
        eventManager.notify(new GameEvent(GameEventType.PLAYER_STATS_CHANGED, null));
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

    private void handleEnchant(Command command) {
        if (!command.hasArgs()) {
            System.out.println("Enchant what? (e.g. 'enchant iron sword')");
            return;
        }
        Enchanter enchanter = world.getCurrentLocation().findNPCOfType(Enchanter.class);
        if (enchanter == null) {
            System.out.println("There is no enchanter here.");
            return;
        }
        Item item = player.getInventory().findItem(command.getArgString());
        if (item == null) {
            System.out.println("You don't have a '" + command.getArgString() + "'.");
            return;
        }
        enchanter.enchant(player, item, difficulty);
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

    private void handleRespawn() {
        ArenaMaster arena = world.getCurrentLocation().findNPCOfType(ArenaMaster.class);
        if (arena == null) {
            System.out.println("There is no arena master here. Try the Proving Grounds or the Celestial Barracks.");
            return;
        }
        arena.respawn(player, world.getCurrentLocation(), difficulty);
    }

    private void handleMap() {
        eventManager.notify(new GameEvent(GameEventType.SHOW_MAP, "map",
                world.getCurrentLocation().getId()));
    }

    private void handleSave(Command command) {
        if (!command.hasArgs()) {
            System.out.println("  Which slot? Use 'save 1', 'save 2', or 'save 3'.");
            return;
        }
        int slot;
        try {
            slot = Integer.parseInt(command.getArgString().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Use 'save 1', 'save 2', or 'save 3'.");
            return;
        }
        if (slot < 1 || slot > SaveManager.NUM_SLOTS) {
            System.out.println("  Slot must be 1, 2, or 3.");
            return;
        }
        GameSnapshot snapshot = world.captureState(player, difficulty);
        SaveManager.save(slot, snapshot);
        System.out.println("  Game saved to slot " + slot + ".");
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
        System.out.println("  befriend <npc>       Offer friendship instead of combat");
        System.out.println("  attack <enemy>       Attack an enemy");
        System.out.println("  solve                Attempt to solve a puzzle");
        System.out.println("  buy <item>           Buy from a merchant");
        System.out.println("  sell <item>          Sell to a merchant");
        System.out.println("  enchant <item>       Enchant gear at an enchanter (30/50/75g + Shadow Crystal)");
        System.out.println("  respawn              Pay to respawn enemies in an arena (Proving Grounds / Celestial Barracks)");
        System.out.println("  save <1-3>           Save game to slot 1, 2, or 3");
        System.out.println("  stats                View your character statistics");
        System.out.println("  quests               View your quest log");
        System.out.println("  map                  Open the world map");
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
        pause(1500);
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("                   V I C T O R Y                       ");
        System.out.println("════════════════════════════════════════════════════════");
        pause(800);
        System.out.println("\n  As the Shadow Lord's form dissolves, a faint light");
        System.out.println("  pulses from the centre of the throne room.");
        pause(1000);
        System.out.println("\n  The shattered Ancient Relic — its pieces scattered by");
        System.out.println("  the curse — begins to draw together. Fragment by");
        System.out.println("  fragment, it reassembles in a flood of warm golden light.");
        pause(1200);
        System.out.println("\n  The shadow corruption on the walls peels away. The");
        System.out.println("  purple crystals shatter. The kingdom breathes again.");
        pause(1000);
        System.out.println("\n  You carry the relic out into the open air. The sky");
        System.out.println("  above the corrupted kingdom — dark for so long —");
        System.out.println("  begins to clear. Stars appear where only emptiness");
        System.out.println("  stood before.");
        pause(1400);
        System.out.println("\n  The shadow followers scatter without their lord.");
        System.out.println("  The people who fled to hidden places will slowly return.");
        System.out.println("  The kingdom will take time to heal, but it will heal.");
        pause(1200);
        System.out.println("\n  ── " + player.getName() + " — the hero of the fallen kingdom. ──");
        pause(1000);
        System.out.println("\n  But somewhere north, beyond the Forgotten Battlefield,");
        System.out.println("  something ancient and divine still stirs. The Arbiter");
        System.out.println("  waits. That is a battle for another day — if you");
        System.out.println("  choose to return.");
        pause(1600);
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("              T H A N K S   F O R   P L A Y I N G      ");
        System.out.println("════════════════════════════════════════════════════════\n");
    }

    private void displayDefeat() {
        pause(1500);
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("                  G A M E   O V E R                    ");
        System.out.println("════════════════════════════════════════════════════════");
        pause(800);
        System.out.println("\n  You fall. The darkness closes in around you,");
        System.out.println("  cold and absolute.");
        pause(1000);
        System.out.println("\n  The last thing you see is the Shadow Lord standing");
        System.out.println("  above you — or whatever enemy has cut you down —");
        System.out.println("  its eyes burning with ancient indifference.");
        pause(1200);
        System.out.println("\n  The Ancient Relic stays shattered. The shadow");
        System.out.println("  corruption will spread unchecked. The villages");
        System.out.println("  will empty. The ruins will crumble further.");
        pause(1200);
        System.out.println("\n  The world will forget there was ever light.");
        pause(1400);
        System.out.println("\n  ── Perhaps another adventurer will rise where you fell. ──");
        pause(1000);
        System.out.println("\n════════════════════════════════════════════════════════\n");
    }

    // -------------------------------------------------------------------------
    // Quest initialisation
    // -------------------------------------------------------------------------

    private void initializeQuests() {
        questManager.initializeQuests(player);
    }

    // -------------------------------------------------------------------------
    // Tab-autocomplete provider — called from the EDT, returns target names
    // -------------------------------------------------------------------------

    /**
     * Returns a list of valid completion strings for the given command and target prefix.
     * Used by ExplorationPanel to power tab-autocomplete in the input field.
     * Safe to call from the EDT; returns empty list if the world is not yet initialised.
     */
    public List<String> getCompletions(String command, String prefix) {
        if (world == null || player == null) return Collections.emptyList();
        Location loc = world.getCurrentLocation();
        List<String> results = new ArrayList<>();
        String lp = prefix.toLowerCase();

        switch (command.toLowerCase()) {
            case "talk":
                for (NPC n : loc.getNpcs())
                    if (n.getName().toLowerCase().startsWith(lp)) results.add(n.getName());
                break;
            case "attack":
                for (Enemy e : loc.getEnemies())
                    if (e.getName().toLowerCase().startsWith(lp)) results.add(e.getName());
                break;
            case "take":
                for (Item i : loc.getItems())
                    if (i.getName().toLowerCase().startsWith(lp)) results.add(i.getName());
                break;
            case "drop": case "use": case "equip": case "sell": case "enchant":
                for (Item i : player.getInventory().getItems())
                    if (i.getName().toLowerCase().startsWith(lp)) results.add(i.getName());
                break;
            case "buy":
                Merchant m = loc.findNPCOfType(Merchant.class);
                if (m != null)
                    for (Item i : m.getShopItems())
                        if (i.getName().toLowerCase().startsWith(lp)) results.add(i.getName());
                break;
            case "go":
                for (Direction d : Direction.values())
                    if (loc.hasExit(d) && d.name().toLowerCase().startsWith(lp))
                        results.add(d.name().toLowerCase());
                break;
            default:
                break;
        }
        return results;
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private static void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
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
    public World   getWorld()    { return world; }
    boolean isRunning()   { return running; }

    /** Allows the combat system (or tests) to stop the main loop externally. */
    public void setRunning(boolean running) {
        this.running = running;
    }
}
