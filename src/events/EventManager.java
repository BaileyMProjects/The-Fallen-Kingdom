package events;

import java.util.ArrayList;
import java.util.List;

/**
 * EventManager — the Subject (publisher) in the Observer pattern.
 *
 * Maintains a list of GameObserver subscribers and broadcasts GameEvent
 * objects to all of them when something significant happens in the game.
 *
 * Pattern: Observer (behavioural)
 * Role   : Subject / Publisher
 * Observers registered at runtime: QuestManager, QuestObserver,
 *          PlayerObserver, and GuiObserver (Batch 12).
 *
 * HOW IT WORKS:
 *   1. Systems that want to react to events call subscribe().
 *   2. Systems that produce events call notify() with a GameEvent.
 *   3. EventManager forwards the event to every registered observer.
 *   4. Each observer decides independently whether to act on it.
 *
 * This means CombatSystem, World, and Puzzle classes only depend on
 * EventManager — they never need references to QuestManager or the GUI.
 *
 * AI NOTE:
 *   The structure of this class was implemented with assistance from
 *   Claude Code (Anthropic). See REPORT.md Section 2 for full details.
 */
public class EventManager {

    private final List<GameObserver> observers;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public EventManager() {
        this.observers = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Subscription management
    // -------------------------------------------------------------------------

    /**
     * Registers an observer to receive future events.
     * Silently ignores duplicate registrations so callers do not need to
     * track whether they have already subscribed.
     */
    public void subscribe(GameObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Removes a previously registered observer.
     * Has no effect if the observer was not registered.
     */
    public void unsubscribe(GameObserver observer) {
        observers.remove(observer);
    }

    // -------------------------------------------------------------------------
    // Event dispatch
    // -------------------------------------------------------------------------

    /**
     * Broadcasts the given event to every registered observer.
     *
     * A snapshot copy of the observer list is used so that an observer
     * calling subscribe() or unsubscribe() inside onEvent() does not
     * cause a ConcurrentModificationException.
     *
     * @param event the event to broadcast (ignored if null)
     */
    public void notify(GameEvent event) {
        if (event == null) return;

        // Iterate over a snapshot to allow safe mutation during dispatch
        List<GameObserver> snapshot = new ArrayList<>(observers);
        for (GameObserver observer : snapshot) {
            observer.onEvent(event);
        }
    }

    // -------------------------------------------------------------------------
    // Accessors (for testing)
    // -------------------------------------------------------------------------

    /** Returns the number of currently registered observers. */
    public int getObserverCount() {
        return observers.size();
    }
}
