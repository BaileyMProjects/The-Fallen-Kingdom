package characters;

/**
 * EnemyType — identifies every distinct enemy kind in the game.
 *
 * Used as the key in EnemyFactory.create() so all enemy instantiation is
 * centralised in the factory rather than scattered as direct 'new' calls.
 */
public enum EnemyType {
    // Original enemies
    SHADOW_GOBLIN,
    DARK_KNIGHT,
    SHADOW_LORD,

    // Expanded roster
    PLAGUE_RAT,         // weak swarm enemy found in the forest and battlefield
    STONE_SENTINEL,     // heavy defensive guardian on the Forgotten Battlefield
    VOID_WRAITH,        // aggressive shadow-energy creature in the shadow domain
    TREE_PROTECTOR_ENEMY // the Tree Protector when the player chooses to fight
}
