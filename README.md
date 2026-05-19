# The Fallen Kingdom

A text-based fantasy RPG written in Java for the CS1OP Object Oriented Programming assignment at the University of Reading.

## Story

The Shadow Lord has corrupted the once-peaceful kingdom. As a young adventurer, you must explore six perilous locations, solve ancient puzzles, recover the **Ancient Relic**, and defeat the Shadow Lord to restore peace to The Fallen Kingdom.

## Prerequisites

- Java Development Kit (JDK) 11 or higher
- A terminal / command prompt

## Compiling the Game

Run the following command from the **project root** directory:

**Windows (PowerShell):**
```powershell
javac -d out -sourcepath src src/core/Main.java
```

**macOS / Linux:**
```bash
javac -d out -sourcepath src src/core/Main.java
```

> The `-sourcepath` flag tells the compiler to automatically find and compile all referenced classes within `src/`.

## Running the Game

After compiling, start the game with:

```bash
java -cp out core.Main
```

## Running the Unit Tests

Download `junit-platform-console-standalone-1.10.0.jar` from [Maven Central](https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.0/) and place it in the project root.

**Compile tests:**
```bash
javac -d out -cp "junit-platform-console-standalone-1.10.0.jar;out" -sourcepath src tests/*.java
```

**Run tests:**
```bash
java -jar junit-platform-console-standalone-1.10.0.jar --class-path out --scan-class-path
```

## Game Commands

| Command                   | Description                                    |
|---------------------------|------------------------------------------------|
| `go <direction>`          | Move north, south, east, or west               |
| `look`                    | Describe the current location                  |
| `look at <target>`        | Examine an item, NPC, or enemy in detail       |
| `take <item>`             | Pick up an item from the current location      |
| `drop <item>`             | Drop an item from your inventory               |
| `inventory` / `inv`       | View your inventory                            |
| `use <item>`              | Use an item (e.g., a health potion)            |
| `equip <item>`            | Equip a weapon or piece of armour              |
| `talk <npc>`              | Talk to an NPC in the current location         |
| `attack <enemy>`          | Initiate combat with an enemy                  |
| `solve`                   | Attempt to solve a puzzle in this location     |
| `buy <item>`              | Buy an item from a merchant                    |
| `sell <item>`             | Sell an item to a merchant                     |
| `stats`                   | View your character statistics                 |
| `quests`                  | View your quest log                            |
| `help`                    | Display all available commands                 |
| `quit`                    | Exit the game                                  |

## Locations

| Location            | Description                                      |
|---------------------|--------------------------------------------------|
| Village             | Safe starting area with a Merchant and the Elder |
| Dark Forest         | Dangerous woods patrolled by Shadow Goblins      |
| Ancient Ruins       | Crumbling ruins hiding an ancient riddle         |
| Underground Dungeon | A dark dungeon with a mysterious lever mechanism |
| Corrupted Castle    | The Shadow Lord's stronghold, guarded by knights |
| Shadow Throne Room  | Final confrontation with the Shadow Lord         |

## Assumptions

- The game is **single-player** and **turn-based**.
- Combat proceeds in rounds; the player chooses to attack or run each round.
- Puzzles must be solved to unlock access to later areas.
- Items dropped in a location remain until picked up again.
- All input is **case-insensitive**.
- The player cannot re-enter the Shadow Throne Room after the final battle.
- The Merchant only appears in the Village.
