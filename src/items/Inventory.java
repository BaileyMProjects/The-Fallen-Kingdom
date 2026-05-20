package items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Inventory — a container for the items a character is carrying.
 *
 * Inventory is used via composition inside Player: Player HAS-AN Inventory.
 * It deliberately knows nothing about Player or combat — it is purely a
 * managed list of Item objects with helper methods.
 *
 * A maximum capacity is enforced so the player cannot carry unlimited items,
 * adding a light resource-management dimension to the game.
 */
public class Inventory {

    private static final int MAX_CAPACITY = 20;

    private final List<Item> items;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Inventory() {
        this.items = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Mutators
    // -------------------------------------------------------------------------

    /**
     * Adds an item if the inventory is not full.
     *
     * @return true if added successfully, false if the inventory was full
     */
    public boolean addItem(Item item) {
        if (items.size() >= MAX_CAPACITY) {
            System.out.println("Your inventory is full! (max " + MAX_CAPACITY + " items)");
            return false;
        }
        items.add(item);
        return true;
    }

    /**
     * Removes the given item.
     *
     * @return true if the item was present and removed, false otherwise
     */
    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Case-insensitive partial-name search.
     * "sword", "iron sword", and "Iron Sword" all find the same item.
     *
     * @return the first matching item, or {@code null} if not found
     */
    public Item findItem(String name) {
        if (name == null) return null;
        String search = name.toLowerCase().trim();
        return items.stream()
                    .filter(i -> i.getName().toLowerCase().contains(search))
                    .findFirst()
                    .orElse(null);
    }

    public boolean contains(Item item)  { return items.contains(item); }
    public boolean isEmpty()            { return items.isEmpty(); }
    public int     size()               { return items.size(); }
    public int     getCapacity()        { return MAX_CAPACITY; }

    /** Returns a read-only view so callers cannot modify the list directly. */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    /**
     * Prints a numbered list of every item with its description.
     * Called by Game.handleInventory().
     */
    public void listItems() {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            System.out.println("  " + (i + 1) + ". " + item.getName()
                    + " — " + item.getDescription());
        }
        System.out.println("  (" + items.size() + "/" + MAX_CAPACITY + " slots used)");
    }
}
