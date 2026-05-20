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
 *
 * AI NOTE:
 *   The structure of this factory was generated with the assistance of
 *   Claude Code (Anthropic) and then reviewed and adapted for this project.
 *   See REPORT.md — Section 2 for full details.
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
            case IRON_SWORD:     return createIronSword();
            case SHADOW_BLADE:   return createShadowBlade();
            case LEATHER_ARMOUR: return createLeatherArmour();
            case HEALTH_POTION:  return createHealthPotion();
            case ANCIENT_KEY:    return createAncientKey();
            case ANCIENT_RELIC:  return createAncientRelic();
            default:
                throw new IllegalArgumentException("Unknown item type: " + type);
        }
    }

    // -------------------------------------------------------------------------
    // Private creation methods — one per item type
    // -------------------------------------------------------------------------

    /**
     * Iron Sword — the standard weapon, sold by the Merchant.
     * A solid early upgrade that makes a meaningful difference against goblins.
     */
    private static Weapon createIronSword() {
        return new Weapon(
            "Iron Sword",
            "A well-balanced iron sword with a keen edge. Reliable, if unremarkable.",
            15,   // gold value (buy price)
            8     // attack bonus
        );
    }

    /**
     * Shadow Blade — the advanced weapon, rewarded by the dungeon puzzle.
     * Cannot be sold (value 0) — it is a dungeon treasure, not a trade good.
     */
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
     * Leather Armour — the standard armour, sold by the Merchant.
     */
    private static Armour createLeatherArmour() {
        return new Armour(
            "Leather Armour",
            "Sturdy tanned leather armour — not glamorous, but it keeps you alive.",
            20,   // gold value
            3     // defense bonus
        );
    }

    /**
     * Health Potion — the only consumable in the game.
     * Available from the Merchant and dropped by the Dark Knight.
     */
    private static Potion createHealthPotion() {
        return new Potion(
            "Health Potion",
            "A small flask of glowing red liquid. Restores 40 HP when consumed.",
            10,   // gold value
            40    // heal amount
        );
    }

    /**
     * Ancient Key — unlocks the iron door between the Underground Dungeon and
     * the Corrupted Castle.  Awarded on solving the Ancient Ruins riddle.
     * No sell value — it serves a single plot-critical purpose.
     */
    private static Key createAncientKey() {
        return new Key(
            "Ancient Key",
            "An ornate iron key engraved with faintly glowing runes. It radiates a\n" +
            "  faint warmth, as though it remembers what it was made to protect."
        );
    }

    /**
     * Ancient Relic — the win-condition item, dropped by the Shadow Lord.
     * A QuestItem: no sell value, use() displays the victory lore text.
     */
    private static QuestItem createAncientRelic() {
        return new QuestItem(
            "Ancient Relic",
            "A crystalline orb that pulses with warm golden light. This is what you came for.",
            "The Ancient Relic — a crystalline orb of boundless power. The Shadow Lord " +
            "shattered it to plunge the kingdom into darkness. Holding it now, you feel " +
            "its light pushing back the shadows. The kingdom can be saved."
        );
    }
}
