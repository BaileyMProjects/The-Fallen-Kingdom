package items;

import characters.Player;

/**
 * Armour — an equippable item that increases the player's defense rating.
 *
 * An optional missBonus (0.0–1.0) can be applied to any armour piece.
 * When equipped, Player.getEvasionBonus() surfaces this value and
 * CombatSystem adds it on top of the base 10% enemy miss chance.
 * The Evasive Boots use this field (missBonus = 0.20); all other armour
 * pieces default to 0.0.
 */
public class Armour extends Item {

    private final int        defenseBonus;
    private final double     missBonus;
    private final ArmourSlot slot;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Standard armour with no evasion bonus. */
    public Armour(String name, String description, int value, int defenseBonus, ArmourSlot slot) {
        this(name, description, value, defenseBonus, 0.0, slot);
    }

    /** Armour that also grants an additional enemy miss-chance when equipped. */
    public Armour(String name, String description, int value,
                  int defenseBonus, double missBonus, ArmourSlot slot) {
        super(name, description, value);
        this.defenseBonus = defenseBonus;
        this.missBonus    = missBonus;
        this.slot         = slot;
    }

    // -------------------------------------------------------------------------
    // Use
    // -------------------------------------------------------------------------

    @Override
    public void use(Player player) {
        player.equip(this);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int        getDefenseBonus() { return defenseBonus; }
    public ArmourSlot getSlot()         { return slot; }

    /**
     * Additional probability (0.0–1.0) that enemy attacks miss the wearer.
     * 0.0 for ordinary armour; 0.20 for the Evasive Boots.
     */
    public double getMissBonus() { return missBonus; }

    @Override
    public String getDescription() {
        String slotLabel = slot.name().charAt(0) + slot.name().substring(1).toLowerCase();
        String base = super.getDescription()
                + "  [" + slotLabel + "]"
                + "  [+" + defenseBonus + " defense]";
        return missBonus > 0
                ? base + "  [+" + (int)(missBonus * 100) + "% enemy miss chance]"
                : base;
    }
}
