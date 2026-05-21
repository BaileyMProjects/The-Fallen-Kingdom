package enchantments;

import core.Difficulty;
import items.Armour;

public class ThornsEnchantment extends ArmourEnchantment {

    private final double reflectRate;

    public ThornsEnchantment(Armour base, Difficulty difficulty) {
        super(base, "Thorns",
              difficulty.ordinal() == 2 ? 1 : 2,   // Easy:2, Medium:2, Hard:1
              0.0,
              EnchantmentTier.TIER_1);
        this.reflectRate = 0.10 - 0.02 * difficulty.ordinal(); // Easy:10%, Medium:8%, Hard:6%
    }

    @Override public double getReflectRate() { return reflectRate; }

    @Override
    public String getDescription() {
        return super.getDescription()
                + "  [T1 — Thorns: reflects " + (int)(reflectRate * 100) + "% damage taken]";
    }
}
