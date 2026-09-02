package arena.enums;

/** Difficulty affects enemy damage and career rewards while Standard stays unchanged. */
public enum Difficulty {
    STORY("Story", "Lighter combat and 20% higher rewards", 0.82, 1.20),
    STANDARD("Standard", "The original balanced experience", 1.00, 1.00),
    MERCILESS("Merciless", "Harder enemy hits and 20% lower rewards", 1.18, 0.80);

    private final String displayName;
    private final String description;
    private final double incomingDamageMultiplier;
    private final double rewardMultiplier;

    Difficulty(String displayName, String description,
               double incomingDamageMultiplier, double rewardMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.incomingDamageMultiplier = incomingDamageMultiplier;
        this.rewardMultiplier = rewardMultiplier;
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

    @Override
    public String toString() {
        return displayName;
    }
}
