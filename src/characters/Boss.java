package characters;

/**
 * Boss — a uniquely powerful Enemy that triggers the victory sequence on defeat.
 *
 * Overrides isBoss() to return true, which Game.handleAttack() checks after
 * combat to decide whether to display the victory screen.
 *
 * Extending Enemy rather than duplicating logic respects the Liskov
 * Substitution Principle: a Boss can be used wherever an Enemy is expected,
 * and any code that works with Enemy objects automatically works with Boss.
 */
public class Boss extends Enemy {

    public Boss(String name, int maxHealth, int attackPower, int defense,
                int goldDrop, String description) {
        super(name, maxHealth, attackPower, defense, goldDrop, description);
    }

    /** Always returns true — signals Game that defeating this enemy wins the game. */
    @Override
    public boolean isBoss() {
        return true;
    }
}
