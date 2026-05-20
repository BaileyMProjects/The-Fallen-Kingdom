package quests;

import events.GameEvent;
import events.GameEventType;

/**
 * SideQuest — a configurable optional quest driven by a single event type.
 *
 * Each SideQuest tracks how many times a given GameEventType fires (optionally
 * filtered by subject string) and completes when the count reaches the target.
 *
 * Current side quests defined in QuestManager:
 *   "The Elder's Plea"  — talk to the Village Elder (1× TALKED_TO_NPC)
 *   "Dungeon Delver"    — solve 2 puzzles (2× PUZZLE_SOLVED, any subject)
 *
 * Auto-start behaviour:
 *   A SideQuest auto-starts the first time a matching event is received while
 *   the quest is still NOT_STARTED.  QuestManager calls matchesEvent() to
 *   detect this and then calls start() + updateProgress() together so the
 *   triggering event also counts as the first unit of progress.
 */
public class SideQuest extends Quest {

    private final GameEventType triggerType;
    private final String        requiredSubject;   // null = match any subject
    private final int           requiredCount;
    private       int           currentCount;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param name            display name shown in the quest log
     * @param description     narrative description
     * @param objective       one-line goal shown while in progress
     * @param triggerType     the event type that advances this quest
     * @param requiredSubject subject the event must contain, or null for any
     * @param requiredCount   how many matching events are needed to complete
     * @param rewardGold      gold awarded on completion
     */
    public SideQuest(String name, String description, String objective,
                     GameEventType triggerType, String requiredSubject,
                     int requiredCount, int rewardGold) {
        super(name, description, objective, rewardGold);
        this.triggerType     = triggerType;
        this.requiredSubject = requiredSubject;
        this.requiredCount   = Math.max(1, requiredCount);
        this.currentCount    = 0;
    }

    // -------------------------------------------------------------------------
    // Quest contract
    // -------------------------------------------------------------------------

    @Override
    public void updateProgress(GameEvent event) {
        if (status == QuestStatus.COMPLETED) return;
        if (matchesEvent(event)) {
            currentCount++;
        }
    }

    @Override
    public boolean isComplete() {
        return currentCount >= requiredCount;
    }

    @Override
    public String getProgressDisplay() {
        return currentCount + "/" + requiredCount + "  " + objective;
    }

    // -------------------------------------------------------------------------
    // Auto-start check
    // -------------------------------------------------------------------------

    /**
     * Returns true if this event matches the quest's trigger type and subject.
     * Used by QuestManager to auto-start side quests on their first relevant event.
     */
    public boolean matchesEvent(GameEvent event) {
        if (event.getType() != triggerType) return false;
        if (requiredSubject == null)        return true;
        return event.getSubject() != null
            && event.getSubject().toLowerCase().contains(requiredSubject.toLowerCase());
    }

    // -------------------------------------------------------------------------
    // Getters (for testing and GUI display)
    // -------------------------------------------------------------------------

    public int getCurrentCount()  { return currentCount; }
    public int getRequiredCount() { return requiredCount; }
}
