package items;

import characters.Player;

/**
 * CombatPotion — a throwable or consumable item usable during combat.
 *
 * Unlike a regular Potion (which heals HP), CombatPotions apply temporary
 * stat buffs to the player or debuffs to the enemy.  They do this by writing
 * pending-effect state onto the Player; CombatSystem reads and clears that
 * state each turn.
 *
 * This keeps CombatPotion decoupled from CombatSystem: the potion just sets
 * state on Player, and CombatSystem is the only place that acts on it.
 *
 * Combat potions are always consumed on use (removed from inventory).
 * They can only be used inside combat; Game.handleUse() outside combat will
 * still call use() which is safe — buffs set there carry into the next fight.
 */
public class CombatPotion extends Item {

    public enum Effect {
        BUFF_ATTACK,    // temporary player attack bonus
        BUFF_DEFENSE,   // temporary player defense bonus
        DEBUFF_POISON,  // apply poison DoT to enemy
        DEBUFF_BLIND,   // increase enemy miss chance for N turns
        DEBUFF_STUN     // enemy skips their next attack entirely
    }

    private final Effect effect;
    private final int    amount;    // attack/defense bonus OR poison damage OR blind miss %
    private final int    duration;  // turns the effect lasts (1 for DEBUFF_STUN)

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public CombatPotion(String name, String description, int value,
                        Effect effect, int amount, int duration) {
        super(name, description, value);
        this.effect   = effect;
        this.amount   = amount;
        this.duration = duration;
    }

    // -------------------------------------------------------------------------
    // Use
    // -------------------------------------------------------------------------

    @Override
    public void use(Player player) {
        player.getInventory().removeItem(this);
        switch (effect) {
            case BUFF_ATTACK:
                player.addCombatAttackBonus(amount, duration);
                System.out.println("You drink the " + getName() + ".");
                System.out.println("  A surge of power flows through your arms! +"
                        + amount + " attack for " + duration + " turns.");
                break;
            case BUFF_DEFENSE:
                player.addCombatDefenseBonus(amount, duration);
                System.out.println("You drink the " + getName() + ".");
                System.out.println("  Your skin hardens like stone! +"
                        + amount + " defense for " + duration + " turns.");
                break;
            case DEBUFF_POISON:
                player.setPendingEnemyPoison(amount, duration);
                System.out.println("You hurl the " + getName() + " at your enemy!");
                System.out.println("  Venom coats them — " + amount
                        + " damage per turn for " + duration + " turns.");
                break;
            case DEBUFF_BLIND:
                player.setPendingEnemyBlind(amount, duration);
                System.out.println("You fling the " + getName() + " into your enemy's face!");
                System.out.println("  Choking powder blinds them — +" + amount
                        + "% miss chance for " + duration + " turns.");
                break;
            case DEBUFF_STUN:
                player.setPendingEnemyStun();
                System.out.println("You hurl the " + getName() + " at your enemy!");
                System.out.println("  A dense cloud of smoke erupts — they lose their next attack!");
                break;
        }
    }

    // -------------------------------------------------------------------------
    // Description
    // -------------------------------------------------------------------------

    @Override
    public String getDescription() {
        switch (effect) {
            case BUFF_ATTACK:
                return super.getDescription()
                        + "  [combat buff: +" + amount + " attack for " + duration + " turns]";
            case BUFF_DEFENSE:
                return super.getDescription()
                        + "  [combat buff: +" + amount + " defense for " + duration + " turns]";
            case DEBUFF_POISON:
                return super.getDescription()
                        + "  [combat debuff: poisons enemy — " + amount
                        + " dmg/turn for " + duration + " turns]";
            case DEBUFF_BLIND:
                return super.getDescription()
                        + "  [combat debuff: +" + amount + "% enemy miss chance for "
                        + duration + " turns]";
            case DEBUFF_STUN:
                return super.getDescription()
                        + "  [combat debuff: enemy loses next attack]";
            default:
                return super.getDescription();
        }
    }

    // -------------------------------------------------------------------------
    // Accessors (read by CombatSystem)
    // -------------------------------------------------------------------------

    public Effect getEffect()   { return effect; }
    public int    getAmount()   { return amount; }
    public int    getDuration() { return duration; }
}
