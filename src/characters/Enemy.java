package characters;

import combat.AttackStrategy;
import combat.EnemySpecialAttack;
import combat.RandomStrategy;
import items.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Enemy — a hostile character the player can fight.
 *
 * Enemies carry:
 *   - An AttackStrategy (Strategy pattern) governing their combat behaviour.
 *     This allows different enemies to fight differently without subclassing
 *     for every variation. EnemyFactory injects the correct strategy.
 *   - A gold drop and optional loot items awarded on defeat.
 *
 * Pattern reference: the AttackStrategy field is where the Strategy pattern
 * is applied. See the combat package for implementations.
 */
public class Enemy extends Character {

    private static final Random RNG = new Random();

    private AttackStrategy  attackStrategy;
    private final int        goldDrop;
    private final List<Item> lootItems;
    private final List<Item>   chanceLootItems;
    private final List<Double> chanceLootChances;
    private final List<EnemySpecialAttack> specialAttacks = new ArrayList<>();
    private final String     description;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Enemy(String name, int maxHealth, int attackPower, int defense,
                 int goldDrop, String description) {
        super(name, maxHealth, attackPower, defense);
        this.goldDrop          = goldDrop;
        this.description       = description;
        this.lootItems         = new ArrayList<>();
        this.chanceLootItems   = new ArrayList<>();
        this.chanceLootChances = new ArrayList<>();
        this.attackStrategy    = new RandomStrategy();  // safe default until Factory sets it
    }

    // -------------------------------------------------------------------------
    // Strategy pattern — combat AI
    // -------------------------------------------------------------------------

    /**
     * Replaces the combat strategy for this enemy.
     * Called by EnemyFactory to assign the appropriate AI to each enemy type.
     */
    public void setAttackStrategy(AttackStrategy strategy) {
        this.attackStrategy = strategy;
    }

    public AttackStrategy getAttackStrategy() {
        return attackStrategy;
    }

    // -------------------------------------------------------------------------
    // Loot
    // -------------------------------------------------------------------------

    public void addLootItem(Item item)   { lootItems.add(item); }
    public List<Item> getLootItems()     { return Collections.unmodifiableList(lootItems); }
    public int        getGoldDrop()      { return goldDrop; }

    /**
     * Registers a loot item that drops with the given probability (0.0–1.0).
     * Rolled once per enemy defeat in Game.java.
     */
    public void addChanceLoot(Item item, double chance) {
        chanceLootItems.add(item);
        chanceLootChances.add(Math.min(1.0, Math.max(0.0, chance)));
    }

    /**
     * Rolls all chance-based loot entries and returns every item that dropped.
     * Called by Game after the guaranteed loot loop.
     */
    public List<Item> rollChanceLoot() {
        List<Item> dropped = new ArrayList<>();
        for (int i = 0; i < chanceLootItems.size(); i++) {
            if (RNG.nextDouble() < chanceLootChances.get(i)) {
                dropped.add(chanceLootItems.get(i));
            }
        }
        return dropped;
    }

    // -------------------------------------------------------------------------
    // Special attacks
    // -------------------------------------------------------------------------

    public void addSpecialAttack(EnemySpecialAttack s) { specialAttacks.add(s); }

    /** Rolls each registered special in order; returns the first that fires, or null. */
    public EnemySpecialAttack rollSpecialAttack() {
        for (EnemySpecialAttack s : specialAttacks) {
            if (RNG.nextDouble() < s.chance) return s;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Boss flag — overridden to true in the Boss subclass
    // -------------------------------------------------------------------------

    public boolean isBoss() { return false; }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    @Override
    public String getDescription() {
        return description + "\n"
             + "  HP: "      + getHealth()      + "/" + getMaxHealth()
             + "  Attack: "  + getAttackPower()
             + "  Defense: " + getDefense();
    }
}
