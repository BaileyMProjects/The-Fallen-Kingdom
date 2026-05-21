package items;

import characters.Player;

/**
 * MaterialItem — a collectible crafting material with a monetary value.
 *
 * Unlike QuestItem (which is always worth 0 gold), MaterialItem can be bought
 * from a merchant and sold back at the standard 50% rate. It has no direct
 * use in combat — it is consumed by NPCs such as the Enchanter during
 * crafting interactions.
 */
public class MaterialItem extends Item {

    private final String loreText;

    public MaterialItem(String name, String description, String loreText, int value) {
        super(name, description, value);
        this.loreText = loreText;
    }

    @Override
    public void use(Player player) {
        System.out.println("\n--- " + getName() + " ---");
        System.out.println(loreText);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "\n  \"" + loreText + "\"";
    }
}
