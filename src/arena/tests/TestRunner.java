package arena.tests;

import arena.characters.Enemy;
import arena.characters.Player;
import arena.characters.Rival;
import arena.combat.PlayerAction;
import arena.contracts.FightContract;
import arena.enemies.ArenaRoster;
import arena.engine.BattleSummary;
import arena.engine.GameSession;
import arena.engine.ShopOffer;
import arena.events.ArenaEvent;
import arena.enums.EnemyAbility;
import arena.enums.GladiatorClass;
import arena.enums.InjuryType;
import arena.enums.Difficulty;
import arena.io.ConsoleInput;
import arena.items.Armor;
import arena.items.Weapon;
import arena.saves.SaveManager;
import arena.saves.SaveSlotInfo;
import arena.shop.Shop;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

/**
 * Lightweight automated checks for important rules that are easy to break
 * while changing the game.
 */
public class TestRunner {

    private int passed;

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        runner.run();
    }

    private void run() {
        testSaveLoadKeepsProgress();
        testSaveSlotsKeepSeparateGladiators();
        testLossStreakSurvivesAndResets();
        testCombatantStatLimits();
        testLevelingUsesExperience();
        testClassesHaveDifferentStartingStats();
        testConsoleInputSkipsInvalidValues();
        testGameSessionSupportsGuiActions();
        testGameSessionStartsGuiBattle();
        testPreFightPreviewDoesNotStartBattle();
        testStaggerBlocksSpecialAndWeakensEnemy();
        testDefendCanPrepareCounterattack();
        testChampionMatchUnlocksAtThreeHundredFame();
        testTutorialTipsAppearOnlyOnce();
        testChampionEndingCanContinueOrStartFresh();
        testChampionCanBuyFreedom();
        testGameSessionForcesArenaAfterIdleDays();
        testGameSessionStopsAfterThreeLosses();
        testGameSessionShopShowsNextUpgrade();
        testGameSessionShopExposesConsumables();
        testGameSessionExposesBattleSummaryAndEnemyInfo();
        testGameSessionAppliesCrowdFavorBonus();
        testGameSessionExposesSavePreviewAndBattleItems();
        testGameSessionForfeitCostsFameWithoutLossStreak();
        testPoisonCanBeCuredByAntidoteAndRest();
        testPlayerActionMapping();
        testRivalAbilityIsExplicitState();
        testShopTiersImproveWithFame();
        testArenaRosterCreatesOpponents();
        testCommonEnemyPoolsFollowFameTiers();
        testMeasuredBoutsDoNotCreateRivals();
        testRivalChallengeKeepsRivalHistory();
        testSaveVersionIsRecorded();
        testConsumablesAndInjuries();
        testContractsAndEventsModifyRewards();
        testCrowdFavorCanBeSpent();
        testTrainingAddsFatigueAndCapsStats();
        testBalanceSimulationStaysPlayable();
        testDifficultyScalingAndSave();
        testSaveSlotCanBeDeleted();

        System.out.println("Automated checks passed: " + passed);
    }

    private void testSaveLoadKeepsProgress() {
        SaveManager saveManager = new SaveManager("saves/test-save.txt");
        Player player = new Player("Testus", GladiatorClass.THRAEX);
        Rival titus = createRival("Titus the Butcher", EnemyAbility.BUTCHERS_CLEAVE);
        Rival cassius = createRival("Cassius the Giant", EnemyAbility.CRUSHING_BLOW);
        Rival redWolf = createRival("The Red Wolf", EnemyAbility.DOUBLE_STRIKE);
        Rival viper = createRival("Viper of the Sands", EnemyAbility.POISON_STRIKE);

        player.trainStrength();
        player.trainDefense();
        player.increaseMaxStamina(8);
        player.equipWeapon(new Weapon("Test Sica", 4));
        player.equipArmor(new Armor("Test Armor", 3));
        player.addCrowdFavor(7);
        player.applyInjury(InjuryType.SHAKEN);
        player.addHealingSalve();
        player.addStaminaDraught();
        player.addAntidote();
        player.addWhetstone();
        player.setDifficulty(Difficulty.MERCILESS);
        player.gainReward(75, 30);
        player.addLoss();
        player.addWin();
        player.addLoss();
        titus.addDefeatAgainstPlayer();
        titus.addEncounterAgainstPlayer();
        titus.addEncounterAgainstPlayer();
        titus.addVictoryAgainstPlayer();

        saveManager.saveGame(player, 9, titus, cassius, redWolf, viper, true, 7, true);
        SaveManager.GameSave loaded = saveManager.loadGame();

        require(loaded != null, "Expected save file to load");
        Player loadedPlayer = loaded.getPlayer();
        require(loaded.getDay() == 9, "Day should survive save/load");
        require(loaded.getTitusDefeats() == 1, "Rival rematch history should survive save/load");
        require(loaded.getRivalEncounters()[0] == 2, "Rival encounters should survive save/load");
        require(loaded.getRivalVictories()[0] == 1, "Rival victories should survive save/load");
        require(loaded.isChampionDefeated(), "Champion victory should survive save/load");
        require(loaded.getTitleDefenses() == 7, "Title defenses should survive save/load");
        require(loaded.isFreedomPurchased(), "Purchased freedom should survive save/load");
        require(loadedPlayer.getGladiatorClass() == GladiatorClass.THRAEX, "Class should survive save/load");
        require(loadedPlayer.getBaseStrength() == player.getBaseStrength(), "Base strength should survive save/load");
        require(loadedPlayer.getStrength() == player.getStrength(), "Equipped strength should survive save/load");
        require(loadedPlayer.getMaxStamina() == player.getMaxStamina(), "Max stamina should survive save/load");
        require(loadedPlayer.getWins() == 1, "Record should survive save/load");
        require(loadedPlayer.getLosses() == 2, "Loss record should survive save/load");
        require(loadedPlayer.getConsecutiveLosses() == 1, "Loss streak should survive save/load");
        require(loadedPlayer.getExperience() == player.getExperience(), "Experience should survive save/load");
        require(loaded.getSaveVersion() >= 2, "Save version should be recorded");
        require(loadedPlayer.getCrowdFavor() == player.getCrowdFavor(), "Crowd favor should survive save/load");
        require(loadedPlayer.getFatigue() == player.getFatigue(), "Fatigue should survive save/load");
        require(loadedPlayer.getDaysSinceFight() == player.getDaysSinceFight(), "Days since fight should survive save/load");
        require(loadedPlayer.getInjuryType() == InjuryType.SHAKEN, "Injury should survive save/load");
        require(loadedPlayer.getHealingSalves() == 1, "Salves should survive save/load");
        require(loadedPlayer.getStaminaDraughts() == 1, "Draughts should survive save/load");
        require(loadedPlayer.getAntidotes() == 1, "Antidotes should survive save/load");
        require(loadedPlayer.getWhetstones() == 1, "Whetstones should survive save/load");
        require(loadedPlayer.getDifficulty() == Difficulty.MERCILESS,
                "Difficulty should survive save/load");
        passed++;
    }

    private void testDifficultyScalingAndSave() {
        require(Difficulty.STANDARD.adjustIncomingDamage(10) == 10,
                "Standard damage must remain unchanged");
        require(Difficulty.STORY.adjustIncomingDamage(10) < 10,
                "Story should reduce incoming damage");
        require(Difficulty.MERCILESS.adjustIncomingDamage(10) > 10,
                "Merciless should increase incoming damage");
        require(Difficulty.STORY.adjustReward(10) > 10,
                "Story should increase rewards");
        require(Difficulty.MERCILESS.adjustReward(10) < 10,
                "Merciless should reduce rewards");
        Enemy standardEnemy = new Enemy("Standard", 100, 40, 20, 10, 10, 5,
                EnemyAbility.SHIELD_BASH);
        standardEnemy.applyDifficulty(Difficulty.STANDARD);
        require(standardEnemy.getMaxHp() == 100 && standardEnemy.getStrength() == 20
                        && standardEnemy.getDefense() == 10,
                "Standard must not change enemy stats");
        Enemy mercilessEnemy = new Enemy("Merciless", 100, 40, 20, 10, 10, 5,
                EnemyAbility.SHIELD_BASH);
        mercilessEnemy.applyDifficulty(Difficulty.MERCILESS);
        require(mercilessEnemy.getMaxHp() == 108 && mercilessEnemy.getStrength() == 21
                        && mercilessEnemy.getDefense() == 11,
                "Merciless should strengthen enemy HP, strength, and defense");
        require(Difficulty.MERCILESS.getEnemyAbilityChanceBonus() == 8,
                "Merciless should make enemy abilities more frequent");
        passed++;
    }

    private void testSaveSlotCanBeDeleted() {
        SaveManager manager = new SaveManager("saves/test-delete-legacy.txt", "saves/test-delete-slots");
        Player player = new Player("Deleteus", GladiatorClass.MURMILLO);
        Rival titus = createRival("Titus the Butcher", EnemyAbility.BUTCHERS_CLEAVE);
        Rival cassius = createRival("Cassius the Giant", EnemyAbility.CRUSHING_BLOW);
        Rival redWolf = createRival("The Red Wolf", EnemyAbility.DOUBLE_STRIKE);
        Rival viper = createRival("Viper of the Sands", EnemyAbility.POISON_STRIKE);

        manager.saveGame(2, player, 1, titus, cassius, redWolf, viper);
        require(manager.getSlotInfo(2).isLoadable(), "Saved slot should exist before deletion");
        require(manager.deleteGame(2), "Occupied slot should be deleted");
        require(!manager.getSlotInfo(2).isLoadable(), "Deleted slot should become empty");
        require(!manager.deleteGame(2), "Deleting an already empty slot should report no deletion");
        passed++;
    }

    private void testSaveSlotsKeepSeparateGladiators() {
        SaveManager saveManager = new SaveManager("saves/test-save.txt", "saves/test-slots");
        Rival titus = createRival("Titus the Butcher", EnemyAbility.BUTCHERS_CLEAVE);
        Rival cassius = createRival("Cassius the Giant", EnemyAbility.CRUSHING_BLOW);
        Rival redWolf = createRival("The Red Wolf", EnemyAbility.DOUBLE_STRIKE);
        Rival viper = createRival("Viper of the Sands", EnemyAbility.POISON_STRIKE);
        Player first = new Player("Firstus", GladiatorClass.MURMILLO);
        Player second = new Player("Seconda", GladiatorClass.RETIARIUS);

        first.gainReward(0, 50);
        second.gainReward(0, 150);

        saveManager.saveGame(1, first, 2, titus, cassius, redWolf, viper);
        saveManager.saveGame(2, second, 7, titus, cassius, redWolf, viper);

        SaveManager.GameSave firstSave = saveManager.loadGame(1);
        SaveManager.GameSave secondSave = saveManager.loadGame(2);

        require(firstSave != null, "First save slot should load");
        require(secondSave != null, "Second save slot should load");
        require(firstSave.getPlayer().getName().equals("Firstus"), "Slot 1 should keep the first gladiator");
        require(secondSave.getPlayer().getName().equals("Seconda"), "Slot 2 should keep the second gladiator");
        require(firstSave.getDay() == 2, "Slot 1 should keep its own day");
        require(secondSave.getDay() == 7, "Slot 2 should keep its own day");
        require(saveManager.describeSlot(1).contains("Firstus"), "Slot summary should show the saved name");
        passed++;
    }

    private void testLossStreakSurvivesAndResets() {
        Player player = new Player("Bruised", GladiatorClass.MURMILLO);

        player.takeDamage(999);
        player.addLoss();
        player.recoverFromArenaDefeat();

        require(player.isAlive(), "A first arena loss should be survivable");
        require(player.getConsecutiveLosses() == 1, "Loss streak should count a defeat");

        player.addLoss();
        player.addLoss();
        require(player.isBrokenByLosses(), "Three consecutive losses should break the gladiator");

        player.addWin();
        require(player.getConsecutiveLosses() == 0, "Winning should clear the loss streak");
        passed++;
    }

    private void testCombatantStatLimits() {
        Enemy enemy = new Enemy("Training Dummy", 10, 5, 1, 0, 0, 0);

        enemy.takeDamage(100);
        require(!enemy.isAlive(), "Damage should not leave enemy alive below zero HP");
        require(enemy.getHp() == 0, "HP should clamp at zero");

        enemy.restore();
        enemy.useStamina(100);
        require(enemy.getStamina() == 0, "Stamina should clamp at zero");

        enemy.recoverStamina(100);
        require(enemy.getStamina() == enemy.getMaxStamina(), "Stamina should clamp at max");
        passed++;
    }

    private void testLevelingUsesExperience() {
        Player player = new Player("Levelus", GladiatorClass.MURMILLO);
        int startingLevel = player.getLevel();
        int startingMaxHp = player.getMaxHp();

        player.gainExperience(player.getExperienceToNextLevel());

        require(player.getLevel() == startingLevel + 1, "Experience should increase level");
        require(player.getMaxHp() > startingMaxHp, "Leveling should improve max HP");
        require(player.getHp() == player.getMaxHp(), "Leveling should restore HP");
        passed++;
    }

    private void testConsoleInputSkipsInvalidValues() {
        ConsoleInput input = new ConsoleInput(new Scanner("wrong\n3\n"));

        require(input.readMenuChoice() == 3, "Input helper should skip non-numeric values");
        passed++;
    }

    private void testGameSessionSupportsGuiActions() {
        GameSession session = new GameSession();

        session.startNewGame("Guius", GladiatorClass.MURMILLO);
        int startingDay = session.getDay();
        String result = session.trainEndurance();

        require(session.hasPlayer(), "GUI session should create a player");
        require(session.getDay() == startingDay + 1, "GUI session actions should advance days");
        require(result.contains("Endurance"), "GUI session should return action text for the log");
        require(session.getStatsText().contains("Guius"), "GUI session should expose stats text");
        passed++;
    }

    private void testGameSessionStartsGuiBattle() {
        GameSession session = new GameSession();

        session.startNewGame("Battlus", GladiatorClass.THRAEX);
        String intro = session.startArenaFight();

        require(session.isInBattle(), "GUI session should start an arena battle");
        require(session.getCurrentEnemy() != null, "GUI battle should expose the active enemy");
        require(intro.contains("faces"), "GUI battle should return intro text");

        String result = session.battleAttack();

        require(result.contains("Round"), "GUI battle action should return round text");
        passed++;
    }

    private void testPreFightPreviewDoesNotStartBattle() {
        GameSession session = new GameSession();
        session.startNewGame("Scout", GladiatorClass.MURMILLO);

        session.prepareMeasuredBout();
        require(session.hasFightPreview(), "Choosing a contract should create a fight preview");
        require(!session.isInBattle(), "Previewing a fight should not start the battle");
        require(session.getPreFightInfo().getText().contains("Tactical hint:"),
                "Fight preview should include tactical advice");
        require(session.getPreFightInfo().getText().contains("Victory reward:"),
                "Fight preview should include estimated rewards");

        session.confirmFightPreview();
        require(session.isInBattle(), "Confirming the preview should start the selected fight");
        require(!session.hasFightPreview(), "Starting the fight should clear its preview");
        passed++;
    }

    private void testStaggerBlocksSpecialAndWeakensEnemy() {
        GameSession session = new GameSession();
        session.startNewGame("Tired", GladiatorClass.MURMILLO);
        session.startArenaFight();

        int playerTarget = session.getPlayer().getMaxStamina() / 4;
        session.getPlayer().useStamina(session.getPlayer().getStamina() - playerTarget);
        require(session.isPlayerStaggered(), "Player should stagger at 25% stamina");
        require(!session.getSpecialInfo().isEnabled(), "Stagger should block the player's special ability");
        require("Staggered".equals(session.getSpecialInfo().getDisabledReason()),
                "Disabled special should explain the stagger state");

        int enemyTarget = session.getCurrentEnemy().getMaxStamina() / 4;
        session.getCurrentEnemy().useStamina(session.getCurrentEnemy().getStamina() - enemyTarget);
        require(session.isEnemyStaggered(), "Enemy should stagger at 25% stamina");
        passed++;
    }

    private void testDefendCanPrepareCounterattack() {
        GameSession session = new GameSession();
        session.startNewGame("Guardius", GladiatorClass.MURMILLO);
        session.startArenaFight();
        setField(session, "currentEnemy", new Enemy("Practice Guard", 500, 100, 4, 2,
                0, 0, EnemyAbility.NONE));

        session.battleDefend();

        require(session.isCounterattackReady(), "Blocking a hit should prepare a counterattack");
        require("Counterattack".equals(session.getAttackInfo().getName()),
                "The attack command should show when the counter is ready");
        passed++;
    }

    private void testChampionMatchUnlocksAtThreeHundredFame() {
        GameSession session = new GameSession();
        session.startNewGame("Crowned", GladiatorClass.THRAEX);

        require(!session.isChampionMatchUnlocked(), "Champion Match should start locked");
        session.getPlayer().gainReward(0, 300);
        require(session.isChampionMatchUnlocked(), "Champion Match should unlock at 300 fame");
        session.prepareChampionMatch();
        require(session.hasFightPreview(), "Unlocked Champion Match should have a preview");
        require(session.getPreFightInfo().getText().contains("Aurelius the Unbroken"),
                "Champion preview should name the final opponent");
        passed++;
    }

    private void testTutorialTipsAppearOnlyOnce() {
        GameSession session = new GameSession();
        session.startNewGame("Novice", GladiatorClass.RETIARIUS);

        require(session.consumeMainTutorialTip().contains("300 Fame"),
                "New career should explain its main objective");
        require(session.consumeMainTutorialTip().isEmpty(),
                "Main tutorial tip should not repeat");
        require(session.consumeArenaTutorialTip().contains("Forfeit"),
                "Arena tutorial should explain contracts and forfeit");
        require(session.consumeArenaTutorialTip().isEmpty(),
                "Arena tutorial tip should not repeat");
        require(session.consumeTrainingTutorialTip().contains("Fatigue"),
                "Training tutorial should explain fatigue and rest");
        require(session.consumeTrainingTutorialTip().isEmpty(),
                "Training tutorial tip should not repeat");
        passed++;
    }

    private void testChampionEndingCanContinueOrStartFresh() {
        GameSession session = new GameSession();
        session.startNewGame("Victor", GladiatorClass.MURMILLO);
        setField(session, "championDefeated", true);
        setField(session, "lastBattleSummary", new BattleSummary(
                BattleSummary.Outcome.VICTORY, "Aurelius the Unbroken", 100, 50,
                "", 0, 3, 20, 2, false, "The arena crown is yours."));

        require(session.hasChampionEnding(), "Champion victory should open the ending");
        require(session.getChampionEndingText().contains("Final record:"),
                "Ending should summarize the career record");
        session.continueAsChampion();
        require(session.hasPlayer() && !session.hasChampionEnding(),
                "Continue as Champion should preserve the career and close the ending");
        require(session.isChampionDefeated(), "Continuing should preserve the arena crown");

        session.returnToNewCareer();
        require(!session.hasPlayer(), "New Career should return to character creation");
        passed++;
    }

    private void testChampionCanBuyFreedom() {
        GameSession session = new GameSession();
        session.startNewGame("Liberatus", GladiatorClass.THRAEX);
        setField(session, "championDefeated", true);
        session.getPlayer().gainReward(GameSession.FREEDOM_PRICE, 0);
        int goldBefore = session.getPlayer().getGold();

        require(session.canBuyFreedom(), "Champion with enough gold should be able to buy freedom");
        require(session.buyFreedom().contains("freedom"), "Freedom purchase should return a clear result");
        require(session.isFreedomPurchased(), "Freedom purchase should update career state");
        require(session.getPlayer().getGold() == goldBefore - GameSession.FREEDOM_PRICE,
                "Freedom purchase should spend its exact gold price");
        require(session.shouldShowFreedomEnding(), "Freedom purchase should open its ending once");
        session.acknowledgeFreedomEnding();
        require(!session.shouldShowFreedomEnding(), "Acknowledged freedom ending should not repeat");
        passed++;
    }

    private void testGameSessionForcesArenaAfterIdleDays() {
        GameSession session = new GameSession();

        session.startNewGame("Idleus", GladiatorClass.MURMILLO);
        session.restOneDay();
        session.restOneDay();
        session.restOneDay();
        session.restOneDay();
        String result = session.trainEndurance();

        require(session.isInBattle(), "GUI session should force an arena fight after too many idle days");
        require(result.contains("lanista"), "Forced fight should explain lanista pressure");
        passed++;
    }

    private void testGameSessionStopsAfterThreeLosses() {
        GameSession session = new GameSession();

        session.startNewGame("Loserus", GladiatorClass.MURMILLO);
        session.getPlayer().addLoss();
        session.getPlayer().addLoss();
        session.getPlayer().addLoss();
        session.startArenaFight();

        require(session.isGameOver(), "GUI session should treat three losses as game over");
        require(!session.isInBattle(), "Game over should not allow a new battle");
        require(session.trainEndurance().contains("run is over"), "Game over should block campaign actions");
        passed++;
    }

    private void testGameSessionShopShowsNextUpgrade() {
        GameSession session = new GameSession();

        session.startNewGame("Buyerus", GladiatorClass.MURMILLO);
        session.getPlayer().gainReward(100, 0);

        require(session.getWeaponOffer().getName().equals("Bronze Gladius"),
                "First GUI weapon offer should be Bronze");

        session.buyWeapon();

        require(session.getWeaponOffer().getName().equals("Iron Gladius"),
                "After buying Bronze, GUI shop should show Iron as the next weapon");
        require(session.getWeaponOffer().getStatus() == ShopOffer.Status.LOCKED,
                "Locked next weapon should show its fame requirement");
        require(session.getWeaponOffer().getFameRequirement() == 50,
                "Locked next weapon should keep its fame requirement as data");
        passed++;
    }

    private void testGameSessionShopExposesConsumables() {
        GameSession session = new GameSession();

        session.startNewGame("Marketus", GladiatorClass.RETIARIUS);
        session.getPlayer().gainReward(100, 0);

        require(session.getHealingSalveOffer().getOwned() == 0,
                "Healing Salve offer should expose owned count even at zero");
        require(session.getStaminaDraughtOffer().isBuyable(),
                "Stamina Draught should be buyable from the GUI shop");
        require(session.getAntidoteOffer().isBuyable(),
                "Antidote should be buyable from the GUI shop");
        require(session.getWhetstoneOffer().isBuyable(),
                "Whetstone should be buyable from the GUI shop");

        session.buyStaminaDraught();
        session.buyAntidote();
        session.buyWhetstone();

        require(session.getPlayer().getStaminaDraughts() == 1,
                "Buying a Stamina Draught through GameSession should add it to inventory");
        require(session.getPlayer().getAntidotes() == 1,
                "Buying an Antidote through GameSession should add it to inventory");
        require(session.getPlayer().getWhetstones() == 1,
                "Buying a Whetstone through GameSession should add it to inventory");
        passed++;
    }

    private void testGameSessionExposesBattleSummaryAndEnemyInfo() {
        GameSession session = new GameSession();

        session.startNewGame("Reporter", GladiatorClass.DIMACHAERUS);
        session.getPlayer().equipWeapon(new Weapon("Reporter Blades", 50));
        String intro = session.startMeasuredBout();

        require(session.getEnemyInfoText().contains("Reward"),
                "GUI session should expose active enemy rewards");
        require(intro.contains("Arena Event"), "GUI fight intro should include arena event text");

        int guard = 0;
        while (session.isInBattle() && guard < 120) {
            if (session.getAttackInfo().isEnabled()) {
                session.battleAttack();
            } else {
                session.battleRest();
            }
            guard++;
        }

        require(session.hasBattleSummary(), "Victory should create a battle summary");
        require(session.getLastBattleSummaryInfo().getOutcome() == BattleSummary.Outcome.VICTORY,
                "Battle summary should expose victory as structured data");
        require(session.getLastBattleSummary().contains("Record"),
                "Battle summary should include the current record");

        session.clearBattleSummary();
        require(!session.hasBattleSummary(), "Battle summary should be clearable");
        passed++;
    }

    private void testGameSessionAppliesCrowdFavorBonus() {
        GameSession session = new GameSession();

        session.startNewGame("Favored", GladiatorClass.DIMACHAERUS);
        session.getPlayer().equipWeapon(new Weapon("Test Blades", 50));
        session.getPlayer().addCrowdFavor(20);
        session.startMeasuredBout();

        int guard = 0;
        while (session.isInBattle() && guard < 120) {
            if (session.getHeavyAttackInfo().isEnabled()) {
                session.battleHeavyAttack();
            } else {
                session.battleRest();
            }
            guard++;
        }

        require(session.hasBattleSummary(), "Winning with stored favor should still produce a battle summary");
        require(session.getPlayer().getCrowdFavor() < 20,
                "GUI victory flow should spend crowd favor when the bonus triggers");
        require(session.getLastBattleSummaryInfo().getGoldReward() >= 15,
                "Crowd favor bonus should be included in structured victory rewards");
        passed++;
    }

    private void testGameSessionExposesSavePreviewAndBattleItems() {
        GameSession session = new GameSession();

        session.startNewGame("Itemus", GladiatorClass.MURMILLO);
        require(session.getSaveSlotText(1).startsWith("Slot 1"),
                "GUI session should expose save slot previews");
        SaveSlotInfo slotInfo = session.getSaveSlotInfo(1);
        require(slotInfo.getSlot() == 1, "GUI save slot info should expose the slot number");

        session.getPlayer().addHealingSalve();
        session.getPlayer().addStaminaDraught();
        session.getPlayer().takeDamage(10);
        session.getPlayer().useStamina(10);
        session.startMeasuredBout();

        require(session.getHealingSalveInfo().isEnabled(),
                "Healing salve should be available in battle when hurt");
        require(session.getStaminaDraughtInfo().isEnabled(),
                "Stamina draught should be available in battle when stamina is missing");
        require(session.getHealingSalveInfo().getDetail().contains("owned"),
                "Item battle text should show owned count");
        passed++;
    }

    private void testGameSessionForfeitCostsFameWithoutLossStreak() {
        GameSession session = new GameSession();

        session.startNewGame("Yieldus", GladiatorClass.MURMILLO);
        session.getPlayer().gainReward(0, 25);
        session.startMeasuredBout();
        String result = session.battleForfeit();

        require(result.contains("Forfeit"), "Forfeit should report the match was yielded");
        require(!session.isInBattle(), "Forfeit should end the active battle");
        require(session.getPlayer().getFame() == 15, "Forfeit should subtract fame");
        require(session.getPlayer().getConsecutiveLosses() == 0,
                "Forfeit should not count as a knockout loss streak");
        require(session.hasBattleSummary(), "Forfeit should create a battle summary");
        passed++;
    }

    private void testPoisonCanBeCuredByAntidoteAndRest() {
        GameSession session = new GameSession();

        session.startNewGame("Curius", GladiatorClass.MURMILLO);
        session.startMeasuredBout();
        setField(session, "currentEnemy", new Enemy("Test Striker", 50, 30, 1, 1, 1, 1));
        setField(session, "playerPoisoned", true);
        session.getPlayer().addAntidote();

        session.battleUseAntidote();
        require(!session.isPlayerPoisoned(), "Antidote should cure poison during battle");

        session.battleForfeit();
        setField(session, "playerPoisoned", true);
        session.restOneDay();
        require(!session.isPlayerPoisoned(), "Rest should clear poison after battle");
        passed++;
    }

    private void testPlayerActionMapping() {
        require(PlayerAction.fromMenuChoice(1) == PlayerAction.ATTACK, "Choice 1 should be Attack");
        require(PlayerAction.fromMenuChoice(3) == PlayerAction.SPECIAL_ABILITY, "Choice 3 should be Special");
        require(PlayerAction.fromMenuChoice(4) == PlayerAction.DEFEND, "Choice 4 should be Defend");
        require(PlayerAction.fromMenuChoice(5) == PlayerAction.REST, "Choice 5 should be Rest");
        require(PlayerAction.fromMenuChoice(6) == PlayerAction.USE_ITEM, "Choice 6 should be Item");
        require(PlayerAction.fromMenuChoice(99) == PlayerAction.INVALID, "Unknown choices should be invalid");
        passed++;
    }

    private void testClassesHaveDifferentStartingStats() {
        Player murmillo = new Player("Guard", GladiatorClass.MURMILLO);
        Player retiarius = new Player("Net", GladiatorClass.RETIARIUS);
        Player dimachaerus = new Player("Blade", GladiatorClass.DIMACHAERUS);

        require(murmillo.getMaxHp() > retiarius.getMaxHp(), "Murmillo should be tougher than Retiarius");
        require(retiarius.getMaxStamina() > murmillo.getMaxStamina(), "Retiarius should have more stamina");
        require(dimachaerus.getBaseStrength() > retiarius.getBaseStrength(), "Dimachaerus should start stronger");
        passed++;
    }

    private void testRivalAbilityIsExplicitState() {
        Rival rival = createRival("Any Name", EnemyAbility.POISON_STRIKE);

        require(rival.getAbility() == EnemyAbility.POISON_STRIKE, "Rival ability should not depend on name text");
        passed++;
    }

    private void testShopTiersImproveWithFame() {
        Shop shop = new Shop();
        Player player = new Player("Buyer", GladiatorClass.MURMILLO);
        int startingBonus = shop.createClassWeapon(player).getStrengthBonus();

        player.setFame(150);

        require(shop.createClassWeapon(player).getStrengthBonus() > startingBonus,
                "Higher fame should unlock stronger weapons");
        require(shop.getArmorItem(player).getPrice() > 30,
                "Higher fame armor should cost more than starter armor");
        passed++;
    }

    private void testSaveVersionIsRecorded() {
        SaveManager.GameSave save = new SaveManager.GameSave(
                new Player("Versioned", GladiatorClass.THRAEX),
                2,
                1,
                0,
                0,
                0,
                0);

        require(save.getSaveVersion() == 2, "GameSave should expose save version");
        passed++;
    }

    private void testArenaRosterCreatesOpponents() {
        ArenaRoster roster = new ArenaRoster(new Random(1));
        Player player = new Player("Roster", GladiatorClass.MURMILLO);
        Enemy opponent = roster.createArenaOpponent(1, player);
        Enemy champion = roster.createChampion();

        require(opponent != null, "Roster should create arena opponents");
        require(champion.getName().equals("Aurelius the Unbroken"), "Roster should create the champion");
        passed++;
    }

    private void testCommonEnemyPoolsFollowFameTiers() {
        ArenaRoster roster = new ArenaRoster(new Random(17));
        Player player = new Player("Tiered", GladiatorClass.MURMILLO);

        Set<String> early = collectCommonEnemyNames(roster, player, 120);
        require(early.contains("Nervous Shieldbearer"), "Early pool should include Nervous Shieldbearer");
        require(early.contains("Dust Runner"), "Early pool should include Dust Runner");
        require(early.contains("Arena Brawler"), "Early pool should include Arena Brawler");
        require(!early.contains("The Hollow Helm"), "Elite enemy should not appear in the early pool");

        player.gainReward(0, 60);
        Set<String> middle = collectCommonEnemyNames(roster, player, 160);
        require(middle.contains("Hookblade Duelist"), "Middle pool should include Hookblade Duelist");
        require(middle.contains("Iron Netter"), "Middle pool should include Iron Netter");
        require(!middle.contains("The Hollow Helm"), "Elite enemy should not appear before veteran fame");

        player.gainReward(0, 100);
        Set<String> veteran = collectCommonEnemyNames(roster, player, 140);
        require(veteran.contains("The Hollow Helm"), "Veteran pool should include The Hollow Helm");
        passed++;
    }

    private Set<String> collectCommonEnemyNames(ArenaRoster roster, Player player, int fights) {
        Set<String> names = new HashSet<String>();
        for (int i = 0; i < fights; i++) {
            Enemy enemy = roster.createArenaOpponent(5, player, false);
            require(!(enemy instanceof Rival), "Common enemy pool should never produce a rival");
            require(enemy.getAbility() != EnemyAbility.NONE, "Every common enemy should have an ability");
            names.add(enemy.getName());
        }
        return names;
    }

    private void testMeasuredBoutsDoNotCreateRivals() {
        ArenaRoster roster = new ArenaRoster(new Random(1));
        Player player = new Player("Measured", GladiatorClass.MURMILLO);

        for (int day = 1; day <= 12; day++) {
            Enemy opponent = roster.createArenaOpponent(day, player, false);
            require(!(opponent instanceof Rival), "Measured bouts should create common enemies, not rivals");
            require(opponent.getAbility() != EnemyAbility.NONE,
                    "Common enemies should have battle abilities in GUI fights");
        }

        passed++;
    }

    private void testRivalChallengeKeepsRivalHistory() {
        ArenaRoster roster = new ArenaRoster(new Random(1));
        Player player = new Player("Rivaled", GladiatorClass.MURMILLO);
        Enemy opponent = roster.createArenaOpponent(1, player, true);

        require(opponent instanceof Rival, "Forced rival challenge should create a Rival");

        Rival rival = (Rival) opponent;
        String firstQuote = rival.getEncounterQuote();
        roster.recordRivalEncounter(rival);
        roster.recordRivalVictory(rival);
        roster.recordRivalDefeat(rival);
        require(firstQuote.equals(rival.getEncounterQuote()),
                "The first encounter should use the rival's opening introduction");
        roster.recordRivalEncounter(rival);

        require(getRememberedDefeats(roster, rival.getName()) == 1,
                "Rival defeat should be recorded on the roster");
        require(rival.getEncountersAgainstPlayer() == 2,
                "Rival encounters should be recorded on the active opponent");
        require(rival.getVictoriesAgainstPlayer() == 1,
                "Rival victory should be recorded on the active opponent");
        require(!firstQuote.equals(rival.getEncounterQuote()),
                "A rival rematch should use a different introduction");
        passed++;
    }

    private int getRememberedDefeats(ArenaRoster roster, String rivalName) {
        if (roster.getTitus().getName().equals(rivalName)) {
            return roster.getTitus().getDefeatsAgainstPlayer();
        } else if (roster.getCassius().getName().equals(rivalName)) {
            return roster.getCassius().getDefeatsAgainstPlayer();
        } else if (roster.getRedWolf().getName().equals(rivalName)) {
            return roster.getRedWolf().getDefeatsAgainstPlayer();
        } else if (roster.getViper().getName().equals(rivalName)) {
            return roster.getViper().getDefeatsAgainstPlayer();
        }

        return 0;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not set test field: " + fieldName, exception);
        }
    }

    private void testConsumablesAndInjuries() {
        Player player = new Player("Prepared", GladiatorClass.DIMACHAERUS);
        int strength = player.getStrength();

        player.addHealingSalve();
        player.addStaminaDraught();
        player.addAntidote();
        player.addWhetstone();
        player.takeDamage(20);

        require(player.useHealingSalve(), "Healing salve should be usable");
        require(player.useStaminaDraught(), "Stamina draught should be usable");
        require(player.useAntidote(), "Antidote should be usable");
        require(player.useWhetstone(), "Whetstone should be usable");
        require(player.getStrength() > strength, "Whetstone should improve strength");

        player.applyInjury(InjuryType.WOUNDED_ARM);
        require(player.getInjuryType() == InjuryType.WOUNDED_ARM, "Injury should be tracked");
        player.fullRest();
        require(player.getInjuryType() == InjuryType.NONE, "Rest should clear injuries");
        passed++;
    }

    private void testContractsAndEventsModifyRewards() {
        FightContract contract = new FightContract("Test", "test", 150, 120, true);
        Player player = new Player("Crowd", GladiatorClass.THRAEX);

        ArenaEvent.CROWD_FAVOR.announce(player);

        require(contract.applyGold(10) == 15, "Contract should modify gold");
        require(contract.applyFame(10) == 12, "Contract should modify fame");
        require(contract.isRivalChallenge(), "Contract should preserve rival flag");
        require(player.getCrowdFavor() > 0, "Crowd event should add favor");
        passed++;
    }

    private void testCrowdFavorCanBeSpent() {
        Player player = new Player("Popular", GladiatorClass.MURMILLO);

        player.addCrowdFavor(20);

        require(player.spendCrowdFavor(20), "Crowd favor should be spendable");
        require(player.getCrowdFavor() == 0, "Spent crowd favor should be removed");
        passed++;
    }

    private void testTrainingAddsFatigueAndCapsStats() {
        Player player = new Player("Trainee", GladiatorClass.RETIARIUS);

        player.trainStrength();
        require(player.getFatigue() > 0, "Training should add fatigue");

        for (int i = 0; i < 20; i++) {
            player.trainStrength();
        }

        require(player.getBaseStrength() <= 20, "Pit Fighter strength training should cap at 20");

        int fatigueBeforeRest = player.getFatigue();
        player.fullRest();
        require(player.getFatigue() < fatigueBeforeRest, "Rest should reduce fatigue");
        passed++;
    }

    private void testBalanceSimulationStaysPlayable() {
        BalanceSimulator.Summary summary = BalanceSimulator.runQuietly(40, 3L);

        require(summary.getChampionWins() > 0, "Some simulated careers should beat the champion");
        require(summary.getDeathRate() < 95.0, "Balance should not kill nearly every simulated career");
        require(summary.getAverageFame() >= 40.0, "Simulated careers should make meaningful fame progress");
        passed++;
    }

    private Rival createRival(String name, EnemyAbility ability) {
        return new Rival(name, 80, 40, 10, 3, 25, 8, ability,
                "You again? This time I will break you.");
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
