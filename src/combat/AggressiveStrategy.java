package combat;

import characters.Character;

/**
 * AggressiveStrategy — a relentless, high-damage attack style.
 *
 * Multiplies the attacker's power by 1.5 before subtracting the target's
 * defence, making it significantly more dangerous than a normal strike.
 * This strategy is assigned to the Shadow Lord by EnemyFactory, reflecting
 * his overwhelming power.
 *
 * Trade-off: pure offence with no defensive bonus.
 */
public class AggressiveStrategy implements AttackStrategy {

    @Override
    public int calculateDamage(Character attacker, Character target) {
        int raw = (int)(attacker.getAttackPower() * 1.5) - target.getDefense();
        return Math.max(1, raw);
    }

    @Override
    public String getAttackName() {
        return "Aggressive";
    }
}
