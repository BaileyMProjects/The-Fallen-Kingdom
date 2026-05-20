package items;

/**
 * ItemType — identifies every concrete item the game can create.
 *
 * Used as the key for ItemFactory.create() so all item instantiation is
 * centralised in the factory rather than scattered as direct 'new' calls.
 * Each constant maps to one specific item definition inside the factory.
 */
public enum ItemType {
    // Weapons
    IRON_SWORD,
    SHADOW_BLADE,

    // Armour
    LEATHER_ARMOUR,

    // Consumables
    HEALTH_POTION,

    // Keys
    ANCIENT_KEY,

    // Quest items
    ANCIENT_RELIC
}
