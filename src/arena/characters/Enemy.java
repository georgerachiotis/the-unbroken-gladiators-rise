package arena.characters;

import arena.enums.EnemyAbility;

/**
 * Represents an opponent in the arena and the rewards granted when defeated.
 */
public class Enemy extends Combatant {

    private int goldReward;
    private int fameReward;
    private EnemyAbility ability;

    public Enemy(String name, int maxHp, int maxStamina, int strength, int defense,
                 int goldReward, int fameReward) {
        this(name, maxHp, maxStamina, strength, defense, goldReward, fameReward, EnemyAbility.NONE);
    }

    public Enemy(String name,
                 int maxHp,
                 int maxStamina,
                 int strength,
                 int defense,
                 int goldReward,
                 int fameReward,
                 EnemyAbility ability) {
        super(name, maxHp, maxStamina, strength, defense);

        this.goldReward = goldReward;
        this.fameReward = fameReward;
        this.ability = ability;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public int getFameReward() {
        return fameReward;
    }

    public EnemyAbility getAbility() {
        return ability;
    }

    public String getBattleStatus() {
        return getName() + " HP " + getHp() + "/" + getMaxHp()
                + " | STA " + getStamina() + "/" + getMaxStamina()
                + " | STR " + getStrength()
                + " | DEF " + getDefense();
    }
}
