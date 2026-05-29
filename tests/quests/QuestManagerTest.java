package quests;

import characters.Player;
import events.EventManager;
import events.GameEvent;
import events.GameEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuestManagerTest — unit tests for the QuestManager (Observer pattern).
 *
 * QuestManager implements GameObserver and must be subscribed to the
 * EventManager before events are fired.  Tests verify that:
 *   - The main quest is auto-started on initializeQuests().
 *   - Side quests auto-start and progress on matching events.
 *   - Quests complete when their conditions are met.
 *   - Gold rewards are transferred to the player on completion.
 *
 * All events are fired through EventManager.notify() to exercise the full
 * Observer-pattern wiring, not by calling onEvent() directly.
 */
class QuestManagerTest {

    private EventManager eventManager;
    private QuestManager questManager;
    private Player       player;

    @BeforeEach
    void setUp() {
        eventManager = new EventManager();
        questManager = new QuestManager(eventManager);
        eventManager.subscribe(questManager);          // wire Observer pattern
        player = new Player("Tester", 100, 10, 5, 0);
        questManager.initializeQuests(player);
    }

    // ── Main quest ────────────────────────────────────────────────────────────

    @Test
    void mainQuest_isInProgressAfterInit() {
        List<Quest> active = questManager.getActiveQuests();
        assertTrue(active.stream().anyMatch(q -> q instanceof MainQuest),
                "MainQuest should be IN_PROGRESS immediately after initializeQuests()");
    }

    @Test
    void mainQuest_completesWhenShadowLordDefeated() {
        eventManager.notify(new GameEvent(
                GameEventType.ENEMY_DEFEATED, "shadow lord"));

        assertTrue(questManager.isMainQuestComplete(),
                "MainQuest should be COMPLETED after ENEMY_DEFEATED(shadow lord)");
    }

    @Test
    void mainQuest_doesNotCompleteForOtherEnemies() {
        eventManager.notify(new GameEvent(
                GameEventType.ENEMY_DEFEATED, "shadow goblin"));

        assertFalse(questManager.isMainQuestComplete(),
                "MainQuest should not complete when a non-boss enemy is defeated");
    }

    // ── The Elder's Plea (side quest) ─────────────────────────────────────────

    @Test
    void elderPlea_startsOnVillageElderTalk() {
        eventManager.notify(new GameEvent(
                GameEventType.TALKED_TO_NPC, "village elder"));

        List<Quest> quests = questManager.getQuests();
        assertTrue(quests.stream()
                         .filter(q -> q.getName().equals("The Elder's Plea"))
                         .anyMatch(q -> q.isInProgress() || q.isCompleted()),
                "The Elder's Plea should start (and may immediately complete) when TALKED_TO_NPC(village elder) fires");
    }

    @Test
    void elderPlea_completesAfterSingleTalk() {
        // The quest requires talking to the elder exactly once (requiredCount = 1)
        eventManager.notify(new GameEvent(
                GameEventType.TALKED_TO_NPC, "village elder"));

        List<Quest> quests = questManager.getQuests();
        Quest elderPlea = quests.stream()
                                .filter(q -> q.getName().equals("The Elder's Plea"))
                                .findFirst()
                                .orElse(null);
        assertNotNull(elderPlea);
        assertTrue(elderPlea.isCompleted(),
                "The Elder's Plea should be completed after one TALKED_TO_NPC event");
    }

    @Test
    void elderPlea_awardsGoldOnCompletion() {
        int goldBefore = player.getGold();
        eventManager.notify(new GameEvent(
                GameEventType.TALKED_TO_NPC, "village elder"));

        assertEquals(goldBefore + 20, player.getGold(),
                "Player should receive 20 gold reward for The Elder's Plea");
    }

    @Test
    void elderPlea_doesNotStartForOtherNpcs() {
        eventManager.notify(new GameEvent(
                GameEventType.TALKED_TO_NPC, "merchant"));

        List<Quest> active = questManager.getActiveQuests();
        assertFalse(active.stream()
                          .anyMatch(q -> q.getName().equals("The Elder's Plea")),
                "The Elder's Plea should not start when talking to a different NPC");
    }

    // ── Dungeon Delver (side quest) ────────────────────────────────────────────

    @Test
    void dungeonDelver_startsOnFirstPuzzleSolve() {
        eventManager.notify(new GameEvent(
                GameEventType.PUZZLE_SOLVED, "Riddle Puzzle"));

        List<Quest> active = questManager.getActiveQuests();
        assertTrue(active.stream()
                         .anyMatch(q -> q.getName().equals("Dungeon Delver")),
                "Dungeon Delver should auto-start on the first PUZZLE_SOLVED event");
    }

    @Test
    void dungeonDelver_doesNotCompleteAfterOnePuzzle() {
        eventManager.notify(new GameEvent(
                GameEventType.PUZZLE_SOLVED, "Riddle Puzzle"));

        List<Quest> quests = questManager.getQuests();
        Quest delver = quests.stream()
                             .filter(q -> q.getName().equals("Dungeon Delver"))
                             .findFirst()
                             .orElse(null);
        assertNotNull(delver);
        assertFalse(delver.isCompleted(),
                "Dungeon Delver requires two puzzles — should not be complete after one");
    }

    @Test
    void dungeonDelver_completesAfterTwoPuzzles() {
        eventManager.notify(new GameEvent(GameEventType.PUZZLE_SOLVED, "Riddle Puzzle"));
        eventManager.notify(new GameEvent(GameEventType.PUZZLE_SOLVED, "Lever Puzzle"));

        List<Quest> quests = questManager.getQuests();
        Quest delver = quests.stream()
                             .filter(q -> q.getName().equals("Dungeon Delver"))
                             .findFirst()
                             .orElse(null);
        assertNotNull(delver);
        assertTrue(delver.isCompleted(),
                "Dungeon Delver should be completed after two PUZZLE_SOLVED events");
    }

    @Test
    void dungeonDelver_awardsGoldOnCompletion() {
        int goldBefore = player.getGold();
        eventManager.notify(new GameEvent(GameEventType.PUZZLE_SOLVED, "Riddle Puzzle"));
        eventManager.notify(new GameEvent(GameEventType.PUZZLE_SOLVED, "Lever Puzzle"));

        assertEquals(goldBefore + 50, player.getGold(),
                "Player should receive 50 gold reward for completing Dungeon Delver");
    }

    // ── General ───────────────────────────────────────────────────────────────

    @Test
    void initiallyOneActiveQuest() {
        assertEquals(1, questManager.getActiveQuests().size(),
                "Only the MainQuest should be active before any side-quest triggers fire");
    }

    @Test
    void getQuestLog_containsAllQuestNames() {
        String log = questManager.getQuestLog();
        assertTrue(log.contains("The Elder's Plea"),  "Quest log should list The Elder's Plea");
        assertTrue(log.contains("Dungeon Delver"),     "Quest log should list Dungeon Delver");
    }
}
