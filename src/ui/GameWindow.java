package ui;

import core.Game;
import events.GameObserver;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

/**
 * GameWindow — the root Swing JFrame for The Fallen Kingdom GUI.
 *
 * Responsibilities:
 *   1. Create the Game and enable GUI input mode.
 *   2. Redirect System.out to whichever panel is currently active.
 *   3. Host a CardLayout that switches between ExplorationPanel and
 *      CombatPanel when COMBAT_STARTED / COMBAT_ENDED events fire.
 *   4. Start the game logic on a daemon thread so Swing remains responsive.
 *   5. Route text-field submissions back into InputHandler's BlockingQueue.
 *
 * The game thread and the Swing EDT never share mutable state directly —
 * all GUI updates are dispatched via SwingUtilities.invokeLater().
 */
public class GameWindow extends JFrame {

    private static final String EXPLORE_CARD = "EXPLORE";
    private static final String COMBAT_CARD  = "COMBAT";

    private final Game             game;
    private final CardLayout       cardLayout;
    private final JPanel           cardPanel;
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

        // Shared input callback — both panels call this when the player submits
        Consumer<String> onSubmit = text -> game.getInputHandler().provide(text);

        // Build the two screen panels
        this.explorationPanel = new ExplorationPanel(onSubmit);
        this.combatPanel      = new CombatPanel(onSubmit);

        // CardLayout container
        this.cardLayout = new CardLayout();
        this.cardPanel  = new JPanel(cardLayout);
        cardPanel.add(explorationPanel, EXPLORE_CARD);
        cardPanel.add(combatPanel,      COMBAT_CARD);

        // Redirect System.out → exploration panel (which manages its own history)
        this.outputStream = new TextAreaStream(explorationPanel::appendText);
        System.setOut(new PrintStream(outputStream, true));

        // Register the GUI observer so it can react to game events
        GuiObserver guiObserver = new GuiObserver(this, explorationPanel, combatPanel, game);
        game.registerObserver(guiObserver);

        // Frame setup
        setContentPane(cardPanel);
        setSize(960, 660);
        setMinimumSize(new Dimension(720, 520));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Game logic runs on a daemon thread — blocks on BlockingQueue for input
        Thread gameThread = new Thread(game::start, "game-thread");
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
            JTextArea combatArea = combatPanel.getOutputArea();
            outputStream.setConsumer(text -> {
                combatArea.append(text);
                combatArea.setCaretPosition(combatArea.getDocument().getLength());
            });
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
