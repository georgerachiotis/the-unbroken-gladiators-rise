package arena.enums;

/**
 * Temporary penalty that can affect the player after dangerous fights.
 */
public enum InjuryType {
    NONE("None"),
    BRUISED_RIBS("Bruised Ribs"),
    WOUNDED_ARM("Wounded Arm"),
    SHAKEN("Shaken");

    private final String displayName;

    InjuryType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
