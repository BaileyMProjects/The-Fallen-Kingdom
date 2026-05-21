package util;

import java.util.Arrays;

/**
 * CommandParser — converts a raw input string into a typed Command object.
 *
 * Responsibilities:
 *   1. Normalise input (lowercase, collapse whitespace).
 *   2. Identify the command verb from a comprehensive list of aliases.
 *   3. Strip filler words ("to" after "talk", "up" after "pick").
 *   4. Return a Command with the correct type and remaining args.
 *   5. Return Command{UNKNOWN} for anything unrecognised — never throw.
 *
 * All command aliases are defined here, so adding a new alias for an
 * existing command is a one-line change in this single class.
 *
 * Error handling: CommandParser never crashes on unexpected input.
 * Null and empty input both return Command{UNKNOWN}.
 */
public class CommandParser {

    // -------------------------------------------------------------------------
    // Parse
    // -------------------------------------------------------------------------

    /**
     * Parses a raw player input string into a Command.
     *
     * @param input the raw string from InputHandler (may be null)
     * @return a Command object — never null
     */
    public Command parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new Command(CommandType.UNKNOWN, new String[0]);
        }

        // Normalise: lowercase and collapse multiple spaces
        String[] tokens = input.toLowerCase().trim().split("\\s+");
        String   verb   = tokens[0];
        String[] args   = Arrays.copyOfRange(tokens, 1, tokens.length);

        switch (verb) {

            // ── Movement ────────────────────────────────────────────────────
            case "go": case "move": case "walk": case "run": case "head":
                return new Command(CommandType.GO, args);

            // ── Observation ─────────────────────────────────────────────────
            case "look": case "l":
                // "look at X" → LOOK_AT  |  "look" alone → LOOK
                if (args.length > 0 && args[0].equals("at")) {
                    return new Command(CommandType.LOOK_AT,
                            Arrays.copyOfRange(args, 1, args.length));
                }
                return args.length == 0
                        ? new Command(CommandType.LOOK, new String[0])
                        : new Command(CommandType.LOOK_AT, args);

            case "examine": case "x": case "inspect": case "check": case "read":
                return new Command(CommandType.LOOK_AT, args);

            // ── Taking items ────────────────────────────────────────────────
            case "take": case "get": case "grab":
                return new Command(CommandType.TAKE, args);

            case "pick":
                // "pick up X" → strip "up"
                if (args.length > 0 && args[0].equals("up")) {
                    return new Command(CommandType.TAKE,
                            Arrays.copyOfRange(args, 1, args.length));
                }
                return new Command(CommandType.TAKE, args);

            // ── Dropping items ───────────────────────────────────────────────
            case "drop": case "leave": case "discard": case "throw":
                return new Command(CommandType.DROP, args);

            // ── Inventory ────────────────────────────────────────────────────
            case "inventory": case "inv": case "i": case "items": case "bag": case "pack":
                return new Command(CommandType.INVENTORY, new String[0]);

            // ── Using / equipping items ─────────────────────────────────────
            case "use": case "consume": case "drink": case "eat":
                return new Command(CommandType.USE, args);

            case "equip": case "wear": case "wield": case "put":
                // "put on X" → strip "on"
                if (args.length > 0 && args[0].equals("on")) {
                    return new Command(CommandType.EQUIP,
                            Arrays.copyOfRange(args, 1, args.length));
                }
                return new Command(CommandType.EQUIP, args);

            // ── NPC interaction ──────────────────────────────────────────────
            case "talk": case "speak": case "chat": case "converse":
                // "talk to X" → strip "to"
                if (args.length > 0 && args[0].equals("to")) {
                    return new Command(CommandType.TALK,
                            Arrays.copyOfRange(args, 1, args.length));
                }
                return new Command(CommandType.TALK, args);

            case "buy": case "purchase":
                return new Command(CommandType.BUY, args);

            case "sell": case "trade":
                return new Command(CommandType.SELL, args);

            case "enchant": case "ench":
                return new Command(CommandType.ENCHANT, args);

            case "respawn": case "summon": case "reset":
                return new Command(CommandType.RESPAWN, new String[0]);

            case "befriend": case "friend": case "ally":
                return new Command(CommandType.BEFRIEND, args);

            // ── Combat ───────────────────────────────────────────────────────
            case "attack": case "fight": case "kill": case "hit":
            case "strike": case "slay": case "battle":
                return new Command(CommandType.ATTACK, args);

            // ── Puzzle ───────────────────────────────────────────────────────
            case "solve": case "puzzle": case "attempt": case "try":
                return new Command(CommandType.SOLVE, new String[0]);

            // ── Information ──────────────────────────────────────────────────
            case "stats": case "status": case "character": case "me": case "self":
                return new Command(CommandType.STATS, new String[0]);

            case "quests": case "quest": case "journal": case "log": case "objectives":
                return new Command(CommandType.QUESTS, new String[0]);

            case "help": case "?": case "commands": case "h": case "man":
                return new Command(CommandType.HELP, new String[0]);

            // ── Meta ─────────────────────────────────────────────────────────
            case "quit": case "exit": case "bye": case "q":
                return new Command(CommandType.QUIT, new String[0]);

            // ── Unknown ──────────────────────────────────────────────────────
            default:
                return new Command(CommandType.UNKNOWN, tokens);
        }
    }
}
