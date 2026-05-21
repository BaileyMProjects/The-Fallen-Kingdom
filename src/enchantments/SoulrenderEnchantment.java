package enchantments;

import core.Difficulty;
import items.Weapon;

public class SoulrenderEnchantment extends WeaponEnchantment {

    private final double lifestealRate;
    private final double stunChance;

    public SoulrenderEnchantment(Weapon base, Difficulty difficulty) {
        super(base, "Soulrender", 12 - 2 * difficulty.ordinal(), EnchantmentTier.TIER_3);
        this.lifestealRate = 0.35 - 0.05 * difficulty.ordinal(); // Easy:35%, Medium:30%, Hard:25%
        this.stunChance    = 0.20 - 0.05 * difficulty.ordinal(); // Easy:20%, Medium:15%, Hard:10%
    }

    @Override public double getLifestealRate() { return lifestealRate; }
    @Override public double getStunChance()    { return stunChance; }

    @Override
    public String getDescription() {
        return super.getDescription()
                + "  [T3 — Soulrender: heals " + (int)(lifestealRate * 100) + "% damage dealt"
                + ", " + (int)(stunChance * 100) + "% stun chance]";
    }
}
