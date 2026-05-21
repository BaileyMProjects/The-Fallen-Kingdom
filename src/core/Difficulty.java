package core;

/**
 * Difficulty — the three selectable challenge levels for The Fallen Kingdom.
 *
 * Each constant packages the starting player stats and the multiplier applied
 * to every point of damage enemies deal, so the rest of the game never needs
 * a switch statement on difficulty mode.
 */
public enum Difficulty {

    EASY  ("Easy",
           "Generous HP and starting gold. Great for new players.",
           150, 12, 8, 50, 0.75),

    MEDIUM("Medium",
           "The intended experience. Balanced challenge.",
           100, 10, 5,  0, 1.00),

    HARD  ("Hard",
           "Low HP, no starting gold. For seasoned adventurers only.",
            70,  8, 3,  0, 1.30);

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

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    Difficulty(String label, String description,
               int hp, int atk, int def, int gold, double mul) {
        this.label                = label;
        this.description          = description;
        this.startHp              = hp;
        this.startAttack          = atk;
        this.startDefense         = def;
        this.startGold            = gold;
        this.enemyDamageMultiplier = mul;
    }
}
