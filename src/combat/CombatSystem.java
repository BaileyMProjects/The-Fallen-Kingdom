package combat;

import characters.Boss;
import characters.Enemy;
import characters.Player;
import enchantments.ArmourEnchantment;
import enchantments.WeaponEnchantment;
import events.EventManager;
import events.GameEvent;
import events.GameEventType;
import items.Armour;
import items.CombatPotion;
import items.Item;
import items.Potion;
import util.InputHandler;

/**
 * CombatSystem — orchestrates turn-based combat between the player and an enemy.
 *
 * Each round proceeds as:
 *   1. Apply regen (armour enchant) and poison (weapon enchant / Venom Flask) to start of turn.
 *   2. Display both characters' current HP.
 *   3. Player chooses: [A]ttack, [U]se item, or [R]un.
 *   4. If the player used a CombatPotion, apply pending enemy effects (poison, stun).
 *   5. If the enemy is still alive, it attacks using its AttackStrategy.
 *   6. Decrement player combat-buff durations.
 *   7. Repeat until one side is defeated or the player escapes.
 *
 * STRATEGY PATTERN: enemy.getAttackStrategy().calculateDamage() is called once
 * per enemy attack — no switch on enemy type anywhere in this class.
 *
 * PHASE TRANSITIONS: after each player attack, checkPhaseTransition() is called
 * on Boss enemies.  If it fires, the boss fully regenerates and stats increase.
 *
 * COMBAT POTIONS: item.use(player) sets pending-effect fields on Player.
 * applyPendingCombatItemEffects() drains those fields into CombatSystem state
 * between the player action and the enemy response.
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

    public boolean executeCombat(Player player, Enemy enemy, InputHandler inputHandler) {
        // Reset all per-combat state
        poisonTurnsRemaining = 0;
        poisonDamagePerTurn  = 0;
        enemySlowed          = false;
        enemyStunned         = false;
        player.clearCombatState();

        eventManager.notify(new GameEvent(GameEventType.COMBAT_STARTED, enemy.getName(), enemy));
        pause(400);

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

            // Drain any pending effects set by CombatPotion this turn
            applyPendingCombatItemEffects(player);

            pause(700);
            enemyAttack(enemy, player);
            pause(800);

            // Tick down player buff/debuff durations at end of each full turn
            player.decrementCombatBuffs();
        }

        if (!player.isAlive()) {
            pause(2000);
            eventManager.notify(new GameEvent(GameEventType.COMBAT_ENDED, enemy.getName()));
            return false;
        }

        onEnemyDefeated(player, enemy);
        pause(2000);
        eventManager.notify(new GameEvent(GameEventType.COMBAT_ENDED, enemy.getName()));
        pause(500);
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
        int damage = Math.max(1,
                player.getAttackPower() + player.getCombatAttackBonus() - enemy.getDefense());
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
                if (enemy instanceof Boss && ((Boss) enemy).isImmuneToStun()) {
                    System.out.println("  Your strike crackles with energy — but " + enemy.getName()
                            + " shrugs off the stun!");
                } else {
                    enemyStunned = true;
                    System.out.println("  The force of your blow stuns " + enemy.getName() + "!");
                }
            }
        }

        System.out.println("  " + enemy.getName() + " HP: "
                + Math.max(0, enemy.getHealth()) + "/" + enemy.getMaxHealth());

        if (!enemy.isAlive()) {
            pause(700);
            System.out.println("\n  " + enemy.getName() + " has been defeated!");
            return;
        }

        // Phase transition check — only on Boss enemies
        if (enemy instanceof Boss) {
            String msg = ((Boss) enemy).checkPhaseTransition();
            if (msg != null) displayPhaseTransition((Boss) enemy);
        }
    }

    private void enemyAttack(Enemy enemy, Player player) {
        // Stun check
        if (enemyStunned) {
            if (enemy instanceof Boss && ((Boss) enemy).isImmuneToStun()) {
                System.out.println("\n  " + enemy.getName()
                        + " shrugs off the stun — in this form they cannot be held!");
                enemyStunned = false;
                // fall through — they still attack
            } else {
                System.out.println("\n  " + enemy.getName() + " is stunned and cannot attack!");
                enemyStunned = false;
                enemySlowed  = false;
                return;
            }
        }

        // Slow check
        if (enemySlowed) {
            System.out.println("\n  " + enemy.getName() + " is slowed and loses their turn!");
            enemySlowed = false;
            return;
        }

        // Miss check: base + player evasion + blind debuff from combat item
        double blindBonus = player.getPendingEnemyBlindMissBonus() / 100.0;
        double totalMiss  = BASE_MISS_CHANCE + player.getEvasionBonus() + blindBonus;
        if (Math.random() < totalMiss) {
            System.out.println("\n  " + enemy.getName() + " attacks — but misses you!");
            return;
        }

        // Damage calculation — subtract combat defense bonus from incoming damage
        int raw    = enemy.getAttackStrategy().calculateDamage(enemy, player);
        int damage = (int) Math.max(1,
                Math.round(raw * enemyDamageMultiplier) - player.getCombatDefenseBonus());
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
     * Both Potion and CombatPotion are permitted; all other items are blocked.
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

        if (itemName.equalsIgnoreCase("cancel")) return false;

        Item item = player.getInventory().findItem(itemName);
        if (item == null) {
            System.out.println("  You don't have a '" + itemName + "'.");
            return false;
        }
        if (!(item instanceof Potion) && !(item instanceof CombatPotion)) {
            System.out.println("  You can only use potions during combat.");
            return false;
        }

        item.use(player);
        return true;
    }

    /**
     * Drains pending enemy effects that a CombatPotion set on the Player this turn
     * and applies them to CombatSystem state so they take effect on the enemy's
     * upcoming attack or the following turn's poison tick.
     */
    private void applyPendingCombatItemEffects(Player player) {
        int poisonDmg   = player.drainPendingPoisonDamage();
        int poisonTurns = player.drainPendingPoisonTurns();
        if (poisonDmg > 0) {
            poisonDamagePerTurn  = poisonDmg;
            poisonTurnsRemaining = poisonTurns;
        }
        if (player.drainPendingEnemyStun()) {
            enemyStunned = true;
        }
    }

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
    // Phase transition display
    // -------------------------------------------------------------------------

    private void displayPhaseTransition(Boss boss) {
        pause(500);
        System.out.println("\n  ══════════════════════════════════════════════════");
        System.out.println("  " + boss.getName().toUpperCase() + " LET OUT A TERRIFYING ROAR!");
        pause(900);
        System.out.println("  Darkness erupts from its form — it is REFORMING!");
        pause(1000);
        System.out.println("  The air turns to ice. A second power awakens...");
        pause(900);
        System.out.println("  HP FULLY RESTORED. PHASE 2 BEGINS.");
        System.out.println("  ══════════════════════════════════════════════════\n");
        pause(1400);
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

    // -------------------------------------------------------------------------
    // Post-combat
    // -------------------------------------------------------------------------

    private void onEnemyDefeated(Player player, Enemy enemy) {
        int xp = Math.max(5, enemy.getMaxHealth() / 2);
        player.gainExperience(xp);
        eventManager.notify(new GameEvent(GameEventType.ENEMY_DEFEATED, enemy.getName()));
    }

    // -------------------------------------------------------------------------
    // Display / utility
    // -------------------------------------------------------------------------

    private void printCombatStatus(Player player, Enemy enemy) {
        System.out.println("\n  " + player.getName()
                + "  HP: " + player.getHealth() + "/" + player.getMaxHealth()
                + "    |    "
                + enemy.getName()
                + "  HP: " + enemy.getHealth() + "/" + enemy.getMaxHealth());
    }

    private static void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
