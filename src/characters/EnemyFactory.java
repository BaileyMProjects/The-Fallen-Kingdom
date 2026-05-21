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
            case SHADOW_GOBLIN:        return createShadowGoblin();
            case DARK_KNIGHT:          return createDarkKnight();
            case SHADOW_LORD:          return createShadowLord();
            case PLAGUE_RAT:           return createPlagueRat();
            case STONE_SENTINEL:       return createStoneSentinel();
            case VOID_WRAITH:          return createVoidWraith();
            case TREE_PROTECTOR_ENEMY: return createTreeProtectorEnemy();
            // Divine realm
            case CORRUPTED_PALADIN:    return createCorruptedPaladin();
            case RADIANT_STALKER:      return createRadiantStalker();
            case CELESTIAL_SENTINEL:   return createCelestialSentinel();
            case FALLEN_SERAPH:        return createFallenSeraph();
            case THE_ARBITER:          return createTheArbiter();
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
        knight.addChanceLoot(ItemFactory.create(ItemType.SHADOW_CRYSTAL), 0.12);
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
        sentinel.addChanceLoot(ItemFactory.create(ItemType.SHADOW_CRYSTAL), 0.13);
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
        wraith.addChanceLoot(ItemFactory.create(ItemType.SHADOW_CRYSTAL), 0.15);
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

    // -------------------------------------------------------------------------
    // Divine realm enemies
    // -------------------------------------------------------------------------

    /**
     * Corrupted Paladin — a fallen holy soldier, the most common divine enemy.
     * High defense reflects the heavy plate they wore in life.
     * Rare chance to drop the Corrupted Mace they carry.
     */
    private static Enemy createCorruptedPaladin() {
        Enemy paladin = new Enemy(
            "Corrupted Paladin",
            50, 13, 10, 22,
            "A knight clad in once-gleaming armour now cracked with dark fissures.\n" +
            "  A divine sigil on their breastplate is crossed out — consumed by shadow."
        );
        paladin.setAttackStrategy(new DefensiveStrategy());
        paladin.addChanceLoot(ItemFactory.create(ItemType.CORRUPTED_MACE), 0.12);
        paladin.addChanceLoot(ItemFactory.create(ItemType.DIVINE_CRYSTAL),  0.09);
        return paladin;
    }

    /**
     * Radiant Stalker — a fast, lightly armoured hunter of the divine realm.
     * Aggressive strategy and high attack make it dangerous despite low defense.
     */
    private static Enemy createRadiantStalker() {
        Enemy stalker = new Enemy(
            "Radiant Stalker",
            40, 16, 5, 20,
            "A lithe figure wrapped in tattered white robes, moving with unsettling\n" +
            "  speed. Its eyes burn gold — something divine still lingers in it."
        );
        stalker.setAttackStrategy(new AggressiveStrategy());
        stalker.addChanceLoot(ItemFactory.create(ItemType.DIVINE_CRYSTAL), 0.08);
        return stalker;
    }

    /**
     * Celestial Sentinel — the divine equivalent of the Stone Sentinel.
     * Very high HP and defense; drops good gold and crystals.
     * The primary farming target in the Celestial Barracks arena.
     */
    private static Enemy createCelestialSentinel() {
        Enemy sentinel = new Enemy(
            "Celestial Sentinel",
            85, 17, 15, 32,
            "A towering construct of divine stone and golden runes, animated by the\n" +
            "  last embers of celestial war-magic. Each step shakes the ground."
        );
        sentinel.setAttackStrategy(new DefensiveStrategy());
        sentinel.addChanceLoot(ItemFactory.create(ItemType.DIVINE_CRYSTAL), 0.10);
        return sentinel;
    }

    /**
     * Fallen Seraph — the most dangerous regular enemy in the divine realm.
     * Aggressive and fast; the main threat in the Barracks wave and the Vault.
     */
    private static Enemy createFallenSeraph() {
        Enemy seraph = new Enemy(
            "Fallen Seraph",
            70, 22, 7, 38,
            "A winged figure, its feathers the colour of ash and dried blood.\n" +
            "  It moves in bursts of terrifying speed, golden eyes devoid of mercy."
        );
        seraph.setAttackStrategy(new AggressiveStrategy());
        seraph.addLootItem(ItemFactory.create(ItemType.HEALTH_POTION));
        seraph.addChanceLoot(ItemFactory.create(ItemType.DIVINE_CRYSTAL), 0.09);
        return seraph;
    }

    /**
     * The Arbiter — the divine area's supreme guardian and optional boss.
     * 220 HP, the highest of any enemy. Drops the Seraphic Blade and a
     * guaranteed Divine Crystal. Does NOT extend Boss — defeating him does
     * not end the game.
     */
    private static Enemy createTheArbiter() {
        Enemy arbiter = new Enemy(
            "The Arbiter",
            220, 26, 18, 0,
            "A towering figure of celestial armour and blinding light. Once the\n" +
            "  supreme judge of the divine order — now lost to grief and madness.\n" +
            "  Two enormous wings of shattered light frame him like a broken halo.\n" +
            "  His voice resonates with a power that shakes the sanctum walls."
        );
        arbiter.setAttackStrategy(new AggressiveStrategy());
        arbiter.addLootItem(ItemFactory.create(ItemType.SERAPHIC_BLADE));
        arbiter.addLootItem(ItemFactory.create(ItemType.DIVINE_CRYSTAL));
        return arbiter;
    }
}
