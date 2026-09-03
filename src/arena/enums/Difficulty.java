package arena.enums;

/** Difficulty affects enemy combat pressure and career rewards while Standard stays unchanged. */
public enum Difficulty {
    STORY("Story", "Lighter combat and 20% higher rewards", 0.82, 1.20, 1.00, 1.00, 1.00, 0),
    STANDARD("Standard", "The original balanced experience", 1.00, 1.00, 1.00, 1.00, 1.00, 0),
    MERCILESS("Merciless", "Relentless opponents and 20% lower rewards", 1.18, 0.80, 1.08, 1.05, 1.05, 8);

    private final String displayName;
    private final String description;
    private final double incomingDamageMultiplier;
    private final double rewardMultiplier;
    private final double enemyHpMultiplier;
    private final double enemyStrengthMultiplier;
    private final double enemyDefenseMultiplier;
    private final int enemyAbilityChanceBonus;

    Difficulty(String displayName, String description,
               double incomingDamageMultiplier, double rewardMultiplier,
               double enemyHpMultiplier, double enemyStrengthMultiplier,
               double enemyDefenseMultiplier, int enemyAbilityChanceBonus) {
        this.displayName = displayName;
        this.description = description;
        this.incomingDamageMultiplier = incomingDamageMultiplier;
        this.rewardMultiplier = rewardMultiplier;
        this.enemyHpMultiplier = enemyHpMultiplier;
        this.enemyStrengthMultiplier = enemyStrengthMultiplier;
        this.enemyDefenseMultiplier = enemyDefenseMultiplier;
        this.enemyAbilityChanceBonus = enemyAbilityChanceBonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int adjustIncomingDamage(int damage) {
        if (damage <= 0) return 0;
        return Math.max(1, (int) Math.round(damage * incomingDamageMultiplier));
    }

    public int adjustReward(int reward) {
        if (reward <= 0) return reward;
        return Math.max(1, (int) Math.round(reward * rewardMultiplier));
    }

    public int adjustEnemyHp(int value) { return scaleEnemyStat(value, enemyHpMultiplier); }
    public int adjustEnemyStrength(int value) { return scaleEnemyStat(value, enemyStrengthMultiplier); }
    public int adjustEnemyDefense(int value) { return scaleEnemyStat(value, enemyDefenseMultiplier); }
    public int getEnemyAbilityChanceBonus() { return enemyAbilityChanceBonus; }

    private int scaleEnemyStat(int value, double multiplier) {
        return Math.max(1, (int) Math.round(value * multiplier));
    }

    @Override
    public String toString() {
        return displayName;
    }
}
