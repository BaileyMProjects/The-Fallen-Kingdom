package items;

/**
 * ItemFactory — Factory design pattern for item creation (creational).
 *
 * Mirrors EnemyFactory: every item in the game is created here, so item
 * definitions (name, stats, description, gold value) are maintained in one
 * place rather than duplicated across World, Puzzle, and Enemy loot tables.
 *
 * WHY FACTORY HERE:
 *   Items appear in multiple contexts: the Merchant sells them, puzzles
 *   reward them, and enemies drop them.  If each caller used 'new Weapon(...)'
 *   directly, changing a stat (e.g., boosting the Shadow Blade's attack bonus
 *   from 15 to 18) would require finding and editing every occurrence.
 *   With ItemFactory, it's a one-line change.
 *
 * HOW TO EXTEND:
 *   1. Add a constant to ItemType.
 *   2. Add a private create method here.
 *   3. Add a case to the switch.
 *   No caller changes required.
 */
public class ItemFactory {

    /** Utility class — prevent instantiation. */
    private ItemFactory() {}

    // -------------------------------------------------------------------------
    // Factory method
    // -------------------------------------------------------------------------

    /**
     * Creates a new item of the given type.
     *
     * @param type the kind of item to create
     * @return a new Item instance fully configured and ready to use
     * @throws IllegalArgumentException if the type is unrecognised
     */
    public static Item create(ItemType type) {
        switch (type) {
            // Weapons
            case IRON_SWORD:     return createIronSword();
            case SHADOW_BLADE:   return createShadowBlade();
            case BATTLE_AXE:     return createBattleAxe();

            // Armour
            case LEATHER_ARMOUR: return createLeatherArmour();
            case SHADOW_ROBE:    return createShadowRobe();
            case EVASIVE_BOOTS:  return createEvasiveBoots();

            // Consumables
            case HEALTH_POTION:  return createHealthPotion();
            case ELIXIR:         return createElixir();

            // Keys
            case ANCIENT_KEY:    return createAncientKey();

            // Quest / special items
            case ANCIENT_RELIC:  return createAncientRelic();
            case FOREST_GEM:     return createForestGem();

            default:
                throw new IllegalArgumentException("Unknown item type: " + type);
        }
    }

    // -------------------------------------------------------------------------
    // Weapons
    // -------------------------------------------------------------------------

    private static Weapon createIronSword() {
        return new Weapon(
            "Iron Sword",
            "A well-balanced iron sword with a keen edge. Reliable, if unremarkable.",
            15,   // gold value (buy price)
            8     // attack bonus
        );
    }

    private static Weapon createShadowBlade() {
        return new Weapon(
            "Shadow Blade",
            "A blade forged from crystallised shadow energy. It hums with dark power\n" +
            "  and feels unnaturally light in your hand.",
            0,    // not for sale
            15    // attack bonus
        );
    }

    /**
     * Battle Axe — a heavy two-handed weapon found on the Forgotten Battlefield.
     * Higher attack than the Iron Sword but slower feel (lore-only difference).
     */
    private static Weapon createBattleAxe() {
        return new Weapon(
            "Battle Axe",
            "A weathered battle axe salvaged from the ruined battlefield. Heavy and\n" +
            "  brutal — each swing carries the weight of a forgotten war.",
            18,   // sell value
            12    // attack bonus
        );
    }

    // -------------------------------------------------------------------------
    // Armour
    // -------------------------------------------------------------------------

    private static Armour createLeatherArmour() {
        return new Armour(
            "Leather Armour",
            "Sturdy tanned leather armour — not glamorous, but it keeps you alive.",
            20,   // gold value
            3     // defense bonus
        );
    }

    /**
     * Shadow Robe — found in the Shadow Barracks.
     * Superior defense compared to Leather Armour; imbued with faint shadow energy.
     */
    private static Armour createShadowRobe() {
        return new Armour(
            "Shadow Robe",
            "A robe woven from compressed shadow energy. Cold to the touch but\n" +
            "  surprisingly resilient — the darkness itself deflects blows.",
            0,    // treasure, not for sale
            6     // defense bonus
        );
    }

    /**
     * Evasive Boots — gifted by the Tree Protector on befriending.
     * Grants +1 defense and adds 20% to the enemy miss-chance when worn.
     */
    private static Armour createEvasiveBoots() {
        return new Armour(
            "Evasive Boots",
            "Boots woven from ancient root-fibre and living wind. Light as a leaf,\n" +
            "  they shift your weight just enough to make enemies swing wide.",
            0,    // gift, not for sale
            1,    // defense bonus
            0.20  // +20% enemy miss chance
        );
    }

    // -------------------------------------------------------------------------
    // Consumables
    // -------------------------------------------------------------------------

    private static Potion createHealthPotion() {
        return new Potion(
            "Health Potion",
            "A small flask of glowing red liquid. Restores 40 HP when consumed.",
            10,   // gold value
            40    // heal amount
        );
    }

    /**
     * Elixir — a stronger restorative found deep in the shadow domain.
     * Sold by Griswold and rewarded by the Cursed Archives puzzle.
     */
    private static Potion createElixir() {
        return new Potion(
            "Elixir",
            "A luminous silver vial that radiates warmth. Restores 70 HP when consumed.",
            20,   // gold value
            70    // heal amount
        );
    }

    // -------------------------------------------------------------------------
    // Keys
    // -------------------------------------------------------------------------

    private static Key createAncientKey() {
        return new Key(
            "Ancient Key",
            "An ornate iron key engraved with faintly glowing runes. It radiates a\n" +
            "  faint warmth, as though it remembers what it was made to protect."
        );
    }

    // -------------------------------------------------------------------------
    // Quest / special items
    // -------------------------------------------------------------------------

    private static QuestItem createAncientRelic() {
        return new QuestItem(
            "Ancient Relic",
            "A crystalline orb that pulses with warm golden light. This is what you came for.",
            "The Ancient Relic — a crystalline orb of boundless power. The Shadow Lord " +
            "shattered it to plunge the kingdom into darkness. Holding it now, you feel " +
            "its light pushing back the shadows. The kingdom can be saved."
        );
    }

    /**
     * Forest Gem — a rare gem nestled inside the Tree Protector's sacred oak.
     * Has no buy price (unobtainable from shops) but triggers special merchant
     * dialogue and a gold reward of 50–100 when sold to Griswold.
     */
    private static QuestItem createForestGem() {
        return new QuestItem(
            "Forest Gem",
            "A flawless gem that pulses with soft emerald light. It is warm to the\n" +
            "  touch, as though the ancient grove still lives within it.",
            "A Forest Gem — extraordinarily rare, born from centuries of grove magic.\n" +
            "  Merchants would pay handsomely for something like this."
        );
    }
}
