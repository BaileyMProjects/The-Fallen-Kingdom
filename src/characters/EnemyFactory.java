package characters;

import combat.AggressiveStrategy;
import combat.DefensiveStrategy;
import combat.RandomStrategy;
import items.ItemFactory;
import items.ItemType;

/**
 * EnemyFactory — Factory design pattern for enemy creation (creational).
 *
 * Centralises all enemy instantiation behind a single static method.
 * Game code asks for an EnemyType and receives a fully configured Enemy —
 * it never calls 'new ShadowGoblin()' directly and never sets strategies
 * or loot itself.
 *
 * WHY FACTORY HERE:
 *   Each enemy requires several configuration steps after construction
 *   (assign strategy, add loot items).  Without the factory these steps
 *   would be duplicated wherever an enemy is created.  The factory is the
 *   single place to change enemy stats, strategies, or drops — game balance
 *   tuning touches exactly one file.
 *
 * HOW TO EXTEND:
 *   1. Add a constant to EnemyType.
 *   2. Add a private create method here.
 *   3. Add a case to the switch.
 *   World.java and any other caller require zero changes (Open/Closed Principle).
 */
public class EnemyFactory {

    /** Utility class — prevent instantiation. */
    private EnemyFactory() {}

    // -------------------------------------------------------------------------
    // Factory method
    // -------------------------------------------------------------------------

    /**
     * Creates and fully configures an enemy of the given type.
     *
     * @param type the kind of enemy to create
     * @return a new, ready-to-use Enemy instance
     * @throws IllegalArgumentException if the type is unrecognised
     */
    public static Enemy create(EnemyType type) {
        switch (type) {
            case SHADOW_GOBLIN: return createShadowGoblin();
            case DARK_KNIGHT:   return createDarkKnight();
            case SHADOW_LORD:   return createShadowLord();
            default:
                throw new IllegalArgumentException("Unknown enemy type: " + type);
        }
    }

    // -------------------------------------------------------------------------
    // Private creation methods — one per enemy type
    // -------------------------------------------------------------------------

    /**
     * Shadow Goblin — a weak early-game enemy.
     * Uses RandomStrategy: its attacks vary between weak, normal, and heavy
     * hits, making it unpredictable despite low base stats.
     */
    private static Enemy createShadowGoblin() {
        Enemy goblin = new Enemy(
            "Shadow Goblin",
            30,   // HP
            8,    // attack
            2,    // defense
            10,   // gold drop
            "A hunched creature wreathed in shadow with glowing red eyes.\n" +
            "  Its movements are twitchy and unpredictable."
        );
        goblin.setAttackStrategy(new RandomStrategy());
        return goblin;
    }

    /**
     * Dark Knight — a mid-game armoured enemy.
     * Uses DefensiveStrategy: lower base damage but a 30% chance of a
     * Shield Bash that deals bonus damage — makes him feel disciplined but
     * dangerous.  Drops a Health Potion on defeat.
     */
    private static Enemy createDarkKnight() {
        Enemy knight = new Enemy(
            "Dark Knight",
            70,   // HP
            15,   // attack
            8,    // defense
            25,   // gold drop
            "A towering knight clad in black armour that seems to drink the light.\n" +
            "  He moves with slow, measured precision — every step deliberate."
        );
        knight.setAttackStrategy(new DefensiveStrategy());
        knight.addLootItem(ItemFactory.create(ItemType.HEALTH_POTION));
        return knight;
    }

    /**
     * Shadow Lord — the final boss.
     * Created as a Boss (isBoss() == true) so Game triggers the victory
     * screen on defeat.  Uses AggressiveStrategy: 1.5× multiplier before
     * applying player defence, making him consistently lethal.
     * Drops the Ancient Relic — the win condition item.
     */
    private static Enemy createShadowLord() {
        Boss shadowLord = new Boss(
            "Shadow Lord",
            120,  // HP
            20,   // attack
            10,   // defense
            0,    // gold drop (irrelevant — winning is the reward)
            "A being of pure darkness standing twice the height of a man.\n" +
            "  His eyes burn with ancient malice. The air grows cold around him."
        );
        shadowLord.setAttackStrategy(new AggressiveStrategy());
        shadowLord.addLootItem(ItemFactory.create(ItemType.ANCIENT_RELIC));
        return shadowLord;
    }
}
