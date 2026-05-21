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
    BATTLE_AXE,

    // Armour
    LEATHER_ARMOUR,
    SHADOW_ROBE,
    EVASIVE_BOOTS,

    // Consumables
    HEALTH_POTION,
    ELIXIR,

    // Keys
    ANCIENT_KEY,

    // Quest items
    ANCIENT_RELIC,
    FOREST_GEM
}
