package world;

/**
 * Direction — enum representing the four cardinal movement directions.
 *
 * Using an enum instead of raw strings means the compiler catches typos
 * and switch statements can be exhaustive.
 */
public enum Direction {
    NORTH, SOUTH, EAST, WEST;

    /**
     * Converts a player-typed string to a Direction.
     * Accepts full names and single-letter shortcuts (n/s/e/w).
     *
     * @param input the raw string from the player (may be any case)
     * @return the matching Direction, or {@code null} if unrecognised
     */
    public static Direction fromString(String input) {
        if (input == null) return null;
        switch (input.toLowerCase().trim()) {
            case "north": case "n": return NORTH;
            case "south": case "s": return SOUTH;
            case "east":  case "e": return EAST;
            case "west":  case "w": return WEST;
            default: return null;
        }
    }

    /** Returns the Direction directly opposite to this one. */
    public Direction opposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST:  return WEST;
            case WEST:  return EAST;
            default:    return null;
        }
    }
}
