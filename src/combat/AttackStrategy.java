package combat;

import characters.Character;

/**
 * AttackStrategy — Strategy pattern interface for enemy combat behaviour.
 *
 * Defines the algorithm contract: given an attacker and a target, calculate
 * how much damage is dealt.  Each concrete strategy encapsulates a different
 * fighting style, so enemies can behave completely differently with no
 * conditional logic inside CombatSystem.
 *
 * Pattern: Strategy (behavioural)
 * Context : CombatSystem holds a strategy reference for the current enemy.
 * Concrete: AggressiveStrategy, DefensiveStrategy, RandomStrategy.
 *
 * WHY THIS PATTERN:
 *   Without it, CombatSystem would need an if/else chain keyed on enemy type
 *   for every combat calculation.  Adding a new enemy type would require
 *   editing CombatSystem.  With Strategy, adding a new behaviour means
 *   creating a new class — existing code is never touched (Open/Closed
 *   Principle).
 */
public interface AttackStrategy {

    /**
     * Calculates the damage this strategy deals from attacker to target.
     * Implementations may print flavour text (e.g. "Heavy strike!") as a
     * side effect so the player sees why the damage value varies.
     *
     * @param attacker the character performing the attack
     * @param target   the character receiving the attack
     * @return net damage to apply (always >= 1)
     */
    int calculateDamage(Character attacker, Character target);

    /** Short name shown in combat logs (e.g. "Aggressive", "Defensive"). */
    String getAttackName();
}
