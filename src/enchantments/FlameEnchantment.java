package enchantments;

import core.Difficulty;
import items.Weapon;

public class FlameEnchantment extends WeaponEnchantment {

    public FlameEnchantment(Weapon base, Difficulty difficulty) {
        super(base, "Flame", 5 - difficulty.ordinal(), EnchantmentTier.TIER_1);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [T1 — Flame]";
    }
}
