package enchantments;

import core.Difficulty;
import items.Weapon;

public class FrostEnchantment extends WeaponEnchantment {

    private final double slowChance;

    public FrostEnchantment(Weapon base, Difficulty difficulty) {
        super(base, "Frost", Math.max(2, 3 - difficulty.ordinal()), EnchantmentTier.TIER_1);
        this.slowChance = 0.25 - 0.05 * difficulty.ordinal(); // Easy:25%, Medium:20%, Hard:15%
    }

    @Override public double getSlowChance() { return slowChance; }

    @Override
    public String getDescription() {
        return super.getDescription()
                + "  [T1 — Frost: " + (int)(slowChance * 100) + "% chance to slow]";
    }
}
