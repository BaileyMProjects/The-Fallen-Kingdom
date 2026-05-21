package enchantments;

import core.Difficulty;
import items.Weapon;

public class VoidEdgeEnchantment extends WeaponEnchantment {

    public VoidEdgeEnchantment(Weapon base, Difficulty difficulty) {
        super(base, "Void Edge", 18 - 3 * difficulty.ordinal(), EnchantmentTier.TIER_3);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [T3 — Void Edge]";
    }
}
