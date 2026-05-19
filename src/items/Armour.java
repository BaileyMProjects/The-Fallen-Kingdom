package items;

import characters.Player;

/**
 * Armour — an equippable item that increases the player's defense rating.
 *
 * Mirrors Weapon in structure.  use() delegates to Player.equip() so the
 * stat swap logic stays inside Player and Armour stays a simple data holder.
 */
public class Armour extends Item {

    private final int defenseBonus;

    public Armour(String name, String description, int value, int defenseBonus) {
        super(name, description, value);
        this.defenseBonus = defenseBonus;
    }

    /**
     * Equips this armour on the player.
     */
    @Override
    public void use(Player player) {
        player.equip(this);
    }

    public int getDefenseBonus() { return defenseBonus; }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [+" + defenseBonus + " defense]";
    }
}
