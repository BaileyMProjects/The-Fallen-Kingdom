package events;

/**
 * GameEvent — an immutable data object describing something that happened.
 *
 * Every event has:
 *   type    — what kind of thing happened (from GameEventType)
 *   subject — the primary name involved (e.g. enemy name, quest name)
 *   context — optional extra data for rich observers (e.g. the Enemy object
 *             itself so the GUI can display its ASCII art on COMBAT_STARTED)
 *
 * Keeping GameEvent immutable means observers can safely share references
 * without risk of one observer mutating the event and confusing another.
 */
public class GameEvent {

    private final GameEventType type;
    private final String        subject;
    private final Object        context;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Convenience constructor for events with no extra context. */
    public GameEvent(GameEventType type, String subject) {
        this(type, subject, null);
    }

    /**
     * Full constructor.
     *
     * @param type    what happened
     * @param subject the primary name (enemy, quest, NPC, etc.)
     * @param context optional object for observers that need more detail
     */
    public GameEvent(GameEventType type, String subject, Object context) {
        this.type    = type;
        this.subject = subject;
        this.context = context;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public GameEventType getType()    { return type; }
    public String        getSubject() { return subject; }

    /**
     * Returns the optional context object.
     * Callers should cast to the expected type after checking the event type,
     * e.g. {@code (Enemy) event.getContext()} when type == COMBAT_STARTED.
     */
    public Object getContext() { return context; }

    @Override
    public String toString() {
        return "GameEvent{type=" + type + ", subject='" + subject + "'}";
    }
}
