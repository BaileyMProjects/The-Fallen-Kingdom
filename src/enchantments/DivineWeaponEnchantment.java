package enchantments;

import core.Difficulty;
import items.Weapon;

/**
 * DivineWeaponEnchantment — Tier 4, the most powerful weapon enchantment.
 *
 * Combines the best aspects of Lifesteal (healing), Thunderstrike (stun),
 * and Lucky (gold bonus) into a single elite enchantment.  Only obtainable
 * through Luminara the Soulsmith using a Divine Crystal.
 *
 * Stats scale with difficulty (Easy / Medium / Hard):
 *   Attack bonus : +22 / +18 / +14
 *   Lifesteal    : 45% / 40% / 35% of damage dealt
 *   Stun chance  : 30% / 25% / 20%
 *   Gold bonus   : +30% / +25% / +20%
 */
public class DivineWeaponEnchantment extends WeaponEnchantment {

    private final double lifestealRate;
    private final double stunChance;
    private final double goldMultiplier;

    public DivineWeaponEnchantment(Weapon base, Difficulty difficulty) {
        super(base, "Divine", 22 - 4 * difficulty.ordinal(), EnchantmentTier.TIER_4);
        this.lifestealRate  = 0.45 - 0.05 * difficulty.ordinal();
        this.stunChance     = 0.30 - 0.05 * difficulty.ordinal();
        this.goldMultiplier = 1.30 - 0.05 * difficulty.ordinal();
    }

    @Override public double getLifestealRate()  { return lifestealRate; }
    @Override public double getStunChance()     { return stunChance; }
    @Override public double getGoldMultiplier() { return goldMultiplier; }

    @Override
    public String getDescription() {
        return super.getDescription()
                + "  [T4 — Divine: heals " + (int)(lifestealRate * 100) + "% damage dealt"
                + ", " + (int)(stunChance * 100) + "% stun chance"
                + ", +" + (int)((goldMultiplier - 1.0) * 100) + "% gold from enemies]";
    }
}
