package items;

import characters.Player;

/**
 * Weapon — an equippable item that increases the player's attack power.
 *
 * Calling use() delegates to Player.equip(), which handles swapping out a
 * previously equipped weapon and adjusting the attack stat.  The weapon itself
 * stays simple: it just knows its attack bonus.
 */
public class Weapon extends Item {

    private final int attackBonus;

    public Weapon(String name, String description, int value, int attackBonus) {
        super(name, description, value);
        this.attackBonus = attackBonus;
    }

    /**
     * Equips this weapon on the player.
     * Delegates to Player.equip() so that stat adjustment logic lives in one place.
     */
    @Override
    public void use(Player player) {
        player.equip(this);
    }

    public int getAttackBonus() { return attackBonus; }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [+" + attackBonus + " attack]";
    }
}
