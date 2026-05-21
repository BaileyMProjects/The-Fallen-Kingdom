package items;

import characters.Player;

/**
 * Item — abstract base class for every collectible object in the game.
 *
 * All items share:
 *   - A name and description shown to the player.
 *   - A gold value used by the Merchant for buying and selling.
 *   - A use() method whose behaviour is defined by each concrete subclass.
 *
 * The item category (weapon, armour, potion, etc.) is determined by the
 * subclass type rather than a field, so Java's instanceof and polymorphic
 * dispatch handle category-specific logic cleanly.
 *
 * OOP principles demonstrated:
 *   Abstraction   — Item cannot be instantiated directly.
 *   Encapsulation — fields are private; subclasses access them through getters.
 *   Polymorphism  — use() is overridden to produce completely different
 *                   behaviour in each subclass (equip, heal, unlock, examine).
 */
public abstract class Item implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String description;
    private final int    value;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    protected Item(String name, String description, int value) {
        this.name        = name;
        this.description = description;
        this.value       = Math.max(0, value);
    }

    // -------------------------------------------------------------------------
    // Abstract method — each subclass defines what "using" this item does
    // -------------------------------------------------------------------------

    /**
     * Performs the item's primary action.
     * Weapons and Armour equip themselves; Potions heal and consume themselves;
     * Keys and QuestItems display informational text.
     *
     * @param player the player using the item
     */
    public abstract void use(Player player);

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getName()           { return name; }
    public String getDescription()    { return description; }
    public String getRawDescription() { return description; }
    public int    getValue()          { return value; }

    @Override
    public String toString() {
        return name + " [" + value + "g]";
    }
}
