package characters;

/**
 * Boss — a uniquely powerful Enemy that can trigger the victory sequence on defeat
 * and optionally runs a two-phase fight.
 *
 * Phase system:
 *   CombatSystem calls checkPhaseTransition() after each player attack.
 *   If HP has dropped to ≤50% and the phase hasn't triggered yet, the boss
 *   fully regenerates to max HP and receives a stat boost — once only.
 *   Phase 2 bosses are also immune to stun.
 *
 *   setPhase2Boosts() must be called by EnemyFactory to activate phases.
 *   Bosses without that call behave exactly as before (no phase).
 */
public class Boss extends Enemy {

    private boolean phaseTransitioned  = false;
    private int     phase2AttackBoost  = 0;
    private int     phase2DefenseBoost = 0;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Boss(String name, int maxHealth, int attackPower, int defense,
                int goldDrop, String description) {
        super(name, maxHealth, attackPower, defense, goldDrop, description);
    }

    // -------------------------------------------------------------------------
    // Phase configuration (called by EnemyFactory for multi-phase bosses)
    // -------------------------------------------------------------------------

    /**
     * Activates the two-phase system for this boss.
     * Without this call the boss behaves as a standard single-phase enemy.
     */
    public void setPhase2Boosts(int attackBoost, int defenseBoost) {
        this.phase2AttackBoost  = attackBoost;
        this.phase2DefenseBoost = defenseBoost;
    }

    // -------------------------------------------------------------------------
    // Phase transition (called by CombatSystem after each player attack)
    // -------------------------------------------------------------------------

    /**
     * Checks whether the phase 2 transition should fire.
     *
     * Conditions: HP ≤ 50% of max, phase not yet transitioned, phase boosts set.
     * Effect on trigger: HP fully restored, attack and defense boosted, flag set.
     *
     * @return a non-null transition message if the transition just fired, null otherwise
     */
    public String checkPhaseTransition() {
        if (phaseTransitioned)                              return null;
        if (phase2AttackBoost == 0 && phase2DefenseBoost == 0) return null;
        if (getHealth() > getMaxHealth() / 2)              return null;

        phaseTransitioned = true;
        setHealth(getMaxHealth());
        setAttackPower(getAttackPower() + phase2AttackBoost);
        setDefense(getDefense() + phase2DefenseBoost);

        return getName() + " has entered Phase 2!";
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public boolean isPhaseTransitioned() { return phaseTransitioned; }

    /** Phase 2 bosses shrug off stuns — checked by CombatSystem. */
    public boolean isImmuneToStun() { return phaseTransitioned; }

    /** Always returns true — signals Game.handleAttack() to display the victory screen. */
    @Override
    public boolean isBoss() { return true; }
}
