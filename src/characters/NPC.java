package characters;

import quests.QuestManager;

/**
 * NPC — a non-hostile character the player can talk to.
 *
 * Each NPC carries an array of dialogue lines.  Calling talk() returns the
 * next line and advances the index.  Once all lines are exhausted the last
 * line repeats, so NPCs always respond to repeated conversations.
 *
 * The QuestManager parameter in talk() allows an NPC to interact with the
 * quest system without NPC knowing about quest internals — the dependency
 * flows inward through the method argument, preserving loose coupling.
 *
 * Merchant is the only NPC subclass because it needs extra shop behaviour;
 * every other NPC (Village Elder, Imprisoned Knight) is a plain NPC instance
 * with different dialogue data.
 */
public class NPC extends Character {

    private final String   description;
    private final String[] dialogues;
    private       int      dialogueIndex;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public NPC(String name, String description, String[] dialogues) {
        super(name, 1, 0, 0);   // NPCs have no meaningful combat stats
        this.description   = description;
        this.dialogues     = (dialogues != null) ? dialogues : new String[]{"..."};
        this.dialogueIndex = 0;
    }

    // -------------------------------------------------------------------------
    // Dialogue
    // -------------------------------------------------------------------------

    /**
     * Returns the current dialogue line and advances to the next one.
     * Stays on the final line once all others have been read.
     *
     * @param player       available for future conditional dialogue extensions
     * @param questManager used by subclasses to start or advance quests
     */
    public String talk(Player player, QuestManager questManager) {
        String line = dialogues[dialogueIndex];
        if (dialogueIndex < dialogues.length - 1) {
            dialogueIndex++;
        }
        return line;
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    @Override
    public String getDescription() {
        return description;
    }
}
