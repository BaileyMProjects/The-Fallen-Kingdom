package events;

/**
 * GameEventType — every distinct event that can occur in the game.
 *
 * Used as the key inside GameEvent so observers can filter only the events
 * they care about with a clean switch statement.
 *
 * COMBAT_STARTED and COMBAT_ENDED are included here for use by the GUI
 * (Batch 12): the combat panel subscribes to these events to know when to
 * switch screens and which enemy ASCII art to display.
 */
public enum GameEventType {

    // ── Combat ────────────────────────────────────────────────────────────
    /** Fired by CombatSystem when a fight begins. Context = Enemy object. */
    COMBAT_STARTED,

    /** Fired by CombatSystem when a fight ends (win, loss, or flee). */
    COMBAT_ENDED,

    /** Fired by CombatSystem after the enemy's HP reaches zero. */
    ENEMY_DEFEATED,

    // ── Quests ────────────────────────────────────────────────────────────
    /** Fired by QuestManager when a quest moves from NOT_STARTED → IN_PROGRESS. */
    QUEST_STARTED,

    /** Fired by QuestManager when a quest reaches COMPLETED status. */
    QUEST_COMPLETED,

    // ── Player ────────────────────────────────────────────────────────────
    /** Fired when the player gains enough experience to level up. */
    PLAYER_LEVEL_UP,

    // ── World ─────────────────────────────────────────────────────────────
    /** Fired when the player successfully solves a puzzle. */
    PUZZLE_SOLVED,

    /** Fired when the player talks to any NPC. Subject = NPC name (lowercase). */
    TALKED_TO_NPC,

    /** Fired immediately before displaying a new location, so the GUI can clear the screen. */
    LOCATION_CHANGED
}
