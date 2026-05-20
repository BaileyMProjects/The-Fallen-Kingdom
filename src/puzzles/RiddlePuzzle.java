package puzzles;

import characters.Player;
import items.Item;
import util.InputHandler;
import world.Direction;
import world.Location;

/**
 * RiddlePuzzle — a text-based riddle the player must answer correctly.
 *
 * Used in two locations:
 *   Ancient Ruins    — answer "map",  reward: Ancient Key spawns in location
 *   Corrupted Castle — answer "echo", reward: north exit unlocked (→ Shadow Throne)
 *
 * Behaviour:
 *   - The riddle is displayed each time attempt() is called while unsolved.
 *   - Answers are compared case-insensitively and trimmed.
 *   - A letter hint is given after two consecutive wrong answers.
 *   - The player can type 'quit' at any time to stop trying.
 *   - On success, any configured reward item is added to the location and any
 *     configured exit direction is unlocked.
 */
public class RiddlePuzzle extends Puzzle {

    private final String    riddle;
    private final String    answer;
    private final Item      rewardItem;       // null if no item reward
    private final Direction unlockDirection;  // null if no exit to unlock
    private       int       failedAttempts;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param riddle           the question displayed to the player
     * @param answer           the correct answer (compared case-insensitively)
     * @param rewardItem       item added to the location on success, or null
     * @param unlockDirection  exit direction unlocked on success, or null
     */
    public RiddlePuzzle(String riddle, String answer,
                        Item rewardItem, Direction unlockDirection) {
        super("Riddle", "A stone tablet bears an ancient inscription that glows faintly.");
        this.riddle          = riddle;
        this.answer          = answer.toLowerCase().trim();
        this.rewardItem      = rewardItem;
        this.unlockDirection = unlockDirection;
        this.failedAttempts  = 0;
    }

    // -------------------------------------------------------------------------
    // Puzzle contract
    // -------------------------------------------------------------------------

    @Override
    public void attempt(Player player, InputHandler inputHandler, Location location) {
        System.out.println("\n=== Ancient Riddle ===");
        System.out.println(description);
        System.out.println("\nThe inscription reads:");
        System.out.println("  \"" + riddle + "\"\n");

        while (true) {
            // Hint after two consecutive failures
            if (failedAttempts >= 2) {
                System.out.println("  [Hint: the answer starts with '"
                        + answer.charAt(0) + "']");
            }

            System.out.print("Your answer (or 'quit' to leave): ");
            String input = inputHandler.readInput().toLowerCase().trim();

            if (input.equals("quit") || input.equals("q")) {
                System.out.println("You step back from the tablet. You can return and try again.");
                return;
            }

            if (input.equals(answer)) {
                onSuccess(location);
                return;
            } else {
                failedAttempts++;
                System.out.println("The inscription remains dark. That is not the answer.");
                if (failedAttempts < 2) {
                    System.out.println("Think carefully and try again, or type 'quit' to leave.\n");
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Reward logic
    // -------------------------------------------------------------------------

    private void onSuccess(Location location) {
        solved = true;
        failedAttempts = 0;
        System.out.println("\nThe inscription blazes with golden light!");
        System.out.println("*** Riddle Solved! ***\n");

        if (rewardItem != null) {
            location.addItem(rewardItem);
            System.out.println("A hidden compartment slides open.");
            System.out.println("The " + rewardItem.getName() + " materialises before you.");
        }

        if (unlockDirection != null) {
            location.unlockExit(unlockDirection);
            System.out.println("You hear a heavy mechanism grind —");
            System.out.println("the passage to the " + unlockDirection.name().toLowerCase()
                    + " is now open.");
        }
    }
}
