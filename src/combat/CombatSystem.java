package combat;

import characters.Enemy;
import characters.Player;
import enchantments.ArmourEnchantment;
import enchantments.WeaponEnchantment;
import events.EventManager;
import events.GameEvent;
import events.GameEventType;
import items.Armour;
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

    private static final double BASE_MISS_CHANCE = 0.10;

    private final EventManager eventManager;
    private double  enemyDamageMultiplier = 1.0;

    // Per-combat state — reset at the start of every executeCombat call
    private int     poisonTurnsRemaining = 0;
    private int     poisonDamagePerTurn  = 0;
    private boolean enemySlowed          = false;
    private boolean enemyStunned         = false;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public CombatSystem(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /** Called by Game after the player selects a difficulty. */
    public void setDamageMultiplier(double multiplier) {
        this.enemyDamageMultiplier = multiplier;
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
        poisonTurnsRemaining = 0;
        poisonDamagePerTurn  = 0;
        enemySlowed          = false;
        enemyStunned         = false;

        eventManager.notify(new GameEvent(GameEventType.COMBAT_STARTED, enemy.getName(), enemy));
        pause(400); // let the GUI finish switching panels before text flows in

        System.out.println("\n  A " + enemy.getName() + " blocks your path!");
        pause(900);
        System.out.println("  Prepare yourself — combat begins!\n");
        pause(1000);

        while (player.isAlive() && enemy.isAlive()) {
            applyRegen(player);
            applyPoison(enemy);
            if (!enemy.isAlive()) break;
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
        if (Math.random() < BASE_MISS_CHANCE) {
            System.out.println("\n  You swing at " + enemy.getName() + "... and miss!");
            return;
        }
        int damage = Math.max(1, player.getAttackPower() - enemy.getDefense());
        System.out.println("\n  You strike " + enemy.getName() + "...");
        pause(750);
        enemy.takeDamage(damage);
        System.out.println("  Direct hit! " + damage + " damage dealt.");
        pause(400);

        // Weapon enchantment on-hit effects
        if (player.getEquippedWeapon() instanceof WeaponEnchantment) {
            WeaponEnchantment we = (WeaponEnchantment) player.getEquippedWeapon();

            if (we.getLifestealRate() > 0) {
                int healed = player.heal((int)(damage * we.getLifestealRate()));
                if (healed > 0)
                    System.out.println("  Lifesteal restores " + healed + " HP.");
            }

            if (we.getPoisonDamage() > 0 && poisonTurnsRemaining == 0) {
                poisonDamagePerTurn  = we.getPoisonDamage();
                poisonTurnsRemaining = we.getPoisonTurns();
                System.out.println("  Your weapon's venom poisons " + enemy.getName() + "!");
            }

            if (we.getSlowChance() > 0 && Math.random() < we.getSlowChance()) {
                enemySlowed = true;
                System.out.println("  Frost seeps into " + enemy.getName() + " — they are slowed!");
            }

            if (we.getStunChance() > 0 && Math.random() < we.getStunChance()) {
                enemyStunned = true;
                System.out.println("  The force of your blow stuns " + enemy.getName() + "!");
            }
        }

        System.out.println("  " + enemy.getName() + " HP: " + Math.max(0, enemy.getHealth()) + "/" + enemy.getMaxHealth());
        if (!enemy.isAlive()) {
            pause(700);
            System.out.println("\n  " + enemy.getName() + " has been defeated!");
        }
    }

    private void enemyAttack(Enemy enemy, Player player) {
        // Check stun — enemy loses entire turn
        if (enemyStunned) {
            System.out.println("\n  " + enemy.getName() + " is stunned and cannot attack!");
            enemyStunned = false;
            enemySlowed  = false;
            return;
        }
        // Check slow — enemy loses their attack this turn
        if (enemySlowed) {
            System.out.println("\n  " + enemy.getName() + " is slowed by the frost and loses their turn!");
            enemySlowed = false;
            return;
        }

        // Base 10% miss + any bonus from the player's equipped armour (e.g. Evasive Boots)
        double totalMiss = BASE_MISS_CHANCE + player.getEvasionBonus();
        if (Math.random() < totalMiss) {
            System.out.println("\n  " + enemy.getName() + " attacks — but misses you!");
            return;
        }
        // STRATEGY PATTERN applied here; difficulty multiplier then scales the result
        int raw    = enemy.getAttackStrategy().calculateDamage(enemy, player);
        int damage = (int) Math.max(1, Math.round(raw * enemyDamageMultiplier));
        System.out.println("\n  " + enemy.getName() + " attacks!");
        pause(750);
        player.takeDamage(damage);
        System.out.println("  " + enemy.getName() + " deals " + damage + " damage to you.");
        pause(400);

        // Armour enchantment reflect
        double reflectRate = getTotalReflectRate(player);
        if (reflectRate > 0 && enemy.isAlive()) {
            int reflected = Math.max(1, (int)(damage * reflectRate));
            enemy.takeDamage(reflected);
            System.out.println("  Your enchanted armour reflects " + reflected + " damage!");
            if (!enemy.isAlive()) {
                pause(700);
                System.out.println("\n  " + enemy.getName() + " has been defeated by reflected damage!");
            }
        }

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

    // -------------------------------------------------------------------------
    // Enchantment effect helpers
    // -------------------------------------------------------------------------

    private void applyRegen(Player player) {
        int regen = getTotalRegenAmount(player);
        if (regen > 0 && player.getHealth() < player.getMaxHealth()) {
            int healed = player.heal(regen);
            if (healed > 0)
                System.out.println("\n  Your armour's magic restores " + healed + " HP.");
        }
    }

    private void applyPoison(Enemy enemy) {
        if (poisonTurnsRemaining <= 0) return;
        System.out.println("\n  " + enemy.getName() + " writhes as venom courses through them!");
        pause(400);
        enemy.takeDamage(poisonDamagePerTurn);
        poisonTurnsRemaining--;
        System.out.println("  Poison deals " + poisonDamagePerTurn + " damage."
                + " (" + poisonTurnsRemaining + " turn(s) remaining)");
        if (!enemy.isAlive()) {
            pause(700);
            System.out.println("\n  " + enemy.getName() + " has succumbed to the poison!");
        }
    }

    private double getTotalReflectRate(Player player) {
        double rate = 0.0;
        Armour[] slots = { player.getEquippedHead(), player.getEquippedTorso(), player.getEquippedLegs() };
        for (Armour a : slots) {
            if (a instanceof ArmourEnchantment)
                rate += ((ArmourEnchantment) a).getReflectRate();
        }
        return rate;
    }

    private int getTotalRegenAmount(Player player) {
        int total = 0;
        Armour[] slots = { player.getEquippedHead(), player.getEquippedTorso(), player.getEquippedLegs() };
        for (Armour a : slots) {
            if (a instanceof ArmourEnchantment)
                total += ((ArmourEnchantment) a).getRegenAmount();
        }
        return total;
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
