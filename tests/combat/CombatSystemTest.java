package combat;

import characters.Enemy;
import characters.EnemyFactory;
import characters.EnemyType;
import characters.Player;
import events.EventManager;
import events.GameEvent;
import events.GameEventType;
import events.GameObserver;
import util.InputHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CombatSystemTest — unit tests for CombatSystem (Strategy and Observer patterns).
 *
 * InputHandler is used in GUI mode so inputs can be pre-loaded via provide()
 * before executeCombat() is called.  Because the BlockingQueue is already
 * populated, readInput() returns immediately and the test runs single-threaded
 * without any blocking or thread gymnastics.
 *
 * Enemy stats used: Shadow Goblin — HP 30, ATK 8, DEF 2.
 * Player stats:     name=Hero, maxHP=100, ATK=10, DEF=5, gold=0.
 *   • Player deals max(1, 10-2) = 8 damage per attack → 4 hits to kill goblin.
 *   • Goblin deals at most max(1, 8×1.75 - 5) = 9 damage → 11+ hits to kill player.
 *
 * "Weakling" player:  maxHP=1, ATK=1, DEF=0 — guaranteed to die after one hit.
 *   • Goblin's weakest hit = max(1, 8×0.5 - 0) = 4 → always fatal.
 */
class CombatSystemTest {

    private CombatSystem combatSystem;
    private EventManager eventManager;
    private Player       hero;
    private Enemy        goblin;

    @BeforeEach
    void setUp() {
        eventManager  = new EventManager();
        combatSystem  = new CombatSystem(eventManager);
        hero          = new Player("Hero", 100, 10, 5, 0);
        goblin        = EnemyFactory.create(EnemyType.SHADOW_GOBLIN);
    }

    // ── Player wins ──────────────────────────────────────────────────────────

    @Test
    void executeCombat_returnsTrueWhenPlayerWins() {
        InputHandler input = guiInput();
        for (int i = 0; i < 10; i++) input.provide("a");   // more than enough to win

        assertTrue(combatSystem.executeCombat(hero, goblin, input),
                "executeCombat should return true when the player survives");
    }

    @Test
    void executeCombat_enemyIsDeadAfterPlayerWins() {
        InputHandler input = guiInput();
        for (int i = 0; i < 10; i++) input.provide("a");

        combatSystem.executeCombat(hero, goblin, input);
        assertFalse(goblin.isAlive(), "Goblin should be dead after a victorious combat");
    }

    // ── Player dies ──────────────────────────────────────────────────────────

    @Test
    void executeCombat_returnsFalseWhenPlayerDies() {
        Player weakling = new Player("Weakling", 1, 1, 0, 0);
        InputHandler input = guiInput();
        input.provide("a");   // weakling attacks → goblin survives, then counter-kills

        assertFalse(combatSystem.executeCombat(weakling, goblin, input),
                "executeCombat should return false when the player is killed");
    }

    @Test
    void executeCombat_playerHealthIsZeroOnDeath() {
        Player weakling = new Player("Weakling", 1, 1, 0, 0);
        InputHandler input = guiInput();
        input.provide("a");

        combatSystem.executeCombat(weakling, goblin, input);
        assertEquals(0, weakling.getHealth(), "Player HP should be 0 after being killed");
    }

    // ── Observer pattern — events ────────────────────────────────────────────

    @Test
    void executeCombat_firesEnemyDefeatedEvent() {
        boolean[] fired = {false};
        eventManager.subscribe(new GameObserver() {
            @Override
            public void onEvent(GameEvent event) {
                if (event.getType() == GameEventType.ENEMY_DEFEATED) fired[0] = true;
            }
        });

        InputHandler input = guiInput();
        for (int i = 0; i < 10; i++) input.provide("a");

        combatSystem.executeCombat(hero, goblin, input);
        assertTrue(fired[0], "ENEMY_DEFEATED event must be fired when the enemy dies");
    }

    @Test
    void executeCombat_firesCombatStartedEvent() {
        boolean[] fired = {false};
        eventManager.subscribe(new GameObserver() {
            @Override
            public void onEvent(GameEvent event) {
                if (event.getType() == GameEventType.COMBAT_STARTED) fired[0] = true;
            }
        });

        InputHandler input = guiInput();
        for (int i = 0; i < 10; i++) input.provide("a");

        combatSystem.executeCombat(hero, goblin, input);
        assertTrue(fired[0], "COMBAT_STARTED event must be fired at the start of combat");
    }

    @Test
    void executeCombat_combatStartedEventCarriesEnemyAsContext() {
        Object[] context = {null};
        eventManager.subscribe(new GameObserver() {
            @Override
            public void onEvent(GameEvent event) {
                if (event.getType() == GameEventType.COMBAT_STARTED) {
                    context[0] = event.getContext();
                }
            }
        });

        InputHandler input = guiInput();
        for (int i = 0; i < 10; i++) input.provide("a");

        combatSystem.executeCombat(hero, goblin, input);
        assertSame(goblin, context[0],
                "COMBAT_STARTED context should be the Enemy object");
    }

    // ── Experience ───────────────────────────────────────────────────────────

    @Test
    void executeCombat_playerGainsExperienceOnVictory() {
        int xpBefore = hero.getExperience();

        InputHandler input = guiInput();
        for (int i = 0; i < 10; i++) input.provide("a");

        combatSystem.executeCombat(hero, goblin, input);
        assertTrue(hero.getExperience() > xpBefore,
                "Player should gain experience after defeating an enemy");
    }

    @Test
    void executeCombat_xpAmountIsHalfEnemyMaxHp() {
        // Shadow Goblin maxHP = 30 → expected XP = max(5, 30/2) = 15
        InputHandler input = guiInput();
        for (int i = 0; i < 10; i++) input.provide("a");

        combatSystem.executeCombat(hero, goblin, input);
        assertEquals(15, hero.getExperience(),
                "XP granted should equal max(5, enemy.maxHP / 2)");
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    /** Returns a fresh InputHandler in GUI mode (uses BlockingQueue, no System.in). */
    private static InputHandler guiInput() {
        InputHandler h = new InputHandler();
        h.enableGuiMode();
        return h;
    }
}
