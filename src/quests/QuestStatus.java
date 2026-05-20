package quests;

/**
 * QuestStatus — the three lifecycle states a quest can be in.
 *
 * Quests always begin as NOT_STARTED and progress linearly.
 * QuestManager checks this status to avoid double-completing a quest
 * and to filter the quest log display.
 */
public enum QuestStatus {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED  ("Completed");

    private final String displayName;

    QuestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
