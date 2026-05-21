package enchantments;

import core.Difficulty;
import items.Armour;

public class WardingEnchantment extends ArmourEnchantment {

    public WardingEnchantment(Armour base, Difficulty difficulty) {
        super(base, "Warding", 5 - difficulty.ordinal(), 0.0, EnchantmentTier.TIER_1);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [T1 — Warding]";
    }
}
