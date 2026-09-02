package arena.enemies;

import arena.characters.Enemy;
import arena.characters.Player;
import arena.characters.Rival;
import arena.enums.EnemyAbility;

import java.util.Random;

/**
 * Creates rivals, common enemies, scaled arena opponents, and the champion.
 */
public class ArenaRoster {

    private final Random random;

    private Rival titus;
    private Rival cassius;
    private Rival redWolf;
    private Rival viper;

    public ArenaRoster(Random random) {
        this.random = random;
        resetRivals();
    }

    public void resetRivals() {
        titus = new Rival("Titus the Butcher", 75, 40, 10, 3, 28, 10,
                EnemyAbility.BUTCHERS_CLEAVE,
                "The sand remembers every coward. Show me what it remembers of you.",
                "You again? This time I will break you.");

        cassius = new Rival("Cassius the Giant", 110, 35, 9, 6, 38, 14,
                EnemyAbility.CRUSHING_BLOW,
                "Stand tall. I want the crowd to see you fall.",
                "I remember you, little fighter.");

        redWolf = new Rival("The Red Wolf", 68, 50, 13, 2, 42, 16,
                EnemyAbility.DOUBLE_STRIKE,
                "Run if you wish. The wolf enjoys the chase.",
                "The wolf does not lose twice.");

        viper = new Rival("Viper of the Sands", 62, 60, 11, 3, 34, 12,
                EnemyAbility.POISON_STRIKE,
                "Do not fear the first cut. Fear what follows.",
                "One bite is all I need.");
    }

    public Enemy createArenaOpponent(int day, Player player) {
        return createArenaOpponent(day, player, false);
    }

    public Enemy createArenaOpponent(int day, Player player, boolean forceRival) {
        if (forceRival) {
            return createScaledRival(getRandomRival(), day);
        }

        return createCommonEnemy(day, player.getFame());
    }

    public Enemy createChampion() {
        return new Enemy("Aurelius the Unbroken",
                220, 110, 25, 12,
                100, 50, EnemyAbility.IRON_WALL);
    }

    public Rival getTitus() {
        return titus;
    }

    public Rival getCassius() {
        return cassius;
    }

    public Rival getRedWolf() {
        return redWolf;
    }

    public Rival getViper() {
        return viper;
    }

    public void restoreRivalDefeats(int titusDefeats,
                                    int cassiusDefeats,
                                    int redWolfDefeats,
                                    int viperDefeats) {
        titus.setDefeatsAgainstPlayer(titusDefeats);
        cassius.setDefeatsAgainstPlayer(cassiusDefeats);
        redWolf.setDefeatsAgainstPlayer(redWolfDefeats);
        viper.setDefeatsAgainstPlayer(viperDefeats);
    }

    public void restoreRivalHistory(int[] encounters, int[] playerWins, int[] rivalWins) {
        Rival[] rivals = {titus, cassius, redWolf, viper};
        for (int i = 0; i < rivals.length; i++) {
            rivals[i].restoreHistory(encounters[i], playerWins[i], rivalWins[i]);
        }
    }

    public void recordRivalEncounter(Rival rival) {
        Rival remembered = findRivalByName(rival.getName());
        if (remembered != null) {
            remembered.addEncounterAgainstPlayer();
            rival.addEncounterAgainstPlayer();
        }
    }

    public void recordRivalVictory(Rival rival) {
        Rival remembered = findRivalByName(rival.getName());
        if (remembered != null) {
            remembered.addVictoryAgainstPlayer();
            rival.addVictoryAgainstPlayer();
        }
    }

    public void recordRivalDefeat(Rival defeatedRival) {
        Rival rememberedRival = findRivalByName(defeatedRival.getName());

        if (rememberedRival != null) {
            rememberedRival.addDefeatAgainstPlayer();
            if (rememberedRival != defeatedRival) {
                defeatedRival.addDefeatAgainstPlayer();
            }
        }
    }

    private Rival getRandomRival() {
        int enemyRoll = random.nextInt(4);

        switch (enemyRoll) {
            case 0:
                return titus;
            case 1:
                return cassius;
            case 2:
                return redWolf;
            case 3:
                return viper;
            default:
                return titus;
        }
    }

    private Rival createScaledRival(Rival rival, int day) {
        int rematchBonus = rival.getDefeatsAgainstPlayer();
        int bonusHp = day * 2;
        int bonusStrength = day / 3;
        int bonusDefense = day / 5;

        int bonusGold = day * 2;
        int bonusFame = Math.max(1, day / 3);

        Rival scaledRival = new Rival(
                rival.getName(),
                rival.getMaxHp() + bonusHp + rematchBonus * 8,
                rival.getMaxStamina(),
                rival.getStrength() + bonusStrength + rematchBonus,
                rival.getDefense() + bonusDefense + rematchBonus / 2,
                rival.getGoldReward() + bonusGold,
                rival.getFameReward() + bonusFame,
                rival.getAbility(),
                rival.getEncounterQuote(),
                rival.getRematchQuote()
        );
        scaledRival.restoreHistory(rival.getEncountersAgainstPlayer(),
                rival.getDefeatsAgainstPlayer(), rival.getVictoriesAgainstPlayer());
        return scaledRival;
    }

    private Rival findRivalByName(String name) {
        if (titus.getName().equals(name)) {
            return titus;
        } else if (cassius.getName().equals(name)) {
            return cassius;
        } else if (redWolf.getName().equals(name)) {
            return redWolf;
        } else if (viper.getName().equals(name)) {
            return viper;
        }

        return null;
    }

    private Enemy createCommonEnemy(int day, int fame) {
        String[] pool;
        if (fame < 50) {
            pool = new String[]{"Arena Recruit", "Nervous Shieldbearer", "Dust Runner", "Arena Brawler"};
        } else if (fame < 150) {
            pool = new String[]{"Desert Spearman", "Shield-Bitten Veteran", "Dust Runner",
                    "Arena Brawler", "Hookblade Duelist", "Iron Netter"};
        } else {
            pool = new String[]{"Shield-Bitten Veteran", "Arena Brawler", "Hookblade Duelist",
                    "Iron Netter", "The Hollow Helm"};
        }

        String opponent = pool[random.nextInt(pool.length)];
        int bonusHp = day;
        int bonusStrength = day / 4;
        int bonusDefense = day / 6;
        int bonusGold = day;
        int bonusFame = Math.max(1, day / 4);

        switch (opponent) {
            case "Arena Recruit":
                return new Enemy("Arena Recruit",
                        55 + bonusHp,
                        35,
                        7 + bonusStrength,
                        2 + bonusDefense,
                        12 + bonusGold,
                        5 + bonusFame,
                        EnemyAbility.DOUBLE_STRIKE);
            case "Nervous Shieldbearer":
                return new Enemy("Nervous Shieldbearer",
                        60 + bonusHp, 42, 7 + bonusStrength, 4 + bonusDefense,
                        14 + bonusGold, 5 + bonusFame, EnemyAbility.SHIELD_BASH);
            case "Dust Runner":
                return new Enemy("Dust Runner",
                        50 + bonusHp, 58, 8 + bonusStrength, 1 + bonusDefense,
                        15 + bonusGold, 6 + bonusFame, EnemyAbility.DUST_FLURRY);
            case "Arena Brawler":
                return new Enemy("Arena Brawler",
                        68 + bonusHp, 34, 10 + bonusStrength, 1 + bonusDefense,
                        17 + bonusGold, 6 + bonusFame, EnemyAbility.WILD_SWING);
            case "Desert Spearman":
                return new Enemy("Desert Spearman",
                        62 + bonusHp,
                        45,
                        8 + bonusStrength,
                        3 + bonusDefense,
                        15 + bonusGold,
                        6 + bonusFame,
                        EnemyAbility.POISON_STRIKE);
            case "Hookblade Duelist":
                return new Enemy("Hookblade Duelist",
                        68 + bonusHp, 50, 10 + bonusStrength, 3 + bonusDefense,
                        22 + bonusGold, 8 + bonusFame, EnemyAbility.HOOK_SLASH);
            case "Iron Netter":
                return new Enemy("Iron Netter",
                        72 + bonusHp, 55, 9 + bonusStrength, 4 + bonusDefense,
                        24 + bonusGold, 9 + bonusFame, EnemyAbility.NET_CAST);
            case "The Hollow Helm":
                return new Enemy("The Hollow Helm",
                        98 + bonusHp, 42, 12 + bonusStrength, 8 + bonusDefense,
                        32 + bonusGold, 12 + bonusFame, EnemyAbility.IRON_WALL);
            case "Shield-Bitten Veteran":
            default:
                return new Enemy("Shield-Bitten Veteran",
                        72 + bonusHp,
                        38,
                        8 + bonusStrength,
                        4 + bonusDefense,
                        18 + bonusGold,
                        7 + bonusFame,
                        EnemyAbility.CRUSHING_BLOW);
        }
    }
}
