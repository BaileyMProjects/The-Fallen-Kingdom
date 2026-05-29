package items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Inventory — a container for the items a character is carrying.
 *
 * Stackable items (Potion, CombatPotion, MaterialItem) collapse into a
 * single display entry with a count prefix ("3x Health Potion"). They share
 * one capacity slot regardless of how many are held. Non-stackable items
 * (Weapon, Armour, Key, QuestItem) each occupy their own slot.
 *
 * Internally the list stores every item object separately — this keeps
 * save/load trivially compatible and lets removeItem() work by reference.
 * All display and counting logic aggregates on the fly.
 */
public class Inventory implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

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
     * Adds an item.  Stackable items that already exist in the inventory do not
     * consume an extra slot; the first instance of a stackable type uses one slot.
     *
     * @return true if added successfully, false if the inventory was full
     */
    public boolean addItem(Item item) {
        if (item.isStackable() && findItem(item.getName()) != null) {
            // Already have at least one — stacks don't use additional slots
            items.add(item);
            return true;
        }
        if (size() >= MAX_CAPACITY) {
            System.out.println("Your inventory is full! (max " + MAX_CAPACITY + " slots)");
            return false;
        }
        items.add(item);
        return true;
    }

    /**
     * Removes one instance of the item (by object reference).
     * For stacked items this decrements the count by one.
     */
    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Case-insensitive partial-name search.
     * Returns the first matching item object (the stack representative for stackables).
     */
    public Item findItem(String name) {
        if (name == null) return null;
        String search = name.toLowerCase().trim();
        return items.stream()
                    .filter(i -> i.getName().toLowerCase().contains(search))
                    .findFirst()
                    .orElse(null);
    }

    /** How many copies of this item are currently held (always 1 for non-stackable). */
    public int getCount(Item item) {
        String lower = item.getName().toLowerCase();
        return (int) items.stream()
                          .filter(i -> i.getName().toLowerCase().equals(lower))
                          .count();
    }

    public boolean contains(Item item) { return items.contains(item); }
    public boolean isEmpty()           { return items.isEmpty(); }

    /**
     * Number of distinct inventory slots occupied.
     * Every unique stackable item name counts as one slot regardless of quantity.
     */
    public int size() {
        Set<String> stackableNames = new HashSet<>();
        int count = 0;
        for (Item item : items) {
            if (item.isStackable()) {
                if (stackableNames.add(item.getName().toLowerCase())) count++;
            } else {
                count++;
            }
        }
        return count;
    }

    public int getCapacity() { return MAX_CAPACITY; }

    /** Returns a read-only view so callers cannot modify the list directly. */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    /**
     * Prints a numbered list of every item, grouping stackable items with an
     * "Nx" count prefix.  Called by Game.handleInventory().
     */
    public void listItems() {
        if (items.isEmpty()) {
            System.out.println("  (empty)");
            return;
        }
        int idx = 1;
        Set<String> shownStackables = new HashSet<>();
        for (Item item : items) {
            if (item.isStackable()) {
                String key = item.getName().toLowerCase();
                if (!shownStackables.add(key)) continue; // already printed this stack
                int count = getCount(item);
                String prefix = count > 1 ? count + "x " : "";
                System.out.println("  " + idx++ + ". " + prefix + item.getName()
                        + " — " + item.getDescription());
            } else {
                System.out.println("  " + idx++ + ". " + item.getName()
                        + " — " + item.getDescription());
            }
        }
        System.out.println("  (" + size() + "/" + MAX_CAPACITY + " slots used)");
    }

    /**
     * Prints only consumable items (Potion + CombatPotion) with stack counts.
     * Used by CombatSystem's "use item" menu so weapons never appear there.
     */
    public void listConsumables() {
        int idx = 1;
        Set<String> shown = new HashSet<>();
        boolean any = false;
        for (Item item : items) {
            if (!(item instanceof Potion) && !(item instanceof CombatPotion)) continue;
            if (item.isStackable()) {
                if (!shown.add(item.getName().toLowerCase())) continue;
            }
            any = true;
            int count = getCount(item);
            String prefix = count > 1 ? count + "x " : "";
            System.out.println("  " + idx++ + ". " + prefix + item.getName()
                    + " — " + item.getDescription());
        }
        if (!any) System.out.println("  (no consumables)");
    }
}
