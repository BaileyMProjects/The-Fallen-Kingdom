package enchantments;

import items.Armour;

/**
 * ArmourEnchantment — abstract Decorator for Armour (Decorator design pattern).
 *
 * Mirrors WeaponEnchantment: wraps an existing Armour piece and layers
 * enchantment bonuses on top. The combined defense and miss bonuses are baked
 * into the parent Armour fields so Player.equip() and CombatSystem need no
 * changes to handle enchanted armour.
 */
public abstract class ArmourEnchantment extends Armour {

    protected final Armour          base;
    protected final EnchantmentTier tier;
    protected final String          enchantmentName;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    protected ArmourEnchantment(Armour base, String enchantmentName,
                                 int defenseBonus, double missBonus,
                                 EnchantmentTier tier) {
        super(base.getName() + " [" + enchantmentName + "]",
              base.getRawDescription(),
              base.getValue(),
              base.getDefenseBonus() + defenseBonus,
              base.getMissBonus()    + missBonus,
              base.getSlot());
        this.base            = base;
        this.tier            = tier;
        this.enchantmentName = enchantmentName;
    }

    // -------------------------------------------------------------------------
    // Decorator unwrapping
    // -------------------------------------------------------------------------

    /** Returns the original un-enchanted armour, stripping any prior enchantments. */
    public Armour getBase() {
        return (base instanceof ArmourEnchantment)
                ? ((ArmourEnchantment) base).getBase()
                : base;
    }

    // -------------------------------------------------------------------------
    // Special effect hooks — default no-op; subclasses override as needed
    // -------------------------------------------------------------------------

    /** Fraction of damage taken that is reflected back to the attacker (0.0 = none). */
    public double getReflectRate() { return 0.0; }

    /** HP restored to the player at the start of each combat turn (0 = none). */
    public int    getRegenAmount() { return 0; }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public EnchantmentTier getTier()            { return tier; }
    public String          getEnchantmentName() { return enchantmentName; }
}
