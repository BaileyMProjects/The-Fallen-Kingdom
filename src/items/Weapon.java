package items;

import characters.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Weapon — an equippable item that increases the player's attack power.
 *
 * Calling use() delegates to Player.equip(), which handles swapping out a
 * previously equipped weapon and adjusting the attack stat.  The weapon itself
 * stays simple: it just knows its attack bonus.
 */
public class Weapon extends Item {

    private final int attackBonus;

    private final List<WeaponSpecialAttack> specials = new ArrayList<>();
    private transient Map<String, Integer>  cooldowns;

    public Weapon(String name, String description, int value, int attackBonus) {
        super(name, description, value);
        this.attackBonus = attackBonus;
    }

    /**
     * Equips this weapon on the player.
     * Delegates to Player.equip() so that stat adjustment logic lives in one place.
     */
    @Override
    public void use(Player player) {
        player.equip(this);
    }

    public int getAttackBonus() { return attackBonus; }

    // -------------------------------------------------------------------------
    // Special attacks
    // -------------------------------------------------------------------------

    public void addSpecialAttack(WeaponSpecialAttack s) { specials.add(s); }
    public List<WeaponSpecialAttack> getSpecials() { return Collections.unmodifiableList(specials); }

    // -------------------------------------------------------------------------
    // Cooldown tracking (transient — resets on load, which is intentional)
    // -------------------------------------------------------------------------

    private Map<String, Integer> cooldownMap() {
        if (cooldowns == null) cooldowns = new HashMap<>();
        return cooldowns;
    }

    public int  getCooldown(String name)              { Integer v = cooldownMap().get(name); return v != null ? v : 0; }
    public void setCooldown(String name, int turns)   { cooldownMap().put(name, turns); }
    public void tickCooldowns()                       { cooldownMap().replaceAll((k, v) -> Math.max(0, v - 1)); }
    public void resetCooldowns()                      { cooldownMap().clear(); }

    @Override
    public String getDescription() {
        return super.getDescription() + "  [+" + attackBonus + " attack]";
    }
}
