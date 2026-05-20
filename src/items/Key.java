package items;

import characters.Player;

/**
 * Key — a special item that unlocks a locked exit.
 *
 * Keys are not consumed by use(); the actual unlock happens automatically in
 * World.movePlayer() when the player walks into the locked door while holding
 * the key.  Calling use() simply reminds the player how keys work.
 */
public class Key extends Item {

    public Key(String name, String description) {
        super(name, description, 0);   // keys have no sell value
    }

    /**
     * Keys unlock doors automatically when you walk into them.
     * This method just provides a hint rather than doing nothing silently.
     */
    @Override
    public void use(Player player) {
        System.out.println("The " + getName() + " looks like it unlocks something nearby.");
        System.out.println("Move toward a locked door to use it automatically.");
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [unlocks a locked door]";
    }
}
