package characters;

/**
 * EnemyType — identifies every distinct enemy kind in the game.
 *
 * Used as the key in EnemyFactory.create() so all enemy instantiation
 * is centralised in the factory rather than scattered as direct 'new' calls.
 */
public enum EnemyType {
    SHADOW_GOBLIN,
    DARK_KNIGHT,
    SHADOW_LORD
}
