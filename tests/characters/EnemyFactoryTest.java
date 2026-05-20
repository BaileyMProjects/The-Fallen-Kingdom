package characters;

import combat.AggressiveStrategy;
import combat.DefensiveStrategy;
import combat.RandomStrategy;
import items.Item;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnemyFactoryTest — unit tests for the EnemyFactory (Factory pattern).
 *
 * Verifies that each enemy type is created with the correct base stats,
 * the correct attack strategy (Strategy pattern), and any expected loot.
 * Tests also confirm that the Shadow Lord is correctly identified as a Boss.
 */
class EnemyFactoryTest {

    // ── Shadow Goblin ─────────────────────────────────────────────────────────

    @Test
    void shadowGoblin_hasCorrectName() {
        Enemy goblin = EnemyFactory.create(EnemyType.SHADOW_GOBLIN);
        assertEquals("Shadow Goblin", goblin.getName());
    }

    @Test
    void shadowGoblin_hasCorrectStats() {
        Enemy goblin = EnemyFactory.create(EnemyType.SHADOW_GOBLIN);
        assertAll("Shadow Goblin stats",
                () -> assertEquals(30, goblin.getMaxHealth(), "max HP"),
                () -> assertEquals(8,  goblin.getAttackPower(), "attack"),
                () -> assertEquals(2,  goblin.getDefense(), "defense"),
                () -> assertEquals(10, goblin.getGoldDrop(), "gold drop")
        );
    }

    @Test
    void shadowGoblin_usesRandomStrategy() {
        Enemy goblin = EnemyFactory.create(EnemyType.SHADOW_GOBLIN);
        assertTrue(goblin.getAttackStrategy() instanceof RandomStrategy,
                "Shadow Goblin should use RandomStrategy");
    }

    @Test
    void shadowGoblin_isNotBoss() {
        Enemy goblin = EnemyFactory.create(EnemyType.SHADOW_GOBLIN);
        assertFalse(goblin.isBoss());
    }

    // ── Dark Knight ───────────────────────────────────────────────────────────

    @Test
    void darkKnight_hasCorrectStats() {
        Enemy knight = EnemyFactory.create(EnemyType.DARK_KNIGHT);
        assertAll("Dark Knight stats",
                () -> assertEquals(70, knight.getMaxHealth(), "max HP"),
                () -> assertEquals(15, knight.getAttackPower(), "attack"),
                () -> assertEquals(8,  knight.getDefense(), "defense"),
                () -> assertEquals(25, knight.getGoldDrop(), "gold drop")
        );
    }

    @Test
    void darkKnight_usesDefensiveStrategy() {
        Enemy knight = EnemyFactory.create(EnemyType.DARK_KNIGHT);
        assertTrue(knight.getAttackStrategy() instanceof DefensiveStrategy,
                "Dark Knight should use DefensiveStrategy");
    }

    @Test
    void darkKnight_dropsHealthPotion() {
        Enemy knight = EnemyFactory.create(EnemyType.DARK_KNIGHT);
        List<Item> loot = knight.getLootItems();
        assertFalse(loot.isEmpty(), "Dark Knight should have at least one loot item");
        boolean hasPotion = loot.stream()
                               .anyMatch(i -> i.getName().toLowerCase().contains("potion"));
        assertTrue(hasPotion, "Dark Knight should drop a Health Potion");
    }

    @Test
    void darkKnight_isNotBoss() {
        Enemy knight = EnemyFactory.create(EnemyType.DARK_KNIGHT);
        assertFalse(knight.isBoss());
    }

    // ── Shadow Lord ───────────────────────────────────────────────────────────

    @Test
    void shadowLord_hasCorrectStats() {
        Enemy lord = EnemyFactory.create(EnemyType.SHADOW_LORD);
        assertAll("Shadow Lord stats",
                () -> assertEquals(120, lord.getMaxHealth(), "max HP"),
                () -> assertEquals(20,  lord.getAttackPower(), "attack"),
                () -> assertEquals(10,  lord.getDefense(), "defense")
        );
    }

    @Test
    void shadowLord_isBoss() {
        Enemy lord = EnemyFactory.create(EnemyType.SHADOW_LORD);
        assertTrue(lord.isBoss(), "Shadow Lord must be a Boss");
    }

    @Test
    void shadowLord_usesAggressiveStrategy() {
        Enemy lord = EnemyFactory.create(EnemyType.SHADOW_LORD);
        assertTrue(lord.getAttackStrategy() instanceof AggressiveStrategy,
                "Shadow Lord should use AggressiveStrategy");
    }

    @Test
    void shadowLord_dropsAncientRelic() {
        Enemy lord = EnemyFactory.create(EnemyType.SHADOW_LORD);
        List<Item> loot = lord.getLootItems();
        assertFalse(loot.isEmpty(), "Shadow Lord should drop the Ancient Relic");
        boolean hasRelic = loot.stream()
                              .anyMatch(i -> i.getName().toLowerCase().contains("relic"));
        assertTrue(hasRelic, "Shadow Lord should drop an Ancient Relic");
    }

    // ── Factory creates fresh instances ───────────────────────────────────────

    @Test
    void create_returnsFreshInstanceEachCall() {
        Enemy goblin1 = EnemyFactory.create(EnemyType.SHADOW_GOBLIN);
        Enemy goblin2 = EnemyFactory.create(EnemyType.SHADOW_GOBLIN);
        assertNotSame(goblin1, goblin2,
                "Each call to create() should return a new Enemy instance");
    }

    @Test
    void create_allEnemiesStartAtFullHealth() {
        Enemy goblin = EnemyFactory.create(EnemyType.SHADOW_GOBLIN);
        assertEquals(goblin.getMaxHealth(), goblin.getHealth(),
                "A newly created enemy should have full HP");
    }
}
