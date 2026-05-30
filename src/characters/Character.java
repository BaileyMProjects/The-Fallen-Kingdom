package characters;

/**
 * Character — abstract base class for every living entity in the game.
 *
 * Defines the shared state and behaviour that Player, Enemy, and NPC all
 * inherit: a name, a health pool, attack power, and a defence rating.
 * Concrete subclasses add only what makes them distinct.
 *
 * OOP principles demonstrated:
 *   Abstraction   — Character cannot be instantiated directly.
 *   Encapsulation — all fields are private; access is through defined methods.
 *   Polymorphism  — getDescription() is overridden by each subclass.
 *   Inheritance   — Player, Enemy, and NPC all extend this class.
 */
public abstract class Character implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private int    health;
    private int    maxHealth;
    private int    attackPower;
    private int    defense;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    protected Character(String name, int maxHealth, int attackPower, int defense) {
        this.name        = name;
        this.maxHealth   = maxHealth;
        this.health      = maxHealth;   // every character starts at full health
        this.attackPower = attackPower;
        this.defense     = defense;
    }

    // -------------------------------------------------------------------------
    // Abstract method — each subclass provides its own flavour text
    // -------------------------------------------------------------------------

    public abstract String getDescription();

    // -------------------------------------------------------------------------
    // Combat helpers
    // -------------------------------------------------------------------------

    /**
     * Reduces health by {@code amount}, clamped so health never goes below 0.
     *
     * CombatSystem is responsible for calculating net damage (attack − defence).
     * This method simply applies the already-calculated value so Character
     * stays unaware of combat rules.
     *
     * @return the actual damage dealt after clamping
     */
    public int takeDamage(int amount) {
        int actual = Math.max(0, amount);
        health = Math.max(0, health - actual);
        return actual;
    }

    /**
     * Restores health by {@code amount}, clamped to maxHealth.
     *
     * @return how much health was actually restored
     */
    public int heal(int amount) {
        int before = health;
        health = Math.min(maxHealth, health + amount);
        return health - before;
    }

    public boolean isAlive() { return health > 0; }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getName()        { return name; }
    public int    getHealth()      { return health; }
    public int    getMaxHealth()   { return maxHealth; }
    public int    getAttackPower() { return attackPower; }
    public int    getDefense()     { return defense; }

    // -------------------------------------------------------------------------
    // Protected setters — subclasses adjust stats when equipping gear or levelling
    // -------------------------------------------------------------------------

    protected void setName(String name)         { this.name = name; }
    protected void setAttackPower(int attack)   { this.attackPower = Math.max(0, attack); }
    protected void setDefense(int defense)      { this.defense = Math.max(0, defense); }
    protected void setMaxHealth(int maxHealth)  { this.maxHealth = Math.max(1, maxHealth); }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(this.maxHealth, health));
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return name + " (HP: " + health + "/" + maxHealth + ")";
    }
}
