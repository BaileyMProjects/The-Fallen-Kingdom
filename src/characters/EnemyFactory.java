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

    public static Enemy create(EnemyType type) {
        switch (type) {
            case SHADOW_GOBLIN:       return createShadowGoblin();
            case DARK_KNIGHT:         return createDarkKnight();
            case SHADOW_LORD:         return createShadowLord();
            case PLAGUE_RAT:          return createPlagueRat();
            case STONE_SENTINEL:      return createStoneSentinel();
            case VOID_WRAITH:         return createVoidWraith();
            case TREE_PROTECTOR_ENEMY: return createTreeProtectorEnemy();
            default:
                throw new IllegalArgumentException("Unknown enemy type: " + type);
        }
    }

    // -------------------------------------------------------------------------
    // Original enemies
    // -------------------------------------------------------------------------

    private static Enemy createShadowGoblin() {
        Enemy goblin = new Enemy(
            "Shadow Goblin",
            30, 8, 2, 10,
            "A hunched creature wreathed in shadow with glowing red eyes.\n" +
            "  Its movements are twitchy and unpredictable."
        );
        goblin.setAttackStrategy(new RandomStrategy());
        return goblin;
    }

    private static Enemy createDarkKnight() {
        Enemy knight = new Enemy(
            "Dark Knight",
            70, 15, 8, 25,
            "A towering knight clad in black armour that seems to drink the light.\n" +
            "  He moves with slow, measured precision — every step deliberate."
        );
        knight.setAttackStrategy(new DefensiveStrategy());
        knight.addLootItem(ItemFactory.create(ItemType.HEALTH_POTION));
        return knight;
    }

    private static Enemy createShadowLord() {
        Boss shadowLord = new Boss(
            "Shadow Lord",
            120, 20, 10, 0,
            "A being of pure darkness standing twice the height of a man.\n" +
            "  His eyes burn with ancient malice. The air grows cold around him."
        );
        shadowLord.setAttackStrategy(new AggressiveStrategy());
        shadowLord.addLootItem(ItemFactory.create(ItemType.ANCIENT_RELIC));
        return shadowLord;
    }

    // -------------------------------------------------------------------------
    // New enemies
    // -------------------------------------------------------------------------

    /**
     * Plague Rat — a sickly swarm enemy found in the Dark Forest and Battlefield.
     * Weak individually but their numbers make them unpredictable.
     * Uses RandomStrategy to represent erratic swarming behaviour.
     */
    private static Enemy createPlagueRat() {
        Enemy rat = new Enemy(
            "Plague Rat Swarm",
            25, 6, 1, 5,
            "A seething mass of diseased rats that move as one writhing body.\n" +
            "  Their bites are weak but their sheer number is unsettling."
        );
        rat.setAttackStrategy(new RandomStrategy());
        rat.addLootItem(ItemFactory.create(ItemType.HEALTH_POTION));
        return rat;
    }

    /**
     * Stone Sentinel — a heavy golem-like guardian on the Forgotten Battlefield.
     * High defense makes it durable; DefensiveStrategy means it occasionally
     * lands a powerful Shield Bash despite its slow nature.
     */
    private static Enemy createStoneSentinel() {
        Enemy sentinel = new Enemy(
            "Stone Sentinel",
            55, 14, 10, 15,
            "A massive construct of ancient stone, animated by war-magic long\n" +
            "  since faded. Its fists crack the earth with every step."
        );
        sentinel.setAttackStrategy(new DefensiveStrategy());
        return sentinel;
    }

    /**
     * Void Wraith — an aggressive shadow-energy creature haunting the castle's depths.
     * Low defense but hits hard; AggressiveStrategy means its attacks deal 1.5× raw
     * damage before defense, making it dangerous even to well-armoured players.
     * Drops an Elixir — a nod to the corrupted energy it feeds on.
     */
    private static Enemy createVoidWraith() {
        Enemy wraith = new Enemy(
            "Void Wraith",
            45, 18, 3, 20,
            "A formless entity of swirling shadow energy. It has no face — only\n" +
            "  two burning violet eyes that fix on you with terrible hunger."
        );
        wraith.setAttackStrategy(new AggressiveStrategy());
        wraith.addLootItem(ItemFactory.create(ItemType.ELIXIR));
        return wraith;
    }

    /**
     * Tree Protector (enemy form) — the guardian of the Mystic Glade when the
     * player chooses to fight instead of befriend.
     * He is ancient and powerful in spirit but physically weak — he was never
     * meant to be a warrior. Drops the Forest Gem (visible in the sacred oak).
     */
    private static Enemy createTreeProtectorEnemy() {
        Enemy protector = new Enemy(
            "Tree Protector",
            35, 6, 2, 0,
            "A figure woven from living bark and ancient roots. He did not choose\n" +
            "  this fight — but he will not flee from it."
        );
        protector.setAttackStrategy(new RandomStrategy());
        protector.addLootItem(ItemFactory.create(ItemType.FOREST_GEM));
        return protector;
    }
}
