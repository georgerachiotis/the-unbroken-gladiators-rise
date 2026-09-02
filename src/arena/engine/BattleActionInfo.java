package arena.engine;

public class BattleActionInfo {

    private final String name;
    private final String detail;
    private final boolean enabled;
    private final String disabledReason;

    public BattleActionInfo(String name, String detail, boolean enabled, String disabledReason) {
        this.name = name;
        this.detail = detail;
        this.enabled = enabled;
        this.disabledReason = disabledReason;
    }

    public String getName() {
        return name;
    }

    public String getDetail() {
        return detail;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDisabledReason() {
        return disabledReason;
    }
}
