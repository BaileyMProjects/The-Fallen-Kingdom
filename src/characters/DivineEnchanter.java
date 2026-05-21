package characters;

import core.Difficulty;
import enchantments.EnchantmentFactory;
import items.Item;

/**
 * DivineEnchanter — a specialised Enchanter found in the Divine Forge.
 *
 * Uses Divine Crystals instead of Shadow Crystals, charges the divine enchant
 * cost (120/170/250g by difficulty), and rolls from the divine tier table
 * (35% T1 / 30% T2 / 25% T3 / 10% T4 Divine) rather than the standard one.
 *
 * All validation, cost deduction, inventory swap, and result announcement
 * logic is inherited from Enchanter — only the hook methods differ.
 */
public class DivineEnchanter extends Enchanter {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public DivineEnchanter(String name, String description) {
        super(name, description, new String[]{
            "You seek celestial power. Bring me a weapon or armour and a Divine " +
            "Crystal, and I will channel the light of the fallen order into your gear. " +
            "The cost is greater — the power within is far beyond shadow. Type 'enchant <item>'.",
            "The light does not whisper — it blazes. Tier 4 is exceedingly rare, but those " +
            "who receive the Divine blessing carry the full might of the celestial order. " +
            "Odds: 40% Tier 1 / 30% Tier 2 / 25% Tier 3 / 5% Tier 4 Divine.",
            "A word of caution: enchanting replaces any existing enchantment, including " +
            "shadow ones. The old power is consumed — celestial energy takes its place."
        });
    }

    // -------------------------------------------------------------------------
    // Hooks — override to use Divine Crystal, cost, and pool
    // -------------------------------------------------------------------------

    @Override
    protected int getEnchantCost(Difficulty d) { return d.divineEnchantCost; }

    @Override
    protected String getCrystalSearchName() { return "divine crystal"; }

    @Override
    protected String getCrystalDisplayName() { return "Divine Crystal"; }

    @Override
    protected String getCrystalSourceHint() {
        return "Divine Crystals drop from enemies in the celestial realm. The Arbiter drops one guaranteed.";
    }

    @Override
    protected String getChannelFlavour() { return "a column of blinding golden light"; }

    @Override
    protected String getCrystalShatterFlavour() {
        return "The crystal dissolves into radiant motes. Celestial energy floods the forge.";
    }

    @Override
    protected Item rollEnchantment(Item item, Difficulty d) {
        return EnchantmentFactory.rollDivine(item, d);
    }

    @Override
    protected String getTalkCostLine() {
        return "\n\n  Cost: 120g (Easy) / 170g (Medium) / 250g (Hard) + 1 Divine Crystal"
             + "\n  Odds: 40% Tier 1 / 30% Tier 2 / 25% Tier 3 / 5% Tier 4 (Divine)";
    }
}
