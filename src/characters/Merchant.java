package characters;

import items.Item;
import quests.QuestManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Merchant — a specialised NPC that runs a shop.
 *
 * Extends NPC with buy/sell functionality.
 *
 * Composition is used for the shop stock (a List<Item>), demonstrating that
 * inheritance is used only when the IS-A relationship genuinely holds:
 * a Merchant IS an NPC who also happens to sell things.
 *
 * Buy price  = item.getValue()
 * Sell price = 50% of buy price (rounded up to at least 1 gold)
 */
public class Merchant extends NPC {

    private static final String NAME        = "Griswold the Merchant";
    private static final String DESCRIPTION =
            "A stout, cheerful merchant surrounded by crates and bundles of goods.";
    private static final int SELL_PERCENT   = 50;

    private final List<Item> shopItems;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Merchant() {
        super(NAME, DESCRIPTION, new String[]{
            "Welcome, traveller! Use 'buy <item>' to purchase or 'sell <item>' to offload.",
            "Staying alive costs gold, and I have exactly what you need. Browse away!"
        });
        this.shopItems = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Shop stock management
    // -------------------------------------------------------------------------

    public void addShopItem(Item item) {
        shopItems.add(item);
    }

    // -------------------------------------------------------------------------
    // Transactions
    // -------------------------------------------------------------------------

    /**
     * Player buys an item by partial name match.
     * Deducts gold and moves the item from shop stock into the player's inventory.
     */
    public void buyItem(String itemName, Player player) {
        String search = itemName.toLowerCase().trim();
        Item found = shopItems.stream()
                              .filter(i -> i.getName().toLowerCase().contains(search))
                              .findFirst()
                              .orElse(null);

        if (found == null) {
            System.out.println("I don't stock a '" + itemName + "'. Type 'look at merchant' to see my wares.");
            return;
        }
        if (!player.spendGold(found.getValue())) {
            System.out.println("You can't afford that — it costs " + found.getValue()
                    + " gold and you only have " + player.getGold() + ".");
            return;
        }
        player.getInventory().addItem(found);
        shopItems.remove(found);
        System.out.println("Purchased: " + found.getName() + " for " + found.getValue() + " gold.");
    }

    /**
     * Player sells an item from their inventory.
     * Awards SELL_PERCENT of the item's buy value (minimum 1 gold).
     */
    public void sellItem(Item item, Player player) {
        int sellPrice = Math.max(1, item.getValue() * SELL_PERCENT / 100);
        player.getInventory().removeItem(item);
        player.addGold(sellPrice);
        System.out.println("Sold: " + item.getName() + " for " + sellPrice + " gold.");
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    @Override
    public String talk(Player player, QuestManager questManager) {
        String greeting = super.talk(player, questManager);
        if (shopItems.isEmpty()) {
            return greeting + "\n  (I'm sold out — come back if you find more gold!)";
        }
        StringBuilder sb = new StringBuilder(greeting).append("\n\n  My stock:");
        for (Item item : shopItems) {
            sb.append("\n    - ").append(item.getName())
              .append("  [").append(item.getValue()).append(" gold]");
        }
        return sb.toString();
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder(DESCRIPTION);
        if (shopItems.isEmpty()) {
            sb.append("\n  (Sold out.)");
        } else {
            sb.append("\n\n  Stock for sale:");
            for (Item item : shopItems) {
                sb.append("\n    - ").append(item.getName())
                  .append("  [").append(item.getValue()).append(" gold]");
            }
        }
        return sb.toString();
    }
}
