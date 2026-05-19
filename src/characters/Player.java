package characters;

import items.Armour;
import items.Inventory;
import items.Item;
import items.Weapon;

/**
 * Player — the human-controlled character.
 *
 * Extends Character with:
 *   - An Inventory held via composition (has-a relationship)
 *   - Equipment slots for a Weapon and Armour that modify base stats
 *   - A gold counter used for trading with the Merchant
 *   - A simple level/experience progression system
 *
 * Equipping an item adjusts attackPower or defense directly so that the
 * rest of the game (e.g. CombatSystem) does not need to know about equipment.
 */
public class Player extends Character {

    private final Inventory inventory;

    private int gold;
    private int level;
    private int experience;
    private int experienceToNextLevel;

    private Weapon equippedWeapon;
    private Armour equippedArmour;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Player(String name, int maxHealth, int attackPower, int defense, int startingGold) {
        super(name, maxHealth, attackPower, defense);
        this.inventory             = new Inventory();
        this.gold                  = startingGold;
        this.level                 = 1;
        this.experience            = 0;
        this.experienceToNextLevel = 100;
    }

    // -------------------------------------------------------------------------
    // Equipment
    // -------------------------------------------------------------------------

    /**
     * Equips a Weapon or Armour from inventory, applying its stat bonus.
     * If something is already equipped in that slot, its bonus is removed first.
     * Non-equippable items produce an informative error message.
     */
    public void equip(Item item) {
        if (item instanceof Weapon) {
            Weapon weapon = (Weapon) item;
            if (equippedWeapon != null) {
                setAttackPower(getAttackPower() - equippedWeapon.getAttackBonus());
                System.out.println("You unequip " + equippedWeapon.getName() + ".");
            }
            equippedWeapon = weapon;
            setAttackPower(getAttackPower() + weapon.getAttackBonus());
            System.out.println("You equip " + weapon.getName()
                    + "  (+" + weapon.getAttackBonus() + " attack).");

        } else if (item instanceof Armour) {
            Armour armour = (Armour) item;
            if (equippedArmour != null) {
                setDefense(getDefense() - equippedArmour.getDefenseBonus());
                System.out.println("You unequip " + equippedArmour.getName() + ".");
            }
            equippedArmour = armour;
            setDefense(getDefense() + armour.getDefenseBonus());
            System.out.println("You equip " + armour.getName()
                    + "  (+" + armour.getDefenseBonus() + " defense).");

        } else {
            System.out.println("You can't equip " + item.getName() + ".");
        }
    }

    // -------------------------------------------------------------------------
    // Experience and levelling
    // -------------------------------------------------------------------------

    /**
     * Awards experience points and triggers a level-up if the threshold is reached.
     * Multiple level-ups in one call are handled by the while loop.
     */
    public void gainExperience(int amount) {
        experience += amount;
        System.out.println("You gained " + amount + " experience points.");
        while (experience >= experienceToNextLevel) {
            experience -= experienceToNextLevel;
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        int hpGain  = 10;
        int atkGain = 2;
        int defGain = 1;
        setMaxHealth(getMaxHealth() + hpGain);
        setHealth(getMaxHealth());                      // fully heal on level-up
        setAttackPower(getAttackPower() + atkGain);
        setDefense(getDefense() + defGain);
        experienceToNextLevel = (int)(experienceToNextLevel * 1.5);
        System.out.println("*** LEVEL UP! You are now Level " + level + " ***");
        System.out.println("  +" + hpGain + " Max HP   +" + atkGain + " Attack   +" + defGain + " Defense");
    }

    // -------------------------------------------------------------------------
    // Gold
    // -------------------------------------------------------------------------

    public void addGold(int amount) { gold += amount; }

    /**
     * Attempts to spend the given amount of gold.
     * @return true if the player had enough gold (gold is deducted), false otherwise
     */
    public boolean spendGold(int amount) {
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    public String getStatsDisplay() {
        return "\n--- Character Stats ---\n"
             + "  Name:    " + getName() + "\n"
             + "  Level:   " + level + "\n"
             + "  Health:  " + getHealth() + "/" + getMaxHealth() + "\n"
             + "  Attack:  " + getAttackPower() + "\n"
             + "  Defense: " + getDefense() + "\n"
             + "  Exp:     " + experience + "/" + experienceToNextLevel + "\n"
             + "  Gold:    " + gold + "\n"
             + "  Weapon:  " + (equippedWeapon != null ? equippedWeapon.getName() : "none") + "\n"
             + "  Armour:  " + (equippedArmour != null ? equippedArmour.getName() : "none") + "\n"
             + "-----------------------";
    }

    @Override
    public String getDescription() {
        return "You are " + getName() + ", a brave adventurer. Level " + level + ".";
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Inventory getInventory()       { return inventory; }
    public int       getGold()            { return gold; }
    public int       getLevel()           { return level; }
    public int       getExperience()      { return experience; }
    public Weapon    getEquippedWeapon()  { return equippedWeapon; }
    public Armour    getEquippedArmour()  { return equippedArmour; }
}
