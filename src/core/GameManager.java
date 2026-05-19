package core;

/**
 * GameManager — Singleton design pattern (creational).
 *
 * Guarantees that exactly one instance of the game engine exists for the
 * entire lifetime of the application.  It acts as the central coordinator:
 * it creates the Game object and provides a controlled global access point
 * so that other subsystems can reach shared state without scattering static
 * fields across the codebase.
 *
 * WHY SINGLETON HERE:
 *   A text game has exactly one running game at a time.  If multiple Game
 *   instances could exist simultaneously, they would produce contradictory
 *   world and player state.  Singleton prevents this structurally.
 */
public class GameManager {

    /** The one shared instance — null until the first call to getInstance(). */
    private static GameManager instance;

    /** The currently active game session. */
    private Game game;

    // -------------------------------------------------------------------------
    // Construction (private — enforces the Singleton contract)
    // -------------------------------------------------------------------------

    private GameManager() {}

    // -------------------------------------------------------------------------
    // Singleton accessor
    // -------------------------------------------------------------------------

    /**
     * Returns the single GameManager instance, creating it lazily on first call.
     *
     * This is the standard lazy-initialisation Singleton.  It is not
     * thread-safe; for a single-threaded text game that is intentional — the
     * simpler form is easier to read and audit.
     *
     * @return the shared GameManager instance (never null after this call)
     */
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * Resets the singleton reference to null.
     *
     * ONLY intended for use in unit tests so that each test can start from a
     * clean state without the previous game session leaking in.
     */
    public static void resetInstance() {
        instance = null;
    }

    // -------------------------------------------------------------------------
    // Game lifecycle
    // -------------------------------------------------------------------------

    /**
     * Creates a fresh Game session and starts it.
     * Calling this a second time discards the previous game and starts over.
     */
    public void newGame() {
        game = new Game();
        game.start();
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the active Game, or {@code null} if {@link #newGame()} has not
     * yet been called.
     */
    public Game getGame() {
        return game;
    }
}
