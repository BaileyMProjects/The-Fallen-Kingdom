package world;

import characters.ArenaMaster;
import characters.CelestialWarden;
import characters.DivineEnchanter;
import characters.Enchanter;
import characters.EnemyFactory;
import characters.EnemyType;
import characters.Merchant;
import characters.NPC;
import characters.Player;
import core.Difficulty;
import events.EventManager;
import items.Item;
import items.ItemFactory;
import items.ItemType;
import puzzles.LeverPuzzle;
import puzzles.RiddlePuzzle;
import save.GameSnapshot;

import java.util.*;
import java.util.stream.Collectors;

/**
 * World — builds and manages the entire location graph.
 *
 * Responsibilities:
 *   - Constructing all locations and wiring their exits
 *   - Populating locations with enemies (via EnemyFactory), NPCs, items, and puzzles
 *   - Moving the player between locations, enforcing locked-door logic
 *
 * World is the single source of truth for where everything is in the game.
 * Game.java delegates all movement and location queries here.
 *
 * Map layout (19 locations):
 *
 *   [Vault] --W-- [Radiant Cathedral] --E-- [Celestial Barracks]
 *                        |  S                       |  S
 *                  [Sunken Shrine] --E-- [Forge]  [Sanctum of the Arbiter]
 *                        |  S
 *                  [Celestial Gate]
 *                        |  S
 *                 [Forgotten Battlefield] --W-- [Ancient Ruins]
 *                                                     |  S
 *   [Mystic Glade] --S-- [Dark Forest] ---------------+
 *                              |  E
 *                      [Underground Dungeon]
 *                              |  N (locked: Ancient Key)
 *   [Cursed Archives] --W-- [Corrupted Castle] --E-- [Shadow Barracks]
 *                              |  N (locked: puzzle)
 *                       [Shadow Throne Room]
 *
 *   [Village] --E-- [Dark Forest]
 *   [Village] --S-- [Merchant Village] --S-- [Proving Grounds]
 *   [Dark Forest] --S-- [Mystic Glade]
 */
public class World {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final Map<LocationId, Location> locations;
    private       Location                  currentLocation;
    private final Player                    player;
    private final EventManager              eventManager;

    /**
     * Exits that were explicitly locked during initializeWorld().
     * Tracked so captureState() knows which exits to record as "now unlocked."
     */
    private final Set<String> initiallyLockedExits = new HashSet<>();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public World(Player player, EventManager eventManager) {
        this.player       = player;
        this.eventManager = eventManager;
        this.locations    = new HashMap<>();
        initializeWorld();
    }

    // -------------------------------------------------------------------------
    // World initialisation
    // -------------------------------------------------------------------------

    private void initializeWorld() {

        // ================================================================
        // CREATE ALL LOCATIONS
        // ================================================================

        Location village = new Location(LocationId.VILLAGE, "Village",
            "A small village at the edge of the corrupted kingdom. Stone cottages\n" +
            "line a dusty road. Most inhabitants have fled, but a few brave souls remain.\n" +
            "The air carries the distant smell of shadow magic.");

        Location darkForest = new Location(LocationId.DARK_FOREST, "Dark Forest",
            "Twisted trees block out the sky, their branches clawing at the gloom.\n" +
            "Strange, guttural sounds echo from the undergrowth. A narrow trail\n" +
            "branches in every direction — something ancient stirs in the south.");

        Location ancientRuins = new Location(LocationId.ANCIENT_RUINS, "Ancient Ruins",
            "Crumbling stone columns rise from the earth, engraved with faded runes.\n" +
            "At the centre stands a weathered stone tablet, its inscription still\n" +
            "glowing faintly with ancient power. A ruined archway opens to the east.");

        Location dungeon = new Location(LocationId.UNDERGROUND_DUNGEON, "Underground Dungeon",
            "A damp underground chamber lit by sputtering torches. Three heavy iron\n" +
            "levers protrude from the far wall, and a locked iron door stands to the north.\n" +
            "Water drips steadily from the ceiling.");

        Location castle = new Location(LocationId.CORRUPTED_CASTLE, "Corrupted Castle",
            "The once-grand hall is warped by shadow energy. Dark crystals line the\n" +
            "cracked walls and a sickly purple light pulses from the ceiling.\n" +
            "Passages branch east and west into darker reaches of the castle.");

        Location shadowThrone = new Location(LocationId.SHADOW_THRONE, "Shadow Throne Room",
            "Absolute darkness presses in from every side. A throne of bone and obsidian\n" +
            "looms at the far end of the chamber. Upon it sits the Shadow Lord — a\n" +
            "towering figure of darkness whose eyes burn like dying stars.");

        // ── New locations ────────────────────────────────────────────────────

        Location mysticGlade = new Location(LocationId.MYSTIC_GLADE, "Mystic Glade",
            "A serene clearing bathed in soft emerald light. Ancient oaks form a\n" +
            "natural cathedral around a carpet of luminous moss. The air here feels\n" +
            "different — alive and watchful. At the centre stands a colossal oak,\n" +
            "its bark carved with symbols older than the kingdom itself. A great\n" +
            "guardian stands vigil beneath its boughs.");

        Location forgottenBattlefield = new Location(LocationId.FORGOTTEN_BATTLEFIELD, "Forgotten Battlefield",
            "A vast ruined field strewn with the remnants of a war long forgotten.\n" +
            "Rusted weapons half-buried in scorched earth stretch to the horizon.\n" +
            "Stone formations etched with glowing runes jut from the ground —\n" +
            "this place still holds ancient power. The wind carries the echo of\n" +
            "distant battle cries, and something crawls between the ruins.");

        Location shadowBarracks = new Location(LocationId.SHADOW_BARRACKS, "Shadow Barracks",
            "A grim stone barracks where the Shadow Lord's forces are quartered.\n" +
            "Iron cages hang from the ceiling, most empty. Shadow energy seeps from\n" +
            "the walls, and formless shapes drift between crumbling bunks — remnants\n" +
            "of soldiers long since consumed by darkness. A tattered shadow robe\n" +
            "hangs on the far wall.");

        Location merchantVillage = new Location(LocationId.MERCHANT_VILLAGE, "Merchant's Village",
            "A prosperous settlement built along a well-worn trade road. Unlike the\n" +
            "half-abandoned village to the north, this place hums with activity.\n" +
            "A blacksmith's forge glows orange in one corner; across the square, an\n" +
            "enchantress's tent shimmers with faint arcane light. The air smells of\n" +
            "hot iron and something older — magic.");

        Location cursedArchives = new Location(LocationId.CURSED_ARCHIVES, "Cursed Archives",
            "Row upon row of ancient bookshelves stretch into the darkness, their\n" +
            "tomes crackling with corrupted shadow energy. At the room's heart\n" +
            "stands a stone altar bearing four symbol-etched levers. Carved above\n" +
            "them in shadow-script: 'Knowledge yields to those who seek in order.'");

        Location provingGrounds = new Location(LocationId.PROVING_GROUNDS, "Proving Grounds",
            "A wide stone arena enclosed by iron fencing. Scorch marks and old\n" +
            "bloodstains cover the floor — countless battles have been fought here.\n" +
            "A weathered arena master stands at the gate controls, arms crossed.\n" +
            "Shadow creatures lurk in caged pens along the far wall, waiting to\n" +
            "be released. The air tastes of iron and something darker.");

        // ── Divine realm locations ──────────────────────────────────────────

        Location celestialGate = new Location(LocationId.CELESTIAL_GATE, "Celestial Gate",
            "An immense archway of pale stone covered in still-glowing celestial script.\n" +
            "Beyond it, the air vibrates with divine resonance — a second kingdom\n" +
            "hidden above the mortal realm, now twisted by corruption. A worn monk\n" +
            "kneels before the arch in silent prayer. The path north leads deeper in.");

        Location sunkenShrine = new Location(LocationId.SUNKEN_SHRINE, "Sunken Shrine",
            "A once-sacred shrine half-submerged in glowing golden water that neither\n" +
            "flows nor evaporates. Stone columns rise from its surface carved with\n" +
            "faded celestial runes. At the shrine's heart a narrow altar holds a\n" +
            "riddle inscribed in light — still active, still waiting. Corrupted\n" +
            "paladins stand vigil among the columns, drawn here by the old magic.");

        Location divineForge = new Location(LocationId.DIVINE_FORGE, "Divine Forge",
            "A forge of white stone and golden filigree, its flames burning a pure\n" +
            "celestial blue. The air smells of ozone and something ancient — the\n" +
            "scent of divine power made tangible. Tools of incredible precision hang\n" +
            "from the walls. A merchant offers the finest divine-realm gear from\n" +
            "a side alcove. This place is safe — the corruption cannot reach the flame.");

        Location radiantCathedral = new Location(LocationId.RADIANT_CATHEDRAL, "Radiant Cathedral",
            "The cathedral's vaulted ceiling stretches impossibly high, its stained-glass\n" +
            "windows shattered and dark. Where holy light once fell in coloured patterns\n" +
            "on white stone, shadow-gold corruption now bleeds from the cracks. Broken\n" +
            "pews lead toward a scorched altar. The guardians here were once the most\n" +
            "devout — now they are the most dangerous.");

        Location celestialBarracks = new Location(LocationId.CELESTIAL_BARRACKS, "Celestial Barracks",
            "A vast barracks where the celestial guard once rested between vigils. The\n" +
            "bunks are made of interlocked bone and gold leaf, and each weapon rack\n" +
            "still holds equipment of terrifying quality. A warden — barely recognisable\n" +
            "as the man he once was — stands at the gate controls, one hand resting on\n" +
            "a bell of pure crystal. A merchant operates from the rear of the hall.");

        Location vaultOfFallen = new Location(LocationId.VAULT_OF_THE_FALLEN, "Vault of the Fallen",
            "A hushed chamber lined with suits of armour on stone pedestals — the\n" +
            "equipment of paladins who gave everything for the celestial order. Their\n" +
            "names are carved in the walls in a language older than the kingdom. Dust\n" +
            "drifts through shafts of pale light. Two guardians remain, still loyal\n" +
            "to their vigil even in the grip of corruption.");

        Location sanctumArbiter = new Location(LocationId.SANCTUM_OF_THE_ARBITER, "Sanctum of the Arbiter",
            "A circular chamber of blinding white marble, its ceiling lost in a haze\n" +
            "of golden light. At the centre stands a throne of interlocked haloes —\n" +
            "and upon it sits the Arbiter. His armour gleams with terrible radiance.\n" +
            "He turns to face you with eyes like collapsing suns. There is no mercy\n" +
            "left in them — only the weight of a broken divine order.");

        // ================================================================
        // CONNECT LOCATIONS
        // ================================================================

        // Village ↔ Dark Forest
        village.setExit(Direction.EAST,  darkForest);
        darkForest.setExit(Direction.WEST,  village);

        // Dark Forest ↔ Ancient Ruins
        darkForest.setExit(Direction.NORTH, ancientRuins);
        ancientRuins.setExit(Direction.SOUTH, darkForest);

        // Dark Forest ↔ Underground Dungeon
        darkForest.setExit(Direction.EAST,  dungeon);
        dungeon.setExit(Direction.WEST,  darkForest);

        // Village ↔ Merchant Village
        village.setExit(Direction.SOUTH, merchantVillage);
        merchantVillage.setExit(Direction.NORTH, village);

        // Merchant Village ↔ Proving Grounds
        merchantVillage.setExit(Direction.SOUTH, provingGrounds);
        provingGrounds.setExit(Direction.NORTH, merchantVillage);

        // Dark Forest ↔ Mystic Glade (new)
        darkForest.setExit(Direction.SOUTH, mysticGlade);
        mysticGlade.setExit(Direction.NORTH, darkForest);

        // Ancient Ruins ↔ Forgotten Battlefield (new)
        ancientRuins.setExit(Direction.EAST, forgottenBattlefield);
        forgottenBattlefield.setExit(Direction.WEST, ancientRuins);

        // Underground Dungeon ↔ Corrupted Castle (locked)
        dungeon.setExit(Direction.NORTH, castle);
        dungeon.lockExit(Direction.NORTH,
            "The iron door is locked solid. You need the Ancient Key to open it.");
        initiallyLockedExits.add("UNDERGROUND_DUNGEON:NORTH");
        castle.setExit(Direction.SOUTH, dungeon);

        // Corrupted Castle ↔ Shadow Throne (locked by puzzle)
        castle.setExit(Direction.NORTH, shadowThrone);
        castle.lockExit(Direction.NORTH,
            "The sealed gate won't budge. A puzzle mechanism on the wall might hold the answer.");
        initiallyLockedExits.add("CORRUPTED_CASTLE:NORTH");
        shadowThrone.setExit(Direction.SOUTH, castle);

        // Corrupted Castle ↔ Shadow Barracks (new)
        castle.setExit(Direction.EAST, shadowBarracks);
        shadowBarracks.setExit(Direction.WEST, castle);

        // Corrupted Castle ↔ Cursed Archives (new)
        castle.setExit(Direction.WEST, cursedArchives);
        cursedArchives.setExit(Direction.EAST, castle);

        // ── Divine realm connections ────────────────────────────────────────

        // Forgotten Battlefield ↔ Celestial Gate
        forgottenBattlefield.setExit(Direction.NORTH, celestialGate);
        celestialGate.setExit(Direction.SOUTH, forgottenBattlefield);

        // Celestial Gate ↔ Sunken Shrine
        celestialGate.setExit(Direction.NORTH, sunkenShrine);
        sunkenShrine.setExit(Direction.SOUTH, celestialGate);

        // Sunken Shrine ↔ Divine Forge
        sunkenShrine.setExit(Direction.EAST, divineForge);
        divineForge.setExit(Direction.WEST, sunkenShrine);

        // Sunken Shrine ↔ Radiant Cathedral
        sunkenShrine.setExit(Direction.NORTH, radiantCathedral);
        radiantCathedral.setExit(Direction.SOUTH, sunkenShrine);

        // Radiant Cathedral ↔ Vault of the Fallen
        radiantCathedral.setExit(Direction.WEST, vaultOfFallen);
        vaultOfFallen.setExit(Direction.EAST, radiantCathedral);

        // Radiant Cathedral ↔ Celestial Barracks
        radiantCathedral.setExit(Direction.EAST, celestialBarracks);
        celestialBarracks.setExit(Direction.WEST, radiantCathedral);

        // Celestial Barracks ↔ Sanctum of the Arbiter
        celestialBarracks.setExit(Direction.SOUTH, sanctumArbiter);
        sanctumArbiter.setExit(Direction.NORTH, celestialBarracks);

        // ================================================================
        // LEVEL REQUIREMENTS
        // Ordered by intended progression — enforced in movePlayer()
        // ================================================================

        village.setLevelRequirement(1);
        darkForest.setLevelRequirement(1);
        merchantVillage.setLevelRequirement(1);
        provingGrounds.setLevelRequirement(1);
        ancientRuins.setLevelRequirement(2);
        mysticGlade.setLevelRequirement(3);
        dungeon.setLevelRequirement(4);
        forgottenBattlefield.setLevelRequirement(5);
        castle.setLevelRequirement(6);
        shadowBarracks.setLevelRequirement(7);
        cursedArchives.setLevelRequirement(7);
        shadowThrone.setLevelRequirement(8);
        celestialGate.setLevelRequirement(8);
        sunkenShrine.setLevelRequirement(9);
        divineForge.setLevelRequirement(9);
        radiantCathedral.setLevelRequirement(10);
        vaultOfFallen.setLevelRequirement(10);
        celestialBarracks.setLevelRequirement(11);
        sanctumArbiter.setLevelRequirement(12);

        // Mark the starting location as visited
        player.markVisited(LocationId.VILLAGE);

        // ================================================================
        // POPULATE — NPCs
        // ================================================================

        village.addNPC(new NPC(
            "Village Elder",
            "A weathered old man whose eyes carry the weight of the kingdom's grief.",
            new String[]{
                "Adventurer! You must help us. The Shadow Lord shattered the Ancient Relic " +
                "and now darkness spreads. Recover it from his throne room — it is our " +
                "only hope. Speak to Griswold the Merchant before you leave.",
                "Head east into the Dark Forest. In the Ancient Ruins to the north you'll " +
                "find an inscription — solve its riddle to claim the Ancient Key.",
                "The Ancient Key unlocks the dungeon passage to the Corrupted Castle. " +
                "Be brave, adventurer. The kingdom depends on you."
            }
        ));

        Merchant merchant = new Merchant();
        merchant.addShopItem(ItemFactory.create(ItemType.IRON_SWORD));
        merchant.addShopItem(ItemFactory.create(ItemType.LEATHER_CHESTPLATE));
        merchant.addShopItem(ItemFactory.create(ItemType.HEALTH_POTION));
        merchant.addShopItem(ItemFactory.create(ItemType.ELIXIR));
        merchant.addShopItem(ItemFactory.create(ItemType.VENOM_FLASK));
        merchant.addShopItem(ItemFactory.create(ItemType.SMOKE_BOMB));
        village.addNPC(merchant);

        // Merchant Village — Seraphina the Enchantress
        merchantVillage.addNPC(new Enchanter(
            "Seraphina the Enchantress",
            "A tall woman in robes that seem to shift colour in the light. Her eyes\n" +
            "  glow faintly violet — a side-effect, she says, of years working with\n" +
            "  shadow energy. She regards you with calm, professional curiosity."
        ));

        // Merchant Village — Aldric the Smith (second merchant, better stock)
        Merchant aldric = new Merchant(
            "Aldric the Smith",
            "A broad-shouldered man with forearms scarred by decades at the forge.\n" +
            "  His wares are expensive but noticeably well-made."
        );
        aldric.addShopItem(ItemFactory.create(ItemType.STEEL_SWORD));
        aldric.addShopItem(ItemFactory.create(ItemType.CHAINMAIL_VEST));
        aldric.addShopItem(ItemFactory.create(ItemType.IRON_HELMET));
        aldric.addShopItem(ItemFactory.create(ItemType.LEATHER_GREAVES));
        aldric.addShopItem(ItemFactory.create(ItemType.GREATER_ELIXIR));
        aldric.addShopItem(ItemFactory.create(ItemType.SHADOW_CRYSTAL));
        aldric.addShopItem(ItemFactory.create(ItemType.BATTLE_TONIC));
        merchantVillage.addNPC(aldric);

        // Proving Grounds — ArenaMaster NPC
        provingGrounds.addNPC(new ArenaMaster(
            "Goran the Arena Master",
            "A barrel-chested man with a shaved head and hands like shovels. Old\n" +
            "  scars map every fight he's overseen. He watches you with calculating eyes."
        ));

        // Merchant Village — quest giver NPC
        merchantVillage.addNPC(new NPC(
            "Wandering Scholar",
            "A thin man surrounded by scrolls, scribbling notes with ink-stained fingers.",
            new String[]{
                "Extraordinary! You've ventured from the north — through the shadow corruption? " +
                "I've been studying these creatures for years. The Void Wraiths in the castle " +
                "carry crystallised shadow energy in their bodies. If you recover some, " +
                "Seraphina can put it to remarkable use.",
                "I hear the Shadow Lord himself has fallen recently. Remarkable. And yet " +
                "something darker stirs beyond the throne... the corruption runs deeper " +
                "than anyone realised. Tread carefully if you press on.",
                "My notes are nearly complete. Fascinating times, truly fascinating."
            }
        ));

        castle.addNPC(new NPC(
            "Imprisoned Knight",
            "A knight in tattered armour, chained to the wall. He raises his head with effort.",
            new String[]{
                "You came... thank the heavens. The Shadow Lord's power is tied to the dark " +
                "crystals on his throne. Weaken him by striking them first.",
                "The puzzle on the wall controls the gate north. Solve it and the way to " +
                "the throne room will open. Check the east and west wings too — they hold " +
                "powerful relics that once belonged to the castle's defenders.",
                "Go. End this nightmare. I will hold on a little longer."
            }
        ));

        // ── Divine realm NPCs ───────────────────────────────────────────────

        // Celestial Gate — Brother Aldus (lore guide)
        celestialGate.addNPC(new NPC(
            "Brother Aldus",
            "A robed monk kneeling before the great arch, his skin faintly luminescent.\n" +
            "  He looks up with sorrowful, knowing eyes.",
            new String[]{
                "You bear the mark of a survivor — few reach this gate. Beyond lies the " +
                "Celestial Realm, a land of divine light twisted by a great corruption. " +
                "The Arbiter, once our highest judge, now sits at the heart of the Sanctum, " +
                "lost to grief and celestial madness. Tread carefully — the guardians here " +
                "no longer know friend from foe.",
                "In the Divine Forge to the east of the Sunken Shrine, Luminara the Soulsmith " +
                "still tends her flame. She can imbue your gear with divine power far beyond " +
                "shadow magic — but you will need a Divine Crystal and considerable gold. " +
                "Look for Divine Crystals on fallen enemies throughout this realm.",
                "The Vault of the Fallen lies west of the Radiant Cathedral. Our greatest " +
                "paladins left their armour there — take it, it belongs to those who fight. " +
                "The Sanctum of the Arbiter is south of the Barracks. Face him only when " +
                "you are ready. I pray you succeed where we could not."
            }
        ));

        // Divine Forge — Luminara the Soulsmith (DivineEnchanter)
        divineForge.addNPC(new DivineEnchanter(
            "Luminara the Soulsmith",
            "A woman of ageless bearing, her white hair coiled with golden thread.\n" +
            "  She works at the celestial forge with absolute precision, each movement\n" +
            "  deliberate. Pale light streams from her fingertips as she shapes metal."
        ));

        // Divine Forge — forge merchant (level 6 required)
        Merchant forgeMerchant = new Merchant(
            "Rowan the Supply Keeper",
            "A stocky, no-nonsense figure in reinforced leather, overseeing a well-organised\n" +
            "  collection of divine-realm gear and materials."
        );
        forgeMerchant.setLevelRequirement(6);
        forgeMerchant.addShopItem(ItemFactory.create(ItemType.HOLY_SPEAR));
        forgeMerchant.addShopItem(ItemFactory.create(ItemType.AUREATE_VISOR));
        forgeMerchant.addShopItem(ItemFactory.create(ItemType.BLESSED_HAUBERK));
        forgeMerchant.addShopItem(ItemFactory.create(ItemType.SACRED_GREAVES));
        forgeMerchant.addShopItem(ItemFactory.create(ItemType.DIVINE_TONIC));
        forgeMerchant.addShopItem(ItemFactory.create(ItemType.STONE_SKIN_DRAUGHT));
        forgeMerchant.addShopItem(ItemFactory.create(ItemType.DIVINE_CRYSTAL));
        divineForge.addNPC(forgeMerchant);

        // Radiant Cathedral — Broken Seraph (lore NPC)
        radiantCathedral.addNPC(new NPC(
            "Broken Seraph",
            "A once-magnificent warrior with shattered wings trailing like torn cloth.\n" +
            "  Golden ichor stains her white robes. Her eyes burn with faint awareness.",
            new String[]{
                "You are still uncorrupted. How? The light here devours those who were not " +
                "born of it — or destroys those who once were. Like me. My wings answered " +
                "a false call — the Arbiter's decree. Now I cannot leave this place.",
                "The Vault is to the west. Our fallen brothers left their armour on those " +
                "pedestals — they will not need it. Take what you can carry. Better used by " +
                "a living warrior than left to the corruption that is spreading through these halls.",
                "If you face the Arbiter... remember he was once just. The light in his eyes " +
                "that drives him now is not wisdom — it is grief turned to madness. Strike him " +
                "down and let him rest. That is the only mercy left."
            }
        ));

        // Celestial Barracks — CelestialWarden (arena NPC)
        celestialBarracks.addNPC(new CelestialWarden(
            "The Warden",
            "A hulking figure in tarnished celestial plate, one hand resting on a bell\n" +
            "  of pure crystal. His face is half-obscured by a damaged visor, his posture\n" +
            "  still military-precise despite the corruption eating at his armour."
        ));

        // Celestial Barracks — barracks merchant (level 9 required)
        Merchant barracksMerchant = new Merchant(
            "Selene the Quartermaster",
            "A sharp-eyed woman surrounded by crates of high-tier divine equipment.\n" +
            "  Her expression makes clear she has no patience for the unprepared."
        );
        barracksMerchant.setLevelRequirement(9);
        barracksMerchant.addShopItem(ItemFactory.create(ItemType.AUREATE_SWORD));
        barracksMerchant.addShopItem(ItemFactory.create(ItemType.CORRUPTED_MACE));
        barracksMerchant.addShopItem(ItemFactory.create(ItemType.DIVINE_TONIC));
        barracksMerchant.addShopItem(ItemFactory.create(ItemType.BATTLE_TONIC));
        barracksMerchant.addShopItem(ItemFactory.create(ItemType.BLINDING_POWDER));
        barracksMerchant.addShopItem(ItemFactory.create(ItemType.DIVINE_CRYSTAL));
        celestialBarracks.addNPC(barracksMerchant);

        // Tree Protector — in both NPC and enemy lists so the player can talk OR fight
        mysticGlade.addNPC(new NPC(
            "Tree Protector",
            "A towering figure woven from living bark and ancient roots. Two amber eyes\n" +
            "  glow warmly in a face shaped from gnarled wood. He radiates old magic.",
            new String[]{
                "I am the guardian of this grove — as old as the first oak. I sense the " +
                "shadow's corruption on your path. Will you prove yourself worthy of the " +
                "forest's blessing? Type 'befriend tree protector' to accept my friendship, " +
                "or 'attack tree protector' to take what you want by force.",
                "Choose peace and I will grant you the Evasive Boots — woven from root-fibre " +
                "and living wind, they will help you evade the darkness ahead.",
                "The ancient oak behind me has stood for a thousand years. Whatever happens " +
                "here today, its memory will endure."
            }
        ));
        mysticGlade.addEnemy(EnemyFactory.create(EnemyType.TREE_PROTECTOR_ENEMY));

        // ================================================================
        // POPULATE — Enemies (Factory pattern)
        // ================================================================

        darkForest.addEnemy(EnemyFactory.create(EnemyType.SHADOW_GOBLIN));
        darkForest.addEnemy(EnemyFactory.create(EnemyType.SHADOW_GOBLIN));
        darkForest.addEnemy(EnemyFactory.create(EnemyType.PLAGUE_RAT));

        forgottenBattlefield.addEnemy(EnemyFactory.create(EnemyType.PLAGUE_RAT));
        forgottenBattlefield.addEnemy(EnemyFactory.create(EnemyType.PLAGUE_RAT));
        forgottenBattlefield.addEnemy(EnemyFactory.create(EnemyType.STONE_SENTINEL));

        castle.addEnemy(EnemyFactory.create(EnemyType.DARK_KNIGHT));

        shadowBarracks.addEnemy(EnemyFactory.create(EnemyType.VOID_WRAITH));
        shadowBarracks.addEnemy(EnemyFactory.create(EnemyType.VOID_WRAITH));

        cursedArchives.addEnemy(EnemyFactory.create(EnemyType.VOID_WRAITH));

        shadowThrone.addEnemy(EnemyFactory.create(EnemyType.SHADOW_LORD));

        // ── Divine realm enemies ────────────────────────────────────────────

        sunkenShrine.addEnemy(EnemyFactory.create(EnemyType.CORRUPTED_PALADIN));
        sunkenShrine.addEnemy(EnemyFactory.create(EnemyType.CORRUPTED_PALADIN));
        sunkenShrine.addEnemy(EnemyFactory.create(EnemyType.RADIANT_STALKER));

        radiantCathedral.addEnemy(EnemyFactory.create(EnemyType.CORRUPTED_PALADIN));
        radiantCathedral.addEnemy(EnemyFactory.create(EnemyType.CORRUPTED_PALADIN));
        radiantCathedral.addEnemy(EnemyFactory.create(EnemyType.CELESTIAL_SENTINEL));
        radiantCathedral.addEnemy(EnemyFactory.create(EnemyType.RADIANT_STALKER));

        vaultOfFallen.addEnemy(EnemyFactory.create(EnemyType.FALLEN_SERAPH));
        vaultOfFallen.addEnemy(EnemyFactory.create(EnemyType.CELESTIAL_SENTINEL));

        sanctumArbiter.addEnemy(EnemyFactory.create(EnemyType.THE_ARBITER));

        // ================================================================
        // POPULATE — Ground items
        // ================================================================

        forgottenBattlefield.addItem(ItemFactory.create(ItemType.HEALTH_POTION));
        forgottenBattlefield.addItem(ItemFactory.create(ItemType.BLINDING_POWDER));
        shadowBarracks.addItem(ItemFactory.create(ItemType.SHADOW_ROBE));

        // Divine realm ground items
        vaultOfFallen.addItem(ItemFactory.create(ItemType.SERAPH_CROWN));
        vaultOfFallen.addItem(ItemFactory.create(ItemType.SANCTUM_PLATE));
        vaultOfFallen.addItem(ItemFactory.create(ItemType.SERAPH_TASSETS));

        // ================================================================
        // POPULATE — Puzzles
        // ================================================================

        // Ancient Ruins — riddle rewards the Ancient Key
        ancientRuins.setPuzzle(new RiddlePuzzle(
            "I have cities but no houses, mountains but no trees, and water but no fish. " +
            "What am I?",
            "map",
            ItemFactory.create(ItemType.ANCIENT_KEY),
            null
        ));

        // Underground Dungeon — lever sequence rewards the Shadow Blade
        dungeon.setPuzzle(new LeverPuzzle(
            new int[]{1, 3, 2},
            ItemFactory.create(ItemType.SHADOW_BLADE),
            null
        ));

        // Corrupted Castle — riddle unlocks the gate to the Shadow Throne
        castle.setPuzzle(new RiddlePuzzle(
            "I speak without a mouth, am heard without ears, and have no body yet " +
            "come alive with wind. What am I?",
            "echo",
            null,
            Direction.NORTH
        ));

        // Forgotten Battlefield — ancient runic tablet, rewards Battle Axe
        forgottenBattlefield.setPuzzle(new RiddlePuzzle(
            "The more you take, the more you leave behind. What am I?",
            "footsteps",
            ItemFactory.create(ItemType.BATTLE_AXE),
            null
        ));

        // Sunken Shrine — celestial riddle, rewards Aureate Sword
        sunkenShrine.setPuzzle(new RiddlePuzzle(
            "I have a body but no arms, a head but no eyes. I shed tears but feel no " +
            "pain, and I grow shorter as I give light. What am I?",
            "candle",
            ItemFactory.create(ItemType.AUREATE_SWORD),
            null
        ));

        // Cursed Archives — 4-lever shadow sequence, rewards Elixir
        cursedArchives.setPuzzle(new LeverPuzzle(
            "Shadow Archive Seals",
            "Four shadow runes are carved above the levers, each pulsing darkly.",
            "The runes decree: third eldest, then youngest, then eldest, then second eldest.",
            new int[]{3, 1, 4, 2},
            ItemFactory.create(ItemType.ELIXIR),
            null
        ));

        // ================================================================
        // REGISTER LOCATIONS AND SET START
        // ================================================================

        locations.put(LocationId.VILLAGE,                 village);
        locations.put(LocationId.DARK_FOREST,             darkForest);
        locations.put(LocationId.ANCIENT_RUINS,           ancientRuins);
        locations.put(LocationId.UNDERGROUND_DUNGEON,     dungeon);
        locations.put(LocationId.CORRUPTED_CASTLE,        castle);
        locations.put(LocationId.SHADOW_THRONE,           shadowThrone);
        locations.put(LocationId.MYSTIC_GLADE,            mysticGlade);
        locations.put(LocationId.FORGOTTEN_BATTLEFIELD,   forgottenBattlefield);
        locations.put(LocationId.SHADOW_BARRACKS,         shadowBarracks);
        locations.put(LocationId.CURSED_ARCHIVES,         cursedArchives);
        locations.put(LocationId.MERCHANT_VILLAGE,        merchantVillage);
        locations.put(LocationId.PROVING_GROUNDS,         provingGrounds);
        // Divine realm
        locations.put(LocationId.CELESTIAL_GATE,          celestialGate);
        locations.put(LocationId.SUNKEN_SHRINE,           sunkenShrine);
        locations.put(LocationId.DIVINE_FORGE,            divineForge);
        locations.put(LocationId.RADIANT_CATHEDRAL,       radiantCathedral);
        locations.put(LocationId.CELESTIAL_BARRACKS,      celestialBarracks);
        locations.put(LocationId.VAULT_OF_THE_FALLEN,     vaultOfFallen);
        locations.put(LocationId.SANCTUM_OF_THE_ARBITER,  sanctumArbiter);

        currentLocation = village;
    }

    // -------------------------------------------------------------------------
    // Movement
    // -------------------------------------------------------------------------

    /**
     * Attempts to move the player one step in the given direction.
     *
     * Locked exits:
     *   - Dungeon → Castle: requires the Ancient Key (consumed on use).
     *   - Castle  → Throne: locked by the castle puzzle.
     *
     * @return true if movement succeeded, false if blocked
     */
    public boolean movePlayer(Direction dir) {
        if (!currentLocation.hasExit(dir)) {
            System.out.println("You can't go " + dir.name().toLowerCase() + " from here.");
            return false;
        }

        if (currentLocation.isExitLocked(dir)) {
            if (currentLocation.getId() == LocationId.UNDERGROUND_DUNGEON
                    && dir == Direction.NORTH) {
                Item key = player.getInventory().findItem("ancient key");
                if (key != null) {
                    currentLocation.unlockExit(dir);
                    player.getInventory().removeItem(key);
                    System.out.println("You insert the Ancient Key into the iron lock.");
                    System.out.println("The door swings open with a heavy groan.");
                } else {
                    System.out.println(currentLocation.getLockMessage(dir));
                    return false;
                }
            } else {
                System.out.println(currentLocation.getLockMessage(dir));
                return false;
            }
        }

        // Level gate — checked after locks so the lock message takes priority
        Location destination = currentLocation.getExit(dir);
        int req = destination.getLevelRequirement();
        if (player.getLevel() < req) {
            System.out.println("  The path ahead feels overwhelming. You must reach level "
                    + req + " before venturing there.  (Your level: " + player.getLevel() + ")");
            return false;
        }

        currentLocation = destination;
        player.markVisited(currentLocation.getId());
        return true;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Location getCurrentLocation()       { return currentLocation; }
    public Location getLocation(LocationId id) { return locations.get(id); }

    public Map<LocationId, Integer> getLevelRequirements() {
        Map<LocationId, Integer> map = new EnumMap<>(LocationId.class);
        for (Map.Entry<LocationId, Location> e : locations.entrySet())
            map.put(e.getKey(), e.getValue().getLevelRequirement());
        return map;
    }

    // -------------------------------------------------------------------------
    // Save / load support
    // -------------------------------------------------------------------------

    /**
     * Captures a complete snapshot of current world state for serialization.
     * Enemy counts cover every location (0 = all defeated).
     * Ground items are saved by display name for reconstruction via ItemFactory.
     */
    public GameSnapshot captureState(Player p, Difficulty d) {
        Map<String, Integer>      enemyCounts  = new HashMap<>();
        Map<String, List<String>> groundItems  = new HashMap<>();
        Set<String>               solved        = new HashSet<>();
        Set<String>               unlocked      = new HashSet<>();

        for (Map.Entry<LocationId, Location> entry : locations.entrySet()) {
            String  key = entry.getKey().name();
            Location loc = entry.getValue();

            enemyCounts.put(key, loc.getEnemies().size());

            groundItems.put(key, loc.getItems().stream()
                    .map(Item::getName)
                    .collect(Collectors.toList()));

            if (loc.hasPuzzle() && loc.getPuzzle().isSolved())
                solved.add(key);
        }

        for (String exitKey : initiallyLockedExits) {
            String[] parts  = exitKey.split(":");
            LocationId locId = LocationId.valueOf(parts[0]);
            Direction  dir   = Direction.valueOf(parts[1]);
            Location loc = locations.get(locId);
            if (loc != null && !loc.isExitLocked(dir))
                unlocked.add(exitKey);
        }

        return new GameSnapshot(p, d, currentLocation.getId(),
                                enemyCounts, groundItems, solved, unlocked);
    }

    /**
     * Applies a saved snapshot to this freshly-built world.
     * After this call the world matches the state at the time of the save.
     */
    public void applyState(GameSnapshot snapshot) {
        // ── Enemy counts ────────────────────────────────────────────────────
        for (Map.Entry<String, Integer> entry : snapshot.enemyCounts.entrySet()) {
            try {
                Location loc = locations.get(LocationId.valueOf(entry.getKey()));
                if (loc == null) continue;
                List<characters.Enemy> enemies = new ArrayList<>(loc.getEnemies());
                int target = entry.getValue();
                while (enemies.size() > target)
                    loc.removeEnemy(enemies.remove(enemies.size() - 1));
            } catch (IllegalArgumentException ignored) {}
        }

        // ── Ground items ────────────────────────────────────────────────────
        for (Map.Entry<String, List<String>> entry : snapshot.groundItems.entrySet()) {
            try {
                Location loc = locations.get(LocationId.valueOf(entry.getKey()));
                if (loc == null) continue;
                // Clear current items
                new ArrayList<>(loc.getItems()).forEach(loc::removeItem);
                // Re-add only the ones that were still there at save time
                for (String name : entry.getValue()) {
                    Item item = ItemFactory.findByName(name);
                    if (item != null) loc.addItem(item);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        // ── Solved puzzles ──────────────────────────────────────────────────
        for (String key : snapshot.solvedPuzzles) {
            try {
                Location loc = locations.get(LocationId.valueOf(key));
                if (loc != null && loc.hasPuzzle()) loc.getPuzzle().markSolved();
            } catch (IllegalArgumentException ignored) {}
        }

        // ── Unlocked exits ──────────────────────────────────────────────────
        for (String exitKey : snapshot.unlockedExits) {
            try {
                String[]  parts = exitKey.split(":");
                Location  loc   = locations.get(LocationId.valueOf(parts[0]));
                Direction dir   = Direction.valueOf(parts[1]);
                if (loc != null) loc.unlockExit(dir);
            } catch (IllegalArgumentException ignored) {}
        }

        // ── Current location ────────────────────────────────────────────────
        Location saved = locations.get(snapshot.locationId);
        if (saved != null) currentLocation = saved;
    }
}
