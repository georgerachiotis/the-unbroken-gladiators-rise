package arena.engine;

public class BattleSummary {
    public enum Outcome {
        VICTORY,
        DEFEAT
    }

    private final Outcome outcome;
    private final String opponentName;
    private final int goldReward;
    private final int fameReward;
    private final String injuryName;
    private final int lossStreak;
    private final int maxLossStreak;
    private final int wins;
    private final int losses;
    private final boolean runEnded;
    private final String nextStep;

    public BattleSummary(Outcome outcome,
                         String opponentName,
                         int goldReward,
                         int fameReward,
                         String injuryName,
                         int lossStreak,
                         int maxLossStreak,
                         int wins,
                         int losses,
                         boolean runEnded,
                         String nextStep) {
        this.outcome = outcome;
        this.opponentName = opponentName;
        this.goldReward = goldReward;
        this.fameReward = fameReward;
        this.injuryName = injuryName;
        this.lossStreak = lossStreak;
        this.maxLossStreak = maxLossStreak;
        this.wins = wins;
        this.losses = losses;
        this.runEnded = runEnded;
        this.nextStep = nextStep;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public int getFameReward() {
        return fameReward;
    }

    public String getInjuryName() {
        return injuryName;
    }

    public int getLossStreak() {
        return lossStreak;
    }

    public int getMaxLossStreak() {
        return maxLossStreak;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public boolean isRunEnded() {
        return runEnded;
    }

    public String getNextStep() {
        return nextStep;
    }
}
