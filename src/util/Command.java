package util;

import java.util.Arrays;

/**
 * Command — an immutable data object representing one parsed player action.
 *
 * Produced exclusively by CommandParser.parse().  Consumers (Game,
 * CombatSystem) read the type and arguments but can never modify them.
 *
 * Examples of what CommandParser produces:
 *   "go north"          → Command{GO,      ["north"]}
 *   "look at merchant"  → Command{LOOK_AT, ["merchant"]}
 *   "attack shadow goblin" → Command{ATTACK, ["shadow", "goblin"]}
 *   "inventory"         → Command{INVENTORY, []}
 *
 * getArgString() joins the args array with spaces so callers can pass a
 * complete multi-word target (e.g. "shadow goblin") to Location.findEnemy().
 */
public class Command {

    private final CommandType type;
    private final String[]    args;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Command(CommandType type, String[] args) {
        this.type = type;
        this.args = (args != null) ? args : new String[0];
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public CommandType getType()    { return type; }
    public boolean     hasArgs()    { return args.length > 0; }

    /**
     * Returns all arguments joined as a single space-separated string.
     * e.g. ["shadow", "goblin"] → "shadow goblin"
     */
    public String getArgString() {
        return String.join(" ", args);
    }

    /**
     * Returns the argument at the given index, or an empty string if out of range.
     * Avoids ArrayIndexOutOfBoundsException for callers that need specific tokens.
     */
    public String getArg(int index) {
        if (index < 0 || index >= args.length) return "";
        return args[index];
    }

    /** Returns a defensive copy of the raw args array. */
    public String[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }

    @Override
    public String toString() {
        return type + (args.length > 0 ? " [" + getArgString() + "]" : "");
    }
}
