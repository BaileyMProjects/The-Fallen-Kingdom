package characters;

import combat.AggressiveStrategy;
import combat.DefensiveStrategy;
import combat.EnemySpecialAttack;
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
            35, 12, 3, 10,
            "A hunched creature wreathed in shadow with glowing red eyes.\n" +
            "  Its movements are twitchy and unpredictable."
        );
        goblin.setAttackStrategy(new RandomStrategy());
        goblin.addSpecialAttack(new EnemySpecialAttack(
            "Shadow Swipe",
            "The goblin dashes through the shadows and swipes at you!",
            0.20, 1.4, false, 0, false
        ));
        return goblin;
    }

    private static Enemy createDarkKnight() {
        Enemy knight = new Enemy(
            "Dark Knight",
            300, 55, 22, 25,
            "A towering knight clad in black armour that seems to drink the light.\n" +
            "  He moves with slow, measured precision — every step deliberate."
        );
        knight.setAttackStrategy(new DefensiveStrategy());
        knight.addLootItem(ItemFactory.create(ItemType.HEALTH_POTION));
        knight.addChanceLoot(ItemFactory.create(ItemType.SHADOW_CRYSTAL), 0.12);
        knight.addSpecialAttack(new EnemySpecialAttack(
            "Shield Bash",
            "The Dark Knight slams his shield into you, leaving you dazed!",
            0.25, 1.1, false, 0, true
        ));
        knight.addSpecialAttack(new EnemySpecialAttack(
            "Crushing Strike",
            "The Dark Knight brings his blade down with devastating power!",
            0.15, 2.2, false, 0, false
        ));
        return knight;
    }

    private static Enemy createShadowLord() {
        Boss shadowLord = new Boss(
            "Shadow Lord",
            1500, 90, 30, 0,
            "A being of pure darkness standing twice the height of a man.\n" +
            "  His eyes burn with ancient malice. The air grows cold around him.\n" +
            "  You sense that he is holding something back — a second darkness\n" +
            "  coiled within, waiting for the moment he is pushed to the edge."
        );
        shadowLord.setAttackStrategy(new AggressiveStrategy());
        shadowLord.addLootItem(ItemFactory.create(ItemType.ANCIENT_RELIC));
        shadowLord.setPhase2Boosts(25, 12);
        shadowLord.addSpecialAttack(new EnemySpecialAttack(
            "Shadow Slam",
            "The Shadow Lord's darkness crashes through your very soul!",
            0.30, 1.8, true, 0, false
        ));
        shadowLord.addSpecialAttack(new EnemySpecialAttack(
            "Dark Pulse",
            "A pulse of pure darkness roots you in place — you cannot act!",
            0.20, 1.0, false, 0, true
        ));
        shadowLord.addSpecialAttack(new EnemySpecialAttack(
            "Corruption Nova",
            "The Shadow Lord unleashes a nova of corruption, feasting on your suffering!",
            0.15, 2.2, false, 60, false
        ));
        return shadowLord;
    }

    // -------------------------------------------------------------------------
    // New enemies
    // -------------------------------------------------------------------------

    /**
     * Plague Rat — a sickly swarm enemy found in the Dark Forest and Battlefield.
     * Weak individually but their numbers make them unpredictable.
     */
    private static Enemy createPlagueRat() {
        Enemy rat = new Enemy(
            "Plague Rat Swarm",
            30, 10, 2, 5,
            "A seething mass of diseased rats that move as one writhing body.\n" +
            "  Their bites are weak but their sheer number is unsettling."
        );
        rat.setAttackStrategy(new RandomStrategy());
        rat.addLootItem(ItemFactory.create(ItemType.HEALTH_POTION));
        rat.addSpecialAttack(new EnemySpecialAttack(
            "Frenzied Swarm",
            "The rats swarm over you in a frenzy, overwhelming your senses!",
            0.25, 1.5, false, 0, true
        ));
        return rat;
    }

    /**
     * Stone Sentinel — a heavy golem-like guardian on the Forgotten Battlefield.
     * Very high HP and defense; slow but punishing.
     */
    private static Enemy createStoneSentinel() {
        Enemy sentinel = new Enemy(
            "Stone Sentinel",
            380, 45, 30, 15,
            "A massive construct of ancient stone, animated by war-magic long\n" +
            "  since faded. Its fists crack the earth with every step."
        );
        sentinel.setAttackStrategy(new DefensiveStrategy());
        sentinel.addChanceLoot(ItemFactory.create(ItemType.SHADOW_CRYSTAL), 0.13);
        sentinel.addSpecialAttack(new EnemySpecialAttack(
            "Earthquake Stomp",
            "The Sentinel's massive foot cracks the ground — the shockwave staggers you!",
            0.20, 1.6, false, 0, true
        ));
        sentinel.addSpecialAttack(new EnemySpecialAttack(
            "Boulder Fist",
            "The Sentinel's stone fist smashes into you like a falling boulder!",
            0.12, 2.0, false, 0, false
        ));
        return sentinel;
    }

    /**
     * Void Wraith — an aggressive shadow-energy creature haunting the castle's depths.
     * Low defense but hits devastatingly hard; specials bypass armour entirely.
     */
    private static Enemy createVoidWraith() {
        Enemy wraith = new Enemy(
            "Void Wraith",
            280, 65, 10, 20,
            "A formless entity of swirling shadow energy. It has no face — only\n" +
            "  two burning violet eyes that fix on you with terrible hunger."
        );
        wraith.setAttackStrategy(new AggressiveStrategy());
        wraith.addLootItem(ItemFactory.create(ItemType.ELIXIR));
        wraith.addChanceLoot(ItemFactory.create(ItemType.SHADOW_CRYSTAL), 0.15);
        wraith.addSpecialAttack(new EnemySpecialAttack(
            "Void Drain",
            "The Wraith phases through your armour, siphoning your very life force!",
            0.25, 1.3, true, 35, false
        ));
        wraith.addSpecialAttack(new EnemySpecialAttack(
            "Shadow Phase",
            "The Wraith becomes intangible and strikes directly at your soul!",
            0.15, 2.0, true, 0, false
        ));
        return wraith;
    }

    /**
     * Tree Protector (enemy form) — the guardian of the Mystic Glade when the
     * player chooses to fight instead of befriend.
     * Ancient but not a warrior — drops the Forest Gem.
     */
    private static Enemy createTreeProtectorEnemy() {
        Enemy protector = new Enemy(
            "Tree Protector",
            120, 20, 8, 0,
            "A figure woven from living bark and ancient roots. He did not choose\n" +
            "  this fight — but he will not flee from it."
        );
        protector.setAttackStrategy(new RandomStrategy());
        protector.addLootItem(ItemFactory.create(ItemType.FOREST_GEM));
        protector.addSpecialAttack(new EnemySpecialAttack(
            "Thorn Lash",
            "Root-thorns erupt around the Protector, lashing out at you!",
            0.20, 1.5, false, 0, false
        ));
        return protector;
    }

    // -------------------------------------------------------------------------
    // Divine realm enemies
    // -------------------------------------------------------------------------

    /**
     * Corrupted Paladin — a fallen holy soldier, heavily armoured.
     * High defense and punishing holy specials.
     */
    private static Enemy createCorruptedPaladin() {
        Enemy paladin = new Enemy(
            "Corrupted Paladin",
            360, 65, 28, 22,
            "A knight clad in once-gleaming armour now cracked with dark fissures.\n" +
            "  A divine sigil on their breastplate is crossed out — consumed by shadow."
        );
        paladin.setAttackStrategy(new DefensiveStrategy());
        paladin.addChanceLoot(ItemFactory.create(ItemType.CORRUPTED_MACE), 0.12);
        paladin.addChanceLoot(ItemFactory.create(ItemType.DIVINE_CRYSTAL),  0.09);
        paladin.addSpecialAttack(new EnemySpecialAttack(
            "Holy Smite",
            "The Paladin channels corrupted holy energy into a devastating smite!",
            0.20, 1.7, true, 0, false
        ));
        paladin.addSpecialAttack(new EnemySpecialAttack(
            "Dark Consecration",
            "Dark sigils glow on the Paladin's armour as it drains your vitality!",
            0.15, 1.1, false, 50, false
        ));
        return paladin;
    }

    /**
     * Radiant Stalker — a fast, lightly armoured hunter of the divine realm.
     * Aggressive and unpredictable; dangerous at any gear level.
     */
    private static Enemy createRadiantStalker() {
        Enemy stalker = new Enemy(
            "Radiant Stalker",
            300, 75, 18, 20,
            "A lithe figure wrapped in tattered white robes, moving with unsettling\n" +
            "  speed. Its eyes burn gold — something divine still lingers in it."
        );
        stalker.setAttackStrategy(new AggressiveStrategy());
        stalker.addChanceLoot(ItemFactory.create(ItemType.DIVINE_CRYSTAL), 0.08);
        stalker.addSpecialAttack(new EnemySpecialAttack(
            "Rapid Assault",
            "The Stalker strikes in a blur of golden light — too fast to track!",
            0.25, 1.5, false, 0, false
        ));
        stalker.addSpecialAttack(new EnemySpecialAttack(
            "Lunge",
            "The Stalker lunges past your guard with terrifying precision!",
            0.15, 2.2, true, 0, false
        ));
        return stalker;
    }

    /**
     * Celestial Sentinel — the divine equivalent of the Stone Sentinel.
     * Enormous HP and defense; can partially restore itself mid-fight.
     */
    private static Enemy createCelestialSentinel() {
        Enemy sentinel = new Enemy(
            "Celestial Sentinel",
            520, 60, 38, 32,
            "A towering construct of divine stone and golden runes, animated by the\n" +
            "  last embers of celestial war-magic. Each step shakes the ground."
        );
        sentinel.setAttackStrategy(new DefensiveStrategy());
        sentinel.addChanceLoot(ItemFactory.create(ItemType.DIVINE_CRYSTAL), 0.10);
        sentinel.addSpecialAttack(new EnemySpecialAttack(
            "Divine Slam",
            "The Sentinel's celestial fist drives you into the ground!",
            0.20, 1.8, false, 0, true
        ));
        sentinel.addSpecialAttack(new EnemySpecialAttack(
            "Celestial Renewal",
            "The Sentinel draws on celestial energy, partially restoring its form!",
            0.15, 0.5, false, 80, false
        ));
        return sentinel;
    }

    /**
     * Fallen Seraph — the most dangerous regular enemy in the divine realm.
     * Aggressive, winged, and capable of stunning blows and armour-ignoring strikes.
     */
    private static Enemy createFallenSeraph() {
        Enemy seraph = new Enemy(
            "Fallen Seraph",
            440, 82, 22, 38,
            "A winged figure, its feathers the colour of ash and dried blood.\n" +
            "  It moves in bursts of terrifying speed, golden eyes devoid of mercy."
        );
        seraph.setAttackStrategy(new AggressiveStrategy());
        seraph.addLootItem(ItemFactory.create(ItemType.HEALTH_POTION));
        seraph.addChanceLoot(ItemFactory.create(ItemType.DIVINE_CRYSTAL), 0.09);
        seraph.addSpecialAttack(new EnemySpecialAttack(
            "Wing Blast",
            "The Seraph beats its massive wings with crushing force, stunning you!",
            0.25, 1.5, false, 0, true
        ));
        seraph.addSpecialAttack(new EnemySpecialAttack(
            "Divine Strike",
            "The Seraph channels the last of its divinity into one devastating blow!",
            0.15, 2.5, true, 0, false
        ));
        return seraph;
    }

    /**
     * The Arbiter — the divine realm's supreme guardian and optional super-boss.
     * SubBoss: defeating him does not end the game, but he uses the full Boss
     * phase-2 system. Phase 2 triggers at ≤50% HP with full regen + stat surge.
     * Drops the Seraphic Blade and a guaranteed Divine Crystal.
     */
    private static SubBoss createTheArbiter() {
        SubBoss arbiter = new SubBoss(
            "The Arbiter",
            1200, 78, 35, 0,
            "A towering figure of celestial armour and blinding light. Once the\n" +
            "  supreme judge of the divine order — now lost to grief and madness.\n" +
            "  Two enormous wings of shattered light frame him like a broken halo.\n" +
            "  His voice resonates with a power that shakes the sanctum walls."
        );
        arbiter.setAttackStrategy(new AggressiveStrategy());
        arbiter.addLootItem(ItemFactory.create(ItemType.SERAPHIC_BLADE));
        arbiter.addLootItem(ItemFactory.create(ItemType.DIVINE_CRYSTAL));
        arbiter.setPhase2Boosts(20, 10);
        arbiter.addSpecialAttack(new EnemySpecialAttack(
            "Divine Judgment",
            "The Arbiter renders final judgment — your defences mean nothing before it!",
            0.25, 2.0, true, 0, false
        ));
        arbiter.addSpecialAttack(new EnemySpecialAttack(
            "Celestial Smite",
            "The Arbiter's celestial hammer drives you to your knees in holy fire!",
            0.20, 1.6, false, 0, true
        ));
        arbiter.addSpecialAttack(new EnemySpecialAttack(
            "Divine Retribution",
            "The Arbiter channels your pain into divine restoration!",
            0.15, 1.5, false, 70, false
        ));
        return arbiter;
    }
}
