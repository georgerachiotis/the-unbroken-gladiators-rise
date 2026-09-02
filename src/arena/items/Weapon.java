package arena.items;

/**
 * Equipment item that increases the player's strength while equipped.
 */
public class Weapon {

    private String name;
    private int strengthBonus;

    public Weapon(String name, int strengthBonus) {
        this.name = name;
        this.strengthBonus = strengthBonus;
    }

    public String getName() {
        return name;
    }

    public int getStrengthBonus() {
        return strengthBonus;
    }
}
