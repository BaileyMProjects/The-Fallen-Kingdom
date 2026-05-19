package combat;

import characters.Character;

/**
 * DefensiveStrategy — a disciplined, shield-focused attack style.
 *
 * Deals slightly reduced base damage (80%) but has a 30% chance of landing
 * a "Shield Bash" — a bonus strike equal to half the attacker's defence stat.
 * This makes the Dark Knight feel unpredictable: mostly conservative but
 * occasionally punishing.
 *
 * Trade-off: lower reliable damage in exchange for burst potential.
 */
public class DefensiveStrategy implements AttackStrategy {

    private static final double BASE_MULTIPLIER  = 0.8;
    private static final double BASH_PROBABILITY = 0.3;

    @Override
    public int calculateDamage(Character attacker, Character target) {
        int base = (int)(attacker.getAttackPower() * BASE_MULTIPLIER) - target.getDefense();
        base = Math.max(1, base);

        // 30% chance of a Shield Bash bonus
        if (Math.random() < BASH_PROBABILITY) {
            int bashBonus = Math.max(1, attacker.getDefense() / 2);
            System.out.println("  [Shield Bash! +" + bashBonus + " bonus damage]");
            return base + bashBonus;
        }
        return base;
    }

    @Override
    public String getAttackName() {
        return "Defensive";
    }
}
