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
    private int levelRequirement = 0;

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

    /** Constructor for named merchants (e.g. the second village smith). */
    public Merchant(String name, String description) {
        super(name, description, new String[]{
            "Welcome! Finest wares in the region. Use 'buy <item>' to purchase.",
            "I deal in quality gear — none of that flimsy village stuff. Browse freely."
        });
        this.shopItems = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Shop stock management
    // -------------------------------------------------------------------------

    public void setLevelRequirement(int level) { this.levelRequirement = level; }

    public void addShopItem(Item item) {
        shopItems.add(item);
    }

    public java.util.List<Item> getShopItems() {
        return java.util.Collections.unmodifiableList(shopItems);
    }

    // -------------------------------------------------------------------------
    // Transactions
    // -------------------------------------------------------------------------

    /**
     * Player buys an item by partial name match.
     * Deducts gold and moves the item from shop stock into the player's inventory.
     */
    public void buyItem(String itemName, Player player) {
        if (levelRequirement > 0 && player.getLevel() < levelRequirement) {
            System.out.println("\"My wares are not for the unprepared. Return when you have reached level "
                    + levelRequirement + ".\"");
            return;
        }
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
     * The Forest Gem triggers a unique merchant reaction and a randomised
     * gold reward (50–100 gold) reflecting its rarity.
     * All other items sell for SELL_PERCENT of their buy value (min 1 gold).
     */
    public void sellItem(Item item, Player player) {
        if (item.getName().equalsIgnoreCase("Forest Gem")) {
            int gemGold = 50 + (int)(Math.random() * 51); // 50–100
            player.getInventory().removeItem(item);
            player.addGold(gemGold);
            System.out.println("\nGriswold's eyes go wide as saucers.");
            System.out.println("\"By the old gods... a Forest Gem! I haven't laid eyes on");
            System.out.println("  one of these in thirty years! They're born from centuries");
            System.out.println("  of pure grove magic — utterly irreplaceable!\"");
            System.out.println("\"I'll give you " + gemGold + " gold. That's a generous sum");
            System.out.println("  and you know it. Deal?\"");
            System.out.println("\nSold: Forest Gem for " + gemGold + " gold.\n");
            return;
        }
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
        if (levelRequirement > 0 && player.getLevel() < levelRequirement) {
            return greeting + "\n\n  \"Come back when you are level " + levelRequirement
                    + ". My stock is reserved for those who have proven themselves.\"";
        }
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
