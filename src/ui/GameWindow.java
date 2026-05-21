package ui;

import core.Difficulty;
import core.Game;
import events.GameObserver;
import save.GameSnapshot;
import save.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

/**
 * GameWindow — the root Swing JFrame for The Fallen Kingdom GUI.
 *
 * Responsibilities:
 *   1. Show the MenuPanel on launch so the player can pick a difficulty.
 *   2. Create the Game and enable GUI input mode.
 *   3. Redirect System.out to whichever panel is currently active.
 *   4. Host a CardLayout that switches between MenuPanel, ExplorationPanel,
 *      and CombatPanel as the game progresses.
 *   5. Start the game logic on a daemon thread once difficulty is chosen.
 *   6. Route text-field submissions back into InputHandler's BlockingQueue.
 *
 * The game thread and the Swing EDT never share mutable state directly —
 * all GUI updates are dispatched via SwingUtilities.invokeLater().
 */
public class GameWindow extends JFrame {

    private static final String MENU_CARD    = "MENU";
    private static final String EXPLORE_CARD = "EXPLORE";
    private static final String COMBAT_CARD  = "COMBAT";

    private final Game             game;
    private final CardLayout       cardLayout;
    private final JPanel           cardPanel;
    private final MenuPanel        menuPanel;
    private final ExplorationPanel explorationPanel;
    private final CombatPanel      combatPanel;
    private final TextAreaStream   outputStream;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public GameWindow() {
        super("The Fallen Kingdom");

        // Create game and switch InputHandler to GUI (BlockingQueue) mode
        this.game = new Game();
        this.game.getInputHandler().enableGuiMode();

        // Shared input callback — both gameplay panels call this when player submits
        Consumer<String> onSubmit = text -> game.getInputHandler().provide(text);

        // Build all three screen panels
        this.menuPanel        = new MenuPanel(this::startGame, this::loadGame);
        this.explorationPanel = new ExplorationPanel(onSubmit,
                (cmd, prefix) -> game.getCompletions(cmd, prefix));
        this.combatPanel      = new CombatPanel(onSubmit);

        // CardLayout container — menu is the first card shown
        this.cardLayout = new CardLayout();
        this.cardPanel  = new JPanel(cardLayout);
        cardPanel.add(menuPanel,        MENU_CARD);
        cardPanel.add(explorationPanel, EXPLORE_CARD);
        cardPanel.add(combatPanel,      COMBAT_CARD);

        // Redirect System.out → exploration panel (which manages its own history)
        this.outputStream = new TextAreaStream(explorationPanel::appendText);
        System.setOut(new PrintStream(outputStream, true));

        // Register the GUI observer so it can react to game events
        GuiObserver guiObserver = new GuiObserver(this, explorationPanel, combatPanel, game);
        game.registerObserver(guiObserver);

        // Frame setup — show the menu first; game thread starts after difficulty pick
        setContentPane(cardPanel);
        cardLayout.show(cardPanel, MENU_CARD);
        setSize(960, 660);
        setMinimumSize(new Dimension(720, 520));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // -------------------------------------------------------------------------
    // Difficulty selected — called by MenuPanel on the EDT
    // -------------------------------------------------------------------------

    private void startGame(Difficulty difficulty) {
        game.setDifficulty(difficulty);

        // Switch to the exploration view before the game thread starts printing
        cardLayout.show(cardPanel, EXPLORE_CARD);
        explorationPanel.requestInputFocus();

        Thread gameThread = new Thread(game::start, "game-thread");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    // -------------------------------------------------------------------------
    // Load from save slot — called by MenuPanel on the EDT
    // -------------------------------------------------------------------------

    private void loadGame(int slot) {
        GameSnapshot snapshot = SaveManager.load(slot);
        if (snapshot == null) {
            JOptionPane.showMessageDialog(this,
                    "Save slot " + slot + " could not be loaded.",
                    "Load failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        cardLayout.show(cardPanel, EXPLORE_CARD);
        explorationPanel.requestInputFocus();

        Thread gameThread = new Thread(() -> game.startFromSnapshot(snapshot), "game-thread");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    // -------------------------------------------------------------------------
    // Panel switching — called by GuiObserver on the EDT
    // -------------------------------------------------------------------------

    /** Switch to the exploration view and route System.out there. */
    public void switchToExplore() {
        SwingUtilities.invokeLater(() -> {
            outputStream.setConsumer(explorationPanel::appendText);
            cardLayout.show(cardPanel, EXPLORE_CARD);
            explorationPanel.requestInputFocus();
        });
    }

    /** Switch to the combat view and route System.out to the combat log. */
    public void switchToCombat() {
        SwingUtilities.invokeLater(() -> {
            outputStream.setConsumer(combatPanel::appendText);
            cardLayout.show(cardPanel, COMBAT_CARD);
            combatPanel.requestInputFocus();
        });
    }

    // -------------------------------------------------------------------------
    // Inner class: routes System.out to whatever JTextArea is currently active
    // -------------------------------------------------------------------------

    /**
     * OutputStream whose consumer can be hot-swapped at runtime.
     * All writes are dispatched via invokeLater so the game thread never
     * touches Swing state directly.
     */
    private static class TextAreaStream extends OutputStream {

        private volatile Consumer<String> consumer;

        TextAreaStream(Consumer<String> initial) {
            this.consumer = initial;
        }

        void setConsumer(Consumer<String> c) {
            this.consumer = c;
        }

        @Override
        public void write(int b) {
            write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            final String text = new String(buf, off, len);
            final Consumer<String> c = consumer;
            if (c == null) return;
            SwingUtilities.invokeLater(() -> c.accept(text));
        }
    }
}
