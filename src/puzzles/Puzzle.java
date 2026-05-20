package puzzles;

import characters.Player;
import util.InputHandler;
import world.Location;

/**
 * Puzzle — abstract base class for all interactive puzzles in the game.
 *
 * Each puzzle has a name, a description shown when the player first arrives,
 * and a solved flag that persists for the rest of the session.
 *
 * Concrete subclasses implement attempt(), which is responsible for:
 *   - Presenting the puzzle to the player via System.out
 *   - Reading answers via InputHandler (so the same class works in both
 *     the console and GUI modes without changes)
 *   - Granting rewards by mutating the Location (adding items, unlocking exits)
 *   - Setting solved = true on success
 *
 * Game.handleSolve() calls attempt() and then fires a PUZZLE_SOLVED event
 * if isSolved() returns true afterward (added in Batch 12).
 *
 * OOP principles demonstrated:
 *   Abstraction  — Puzzle cannot be instantiated.
 *   Polymorphism — Game.handleSolve() calls attempt() on any Puzzle subtype.
 *   Encapsulation— solved is protected; only the subclass sets it to true.
 */
public abstract class Puzzle {

    protected final String  name;
    protected final String  description;
    protected       boolean solved;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    protected Puzzle(String name, String description) {
        this.name        = name;
        this.description = description;
        this.solved      = false;
    }

    // -------------------------------------------------------------------------
    // Abstract contract
    // -------------------------------------------------------------------------

    /**
     * Runs one interactive attempt at this puzzle.
     *
     * @param player       the player character (for granting rewards)
     * @param inputHandler used to read player answers
     * @param location     the current location (for adding items or unlocking exits)
     */
    public abstract void attempt(Player player, InputHandler inputHandler, Location location);

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public boolean isSolved()      { return solved; }
    public String  getName()       { return name; }
    public String  getDescription(){ return description; }
}
