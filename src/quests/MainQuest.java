package quests;

import events.GameEvent;
import events.GameEventType;

/**
 * MainQuest — "Recover the Ancient Relic".
 *
 * The primary objective of the game.  Auto-started by QuestManager at the
 * beginning of every session so the player always has a clear goal.
 *
 * Completion condition: an ENEMY_DEFEATED event whose subject contains
 * "shadow lord" (case-insensitive).  This is fired by CombatSystem after
 * the Shadow Lord's HP reaches zero.
 *
 * No gold reward — winning the game is its own reward.
 */
public class MainQuest extends Quest {

    private boolean shadowLordDefeated;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public MainQuest() {
        super(
            "Recover the Ancient Relic",
            "The Shadow Lord has shattered the Ancient Relic and plunged the kingdom " +
            "into darkness. Only by defeating him can the Relic be recovered and " +
            "peace restored.",
            "Defeat the Shadow Lord in the Shadow Throne Room.",
            0    // no gold — the victory condition IS the reward
        );
        this.shadowLordDefeated = false;
    }

    // -------------------------------------------------------------------------
    // Quest contract
    // -------------------------------------------------------------------------

    @Override
    public void updateProgress(GameEvent event) {
        if (event.getType() == GameEventType.ENEMY_DEFEATED
                && event.getSubject().toLowerCase().contains("shadow lord")) {
            shadowLordDefeated = true;
        }
    }

    @Override
    public boolean isComplete() {
        return shadowLordDefeated;
    }

    @Override
    public String getProgressDisplay() {
        return shadowLordDefeated
            ? "[✓] Shadow Lord defeated"
            : "[ ] Defeat the Shadow Lord";
    }
}
