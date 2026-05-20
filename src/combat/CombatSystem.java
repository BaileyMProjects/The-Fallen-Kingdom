package combat;

import characters.Enemy;
import characters.Player;
import events.EventManager;
import events.GameEvent;
import events.GameEventType;
import items.Item;
import items.Potion;
import util.InputHandler;

/**
 * CombatSystem — orchestrates turn-based combat between the player and an enemy.
 *
 * Each round proceeds as:
 *   1. Display both characters' current HP.
 *   2. Player chooses: [A]ttack, [U]se item, or [R]un.
 *   3. Player's action is resolved.
 *   4. If the enemy is still alive, it attacks back using its AttackStrategy.
 *   5. Repeat until one side is defeated or the player escapes.
 *
 * STRATEGY PATTERN IN ACTION:
 *   The single line  enemy.getAttackStrategy().calculateDamage(enemy, player)
 *   is where the Strategy pattern does its work.  CombatSystem never checks
 *   enemy type — it just calls the interface.  Swap the strategy object and
 *   the entire combat feel changes.
 *
 * OBSERVER PATTERN INTEGRATION:
 *   On enemy defeat, an ENEMY_DEFEATED event is fired through EventManager so
 *   that QuestManager (and any other observer) can react without CombatSystem
 *   knowing about quests at all.
 */
public class CombatSystem {

    private final EventManager eventManager;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public CombatSystem(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    // -------------------------------------------------------------------------
    // Main combat loop
    // -------------------------------------------------------------------------

    /**
     * Runs a full combat encounter.
     *
     * @param player       the player character
     * @param enemy        the enemy to fight
     * @param inputHandler used to read player input during combat
     * @return {@code true}  if the player survived (won or fled),
     *         {@code false} if the player was killed
     */
    public boolean executeCombat(Player player, Enemy enemy, InputHandler inputHandler) {
        // OBSERVER PATTERN: GUI listens for this to switch to CombatPanel
        // and load the enemy's ASCII art.  Enemy object passed as context.
        eventManager.notify(new GameEvent(GameEventType.COMBAT_STARTED, enemy.getName(), enemy));
        pause(400); // let the GUI finish switching panels before text flows in

        System.out.println("\n  A " + enemy.getName() + " blocks your path!");
        pause(900);
        System.out.println("  Prepare yourself — combat begins!\n");
        pause(1000);

        while (player.isAlive() && enemy.isAlive()) {
            printCombatStatus(player, enemy);
            pause(500);

            boolean validAction = false;
            while (!validAction) {
                System.out.println("\n  [A] Attack    [U] Use item    [R] Run");
                String input = inputHandler.readInput().toLowerCase().trim();

                switch (input) {
                    case "a": case "attack":
                        playerAttack(player, enemy);
                        validAction = true;
                        break;

                    case "u": case "use":
                        validAction = handleCombatUseItem(player, inputHandler);
                        break;

                    case "r": case "run": case "flee":
                        if (attemptFlee(player, enemy, inputHandler)) {
                            eventManager.notify(new GameEvent(
                                    GameEventType.COMBAT_ENDED, enemy.getName()));
                            return true;
                        }
                        validAction = true;
                        break;

                    default:
                        System.out.println("  Invalid choice. Type 'a', 'u', or 'r'.");
                }
            }

            if (!enemy.isAlive()) break;

            // ----------------------------------------------------------------
            // STRATEGY PATTERN: the enemy attacks using whatever strategy was
            // injected by EnemyFactory — no if/else on enemy type here.
            // ----------------------------------------------------------------
            pause(700);
            enemyAttack(enemy, player);
            pause(800);
        }

        if (!player.isAlive()) {
            pause(2000); // hold the defeat screen long enough to read the message
            eventManager.notify(new GameEvent(GameEventType.COMBAT_ENDED, enemy.getName()));
            return false;
        }

        // Enemy defeated
        onEnemyDefeated(player, enemy);
        pause(2000); // hold the victory moment before returning to exploration
        eventManager.notify(new GameEvent(GameEventType.COMBAT_ENDED, enemy.getName()));
        pause(500); // let GUI switch back before loot text prints
        return true;
    }

    // -------------------------------------------------------------------------
    // Combat actions
    // -------------------------------------------------------------------------

    private void playerAttack(Player player, Enemy enemy) {
        int damage = Math.max(1, player.getAttackPower() - enemy.getDefense());
        System.out.println("\n  You strike " + enemy.getName() + "...");
        pause(750);
        enemy.takeDamage(damage);
        System.out.println("  Direct hit! " + damage + " damage dealt.");
        pause(400);
        System.out.println("  " + enemy.getName() + " HP: " + Math.max(0, enemy.getHealth()) + "/" + enemy.getMaxHealth());
        if (!enemy.isAlive()) {
            pause(700);
            System.out.println("\n  " + enemy.getName() + " has been defeated!");
        }
    }

    private void enemyAttack(Enemy enemy, Player player) {
        // STRATEGY PATTERN applied here
        int damage = enemy.getAttackStrategy().calculateDamage(enemy, player);
        System.out.println("\n  " + enemy.getName() + " attacks!");
        pause(750);
        player.takeDamage(damage);
        System.out.println("  " + enemy.getName() + " deals " + damage + " damage to you.");
        pause(400);
        System.out.println("  Your HP: " + Math.max(0, player.getHealth()) + "/" + player.getMaxHealth());
        if (!player.isAlive()) {
            pause(700);
            System.out.println("\n  You have been defeated...");
        }
    }

    /**
     * Handles using a consumable item mid-combat.
     * Only Potion subclasses are permitted in combat; other items are blocked
     * with an explanatory message.
     *
     * @return true if a valid action was taken (player's turn is consumed),
     *         false if the player cancelled or made no valid choice
     */
    private boolean handleCombatUseItem(Player player, InputHandler inputHandler) {
        if (player.getInventory().isEmpty()) {
            System.out.println("  You have no items to use.");
            return false;
        }

        System.out.println("\n  Inventory:");
        player.getInventory().listItems();
        System.out.println("  Enter item name (or 'cancel'):");
        System.out.print("  > ");
        String itemName = inputHandler.readInput().trim();

        if (itemName.equalsIgnoreCase("cancel")) {
            return false;
        }

        Item item = player.getInventory().findItem(itemName);
        if (item == null) {
            System.out.println("  You don't have a '" + itemName + "'.");
            return false;
        }
        if (!(item instanceof Potion)) {
            System.out.println("  You can only use consumable items during combat.");
            return false;
        }

        item.use(player);
        return true;
    }

    /**
     * Attempts to flee.  50% chance of success.
     * On failure the enemy gets one free attack as a penalty.
     *
     * @return true if the player successfully escaped
     */
    private boolean attemptFlee(Player player, Enemy enemy, InputHandler inputHandler) {
        System.out.println("\n  You attempt to flee...");
        pause(900);
        if (Math.random() < 0.5) {
            System.out.println("  You successfully escape from " + enemy.getName() + "!");
            return true;
        }
        System.out.println("  " + enemy.getName() + " cuts off your escape!");
        pause(600);
        enemyAttack(enemy, player);
        return false;
    }

    private static void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // -------------------------------------------------------------------------
    // Post-combat
    // -------------------------------------------------------------------------

    private void onEnemyDefeated(Player player, Enemy enemy) {
        int xp = Math.max(5, enemy.getMaxHealth() / 2);
        player.gainExperience(xp);

        // OBSERVER PATTERN: notify all subscribers that an enemy was defeated.
        // QuestManager listens for this to track kill-based quest progress.
        eventManager.notify(new GameEvent(GameEventType.ENEMY_DEFEATED, enemy.getName()));
    }

    // -------------------------------------------------------------------------
    // Display helper
    // -------------------------------------------------------------------------

    private void printCombatStatus(Player player, Enemy enemy) {
        System.out.println("\n  " + player.getName()
                + "  HP: " + player.getHealth() + "/" + player.getMaxHealth()
                + "    |    "
                + enemy.getName()
                + "  HP: " + enemy.getHealth() + "/" + enemy.getMaxHealth());
    }
}
