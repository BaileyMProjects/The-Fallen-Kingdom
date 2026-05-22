package combat;

/**
 * EnemySpecialAttack — data for one named special move an enemy can perform.
 *
 * CombatSystem rolls against chance each turn before the normal attack.
 * If it fires, executeEnemySpecial() uses these fields instead of the
 * standard AttackStrategy path.
 */
public class EnemySpecialAttack {

    public final String  name;
    public final String  flavour;
    public final double  chance;
    public final double  damageMultiplier; // applied to (attack - defense) or raw attack if ignoresDefense
    public final boolean ignoresDefense;   // if true: damage = attack * multiplier (no defense subtraction)
    public final int     selfHeal;         // enemy heals this many HP after dealing damage (0 = none)
    public final boolean stunPlayer;       // player cannot act next turn

    public EnemySpecialAttack(String name, String flavour, double chance,
                               double damageMultiplier, boolean ignoresDefense,
                               int selfHeal, boolean stunPlayer) {
        this.name             = name;
        this.flavour          = flavour;
        this.chance           = chance;
        this.damageMultiplier = damageMultiplier;
        this.ignoresDefense   = ignoresDefense;
        this.selfHeal         = selfHeal;
        this.stunPlayer       = stunPlayer;
    }
}
