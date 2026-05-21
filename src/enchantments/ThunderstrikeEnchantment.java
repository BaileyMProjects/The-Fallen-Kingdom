package enchantments;

import core.Difficulty;
import items.Weapon;

public class ThunderstrikeEnchantment extends WeaponEnchantment {

    private final double stunChance;

    public ThunderstrikeEnchantment(Weapon base, Difficulty difficulty) {
        super(base, "Thunderstrike", 8 - difficulty.ordinal(), EnchantmentTier.TIER_2);
        this.stunChance = 0.25 - 0.05 * difficulty.ordinal(); // Easy:25%, Medium:20%, Hard:15%
    }

    @Override public double getStunChance() { return stunChance; }

    @Override
    public String getDescription() {
        return super.getDescription()
                + "  [T2 — Thunderstrike: " + (int)(stunChance * 100) + "% chance to stun]";
    }
}
