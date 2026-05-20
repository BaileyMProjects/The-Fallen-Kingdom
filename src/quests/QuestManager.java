package quests;

import characters.Player;
import events.EventManager;
import events.GameEvent;
import events.GameEventType;
import events.GameObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * QuestManager — owns all quests and drives their lifecycle.
 *
 * Implements GameObserver so it receives every game event from EventManager.
 * When an event arrives, QuestManager:
 *   1. Checks whether any NOT_STARTED SideQuest should auto-start.
 *   2. Calls updateProgress() on every IN_PROGRESS quest.
 *   3. Completes any quest whose isComplete() returns true.
 *   4. Awards the gold reward and fires a QUEST_COMPLETED event.
 *
 * NOTE: Game.handleTalk() must fire a TALKED_TO_NPC event via EventManager
 * for "The Elder's Plea" side quest to trigger correctly.  This line is
 * added in Batch 12 when Game.java is updated for the GUI.
 */
public class QuestManager implements GameObserver {

    private final List<Quest>  quests;
    private final EventManager eventManager;
    private       Player       player;   // set in initializeQuests()

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public QuestManager(EventManager eventManager) {
        this.quests       = new ArrayList<>();
        this.eventManager = eventManager;
    }

    // -------------------------------------------------------------------------
    // Initialisation — called from Game.start() once the Player exists
    // -------------------------------------------------------------------------

    /**
     * Creates all quests and auto-starts the main quest.
     * Side quests auto-start on their first matching game event.
     */
    public void initializeQuests(Player player) {
        this.player = player;

        // Main quest — auto-started immediately
        MainQuest mainQuest = new MainQuest();
        mainQuest.start();
        quests.add(mainQuest);

        // Side quest 1: speak with the Village Elder
        quests.add(new SideQuest(
            "The Elder's Plea",
            "The Village Elder carries urgent knowledge about the Shadow Lord.",
            "Talk to the Village Elder.",
            GameEventType.TALKED_TO_NPC,
            "village elder",
            1,
            20    // gold reward
        ));

        // Side quest 2: solve puzzles scattered across the kingdom
        quests.add(new SideQuest(
            "Dungeon Delver",
            "Ancient puzzle mechanisms are hidden throughout the ruined kingdom.",
            "Solve 2 puzzles.",
            GameEventType.PUZZLE_SOLVED,
            null, // any puzzle counts
            2,
            50    // gold reward
        ));

        // Notify that the main quest has begun
        eventManager.notify(new GameEvent(
            GameEventType.QUEST_STARTED, mainQuest.getName()
        ));
    }

    // -------------------------------------------------------------------------
    // Observer implementation
    // -------------------------------------------------------------------------

    @Override
    public void onEvent(GameEvent event) {
        // Use a snapshot to safely add/modify quests inside the loop
        for (Quest quest : new ArrayList<>(quests)) {
            if (quest.isCompleted()) continue;

            // Auto-start side quests whose trigger event just fired
            if (!quest.isInProgress() && quest instanceof SideQuest) {
                SideQuest sq = (SideQuest) quest;
                if (sq.matchesEvent(event)) {
                    quest.start();
                    quest.updateProgress(event);    // first event also counts as progress
                    eventManager.notify(new GameEvent(GameEventType.QUEST_STARTED, quest.getName()));
                    checkAndComplete(quest);
                    continue;
                }
            }

            // Update progress on active quests
            if (quest.isInProgress()) {
                quest.updateProgress(event);
                checkAndComplete(quest);
            }
        }
    }

    /** Completes a quest if its conditions are met and awards the gold reward. */
    private void checkAndComplete(Quest quest) {
        if (quest.isInProgress() && quest.isComplete()) {
            quest.complete();
            eventManager.notify(new GameEvent(GameEventType.QUEST_COMPLETED, quest.getName()));
            if (player != null && quest.getRewardGold() > 0) {
                player.addGold(quest.getRewardGold());
                System.out.println("  Reward: +" + quest.getRewardGold() + " gold!");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Quest log display
    // -------------------------------------------------------------------------

    /**
     * Returns a formatted quest log string shown by the 'quests' command
     * and the GUI sidebar.
     */
    public String getQuestLog() {
        if (quests.isEmpty()) return "No quests available.";

        StringBuilder sb = new StringBuilder("\n--- Quest Log ---\n");
        for (Quest quest : quests) {
            String symbol;
            switch (quest.getStatus()) {
                case COMPLETED:   symbol = "[✓]"; break;
                case IN_PROGRESS: symbol = "[→]"; break;
                default:          symbol = "[ ]"; break;
            }
            sb.append(symbol).append(" ").append(quest.getName());
            if (quest.getRewardGold() > 0 && !quest.isCompleted()) {
                sb.append("  (reward: ").append(quest.getRewardGold()).append("g)");
            }
            sb.append("\n");
            if (quest.isInProgress()) {
                sb.append("      ").append(quest.getProgressDisplay()).append("\n");
            }
        }
        sb.append("-----------------");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public List<Quest> getQuests() {
        return Collections.unmodifiableList(quests);
    }

    public List<Quest> getActiveQuests() {
        return quests.stream()
                     .filter(Quest::isInProgress)
                     .collect(Collectors.toList());
    }

    public boolean isMainQuestComplete() {
        return quests.stream()
                     .filter(q -> q instanceof MainQuest)
                     .anyMatch(Quest::isCompleted);
    }
}
