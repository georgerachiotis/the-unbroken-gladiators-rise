package arena.tests;

import arena.characters.Enemy;
import arena.characters.Player;
import arena.characters.Rival;
import arena.enemies.ArenaRoster;
import arena.enums.GladiatorClass;
import arena.enums.InjuryType;
import arena.enums.Difficulty;
import arena.shop.Shop;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Random;

/**
 * Runs automated careers with a simple strategy so balance changes have a
 * repeatable smoke test before manual playtesting.
 */
public class BalanceSimulator {

    private static final int DEFAULT_CAREERS = 100;
    private static final int MAX_DAYS = 70;

    public static void main(String[] args) {
        int careers = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_CAREERS;
        long seed = args.length > 1 ? Long.parseLong(args[1]) : 1L;
        Difficulty difficulty = args.length > 2
                ? Difficulty.valueOf(args[2].toUpperCase()) : Difficulty.STANDARD;

        Summary summary = runQuietly(careers, seed, difficulty);
        System.out.println(difficulty.getDisplayName() + "\n" + summary.describe());
    }

    public static Summary runQuietly(int careers, long seed) {
        return runQuietly(careers, seed, Difficulty.STANDARD);
    }

    public static Summary runQuietly(int careers, long seed, Difficulty difficulty) {
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            return run(careers, seed, difficulty);
        } finally {
            System.setOut(originalOut);
        }
    }

    public static Summary run(int careers, long seed) {
        return run(careers, seed, Difficulty.STANDARD);
    }

    public static Summary run(int careers, long seed, Difficulty difficulty) {
        Summary summary = new Summary(careers);
        Random random = new Random(seed);

        for (int i = 0; i < careers; i++) {
            CareerResult result = simulateCareer(new Random(random.nextLong()), difficulty);
            summary.record(result);
        }

        return summary;
    }

    private static CareerResult simulateCareer(Random random, Difficulty difficulty) {
        GladiatorClass[] classes = GladiatorClass.values();
        Player player = new Player("Sim", classes[random.nextInt(classes.length)]);
        player.setDifficulty(difficulty);
        ArenaRoster roster = new ArenaRoster(random);
        Shop shop = new Shop();
        int day = 1;

        while (!player.isBrokenByLosses() && day <= MAX_DAYS) {
            if (player.getFame() >= 300) {
                Enemy champion = roster.createChampion();
                champion.applyDifficulty(difficulty);
                boolean victory = autoBattle(player, champion, random, difficulty);
                player.recordFightDay();
                if (victory) {
                    player.addWin();
                    player.gainReward(difficulty.adjustReward(champion.getGoldReward()),
                            difficulty.adjustReward(adjustedFameReward(player, champion)));
                    return new CareerResult(player.getGladiatorClass(), true, false, day, player.getFame(), player.getWins(), player.getLosses());
                }

                player.addLoss();
                if (player.isBrokenByLosses()) {
                    return new CareerResult(player.getGladiatorClass(), false, true, day, player.getFame(), player.getWins(), player.getLosses());
                }

                applySimulatedInjury(player, random);
                player.recoverFromArenaDefeat();
                recoverAfterSimulatedLoss(player);
            }

            else if (player.getHp() * 100 / player.getMaxHp() <= 45 || player.getInjuryType() != InjuryType.NONE) {
                player.fullRest();
                player.recordNonFightDay();
            } else if (shop.createClassWeapon(player).getStrengthBonus() > player.getWeapon().getStrengthBonus()
                    && player.getGold() >= shop.getWeaponItem(player).getPrice()) {
                if (player.spendGold(shop.getWeaponItem(player).getPrice())) {
                    player.equipWeapon(shop.createClassWeapon(player));
                    player.recordNonFightDay();
                }
            } else if (shop.createRankArmor(player).getDefenseBonus() > player.getArmor().getDefenseBonus()
                    && player.getGold() >= shop.getArmorItem(player).getPrice()) {
                if (player.spendGold(shop.getArmorItem(player).getPrice())) {
                    player.equipArmor(shop.createRankArmor(player));
                    player.recordNonFightDay();
                }
            } else if (player.getHealingSalves() < 2 && player.getGold() >= shop.getHealingSalveItem().getPrice()) {
                if (player.spendGold(shop.getHealingSalveItem().getPrice())) {
                    player.addHealingSalve();
                    player.recordNonFightDay();
                }
            } else if (shouldTrain(player, random)) {
                trainForClass(player, random);
                player.recordNonFightDay();
            } else {
                Enemy enemy = roster.createArenaOpponent(day, player, random.nextInt(100) < 15);
                enemy.applyDifficulty(difficulty);
                boolean victory = autoBattle(player, enemy, random, difficulty);
                player.recordFightDay();

                if (victory) {
                    player.addWin();
                    player.gainReward(difficulty.adjustReward(enemy.getGoldReward()),
                            difficulty.adjustReward(adjustedFameReward(player, enemy)));

                    if (enemy instanceof Rival) {
                        roster.recordRivalDefeat((Rival) enemy);
                    }
                } else {
                    player.addLoss();
                    if (!player.isBrokenByLosses()) {
                        applySimulatedInjury(player, random);
                        player.recoverFromArenaDefeat();
                        recoverAfterSimulatedLoss(player);
                    }
                }
            }

            day++;
        }

        return new CareerResult(player.getGladiatorClass(), false, player.isBrokenByLosses(), Math.min(day, MAX_DAYS), player.getFame(),
                player.getWins(), player.getLosses());
    }

    private static int adjustedFameReward(Player player, Enemy enemy) {
        int fame = enemy.getFameReward();

        if (player.getFame() < 50) {
            fame += 3;
        }

        if (enemy instanceof Rival) {
            Rival rival = (Rival) enemy;
            fame += rival.getDefeatsAgainstPlayer() == 0 ? 8 : 3;
        }

        return fame;
    }

    private static void recoverAfterSimulatedLoss(Player player) {
        if (player.getGold() >= 20) {
            player.spendGold(20);
            player.recoverWithPhysician();
        } else {
            player.recoverInInfirmary();
        }
    }

    private static boolean shouldTrain(Player player, Random random) {
        if (player.getDaysSinceFight() >= 3) {
            return false;
        }

        return player.getFatigue() < 55 && random.nextInt(100) < 40;
    }

    private static void trainForClass(Player player, Random random) {
        switch (player.getGladiatorClass()) {
            case MURMILLO:
                if (random.nextBoolean()) {
                    player.trainDefense();
                } else {
                    player.trainStrength();
                }
                break;
            case RETIARIUS:
                if (random.nextBoolean()) {
                    player.trainStamina();
                } else {
                    player.trainStrength();
                }
                break;
            case DIMACHAERUS:
                player.trainStrength();
                break;
            case THRAEX:
            default:
                if (random.nextBoolean()) {
                    player.trainStrength();
                } else {
                    player.trainDefense();
                }
                break;
        }
    }

    private static boolean autoBattle(Player player, Enemy enemy, Random random, Difficulty difficulty) {
        enemy.restore();
        int rounds = 0;

        while (player.isAlive() && enemy.isAlive() && rounds < 60) {
            rounds++;
            boolean enemyTrapped = playerTurn(player, enemy, random);

            if (enemy.isAlive() && !enemyTrapped) {
                enemyTurn(enemy, player, random, difficulty);
            }
        }

        return player.isAlive() && !enemy.isAlive();
    }

    private static boolean playerTurn(Player player, Enemy enemy, Random random) {
        if (player.getHp() * 100 / player.getMaxHp() <= 30 && player.getHealingSalves() > 0) {
            player.useHealingSalve();
            return false;
        }

        if (player.getStamina() * 4 <= player.getMaxStamina()) {
            player.recoverStamina(player.getGladiatorClass() == GladiatorClass.RETIARIUS ? 20 : 15);
            player.heal(5);
            return false;
        }

        int enemyDefense = enemy.getStamina() * 4 <= enemy.getMaxStamina()
                ? enemy.getDefense() / 2 : enemy.getDefense();

        int specialCost = specialCost(player.getGladiatorClass());
        if (player.hasStamina(specialCost) && random.nextInt(100) < 30) {
            player.useStamina(specialCost);
            switch (player.getGladiatorClass()) {
                case MURMILLO:
                    enemy.takeDamage(Math.max(1, player.getStrength() - enemyDefense / 2));
                    return false;
                case RETIARIUS:
                    if (random.nextInt(100) < 15) return false;
                    enemy.takeDamage(Math.max(1, player.getStrength() - enemyDefense / 2));
                    enemy.useStamina(12);
                    player.recoverStamina(5);
                    return true;
                case DIMACHAERUS:
                    enemy.takeDamage(Math.max(1, player.getStrength() - enemyDefense));
                    enemy.takeDamage(Math.max(1, player.getStrength() - enemyDefense));
                    return false;
                case THRAEX:
                    enemy.takeDamage(Math.max(1, player.getStrength() + 5 - enemyDefense)
                            + (enemy.getDefense() >= 5 ? 3 : 0));
                    return false;
                default:
                    break;
            }
        }

        if (player.getStamina() >= 15 && random.nextInt(100) >= 25) {
            player.useStamina(15);
            int damage = Math.max(1, player.getStrength() * 2 - enemyDefense);
            if (player.getGladiatorClass() == GladiatorClass.THRAEX && enemy.getDefense() >= 5) damage += 3;
            enemy.takeDamage(damage);
            return false;
        }

        player.useStamina(5);
        if (random.nextInt(100) >= 10) {
            int damage = Math.max(1, player.getStrength() - enemyDefense);
            if (player.getGladiatorClass() == GladiatorClass.THRAEX && enemy.getDefense() >= 5) damage += 3;
            enemy.takeDamage(damage);
            if (player.getGladiatorClass() == GladiatorClass.DIMACHAERUS && random.nextInt(100) < 15) {
                enemy.takeDamage(Math.max(1, player.getStrength() / 2 - enemyDefense / 2));
            }
        }
        return false;
    }

    private static int specialCost(GladiatorClass gladiatorClass) {
        switch (gladiatorClass) {
            case MURMILLO: return 12;
            case RETIARIUS: return 15;
            case DIMACHAERUS: return 20;
            case THRAEX: return 14;
            default: return 99;
        }
    }

    private static void enemyTurn(Enemy enemy, Player player, Random random, Difficulty difficulty) {
        boolean staggered = enemy.getStamina() * 4 <= enemy.getMaxStamina();
        if (staggered && random.nextInt(100) < 65) {
            enemy.recoverStamina(18);
            return;
        }

        int playerDefense = player.getStamina() * 4 <= player.getMaxStamina()
                ? player.getDefense() / 2 : player.getDefense();
        int specialChance = (enemy.getHp() * 4 <= enemy.getMaxHp() ? 40 : 25)
                + difficulty.getEnemyAbilityChanceBonus();
        int damage;

        if (!staggered && enemy.hasStamina(12) && enemy.getAbility() != arena.enums.EnemyAbility.NONE
                && random.nextInt(100) < specialChance) {
            enemy.useStamina(12);
            switch (enemy.getAbility()) {
                case CRUSHING_BLOW:
                    damage = random.nextInt(100) < 35 ? 0 : Math.max(1, enemy.getStrength() * 3 - playerDefense);
                    break;
                case BUTCHERS_CLEAVE: case WILD_SWING:
                    damage = random.nextInt(100) < 30 ? 0 : Math.max(1, enemy.getStrength() * 2 - playerDefense);
                    break;
                case DOUBLE_STRIKE: case DUST_FLURRY:
                    damage = Math.max(1, enemy.getStrength() - playerDefense) * 2;
                    break;
                case IRON_WALL:
                    damage = Math.max(1, enemy.getStrength() + enemy.getDefense() - playerDefense);
                    enemy.recoverStamina(10);
                    break;
                case POISON_STRIKE: case HOOK_SLASH:
                    damage = Math.max(1, enemy.getStrength() + 4 - playerDefense / 2) + 3;
                    break;
                case SHIELD_BASH: case NET_CAST:
                    damage = Math.max(1, enemy.getStrength() - playerDefense / 2);
                    player.useStamina(enemy.getAbility() == arena.enums.EnemyAbility.NET_CAST ? 12 : 6);
                    break;
                default:
                    damage = Math.max(1, enemy.getStrength() - playerDefense);
            }
        } else {
            enemy.useStamina(5);
            damage = random.nextInt(100) < 20
                    ? Math.max(1, enemy.getStrength() * 2 - playerDefense)
                    : Math.max(1, enemy.getStrength() - playerDefense + random.nextInt(5));
        }

        player.takeDamage(difficulty.adjustIncomingDamage(damage));
    }

    private static void applySimulatedInjury(Player player, Random random) {
        int roll = random.nextInt(3);

        if (roll == 0) {
            player.applyInjury(InjuryType.BRUISED_RIBS);
        } else if (roll == 1) {
            player.applyInjury(InjuryType.WOUNDED_ARM);
        } else {
            player.applyInjury(InjuryType.SHAKEN);
        }
    }

    private static class CareerResult {
        private final GladiatorClass gladiatorClass;
        private final boolean championDefeated;
        private final boolean died;
        private final int days;
        private final int fame;
        private final int wins;
        private final int losses;

        private CareerResult(GladiatorClass gladiatorClass, boolean championDefeated, boolean died,
                             int days, int fame, int wins, int losses) {
            this.gladiatorClass = gladiatorClass;
            this.championDefeated = championDefeated;
            this.died = died;
            this.days = days;
            this.fame = fame;
            this.wins = wins;
            this.losses = losses;
        }
    }

    public static class Summary {
        private final int careers;
        private int championWins;
        private int deaths;
        private int championWinDays;
        private int totalDays;
        private int totalFame;
        private int totalWins;
        private int totalLosses;
        private final int[] classCareers = new int[GladiatorClass.values().length];
        private final int[] classChampionWins = new int[GladiatorClass.values().length];

        private Summary(int careers) {
            this.careers = careers;
        }

        private void record(CareerResult result) {
            int classIndex = result.gladiatorClass.ordinal();
            classCareers[classIndex]++;
            if (result.championDefeated) {
                championWins++;
                championWinDays += result.days;
                classChampionWins[classIndex]++;
            }

            if (result.died) {
                deaths++;
            }

            totalDays += result.days;
            totalFame += result.fame;
            totalWins += result.wins;
            totalLosses += result.losses;
        }

        public int getChampionWins() {
            return championWins;
        }

        public int getDeaths() {
            return deaths;
        }

        public double getChampionWinRate() {
            return careers == 0 ? 0.0 : championWins * 100.0 / careers;
        }

        public double getDeathRate() {
            return careers == 0 ? 0.0 : deaths * 100.0 / careers;
        }

        public double getAverageDays() {
            return careers == 0 ? 0.0 : totalDays * 1.0 / careers;
        }

        public double getAverageChampionWinDays() {
            return championWins == 0 ? 0.0 : championWinDays * 1.0 / championWins;
        }

        public double getAverageFame() {
            return careers == 0 ? 0.0 : totalFame * 1.0 / careers;
        }

        public String describe() {
            String text = "Balance simulation careers: " + careers
                    + "\nChampion wins: " + championWins + " (" + format(getChampionWinRate()) + "%)"
                    + "\nDeaths: " + deaths + " (" + format(getDeathRate()) + "%)"
                    + "\nAverage days: " + format(getAverageDays())
                    + "\nAverage days to champion win: " + format(getAverageChampionWinDays())
                    + "\nAverage fame: " + format(getAverageFame())
                    + "\nAverage record: " + format(totalWins * 1.0 / careers)
                    + "-" + format(totalLosses * 1.0 / careers);
            for (GladiatorClass gladiatorClass : GladiatorClass.values()) {
                int index = gladiatorClass.ordinal();
                double rate = classCareers[index] == 0 ? 0.0
                        : classChampionWins[index] * 100.0 / classCareers[index];
                text += "\n" + gladiatorClass + " champion win rate: " + format(rate) + "%";
            }
            return text;
        }

        private String format(double value) {
            return String.format("%.1f", value);
        }
    }
}
