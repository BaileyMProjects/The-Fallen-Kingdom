package enchantments;

import core.Difficulty;
import items.Armour;

public class RegenerationEnchantment extends ArmourEnchantment {

    private final int regenAmount;

    public RegenerationEnchantment(Armour base, Difficulty difficulty) {
        super(base, "Regeneration", 5 - difficulty.ordinal(), 0.0, EnchantmentTier.TIER_2);
        this.regenAmount = 8 - 2 * difficulty.ordinal(); // Easy:8, Medium:6, Hard:4
    }

    @Override public int getRegenAmount() { return regenAmount; }

    @Override
    public String getDescription() {
        return super.getDescription()
                + "  [T2 — Regeneration: +" + regenAmount + " HP at start of each turn]";
    }
}
