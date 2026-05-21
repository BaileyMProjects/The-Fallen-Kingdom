package puzzles;

import characters.Player;
import items.Item;
import util.InputHandler;
import world.Direction;
import world.Location;

/**
 * LeverPuzzle — a mechanical sequence puzzle with numbered levers.
 *
 * The number of levers and correct sequence are fully configurable so the
 * same class can drive both the 3-lever dungeon puzzle and the 4-lever
 * Cursed Archives puzzle.
 *
 * Behaviour:
 *   - The player enters lever numbers one at a time.
 *   - Progress is shown after each pull.
 *   - A wrong lever at any point resets the sequence with feedback.
 *   - The player can type 'quit' to stop attempting.
 *   - On success, any configured reward item is added to the location and any
 *     configured exit direction is unlocked.
 */
public class LeverPuzzle extends Puzzle {

    private final int[]     correctSequence;
    private final int       leverCount;   // total levers in this puzzle (derived from max value)
    private final String    clue;
    private final Item      rewardItem;
    private final Direction unlockDirection;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Fully-configurable constructor — name, description, clue, and lever count
     * are all supplied by the caller.  Used for the Cursed Archives puzzle.
     */
    public LeverPuzzle(String name, String description, String clue,
                       int[] correctSequence, Item rewardItem, Direction unlockDirection) {
        super(name, description);
        this.correctSequence  = correctSequence;
        this.clue             = clue;
        this.rewardItem       = rewardItem;
        this.unlockDirection  = unlockDirection;
        int max = 0;
        for (int v : correctSequence) if (v > max) max = v;
        this.leverCount = max;
    }

    /**
     * Convenience constructor matching the original 3-argument signature used
     * by the Underground Dungeon puzzle — no name/description/clue needed.
     */
    public LeverPuzzle(int[] correctSequence, Item rewardItem, Direction unlockDirection) {
        this(
            "Lever Puzzle",
            "Three heavy iron levers protrude from the wall, each engraved with a number.",
            "Follow the moonlight: waxing, waning, half.",
            correctSequence, rewardItem, unlockDirection
        );
    }

    // -------------------------------------------------------------------------
    // Puzzle contract
    // -------------------------------------------------------------------------

    @Override
    public void attempt(Player player, InputHandler inputHandler, Location location) {
        System.out.println("\n=== " + getName() + " ===");
        System.out.println(description);
        System.out.println("\nAn inscription carved beneath them reads:");
        System.out.println("  \"" + clue + "\"\n");
        System.out.println("Pull the levers 1-" + leverCount + " in the correct order.");
        System.out.println("(Type the lever number, or 'quit' to stop)\n");

        int step = 0;

        while (step < correctSequence.length) {
            System.out.print("Pull lever (" + (step + 1) + "/"
                    + correctSequence.length + "): ");
            String input = inputHandler.readInput().trim();

            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("q")) {
                System.out.println("You step back. The levers reset with a clunk.");
                System.out.println("You can return and try again.");
                return;
            }

            int leverNumber;
            try {
                leverNumber = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Enter a number between 1 and " + leverCount
                        + ", or 'quit' to stop.");
                continue;
            }

            if (leverNumber < 1 || leverNumber > leverCount) {
                System.out.println("There is no lever " + leverNumber
                        + ". Choose between 1 and " + leverCount + ".");
                continue;
            }

            if (leverNumber == correctSequence[step]) {
                step++;
                printPullFeedback(leverNumber, step);
            } else {
                System.out.println("Lever " + leverNumber
                        + " clicks but nothing happens — that was the wrong order.");
                System.out.println("The mechanism resets. Start again.\n");
                step = 0;
            }
        }

        onSuccess(location);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void printPullFeedback(int lever, int stepsDone) {
        String[] sounds = {"Click.", "Clunk.", "The wall shudders...", "A deep boom resonates."};
        int idx = Math.min(stepsDone - 1, sounds.length - 1);
        System.out.println("You pull Lever " + lever + ". " + sounds[idx]);
    }

    private void onSuccess(Location location) {
        solved = true;
        System.out.println("\nDeep within the wall, ancient gears turn.");
        System.out.println("*** " + getName() + " Solved! ***\n");

        if (rewardItem != null) {
            location.addItem(rewardItem);
            System.out.println("A hidden compartment grinds open in the wall.");
            System.out.println("The " + rewardItem.getName() + " gleams inside.");
        }

        if (unlockDirection != null) {
            location.unlockExit(unlockDirection);
            System.out.println("The passage to the "
                    + unlockDirection.name().toLowerCase() + " is now open.");
        }
    }

}
