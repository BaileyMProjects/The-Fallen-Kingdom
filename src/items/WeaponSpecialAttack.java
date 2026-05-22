package items;

import java.io.Serializable;

/**
 * WeaponSpecialAttack — data for one named special move on a Weapon.
 *
 * Added to weapons via Weapon.addSpecialAttack(). CombatSystem displays
 * available specials in the combat menu and executes them when chosen.
 * Serializable so weapons with specials survive save/load.
 */
public class WeaponSpecialAttack implements Serializable {

    private static final long serialVersionUID = 1L;

    public final String  name;
    public final String  flavour;
    public final double  multiplier;      // player base attack * multiplier
    public final boolean ignoresDefense;  // if true: skip enemy defense subtraction
    public final double  healPercent;     // fraction of damage healed to player (0.0 = none)
    public final double  stunChance;      // probability enemy loses their next turn (0.0 = none)
    public final int     cooldownTurns;   // turns before usable again

    public WeaponSpecialAttack(String name, String flavour, double multiplier,
                                boolean ignoresDefense, double healPercent,
                                double stunChance, int cooldownTurns) {
        this.name           = name;
        this.flavour        = flavour;
        this.multiplier     = multiplier;
        this.ignoresDefense = ignoresDefense;
        this.healPercent    = healPercent;
        this.stunChance     = stunChance;
        this.cooldownTurns  = cooldownTurns;
    }
}
