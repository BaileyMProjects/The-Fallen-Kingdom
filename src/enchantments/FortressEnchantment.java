package enchantments;

import core.Difficulty;
import items.Armour;

public class FortressEnchantment extends ArmourEnchantment {

    private final double reflectRate;

    public FortressEnchantment(Armour base, Difficulty difficulty) {
        super(base, "Fortress", 10 - 2 * difficulty.ordinal(), 0.0, EnchantmentTier.TIER_3);
        this.reflectRate = 0.15 - 0.025 * difficulty.ordinal(); // Easy:15%, Medium:12.5%, Hard:10%
    }

    @Override public double getReflectRate() { return reflectRate; }

    @Override
    public String getDescription() {
        return super.getDescription()
                + "  [T3 — Fortress: reflects " + (int)(reflectRate * 100) + "% damage taken]";
    }
}
