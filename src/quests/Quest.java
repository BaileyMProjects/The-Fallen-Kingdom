package quests;

import events.GameEvent;

/**
 * Quest — abstract base class for all quests in the game.
 *
 * Defines the lifecycle (NOT_STARTED → IN_PROGRESS → COMPLETED) and the
 * contract that each concrete quest must fulfil:
 *
 *   updateProgress(event) — inspect a game event and advance internal state
 *   isComplete()          — true when all completion conditions are met
 *   getProgressDisplay()  — one-line summary for the quest log
 *
 * QuestManager owns all Quest instances and calls these methods when events
 * arrive, keeping quest logic out of the event system itself.
 *
 * OOP principles demonstrated:
 *   Abstraction  — Quest cannot be instantiated directly.
 *   Inheritance  — MainQuest and SideQuest extend this class.
 *   Polymorphism — QuestManager calls updateProgress() on every Quest without
 *                  knowing the concrete type; each subclass handles it differently.
 */
public abstract class Quest {

    protected final String      name;
    protected final String      description;
    protected final String      objective;
    protected final int         rewardGold;
    protected       QuestStatus status;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    protected Quest(String name, String description, String objective, int rewardGold) {
        this.name        = name;
        this.description = description;
        this.objective   = objective;
        this.rewardGold  = rewardGold;
        this.status      = QuestStatus.NOT_STARTED;
    }

    // -------------------------------------------------------------------------
    // Abstract contract — subclasses define their own completion logic
    // -------------------------------------------------------------------------

    /**
     * Inspects the event and updates internal progress counters or flags.
     * Called by QuestManager for every event while the quest is IN_PROGRESS.
     */
    public abstract void updateProgress(GameEvent event);

    /**
     * @return true when all completion conditions tracked by this quest are met
     */
    public abstract boolean isComplete();

    /**
     * @return a short string shown in the quest log, e.g. "2/3 enemies defeated"
     */
    public abstract String getProgressDisplay();

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /** Transitions from NOT_STARTED → IN_PROGRESS. */
    public void start() {
        if (status == QuestStatus.NOT_STARTED) {
            status = QuestStatus.IN_PROGRESS;
        }
    }

    /** Transitions from IN_PROGRESS → COMPLETED. */
    public void complete() {
        status = QuestStatus.COMPLETED;
    }

    // -------------------------------------------------------------------------
    // Status helpers
    // -------------------------------------------------------------------------

    public boolean isInProgress() { return status == QuestStatus.IN_PROGRESS; }
    public boolean isCompleted()  { return status == QuestStatus.COMPLETED; }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String      getName()        { return name; }
    public String      getDescription() { return description; }
    public String      getObjective()   { return objective; }
    public int         getRewardGold()  { return rewardGold; }
    public QuestStatus getStatus()      { return status; }
}
