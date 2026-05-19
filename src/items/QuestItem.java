package items;

import characters.Player;

/**
 * QuestItem — an item that is important to the story but has no combat use.
 *
 * Quest items cannot be sold (value = 0) and cannot be equipped.
 * use() displays lore text, giving the player narrative context when they
 * examine the item.  The Ancient Relic is the main example in this game.
 */
public class QuestItem extends Item {

    private final String loreText;

    public QuestItem(String name, String description, String loreText) {
        super(name, description, 0);   // quest items have no monetary value
        this.loreText = loreText;
    }

    /**
     * Displays the item's lore text — a piece of narrative the player discovers
     * by examining the object.
     */
    @Override
    public void use(Player player) {
        System.out.println("\n--- " + getName() + " ---");
        System.out.println(loreText);
        System.out.println();
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "\n  \"" + loreText + "\"";
    }
}
