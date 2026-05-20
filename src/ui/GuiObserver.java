package ui;

import characters.Enemy;
import characters.Player;
import core.Game;
import events.GameEvent;
import events.GameObserver;
import util.AsciiArtLoader;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.lang.reflect.InvocationTargetException;

/**
 * GuiObserver — bridges the game event system and the Swing GUI.
 *
 * Implements the Observer pattern (GameObserver interface) and is registered
 * with EventManager before the game starts so it receives every event.
 *
 * Key reactions:
 *   COMBAT_STARTED → load enemy ASCII art, show CombatPanel, start HP timer
 *   COMBAT_ENDED   → stop HP timer, restore ExplorationPanel, refresh sidebar
 *   ENEMY_DEFEATED → do a final HP bar refresh
 *   PLAYER_LEVEL_UP / QUEST_* → refresh the exploration sidebar
 *
 * The HP timer polls player and enemy health every 400 ms while in combat.
 * This is simpler than adding per-damage events and sufficient for the
 * turn-based pace of the game.
 *
 * All Swing mutations are dispatched via SwingUtilities.invokeLater() inside
 * the panel methods, so the game thread never touches Swing state directly.
 */
public class GuiObserver implements GameObserver {

    private final GameWindow       gameWindow;
    private final ExplorationPanel explorationPanel;
    private final CombatPanel      combatPanel;
    private final Game             game;

    private Enemy currentEnemy;
    private Timer hpTimer;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public GuiObserver(GameWindow gameWindow, ExplorationPanel explorationPanel,
                       CombatPanel combatPanel, Game game) {
        this.gameWindow       = gameWindow;
        this.explorationPanel = explorationPanel;
        this.combatPanel      = combatPanel;
        this.game             = game;
    }

    // -------------------------------------------------------------------------
    // Observer contract
    // -------------------------------------------------------------------------

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {

            case COMBAT_STARTED:
                if (event.getContext() instanceof Enemy) {
                    currentEnemy = (Enemy) event.getContext();
                    String art = AsciiArtLoader.load(currentEnemy.getName());
                    combatPanel.setAsciiArt(art);
                    combatPanel.setEnemyName(currentEnemy.getName());
                    refreshHpBars();
                    startHpTimer();
                }
                gameWindow.switchToCombat();
                break;

            case COMBAT_ENDED:
                stopHpTimer();
                currentEnemy = null;
                gameWindow.switchToExplore();
                refreshSidebar();
                break;

            case ENEMY_DEFEATED:
                refreshHpBars();
                refreshSidebar();
                break;

            case PLAYER_LEVEL_UP:
                refreshSidebar();
                break;

            case QUEST_STARTED:
            case QUEST_COMPLETED:
                refreshSidebar();
                break;

            case LOCATION_CHANGED:
                // invokeAndWait blocks the game thread until the clear is done,
                // guaranteeing the new location text prints onto a clean screen.
                try {
                    SwingUtilities.invokeAndWait(() -> explorationPanel.clearScreen());
                } catch (InvocationTargetException | InterruptedException e) {
                    SwingUtilities.invokeLater(() -> explorationPanel.clearScreen());
                }
                break;

            default:
                break;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void refreshHpBars() {
        Player player = game.getPlayer();
        if (player == null || currentEnemy == null) return;
        combatPanel.updateHpBars(
                player.getHealth(),    player.getMaxHealth(),
                currentEnemy.getHealth(), currentEnemy.getMaxHealth());
    }

    private void refreshSidebar() {
        Player player = game.getPlayer();
        if (player == null) return;
        explorationPanel.updateStats(player.getStatsDisplay());
        explorationPanel.updateQuests(game.getQuestManager().getQuestLog());
    }

    /** Start a Swing Timer that keeps the HP bars current during combat. */
    private void startHpTimer() {
        if (hpTimer != null && hpTimer.isRunning()) return;
        hpTimer = new Timer(400, e -> refreshHpBars());
        hpTimer.start();
    }

    private void stopHpTimer() {
        if (hpTimer != null) {
            hpTimer.stop();
            hpTimer = null;
        }
    }
}
