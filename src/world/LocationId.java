package world;

/**
 * LocationId — unique identifier for every location in the game world.
 *
 * Used as the key in World's location map, avoiding magic strings and
 * making it impossible to reference a location that doesn't exist.
 */
public enum LocationId {
    VILLAGE,
    DARK_FOREST,
    ANCIENT_RUINS,
    UNDERGROUND_DUNGEON,
    CORRUPTED_CASTLE,
    SHADOW_THRONE
}
