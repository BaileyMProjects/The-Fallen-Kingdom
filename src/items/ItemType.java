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
    SHADOW_CRYSTAL,
    DIVINE_CRYSTAL,      // required for divine enchanting at Luminara

    // Divine realm — weapons
    HOLY_SPEAR,          // +18 atk, sold at Divine Forge (level 6+)
    AUREATE_SWORD,       // +20 atk, sold at Divine Forge (level 6+)
    CORRUPTED_MACE,      // +17 atk, dropped by Corrupted Paladin
    SERAPHIC_BLADE,      // +25 atk, Arbiter boss drop — best weapon

    // Divine realm — armour (head)
    AUREATE_VISOR,       // +5 def, sold at Divine Forge
    SERAPH_CROWN,        // +9 def, Vault of the Fallen loot

    // Divine realm — armour (torso)
    BLESSED_HAUBERK,     // +7 def, sold at Divine Forge
    SANCTUM_PLATE,       // +12 def, Vault of the Fallen loot

    // Divine realm — armour (legs)
    SACRED_GREAVES,      // +6 def +5% miss, sold at Divine Forge
    SERAPH_TASSETS,      // +9 def, Vault of the Fallen loot

    // Divine realm — consumables
    DIVINE_TONIC,        // +160 HP, sold at Divine Forge

    // Combat potions — usable mid-fight, spread across both maps
    VENOM_FLASK,         // poisons enemy: 6 dmg/turn for 4 turns
    BLINDING_POWDER,     // enemy miss +30% for 2 turns
    BATTLE_TONIC,        // player +10 attack for 3 turns
    STONE_SKIN_DRAUGHT,  // player +8 defense for 3 turns
    SMOKE_BOMB           // enemy skips their next attack
}
