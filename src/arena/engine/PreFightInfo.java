package arena.engine;

/** Read-only information shown before the player commits to an arena fight. */
public class PreFightInfo {
    private final String text;

    public PreFightInfo(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
