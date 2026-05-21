package characters;

import core.Difficulty;
import enchantments.ArmourEnchantment;
import enchantments.EnchantmentFactory;
import enchantments.EnchantmentTier;
import enchantments.WeaponEnchantment;
import items.Armour;
import items.Item;
import items.Weapon;
import quests.QuestManager;

/**
 * Enchanter — a specialised NPC that applies random enchantments to gear.
 *
 * Extends NPC with an enchant() method.  EnchantmentFactory handles the
 * random tier roll and enchantment selection; Enchanter handles the
 * player-facing interaction, cost deduction, and inventory/equipment swap.
 *
 * DivineEnchanter subclasses this and overrides the three protected hook
 * methods to use a different crystal, cost, and enchantment pool — without
 * duplicating any of the shared validation and swap logic.
 *
 * This class is the consumer of the Decorator pattern: it receives a plain
 * Weapon or Armour, wraps it in an enchantment Decorator, and returns the
 * wrapped item to the player's inventory.
 */
public class Enchanter extends NPC {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Enchanter(String name, String description) {
        super(name, description, new String[]{
            "Ah, a wanderer seeking power. Bring me a weapon or armour and a Shadow " +
            "Crystal, and I will call upon the shadows to bind their energy to your gear. " +
            "The fee scales with the darkness you face — type 'enchant <item>' to begin.",
            "The shadows whisper to me the secrets of every enchantment. The result is " +
            "never certain — the shadows choose the power, not I. Tier and effect are " +
            "decided by fate: 60% chance of Tier 1, 30% Tier 2, 10% Tier 3.",
            "A word of caution: enchanting replaces any existing enchantment. The old " +
            "power is consumed and the new one takes its place. Choose wisely."
        });
    }

    /** Protected constructor for subclasses that supply their own dialogue lines. */
    protected Enchanter(String name, String description, String[] dialogue) {
        super(name, description, dialogue);
    }

    // -------------------------------------------------------------------------
    // Protected hooks — overridden by DivineEnchanter
    // -------------------------------------------------------------------------

    protected int    getEnchantCost(Difficulty d)               { return d.enchantCost; }
    protected String getCrystalSearchName()                     { return "shadow crystal"; }
    protected String getCrystalDisplayName()                    { return "Shadow Crystal"; }
    protected String getCrystalSourceHint()                     { return "Aldric sells them in the Merchant's Village, or find them on stronger shadow enemies."; }
    protected String getChannelFlavour()                        { return "a swirling vortex of shadow energy"; }
    protected String getCrystalShatterFlavour()                 { return "The crystal shatters. Dark light floods the room."; }
    protected Item   rollEnchantment(Item item, Difficulty d)   { return EnchantmentFactory.roll(item, d); }

    // -------------------------------------------------------------------------
    // Enchanting
    // -------------------------------------------------------------------------

    /**
     * Attempts to enchant the given item for the player.
     * Cost and crystal type are determined by the protected hooks so that
     * DivineEnchanter can reuse all validation and swap logic unchanged.
     */
    public final void enchant(Player player, Item item, Difficulty difficulty) {
        if (!(item instanceof Weapon) && !(item instanceof Armour)) {
            System.out.println(getName() + " examines the item carefully, then shakes their head.");
            System.out.println("\"I can only enchant weapons and armour. This won't do.\"");
            return;
        }

        int cost = getEnchantCost(difficulty);
        if (player.getGold() < cost) {
            System.out.println("\"The ritual requires " + cost + " gold. You have "
                    + player.getGold() + ". Come back when your purse is heavier.\"");
            return;
        }

        Item crystal = player.getInventory().findItem(getCrystalSearchName());
        if (crystal == null) {
            System.out.println("\"The ritual requires a " + getCrystalDisplayName() + ".");
            System.out.println("  " + getCrystalSourceHint() + "\"");
            return;
        }

        // Record whether the item being enchanted is currently equipped
        boolean wasEquippedWeapon = player.getEquippedWeapon() == item;
        boolean wasEquippedArmour = player.getEquippedHead()  == item
                                 || player.getEquippedTorso() == item
                                 || player.getEquippedLegs()  == item;

        // Deduct costs
        player.spendGold(cost);
        player.getInventory().removeItem(crystal);

        // Perform enchantment
        System.out.println("\n" + getName() + " takes your " + item.getName()
                + " and holds it above " + getChannelFlavour() + "...");
        pause(1200);
        System.out.println("  " + getCrystalShatterFlavour());
        pause(1000);

        Item enchanted = rollEnchantment(item, difficulty);

        // Swap in inventory
        player.getInventory().removeItem(item);
        player.getInventory().addItem(enchanted);

        // Re-equip if the old item was equipped
        if (wasEquippedWeapon || wasEquippedArmour) {
            player.equip(enchanted);
        }

        // Announce result
        String enchName;
        EnchantmentTier tier;
        if (enchanted instanceof WeaponEnchantment) {
            enchName = ((WeaponEnchantment) enchanted).getEnchantmentName();
            tier     = ((WeaponEnchantment) enchanted).getTier();
        } else {
            enchName = ((ArmourEnchantment) enchanted).getEnchantmentName();
            tier     = ((ArmourEnchantment) enchanted).getTier();
        }

        String tierLabel;
        switch (tier) {
            case TIER_1: tierLabel = "Tier 1";         break;
            case TIER_2: tierLabel = "Tier 2";         break;
            case TIER_3: tierLabel = "Tier 3";         break;
            case TIER_4: tierLabel = "Tier 4 — DIVINE"; break;
            default:     tierLabel = tier.name();      break;
        }

        System.out.println("\n  A flash of arcane light — the ritual is complete!");
        pause(700);
        System.out.println("  Result:      " + enchanted.getName());
        System.out.println("  Enchantment: " + enchName + " (" + tierLabel + ")");
        System.out.println("  " + enchanted.getDescription());
    }

    // -------------------------------------------------------------------------
    // Dialogue override — show enchanting info on talk
    // -------------------------------------------------------------------------

    @Override
    public String talk(Player player, QuestManager questManager) {
        return super.talk(player, questManager) + getTalkCostLine();
    }

    protected String getTalkCostLine() {
        return "\n\n  Cost: 30g (Easy) / 50g (Medium) / 75g (Hard) + 1 Shadow Crystal"
             + "\n  Odds: 60% Tier 1 / 30% Tier 2 / 10% Tier 3";
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    protected static void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
