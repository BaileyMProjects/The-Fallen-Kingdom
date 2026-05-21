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
 * Extends NPC with an enchant() method.  The cost is always 30 gold +
 * 1 Shadow Crystal.  EnchantmentFactory handles the random tier roll and
 * enchantment selection; Enchanter handles the player-facing interaction,
 * cost deduction, and inventory/equipment swap.
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

    // -------------------------------------------------------------------------
    // Enchanting
    // -------------------------------------------------------------------------

    /**
     * Attempts to enchant the given item for the player.
     *
     * Requirements: 30 gold + 1 Shadow Crystal in inventory.
     * The item must be a Weapon or Armour (enchanted or plain).
     * On success: costs are deducted, old item removed, enchanted item added,
     * and the item is re-equipped if it was equipped at the time.
     */
    public void enchant(Player player, Item item, Difficulty difficulty) {
        if (!(item instanceof Weapon) && !(item instanceof Armour)) {
            System.out.println(getName() + " examines the item carefully, then shakes her head.");
            System.out.println("\"I can only enchant weapons and armour. This won't do.\"");
            return;
        }

        int cost = difficulty.enchantCost;
        if (player.getGold() < cost) {
            System.out.println("\"The ritual requires " + cost + " gold on your chosen difficulty. You have "
                    + player.getGold() + ". Come back when your purse is heavier.\"");
            return;
        }

        Item crystal = player.getInventory().findItem("shadow crystal");
        if (crystal == null) {
            System.out.println("\"The ritual requires a Shadow Crystal to channel the shadow energy.");
            System.out.println("  Bring me one — Aldric sells them, or you may find them on stronger enemies.\"");
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
                + " and holds it above a swirling vortex of shadow energy...");
        pause(1200);
        System.out.println("  The crystal shatters. Dark light floods the room.");
        pause(1000);

        Item enchanted = EnchantmentFactory.roll(item, difficulty);

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

        String tierLabel = tier == EnchantmentTier.TIER_1 ? "Tier 1"
                         : tier == EnchantmentTier.TIER_2 ? "Tier 2" : "Tier 3";

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
        return super.talk(player, questManager)
                + "\n\n  Cost: 30g (Easy) / 50g (Medium) / 75g (Hard) + 1 Shadow Crystal"
                + "\n  Odds: 60% Tier 1 / 30% Tier 2 / 10% Tier 3";
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private static void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
