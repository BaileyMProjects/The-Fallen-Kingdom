package enchantments;

import core.Difficulty;
import items.Armour;

/**
 * DivineArmourEnchantment — Tier 4, the most powerful armour enchantment.
 *
 * Combines defense, evasion, regeneration, and reflect into a single elite
 * enchantment.  Miss rate is intentionally capped (12/10/8% per piece) to
 * prevent stacking three pieces from becoming overpowered.
 * Only obtainable through Luminara the Soulsmith using a Divine Crystal.
 *
 * Stats scale with difficulty (Easy / Medium / Hard):
 *   Defense bonus : +15 / +12 / +9
 *   Miss rate     : 12% / 10% / 8%   (per piece — intentionally lower than raw numbers)
 *   Regen per turn: +12 / +10 / +8 HP
 *   Reflect rate  : 20% / 17.5% / 15% of damage taken
 */
public class DivineArmourEnchantment extends ArmourEnchantment {

    private static final double[] MISS_BY_DIFF = { 0.12, 0.10, 0.08 };

    private final double enchantMissBonus;
    private final double reflectRate;
    private final int    regenAmount;

    public DivineArmourEnchantment(Armour base, Difficulty difficulty) {
        super(base, "Divine",
              15 - 3 * difficulty.ordinal(),
              MISS_BY_DIFF[difficulty.ordinal()],
              EnchantmentTier.TIER_4);
        this.enchantMissBonus = MISS_BY_DIFF[difficulty.ordinal()];
        this.reflectRate      = 0.20 - 0.025 * difficulty.ordinal();
        this.regenAmount      = 12 - 2 * difficulty.ordinal();
    }

    @Override public double getReflectRate() { return reflectRate; }
    @Override public int    getRegenAmount() { return regenAmount; }

    @Override
    public String getDescription() {
        return super.getDescription()
                + "  [T4 — Divine: +" + regenAmount + " HP/turn regen"
                + ", reflects " + (int)(reflectRate * 100) + "% damage taken"
                + ", +" + (int)(enchantMissBonus * 100) + "% enemy miss chance]";
    }
}
