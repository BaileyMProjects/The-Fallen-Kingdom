package core;

import javax.swing.SwingUtilities;
import ui.GameWindow;

/**
 * Main — application entry point.
 *
 * Launches the Swing GUI on the Event Dispatch Thread.  GameWindow owns the
 * game lifecycle from here: it creates the Game, enables GUI input mode, and
 * starts the game logic on a daemon thread.
 *
 * The original console path (GameManager.getInstance().newGame()) is still
 * exercised by unit tests, keeping the Singleton demonstration intact.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow().setVisible(true));
    }
}
