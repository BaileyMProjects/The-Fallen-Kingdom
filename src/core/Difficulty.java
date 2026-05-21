package core;

/**
 * Difficulty — the three selectable challenge levels for The Fallen Kingdom.
 *
 * Each constant packages the starting player stats and the multiplier applied
 * to every point of damage enemies deal, so the rest of the game never needs
 * a switch statement on difficulty mode.
 */
public enum Difficulty {

    //                label    description                                    hp   atk  def  gold  dmgMul  goldMul  enchCost  respawnCost  divineEnchCost
    EASY  ("Easy",   "Generous HP and starting gold. Great for new players.", 150,  12,  8,   50,  0.75,   1.00,    30,       20,          120),
    MEDIUM("Medium", "The intended experience. Balanced challenge.",           100,  10,  5,    0,  1.00,   0.80,    50,       15,          170),
    HARD  ("Hard",   "Low HP, no starting gold. For seasoned adventurers.",    70,   8,  3,    0,  1.30,   0.60,    75,       12,          250);

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    public final String label;
    public final String description;
    public final int    startHp;
    public final int    startAttack;
    public final int    startDefense;
    public final int    startGold;
    /** Multiplier applied to every point of damage enemies deal. */
    public final double enemyDamageMultiplier;
    /** Multiplier applied to all gold earned from enemies (lower on harder difficulties). */
    public final double goldDropMultiplier;
    /** Gold cost to enchant an item at an Enchanter NPC. */
    public final int    enchantCost;
    /** Gold cost to respawn a wave of enemies in the Proving Grounds. */
    public final int    arenaRespawnCost;
    /** Gold cost to enchant an item at Luminara the divine enchanter. */
    public final int    divineEnchantCost;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    Difficulty(String label, String description,
               int hp, int atk, int def, int gold,
               double dmgMul, double goldMul, int enchCost, int respawnCost, int divineEnchCost) {
        this.label                = label;
        this.description          = description;
        this.startHp              = hp;
        this.startAttack          = atk;
        this.startDefense         = def;
        this.startGold            = gold;
        this.enemyDamageMultiplier = dmgMul;
        this.goldDropMultiplier   = goldMul;
        this.enchantCost          = enchCost;
        this.arenaRespawnCost     = respawnCost;
        this.divineEnchantCost    = divineEnchCost;
    }
}
