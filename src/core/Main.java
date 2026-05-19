package core;

/**
 * Main — application entry point.
 *
 * Kept intentionally minimal: its only responsibility is to hand control
 * to GameManager, enforcing the Singleton pattern from the very first line
 * of execution.
 */
public class Main {

    public static void main(String[] args) {
        GameManager.getInstance().newGame();
    }
}
