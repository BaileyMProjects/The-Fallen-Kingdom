package world;

/**
 * LocationId — unique identifier for every location in the game world.
 *
 * Used as the key in World's location map, avoiding magic strings and
 * making it impossible to reference a location that doesn't exist.
 */
public enum LocationId {
    // Original six locations
    VILLAGE,
    DARK_FOREST,
    ANCIENT_RUINS,
    UNDERGROUND_DUNGEON,
    CORRUPTED_CASTLE,
    SHADOW_THRONE,

    // Expanded world — pre-castle areas
    MYSTIC_GLADE,
    FORGOTTEN_BATTLEFIELD,

    // Expanded world — shadow domain
    SHADOW_BARRACKS,
    CURSED_ARCHIVES,

    // Merchant's Village — second settlement with enchanter and smith
    MERCHANT_VILLAGE,

    // Proving Grounds — arena south of Merchant's Village, mob-respawn farming area
    PROVING_GROUNDS,

    // Divine Realm — second map, accessed north from Forgotten Battlefield
    CELESTIAL_GATE,          // entry point, gatekeeper NPC
    SUNKEN_SHRINE,           // first divine area, moderate difficulty + puzzle
    DIVINE_FORGE,            // safe zone — divine enchanter and first divine merchant
    RADIANT_CATHEDRAL,       // main hall of the fallen celestial order, harder enemies
    CELESTIAL_BARRACKS,      // divine farming arena + second (advanced) divine merchant
    VAULT_OF_THE_FALLEN,     // high-risk treasure vault, best static loot
    SANCTUM_OF_THE_ARBITER  // divine area boss room
}
