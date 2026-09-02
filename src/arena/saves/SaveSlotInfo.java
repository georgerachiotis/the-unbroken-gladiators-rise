package arena.saves;

public class SaveSlotInfo {
    public enum Status {
        FILLED,
        EMPTY,
        DAMAGED
    }

    private final int slot;
    private final Status status;
    private final String playerName;
    private final String dayText;
    private final String rank;

    public SaveSlotInfo(int slot, Status status, String playerName, String dayText, String rank) {
        this.slot = slot;
        this.status = status;
        this.playerName = playerName;
        this.dayText = dayText;
        this.rank = rank;
    }

    public int getSlot() {
        return slot;
    }

    public Status getStatus() {
        return status;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDayText() {
        return dayText;
    }

    public String getRank() {
        return rank;
    }

    public boolean isLoadable() {
        return status == Status.FILLED;
    }
}
