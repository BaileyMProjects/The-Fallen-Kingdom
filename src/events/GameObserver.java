package events;

/**
 * GameObserver — Observer pattern interface (behavioural).
 *
 * Any class that wants to react to game events implements this interface
 * and registers itself with EventManager.
 *
 * Current observers:
 *   QuestManager   — updates quest progress on ENEMY_DEFEATED / PUZZLE_SOLVED
 *   QuestObserver  — prints quest start and completion notifications
 *   PlayerObserver — tracks kill count and player-related statistics
 *   GuiObserver    — switches GUI panels on COMBAT_STARTED / COMBAT_ENDED
 *                    (added in Batch 12)
 *
 * WHY THIS PATTERN:
 *   Without Observer, CombatSystem would need direct references to
 *   QuestManager, the GUI, and any other system that cares about combat
 *   results.  Each new system that needs to know about a combat outcome
 *   would require editing CombatSystem.  With Observer, CombatSystem
 *   fires one event and knows nothing about who receives it.
 */
public interface GameObserver {

    /**
     * Called by EventManager whenever a game event occurs.
     *
     * Implementations should check {@code event.getType()} and ignore
     * event types they are not interested in.
     *
     * @param event the event that occurred (never null)
     */
    void onEvent(GameEvent event);
}
