package events;

import characters.Player;

/**
 * PlayerObserver — tracks player-related statistics across the game session.
 *
 * Listens for events that are meaningful from the player's perspective:
 * enemies defeated (kill count) and level-up notifications.
 *
 * The kill count is surfaced in the GUI stats panel (Batch 12) so the player
 * can see how many enemies they have defeated in the current session.
 */
public class PlayerObserver implements GameObserver {

    private final Player player;
    private int killCount;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public PlayerObserver(Player player) {
        this.player    = player;
        this.killCount = 0;
    }

    // -------------------------------------------------------------------------
    // Observer implementation
    // -------------------------------------------------------------------------

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {

            case ENEMY_DEFEATED:
                killCount++;
                break;

            case PLAYER_LEVEL_UP:
                // Player.levelUp() already prints the level-up message.
                // This hook exists for the GUI to refresh the stats panel
                // immediately when a level-up occurs (Batch 12).
                break;

            default:
                break;
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public int    getKillCount() { return killCount; }
    public Player getPlayer()    { return player; }
}
