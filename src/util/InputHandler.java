package util;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * InputHandler — the single point of contact between the game and player input.
 *
 * Supports two modes transparently:
 *
 *   Console mode (default):
 *     Reads directly from System.in via Scanner.  The game thread blocks on
 *     readInput() until the player presses Enter.
 *
 *   GUI mode (enabled in Batch 12):
 *     The Scanner is not used.  Instead the GUI calls provide(String) when
 *     the player submits text, which places the input into a BlockingQueue.
 *     readInput() blocks on queue.take() until something arrives.
 *     This means the game thread and the Swing EDT never share state directly.
 *
 * Switching modes requires a single call to enableGuiMode() — no other class
 * needs to change.  This is the same interface regardless of which mode is
 * active, demonstrating the Open/Closed Principle.
 */
public class InputHandler {

    private final Scanner              scanner;
    private final BlockingQueue<String> guiInputQueue;
    private       boolean              guiMode;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public InputHandler() {
        this.scanner       = new Scanner(System.in);
        this.guiInputQueue = new LinkedBlockingQueue<>();
        this.guiMode       = false;
    }

    // -------------------------------------------------------------------------
    // Mode switching
    // -------------------------------------------------------------------------

    /**
     * Switches InputHandler into GUI mode.
     * After this call, readInput() blocks on the queue instead of System.in.
     * Called by GameWindow during GUI initialisation (Batch 12).
     */
    public void enableGuiMode() {
        this.guiMode = true;
    }

    // -------------------------------------------------------------------------
    // Input reading
    // -------------------------------------------------------------------------

    /**
     * Reads and returns the next line of player input, trimmed of whitespace.
     *
     * In console mode: blocks until the player presses Enter.
     * In GUI mode:     blocks until the GUI calls provide().
     *
     * Never returns null — returns an empty string on interrupt or EOF.
     */
    public String readInput() {
        if (guiMode) {
            try {
                return guiInputQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "";
            }
        }
        // Console mode
        if (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            return line.isEmpty() ? readInput() : line;   // skip blank lines
        }
        return "";
    }

    /**
     * Prompts the player for their character name and returns it.
     * Falls back to "Adventurer" if the input is blank.
     */
    public String promptName() {
        System.out.println("What is your name, adventurer?");
        if (!guiMode) System.out.print("> ");
        String name = readInput().trim();
        return name.isEmpty() ? "Adventurer" : name;
    }

    // -------------------------------------------------------------------------
    // GUI injection
    // -------------------------------------------------------------------------

    /**
     * Delivers a line of text from the GUI to the waiting game thread.
     * Called from the Swing EDT (Batch 12) — thread-safe via BlockingQueue.
     *
     * @param input the player's text exactly as typed (trimmed by the caller)
     */
    public void provide(String input) {
        if (input != null) {
            guiInputQueue.offer(input.trim());
        }
    }
}
