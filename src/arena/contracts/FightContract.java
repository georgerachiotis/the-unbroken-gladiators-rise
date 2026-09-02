package arena.contracts;

/**
 * A fight offer with a risk profile and reward multiplier.
 */
public class FightContract {

    private final String name;
    private final String description;
    private final int goldPercent;
    private final int famePercent;
    private final boolean rivalChallenge;

    public FightContract(String name, String description, int goldPercent, int famePercent, boolean rivalChallenge) {
        this.name = name;
        this.description = description;
        this.goldPercent = goldPercent;
        this.famePercent = famePercent;
        this.rivalChallenge = rivalChallenge;
    }

    public String getMenuText() {
        return name + " - " + description + " (" + goldPercent + "% gold, " + famePercent + "% fame)";
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getGoldPercent() {
        return goldPercent;
    }

    public int getFamePercent() {
        return famePercent;
    }

    public int applyGold(int gold) {
        return gold * goldPercent / 100;
    }

    public int applyFame(int fame) {
        return fame * famePercent / 100;
    }

    public boolean isRivalChallenge() {
        return rivalChallenge;
    }
}
