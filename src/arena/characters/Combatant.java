package arena.characters;

/**
 * Base type for anything that can fight in the arena. It owns shared combat
 * stats and simple health, stamina, damage, healing, and display behavior.
 */
public class Combatant {

    protected String name;
    protected int hp;
    protected int maxHp;
    protected int stamina;
    protected int maxStamina;
    protected int strength;
    protected int defense;

    public Combatant(String name, int maxHp, int maxStamina, int strength, int defense) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.maxStamina = maxStamina;
        this.stamina = maxStamina;
        this.strength = strength;
        this.defense = defense;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getStamina() {
        return stamina;
    }

    public int getStrength() {
        return strength;
    }

    public int getDefense() {
        return defense;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public boolean hasStamina(int amount) {
        return stamina >= amount;
    }

    public void useStamina(int amount) {
        stamina -= amount;

        if (stamina < 0) {
            stamina = 0;
        }
    }

    public void recoverStamina(int amount) {
        stamina += amount;

        if (stamina > maxStamina) {
            stamina = maxStamina;
        }
    }

    public void takeDamage(int damage) {
        hp -= damage;

        if (hp < 0) {
            hp = 0;
        }
    }

    public void heal(int amount) {
        hp += amount;

        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    public void showStats() {
        System.out.println("\n----- STATS -----");
        System.out.println("Name: " + name);
        System.out.println("HP: " + hp + "/" + maxHp);
        System.out.println("Stamina: " + stamina + "/" + maxStamina);
        System.out.println("Strength: " + strength);
        System.out.println("Defense: " + defense);
    }

    public void restore() {
        hp = maxHp;
        stamina = maxStamina;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getMaxStamina() {
        return maxStamina;
    }

    public int getBaseStrength() {
        return strength;
    }

    public int getBaseDefense() {
        return defense;
    }

    /**
     * Restores raw combat stats from a save file while keeping impossible
     * values, such as negative HP or stamina above max, out of the object.
     */
    protected void restoreCoreStats(int hp,
                                    int maxHp,
                                    int stamina,
                                    int maxStamina,
                                    int strength,
                                    int defense) {
        this.maxHp = Math.max(1, maxHp);
        this.hp = clamp(hp, 0, this.maxHp);
        this.maxStamina = Math.max(0, maxStamina);
        this.stamina = clamp(stamina, 0, this.maxStamina);
        this.strength = Math.max(0, strength);
        this.defense = Math.max(0, defense);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
