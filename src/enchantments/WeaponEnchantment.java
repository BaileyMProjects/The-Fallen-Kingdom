package enchantments;

import items.Weapon;
import items.WeaponSpecialAttack;

import java.util.List;

/**
 * WeaponEnchantment — abstract Decorator for Weapon (Decorator design pattern).
 *
 * Wraps an existing Weapon and adds enchantment bonuses on top without
 * modifying the original class. Each concrete subclass represents one named
 * enchantment and overrides only the effect hooks it needs.
 *
 * Because this extends Weapon, the rest of the game (Player.equip,
 * CombatSystem) handles it identically to a plain Weapon — the Decorator
 * is transparent to all existing code.
 */
public abstract class WeaponEnchantment extends Weapon {

    protected final Weapon          base;
    protected final EnchantmentTier tier;
    protected final String          enchantmentName;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    protected WeaponEnchantment(Weapon base, String enchantmentName,
                                 int attackBonus, EnchantmentTier tier) {
        super(base.getName() + " [" + enchantmentName + "]",
              base.getRawDescription(),
              base.getValue(),
              base.getAttackBonus() + attackBonus);
        this.base            = base;
        this.tier            = tier;
        this.enchantmentName = enchantmentName;
    }

    // -------------------------------------------------------------------------
    // Decorator unwrapping
    // -------------------------------------------------------------------------

    /** Returns the original un-enchanted weapon, stripping any prior enchantments. */
    public Weapon getBase() {
        return (base instanceof WeaponEnchantment)
                ? ((WeaponEnchantment) base).getBase()
                : base;
    }

    // -------------------------------------------------------------------------
    // Special effect hooks — default no-op; subclasses override as needed
    // -------------------------------------------------------------------------

    /** Damage dealt by poison each turn. 0 means no poison. */
    public int    getPoisonDamage()   { return 0; }

    /** Number of turns poison lasts. 0 means no poison. */
    public int    getPoisonTurns()    { return 0; }

    /** Probability (0.0–1.0) that the enemy's next attack is skipped (slow). */
    public double getSlowChance()     { return 0.0; }

    /** Probability (0.0–1.0) that the enemy loses their entire turn (stun). */
    public double getStunChance()     { return 0.0; }

    /** Fraction of damage dealt that heals the player (0.0 = none). */
    public double getLifestealRate()  { return 0.0; }

    /** Multiplier applied to enemy gold drops (1.0 = no change). */
    public double getGoldMultiplier() { return 1.0; }

    // -------------------------------------------------------------------------
    // Weapon special-attack delegation — all calls forwarded to the base weapon
    // -------------------------------------------------------------------------

    @Override public List<WeaponSpecialAttack> getSpecials()                  { return getBase().getSpecials(); }
    @Override public void addSpecialAttack(WeaponSpecialAttack s)             { getBase().addSpecialAttack(s); }
    @Override public int  getCooldown(String name)                            { return getBase().getCooldown(name); }
    @Override public void setCooldown(String name, int turns)                 { getBase().setCooldown(name, turns); }
    @Override public void tickCooldowns()                                     { getBase().tickCooldowns(); }
    @Override public void resetCooldowns()                                    { getBase().resetCooldowns(); }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public EnchantmentTier getTier()            { return tier; }
    public String          getEnchantmentName() { return enchantmentName; }
}
