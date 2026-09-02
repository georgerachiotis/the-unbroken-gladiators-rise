package arena.engine;

import arena.characters.Enemy;
import arena.characters.Player;
import arena.characters.Rival;
import arena.contracts.FightContract;
import arena.enums.GladiatorClass;
import arena.enums.EnemyAbility;
import arena.enums.InjuryType;
import arena.enums.Difficulty;
import arena.enemies.ArenaRoster;
import arena.events.ArenaEvent;
import arena.items.Armor;
import arena.items.Weapon;
import arena.saves.SaveManager;
import arena.saves.SaveSlotInfo;
import arena.shop.Shop;
import arena.shop.ShopItem;

import java.util.Random;

/**
 * GUI-friendly game state wrapper. It exposes game actions as methods instead
 * of console menus, so JavaFX can call them from buttons.
 */
public class GameSession {

    public static final int FREEDOM_PRICE = 1500;

    private final Random random = new Random();
    private final ArenaRoster arenaRoster = new ArenaRoster(random);
    private final SaveManager saveManager = new SaveManager();
    private final Shop shop = new Shop();

    private Player player;
    private Enemy currentEnemy;
    private int day = 1;
    private int battleRound = 0;
    private boolean playerDefending = false;
    private boolean enemyTrapped = false;
    private boolean playerPoisoned = false;
    private boolean counterattackReady = false;
    private FightContract currentContract;
    private ArenaEvent currentArenaEvent = ArenaEvent.NONE;
    private FightContract pendingContract;
    private Enemy pendingEnemy;
    private ArenaEvent pendingArenaEvent = ArenaEvent.NONE;
    private boolean gameOver = false;
    private boolean championDefeated = false;
    private int titleDefenses = 0;
    private boolean freedomPurchased = false;
    private boolean freedomEndingAcknowledged = false;
    private boolean tutorialActive = false;
    private boolean mainTipSeen;
    private boolean arenaTipSeen;
    private boolean trainingTipSeen;
    private boolean battleTipSeen;
    private boolean staggerTipSeen;
    private boolean counterTipSeen;
    private BattleSummary lastBattleSummary;
    private String lastEnemySummary;

    public void startNewGame(String name, GladiatorClass gladiatorClass) {
        startNewGame(name, gladiatorClass, Difficulty.STANDARD);
    }

    public void startNewGame(String name, GladiatorClass gladiatorClass, Difficulty difficulty) {
        String safeName = name == null || name.trim().isEmpty() ? "The Unbroken" : name.trim();

        day = 1;
        gameOver = false;
        championDefeated = false;
        titleDefenses = 0;
        freedomPurchased = false;
        freedomEndingAcknowledged = false;
        tutorialActive = true;
        mainTipSeen = false;
        arenaTipSeen = false;
        trainingTipSeen = false;
        battleTipSeen = false;
        staggerTipSeen = false;
        counterTipSeen = false;
        currentEnemy = null;
        currentContract = null;
        currentArenaEvent = ArenaEvent.NONE;
        clearFightPreview();
        lastBattleSummary = null;
        lastEnemySummary = null;
        arenaRoster.resetRivals();
        player = new Player(safeName, gladiatorClass);
        player.setDifficulty(difficulty);
    }

    public Difficulty getDifficulty() {
        return hasPlayer() ? player.getDifficulty() : Difficulty.STANDARD;
    }

    public boolean hasPlayer() {
        return player != null;
    }

    public Player getPlayer() {
        return player;
    }

    public int getDay() {
        return day;
    }

    public int getFameToChampion() {
        return hasPlayer() ? Math.max(0, 300 - player.getFame()) : 300;
    }

    public double getChampionProgress() {
        return hasPlayer() ? Math.min(1.0, player.getFame() / 300.0) : 0.0;
    }

    public boolean isChampionMatchUnlocked() {
        return hasPlayer() && player.getFame() >= 300 && !championDefeated;
    }

    public boolean isChampionDefeated() {
        return championDefeated;
    }

    public int getTitleDefenses() {
        return titleDefenses;
    }

    public boolean isFreedomPurchased() {
        return freedomPurchased;
    }

    public boolean shouldShowFreedomEnding() {
        return freedomPurchased && !freedomEndingAcknowledged;
    }

    public void acknowledgeFreedomEnding() {
        freedomEndingAcknowledged = true;
    }

    public double getFreedomProgress() {
        return hasPlayer() ? Math.min(1.0, player.getGold() * 1.0 / FREEDOM_PRICE) : 0.0;
    }

    public boolean canBuyFreedom() {
        return hasPlayer() && championDefeated && !freedomPurchased && player.getGold() >= FREEDOM_PRICE;
    }

    public String buyFreedom() {
        if (!championDefeated) return "Only the Arena Champion may negotiate freedom.";
        if (freedomPurchased) return "Your freedom has already been purchased.";
        if (!player.spendGold(FREEDOM_PRICE)) {
            return "You need " + (FREEDOM_PRICE - player.getGold()) + " more gold to buy your freedom.";
        }
        freedomPurchased = true;
        freedomEndingAcknowledged = false;
        return player.getName() + " pays the price of freedom. The chains are broken.";
    }

    public String getFreedomEndingText() {
        if (!freedomPurchased || !hasPlayer()) return "";
        return "FREEDOM\n\n" + player.getName() + " leaves the arena by choice, not by defeat.\n"
                + "Day " + day + " | Record " + player.getWins() + "-" + player.getLosses() + "\n"
                + "Title defenses: " + titleDefenses + "\n\n"
                + "The crowd may remember the Champion. The free gladiator chooses what comes next.";
    }

    public boolean hasChampionEnding() {
        return championDefeated && lastBattleSummary != null
                && "Aurelius the Unbroken".equals(lastBattleSummary.getOpponentName());
    }

    public String getChampionEndingText() {
        if (!hasPlayer()) return "";
        return "THE UNBROKEN\n\n"
                + player.getName() + " has claimed the arena crown.\n"
                + "Class: " + player.getGladiatorClass() + " | Day " + day + "\n"
                + "Final record: " + player.getWins() + "-" + player.getLosses() + "\n"
                + "Fame: " + player.getFame() + "\n\n"
                + "Rivalries\n"
                + rivalCareerLine(arenaRoster.getTitus()) + "\n"
                + rivalCareerLine(arenaRoster.getCassius()) + "\n"
                + rivalCareerLine(arenaRoster.getRedWolf()) + "\n"
                + rivalCareerLine(arenaRoster.getViper())
                + "\n\nThe chains are gone. The name remains.";
    }

    public void continueAsChampion() {
        lastBattleSummary = null;
    }

    public void returnToNewCareer() {
        player = null;
        currentEnemy = null;
        currentContract = null;
        currentArenaEvent = ArenaEvent.NONE;
        clearFightPreview();
        lastBattleSummary = null;
        lastEnemySummary = null;
        gameOver = false;
        championDefeated = false;
        titleDefenses = 0;
        freedomPurchased = false;
        freedomEndingAcknowledged = false;
        tutorialActive = false;
    }

    public String consumeMainTutorialTip() {
        if (!tutorialActive || mainTipSeen) return "";
        mainTipSeen = true;
        return "GOAL: Reach 300 Fame and defeat the Champion. Three consecutive defeats end the career.";
    }

    public String consumeArenaTutorialTip() {
        if (!tutorialActive || arenaTipSeen) return "";
        arenaTipSeen = true;
        return "Measured Bout offers common enemies. Blood Price pays more but is riskier. Rival Challenge faces a named rival. Forfeit costs Fame but does not increase the loss streak.";
    }

    public String consumeTrainingTutorialTip() {
        if (!tutorialActive || trainingTipSeen) return "";
        trainingTipSeen = true;
        return "Training raises your abilities but also Fatigue. High Fatigue risks injury; Rest restores HP, stamina and injuries while reducing Fatigue.";
    }

    public String consumeBattleTutorialTip() {
        if (!tutorialActive) return "";
        if (isPlayerStaggered() && !staggerTipSeen) {
            staggerTipSeen = true;
            return "STAGGERED: At 25% stamina or less, Defense is halved and Special is blocked. Catch Breath or use a Stamina Draught.";
        }
        if (counterattackReady && !counterTipSeen) {
            counterTipSeen = true;
            return "COUNTER READY: Your next successful normal Attack gains bonus damage. A miss does not waste it.";
        }
        if (battleTipSeen) return "";
        battleTipSeen = true;
        return "Stamina powers attacks. Defend reduces the next hit; blocking it prepares a Counterattack. Watch both stamina bars.";
    }

    public String getNextRankName() {
        if (!hasPlayer()) return "Pit Fighter";
        if (player.getFame() < 50) return "Arena Rookie";
        if (player.getFame() < 150) return "Arena Veteran";
        if (player.getFame() < 300) return "Champion Match";
        return "Champion Match Unlocked";
    }

    public int getFameToNextMilestone() {
        if (!hasPlayer()) return 50;
        if (player.getFame() < 50) return 50 - player.getFame();
        if (player.getFame() < 150) return 150 - player.getFame();
        if (player.getFame() < 300) return 300 - player.getFame();
        return 0;
    }

    public String getDayGuidanceText() {
        if (!hasPlayer()) return "Create or load a gladiator to begin.";
        int daysUntilOrder = Math.max(0, 4 - player.getDaysSinceFight());
        String pressure = daysUntilOrder == 0
                ? "The lanista will force the next day action into an arena fight."
                : "Lanista pressure: " + daysUntilOrder + " non-fight day"
                + (daysUntilOrder == 1 ? "" : "s") + " remaining.";
        return "Day " + day + ": choose an arena fight or one day action. " + pressure;
    }

    public String getCareerProgressText() {
        if (!hasPlayer()) return "Start or load a game.";

        if (championDefeated) {
            String freedom = freedomPurchased ? "Freedom purchased."
                    : player.getGold() + "/" + FREEDOM_PRICE + " Gold toward freedom ("
                    + Math.max(0, FREEDOM_PRICE - player.getGold()) + " remaining).";
            return "Arena Champion | " + player.getFame() + " Fame\n"
                    + "Title defenses: " + titleDefenses + "\n" + freedom
                    + "\n\nRivalries (your wins-rival wins)"
                    + "\n" + rivalCareerLine(arenaRoster.getTitus())
                    + "\n" + rivalCareerLine(arenaRoster.getCassius())
                    + "\n" + rivalCareerLine(arenaRoster.getRedWolf())
                    + "\n" + rivalCareerLine(arenaRoster.getViper())
                    + "\n\nCareer record: " + player.getWins() + " wins, " + player.getLosses() + " losses";
        }

        String nextStep = getFameToNextMilestone() == 0
                ? "The Champion Match is unlocked."
                : getFameToNextMilestone() + " Fame needed for " + getNextRankName() + ".";

        return player.getRank() + " | " + player.getFame() + "/300 Fame\n"
                + nextStep
                + "\n\nRivalries (your wins-rival wins)"
                + "\n" + rivalCareerLine(arenaRoster.getTitus())
                + "\n" + rivalCareerLine(arenaRoster.getCassius())
                + "\n" + rivalCareerLine(arenaRoster.getRedWolf())
                + "\n" + rivalCareerLine(arenaRoster.getViper())
                + "\n\nCareer record: " + player.getWins() + " wins, " + player.getLosses() + " losses"
                + "\nCurrent loss streak: " + player.getConsecutiveLosses() + "/3";
    }

    public String getCareerScreenTitle() {
        if (freedomPurchased) return "Life Beyond the Chains";
        if (championDefeated) return "Reign of the Champion";
        return "Road to the Champion";
    }

    public boolean isInBattle() {
        return currentEnemy != null && hasPlayer() && player.isAlive() && currentEnemy.isAlive();
    }

    private String rivalCareerLine(Rival rival) {
        return rival.getName() + ": " + rival.getDefeatsAgainstPlayer()
                + "-" + rival.getVictoriesAgainstPlayer()
                + " in " + rival.getEncountersAgainstPlayer() + " encounter"
                + (rival.getEncountersAgainstPlayer() == 1 ? "" : "s");
    }

    public Enemy getCurrentEnemy() {
        return currentEnemy;
    }

    public ArenaEvent getCurrentArenaEvent() {
        return currentArenaEvent;
    }

    public boolean isPlayerPoisoned() {
        return playerPoisoned;
    }

    public boolean isPlayerStaggered() {
        return hasPlayer() && player.getStamina() * 4 <= player.getMaxStamina();
    }

    public boolean isEnemyStaggered() {
        return currentEnemy != null
                && currentEnemy.getStamina() * 4 <= currentEnemy.getMaxStamina();
    }

    public boolean isCounterattackReady() {
        return counterattackReady;
    }

    public boolean isEnemyTrapped() {
        return enemyTrapped;
    }

    public int getBattleRound() {
        return battleRound;
    }

    public boolean isPlayerDefending() {
        return playerDefending;
    }

    public boolean isRivalBattle() {
        return currentEnemy instanceof Rival;
    }

    public String getRivalBannerText() {
        if (!(currentEnemy instanceof Rival)) return "";
        Rival rival = (Rival) currentEnemy;
        String meeting = rival.getEncountersAgainstPlayer() <= 1
                ? "FIRST ENCOUNTER" : "REMATCH " + rival.getEncountersAgainstPlayer();
        return "RIVAL • " + meeting + " • Record "
                + rival.getDefeatsAgainstPlayer() + "-" + rival.getVictoriesAgainstPlayer();
    }

    public boolean isGameOver() {
        return gameOver || hasPlayer() && player.isBrokenByLosses();
    }

    public double getPlayerHpPercent() {
        if (!hasPlayer()) return 0.0;
        return player.getMaxHp() == 0 ? 0.0 : player.getHp() * 1.0 / player.getMaxHp();
    }

    public double getPlayerStaminaPercent() {
        if (!hasPlayer()) return 0.0;
        return player.getMaxStamina() == 0 ? 0.0 : player.getStamina() * 1.0 / player.getMaxStamina();
    }

    public double getEnemyHpPercent() {
        if (currentEnemy == null) return 0.0;
        return currentEnemy.getMaxHp() == 0 ? 0.0 : currentEnemy.getHp() * 1.0 / currentEnemy.getMaxHp();
    }

    public String getHeaderText() {
        if (!hasPlayer()) {
            return "No gladiator";
        }

        if (isGameOver()) {
            return "Game Over | " + player.getName()
                    + " | Day " + day
                    + " | Record " + player.getWins() + "-" + player.getLosses();
        }

        return "Day " + day
                + " | " + player.getName()
                + " | " + player.getRank()
                + " | " + player.getDifficulty().getDisplayName()
                + " | Gold " + player.getGold()
                + " | Fame " + player.getFame();
    }

    public String getStatsText() {
        if (!hasPlayer()) {
            return "Start or load a game.";
        }

        return "Name: " + player.getName()
                + "\nClass: " + player.getGladiatorClass()
                + "\nHP: " + player.getHp() + "/" + player.getMaxHp()
                + "\nStamina: " + player.getStamina() + "/" + player.getMaxStamina()
                + "\nStrength: " + player.getStrength()
                + "\nDefense: " + player.getDefense()
                + "\nWeapon: " + player.getWeapon().getName()
                + "\nArmor: " + player.getArmor().getName()
                + "\nLevel: " + player.getLevel()
                + "\nXP: " + player.getExperience() + "/" + player.getExperienceToNextLevel()
                + "\nRank: " + player.getRank()
                + "\nGold: " + player.getGold()
                + "\nFame: " + player.getFame()
                + "\nCrowd Favor: " + player.getCrowdFavor()
                + "\nFatigue: " + player.getFatigue() + "/100"
                + "\nInjury: " + player.getInjuryType().getDisplayName()
                + "\nLoss Streak: " + player.getConsecutiveLosses() + "/3"
                + "\nRecord: " + player.getWins() + "-" + player.getLosses();
    }

    public String trainSafeDrills() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        int strengthBefore = player.getBaseStrength();
        int defenseBefore = player.getBaseDefense();
        int fatigueBefore = player.getFatigue();
        player.trainStrength();
        player.trainDefense();
        applyFatigueRisk();
        finishNonFightDay();
        return "Safe drills complete.\n"
                + statChange("STR", strengthBefore, player.getBaseStrength()) + "\n"
                + statChange("DEF", defenseBefore, player.getBaseDefense()) + "\n"
                + statChange("Fatigue", fatigueBefore, player.getFatigue());
    }

    public String trainBrutalConditioning() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        int strengthBefore = player.getBaseStrength();
        int hpBefore = player.getHp();
        int fatigueBefore = player.getFatigue();
        player.trainStrength();
        player.takeDamage(10);
        player.addFatigue(8);
        applyFatigueRisk();
        finishNonFightDay();
        return "Brutal conditioning complete.\n"
                + statChange("STR", strengthBefore, player.getBaseStrength()) + "\n"
                + statChange("HP", hpBefore, player.getHp()) + "\n"
                + statChange("Fatigue", fatigueBefore, player.getFatigue());
    }

    public String trainEndurance() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        int staminaBefore = player.getMaxStamina();
        int fatigueBefore = player.getFatigue();
        player.trainStamina();
        applyFatigueRisk();
        finishNonFightDay();
        return "Endurance laps complete.\n"
                + statChange("Max STA", staminaBefore, player.getMaxStamina()) + "\n"
                + statChange("Fatigue", fatigueBefore, player.getFatigue());
    }

    public String trainPublicSparring() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        int favorBefore = player.getCrowdFavor();
        int xpBefore = player.getExperience();
        InjuryType injuryBefore = player.getInjuryType();
        player.addCrowdFavor(4);
        if (random.nextInt(100) < 25) {
            player.applyInjury(InjuryType.SHAKEN);
            finishNonFightDay();
            return "Public sparring impressed the crowd, but you were shaken.\n"
                    + statChange("Favor", favorBefore, player.getCrowdFavor()) + "\n"
                    + "Injury: " + injuryBefore.getDisplayName() + " -> "
                    + player.getInjuryType().getDisplayName();
        }

        player.gainExperience(12);
        finishNonFightDay();
        return "Public sparring impressed the crowd.\n"
                + statChange("Favor", favorBefore, player.getCrowdFavor()) + "\n"
                + statChange("XP", xpBefore, player.getExperience());
    }

    public String restOneDay() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        playerPoisoned = false;
        counterattackReady = false;
        player.fullRest();
        finishNonFightDay();
        return "You rested for one day.";
    }

    public String restUntilHealed() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        int daysRested = 0;

        do {
            playerPoisoned = false;
            player.fullRest();
            finishNonFightDay();
            daysRested++;
        } while ((player.getHp() < player.getMaxHp()
                || player.getStamina() < player.getMaxStamina()
                || player.getInjuryType() != InjuryType.NONE
                || player.getFatigue() > 0)
                && daysRested < 3
                && player.getDaysSinceFight() < 4);

        return "Rested " + daysRested + " day(s).";
    }

    public String buyWeapon() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        ShopItem nextWeaponItem = shop.getNextWeaponItem(player);
        Weapon nextWeapon = shop.createNextClassWeapon(player);
        int fameRequirement = shop.getNextWeaponFameRequirement(player);

        if (nextWeaponItem == null || nextWeapon == null) {
            return "You already own the best weapon available.";
        }

        if (player.getFame() < fameRequirement) {
            return nextWeaponItem.getName() + " requires " + fameRequirement + " Fame.";
        }

        if (!player.spendGold(nextWeaponItem.getPrice())) {
            return "Not enough gold.";
        }

        player.equipWeapon(nextWeapon);
        finishNonFightDay();
        return "Bought and equipped " + player.getWeapon().getName() + ".";
    }

    public ShopOffer getWeaponOffer() {
        if (!hasPlayer()) return emptyOffer("Weapon");

        return gearOffer(shop.getNextWeaponItem(player), shop.getNextWeaponFameRequirement(player),
                "Best weapon equipped");
    }

    public String buyArmor() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        ShopItem nextArmorItem = shop.getNextArmorItem(player);
        Armor nextArmor = shop.createNextRankArmor(player);
        int fameRequirement = shop.getNextArmorFameRequirement(player);

        if (nextArmorItem == null || nextArmor == null) {
            return "You already own the best armor available.";
        }

        if (player.getFame() < fameRequirement) {
            return nextArmorItem.getName() + " requires " + fameRequirement + " Fame.";
        }

        if (!player.spendGold(nextArmorItem.getPrice())) {
            return "Not enough gold.";
        }

        player.equipArmor(nextArmor);
        finishNonFightDay();
        return "Bought and equipped " + player.getArmor().getName() + ".";
    }

    public ShopOffer getArmorOffer() {
        if (!hasPlayer()) return emptyOffer("Armor");

        return gearOffer(shop.getNextArmorItem(player), shop.getNextArmorFameRequirement(player),
                "Best armor equipped");
    }

    public String buyHealingSalve() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        if (!player.spendGold(shop.getHealingSalveItem().getPrice())) {
            return "Not enough gold.";
        }

        player.addHealingSalve();
        return "Bought a Healing Salve.";
    }

    public String buyStaminaMeal() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        if (!player.spendGold(shop.getStaminaMealItem().getPrice())) {
            return "Not enough gold.";
        }

        player.increaseMaxStamina(shop.getStaminaMealBonus());
        finishNonFightDay();
        return "Ate a Stamina Meal. Max stamina increased.";
    }

    public ShopOffer getHealingSalveOffer() {
        if (!hasPlayer()) return emptyOffer("Healing Salve");

        return consumableOffer(shop.getHealingSalveItem(), player.getHealingSalves());
    }

    public ShopOffer getStaminaMealOffer() {
        if (!hasPlayer()) return emptyOffer("Stamina Meal");

        return consumableOffer(shop.getStaminaMealItem(), 0);
    }

    public String buyStaminaDraught() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        if (!player.spendGold(shop.getStaminaDraughtItem().getPrice())) {
            return "Not enough gold.";
        }

        player.addStaminaDraught();
        return "Bought a Stamina Draught.";
    }

    public ShopOffer getStaminaDraughtOffer() {
        if (!hasPlayer()) return emptyOffer("Stamina Draught");

        return consumableOffer(shop.getStaminaDraughtItem(), player.getStaminaDraughts());
    }

    public String buyAntidote() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        if (!player.spendGold(shop.getAntidoteItem().getPrice())) {
            return "Not enough gold.";
        }

        player.addAntidote();
        return "Bought an Antidote.";
    }

    public ShopOffer getAntidoteOffer() {
        if (!hasPlayer()) return emptyOffer("Antidote");

        return consumableOffer(shop.getAntidoteItem(), player.getAntidotes());
    }

    public String buyWhetstone() {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();

        String blocked = startCompulsoryFightIfNeeded();
        if (blocked != null) return blocked;

        if (!player.spendGold(shop.getWhetstoneItem().getPrice())) {
            return "Not enough gold.";
        }

        player.addWhetstone();
        return "Bought a Whetstone.";
    }

    public ShopOffer getWhetstoneOffer() {
        if (!hasPlayer()) return emptyOffer("Whetstone");

        return consumableOffer(shop.getWhetstoneItem(), player.getWhetstones());
    }

    public String getShopHeaderText() {
        if (!hasPlayer()) return "Shop";

        return "Shop | Gold " + player.getGold() + " | Fame " + player.getFame();
    }

    public TrainingOptionInfo getSafeDrillsInfo() {
        return new TrainingOptionInfo("Safe Drills", "STR + DEF training", "Fatigue risk");
    }

    public TrainingOptionInfo getBrutalConditioningInfo() {
        return new TrainingOptionInfo("Brutal Training", "STR training", "-10 HP, +fatigue");
    }

    public TrainingOptionInfo getEnduranceInfo() {
        return new TrainingOptionInfo("Endurance Laps", "+Max Stamina training", "Fatigue risk");
    }

    public TrainingOptionInfo getPublicSparringInfo() {
        return new TrainingOptionInfo("Public Sparring", "+4 Favor, +12 XP", "25% Shaken");
    }

    public String save(int slot) {
        if (!hasPlayer()) return "Start a game first.";

        saveManager.saveGame(slot, player, day,
                arenaRoster.getTitus(),
                arenaRoster.getCassius(),
                arenaRoster.getRedWolf(),
                arenaRoster.getViper(),
                championDefeated,
                titleDefenses,
                freedomPurchased);
        return "Saved to slot " + slot + ".";
    }

    public String getSaveSlotText(int slot) {
        return saveManager.describeSlot(slot);
    }

    public SaveSlotInfo getSaveSlotInfo(int slot) {
        return saveManager.getSlotInfo(slot);
    }

    public String deleteSave(int slot) {
        return saveManager.deleteGame(slot)
                ? "Deleted save slot " + slot + "."
                : "Could not delete save slot " + slot + ".";
    }

    public String load(int slot) {
        SaveManager.GameSave loadedGame = saveManager.loadGame(slot);

        if (loadedGame == null) {
            return "Could not load slot " + slot + ".";
        }

        player = loadedGame.getPlayer();
        tutorialActive = false;
        day = loadedGame.getDay();
        gameOver = player.isBrokenByLosses();
        championDefeated = loadedGame.isChampionDefeated();
        titleDefenses = loadedGame.getTitleDefenses();
        freedomPurchased = loadedGame.isFreedomPurchased();
        freedomEndingAcknowledged = freedomPurchased;
        currentEnemy = null;
        currentContract = null;
        currentArenaEvent = ArenaEvent.NONE;
        lastBattleSummary = null;
        lastEnemySummary = null;
        arenaRoster.resetRivals();
        arenaRoster.restoreRivalHistory(
                loadedGame.getRivalEncounters(),
                new int[]{loadedGame.getTitusDefeats(), loadedGame.getCassiusDefeats(),
                        loadedGame.getRedWolfDefeats(), loadedGame.getViperDefeats()},
                loadedGame.getRivalVictories());
        return "Loaded slot " + slot + ".";
    }

    public String startArenaFight() {
        return startArenaFight(createMeasuredBout());
    }

    public String startMeasuredBout() {
        return startArenaFight(createMeasuredBout());
    }

    public String startBloodPrice() {
        return startArenaFight(createBloodPrice());
    }

    public String startRivalChallenge() {
        return startArenaFight(createRivalChallenge());
    }

    public String prepareMeasuredBout() {
        return prepareFight(createMeasuredBout());
    }

    public String prepareBloodPrice() {
        return prepareFight(createBloodPrice());
    }

    public String prepareRivalChallenge() {
        return prepareFight(createRivalChallenge());
    }

    public String prepareChampionMatch() {
        if (!hasPlayer()) return "Start a game first.";
        if (championDefeated) return "You have already claimed the arena crown.";
        if (player.getFame() < 300) return "The Champion Match requires 300 fame.";
        pendingContract = createChampionMatch();
        pendingArenaEvent = ArenaEvent.NONE;
        pendingEnemy = arenaRoster.createChampion();
        pendingEnemy.restore();
        return "The Champion Match is ready.";
    }

    public boolean hasFightPreview() {
        return pendingContract != null && pendingEnemy != null;
    }

    public PreFightInfo getPreFightInfo() {
        if (!hasFightPreview()) return new PreFightInfo("Choose an arena contract first.");

        int gold = pendingArenaEvent.applyGold(pendingContract.applyGold(pendingEnemy.getGoldReward()));
        if (championDefeated) gold += 100;
        int fame = pendingArenaEvent.applyFame(pendingContract.applyFame(pendingEnemy.getFameReward()));
        gold = getDifficulty().adjustReward(gold);
        fame = getDifficulty().adjustReward(fame);
        String record = pendingEnemy instanceof Rival
                ? "\nRival record: " + ((Rival) pendingEnemy).getDefeatsAgainstPlayer() + "-"
                + ((Rival) pendingEnemy).getVictoriesAgainstPlayer() : "";
        return new PreFightInfo(
                pendingContract.getName() + "\n\n"
                        + pendingEnemy.getName() + "\n"
                        + "Threat: " + threatLevel(pendingEnemy) + "\n"
                        + "HP " + pendingEnemy.getMaxHp() + " | STA " + pendingEnemy.getMaxStamina()
                        + " | STR " + pendingEnemy.getStrength() + " | DEF " + pendingEnemy.getDefense() + "\n"
                        + "Ability: " + displayAbility(pendingEnemy.getAbility()) + "\n"
                        + "Tactical hint: " + tacticalHint(pendingEnemy.getAbility()) + "\n\n"
                        + "Arena event: " + pendingArenaEvent.getName() + "\n"
                        + pendingArenaEvent.getDescription() + "\n\n"
                        + (championDefeated ? "Title defense purse: about " : "Victory reward: about ")
                        + gold + " gold, " + fame + " fame"
                        + record);
    }

    public String confirmFightPreview() {
        if (!hasFightPreview()) return "Choose an arena contract first.";
        FightContract contract = pendingContract;
        Enemy enemy = pendingEnemy;
        ArenaEvent event = pendingArenaEvent;
        clearFightPreview();
        return startArenaFight(contract, enemy, event);
    }

    public void clearFightPreview() {
        pendingContract = null;
        pendingEnemy = null;
        pendingArenaEvent = ArenaEvent.NONE;
    }

    private String prepareFight(FightContract contract) {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();
        if (isInBattle()) return "A fight is already underway.";
        pendingContract = contract;
        pendingArenaEvent = rollArenaEvent();
        pendingEnemy = arenaRoster.createArenaOpponent(day, player, contract.isRivalChallenge());
        pendingEnemy.restore();
        return "Fight preview ready: " + pendingEnemy.getName() + ".";
    }

    public FightContractInfo getMeasuredBoutInfo() {
        return contractInfo(createMeasuredBout());
    }

    public FightContractInfo getBloodPriceInfo() {
        return contractInfo(createBloodPrice());
    }

    public FightContractInfo getRivalChallengeInfo() {
        return contractInfo(createRivalChallenge());
    }

    private String startArenaFight(FightContract contract) {
        if (!hasPlayer()) return "Start a game first.";
        if (isGameOver()) return gameOverText();
        if (isInBattle()) return "A fight is already underway.";

        Enemy enemy = arenaRoster.createArenaOpponent(day, player, contract.isRivalChallenge());
        return startArenaFight(contract, enemy, rollArenaEvent());
    }

    private String startArenaFight(FightContract contract, Enemy enemy, ArenaEvent event) {
        currentContract = contract;
        currentArenaEvent = event;
        currentArenaEvent.announce(player);
        currentEnemy = enemy;
        currentEnemy.restore();
        lastBattleSummary = null;
        lastEnemySummary = describeEnemy(currentEnemy);
        battleRound = 1;
        playerDefending = false;
        enemyTrapped = false;
        playerPoisoned = false;

        String intro = "Contract: " + contract.getMenuText()
                + "\nArena Event: " + currentArenaEvent.getName()
                + "\n" + currentArenaEvent.getDescription()
                + "\nThe arena gates open.\n" + player.getName() + " faces " + currentEnemy.getName() + ".";
        if (championDefeated) {
            intro += freedomPurchased
                    ? "\nA free champion returns to the sand by choice."
                    : "\nThe Champion returns to defend the crown. Another victory brings freedom closer.";
        }
        if (currentEnemy instanceof Rival) {
            Rival rival = (Rival) currentEnemy;
            arenaRoster.recordRivalEncounter(rival);
            boolean rematch = rival.getEncountersAgainstPlayer() > 1;
            intro += rematch ? "\nA familiar rival returns to the sand."
                    : "\nA named rival enters the arena.";
            intro += "\nRival record: " + rival.getDefeatsAgainstPlayer()
                    + " player wins - " + rival.getVictoriesAgainstPlayer() + " rival wins.";
            intro += "\n\"" + rival.getEncounterQuote() + "\"";
            if (championDefeated) {
                intro += "\nA familiar rival has returned—not for fame, but for the crown.";
            }
        }

        return intro;
    }

    private String threatLevel(Enemy enemy) {
        int playerPower = player.getMaxHp() / 5 + player.getStrength() * 2 + player.getDefense() * 2;
        int enemyPower = enemy.getMaxHp() / 5 + enemy.getStrength() * 2 + enemy.getDefense() * 2;
        if (enemyPower <= playerPower * 85 / 100) return "Low";
        if (enemyPower <= playerPower * 110 / 100) return "Moderate";
        if (enemyPower <= playerPower * 135 / 100) return "High";
        return "Severe";
    }

    private String displayAbility(EnemyAbility ability) {
        String text = ability.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private String tacticalHint(EnemyAbility ability) {
        switch (ability) {
            case POISON_STRIKE: return "Carry an Antidote and end the fight quickly.";
            case DOUBLE_STRIKE: case DUST_FLURRY: return "Defend when your HP is exposed to two hits.";
            case CRUSHING_BLOW: case BUTCHERS_CLEAVE: case WILD_SWING: return "Heavy but inaccurate; guarding limits a successful blow.";
            case SHIELD_BASH: case NET_CAST: return "Protect your stamina and keep a Draught ready.";
            case HOOK_SLASH: return "Its hook partly bypasses defense; favor offense over turtling.";
            case IRON_WALL: return "A durable opponent; manage stamina for a longer fight.";
            case NONE: default: return "Watch your stamina and defend before dangerous turns.";
        }
    }

    public BattleActionInfo getAttackInfo() {
        return staminaAction(counterattackReady ? "Counterattack" : "Attack",
                counterattackReady ? "5 STA | bonus damage" : "5 STA", 5);
    }

    public BattleActionInfo getHeavyAttackInfo() {
        return staminaAction("Heavy Attack", "15 STA", 15);
    }

    public BattleActionInfo getSpecialInfo() {
        String detail = hasPlayer() ? getSpecialStaminaCost() + " STA | " + player.getGladiatorClass() : "";
        if (isPlayerStaggered()) {
            return new BattleActionInfo(hasPlayer() ? getSpecialMoveName() : "Special", detail,
                    false, "Staggered");
        }
        return staminaAction(hasPlayer() ? getSpecialMoveName() : "Special", detail,
                hasPlayer() ? getSpecialStaminaCost() : 0);
    }

    public BattleActionInfo getDefendInfo() {
        return staminaAction("Defend", "3 STA", 3);
    }

    public BattleActionInfo getCatchBreathInfo() {
        return new BattleActionInfo("Catch Breath", "+15 STA, +5 HP", isInBattle(), isInBattle() ? "" : "No battle");
    }

    public BattleActionInfo getForfeitInfo() {
        return new BattleActionInfo("Forfeit", "-10 Fame", isInBattle(), isInBattle() ? "" : "No battle");
    }

    public BattleActionInfo getHealingSalveInfo() {
        int owned = hasPlayer() ? player.getHealingSalves() : 0;
        boolean enabled = isInBattle() && owned > 0 && player.getHp() < player.getMaxHp();
        return new BattleActionInfo("Healing Salve", owned + " owned | +30 HP", enabled,
                itemDisabledReason(owned, "HP is full"));
    }

    public BattleActionInfo getStaminaDraughtInfo() {
        int owned = hasPlayer() ? player.getStaminaDraughts() : 0;
        boolean enabled = isInBattle() && owned > 0 && player.getStamina() < player.getMaxStamina();
        return new BattleActionInfo("Stamina Draught", owned + " owned | +25 STA", enabled,
                itemDisabledReason(owned, "Stamina is full"));
    }

    public BattleActionInfo getAntidoteInfo() {
        int owned = hasPlayer() ? player.getAntidotes() : 0;
        boolean enabled = isInBattle() && owned > 0 && playerPoisoned;
        return new BattleActionInfo("Antidote", owned + " owned | cures poison", enabled,
                itemDisabledReason(owned, "Not poisoned"));
    }

    public BattleActionInfo getWhetstoneInfo() {
        int owned = hasPlayer() ? player.getWhetstones() : 0;
        boolean enabled = isInBattle() && owned > 0;
        return new BattleActionInfo("Whetstone", owned + " owned | +1 STR", enabled,
                itemDisabledReason(owned, "No battle"));
    }

    public String battleUseStaminaDraught() {
        if (!isInBattle()) return "Start an arena fight first.";

        if (!player.useStaminaDraught()) {
            return "No Stamina Draughts.";
        }

        return finishPlayerBattleAction("Round " + battleRound
                + "\n" + player.getName() + " drinks a Stamina Draught.");
    }

    public String battleUseAntidote() {
        if (!isInBattle()) return "Start an arena fight first.";

        if (!player.useAntidote()) {
            return "No Antidotes.";
        }

        playerPoisoned = false;
        return finishPlayerBattleAction("Round " + battleRound
                + "\n" + player.getName() + " uses an Antidote.");
    }

    public String battleUseWhetstone() {
        if (!isInBattle()) return "Start an arena fight first.";

        if (!player.useWhetstone()) {
            return "No Whetstones.";
        }

        return finishPlayerBattleAction("Round " + battleRound
                + "\n" + player.getName() + " sharpens the weapon.");
    }

    public boolean hasBattleSummary() {
        return lastBattleSummary != null;
    }

    public BattleSummary getLastBattleSummaryInfo() {
        return lastBattleSummary;
    }

    public String getLastBattleSummary() {
        if (lastBattleSummary == null) {
            return "";
        }

        return formatBattleSummary(lastBattleSummary);
    }

    public void clearBattleSummary() {
        lastBattleSummary = null;
    }

    public String getEnemyInfoText() {
        if (currentEnemy != null) {
            return describeEnemy(currentEnemy);
        }

        return lastEnemySummary == null ? "No active enemy." : lastEnemySummary;
    }

    public String battleAttack() {
        if (!isInBattle()) return "Start an arena fight first.";

        String result = "Round " + battleRound + "\n";

        if (!player.hasStamina(5)) {
            return result + "Not enough stamina for Attack.";
        }

        player.useStamina(5);

        if (random.nextInt(100) < 10) {
            result += player.getName() + " attacks, but misses.";
        } else {
            int damage = Math.max(1, player.getStrength() - effectiveEnemyDefense());
            damage = applyThraexPassive(damage);
            if (counterattackReady) {
                int counterBonus = player.getGladiatorClass() == GladiatorClass.MURMILLO
                        ? player.getStrength() : Math.max(2, player.getStrength() / 2);
                damage += counterBonus;
                result += "Counterattack!\n";
                counterattackReady = false;
            }
            if (random.nextInt(100) < 15) {
                damage *= 2;
                result += "Critical hit!\n";
            }
            currentEnemy.takeDamage(damage);
            int extraDamage = applyDimachaerusPassive();
            result += player.getName() + " attacks for " + damage + " damage.";
            if (extraDamage > 0) {
                result += "\n" + player.getName() + " follows with an extra blade for "
                        + extraDamage + " damage.";
            }
        }

        return finishPlayerBattleAction(result);
    }

    public String battleSpecial() {
        if (!isInBattle()) return "Start an arena fight first.";

        String result = "Round " + battleRound + "\n";
        if (isPlayerStaggered()) {
            return result + "You are staggered and cannot use a special ability. Recover stamina first.";
        }
        int staminaCost = getSpecialStaminaCost();

        if (!player.hasStamina(staminaCost)) {
            return result + "Not enough stamina for your class special.";
        }

        player.useStamina(staminaCost);

        switch (player.getGladiatorClass()) {
            case MURMILLO:
                int bashDamage = Math.max(1, player.getStrength() - effectiveEnemyDefense() / 2);
                bashDamage = applyThraexPassive(bashDamage);
                currentEnemy.takeDamage(bashDamage);
                playerDefending = true;
                result += player.getName() + " uses Shield Bash for " + bashDamage
                        + " damage and braces for impact.";
                break;
            case RETIARIUS:
                if (random.nextInt(100) < 15) {
                    result += player.getName() + " casts the net, but misses.";
                } else {
                    int netDamage = Math.max(1, player.getStrength() - effectiveEnemyDefense() / 2);
                    netDamage = applyThraexPassive(netDamage);
                    currentEnemy.takeDamage(netDamage);
                    currentEnemy.useStamina(12);
                    enemyTrapped = true;
                    player.recoverStamina(5);
                    result += player.getName() + " traps " + currentEnemy.getName()
                            + " for " + netDamage + " damage and drains 12 stamina. The enemy loses the turn.";
                    if (!currentEnemy.isAlive()) {
                        return result + "\n" + finishBattleVictory();
                    }
                    battleRound++;
                    playerDefending = false;
                    return result;
                }
                break;
            case DIMACHAERUS:
                int first = applyThraexPassive(Math.max(1, player.getStrength() - effectiveEnemyDefense()));
                int second = applyThraexPassive(Math.max(1, player.getStrength() - effectiveEnemyDefense()));
                currentEnemy.takeDamage(first);
                currentEnemy.takeDamage(second);
                result += player.getName() + " strikes with both blades. "
                        + first + " and " + second + " damage.";
                break;
            case THRAEX:
            default:
                int slashDamage = Math.max(1, player.getStrength() + 5 - effectiveEnemyDefense());
                slashDamage = applyThraexPassive(slashDamage);
                currentEnemy.takeDamage(slashDamage);
                result += player.getName() + " hooks past the guard for " + slashDamage + " damage.";
                break;
        }

        return finishPlayerBattleAction(result);
    }

    public String battleUseHealingSalve() {
        if (!isInBattle()) return "Start an arena fight first.";

        if (!player.useHealingSalve()) {
            return "No Healing Salves.";
        }

        return finishPlayerBattleAction("Round " + battleRound
                + "\n" + player.getName() + " uses a Healing Salve.");
    }

    public String battleHeavyAttack() {
        if (!isInBattle()) return "Start an arena fight first.";

        String result = "Round " + battleRound + "\n";

        if (!player.hasStamina(15)) {
            return result + "Not enough stamina for Heavy Attack.";
        }

        player.useStamina(15);

        if (random.nextInt(100) < 25) {
            result += player.getName() + " commits to a heavy attack, but misses.";
        } else {
            int damage = Math.max(1, player.getStrength() * 2 - effectiveEnemyDefense());
            damage = applyThraexPassive(damage);
            if (random.nextInt(100) < 10) {
                damage *= 2;
                result += "Brutal critical hit!\n";
            }
            currentEnemy.takeDamage(damage);
            result += player.getName() + " lands a heavy attack for " + damage + " damage.";
        }

        return finishPlayerBattleAction(result);
    }

    public String battleDefend() {
        if (!isInBattle()) return "Start an arena fight first.";

        if (!player.hasStamina(3)) {
            return "Round " + battleRound + "\nNot enough stamina to defend.";
        }

        player.useStamina(3);
        playerDefending = true;
        return finishPlayerBattleAction("Round " + battleRound + "\n" + player.getName() + " raises a guard.");
    }

    public String battleRest() {
        if (!isInBattle()) return "Start an arena fight first.";

        int staminaRecovered = player.getGladiatorClass() == GladiatorClass.RETIARIUS ? 20 : 15;
        player.recoverStamina(staminaRecovered);
        player.heal(5);
        return finishPlayerBattleAction("Round " + battleRound + "\n" + player.getName()
                + " catches his breath. +" + staminaRecovered + " stamina, +5 HP.");
    }

    public String battleForfeit() {
        if (!isInBattle()) return "Start an arena fight first.";

        Enemy forfeitedEnemy = currentEnemy;
        currentEnemy = null;
        enemyTrapped = false;
        playerPoisoned = false;
        currentContract = null;
        currentArenaEvent = ArenaEvent.NONE;

        player.recordFightDay();
        player.gainReward(0, -10);
        nextDay();

        lastBattleSummary = new BattleSummary(
                BattleSummary.Outcome.DEFEAT,
                forfeitedEnemy.getName(),
                0,
                -10,
                "None",
                player.getConsecutiveLosses(),
                3,
                player.getWins(),
                player.getLosses(),
                false,
                "You left the sand alive, but the crowd remembers.");

        return "Forfeit. " + player.getName() + " yields the match and loses 10 fame.";
    }

    private String finishPlayerBattleAction(String result) {
        if (!currentEnemy.isAlive()) {
            return result + "\n" + finishBattleVictory();
        }

        if (playerPoisoned) {
            int poisonTickDamage = difficultyDamage(3);
            player.takeDamage(poisonTickDamage);
            result += "\nPoison burns through " + player.getName() + " for "
                    + poisonTickDamage + " damage.";
            if (!player.isAlive()) {
                return result + "\n" + finishBattleDefeat();
            }
        }

        if (enemyTrapped) {
            enemyTrapped = false;
            battleRound++;
            playerDefending = false;
            return result + "\n" + currentEnemy.getName() + " is trapped in the net and loses the turn.";
        }

        result += "\n" + enemyTurn();

        if (!player.isAlive()) {
            return result + "\n" + finishBattleDefeat();
        }

        battleRound++;
        playerDefending = false;
        return result;
    }

    private String enemyTurn() {
        if (isEnemyStaggered() && random.nextInt(100) < 65) {
            currentEnemy.recoverStamina(18);
            return currentEnemy.getName() + " steps back and recovers 18 stamina.";
        }

        int specialChance = currentEnemy.getHp() * 4 <= currentEnemy.getMaxHp() ? 40 : 25;
        if (!isEnemyStaggered() && currentEnemy.hasStamina(12)
                && currentEnemy.getAbility() != EnemyAbility.NONE && random.nextInt(100) < specialChance) {
            currentEnemy.useStamina(12);
            return enemySpecialAttack();
        }

        currentEnemy.useStamina(5);
        return normalEnemyAttack();
    }

    private String normalEnemyAttack() {
        int damage = Math.max(1, currentEnemy.getStrength() - effectivePlayerDefense() + random.nextInt(5));
        damage = reduceForMurmilloPassive(damage);

        if (random.nextInt(100) < 20) {
            damage = Math.max(1, currentEnemy.getStrength() * 2 - effectivePlayerDefense());
            damage = reduceForMurmilloPassive(damage);
        }

        if (playerDefending) {
            damage = Math.max(1, damage / 2);
            counterattackReady = true;
        }

        damage = difficultyDamage(damage);
        player.takeDamage(damage);
        return currentEnemy.getName() + " strikes back for " + damage + " damage.";
    }

    private String enemySpecialAttack() {
        switch (currentEnemy.getAbility()) {
            case BUTCHERS_CLEAVE:
                if (random.nextInt(100) < 25) {
                    return currentEnemy.getName() + " swings wildly and misses.";
                }
                int cleaveDamage = reduceIncomingDamage(
                        Math.max(1, currentEnemy.getStrength() * 2 - effectivePlayerDefense()));
                cleaveDamage = difficultyDamage(cleaveDamage);
                player.takeDamage(cleaveDamage);
                return currentEnemy.getName() + " uses Butcher's Cleave for " + cleaveDamage + " damage.";
            case CRUSHING_BLOW:
                if (random.nextInt(100) < 35) {
                    return currentEnemy.getName() + " tries a Crushing Blow but misses.";
                }
                int crushingDamage = reduceIncomingDamage(
                        Math.max(1, currentEnemy.getStrength() * 3 - effectivePlayerDefense()));
                crushingDamage = difficultyDamage(crushingDamage);
                player.takeDamage(crushingDamage);
                return currentEnemy.getName() + " lands a Crushing Blow for " + crushingDamage + " damage.";
            case DOUBLE_STRIKE:
                int first = reduceIncomingDamage(Math.max(1, currentEnemy.getStrength() - effectivePlayerDefense()));
                int second = reduceIncomingDamage(Math.max(1, currentEnemy.getStrength() - effectivePlayerDefense()));
                first = difficultyDamage(first);
                second = difficultyDamage(second);
                player.takeDamage(first);
                player.takeDamage(second);
                return currentEnemy.getName() + " attacks twice!\nFirst hit: " + first
                        + " damage.\nSecond hit: " + second + " damage.";
            case POISON_STRIKE:
                int poisonDamage = reduceIncomingDamage(
                        Math.max(1, currentEnemy.getStrength() + 4 - effectivePlayerDefense()));
                poisonDamage = difficultyDamage(poisonDamage);
                player.takeDamage(poisonDamage);
                playerPoisoned = true;
                return player.getName() + " is poisoned.\n" + currentEnemy.getName()
                        + " uses " + getPoisonMoveName() + " for " + poisonDamage + " damage.";
            case SHIELD_BASH:
                int bashDamage = reduceIncomingDamage(
                        Math.max(1, currentEnemy.getStrength() - effectivePlayerDefense() / 2));
                bashDamage = difficultyDamage(bashDamage);
                player.takeDamage(bashDamage);
                player.useStamina(6);
                return currentEnemy.getName() + " slams the shield for " + bashDamage
                        + " damage and drains 6 stamina.";
            case DUST_FLURRY:
                int quickFirst = reduceIncomingDamage(
                        Math.max(1, currentEnemy.getStrength() - effectivePlayerDefense()));
                int quickSecond = reduceIncomingDamage(
                        Math.max(1, currentEnemy.getStrength() - effectivePlayerDefense() - 2));
                quickFirst = difficultyDamage(quickFirst);
                quickSecond = difficultyDamage(quickSecond);
                player.takeDamage(quickFirst);
                player.takeDamage(quickSecond);
                return currentEnemy.getName() + " uses Dust Flurry!\nFirst cut: " + quickFirst
                        + " damage.\nSecond cut: " + quickSecond + " damage.";
            case WILD_SWING:
                if (random.nextInt(100) < 40) {
                    return currentEnemy.getName() + " swings the club wildly and misses.";
                }
                int swingDamage = reduceIncomingDamage(
                        Math.max(1, currentEnemy.getStrength() * 2 - effectivePlayerDefense()));
                swingDamage = difficultyDamage(swingDamage);
                player.takeDamage(swingDamage);
                return currentEnemy.getName() + " lands a Wild Swing for " + swingDamage + " damage.";
            case HOOK_SLASH:
                int hookDamage = reduceIncomingDamage(
                        Math.max(1, currentEnemy.getStrength() + 3 - effectivePlayerDefense() / 2));
                hookDamage = difficultyDamage(hookDamage);
                player.takeDamage(hookDamage);
                return currentEnemy.getName() + " hooks past the guard for " + hookDamage + " damage.";
            case NET_CAST:
                int netDamage = reduceIncomingDamage(
                        Math.max(1, currentEnemy.getStrength() - effectivePlayerDefense()));
                netDamage = difficultyDamage(netDamage);
                player.takeDamage(netDamage);
                player.useStamina(12);
                return currentEnemy.getName() + " catches " + player.getName()
                        + " in the weighted net for " + netDamage + " damage and drains 12 stamina.";
            case IRON_WALL:
                int wallDamage = reduceIncomingDamage(
                        Math.max(1, currentEnemy.getStrength() + currentEnemy.getDefense()
                                - effectivePlayerDefense()));
                wallDamage = difficultyDamage(wallDamage);
                player.takeDamage(wallDamage);
                currentEnemy.recoverStamina(10);
                return currentEnemy.getName() + " drives forward behind the shield for "
                        + wallDamage + " damage.";
            case NONE:
            default:
                return normalEnemyAttack();
        }
    }

    private String getPoisonMoveName() {
        return currentEnemy.getName().equals("Viper of the Sands") ? "Viper Strike" : "Venom Strike";
    }

    private int difficultyDamage(int damage) {
        return getDifficulty().adjustIncomingDamage(damage);
    }

    private int reduceIncomingDamage(int damage) {
        damage = reduceForMurmilloPassive(damage);

        if (playerDefending) {
            damage = Math.max(1, damage / 2);
            counterattackReady = true;
        }

        return damage;
    }

    private int effectivePlayerDefense() {
        return isPlayerStaggered() ? Math.max(0, player.getDefense() / 2) : player.getDefense();
    }

    private int effectiveEnemyDefense() {
        return isEnemyStaggered() ? Math.max(0, currentEnemy.getDefense() / 2) : currentEnemy.getDefense();
    }

    private String finishBattleVictory() {
        Enemy defeatedEnemy = currentEnemy;
        boolean reigningChampion = championDefeated;
        FightContract contract = currentContract == null ? createMeasuredBout() : currentContract;
        ArenaEvent event = currentArenaEvent;
        currentEnemy = null;
        enemyTrapped = false;
        playerPoisoned = false;
        currentContract = null;
        currentArenaEvent = ArenaEvent.NONE;

        player.addWin();
        player.recordFightDay();

        int goldReward = event.applyGold(contract.applyGold(defeatedEnemy.getGoldReward()));
        int fameReward = event.applyFame(contract.applyFame(defeatedEnemy.getFameReward()));
        if (player.getFame() < 50) {
            fameReward += 3;
        }
        if (defeatedEnemy instanceof Rival) {
            Rival rival = (Rival) defeatedEnemy;
            fameReward += rival.getDefeatsAgainstPlayer() == 0 ? 8 : 3;
            arenaRoster.recordRivalDefeat(rival);
        }
        boolean championVictory = defeatedEnemy.getName().equals("Aurelius the Unbroken");
        if (championVictory) {
            championDefeated = true;
        }
        if (reigningChampion && !championVictory) {
            titleDefenses++;
            goldReward += 100;
        }

        goldReward = getDifficulty().adjustReward(goldReward);
        fameReward = getDifficulty().adjustReward(fameReward);

        player.gainReward(goldReward, fameReward);
        player.addCrowdFavor(3);
        if (player.getHp() * 100 / player.getMaxHp() <= 25) {
            player.addCrowdFavor(5);
        }
        if (player.spendCrowdFavor(20)) {
            goldReward += 15;
            fameReward += 5;
            player.gainReward(15, 5);
        }
        nextDay();

        String nextStep = "Rest if hurt, shop if you can upgrade, or pick another contract.";
        if (defeatedEnemy instanceof Rival) {
            Rival rival = (Rival) defeatedEnemy;
            nextStep = "Rivalry record: " + rival.getDefeatsAgainstPlayer() + "-"
                    + rival.getVictoriesAgainstPlayer() + ". The rival will remember this defeat.";
        }
        if (championVictory) {
            nextStep = "The arena crown is yours. Your gladiator's rise is complete.";
        } else if (reigningChampion) {
            nextStep = freedomPurchased
                    ? "The free Champion successfully defends the crown."
                    : "Title defense " + titleDefenses + " complete. "
                    + Math.max(0, FREEDOM_PRICE - player.getGold()) + " gold remains before freedom.";
        }

        lastBattleSummary = new BattleSummary(
                BattleSummary.Outcome.VICTORY,
                defeatedEnemy.getName(),
                goldReward,
                fameReward,
                "",
                player.getConsecutiveLosses(),
                3,
                player.getWins(),
                player.getLosses(),
                false,
                nextStep);

        return (reigningChampion ? "The Champion successfully defends the crown! " : "Victory! ")
                + "+" + goldReward + " gold, +" + fameReward + " fame.";
    }

    private String finishBattleDefeat() {
        Enemy defeatedBy = currentEnemy;
        if (defeatedBy instanceof Rival) {
            arenaRoster.recordRivalVictory((Rival) defeatedBy);
        }
        currentEnemy = null;
        enemyTrapped = false;
        playerPoisoned = false;
        currentContract = null;
        currentArenaEvent = ArenaEvent.NONE;

        player.addLoss();
        player.recordFightDay();

        int injuryRoll = random.nextInt(100);
        if (injuryRoll < 33) {
            player.applyInjury(InjuryType.BRUISED_RIBS);
        } else if (injuryRoll < 66) {
            player.applyInjury(InjuryType.WOUNDED_ARM);
        } else {
            player.applyInjury(InjuryType.SHAKEN);
        }

        if (player.isBrokenByLosses()) {
            gameOver = true;
            lastBattleSummary = new BattleSummary(
                    BattleSummary.Outcome.DEFEAT,
                    defeatedBy == null ? "" : defeatedBy.getName(),
                    0,
                    0,
                    player.getInjuryType().getDisplayName(),
                    player.getConsecutiveLosses(),
                    3,
                    player.getWins(),
                    player.getLosses(),
                    true,
                    defeatedBy instanceof Rival
                            ? "Rivalry record: " + ((Rival) defeatedBy).getDefeatsAgainstPlayer()
                            + "-" + ((Rival) defeatedBy).getVictoriesAgainstPlayer()
                            + ". Three consecutive losses ended the run."
                            : "Three consecutive losses ended the run.");
            return "Defeat. Three consecutive losses end the gladiator's story.";
        }

        player.recoverFromArenaDefeat();
        player.recoverInInfirmary();
        player.gainReward(0, -8);
        nextDay();

        lastBattleSummary = new BattleSummary(
                BattleSummary.Outcome.DEFEAT,
                defeatedBy == null ? "" : defeatedBy.getName(),
                0,
                -8,
                player.getInjuryType().getDisplayName(),
                player.getConsecutiveLosses(),
                3,
                player.getWins(),
                player.getLosses(),
                false,
                defeatedBy instanceof Rival
                        ? "Rivalry record: " + ((Rival) defeatedBy).getDefeatsAgainstPlayer()
                        + "-" + ((Rival) defeatedBy).getVictoriesAgainstPlayer()
                        + ". Recover before seeking a rematch."
                        : "Recover, then rebuild with safer contracts.");

        return "Defeat. The guards drag you to the infirmary. Loss streak: "
                + player.getConsecutiveLosses() + "/3.";
    }

    private void finishNonFightDay() {
        player.recordNonFightDay();
        nextDay();
    }

    private void nextDay() {
        day++;
        applyArenaPressure();
    }

    private String startCompulsoryFightIfNeeded() {
        if (player.getDaysSinceFight() < 4 || isInBattle()) {
            return null;
        }

        currentEnemy = arenaRoster.createArenaOpponent(day, player, false);
        currentContract = createLanistaOrder();
        currentArenaEvent = ArenaEvent.NONE;
        currentEnemy.restore();
        lastBattleSummary = null;
        lastEnemySummary = describeEnemy(currentEnemy);
        battleRound = 1;
        playerDefending = false;
        enemyTrapped = false;
        playerPoisoned = false;
        counterattackReady = false;

        return "The lanista refuses another idle day.\n"
                + "\"Enough. You earn your food in the sand.\"\n"
                + player.getName() + " is forced to face " + currentEnemy.getName() + ".";
    }

    private void applyArenaPressure() {
        if (player.getDaysSinceFight() >= 4) {
            player.addCrowdFavor(-5);
            player.gainReward(0, -5);
        }
    }

    private void applyFatigueRisk() {
        if (player.getFatigue() < 70) {
            return;
        }

        if (random.nextInt(100) < player.getFatigue() - 55) {
            player.applyInjury(InjuryType.BRUISED_RIBS);
        }
    }

    private int getSpecialStaminaCost() {
        switch (player.getGladiatorClass()) {
            case MURMILLO:
                return 12;
            case RETIARIUS:
                return 15;
            case DIMACHAERUS:
                return 20;
            case THRAEX:
                return 14;
            default:
                return 0;
        }
    }

    private String getSpecialMoveName() {
        switch (player.getGladiatorClass()) {
            case MURMILLO:
                return "Shield Bash";
            case RETIARIUS:
                return "Net Cast";
            case DIMACHAERUS:
                return "Twin Blades";
            case THRAEX:
                return "Hook Slash";
            default:
                return "Special";
        }
    }

    private int applyThraexPassive(int damage) {
        if (player.getGladiatorClass() == GladiatorClass.THRAEX && currentEnemy.getDefense() >= 5) {
            return damage + 3;
        }

        return damage;
    }

    private int applyDimachaerusPassive() {
        if (player.getGladiatorClass() != GladiatorClass.DIMACHAERUS || random.nextInt(100) >= 15) {
            return 0;
        }

        int damage = Math.max(1, player.getStrength() / 2 - effectiveEnemyDefense() / 2);
        currentEnemy.takeDamage(damage);
        return damage;
    }

    private int reduceForMurmilloPassive(int damage) {
        if (playerDefending && player.getGladiatorClass() == GladiatorClass.MURMILLO) {
            return Math.max(1, damage - 2);
        }

        return damage;
    }

    private ShopOffer gearOffer(ShopItem item, int fameRequirement, String maxText) {
        if (item == null) {
            return new ShopOffer(maxText, "", 0, -1, ShopOffer.Status.OWNED_BEST);
        }

        if (player.getFame() < fameRequirement) {
            return new ShopOffer(item.getName(), item.getDescription(), item.getPrice(), fameRequirement,
                    ShopOffer.Status.LOCKED);
        }

        if (player.getGold() < item.getPrice()) {
            return new ShopOffer(item.getName(), item.getDescription(), item.getPrice(), fameRequirement,
                    ShopOffer.Status.NOT_ENOUGH_GOLD);
        }

        return new ShopOffer(item.getName(), item.getDescription(), item.getPrice(), fameRequirement,
                ShopOffer.Status.AVAILABLE);
    }

    private ShopOffer consumableOffer(ShopItem item, int owned) {
        if (player.getGold() < item.getPrice()) {
            return new ShopOffer(item.getName(), item.getDescription(), item.getPrice(), 0, owned,
                    ShopOffer.Status.NOT_ENOUGH_GOLD);
        }

        return new ShopOffer(item.getName(), item.getDescription(), item.getPrice(), 0, owned,
                ShopOffer.Status.AVAILABLE);
    }

    private ShopOffer emptyOffer(String name) {
        return new ShopOffer(name, "Start a game first", 0, 0, ShopOffer.Status.LOCKED);
    }

    private BattleActionInfo staminaAction(String name, String detail, int staminaCost) {
        if (!isInBattle()) {
            return new BattleActionInfo(name, detail, false, "No battle");
        }

        if (!player.hasStamina(staminaCost)) {
            return new BattleActionInfo(name, detail, false, "Not enough stamina");
        }

        return new BattleActionInfo(name, detail, true, "");
    }

    private FightContractInfo contractInfo(FightContract contract) {
        return new FightContractInfo(
                contract.getName(),
                contract.getDescription(),
                contract.getGoldPercent(),
                contract.getFamePercent(),
                contract.isRivalChallenge());
    }

    private String itemDisabledReason(int owned, String fallbackReason) {
        if (!isInBattle()) {
            return "No battle";
        }

        if (owned <= 0) {
            return "None owned";
        }

        return fallbackReason;
    }

    private String formatBattleSummary(BattleSummary summary) {
        if (summary.getOutcome() == BattleSummary.Outcome.VICTORY) {
            return "Victory over " + summary.getOpponentName()
                    + "\nRewards: +" + summary.getGoldReward() + " gold, +"
                    + summary.getFameReward() + " fame"
                    + "\nRecord: " + summary.getWins() + "-" + summary.getLosses()
                    + "\nNext: " + summary.getNextStep();
        }

        String text = "Defeat";
        if (summary.isRunEnded()) {
            text += "\n" + summary.getNextStep();
        } else {
            text += "\nInjury: " + summary.getInjuryName()
                    + "\nFame penalty: " + summary.getFameReward()
                    + "\nLoss streak: " + summary.getLossStreak() + "/" + summary.getMaxLossStreak()
                    + "\nNext: " + summary.getNextStep();
        }

        return text + "\nRecord: " + summary.getWins() + "-" + summary.getLosses();
    }

    private String describeEnemy(Enemy enemy) {
        return enemy.getName()
                + "\nHP " + enemy.getHp() + "/" + enemy.getMaxHp()
                + " | STA " + enemy.getStamina() + "/" + enemy.getMaxStamina()
                + "\nSTR " + enemy.getStrength() + " | DEF " + enemy.getDefense()
                + "\nAbility: " + enemy.getAbility()
                + "\nReward: " + enemy.getGoldReward() + " gold, " + enemy.getFameReward() + " fame";
    }

    private String statChange(String label, int before, int after) {
        if (before == after) {
            return label + " " + after + " (no change)";
        }

        return label + " " + before + " -> " + after;
    }

    private FightContract createMeasuredBout() {
        return new FightContract("Measured Bout", "lower risk, steady pay", 90, 90, false);
    }

    private FightContract createBloodPrice() {
        return new FightContract("Blood Price", "harder crowd, better purse", 130, 125, false);
    }

    private FightContract createRivalChallenge() {
        return new FightContract("Rival Challenge", "face a named rival", 115, 140, true);
    }

    private FightContract createLanistaOrder() {
        return new FightContract("Lanista's Order", "compulsory bout", 100, 100, false);
    }

    private FightContract createChampionMatch() {
        return new FightContract("Champion Match", "fight for the arena crown", 100, 100, false);
    }

    private ArenaEvent rollArenaEvent() {
        int roll = random.nextInt(100);

        if (roll < 20) {
            return ArenaEvent.CROWD_FAVOR;
        } else if (roll < 35) {
            return ArenaEvent.SANDSTORM;
        } else if (roll < 50) {
            return ArenaEvent.NOBLE_SPONSOR;
        }

        return ArenaEvent.NONE;
    }

    private String gameOverText() {
        return "The run is over. Three consecutive losses ended this gladiator's story.";
    }
}
