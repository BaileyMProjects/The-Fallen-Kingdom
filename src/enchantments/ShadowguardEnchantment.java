package enchantments;

import core.Difficulty;
import items.Armour;

public class ShadowguardEnchantment extends ArmourEnchantment {

    // Miss bonus values: Easy:12.5%, Medium:10%, Hard:7.5%
    private static final double[] MISS_BY_DIFF = { 0.125, 0.10, 0.075 };

    public ShadowguardEnchantment(Armour base, Difficulty difficulty) {
        super(base, "Shadowguard", 5, MISS_BY_DIFF[difficulty.ordinal()], EnchantmentTier.TIER_3);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [T3 — Shadowguard]";
    }
}
