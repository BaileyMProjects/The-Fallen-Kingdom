package characters;

import core.Difficulty;
import items.ItemFactory;
import items.ItemType;
import quests.QuestManager;
import world.Location;

/**
 * ArenaMaster — NPC that manages the Proving Grounds arena.
 *
 * The player pays a gold fee (difficulty.arenaRespawnCost) to respawn a fresh
 * wave of enemies.  The fee is always less than the gold earned from killing
 * the wave, so the arena is profitable even without crystals.  The 8% chance
 * of a Shadow Crystal drop on each enemy makes crystals farmable here.
 *
 * Wave composition: 3× Shadow Goblin + 1× Plague Rat
 *   Base gold: (3×10g) + (1×5g) = 35g before goldDropMultiplier
 *   Respawn cost: 20g (Easy) / 15g (Medium) / 12g (Hard)
 *   Minimum profit after multiplier: 21g − 12g = 9g on Hard
 */
public class ArenaMaster extends NPC {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ArenaMaster(String name, String description) {
        super(name, description, new String[]{
            "Welcome to the Proving Grounds, adventurer. This arena draws shadow creatures " +
            "from the corrupted lands. Pay me gold and I'll open the gates — clear the wave " +
            "and keep everything you find. Type 'respawn' when you're ready.",
            "The creatures here sometimes carry crystallised shadow energy — rare, but worth " +
            "far more than the gold they drop. Fight enough waves and you'll have plenty to " +
            "bring to Seraphina for enchanting.",
            "The fee is lower than what you'll earn — I make my coin on the brave ones who " +
            "keep coming back. Prove yourself, adventurer."
        });
    }

    /** Protected constructor for subclasses that supply their own dialogue. */
    protected ArenaMaster(String name, String description, String[] dialogue) {
        super(name, description, dialogue);
    }

    // -------------------------------------------------------------------------
    // Respawn
    // -------------------------------------------------------------------------

    /**
     * Pays for and spawns a fresh wave of enemies in the arena location.
     *
     * Requirements:
     *   - No enemies currently alive in the location (must clear first).
     *   - Player has at least difficulty.arenaRespawnCost gold.
     *
     * Wave: 3× Shadow Goblin + 1× Plague Rat, each with an 8% crystal drop.
     */
    public void respawn(Player player, Location location, Difficulty difficulty) {
        if (!location.getEnemies().isEmpty()) {
            System.out.println(getName() + " shakes his head.");
            System.out.println("\"Clear the arena first. No point opening the gates while creatures still roam.\"");
            return;
        }

        int cost = difficulty.arenaRespawnCost;
        if (player.getGold() < cost) {
            System.out.println("\"It costs " + cost + " gold to open the gates. You have "
                    + player.getGold() + ". Come back when you're better funded.\"");
            return;
        }

        player.spendGold(cost);
        System.out.println("\n" + getName() + " accepts your " + cost + " gold and pulls a heavy lever.");
        pause(800);
        System.out.println("  Iron gates screech open. Shadow creatures pour into the arena.\n");

        for (int i = 0; i < 3; i++) {
            Enemy goblin = EnemyFactory.create(EnemyType.SHADOW_GOBLIN);
            goblin.addChanceLoot(ItemFactory.create(ItemType.SHADOW_CRYSTAL), 0.08);
            location.addEnemy(goblin);
        }
        Enemy rat = EnemyFactory.create(EnemyType.PLAGUE_RAT);
        rat.addChanceLoot(ItemFactory.create(ItemType.SHADOW_CRYSTAL), 0.08);
        location.addEnemy(rat);

        System.out.println("  A wave has arrived: 3x Shadow Goblin, 1x Plague Rat.");
        System.out.println("  Gold: " + cost + "g spent. Clear them for profit and a chance at Shadow Crystals.");
    }

    // -------------------------------------------------------------------------
    // Dialogue override — show respawn cost on talk
    // -------------------------------------------------------------------------

    @Override
    public String talk(Player player, QuestManager questManager) {
        return super.talk(player, questManager)
                + "\n\n  Respawn cost: 20g (Easy) / 15g (Medium) / 12g (Hard)"
                + "\n  Wave reward:  ~35g base + 8% crystal per enemy";
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private static void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
