package enchantments;

import core.Difficulty;
import items.Armour;

public class BulwarkEnchantment extends ArmourEnchantment {

    public BulwarkEnchantment(Armour base, Difficulty difficulty) {
        super(base, "Bulwark", 10 - 2 * difficulty.ordinal(), 0.0, EnchantmentTier.TIER_2);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [T2 — Bulwark]";
    }
}
