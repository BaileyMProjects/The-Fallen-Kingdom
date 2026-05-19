package characters;

import combat.AttackStrategy;
import combat.RandomStrategy;
import items.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private AttackStrategy  attackStrategy;
    private final int        goldDrop;
    private final List<Item> lootItems;
    private final String     description;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Enemy(String name, int maxHealth, int attackPower, int defense,
                 int goldDrop, String description) {
        super(name, maxHealth, attackPower, defense);
        this.goldDrop       = goldDrop;
        this.description    = description;
        this.lootItems      = new ArrayList<>();
        this.attackStrategy = new RandomStrategy();  // safe default until Factory sets it
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
