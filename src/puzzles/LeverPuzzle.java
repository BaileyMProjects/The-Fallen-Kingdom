package puzzles;

import characters.Player;
import items.Item;
import util.InputHandler;
import world.Direction;
import world.Location;

/**
 * LeverPuzzle — a mechanical sequence puzzle with numbered levers.
 *
 * Used in the Underground Dungeon.  Three levers must be pulled in the
 * correct order (1 → 3 → 2).  The cryptic clue references moon phases:
 *   "Waxing" (growing, first) = 1
 *   "Waning" (shrinking, last) = 3
 *   "Half"   (middle)          = 2
 *
 * Behaviour:
 *   - The player enters lever numbers one at a time.
 *   - Progress is shown after each pull ("Lever 1 pulled. Next: ?/?").
 *   - A wrong lever at any point resets the sequence with feedback.
 *   - The player can type 'quit' to stop attempting.
 *   - On success, any configured reward item is added to the location.
 *
 * Validates all input: non-numeric entries and out-of-range numbers are
 * handled gracefully with an error message rather than a crash.
 */
public class LeverPuzzle extends Puzzle {

    private static final int LEVER_COUNT = 3;

    private final int[]     correctSequence;
    private final Item      rewardItem;
    private final Direction unlockDirection;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param correctSequence the order in which levers must be pulled
     * @param rewardItem      item added to the location on success, or null
     * @param unlockDirection exit unlocked on success, or null
     */
    public LeverPuzzle(int[] correctSequence, Item rewardItem, Direction unlockDirection) {
        super("Lever Puzzle",
              "Three heavy iron levers protrude from the wall, each engraved with a number.");
        this.correctSequence = correctSequence;
        this.rewardItem      = rewardItem;
        this.unlockDirection = unlockDirection;
    }

    // -------------------------------------------------------------------------
    // Puzzle contract
    // -------------------------------------------------------------------------

    @Override
    public void attempt(Player player, InputHandler inputHandler, Location location) {
        System.out.println("\n=== Lever Mechanism ===");
        System.out.println(description);
        System.out.println("\nAn inscription carved beneath them reads:");
        System.out.println("  \"Follow the moonlight: waxing, waning, half.\"\n");
        System.out.println("Pull the levers 1-" + LEVER_COUNT + " in the correct order.");
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

            // Validate: must be a number in range
            int leverNumber;
            try {
                leverNumber = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Enter a number between 1 and " + LEVER_COUNT
                        + ", or 'quit' to stop.");
                continue;
            }

            if (leverNumber < 1 || leverNumber > LEVER_COUNT) {
                System.out.println("There is no lever " + leverNumber
                        + ". Choose between 1 and " + LEVER_COUNT + ".");
                continue;
            }

            // Check if this is the correct lever for this step
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
        String[] sounds = {"Click.", "Clunk.", "The wall shudders..."};
        int soundIndex  = Math.min(stepsDone - 1, sounds.length - 1);
        System.out.println("You pull Lever " + lever + ". " + sounds[soundIndex]);
    }

    private void onSuccess(Location location) {
        solved = true;
        System.out.println("\nDeep within the wall, ancient gears turn.");
        System.out.println("*** Lever Puzzle Solved! ***\n");

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
