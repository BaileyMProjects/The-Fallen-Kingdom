package enchantments;

import core.Difficulty;
import items.Weapon;

public class BlazeEnchantment extends WeaponEnchantment {

    public BlazeEnchantment(Weapon base, Difficulty difficulty) {
        super(base, "Blaze", 10 - 2 * difficulty.ordinal(), EnchantmentTier.TIER_2);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [T2 — Blaze]";
    }
}
