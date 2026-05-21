package enchantments;

import core.Difficulty;
import items.Armour;

public class SwiftnessEnchantment extends ArmourEnchantment {

    // Miss bonus values: Easy:15%, Medium:10%, Hard:8%
    private static final double[] MISS_BY_DIFF = { 0.15, 0.10, 0.08 };

    public SwiftnessEnchantment(Armour base, Difficulty difficulty) {
        super(base, "Swiftness",
              Math.max(2, 3 - difficulty.ordinal()),    // Easy:3, Medium:2, Hard:2
              MISS_BY_DIFF[difficulty.ordinal()],
              EnchantmentTier.TIER_1);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [T1 — Swiftness]";
    }
}
