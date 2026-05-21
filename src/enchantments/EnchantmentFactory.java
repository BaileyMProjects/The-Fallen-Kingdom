package enchantments;

import core.Difficulty;
import items.Armour;
import items.Weapon;

import java.util.Random;

/**
 * EnchantmentFactory — creates randomised enchantments for weapons and armour.
 *
 * Tier is rolled first (60% T1 / 30% T2 / 10% T3), then one enchantment is
 * chosen at random from the pool for that tier and item category.
 * All stat values scale with the supplied Difficulty via each enchantment's
 * own constructor, keeping the factory free of any balance numbers.
 */
public class EnchantmentFactory {

    private static final Random RNG = new Random();

    private EnchantmentFactory() {}

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    /**
     * Rolls a random enchantment appropriate for the given item and difficulty.
     * If the item is already enchanted its base (un-enchanted) form is used,
     * so the old enchantment is replaced rather than stacked.
     *
     * @param item       a Weapon or Armour from the player's inventory
     * @param difficulty the current game difficulty
     * @return the enchanted item (WeaponEnchantment or ArmourEnchantment)
     * @throws IllegalArgumentException if item is neither a Weapon nor an Armour
     */
    public static Object roll(Object item, Difficulty difficulty) {
        EnchantmentTier tier = rollTier();

        if (item instanceof WeaponEnchantment) {
            return rollWeapon(((WeaponEnchantment) item).getBase(), difficulty, tier);
        } else if (item instanceof Weapon) {
            return rollWeapon((Weapon) item, difficulty, tier);
        } else if (item instanceof ArmourEnchantment) {
            return rollArmour(((ArmourEnchantment) item).getBase(), difficulty, tier);
        } else if (item instanceof Armour) {
            return rollArmour((Armour) item, difficulty, tier);
        }
        throw new IllegalArgumentException("Only weapons and armour can be enchanted.");
    }

    // -------------------------------------------------------------------------
    // Tier roll — 60 / 30 / 10
    // -------------------------------------------------------------------------

    private static EnchantmentTier rollTier() {
        double r = RNG.nextDouble();
        if (r < 0.60) return EnchantmentTier.TIER_1;
        if (r < 0.90) return EnchantmentTier.TIER_2;
        return EnchantmentTier.TIER_3;
    }

    // -------------------------------------------------------------------------
    // Weapon pools
    // -------------------------------------------------------------------------

    private static WeaponEnchantment rollWeapon(Weapon base, Difficulty d, EnchantmentTier tier) {
        switch (tier) {
            case TIER_1: return rollWeaponT1(base, d);
            case TIER_2: return rollWeaponT2(base, d);
            default:     return rollWeaponT3(base, d);
        }
    }

    private static WeaponEnchantment rollWeaponT1(Weapon base, Difficulty d) {
        switch (RNG.nextInt(5)) {
            case 0:  return new FlameEnchantment(base, d);
            case 1:  return new SharpnessEnchantment(base, d);
            case 2:  return new VenomEnchantment(base, d);
            case 3:  return new FrostEnchantment(base, d);
            default: return new LuckyEnchantment(base, d);
        }
    }

    private static WeaponEnchantment rollWeaponT2(Weapon base, Difficulty d) {
        switch (RNG.nextInt(3)) {
            case 0:  return new BlazeEnchantment(base, d);
            case 1:  return new LifestealEnchantment(base, d);
            default: return new ThunderstrikeEnchantment(base, d);
        }
    }

    private static WeaponEnchantment rollWeaponT3(Weapon base, Difficulty d) {
        return RNG.nextBoolean()
                ? new VoidEdgeEnchantment(base, d)
                : new SoulrenderEnchantment(base, d);
    }

    // -------------------------------------------------------------------------
    // Armour pools
    // -------------------------------------------------------------------------

    private static ArmourEnchantment rollArmour(Armour base, Difficulty d, EnchantmentTier tier) {
        switch (tier) {
            case TIER_1: return rollArmourT1(base, d);
            case TIER_2: return rollArmourT2(base, d);
            default:     return rollArmourT3(base, d);
        }
    }

    private static ArmourEnchantment rollArmourT1(Armour base, Difficulty d) {
        switch (RNG.nextInt(3)) {
            case 0:  return new WardingEnchantment(base, d);
            case 1:  return new ThornsEnchantment(base, d);
            default: return new SwiftnessEnchantment(base, d);
        }
    }

    private static ArmourEnchantment rollArmourT2(Armour base, Difficulty d) {
        return RNG.nextBoolean()
                ? new BulwarkEnchantment(base, d)
                : new RegenerationEnchantment(base, d);
    }

    private static ArmourEnchantment rollArmourT3(Armour base, Difficulty d) {
        return RNG.nextBoolean()
                ? new ShadowguardEnchantment(base, d)
                : new FortressEnchantment(base, d);
    }
}
