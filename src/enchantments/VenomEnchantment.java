package enchantments;

import core.Difficulty;
import items.Weapon;

public class VenomEnchantment extends WeaponEnchantment {

    private static final int POISON_TURNS = 3;
    private final int poisonDamage;

    public VenomEnchantment(Weapon base, Difficulty difficulty) {
        super(base, "Venom", Math.max(2, 3 - difficulty.ordinal()), EnchantmentTier.TIER_1);
        this.poisonDamage = 4 - difficulty.ordinal(); // Easy:4, Medium:3, Hard:2
    }

    @Override public int getPoisonDamage() { return poisonDamage; }
    @Override public int getPoisonTurns()  { return POISON_TURNS; }

    @Override
    public String getDescription() {
        return super.getDescription()
                + "  [T1 — Venom: " + poisonDamage + " dmg/turn for " + POISON_TURNS + " turns]";
    }
}
