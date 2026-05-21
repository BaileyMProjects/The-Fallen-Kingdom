package enchantments;

import core.Difficulty;
import items.Weapon;

public class LuckyEnchantment extends WeaponEnchantment {

    private final double goldMultiplier;

    public LuckyEnchantment(Weapon base, Difficulty difficulty) {
        super(base, "Lucky", Math.max(2, 3 - difficulty.ordinal()), EnchantmentTier.TIER_1);
        this.goldMultiplier = 1.20 - 0.05 * difficulty.ordinal(); // Easy:1.20, Medium:1.15, Hard:1.10
    }

    @Override public double getGoldMultiplier() { return goldMultiplier; }

    @Override
    public String getDescription() {
        return super.getDescription()
                + "  [T1 — Lucky: +" + (int)((goldMultiplier - 1.0) * 100) + "% gold from enemies]";
    }
}
