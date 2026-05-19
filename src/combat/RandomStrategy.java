package combat;

import characters.Character;

/**
 * RandomStrategy — an erratic, unpredictable attack style.
 *
 * Each attack is randomly one of three outcomes:
 *   50% — Normal strike  (1.0× base damage)
 *   30% — Heavy strike   (1.75× base damage)
 *   20% — Weak strike    (0.5× base damage)
 *
 * Assigned to Shadow Goblins by EnemyFactory.  Used as the safe default in
 * Enemy's constructor because every enemy needs a strategy even before the
 * factory customises it.
 *
 * Trade-off: high variance — dangerous one moment, harmless the next.
 */
public class RandomStrategy implements AttackStrategy {

    @Override
    public int calculateDamage(Character attacker, Character target) {
        double roll = Math.random();
        double multiplier;
        String label;

        if (roll < 0.20) {
            multiplier = 0.5;
            label      = "Weak strike";
        } else if (roll < 0.70) {
            multiplier = 1.0;
            label      = "Strike";
        } else {
            multiplier = 1.75;
            label      = "Heavy strike";
        }

        int base   = attacker.getAttackPower() - target.getDefense();
        int damage = Math.max(1, (int)(base * multiplier));
        System.out.println("  [" + label + "]");
        return damage;
    }

    @Override
    public String getAttackName() {
        return "Random";
    }
}
