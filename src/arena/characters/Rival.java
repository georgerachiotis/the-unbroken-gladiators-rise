package arena.characters;

import arena.enums.EnemyAbility;

/**
 * Represents a named recurring enemy who remembers defeats and can use a
 * rematch quote after the player has beaten them before.
 */
public class Rival extends Enemy {

    private int defeatsAgainstPlayer;
    private int encountersAgainstPlayer;
    private int victoriesAgainstPlayer;
    private String firstEncounterQuote;
    private String rematchQuote;

    public Rival(String name,
                 int maxHp,
                 int maxStamina,
                 int strength,
                 int defense,
                 int goldReward,
                 int fameReward,
                 EnemyAbility ability,
                 String rematchQuote) {

        this(name, maxHp, maxStamina, strength, defense, goldReward, fameReward,
                ability, "The arena will remember this meeting.", rematchQuote);
    }

    public Rival(String name,
                 int maxHp,
                 int maxStamina,
                 int strength,
                 int defense,
                 int goldReward,
                 int fameReward,
                 EnemyAbility ability,
                 String firstEncounterQuote,
                 String rematchQuote) {

        super(name, maxHp, maxStamina, strength, defense, goldReward, fameReward, ability);

        this.defeatsAgainstPlayer = 0;
        this.encountersAgainstPlayer = 0;
        this.victoriesAgainstPlayer = 0;
        this.firstEncounterQuote = firstEncounterQuote;
        this.rematchQuote = rematchQuote;
    }

    public int getDefeatsAgainstPlayer() {
        return defeatsAgainstPlayer;
    }

    public void addDefeatAgainstPlayer() {
        defeatsAgainstPlayer++;
    }

    public void setDefeatsAgainstPlayer(int defeatsAgainstPlayer) {
        this.defeatsAgainstPlayer = Math.max(0, defeatsAgainstPlayer);
    }

    public String getRematchQuote() {
        return rematchQuote;
    }

    public int getEncountersAgainstPlayer() {
        return encountersAgainstPlayer;
    }

    public int getVictoriesAgainstPlayer() {
        return victoriesAgainstPlayer;
    }

    public void addEncounterAgainstPlayer() {
        encountersAgainstPlayer++;
    }

    public void addVictoryAgainstPlayer() {
        victoriesAgainstPlayer++;
    }

    public void restoreHistory(int encounters, int playerWins, int rivalWins) {
        encountersAgainstPlayer = Math.max(0, encounters);
        defeatsAgainstPlayer = Math.max(0, playerWins);
        victoriesAgainstPlayer = Math.max(0, rivalWins);
    }

    public String getEncounterQuote() {
        return encountersAgainstPlayer <= 1 ? firstEncounterQuote : rematchQuote;
    }

    public void showIntro() {
        System.out.println("\n" + getName() + " enters the arena.");

        System.out.println("\"" + getEncounterQuote() + "\"");
    }
}
