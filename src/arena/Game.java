package arena;

import arena.characters.Enemy;
import arena.characters.Player;
import arena.characters.Rival;
import arena.combat.Battle;
import arena.contracts.FightContract;
import arena.enemies.ArenaRoster;
import arena.events.ArenaEvent;
import arena.enums.EnemyAbility;
import arena.enums.GladiatorClass;
import arena.enums.InjuryType;
import arena.io.ConsoleInput;
import arena.saves.SaveManager;
import arena.shop.Shop;
import arena.shop.ShopItem;

import java.util.Random;
import java.util.Scanner;

/**
 * Coordinates the overall game flow: player creation, menus, training, shop
 * visits, arena fights, champion fights, and saving or loading progress.
 */
public class Game {

    private final Random random = new Random();
    private final Scanner scanner = new Scanner(System.in);
    private final ConsoleInput input = new ConsoleInput(scanner);
    private final SaveManager saveManager = new SaveManager();
    private final Shop shop = new Shop();
    private final ArenaRoster arenaRoster = new ArenaRoster(random);

    private Player player;
    private int day = 1;
    private boolean gameWon = false;
    private boolean compactBattleText = false;
    private int lastTrainingChoice = -1;
    private int lastShopChoice = -1;

    public void start() {
        showIntro();
        showTitleMenu();
    }

    private void showIntro() {
        System.out.println("=================================");
        System.out.println(" THE UNBROKEN");
        System.out.println(" Gladiator's Rise");
        System.out.println("=================================\n");
    }

    private void showTitleMenu() {
        boolean choosing = true;

        while (choosing) {
            System.out.println("===== MAIN MENU =====");
            System.out.println("1. New Game");
            System.out.println("2. Load Game");
            System.out.println("3. Exit");
            System.out.print("> ");

            switch (input.readMenuChoice()) {
                case 1:
                    startNewGame();
                    gameLoop();
                    choosing = false;
                    break;
                case 2:
                    if (loadGameFromMenu()) {
                        gameLoop();
                        choosing = false;
                    }
                    break;
                case 3:
                    System.out.println("The arena waits for your return.");
                    choosing = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    private void startNewGame() {
        day = 1;
        gameWon = false;
        arenaRoster.resetRivals();
        createPlayer();
        runOpeningFight();
    }

    private void createPlayer() {
        System.out.print("Enter your gladiator's name: ");
        String name = input.readLine();

        GladiatorClass gladiatorClass = chooseGladiatorClass();

        player = new Player(name, gladiatorClass);

        System.out.println("\nWelcome, " + player.getName() + ".");
        System.out.println("You awaken in chains beneath the arena.");
        System.out.println("The lanista wants proof you can survive the school.");
    }

    private void runOpeningFight() {
        Enemy sparringSlave = new Enemy("Doros, a frightened slave", 80, 35, 5, 1, 0, 0, EnemyAbility.NONE);

        System.out.println("\n===== PROLOGUE: FIRST BLOOD =====");
        System.out.println("Before you see the arena, guards drag you into a sand-streaked training pit.");
        System.out.println("Across from you stands another slave clutching a practice blade.");

        Battle tutorial = new Battle(input);
        boolean wonLesson = tutorial.startTutorial(player, sparringSlave);

        player.restore();

        if (wonLesson) {
            System.out.println("\nThe lanista gives a thin nod.");
            System.out.println("\"Good. Tomorrow, you begin earning your keep.\"");
        } else {
            System.out.println("\nThe lanista signals for the beating to stop.");
            System.out.println("\"Pathetic, but alive. Learn quickly, or the arena will teach you once.\"");
        }

        System.out.println("Only after the pit fight does the routine of the ludus begin.");
    }

    private GladiatorClass chooseGladiatorClass() {
        System.out.println("\nChoose your gladiator class:");
        System.out.println("1. Murmillo - Sword and shield");
        System.out.println("2. Retiarius - Trident and net");
        System.out.println("3. Dimachaerus - Dual swords");
        System.out.println("4. Thraex - Curved sword and shield");
        System.out.print("> ");

        int choice = input.readMenuChoice();

        switch (choice) {
            case 1:
                return GladiatorClass.MURMILLO;
            case 2:
                return GladiatorClass.RETIARIUS;
            case 3:
                return GladiatorClass.DIMACHAERUS;
            case 4:
                return GladiatorClass.THRAEX;
            default:
                System.out.println("Invalid choice. Defaulting to Murmillo.");
                return GladiatorClass.MURMILLO;
        }
    }

    private void gameLoop() {
        boolean running = true;

        while (running && player.isAlive() && !gameWon) {
            if (player.getDaysSinceFight() >= 4) {
                forceLanistaFight();
                continue;
            }

            showMainMenu();

            int choice = input.readMenuChoice();

            if (player.getFame() >= 300) {
                running = handleChampionMenu(choice, running);
            } else {
                running = handleNormalMenu(choice, running);
            }
        }

        if (!player.isAlive()) {
            showDefeatEnding();
        } else if (gameWon) {
            showVictoryEnding();
        } else {
            System.out.println("\nThe arena waits for your return.");
        }
    }

    private void showDefeatEnding() {
        System.out.println("\n===== GAME OVER =====");
        System.out.println(player.getName() + " falls beneath the roar of the crowd.");
        System.out.println("Final Day: " + day);
        System.out.println("Final Rank: " + player.getRank());
        System.out.println("Record: " + player.getWins() + "-" + player.getLosses());
        System.out.println("Your story ends in the sand.");
    }

    private void showVictoryEnding() {
        System.out.println("\n===== LEGEND COMPLETE =====");
        System.out.println("Final Day: " + day);
        System.out.println("Final Record: " + player.getWins() + "-" + player.getLosses());
        System.out.println("Final Fame: " + player.getFame());
        System.out.println("The chains are broken. The name remains.");
    }

    private void showMainMenu() {
        System.out.println("\n========== DAY " + day + " ==========");
        System.out.println("1. View Gladiator");
        System.out.println("2. Training Grounds");
        System.out.println("3. Enter Arena");

        if (player.getFame() >= 300) {
            System.out.println("4. Champion Match");
            System.out.println("5. Visit Shop");
            System.out.println("6. Rest");
            System.out.println("7. Save Game");
            System.out.println("8. Load Game");
            System.out.println("9. Settings");
            System.out.println("10. Exit Game");
        } else {
            System.out.println("4. Visit Shop");
            System.out.println("5. Rest");
            System.out.println("6. Save Game");
            System.out.println("7. Load Game");
            System.out.println("8. Settings");
            System.out.println("9. Exit Game");
        }

        System.out.print("> ");
    }

    private boolean handleNormalMenu(int choice, boolean running) {
        switch (choice) {
            case 1:
                player.showStats();
                break;
            case 2:
                trainingGrounds();
                break;
            case 3:
                enterArena();
                nextDay();
                break;
            case 4:
                visitShop();
                break;
            case 5:
                restMenu();
                break;
            case 6:
                saveGameToMenuSlot();
                break;
            case 7:
                loadGameFromMenu();
                break;
            case 8:
                settingsMenu();
                break;
            case 9:
                running = false;
                System.out.println("You leave the game.");
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }

        return running;
    }

    private boolean handleChampionMenu(int choice, boolean running) {
        switch (choice) {
            case 1:
                player.showStats();
                break;
            case 2:
                trainingGrounds();
                break;
            case 3:
                enterArena();
                nextDay();
                break;
            case 4:
                championMatch();
                nextDay();
                break;
            case 5:
                visitShop();
                break;
            case 6:
                restMenu();
                break;
            case 7:
                saveGameToMenuSlot();
                break;
            case 8:
                loadGameFromMenu();
                break;
            case 9:
                settingsMenu();
                break;
            case 10:
                running = false;
                System.out.println("You leave the game.");
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }

        return running;
    }

    private void trainingGrounds() {
        System.out.println("\n===== TRAINING GROUNDS =====");
        System.out.println("1. Safe drills (Strength + Defense)");
        System.out.println("2. Brutal conditioning (+2 Strength, -10 HP)");
        System.out.println("3. Endurance laps (+5 Max Stamina)");
        System.out.println("4. Public sparring (+4 Crowd Favor, chance of injury)");
        if (lastTrainingChoice >= 1) {
            System.out.println("5. Repeat last drill");
        }
        System.out.print("> ");

        int choice = input.readMenuChoice();
        if (choice == 5 && lastTrainingChoice >= 1) {
            choice = lastTrainingChoice;
        }

        switch (choice) {
            case 1:
                player.trainStrength();
                player.trainDefense();
                break;
            case 2:
                player.trainStrength();
                player.takeDamage(10);
                player.addFatigue(8);
                System.out.println("The brutal session leaves bruises. -10 HP");
                break;
            case 3:
                player.trainStamina();
                break;
            case 4:
                player.addCrowdFavor(4);
                if (random.nextInt(100) < 25) {
                    player.applyInjury(InjuryType.SHAKEN);
                } else {
                    player.gainExperience(12);
                    System.out.println("The crowd enjoys the sparring. +12 XP");
                }
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        lastTrainingChoice = choice;
        applyFatigueRisk();
        player.recordNonFightDay();
        nextDay();
    }

    private void visitShop() {
        ShopItem weaponItem = shop.getWeaponItem(player);
        ShopItem armorItem = shop.getArmorItem(player);
        ShopItem staminaMeal = shop.getStaminaMealItem();
        ShopItem healingSalve = shop.getHealingSalveItem();
        ShopItem staminaDraught = shop.getStaminaDraughtItem();
        ShopItem antidote = shop.getAntidoteItem();
        ShopItem whetstone = shop.getWhetstoneItem();

        System.out.println("\n===== ARENA MARKET =====");
        System.out.println("Gold: " + player.getGold());
        System.out.println("1. " + weaponItem.getMenuText());
        System.out.println("2. " + armorItem.getMenuText());
        System.out.println("3. " + staminaMeal.getMenuText());
        System.out.println("4. " + healingSalve.getMenuText());
        System.out.println("5. " + staminaDraught.getMenuText());
        System.out.println("6. " + antidote.getMenuText());
        System.out.println("7. " + whetstone.getMenuText());
        if (lastShopChoice >= 1) {
            System.out.println("8. Buy last item again");
            System.out.println("9. Leave Shop");
        } else {
            System.out.println("8. Leave Shop");
        }
        System.out.print("> ");

        int choice = input.readMenuChoice();
        if (choice == 8 && lastShopChoice >= 1) {
            choice = lastShopChoice;
        } else if (choice == 9 && lastShopChoice >= 1) {
            choice = 8;
        }

        switch (choice) {
            case 1:
                if (shop.createClassWeapon(player).getStrengthBonus() <= player.getWeapon().getStrengthBonus()) {
                    System.out.println("You already own a weapon of this quality.");
                    break;
                }

                if (player.spendGold(weaponItem.getPrice())) {
                    player.equipWeapon(shop.createClassWeapon(player));
                    lastShopChoice = choice;
                    player.recordNonFightDay();
                    nextDay();
                }
                break;

            case 2:
                if (shop.createRankArmor(player).getDefenseBonus() <= player.getArmor().getDefenseBonus()) {
                    System.out.println("You already own armor of this quality.");
                    break;
                }

                if (player.spendGold(armorItem.getPrice())) {
                    player.equipArmor(shop.createRankArmor(player));
                    lastShopChoice = choice;
                    player.recordNonFightDay();
                    nextDay();
                }
                break;

            case 3:
                if (player.spendGold(staminaMeal.getPrice())) {
                    player.increaseMaxStamina(shop.getStaminaMealBonus());
                    System.out.println("You ate a rich stamina meal.");
                    lastShopChoice = choice;
                    player.recordNonFightDay();
                    nextDay();
                }
                break;

            case 4:
                if (player.spendGold(healingSalve.getPrice())) {
                    player.addHealingSalve();
                    lastShopChoice = choice;
                }
                break;
            case 5:
                if (player.spendGold(staminaDraught.getPrice())) {
                    player.addStaminaDraught();
                    lastShopChoice = choice;
                }
                break;
            case 6:
                if (player.spendGold(antidote.getPrice())) {
                    player.addAntidote();
                    lastShopChoice = choice;
                }
                break;
            case 7:
                if (player.spendGold(whetstone.getPrice())) {
                    player.addWhetstone();
                    lastShopChoice = choice;
                }
                break;
            case 8:
                System.out.println("You leave the market.");
                break;

            default:
                System.out.println("Invalid choice.");
                break;
        }
    }

    private void enterArena() {
        FightContract contract = chooseFightContract();
        enterArena(contract);
    }

    private void enterArena(FightContract contract) {
        Enemy enemy = arenaRoster.createArenaOpponent(day, player, contract.isRivalChallenge());
        enemy.restore();
        ArenaEvent event = rollArenaEvent();
        event.announce(player);

        if (enemy instanceof Rival) {
            ((Rival) enemy).showIntro();
        } else {
            System.out.println("\n" + enemy.getName() + " steps into the arena.");
        }

        Battle battle = new Battle(input, compactBattleText);
        boolean victory = battle.start(player, enemy);

        if (victory) {
            player.addWin();
            player.recordFightDay();
            int goldReward = event.applyGold(contract.applyGold(enemy.getGoldReward()));
            int fameReward = event.applyFame(contract.applyFame(enemy.getFameReward()));
            if (player.getFame() < 50) {
                fameReward += 3;
            }
            if (enemy instanceof Rival) {
                fameReward += getRivalBonusFame((Rival) enemy);
            }
            player.gainReward(goldReward, fameReward);

            if (enemy instanceof Rival) {
                arenaRoster.recordRivalDefeat((Rival) enemy);
            }

            System.out.println("The crowd chants your name.");
            player.addCrowdFavor(3);
            if (player.getHp() * 100 / player.getMaxHp() <= 25) {
                System.out.println("The crowd roars for the narrow survival.");
                player.addCrowdFavor(5);
            }
            if (player.spendCrowdFavor(20)) {
                System.out.println("The crowd hurls coins and praise into the sand.");
                goldReward += 15;
                fameReward += 5;
                player.gainReward(15, 5);
            }
            System.out.println("+" + goldReward + " Gold");
            System.out.println("+" + fameReward + " Fame");
            System.out.println("Current Rank: " + player.getRank());
        } else {
            handleArenaLoss();
        }
    }

    private FightContract chooseFightContract() {
        FightContract safe = new FightContract("Measured Bout", "lower risk, steady pay", 90, 90, false);
        FightContract dangerous = new FightContract("Blood Price", "harder crowd, better purse", 130, 125, false);
        FightContract rival = new FightContract("Rival Challenge", "face a named rival", 115, 140, true);

        System.out.println("\n===== FIGHT CONTRACTS =====");
        System.out.println("1. " + safe.getMenuText());
        System.out.println("2. " + dangerous.getMenuText());
        System.out.println("3. " + rival.getMenuText());
        System.out.print("> ");

        switch (input.readMenuChoice()) {
            case 2:
                return dangerous;
            case 3:
                return rival;
            case 1:
            default:
                return safe;
        }
    }

    private void forceLanistaFight() {
        FightContract order = new FightContract("Lanista's Order", "compulsory bout", 100, 100, false);

        System.out.println("\nThe lanista sends guards to your cell.");
        System.out.println("\"Enough drills. You earn your food in the sand.\"");
        enterArena(order);
        nextDay();
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

    private void applyPostFightInjury() {
        int roll = random.nextInt(100);

        if (roll < 25) {
            player.applyInjury(InjuryType.BRUISED_RIBS);
        } else if (roll < 50) {
            player.applyInjury(InjuryType.WOUNDED_ARM);
        } else if (roll < 75) {
            player.applyInjury(InjuryType.SHAKEN);
        }
    }

    private void championMatch() {
        Enemy champion = arenaRoster.createChampion();

        System.out.println("\n===== CHAMPION MATCH =====");
        System.out.println("The gates open for the final time.");
        System.out.println("Before you stands " + champion.getName() + ".");

        Battle battle = new Battle(input, compactBattleText);
        boolean victory = battle.start(player, champion);

        if (victory) {
            player.addWin();
            player.recordFightDay();
            player.gainReward(champion.getGoldReward(), champion.getFameReward());
            gameWon = true;

            System.out.println("\nThe crowd falls silent.");
            System.out.println("Then the arena erupts.");
            System.out.println(player.getName() + " has defeated the Champion.");
            System.out.println(player.getName() + " is no longer a slave.");
            System.out.println(player.getName() + " is The Unbroken.");
            System.out.println("\nYOU WIN!");
        } else {
            handleArenaLoss();
        }
    }

    private void handleArenaLoss() {
        player.addLoss();
        player.recordFightDay();
        applyPostFightInjury();

        if (player.isBrokenByLosses()) {
            System.out.println("\nThe lanista watches without mercy.");
            System.out.println("\"Three failures. The school has no more use for you.\"");
            return;
        }

        System.out.println("\nYou are dragged from the sand before the killing blow.");
        System.out.println("The loss leaves you injured and barely standing.");
        player.recoverFromArenaDefeat();
        player.addCrowdFavor(-4);
        player.gainReward(0, -8);
        System.out.println("Consecutive Losses: " + player.getConsecutiveLosses() + "/3");
        System.out.println("A win will break the losing streak.");
        chooseDefeatRecovery();
    }

    private int getRivalBonusFame(Rival rival) {
        return rival.getDefeatsAgainstPlayer() == 0 ? 8 : 3;
    }

    private void chooseDefeatRecovery() {
        System.out.println("\nRecovery:");
        System.out.println("1. Infirmary cot (free, clears injury, half recovery)");
        System.out.println("2. Bribe physician (20 gold, better recovery)");
        System.out.println("3. Train through pain (+10 XP, keep injury)");
        System.out.print("> ");

        switch (input.readMenuChoice()) {
            case 2:
                if (player.spendGold(20)) {
                    player.recoverWithPhysician();
                } else {
                    player.recoverInInfirmary();
                }
                break;
            case 3:
                player.gainExperience(10);
                System.out.println("You grit your teeth and study the pain. +10 XP");
                break;
            case 1:
            default:
                player.recoverInInfirmary();
                break;
        }
    }

    private void restMenu() {
        if (player.getHp() == player.getMaxHp()
                && player.getStamina() == player.getMaxStamina()
                && player.getInjuryType() == InjuryType.NONE
                && player.getFatigue() == 0) {
            player.fullRest();
            player.recordNonFightDay();
            nextDay();
            return;
        }

        System.out.println("\n===== REST =====");
        System.out.println("1. Rest one day");
        System.out.println("2. Rest until healed");
        System.out.println("3. Back");
        System.out.print("> ");

        switch (input.readMenuChoice()) {
            case 2:
                restUntilHealed();
                break;
            case 3:
                System.out.println("You stay awake.");
                break;
            case 1:
            default:
                player.fullRest();
                player.recordNonFightDay();
                nextDay();
                break;
        }
    }

    private void restUntilHealed() {
        int daysRested = 0;

        do {
            player.fullRest();
            player.recordNonFightDay();
            nextDay();
            daysRested++;
        } while ((player.getHp() < player.getMaxHp()
                || player.getStamina() < player.getMaxStamina()
                || player.getInjuryType() != InjuryType.NONE
                || player.getFatigue() > 0)
                && daysRested < 3
                && player.getDaysSinceFight() < 4);

        System.out.println("Rested " + daysRested + " day(s).");
    }

    private void settingsMenu() {
        System.out.println("\n===== SETTINGS =====");
        System.out.println("1. Fight Text: " + (compactBattleText ? "Compact" : "Detailed"));
        System.out.println("2. Back");
        System.out.print("> ");

        if (input.readMenuChoice() == 1) {
            compactBattleText = !compactBattleText;
            System.out.println("Fight Text is now " + (compactBattleText ? "Compact." : "Detailed."));
        }
    }

    private void nextDay() {
        day++;
        applyArenaPressure();
    }

    private void applyArenaPressure() {
        if (player.getDaysSinceFight() == 3) {
            System.out.println("\nThe lanista warns you: the arena does not feed idle fighters.");
            System.out.println("Fight soon, or he will choose your next bout himself.");
        } else if (player.getDaysSinceFight() >= 4) {
            System.out.println("\nThe lanista marks your name for the next bout and docks your standing.");
            player.addCrowdFavor(-5);
            player.gainReward(0, -5);
        }
    }

    private void applyFatigueRisk() {
        if (player.getFatigue() < 70) {
            return;
        }

        if (random.nextInt(100) < player.getFatigue() - 55) {
            System.out.println("Exhaustion catches up with you.");
            player.applyInjury(InjuryType.BRUISED_RIBS);
        }
    }

    private void saveGameToMenuSlot() {
        int slot = chooseSaveSlot("SAVE GAME");

        if (slot == -1) {
            System.out.println("Save cancelled.");
            return;
        }

        saveManager.saveGame(slot, player, day,
                arenaRoster.getTitus(),
                arenaRoster.getCassius(),
                arenaRoster.getRedWolf(),
                arenaRoster.getViper());
    }

    private boolean loadGameFromMenu() {
        int slot = chooseSaveSlot("LOAD GAME");

        if (slot == -1) {
            System.out.println("Load cancelled.");
            return false;
        }

        SaveManager.GameSave loadedGame = saveManager.loadGame(slot);

        if (loadedGame != null) {
            player = loadedGame.getPlayer();
            day = loadedGame.getDay();
            gameWon = false;
            arenaRoster.resetRivals();
            arenaRoster.restoreRivalDefeats(
                    loadedGame.getTitusDefeats(),
                    loadedGame.getCassiusDefeats(),
                    loadedGame.getRedWolfDefeats(),
                    loadedGame.getViperDefeats());
            arenaRoster.restoreRivalHistory(
                    loadedGame.getRivalEncounters(),
                    new int[]{loadedGame.getTitusDefeats(), loadedGame.getCassiusDefeats(),
                            loadedGame.getRedWolfDefeats(), loadedGame.getViperDefeats()},
                    loadedGame.getRivalVictories());
            return true;
        }

        return false;
    }

    private int chooseSaveSlot(String title) {
        System.out.println("\n===== " + title + " =====");

        for (int slot = 1; slot <= SaveManager.MAX_SAVE_SLOTS; slot++) {
            System.out.println(slot + ". " + saveManager.describeSlot(slot));
        }

        System.out.println((SaveManager.MAX_SAVE_SLOTS + 1) + ". Cancel");
        System.out.print("> ");

        int choice = input.readMenuChoice();

        if (choice >= 1 && choice <= SaveManager.MAX_SAVE_SLOTS) {
            return choice;
        }

        return -1;
    }
}
