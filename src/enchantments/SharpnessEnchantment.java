package enchantments;

import core.Difficulty;
import items.Weapon;

public class SharpnessEnchantment extends WeaponEnchantment {

    public SharpnessEnchantment(Weapon base, Difficulty difficulty) {
        super(base, "Sharpness", 6 - difficulty.ordinal(), EnchantmentTier.TIER_1);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [T1 — Sharpness]";
    }
}
