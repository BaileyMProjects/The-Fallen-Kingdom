package util;

/**
 * CommandType — every action the player can perform, as a typed constant.
 *
 * CommandParser maps raw text strings to these values so the rest of the
 * game never needs to compare raw strings.  Game.processCommand() switches
 * on this enum, making it impossible to accidentally handle a typo as a
 * valid command.
 */
public enum CommandType {

    // Movement
    GO,

    // Observation
    LOOK,       // describe current location
    LOOK_AT,    // examine a specific target

    // Item interaction
    TAKE,
    DROP,
    INVENTORY,
    USE,
    EQUIP,

    // NPC interaction
    TALK,
    BUY,
    SELL,
    ENCHANT,    // enchant a weapon or armour at an Enchanter NPC
    BEFRIEND,   // choose friendship over combat (Tree Protector)
    RESPAWN,    // pay to respawn arena enemies in the Proving Grounds

    // Combat
    ATTACK,

    // Puzzle
    SOLVE,

    // Information
    STATS,
    QUESTS,
    HELP,

    // Meta
    QUIT,
    SAVE,
    MAP,

    OPTEST,

    /** Returned when the input does not match any known command. */
    UNKNOWN
}
