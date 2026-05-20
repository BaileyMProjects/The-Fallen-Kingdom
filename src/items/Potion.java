package items;

import characters.Player;

/**
 * Potion — a consumable item that restores the player's health.
 *
 * Unlike Weapon and Armour, use() actually consumes the item by removing it
 * from the player's inventory after healing.  The actual amount restored may
 * be less than healAmount if the player is near full health — Character.heal()
 * handles the clamping and returns the true amount.
 */
public class Potion extends Item {

    private final int healAmount;

    public Potion(String name, String description, int value, int healAmount) {
        super(name, description, value);
        this.healAmount = healAmount;
    }

    /**
     * Heals the player, then removes this potion from their inventory.
     * The item is always consumed even if the player was already at full HP.
     */
    @Override
    public void use(Player player) {
        int restored = player.heal(healAmount);
        player.getInventory().removeItem(this);
        if (restored > 0) {
            System.out.println("You drink the " + getName() + " and restore " + restored + " HP.");
        } else {
            System.out.println("You drink the " + getName() + " but your health is already full.");
        }
        System.out.println("Health: " + player.getHealth() + "/" + player.getMaxHealth());
    }

    public int getHealAmount() { return healAmount; }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [restores " + healAmount + " HP]";
    }
}
