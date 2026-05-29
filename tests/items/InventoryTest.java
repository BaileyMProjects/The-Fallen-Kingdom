package items;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InventoryTest — unit tests for the Inventory class.
 *
 * Tests cover: capacity enforcement, partial/case-insensitive search,
 * add/remove correctness, and size tracking.  All tests use concrete
 * Item subclasses (Weapon, Potion) created through ItemFactory to avoid
 * coupling the test to private constructors.
 */
class InventoryTest {

    private Inventory inventory;
    private Item      sword;
    private Item      potion;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        sword     = ItemFactory.create(ItemType.IRON_SWORD);
        potion    = ItemFactory.create(ItemType.HEALTH_POTION);
    }

    // ── addItem ──────────────────────────────────────────────────────────────

    @Test
    void addItem_returnsTrueWhenSpaceAvailable() {
        assertTrue(inventory.addItem(sword),
                "addItem should return true when the inventory has free slots");
    }

    @Test
    void addItem_itemIsActuallyContained() {
        inventory.addItem(sword);
        assertTrue(inventory.contains(sword));
    }

    @Test
    void addItem_returnsFalseWhenFull() {
        // Fill to MAX_CAPACITY (20) using fresh non-stackable items (one slot each)
        for (int i = 0; i < inventory.getCapacity(); i++) {
            inventory.addItem(ItemFactory.create(ItemType.IRON_SWORD));
        }
        assertFalse(inventory.addItem(sword),
                "addItem should return false once the inventory is at capacity");
    }

    @Test
    void addItem_sizeDoesNotExceedCapacity() {
        for (int i = 0; i <= inventory.getCapacity(); i++) {
            inventory.addItem(ItemFactory.create(ItemType.IRON_SWORD));
        }
        assertEquals(inventory.getCapacity(), inventory.size());
    }

    // ── removeItem ────────────────────────────────────────────────────────────

    @Test
    void removeItem_returnsTrueWhenPresent() {
        inventory.addItem(sword);
        assertTrue(inventory.removeItem(sword));
    }

    @Test
    void removeItem_returnsFalseWhenAbsent() {
        assertFalse(inventory.removeItem(sword),
                "Removing an item never added should return false");
    }

    @Test
    void removeItem_decreasesSize() {
        inventory.addItem(sword);
        inventory.addItem(potion);
        inventory.removeItem(sword);
        assertEquals(1, inventory.size());
    }

    // ── findItem ─────────────────────────────────────────────────────────────

    @Test
    void findItem_byExactName() {
        inventory.addItem(sword);
        assertNotNull(inventory.findItem("Iron Sword"));
    }

    @Test
    void findItem_byPartialName() {
        inventory.addItem(sword);
        // "sword" is a substring of "Iron Sword"
        Item found = inventory.findItem("sword");
        assertNotNull(found, "findItem should match a partial name");
        assertEquals("Iron Sword", found.getName());
    }

    @Test
    void findItem_isCaseInsensitive() {
        inventory.addItem(sword);
        assertNotNull(inventory.findItem("IRON SWORD"));
        assertNotNull(inventory.findItem("iron sword"));
        assertNotNull(inventory.findItem("Iron Sword"));
    }

    @Test
    void findItem_returnsNullWhenNotFound() {
        inventory.addItem(sword);
        assertNull(inventory.findItem("nonexistent item"),
                "findItem should return null when no item matches");
    }

    // ── isEmpty / size ────────────────────────────────────────────────────────

    @Test
    void isEmpty_trueOnCreation() {
        assertTrue(inventory.isEmpty());
    }

    @Test
    void isEmpty_falseAfterAdd() {
        inventory.addItem(potion);
        assertFalse(inventory.isEmpty());
    }

    @Test
    void size_incrementsOnAdd() {
        assertEquals(0, inventory.size());
        inventory.addItem(sword);
        assertEquals(1, inventory.size());
        inventory.addItem(potion);
        assertEquals(2, inventory.size());
    }

    @Test
    void getCapacity_returns20() {
        assertEquals(20, inventory.getCapacity());
    }
}
