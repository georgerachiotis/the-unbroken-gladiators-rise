package arena.events;

import arena.characters.Player;

/**
 * Pre-fight condition that slightly changes rewards or player state.
 */
public enum ArenaEvent {
    NONE("Clear Sands", "The arena is steady today.", 100, 100, 0),
    CROWD_FAVOR("Crowd Favorite", "The crowd is already chanting your name.", 100, 115, 6),
    SANDSTORM("Sandstorm", "Dust whips across the arena. The fight will feel uglier.", 110, 110, 0),
    NOBLE_SPONSOR("Noble Sponsor", "A noble watches from the shade, purse in hand.", 125, 100, 0);

    private final String name;
    private final String description;
    private final int goldPercent;
    private final int famePercent;
    private final int crowdFavorBonus;

    ArenaEvent(String name, String description, int goldPercent, int famePercent, int crowdFavorBonus) {
        this.name = name;
        this.description = description;
        this.goldPercent = goldPercent;
        this.famePercent = famePercent;
        this.crowdFavorBonus = crowdFavorBonus;
    }

    public void announce(Player player) {
        System.out.println("\nArena Event: " + name);
        System.out.println(description);

        if (crowdFavorBonus > 0) {
            player.addCrowdFavor(crowdFavorBonus);
        }
    }

    public int applyGold(int gold) {
        return gold * goldPercent / 100;
    }

    public int applyFame(int fame) {
        return fame * famePercent / 100;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
