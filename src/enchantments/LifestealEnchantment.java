package enchantments;

import core.Difficulty;
import items.Weapon;

public class LifestealEnchantment extends WeaponEnchantment {

    private final double lifestealRate;

    public LifestealEnchantment(Weapon base, Difficulty difficulty) {
        super(base, "Lifesteal", 7 - difficulty.ordinal(), EnchantmentTier.TIER_2);
        this.lifestealRate = 0.30 - 0.05 * difficulty.ordinal(); // Easy:30%, Medium:25%, Hard:20%
    }

    @Override public double getLifestealRate() { return lifestealRate; }

    @Override
    public String getDescription() {
        return super.getDescription()
                + "  [T2 — Lifesteal: heals " + (int)(lifestealRate * 100) + "% of damage dealt]";
    }
}
