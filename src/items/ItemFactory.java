package items;

/**
 * ItemFactory — Factory design pattern for item creation (creational).
 *
 * Mirrors EnemyFactory: every item in the game is created here, so item
 * definitions (name, stats, description, gold value) are maintained in one
 * place rather than duplicated across World, Puzzle, and Enemy loot tables.
 *
 * WHY FACTORY HERE:
 *   Items appear in multiple contexts: the Merchant sells them, puzzles
 *   reward them, and enemies drop them.  If each caller used 'new Weapon(...)'
 *   directly, changing a stat (e.g., boosting the Shadow Blade's attack bonus
 *   from 15 to 18) would require finding and editing every occurrence.
 *   With ItemFactory, it's a one-line change.
 *
 * HOW TO EXTEND:
 *   1. Add a constant to ItemType.
 *   2. Add a private create method here.
 *   3. Add a case to the switch.
 *   No caller changes required.
 */
public class ItemFactory {

    /** Utility class — prevent instantiation. */
    private ItemFactory() {}

    // -------------------------------------------------------------------------
    // Reverse name lookup — used by the save system to reconstruct ground items
    // -------------------------------------------------------------------------

    private static final java.util.Map<String, ItemType> NAME_TO_TYPE = buildNameMap();

    private static java.util.Map<String, ItemType> buildNameMap() {
        java.util.Map<String, ItemType> map = new java.util.HashMap<>();
        for (ItemType type : ItemType.values()) {
            try { map.put(create(type).getName().toLowerCase(), type); }
            catch (Exception ignored) {}
        }
        return java.util.Collections.unmodifiableMap(map);
    }

    /**
     * Creates a new item whose display name matches the given string (case-insensitive).
     * Returns null for enchanted items or unknown names — callers must handle null.
     */
    public static Item findByName(String name) {
        if (name == null) return null;
        ItemType type = NAME_TO_TYPE.get(name.toLowerCase().trim());
        return type != null ? create(type) : null;
    }

    // -------------------------------------------------------------------------
    // Factory method
    // -------------------------------------------------------------------------

    /**
     * Creates a new item of the given type.
     *
     * @param type the kind of item to create
     * @return a new Item instance fully configured and ready to use
     * @throws IllegalArgumentException if the type is unrecognised
     */
    public static Item create(ItemType type) {
        switch (type) {
            // Weapons — shadow map
            case IRON_SWORD:     return createIronSword();
            case STEEL_SWORD:    return createSteelSword();
            case SHADOW_BLADE:   return createShadowBlade();
            case BATTLE_AXE:     return createBattleAxe();

            // Weapons — divine realm
            case HOLY_SPEAR:       return createHolySpear();
            case AUREATE_SWORD:    return createAureateSword();
            case CORRUPTED_MACE:   return createCorruptedMace();
            case SERAPHIC_BLADE:   return createSeraphicBlade();

            // Armour — head
            case IRON_HELMET:      return createIronHelmet();
            case AUREATE_VISOR:    return createAureateVisor();
            case SERAPH_CROWN:     return createSeraphCrown();

            // Armour — torso
            case LEATHER_CHESTPLATE: return createLeatherChestplate();
            case CHAINMAIL_VEST:     return createChainmailVest();
            case SHADOW_ROBE:        return createShadowRobe();
            case BLESSED_HAUBERK:    return createBlessedHauberk();
            case SANCTUM_PLATE:      return createSanctumPlate();

            // Armour — legs
            case LEATHER_GREAVES:  return createLeatherGreaves();
            case EVASIVE_BOOTS:    return createEvasiveBoots();
            case SACRED_GREAVES:   return createSacredGreaves();
            case SERAPH_TASSETS:   return createSeraphTassets();

            // Consumables — healing
            case HEALTH_POTION:  return createHealthPotion();
            case ELIXIR:         return createElixir();
            case GREATER_ELIXIR: return createGreaterElixir();
            case DIVINE_TONIC:   return createDivineTonic();

            // Consumables — combat potions
            case VENOM_FLASK:        return createVenomFlask();
            case BLINDING_POWDER:    return createBlindingPowder();
            case BATTLE_TONIC:       return createBattleTonic();
            case STONE_SKIN_DRAUGHT: return createStoneSkinDraught();
            case SMOKE_BOMB:         return createSmokeBomb();

            // Keys
            case ANCIENT_KEY:    return createAncientKey();

            // Quest / special items
            case ANCIENT_RELIC:  return createAncientRelic();
            case FOREST_GEM:     return createForestGem();

            // Crafting materials
            case SHADOW_CRYSTAL: return createShadowCrystal();
            case DIVINE_CRYSTAL: return createDivineCrystal();

            default:
                throw new IllegalArgumentException("Unknown item type: " + type);
        }
    }

    // -------------------------------------------------------------------------
    // Weapons
    // -------------------------------------------------------------------------

    private static Weapon createIronSword() {
        return new Weapon(
            "Iron Sword",
            "A well-balanced iron sword with a keen edge. Reliable, if unremarkable.",
            15,   // gold value (buy price)
            8     // attack bonus
        );
    }

    private static Weapon createSteelSword() {
        return new Weapon(
            "Steel Sword",
            "A finely forged steel blade — heavier than iron, but the balance is superb.",
            30,   // gold value
            12    // attack bonus
        );
    }

    private static Weapon createShadowBlade() {
        return new Weapon(
            "Shadow Blade",
            "A blade forged from crystallised shadow energy. It hums with dark power\n" +
            "  and feels unnaturally light in your hand.",
            0,    // not for sale
            15    // attack bonus
        );
    }

    /**
     * Battle Axe — a heavy two-handed weapon found on the Forgotten Battlefield.
     * Higher attack than the Iron Sword but slower feel (lore-only difference).
     */
    private static Weapon createBattleAxe() {
        return new Weapon(
            "Battle Axe",
            "A weathered battle axe salvaged from the ruined battlefield. Heavy and\n" +
            "  brutal — each swing carries the weight of a forgotten war.",
            18,   // sell value
            12    // attack bonus
        );
    }

    private static Weapon createHolySpear() {
        return new Weapon(
            "Holy Spear",
            "A long spear tipped with a head of condensed holy light. It hums faintly\n" +
            "  and feels warm even in the coldest reaches of the divine realm.",
            65,   // gold value
            18    // attack bonus
        );
    }

    private static Weapon createAureateSword() {
        return new Weapon(
            "Aureate Sword",
            "A blade forged from gold-veined divine steel. Its edge never dulls and\n" +
            "  a faint luminescence trails each swing.",
            80,   // gold value
            20    // attack bonus
        );
    }

    private static Weapon createCorruptedMace() {
        return new Weapon(
            "Corrupted Mace",
            "A heavy mace once carried by a paladin of the celestial order. Dark\n" +
            "  cracks run along the head where shadow energy has seeped into the metal.",
            0,    // drop only
            17    // attack bonus
        );
    }

    private static Weapon createSeraphicBlade() {
        return new Weapon(
            "Seraphic Blade",
            "The Arbiter's own weapon — a sword of pure celestial energy that burns\n" +
            "  cold as starlight. The most powerful blade in existence.",
            0,    // boss drop, not for sale
            25    // attack bonus
        );
    }

    // -------------------------------------------------------------------------
    // Armour
    // -------------------------------------------------------------------------

    private static Armour createIronHelmet() {
        return new Armour(
            "Iron Helmet",
            "A solid iron helm that covers the skull and cheeks. Dented but dependable.",
            25,              // gold value
            3,               // defense bonus
            ArmourSlot.HEAD
        );
    }

    private static Armour createLeatherChestplate() {
        return new Armour(
            "Leather Chestplate",
            "A sturdy tanned leather chestplate — not glamorous, but it keeps you alive.",
            20,             // gold value
            3,              // defense bonus
            ArmourSlot.TORSO
        );
    }

    private static Armour createChainmailVest() {
        return new Armour(
            "Chainmail Vest",
            "Interlocking iron rings form a flexible but tough vest. Heavier than leather\n" +
            "  but far more resistant to blades and claws.",
            35,               // gold value
            5,                // defense bonus
            ArmourSlot.TORSO
        );
    }

    /** Shadow Robe — found in the Shadow Barracks. Torso slot; cannot stack with Leather Chestplate. */
    private static Armour createShadowRobe() {
        return new Armour(
            "Shadow Robe",
            "A robe woven from compressed shadow energy. Cold to the touch but\n" +
            "  surprisingly resilient — the darkness itself deflects blows.",
            0,              // treasure, not for sale
            6,              // defense bonus
            ArmourSlot.TORSO
        );
    }

    private static Armour createLeatherGreaves() {
        return new Armour(
            "Leather Greaves",
            "Thick leather leg guards stitched with reinforced panels. Simple but effective.",
            20,              // gold value
            2,               // defense bonus
            ArmourSlot.LEGS
        );
    }

    /** Evasive Boots — gifted by the Tree Protector. Legs slot; adds 20% enemy miss-chance. */
    private static Armour createEvasiveBoots() {
        return new Armour(
            "Evasive Boots",
            "Boots woven from ancient root-fibre and living wind. Light as a leaf,\n" +
            "  they shift your weight just enough to make enemies swing wide.",
            0,              // gift, not for sale
            1,              // defense bonus
            0.20,           // +20% enemy miss chance
            ArmourSlot.LEGS
        );
    }

    // ── Divine realm armour ──────────────────────────────────────────────────

    private static Armour createAureateVisor() {
        return new Armour(
            "Aureate Visor",
            "A polished visor hammered from divine gold-steel. Light but surprisingly\n" +
            "  resilient — the celestial alloy disperses impact across the whole helm.",
            45,
            5,
            ArmourSlot.HEAD
        );
    }

    private static Armour createSeraphCrown() {
        return new Armour(
            "Seraph's Crown",
            "A crown of woven divine metal recovered from the Vault of the Fallen.\n" +
            "  Once worn by the highest rank of the celestial order — its power endures.",
            0,    // vault loot
            9,
            ArmourSlot.HEAD
        );
    }

    private static Armour createBlessedHauberk() {
        return new Armour(
            "Blessed Hauberk",
            "A coat of interlocking divine-alloy rings, blessed by the celestial forge.\n" +
            "  Heavier than chainmail but the radiance it exudes deflects shadow energy.",
            60,
            7,
            ArmourSlot.TORSO
        );
    }

    private static Armour createSanctumPlate() {
        return new Armour(
            "Sanctum Plate",
            "Thick plate armour recovered from the sanctum's inner vault. Engraved with\n" +
            "  celestial sigils that glow faintly — evidence of rituals long forgotten.",
            0,    // vault loot
            12,
            ArmourSlot.TORSO
        );
    }

    private static Armour createSacredGreaves() {
        return new Armour(
            "Sacred Greaves",
            "Leg armour blessed at the divine forge. The sacred sigils etched into the\n" +
            "  metal seem to guide your footing, making you harder to track.",
            50,
            6,
            0.05,  // +5% enemy miss chance
            ArmourSlot.LEGS
        );
    }

    private static Armour createSeraphTassets() {
        return new Armour(
            "Seraph's Tassets",
            "Heavy plate tassets recovered from the Vault of the Fallen, forged for the\n" +
            "  celestial order's elite guard. Near-indestructible and immaculately balanced.",
            0,    // vault loot
            9,
            ArmourSlot.LEGS
        );
    }

    // -------------------------------------------------------------------------
    // Consumables
    // -------------------------------------------------------------------------

    private static Potion createHealthPotion() {
        return new Potion(
            "Health Potion",
            "A small flask of glowing red liquid. Restores 40 HP when consumed.",
            10,   // gold value
            40    // heal amount
        );
    }

    /**
     * Elixir — a stronger restorative found deep in the shadow domain.
     * Sold by Griswold and rewarded by the Cursed Archives puzzle.
     */
    private static Potion createGreaterElixir() {
        return new Potion(
            "Greater Elixir",
            "A deep crimson vial that radiates intense warmth. Restores 100 HP when consumed.",
            35,   // gold value
            100   // heal amount
        );
    }

    private static Potion createElixir() {
        return new Potion(
            "Elixir",
            "A luminous silver vial that radiates warmth. Restores 70 HP when consumed.",
            20,   // gold value
            70    // heal amount
        );
    }

    private static Potion createDivineTonic() {
        return new Potion(
            "Divine Tonic",
            "A radiant golden vial that pulses with celestial energy. Restores 160 HP.",
            60,   // gold value
            160   // heal amount
        );
    }

    // ── Combat potions — usable mid-fight ────────────────────────────────────

    private static CombatPotion createVenomFlask() {
        return new CombatPotion(
            "Venom Flask",
            "A small vial of concentrated venom. Hurl it at an enemy to coat them\n" +
            "  in a fast-acting poison. Sold by merchants throughout the kingdom.",
            28,
            CombatPotion.Effect.DEBUFF_POISON,
            6,    // damage per turn
            4     // turns
        );
    }

    private static CombatPotion createBlindingPowder() {
        return new CombatPotion(
            "Blinding Powder",
            "A pouch of fine choking dust. Thrown in an enemy's face it severely\n" +
            "  impairs their vision, making them far more likely to miss.",
            25,
            CombatPotion.Effect.DEBUFF_BLIND,
            30,   // +30% enemy miss chance
            2     // turns
        );
    }

    private static CombatPotion createBattleTonic() {
        return new CombatPotion(
            "Battle Tonic",
            "A bitter red draught favoured by mercenaries. Floods the body with\n" +
            "  adrenaline, temporarily sharpening every strike.",
            32,
            CombatPotion.Effect.BUFF_ATTACK,
            10,   // +10 attack
            3     // turns
        );
    }

    private static CombatPotion createStoneSkinDraught() {
        return new CombatPotion(
            "Stone Skin Draught",
            "A thick grey liquid that causes the skin to harden temporarily on\n" +
            "  contact. Sought after by front-line fighters venturing into divine ruins.",
            35,
            CombatPotion.Effect.BUFF_DEFENSE,
            8,    // +8 defense
            3     // turns
        );
    }

    private static CombatPotion createSmokeBomb() {
        return new CombatPotion(
            "Smoke Bomb",
            "A compact clay sphere filled with choking smoke. Shatters on impact\n" +
            "  and engulfs the enemy, causing them to lose their next attack.",
            30,
            CombatPotion.Effect.DEBUFF_STUN,
            0,    // no amount (stun only)
            1     // one turn skip
        );
    }

    // -------------------------------------------------------------------------
    // Keys
    // -------------------------------------------------------------------------

    private static Key createAncientKey() {
        return new Key(
            "Ancient Key",
            "An ornate iron key engraved with faintly glowing runes. It radiates a\n" +
            "  faint warmth, as though it remembers what it was made to protect."
        );
    }

    // -------------------------------------------------------------------------
    // Quest / special items
    // -------------------------------------------------------------------------

    private static QuestItem createAncientRelic() {
        return new QuestItem(
            "Ancient Relic",
            "A crystalline orb that pulses with warm golden light. This is what you came for.",
            "The Ancient Relic — a crystalline orb of boundless power. The Shadow Lord " +
            "shattered it to plunge the kingdom into darkness. Holding it now, you feel " +
            "its light pushing back the shadows. The kingdom can be saved."
        );
    }

    /**
     * Forest Gem — a rare gem nestled inside the Tree Protector's sacred oak.
     * Has no buy price (unobtainable from shops) but triggers special merchant
     * dialogue and a gold reward of 50–100 when sold to Griswold.
     */
    private static MaterialItem createShadowCrystal() {
        return new MaterialItem(
            "Shadow Crystal",
            "A shard of crystallised shadow energy, still pulsing faintly with dark magic.\n" +
            "  An enchanter could use this to bind power to your equipment.",
            "A Shadow Crystal — raw, concentrated shadow energy given physical form.\n" +
            "  Seraphina the Enchantress can use this to enchant your gear for 30 gold.",
            60    // gold value — sold by second merchant, sell-back at 30g
        );
    }

    private static QuestItem createForestGem() {
        return new QuestItem(
            "Forest Gem",
            "A flawless gem that pulses with soft emerald light. It is warm to the\n" +
            "  touch, as though the ancient grove still lives within it.",
            "A Forest Gem — extraordinarily rare, born from centuries of grove magic.\n" +
            "  Merchants would pay handsomely for something like this."
        );
    }

    private static MaterialItem createDivineCrystal() {
        return new MaterialItem(
            "Divine Crystal",
            "A shard of crystallised celestial energy, pulsing with warm golden light.\n" +
            "  Luminara the Soulsmith can channel this into divine enchantments.",
            "A Divine Crystal — concentrated celestial power given physical form.\n" +
            "  Luminara in the Divine Forge can use this to enchant your gear.",
            100   // gold value — substantially more than Shadow Crystal
        );
    }
}
