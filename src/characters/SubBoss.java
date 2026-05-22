package characters;

/**
 * SubBoss — a powerful boss-class enemy that does NOT trigger the game-victory
 * screen on defeat.
 *
 * Inherits the two-phase system from Boss (call setPhase2Boosts() from
 * EnemyFactory to activate it), but overrides isBoss() to return false so
 * Game.handleAttack() treats it as a regular (if extremely tough) enemy.
 *
 * Used for optional super-bosses like The Arbiter that should be harder than
 * regular enemies but should not end the game when killed.
 */
public class SubBoss extends Boss {

    public SubBoss(String name, int maxHealth, int attackPower, int defense,
                   int goldDrop, String description) {
        super(name, maxHealth, attackPower, defense, goldDrop, description);
    }

    @Override
    public boolean isBoss() { return false; }
}
