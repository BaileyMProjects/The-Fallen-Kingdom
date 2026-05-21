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
    STEEL_SWORD,
    SHADOW_BLADE,
    BATTLE_AXE,

    // Armour — head
    IRON_HELMET,

    // Armour — torso
    LEATHER_CHESTPLATE,
    CHAINMAIL_VEST,
    SHADOW_ROBE,

    // Armour — legs
    LEATHER_GREAVES,
    EVASIVE_BOOTS,

    // Consumables
    HEALTH_POTION,
    ELIXIR,
    GREATER_ELIXIR,

    // Keys
    ANCIENT_KEY,

    // Quest items
    ANCIENT_RELIC,
    FOREST_GEM,

    // Crafting materials
    SHADOW_CRYSTAL
}
