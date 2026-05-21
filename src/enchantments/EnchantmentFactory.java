package enchantments;

import core.Difficulty;
import items.Armour;
import items.Item;
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
     * Rolls a random enchantment using the standard Shadow Enchanter odds (60/30/10).
     * If the item is already enchanted its base form is used (old enchant replaced).
     */
    public static Item roll(Item item, Difficulty difficulty) {
        EnchantmentTier tier = rollTier();
        return rollForTier(item, difficulty, tier);
    }

    /**
     * Rolls a random enchantment using the Divine Enchanter odds (35/30/25/10).
     * T4 produces a Divine enchantment; T1-T3 use the same pools as roll().
     */
    public static Item rollDivine(Item item, Difficulty difficulty) {
        EnchantmentTier tier = rollDivineTier();

        if (tier == EnchantmentTier.TIER_4) {
            Weapon  baseWeapon = unwrapWeapon(item);
            Armour  baseArmour = unwrapArmour(item);
            if (baseWeapon != null) return new DivineWeaponEnchantment(baseWeapon, difficulty);
            if (baseArmour != null) return new DivineArmourEnchantment(baseArmour, difficulty);
            throw new IllegalArgumentException("Only weapons and armour can be enchanted.");
        }

        return rollForTier(item, difficulty, tier);
    }

    // -------------------------------------------------------------------------
    // Tier rolls
    // -------------------------------------------------------------------------

    /** Standard Shadow Enchanter odds: 60% T1 / 30% T2 / 10% T3. */
    private static EnchantmentTier rollTier() {
        double r = RNG.nextDouble();
        if (r < 0.60) return EnchantmentTier.TIER_1;
        if (r < 0.90) return EnchantmentTier.TIER_2;
        return EnchantmentTier.TIER_3;
    }

    /** Divine Enchanter odds: 35% T1 / 30% T2 / 25% T3 / 10% T4. */
    private static EnchantmentTier rollDivineTier() {
        double r = RNG.nextDouble();
        if (r < 0.35) return EnchantmentTier.TIER_1;
        if (r < 0.65) return EnchantmentTier.TIER_2;
        if (r < 0.90) return EnchantmentTier.TIER_3;
        return EnchantmentTier.TIER_4;
    }

    // -------------------------------------------------------------------------
    // Shared dispatch
    // -------------------------------------------------------------------------

    private static Item rollForTier(Item item, Difficulty difficulty, EnchantmentTier tier) {
        Weapon baseWeapon = unwrapWeapon(item);
        Armour baseArmour = unwrapArmour(item);
        if (baseWeapon != null) return rollWeapon(baseWeapon, difficulty, tier);
        if (baseArmour != null) return rollArmour(baseArmour, difficulty, tier);
        throw new IllegalArgumentException("Only weapons and armour can be enchanted.");
    }

    private static Weapon unwrapWeapon(Item item) {
        if (item instanceof WeaponEnchantment) return ((WeaponEnchantment) item).getBase();
        if (item instanceof Weapon)            return (Weapon) item;
        return null;
    }

    private static Armour unwrapArmour(Item item) {
        if (item instanceof ArmourEnchantment) return ((ArmourEnchantment) item).getBase();
        if (item instanceof Armour)            return (Armour) item;
        return null;
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
