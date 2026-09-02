package arena.engine;

public class FightContractInfo {
    private final String name;
    private final String description;
    private final int goldPercent;
    private final int famePercent;
    private final boolean rivalChallenge;

    public FightContractInfo(String name,
                             String description,
                             int goldPercent,
                             int famePercent,
                             boolean rivalChallenge) {
        this.name = name;
        this.description = description;
        this.goldPercent = goldPercent;
        this.famePercent = famePercent;
        this.rivalChallenge = rivalChallenge;
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

    public boolean isRivalChallenge() {
        return rivalChallenge;
    }
}
