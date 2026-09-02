package arena.items;

/**
 * Equipment item that increases the player's defense while equipped.
 */
public class Armor {

    private String name;
    private int defenseBonus;

    public Armor(String name, int defenseBonus) {
        this.name = name;
        this.defenseBonus = defenseBonus;
    }

    public String getName() {
        return name;
    }

    public int getDefenseBonus() {
        return defenseBonus;
    }
}
