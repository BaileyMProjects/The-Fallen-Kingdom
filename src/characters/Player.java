package characters;

import items.Armour;
import items.Inventory;
import items.Item;
import items.Weapon;
import world.LocationId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    private Armour equippedHead;
    private Armour equippedTorso;
    private Armour equippedLegs;

    // Per-combat buff state — set by CombatPotion.use(), cleared by CombatSystem
    private int     combatAttackBonus;
    private int     combatAttackBonusTurns;
    private int     combatDefenseBonus;
    private int     combatDefenseBonusTurns;

    // Pending enemy debuffs — written by CombatPotion.use(), read+cleared by CombatSystem
    private int     pendingEnemyPoisonDamage;
    private int     pendingEnemyPoisonTurns;
    private int     pendingEnemyBlindMissBonus;  // as a percentage e.g. 30 = 30%
    private int     pendingEnemyBlindTurns;
    private boolean pendingEnemyStun;

    private Set<LocationId> visitedLocations = new HashSet<>();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Player(String name, int maxHealth, int attackPower, int defense, int startingGold) {
        super(name, maxHealth, attackPower, defense);
        this.inventory             = new Inventory();
        this.gold                  = startingGold;
        this.level                 = 1;
        this.experience            = 0;
        this.experienceToNextLevel = 25;
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
            Armour current = getEquippedInSlot(armour.getSlot());
            if (current != null) {
                setDefense(getDefense() - current.getDefenseBonus());
                System.out.println("You unequip " + current.getName() + ".");
            }
            setEquippedInSlot(armour);
            setDefense(getDefense() + armour.getDefenseBonus());
            System.out.println("You equip " + armour.getName()
                    + "  [" + slotLabel(armour.getSlot()) + "]"
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
        experienceToNextLevel = (int)(experienceToNextLevel * 1.4);
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
    // Combat buff / debuff state (managed by CombatSystem each turn)
    // -------------------------------------------------------------------------

    /** Clears all per-combat state. Called by CombatSystem at the start of every fight. */
    public void clearCombatState() {
        combatAttackBonus        = 0;  combatAttackBonusTurns  = 0;
        combatDefenseBonus       = 0;  combatDefenseBonusTurns = 0;
        pendingEnemyPoisonDamage = 0;  pendingEnemyPoisonTurns = 0;
        pendingEnemyBlindMissBonus = 0; pendingEnemyBlindTurns = 0;
        pendingEnemyStun         = false;
    }

    /** Called by CombatPotion(BUFF_ATTACK). Overwrites any existing attack buff. */
    public void addCombatAttackBonus(int amount, int turns) {
        combatAttackBonus      = amount;
        combatAttackBonusTurns = turns;
    }

    /** Called by CombatPotion(BUFF_DEFENSE). Overwrites any existing defense buff. */
    public void addCombatDefenseBonus(int amount, int turns) {
        combatDefenseBonus      = amount;
        combatDefenseBonusTurns = turns;
    }

    /** Called by CombatPotion(DEBUFF_POISON). Overwrites any existing pending poison. */
    public void setPendingEnemyPoison(int damage, int turns) {
        pendingEnemyPoisonDamage = damage;
        pendingEnemyPoisonTurns  = turns;
    }

    /** Called by CombatPotion(DEBUFF_BLIND). missBonus is a percentage (e.g. 30 = 30%). */
    public void setPendingEnemyBlind(int missBonus, int turns) {
        pendingEnemyBlindMissBonus = missBonus;
        pendingEnemyBlindTurns     = turns;
    }

    /** Called by CombatPotion(DEBUFF_STUN). Enemy skips their next attack. */
    public void setPendingEnemyStun() { pendingEnemyStun = true; }

    /** Returns active attack bonus, 0 if no turns remain. */
    public int getCombatAttackBonus()  { return combatAttackBonusTurns  > 0 ? combatAttackBonus  : 0; }

    /** Returns active defense bonus, 0 if no turns remain. */
    public int getCombatDefenseBonus() { return combatDefenseBonusTurns > 0 ? combatDefenseBonus : 0; }

    /** Reads and clears pending poison (so it's applied once by CombatSystem). */
    public int drainPendingPoisonDamage() { int v = pendingEnemyPoisonDamage; pendingEnemyPoisonDamage = 0; return v; }
    public int drainPendingPoisonTurns()  { int v = pendingEnemyPoisonTurns;  pendingEnemyPoisonTurns  = 0; return v; }

    /** Returns remaining blind turns (so CombatSystem can add miss bonus each turn). */
    public int getPendingEnemyBlindMissBonus() { return pendingEnemyBlindTurns > 0 ? pendingEnemyBlindMissBonus : 0; }
    public int getPendingEnemyBlindTurns()     { return pendingEnemyBlindTurns; }

    /** Reads and clears stun flag. */
    public boolean drainPendingEnemyStun() { boolean v = pendingEnemyStun; pendingEnemyStun = false; return v; }

    /**
     * Called by CombatSystem at the end of each full turn to count down buff/debuff durations.
     * Prints expiry messages so the player knows when effects run out.
     */
    public void decrementCombatBuffs() {
        if (combatAttackBonusTurns > 0) {
            combatAttackBonusTurns--;
            if (combatAttackBonusTurns == 0)
                System.out.println("  The attack boost from your Battle Tonic has faded.");
        }
        if (combatDefenseBonusTurns > 0) {
            combatDefenseBonusTurns--;
            if (combatDefenseBonusTurns == 0)
                System.out.println("  The stone-skin effect has worn off.");
        }
        if (pendingEnemyBlindTurns > 0) {
            pendingEnemyBlindTurns--;
        }
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
             + "  Head:    " + (equippedHead  != null ? equippedHead.getName()  : "none") + "\n"
             + "  Torso:   " + (equippedTorso != null ? equippedTorso.getName() : "none") + "\n"
             + "  Legs:    " + (equippedLegs  != null ? equippedLegs.getName()  : "none") + "\n"
             + "-----------------------";
    }

    @Override
    public String getDescription() {
        return "You are " + getName() + ", a brave adventurer. Level " + level + ".";
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Inventory getInventory()      { return inventory; }
    public int       getGold()           { return gold; }
    public int       getLevel()          { return level; }
    public int       getExperience()     { return experience; }
    public Weapon    getEquippedWeapon() { return equippedWeapon; }
    public Armour    getEquippedHead()   { return equippedHead; }
    public Armour    getEquippedTorso()  { return equippedTorso; }
    public Armour    getEquippedLegs()   { return equippedLegs; }

    /**
     * Sums evasion bonuses across all equipped armour slots.
     * The Evasive Boots (LEGS) contribute 0.20; all other current pieces contribute 0.0.
     */
    public double getEvasionBonus() {
        double bonus = 0.0;
        if (equippedHead  != null) bonus += equippedHead.getMissBonus();
        if (equippedTorso != null) bonus += equippedTorso.getMissBonus();
        if (equippedLegs  != null) bonus += equippedLegs.getMissBonus();
        return bonus;
    }

    // -------------------------------------------------------------------------
    // Exploration tracking
    // -------------------------------------------------------------------------

    public void markVisited(LocationId id) {
        if (visitedLocations == null) visitedLocations = new HashSet<>();
        visitedLocations.add(id);
    }

    public boolean hasVisited(LocationId id) {
        return visitedLocations != null && visitedLocations.contains(id);
    }

    public Set<LocationId> getVisitedLocations() {
        if (visitedLocations == null) visitedLocations = new HashSet<>();
        return Collections.unmodifiableSet(visitedLocations);
    }

    // -------------------------------------------------------------------------
    // Private slot helpers
    // -------------------------------------------------------------------------

    private Armour getEquippedInSlot(items.ArmourSlot slot) {
        switch (slot) {
            case HEAD:  return equippedHead;
            case TORSO: return equippedTorso;
            case LEGS:  return equippedLegs;
            default:    return null;
        }
    }

    private void setEquippedInSlot(Armour armour) {
        switch (armour.getSlot()) {
            case HEAD:  equippedHead  = armour; break;
            case TORSO: equippedTorso = armour; break;
            case LEGS:  equippedLegs  = armour; break;
        }
    }

    private static String slotLabel(items.ArmourSlot slot) {
        String s = slot.name();
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
