package arena.engine;

public class TrainingOptionInfo {

    private final String name;
    private final String reward;
    private final String risk;

    public TrainingOptionInfo(String name, String reward, String risk) {
        this.name = name;
        this.reward = reward;
        this.risk = risk;
    }

    public String getName() {
        return name;
    }

    public String getReward() {
        return reward;
    }

    public String getRisk() {
        return risk;
    }
}
