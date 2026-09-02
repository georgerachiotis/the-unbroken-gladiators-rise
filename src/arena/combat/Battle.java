package arena.combat;

import arena.characters.Enemy;
import arena.characters.Player;
import arena.enums.GladiatorClass;
import arena.io.ConsoleInput;

import java.util.Random;

/**
 * Runs one arena fight between the player and an enemy, including player
 * actions, enemy turns, class abilities, poison, traps, and victory checks.
 */
public class Battle {

    private final ConsoleInput input;
    private final Random random = new Random();
    private final boolean compactText;
    private boolean enemyTrapped = false;
    private boolean playerPoisoned = false;

    public Battle(ConsoleInput input) {
        this(input, false);
    }

    public Battle(ConsoleInput input, boolean compactText) {
        this.input = input;
        this.compactText = compactText;
    }

    public boolean start(Player player, Enemy enemy) {
        return start(player, enemy, false);
    }

    public boolean startTutorial(Player player, Enemy enemy) {
        return startGuidedTutorial(player, enemy);
    }

    private boolean start(Player player, Enemy enemy, boolean tutorial) {
        System.out.println("\n=================================");
        System.out.println(tutorial ? " THE TRAINING PIT" : " THE ARENA GATES OPEN");
        System.out.println("=================================");
        System.out.println(player.getName() + " faces " + enemy.getName() + "!");

        if (tutorial) {
            showTutorialIntro();
        }

        boolean defending = false;
        enemyTrapped = false;
        playerPoisoned = false;
        int round = 1;

        while (player.isAlive() && enemy.isAlive()) {

            if (compactText && !tutorial) {
                System.out.println("\nR" + round + " | " + player.getName()
                        + " HP " + player.getHp() + "/" + player.getMaxHp()
                        + " STA " + player.getStamina() + "/" + player.getMaxStamina()
                        + " vs " + enemy.getName()
                        + " HP " + enemy.getHp() + "/" + enemy.getMaxHp());
            } else {
                System.out.println("\n----- ROUND " + round + " -----");
                System.out.println(player.getBattleStatus());
                System.out.println(enemy.getBattleStatus());
            }

            if (tutorial) {
                showTutorialTip(round);
            }

            if (playerPoisoned) {
                player.takeDamage(3);
                System.out.println("Effect: " + player.getName() + " suffers 3 poison damage.");

                if (!player.isAlive()) {
                    break;
                }
            }

            defending = takePlayerTurn(player, enemy);
            if (enemy.isAlive()) {
                if (!compactText || tutorial) {
                    System.out.println("\nEnemy move:");
                }
                if (enemyTrapped) {
                    System.out.println(enemy.getName() + " is trapped in the net and loses the turn!");
                    enemyTrapped = false;
                } else {
                    enemyTurn(enemy, player, defending);
                }
            }
            round++;
        }

        if (player.isAlive()) {
            System.out.println(tutorial ? "\nLESSON WON!" : "\nVICTORY!");
            System.out.println(player.getName() + " defeated " + enemy.getName() + "!");
            return true;
        } else {
            System.out.println(tutorial ? "\nLESSON LOST..." : "\nDEFEAT...");
            System.out.println(player.getName() + " falls.");
            return false;
        }
    }

    private boolean startGuidedTutorial(Player player, Enemy enemy) {
        enemyTrapped = false;
        playerPoisoned = false;

        System.out.println("\n=================================");
        System.out.println(" THE TRAINING PIT");
        System.out.println("=================================");
        System.out.println(player.getName() + " faces " + enemy.getName() + "!");
        showTutorialIntro();

        teachAttack(player, enemy);
        if (!continueTutorial(player, enemy)) return false;

        teachHeavyAttack(player, enemy);
        if (!continueTutorial(player, enemy)) return false;

        teachDefend(player, enemy);
        if (!player.isAlive()) return false;

        prepareRestLesson(player);
        teachRest(player, enemy);
        if (!continueTutorial(player, enemy)) return false;

        prepareFinisher(enemy);
        teachSpecialFinisher(player, enemy);

        if (player.isAlive() && !enemy.isAlive()) {
            System.out.println("\nLESSON WON!");
            System.out.println(player.getName() + " defeated " + enemy.getName() + "!");
            return true;
        }

        System.out.println("\nLESSON LOST...");
        System.out.println(player.getName() + " falls.");
        return false;
    }

    private boolean continueTutorial(Player player, Enemy enemy) {
        if (!player.isAlive()) {
            return false;
        }

        if (enemy.isAlive()) {
            System.out.println("\nEnemy move:");
            normalEnemyAttack(enemy, player, false);
        }

        return player.isAlive();
    }

    private void teachAttack(Player player, Enemy enemy) {
        System.out.println("\n----- TUTORIAL ROUND 1: BASIC ATTACK -----");
        System.out.println("The lanista points at " + enemy.getName() + ".");
        System.out.println("\"First lesson: a quick cut. Use Attack.\"");
        showTutorialStatus(player, enemy);

        while (true) {
            showTutorialActionPrompt(player, 1);
            PlayerAction action = PlayerAction.fromMenuChoice(input.readMenuChoice());

            if (action == PlayerAction.ATTACK) {
                System.out.println("\nYour move:");
                attack(player, enemy);
                return;
            }

            explainExpectedAction("Attack is the simple strike. Choose 1.");
        }
    }

    private void teachHeavyAttack(Player player, Enemy enemy) {
        System.out.println("\n----- TUTORIAL ROUND 2: HEAVY ATTACK -----");
        System.out.println("Doros staggers, but the lanista does not let the lesson end.");
        System.out.println("\"Now commit your weight. Heavy attacks cost more stamina and can miss, but they hit harder.\"");
        showTutorialStatus(player, enemy);

        while (true) {
            showTutorialActionPrompt(player, 2);
            PlayerAction action = PlayerAction.fromMenuChoice(input.readMenuChoice());

            if (action == PlayerAction.HEAVY_ATTACK) {
                System.out.println("\nYour move:");
                heavyAttack(player, enemy);
                return;
            }

            explainExpectedAction("This round is for Heavy Attack. Choose 2.");
        }
    }

    private void teachDefend(Player player, Enemy enemy) {
        System.out.println("\n----- TUTORIAL ROUND 3: DEFEND -----");
        System.out.println("The lanista raises a hand before you can swing again.");
        System.out.println("\"You will not always be faster. Raise your guard and take the blow properly.\"");
        showTutorialStatus(player, enemy);

        while (true) {
            showTutorialActionPrompt(player, 3);
            PlayerAction action = PlayerAction.fromMenuChoice(input.readMenuChoice());

            if (action == PlayerAction.DEFEND) {
                System.out.println("\nYour move:");
                if (defend(player)) {
                    System.out.println("\nEnemy move:");
                    normalEnemyAttack(enemy, player, true);
                    return;
                }
            } else {
                explainExpectedAction("This round is for Defend. Choose 4 to reduce the next hit.");
            }
        }
    }

    private void prepareRestLesson(Player player) {
        if (player.getStamina() > 4) {
            player.useStamina(player.getStamina() - 4);
        }
    }

    private void teachRest(Player player, Enemy enemy) {
        System.out.println("\n----- TUTORIAL ROUND 4: REST -----");
        System.out.println("The lanista makes you hold your guard until your arms shake.");
        System.out.println("\"No stamina, no attack. When your breath is gone, rest and take it back.\"");
        showTutorialStatus(player, enemy);

        while (true) {
            showTutorialActionPrompt(player, 4);
            PlayerAction action = PlayerAction.fromMenuChoice(input.readMenuChoice());

            if (action == PlayerAction.REST) {
                System.out.println("\nYour move:");
                rest(player);
                return;
            }

            if (action == PlayerAction.ATTACK
                    || action == PlayerAction.HEAVY_ATTACK
                    || action == PlayerAction.SPECIAL_ABILITY
                    || action == PlayerAction.DEFEND) {
                System.out.println("Not enough stamina to fight well. This is when you Rest. Choose 5.");
            } else {
                explainExpectedAction("This round is for Rest. Choose 5.");
            }
        }
    }

    private void prepareFinisher(Enemy enemy) {
        if (enemy.getHp() > 1) {
            enemy.takeDamage(enemy.getHp() - 1);
        }
    }

    private void teachSpecialFinisher(Player player, Enemy enemy) {
        System.out.println("\n----- TUTORIAL ROUND 5: SPECIAL -----");
        System.out.println("Doros can barely stand. The lanista leans over the rail.");
        System.out.println("\"Every style has a trick. Use yours. End this.\"");
        showTutorialStatus(player, enemy);

        while (true) {
            showTutorialActionPrompt(player, 5);
            PlayerAction action = PlayerAction.fromMenuChoice(input.readMenuChoice());

            if (action == PlayerAction.SPECIAL_ABILITY) {
                System.out.println("\nYour move:");
                tutorialSpecialFinisher(player, enemy);
                return;
            }

            explainExpectedAction("This round is for your class Special. Choose 3.");
        }
    }

    private void tutorialSpecialFinisher(Player player, Enemy enemy) {
        int staminaCost = getSpecialStaminaCost(player);

        if (!player.hasStamina(staminaCost)) {
            System.out.println("The lanista snarls: \"You spent your breath. Rest before using a special.\"");
            return;
        }

        player.useStamina(staminaCost);
        enemy.takeDamage(enemy.getHp());

        switch (player.getGladiatorClass()) {
            case MURMILLO:
                System.out.println(player.getName() + " drives forward with Shield Bash.");
                break;
            case RETIARIUS:
                System.out.println(player.getName() + " casts the net and pulls Doros into the sand.");
                break;
            case DIMACHAERUS:
                System.out.println(player.getName() + " finishes with both blades.");
                break;
            case THRAEX:
                System.out.println(player.getName() + " hooks past the guard and slashes cleanly.");
                break;
            default:
                System.out.println(player.getName() + " ends the lesson with a special strike.");
                break;
        }
    }

    private void showTutorialStatus(Player player, Enemy enemy) {
        System.out.println(player.getBattleStatus());
        System.out.println(enemy.getBattleStatus());
    }

    private void explainExpectedAction(String message) {
        System.out.println("The lanista snaps his whip against the sand.");
        System.out.println(message);
    }

    private void showTutorialIntro() {
        System.out.println("\nThe lanista paces along the pit wall.");
        System.out.println("\"Before the crowd sees you, I will see if you can stand.\"");
        System.out.println("This lesson teaches one battle action at a time.");
        System.out.println("The item menu opens supplies in real fights, but you start with none.");
    }

    private void showTutorialTip(int round) {
        switch (round) {
            case 1:
                System.out.println("Tutorial: Basic attacks are cheap. Heavy attacks hit harder but cost more stamina.");
                break;
            case 2:
                System.out.println("Tutorial: Your special move depends on your class and spends its own stamina cost.");
                break;
            case 3:
                System.out.println("Tutorial: Defend lowers incoming damage. Rest restores stamina and a little HP.");
                break;
            case 4:
                System.out.println("Tutorial: Item opens salves, draughts, antidotes, and whetstones when you have them.");
                break;
            default:
                break;
        }
    }

    private boolean takePlayerTurn(Player player, Enemy enemy) {
        while (true) {
            showActionPrompt(player);
            PlayerAction action = PlayerAction.fromMenuChoice(input.readMenuChoice());

            if (!compactText) {
                System.out.println("\nYour move:");
            }
            switch (action) {
                case ATTACK:
                    if (attack(player, enemy)) {
                        return false;
                    }
                    break;
                case HEAVY_ATTACK:
                    if (heavyAttack(player, enemy)) {
                        return false;
                    }
                    break;
                case SPECIAL_ABILITY:
                    if (specialAbility(player, enemy)) {
                        return player.getGladiatorClass() == GladiatorClass.MURMILLO;
                    }
                    break;
                case DEFEND:
                    if (defend(player)) {
                        return true;
                    }
                    break;
                case REST:
                    rest(player);
                    return false;
                case USE_ITEM:
                    if (useItem(player)) {
                        return false;
                    }
                    break;
                default:
                    System.out.println("Invalid choice. Choose another action.");
                    break;
            }
        }
    }

    private boolean attack(Player attacker, Enemy defender) {
        int staminaCost = 5;

        if (!attacker.hasStamina(staminaCost)) {
            System.out.println("Not enough stamina. Choose another action.");
            return false;
        }

        attacker.useStamina(staminaCost);

        int hitRoll = random.nextInt(100);

        if (hitRoll < 10) {
            System.out.println(attacker.getName() + " attacks, but misses.");
            return true;
        }

        int damage = Math.max(1, attacker.getStrength() - defender.getDefense());
        damage = applyThraexPassive(attacker, defender, damage);

        int critRoll = random.nextInt(100);

        if (critRoll < 15) {
            damage *= 2;
            System.out.println("Critical hit!");
        }

        defender.takeDamage(damage);
        applyDimachaerusPassive(attacker, defender);

        System.out.println(attacker.getName() + " attacks for " + damage + " damage.");
        return true;
    }

    private boolean heavyAttack(Player attacker, Enemy defender) {
        int staminaCost = 15;

        if (!attacker.hasStamina(staminaCost)) {
            System.out.println("Not enough stamina. Choose another action.");
            return false;
        }

        attacker.useStamina(staminaCost);

        int hitRoll = random.nextInt(100);

        if (hitRoll < 25) {
            System.out.println(attacker.getName() + " commits to a heavy attack, but misses.");
            return true;
        }

        int damage = Math.max(1, attacker.getStrength() * 2 - defender.getDefense());
        damage = applyThraexPassive(attacker, defender, damage);

        int critRoll = random.nextInt(100);

        if (critRoll < 10) {
            damage *= 2;
            System.out.println("Brutal critical hit!");
        }

        defender.takeDamage(damage);

        System.out.println(attacker.getName() + " lands a heavy attack for " + damage + " damage.");
        return true;
    }

    /**
     * Returns true only when the player actually spends stamina and raises guard.
     */
    private boolean defend(Player player) {
        int staminaCost = 3;

        if (!player.hasStamina(staminaCost)) {
            System.out.println("Not enough stamina to raise a guard. Choose another action.");
            return false;
        }

        player.useStamina(staminaCost);
        System.out.println(player.getName() + " raises his guard.");
        return true;
    }

    private void showActionPrompt(Player player) {
        System.out.println("\nActions: 1 Attack (5 STA)"
                + " | 2 Heavy (15 STA)"
                + " | 3 Special (" + getSpecialStaminaCost(player) + " STA)"
                + " | 4 Defend (3 STA)"
                + " | 5 Rest (+15 STA, +5 HP)"
                + " | 6 Item");
        System.out.print("> ");
    }

    private void showTutorialActionPrompt(Player player, int lesson) {
        String prompt = "\nActions: 1 Attack (5 STA)";

        if (lesson >= 2) {
            prompt += " | 2 Heavy (15 STA)";
        }

        if (lesson >= 3) {
            prompt += " | 4 Defend (3 STA)";
        }

        if (lesson >= 4) {
            prompt += " | 5 Rest (+15 STA, +5 HP)";
        }

        if (lesson >= 5) {
            prompt += " | 3 Special (" + getSpecialStaminaCost(player) + " STA)";
        }

        System.out.println(prompt);
        System.out.print("> ");
    }

    private int getSpecialStaminaCost(Player player) {
        switch (player.getGladiatorClass()) {
            case MURMILLO:
                return 12;
            case RETIARIUS:
                return 15;
            case DIMACHAERUS:
                return 18;
            case THRAEX:
                return 14;
            default:
                return 0;
        }
    }

    private void rest(Player player) {
        player.recoverStamina(15);
        player.heal(5);

        System.out.println(player.getName() + " catches his breath.");
        System.out.println("+15 stamina");
        System.out.println("+5 HP");
    }

    private boolean useItem(Player player) {
        System.out.println("\nItems: 1 Salve (" + player.getHealingSalves() + ")"
                + " | 2 Draught (" + player.getStaminaDraughts() + ")"
                + " | 3 Antidote (" + player.getAntidotes() + ")"
                + " | 4 Whetstone (" + player.getWhetstones() + ")"
                + " | 5 Back");
        System.out.print("> ");

        switch (input.readMenuChoice()) {
            case 1:
                return player.useHealingSalve();
            case 2:
                return player.useStaminaDraught();
            case 3:
                if (player.useAntidote()) {
                    playerPoisoned = false;
                    return true;
                }
                return false;
            case 4:
                return player.useWhetstone();
            default:
                return false;
        }
    }

    /**
     * Returns true when the special ability was used. A miss still counts as a
     * used ability, but lacking stamina does not.
     */
    private boolean specialAbility(Player player, Enemy enemy) {
        switch (player.getGladiatorClass()) {
            case MURMILLO:
                return shieldBash(player, enemy);
            case RETIARIUS:
                return netThrow(player, enemy);
            case DIMACHAERUS:
                return doubleStrike(player, enemy);
            case THRAEX:
                return hookSlash(player, enemy);
            default:
                return false;
        }
    }

    private boolean shieldBash(Player player, Enemy enemy) {
        int staminaCost = 12;

        if (!player.hasStamina(staminaCost)) {
            System.out.println("Not enough stamina for Shield Bash. Choose another action.");
            return false;
        }

        player.useStamina(staminaCost);

        int damage = Math.max(1, player.getStrength() - enemy.getDefense() / 2);
        damage = applyThraexPassive(player, enemy, damage);
        enemy.takeDamage(damage);

        System.out.println(player.getName() + " uses Shield Bash!");
        System.out.println(enemy.getName() + " takes " + damage + " damage.");
        System.out.println(player.getName() + " braces for the next attack.");
        return true;
    }

    private boolean netThrow(Player player, Enemy enemy) {
        int staminaCost = 15;

        if (!player.hasStamina(staminaCost)) {
            System.out.println("Not enough stamina to throw the net. Choose another action.");
            return false;
        }

        player.useStamina(staminaCost);

        int hitRoll = random.nextInt(100);

        if (hitRoll < 25) {
            System.out.println(player.getName() + " throws the net but misses!");
            return true;
        }

        int damage = Math.max(1, player.getStrength() - enemy.getDefense());
        damage = applyThraexPassive(player, enemy, damage);
        enemy.takeDamage(damage);
        enemyTrapped = true;
        player.recoverStamina(5);

        System.out.println(player.getName() + " traps " + enemy.getName() + " with the net!");
        System.out.println(enemy.getName() + " takes " + damage + " damage.");
        System.out.println("Retiarius rhythm restores 5 stamina.");
        return true;
    }

    private boolean doubleStrike(Player player, Enemy enemy) {
        int staminaCost = 18;

        if (!player.hasStamina(staminaCost)) {
            System.out.println("Not enough stamina for Double Strike. Choose another action.");
            return false;
        }

        player.useStamina(staminaCost);

        int damage1 = applyThraexPassive(player, enemy, Math.max(1, player.getStrength() - enemy.getDefense()));
        int damage2 = applyThraexPassive(player, enemy, Math.max(1, player.getStrength() - enemy.getDefense()));

        enemy.takeDamage(damage1);
        enemy.takeDamage(damage2);

        System.out.println(player.getName() + " attacks with both blades!");
        System.out.println("First hit: " + damage1 + " damage.");
        System.out.println("Second hit: " + damage2 + " damage.");
        return true;
    }

    private boolean hookSlash(Player player, Enemy enemy) {
        int staminaCost = 14;

        if (!player.hasStamina(staminaCost)) {
            System.out.println("Not enough stamina for Hook Slash. Choose another action.");
            return false;
        }

        player.useStamina(staminaCost);

        int damage = Math.max(1, player.getStrength() + 5 - enemy.getDefense());
        damage = applyThraexPassive(player, enemy, damage);
        enemy.takeDamage(damage);

        System.out.println(player.getName() + " uses Hook Slash!");
        System.out.println(enemy.getName() + " takes " + damage + " damage.");
        return true;
    }

    private void enemyTurn(Enemy enemy, Player player, boolean playerDefending) {
        int specialRoll = random.nextInt(100);

        if (specialRoll < 25) {
            enemySpecialAttack(enemy, player, playerDefending);
        } else {
            normalEnemyAttack(enemy, player, playerDefending);
        }
    }

    private void normalEnemyAttack(Enemy enemy, Player player, boolean playerDefending) {
        int baseDamage = enemy.getStrength() - player.getDefense();
        int randomBonus = random.nextInt(5);

        int damage = Math.max(1, baseDamage + randomBonus);
        damage = reduceForMurmilloPassive(player, playerDefending, damage);

        if (playerDefending) {
            damage /= 2;

            if (damage < 1) {
                damage = 1;
            }
        }

        player.takeDamage(damage);

        System.out.println(enemy.getName() + " strikes back for " + damage + " damage.");
    }

    private void enemySpecialAttack(Enemy enemy, Player player, boolean playerDefending) {
        switch (enemy.getAbility()) {
            case BUTCHERS_CLEAVE:
                butchersCleave(enemy, player, playerDefending);
                break;
            case CRUSHING_BLOW:
                crushingBlow(enemy, player, playerDefending);
                break;
            case DOUBLE_STRIKE:
                enemyDoubleStrike(enemy, player, playerDefending);
                break;
            case POISON_STRIKE:
                poisonStrike(enemy, player, playerDefending);
                break;
            case SHIELD_BASH:
                shieldBash(enemy, player, playerDefending);
                break;
            case DUST_FLURRY:
                dustFlurry(enemy, player, playerDefending);
                break;
            case WILD_SWING:
                wildSwing(enemy, player, playerDefending);
                break;
            case HOOK_SLASH:
                hookSlash(enemy, player, playerDefending);
                break;
            case NET_CAST:
                netCast(enemy, player, playerDefending);
                break;
            case IRON_WALL:
                ironWall(enemy, player, playerDefending);
                break;
            default:
                normalEnemyAttack(enemy, player, playerDefending);
                break;
        }
    }

    private void butchersCleave(Enemy enemy, Player player, boolean playerDefending) {
        int hitRoll = random.nextInt(100);

        if (hitRoll < 25) {
            System.out.println(enemy.getName() + " swings wildly and misses.");
            return;
        }

        int damage = Math.max(1, enemy.getStrength() * 2 - player.getDefense());
        damage = reduceForMurmilloPassive(player, playerDefending, damage);

        if (playerDefending) {
            damage /= 2;
            if (damage < 1) damage = 1;
        }

        player.takeDamage(damage);

        System.out.println(enemy.getName() + " uses Butcher's Cleave for " + damage + " damage.");
    }

    private void crushingBlow(Enemy enemy, Player player, boolean playerDefending) {
        int hitRoll = random.nextInt(100);

        if (hitRoll < 35) {
            System.out.println(enemy.getName() + " tries a Crushing Blow but misses.");
            return;
        }

        int damage = Math.max(1, enemy.getStrength() * 3 - player.getDefense());
        damage = reduceForMurmilloPassive(player, playerDefending, damage);

        if (playerDefending) {
            damage /= 2;
            if (damage < 1) damage = 1;
        }

        player.takeDamage(damage);

        System.out.println(enemy.getName() + " lands a Crushing Blow for " + damage + " damage.");
    }

    private void enemyDoubleStrike(Enemy enemy, Player player, boolean playerDefending) {
        int damage1 = reduceForMurmilloPassive(player, playerDefending,
                Math.max(1, enemy.getStrength() - player.getDefense()));
        int damage2 = reduceForMurmilloPassive(player, playerDefending,
                Math.max(1, enemy.getStrength() - player.getDefense()));

        if (playerDefending) {
            damage1 /= 2;
            damage2 /= 2;

            if (damage1 < 1) damage1 = 1;
            if (damage2 < 1) damage2 = 1;
        }

        player.takeDamage(damage1);
        player.takeDamage(damage2);

        System.out.println(enemy.getName() + " attacks twice!");
        System.out.println("First hit: " + damage1 + " damage.");
        System.out.println("Second hit: " + damage2 + " damage.");
    }

    private void poisonStrike(Enemy enemy, Player player, boolean playerDefending) {
        int damage = Math.max(1, enemy.getStrength() + 4 - player.getDefense());
        damage = reduceForMurmilloPassive(player, playerDefending, damage);

        if (playerDefending) {
            damage /= 2;
            if (damage < 1) damage = 1;
        }

        player.takeDamage(damage);
        playerPoisoned = true;
        System.out.println(player.getName() + " is poisoned.");

        System.out.println(enemy.getName() + " uses Viper Strike for " + damage + " damage.");
    }

    private int specialDamage(Player player, boolean defending, int damage) {
        damage = reduceForMurmilloPassive(player, defending, Math.max(1, damage));
        return defending ? Math.max(1, damage / 2) : damage;
    }

    private void shieldBash(Enemy enemy, Player player, boolean defending) {
        int damage = specialDamage(player, defending,
                enemy.getStrength() - player.getDefense() / 2);
        player.takeDamage(damage);
        player.useStamina(6);
        System.out.println(enemy.getName() + " slams the shield for " + damage
                + " damage and drains 6 stamina.");
    }

    private void dustFlurry(Enemy enemy, Player player, boolean defending) {
        int first = specialDamage(player, defending, enemy.getStrength() - player.getDefense());
        int second = specialDamage(player, defending, enemy.getStrength() - player.getDefense() - 2);
        player.takeDamage(first);
        player.takeDamage(second);
        System.out.println(enemy.getName() + " uses Dust Flurry for " + first + " and " + second + " damage.");
    }

    private void wildSwing(Enemy enemy, Player player, boolean defending) {
        if (random.nextInt(100) < 40) {
            System.out.println(enemy.getName() + " swings the club wildly and misses.");
            return;
        }
        int damage = specialDamage(player, defending,
                enemy.getStrength() * 2 - player.getDefense());
        player.takeDamage(damage);
        System.out.println(enemy.getName() + " lands a Wild Swing for " + damage + " damage.");
    }

    private void hookSlash(Enemy enemy, Player player, boolean defending) {
        int damage = specialDamage(player, defending,
                enemy.getStrength() + 3 - player.getDefense() / 2);
        player.takeDamage(damage);
        System.out.println(enemy.getName() + " hooks past the guard for " + damage + " damage.");
    }

    private void netCast(Enemy enemy, Player player, boolean defending) {
        int damage = specialDamage(player, defending,
                enemy.getStrength() - player.getDefense());
        player.takeDamage(damage);
        player.useStamina(12);
        System.out.println(enemy.getName() + " casts a weighted net for " + damage
                + " damage and drains 12 stamina.");
    }

    private void ironWall(Enemy enemy, Player player, boolean defending) {
        int damage = specialDamage(player, defending,
                enemy.getStrength() + enemy.getDefense() - player.getDefense());
        player.takeDamage(damage);
        enemy.recoverStamina(10);
        System.out.println(enemy.getName() + " drives forward behind the shield for "
                + damage + " damage.");
    }

    private int applyThraexPassive(Player player, Enemy enemy, int damage) {
        if (player.getGladiatorClass() == GladiatorClass.THRAEX && enemy.getDefense() >= 5) {
            return damage + 2;
        }

        return damage;
    }

    private void applyDimachaerusPassive(Player attacker, Enemy defender) {
        if (attacker.getGladiatorClass() == GladiatorClass.DIMACHAERUS && random.nextInt(100) < 15) {
            int damage = Math.max(1, attacker.getStrength() / 2 - defender.getDefense() / 2);
            defender.takeDamage(damage);
            System.out.println(attacker.getName() + " follows with an extra blade for " + damage + " damage.");
        }
    }

    private int reduceForMurmilloPassive(Player player, boolean defending, int damage) {
        if (defending && player.getGladiatorClass() == GladiatorClass.MURMILLO) {
            return Math.max(1, damage - 2);
        }

        return damage;
    }
}
