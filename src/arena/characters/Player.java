package arena.characters;

import arena.enums.GladiatorClass;
import arena.enums.InjuryType;
import arena.enums.Difficulty;
import arena.items.Armor;
import arena.items.Weapon;

/**
 * Represents the player's gladiator, including class choice, progression,
 * combat record, money, fame, and equipped weapon and armor.
 */
public class Player extends Combatant {

    private GladiatorClass gladiatorClass;
    private int gold;
    private int fame;
    private int level;
    private int experience;
    private int wins;
    private int losses;
    private int consecutiveLosses;
    private int crowdFavor;
    private int fatigue;
    private int daysSinceFight;
    private InjuryType injuryType;
    private int healingSalves;
    private int staminaDraughts;
    private int antidotes;
    private int whetstones;
    private Weapon weapon;
    private Armor armor;
    private Difficulty difficulty;

    public Player(String name, GladiatorClass gladiatorClass) {
        super(name,
                startingHp(gladiatorClass),
                startingStamina(gladiatorClass),
                startingStrength(gladiatorClass),
                startingDefense(gladiatorClass));

        this.gladiatorClass = gladiatorClass;
        this.gold = 20;
        this.fame = 0;
        this.level = 1;
        this.experience = 0;
        this.wins = 0;
        this.losses = 0;
        this.consecutiveLosses = 0;
        this.crowdFavor = 0;
        this.fatigue = 0;
        this.daysSinceFight = 0;
        this.injuryType = InjuryType.NONE;
        this.healingSalves = 0;
        this.staminaDraughts = 0;
        this.antidotes = 0;
        this.whetstones = 0;
        this.difficulty = Difficulty.STANDARD;

        this.weapon = createStartingWeapon(gladiatorClass);
        this.armor = new Armor("Worn Leather Armor", 0);
    }

    private static int startingHp(GladiatorClass gladiatorClass) {
        switch (gladiatorClass) {
            case MURMILLO:
                return 115;
            case RETIARIUS:
                return 100;
            case DIMACHAERUS:
                return 95;
            case THRAEX:
                return 105;
            default:
                return 100;
        }
    }

    private static int startingStamina(GladiatorClass gladiatorClass) {
        switch (gladiatorClass) {
            case MURMILLO:
                return 45;
            case RETIARIUS:
                return 65;
            case DIMACHAERUS:
                return 50;
            case THRAEX:
                return 55;
            default:
                return 50;
        }
    }

    private static int startingStrength(GladiatorClass gladiatorClass) {
        switch (gladiatorClass) {
            case MURMILLO:
                return 11;
            case RETIARIUS:
                return 10;
            case DIMACHAERUS:
                return 12;
            case THRAEX:
                return 12;
            default:
                return 12;
        }
    }

    private static int startingDefense(GladiatorClass gladiatorClass) {
        switch (gladiatorClass) {
            case MURMILLO:
                return 7;
            case RETIARIUS:
                return 5;
            case DIMACHAERUS:
                return 4;
            case THRAEX:
                return 6;
            default:
                return 5;
        }
    }

    private Weapon createStartingWeapon(GladiatorClass gladiatorClass) {
        switch (gladiatorClass) {
            case MURMILLO:
                return new Weapon("Rusty Gladius", 0);
            case RETIARIUS:
                return new Weapon("Rusty Trident", 0);
            case DIMACHAERUS:
                return new Weapon("Rusty Dual Blades", 0);
            case THRAEX:
                return new Weapon("Rusty Sica", 0);
            default:
                return new Weapon("Rusty Weapon", 0);
        }
    }

    @Override
    public int getStrength() {
        int injuryPenalty = injuryType == InjuryType.WOUNDED_ARM ? 2 : 0;
        return Math.max(1, strength + weapon.getStrengthBonus() - injuryPenalty);
    }

    @Override
    public int getDefense() {
        int injuryPenalty = injuryType == InjuryType.SHAKEN ? 2 : 0;
        return Math.max(0, defense + armor.getDefenseBonus() - injuryPenalty);
    }

    @Override
    public int getStamina() {
        int injuryPenalty = injuryType == InjuryType.BRUISED_RIBS ? 8 : 0;
        return Math.max(0, stamina - injuryPenalty);
    }

    @Override
    public boolean hasStamina(int amount) {
        return getStamina() >= amount;
    }

    public GladiatorClass getGladiatorClass() {
        return gladiatorClass;
    }

    public int getGold() {
        return gold;
    }

    public int getFame() {
        return fame;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public int getExperienceToNextLevel() {
        return level * 40;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getConsecutiveLosses() {
        return consecutiveLosses;
    }

    public int getCrowdFavor() {
        return crowdFavor;
    }

    public int getFatigue() {
        return fatigue;
    }

    public int getDaysSinceFight() {
        return daysSinceFight;
    }

    public InjuryType getInjuryType() {
        return injuryType;
    }

    public int getHealingSalves() {
        return healingSalves;
    }

    public int getStaminaDraughts() {
        return staminaDraughts;
    }

    public int getAntidotes() {
        return antidotes;
    }

    public int getWhetstones() {
        return whetstones;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public Armor getArmor() {
        return armor;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty == null ? Difficulty.STANDARD : difficulty;
    }

    public void equipWeapon(Weapon weapon) {
        this.weapon = weapon;
        System.out.println("Equipped weapon: " + weapon.getName());
    }

    public void equipArmor(Armor armor) {
        this.armor = armor;
        System.out.println("Equipped armor: " + armor.getName());
    }

    public void addWin() {
        wins++;
        consecutiveLosses = 0;
    }

    public void addLoss() {
        losses++;
        consecutiveLosses++;
    }

    public boolean isBrokenByLosses() {
        return consecutiveLosses >= 3;
    }

    public void recoverFromArenaDefeat() {
        hp = Math.max(1, maxHp / 4);
        stamina = Math.max(0, maxStamina / 4);
        addFatigue(18);
    }

    public void recoverInInfirmary() {
        hp = Math.max(hp, maxHp / 2);
        stamina = Math.max(stamina, maxStamina / 2);
        injuryType = InjuryType.NONE;
        reduceFatigue(10);
        System.out.println("The infirmary binds your wounds.");
        System.out.println("HP and stamina restored to half. Injury cleared.");
    }

    public void recoverWithPhysician() {
        hp = Math.max(hp, maxHp * 3 / 4);
        stamina = Math.max(stamina, maxStamina * 3 / 4);
        injuryType = InjuryType.NONE;
        reduceFatigue(18);
        System.out.println("The physician treats you with clean linen and bitter wine.");
        System.out.println("HP and stamina restored to three quarters. Injury cleared.");
    }

    public String getRank() {
        if (fame >= 300) {
            return "Champion";
        } else if (fame >= 150) {
            return "Arena Veteran";
        } else if (fame >= 50) {
            return "Arena Rookie";
        } else {
            return "Pit Fighter";
        }
    }

    public void gainReward(int gold, int fame) {
        this.gold = Math.max(0, this.gold + gold);
        this.fame = Math.max(0, this.fame + fame);
        gainExperience(Math.max(0, fame) * 3);
    }

    public void addCrowdFavor(int amount) {
        crowdFavor = Math.max(0, crowdFavor + amount);
        System.out.println("Crowd Favor: " + crowdFavor);
    }

    public void addFatigue(int amount) {
        fatigue = Math.min(100, Math.max(0, fatigue + amount));
        System.out.println("Fatigue: " + fatigue + "/100");
    }

    public void reduceFatigue(int amount) {
        fatigue = Math.max(0, fatigue - amount);
    }

    public void recordNonFightDay() {
        daysSinceFight++;
    }

    public void recordFightDay() {
        daysSinceFight = 0;
        reduceFatigue(8);
    }

    public boolean spendCrowdFavor(int amount) {
        if (crowdFavor < amount) {
            return false;
        }

        crowdFavor -= amount;
        return true;
    }

    public void applyInjury(InjuryType injuryType) {
        if (injuryType == InjuryType.NONE) {
            return;
        }

        this.injuryType = injuryType;
        System.out.println("Injury: " + injuryType.getDisplayName());
    }

    public void gainExperience(int amount) {
        experience += Math.max(0, amount);

        while (experience >= getExperienceToNextLevel()) {
            experience -= getExperienceToNextLevel();
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        maxHp += 8;
        maxStamina += 4;

        if (level % 2 == 0) {
            strength += 1;
        } else {
            defense += 1;
        }

        hp = maxHp;
        stamina = maxStamina;

        System.out.println(name + " reaches level " + level + "!");
        System.out.println("HP and stamina restored.");
    }

    public boolean spendGold(int amount) {
        if (gold < amount) {
            System.out.println("Not enough gold.");
            return false;
        }

        gold -= amount;
        return true;
    }

    public void addHealingSalve() {
        healingSalves++;
        System.out.println("Bought Healing Salve.");
    }

    public void addStaminaDraught() {
        staminaDraughts++;
        System.out.println("Bought Stamina Draught.");
    }

    public void addAntidote() {
        antidotes++;
        System.out.println("Bought Antidote.");
    }

    public void addWhetstone() {
        whetstones++;
        System.out.println("Bought Whetstone.");
    }

    public boolean useHealingSalve() {
        if (healingSalves <= 0) {
            System.out.println("No Healing Salves.");
            return false;
        }

        healingSalves--;
        heal(30);
        System.out.println("You use a Healing Salve. +30 HP");
        return true;
    }

    public boolean useStaminaDraught() {
        if (staminaDraughts <= 0) {
            System.out.println("No Stamina Draughts.");
            return false;
        }

        staminaDraughts--;
        recoverStamina(25);
        System.out.println("You drink a Stamina Draught. +25 stamina");
        return true;
    }

    public boolean useAntidote() {
        if (antidotes <= 0) {
            System.out.println("No Antidotes.");
            return false;
        }

        antidotes--;
        System.out.println("You use an Antidote.");
        return true;
    }

    public boolean useWhetstone() {
        if (whetstones <= 0) {
            System.out.println("No Whetstones.");
            return false;
        }

        whetstones--;
        strength += 1;
        System.out.println("You sharpen your weapon. +1 Strength");
        return true;
    }

    public boolean trainStrength() {
        int cap = getStrengthTrainingCap();
        if (strength >= cap) {
            showTrainingCapMessage("Strength", cap);
            return false;
        }

        int gain = Math.min(fatigue >= 60 ? 1 : 2, cap - strength);
        strength += gain;
        addFatigue(12);
        System.out.println(name + " trains with a wooden sword.");
        System.out.println("+" + gain + " Strength");
        return true;
    }

    public boolean trainDefense() {
        int cap = getDefenseTrainingCap();
        if (defense >= cap) {
            showTrainingCapMessage("Defense", cap);
            return false;
        }

        defense = Math.min(cap, defense + 1);
        addFatigue(10);
        System.out.println(name + " trains with a shield.");
        System.out.println("+1 Defense");
        return true;
    }

    public boolean trainStamina() {
        int cap = getStaminaTrainingCap();
        if (maxStamina >= cap) {
            showTrainingCapMessage("Max Stamina", cap);
            return false;
        }

        int gain = Math.min(fatigue >= 60 ? 3 : 5, cap - maxStamina);
        maxStamina += gain;
        stamina = maxStamina;
        addFatigue(10);
        System.out.println(name + " improves endurance.");
        System.out.println("+" + gain + " Max Stamina");
        return true;
    }

    public void increaseMaxStamina(int amount) {
        maxStamina += amount;
        stamina = maxStamina;
        System.out.println("+" + amount + " Max Stamina");
    }

    public void fullRest() {
        hp = maxHp;
        stamina = maxStamina;
        injuryType = InjuryType.NONE;
        reduceFatigue(35);
        System.out.println(name + " rests for the day.");
        System.out.println("HP, stamina, injuries restored. Fatigue reduced.");
    }

    @Override
    public void showStats() {
        System.out.println("\n----- STATS -----");
        System.out.println("Name: " + name);
        System.out.println("Class: " + gladiatorClass);
        System.out.println("HP: " + hp + "/" + maxHp);
        System.out.println("Stamina: " + stamina + "/" + maxStamina);
        System.out.println("Strength: " + getStrength());
        System.out.println("Defense: " + getDefense());
        System.out.println("Weapon: " + weapon.getName() + " (+" + weapon.getStrengthBonus() + " STR)");
        System.out.println("Armor: " + armor.getName() + " (+" + armor.getDefenseBonus() + " DEF)");
        System.out.println("Level: " + level);
        System.out.println("XP: " + experience + "/" + getExperienceToNextLevel());
        System.out.println("Rank: " + getRank());
        System.out.println("Gold: " + gold);
        System.out.println("Fame: " + fame);
        System.out.println("Crowd Favor: " + crowdFavor);
        System.out.println("Fatigue: " + fatigue + "/100");
        System.out.println("Days Since Fight: " + daysSinceFight);
        System.out.println("Injury: " + injuryType.getDisplayName());
        System.out.println("Consecutive Losses: " + consecutiveLosses + "/3");
        System.out.println("Consumables: Salves " + healingSalves
                + ", Draughts " + staminaDraughts
                + ", Antidotes " + antidotes
                + ", Whetstones " + whetstones);
        System.out.println("Record: " + wins + "-" + losses);
    }

    public String getBattleStatus() {
        return name + " HP " + hp + "/" + maxHp
                + " | STA " + getStamina() + "/" + maxStamina
                + " | STR " + getStrength()
                + " | DEF " + getDefense()
                + " | Favor " + crowdFavor
                + " | Fatigue " + fatigue
                + " | Loss Streak " + consecutiveLosses + "/3"
                + (injuryType == InjuryType.NONE ? "" : " | " + injuryType.getDisplayName());
    }

    public void restoreExtras(int crowdFavor,
                              InjuryType injuryType,
                              int healingSalves,
                              int staminaDraughts,
                              int antidotes,
                              int whetstones,
                              int fatigue,
                              int daysSinceFight,
                              int consecutiveLosses) {
        this.crowdFavor = Math.max(0, crowdFavor);
        this.injuryType = injuryType == null ? InjuryType.NONE : injuryType;
        this.healingSalves = Math.max(0, healingSalves);
        this.staminaDraughts = Math.max(0, staminaDraughts);
        this.antidotes = Math.max(0, antidotes);
        this.whetstones = Math.max(0, whetstones);
        this.fatigue = Math.max(0, Math.min(100, fatigue));
        this.daysSinceFight = Math.max(0, daysSinceFight);
        this.consecutiveLosses = Math.max(0, consecutiveLosses);
    }

    private int getStrengthTrainingCap() {
        if (fame >= 300) {
            return 45;
        } else if (fame >= 150) {
            return 36;
        } else if (fame >= 50) {
            return 28;
        } else {
            return 20;
        }
    }

    private int getDefenseTrainingCap() {
        if (fame >= 300) {
            return 35;
        } else if (fame >= 150) {
            return 28;
        } else if (fame >= 50) {
            return 22;
        } else {
            return 16;
        }
    }

    private int getStaminaTrainingCap() {
        if (fame >= 300) {
            return 180;
        } else if (fame >= 150) {
            return 150;
        } else if (fame >= 50) {
            return 120;
        } else {
            return 90;
        }
    }

    private void showTrainingCapMessage(String statName, int cap) {
        System.out.println("The lanista stops the drill. " + statName
                + " is capped at " + cap + " for your current rank ("
                + getRank() + "). Win more fame before this training helps again.");
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    /**
     * Restores the full player state after construction. Equipment is supplied
     * separately so base stats do not accidentally include item bonuses twice.
     */
    public void restoreSavedState(int hp,
                                  int maxHp,
                                  int stamina,
                                  int maxStamina,
                                  int strength,
                                  int defense,
                                  int gold,
                                  int fame,
                                  int level,
                                  int experience,
                                  int wins,
                                  int losses,
                                  Weapon weapon,
                                  Armor armor) {
        restoreCoreStats(hp, maxHp, stamina, maxStamina, strength, defense);
        this.gold = Math.max(0, gold);
        this.fame = Math.max(0, fame);
        this.level = Math.max(1, level);
        this.experience = Math.max(0, experience);
        this.wins = Math.max(0, wins);
        this.losses = Math.max(0, losses);
        this.weapon = weapon;
        this.armor = armor;
    }
}
