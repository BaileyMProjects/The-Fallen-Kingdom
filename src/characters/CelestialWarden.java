package characters;

import core.Difficulty;
import items.ItemFactory;
import items.ItemType;
import quests.QuestManager;
import world.Location;

/**
 * CelestialWarden — NPC that manages the Celestial Barracks arena in the divine realm.
 *
 * Extends ArenaMaster so that findNPCOfType(ArenaMaster.class) discovers it and
 * Game.handleRespawn() calls the overridden respawn() via polymorphism — no changes
 * needed in Game.java.
 *
 * Wave: 2× Fallen Seraph + 1× Celestial Sentinel
 *   Base gold: (2×38g) + (1×32g) = 108g before goldDropMultiplier
 *   Respawn cost: 45g (Easy) / 38g (Medium) / 32g (Hard)
 *   Min profit after Hard multiplier: 64.8g − 32g = 32.8g
 *   10% Divine Crystal chance on each enemy
 */
public class CelestialWarden extends ArenaMaster {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public CelestialWarden(String name, String description) {
        super(name, description, new String[]{
            "Only the strongest may face the celestial trials. Pay my toll and I will " +
            "unleash the divine guard upon you. Clear the wave and keep everything they " +
            "drop. Type 'respawn' when you are ready to fight.",
            "The Fallen Seraphs carry Divine Crystals sometimes — rare, but if you need " +
            "them for Luminara's forge, this is the fastest way to farm them.",
            "The toll is steep but the rewards are far greater than what the shadow arena " +
            "offers. Prove you belong here."
        });
    }

    // -------------------------------------------------------------------------
    // Respawn
    // -------------------------------------------------------------------------

    /**
     * Spawns a fresh divine wave: 2× Fallen Seraph + 1× Celestial Sentinel.
     * Each has a 10% chance to drop a Divine Crystal on defeat.
     */
    @Override
    public void respawn(Player player, Location location, Difficulty difficulty) {
        if (!location.getEnemies().isEmpty()) {
            System.out.println(getName() + " crosses their arms.");
            System.out.println("\"Defeat the current wave before you call for more.\"");
            return;
        }

        int cost = difficulty.divineArenaRespawnCost;
        if (player.getGold() < cost) {
            System.out.println("\"The toll is " + cost + " gold. You have "
                    + player.getGold() + ". Come back when you're richer.\"");
            return;
        }

        player.spendGold(cost);
        System.out.println("\n" + getName() + " accepts your " + cost
                + " gold and strikes the celestial bell.");
        pause(800);
        System.out.println("  A blinding pulse of light fills the arena. The divine guard descends.\n");

        for (int i = 0; i < 2; i++) {
            Enemy seraph = EnemyFactory.create(EnemyType.FALLEN_SERAPH);
            seraph.addChanceLoot(ItemFactory.create(ItemType.DIVINE_CRYSTAL), 0.10);
            location.addEnemy(seraph);
        }
        Enemy sentinel = EnemyFactory.create(EnemyType.CELESTIAL_SENTINEL);
        sentinel.addChanceLoot(ItemFactory.create(ItemType.DIVINE_CRYSTAL), 0.10);
        location.addEnemy(sentinel);

        System.out.println("  A wave has arrived: 2x Fallen Seraph, 1x Celestial Sentinel.");
        System.out.println("  Gold: " + cost + "g spent. Clear them for profit and a chance at Divine Crystals.");
    }

    // -------------------------------------------------------------------------
    // Dialogue override — show respawn cost and wave info on talk
    // -------------------------------------------------------------------------

    @Override
    public String talk(Player player, QuestManager questManager) {
        return super.talk(player, questManager)
                + "\n\n  Respawn cost: 45g (Easy) / 38g (Medium) / 32g (Hard)"
                + "\n  Wave reward:  ~108g base + 10% Divine Crystal per enemy";
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private static void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
