package arena.saves;

import arena.characters.Player;
import arena.characters.Rival;
import arena.enums.GladiatorClass;
import arena.enums.InjuryType;
import arena.enums.Difficulty;
import arena.items.Armor;
import arena.items.Weapon;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Handles writing and reading saved progress for the player and world state.
 */
public class SaveManager {

    public static final int MAX_SAVE_SLOTS = 3;

    private static final int SAVE_VERSION = 9;
    private static final String LEGACY_SAVE_FILE = "saves/save.txt";
    private static final String LEGACY_SAVE_FOLDER = "saves";

    private final String saveFile;
    private final String saveFolder;
    private final boolean legacyFallback;

    public SaveManager() {
        this(defaultSaveFolder() + File.separator + "save.txt", defaultSaveFolder(), true);
    }

    public SaveManager(String saveFile) {
        this(saveFile, LEGACY_SAVE_FOLDER, false);
    }

    public SaveManager(String saveFile, String saveFolder) {
        this(saveFile, saveFolder, false);
    }

    private SaveManager(String saveFile, String saveFolder, boolean legacyFallback) {
        this.saveFile = saveFile;
        this.saveFolder = saveFolder;
        this.legacyFallback = legacyFallback;
    }

    private static String defaultSaveFolder() {
        String localAppData = System.getenv("LOCALAPPDATA");
        String baseFolder = localAppData == null || localAppData.trim().isEmpty()
                ? System.getProperty("user.home") + File.separator + ".the-unbroken-gladiators-rise"
                : localAppData + File.separator + "TheUnbrokenGladiatorsRise";
        return baseFolder + File.separator + "saves";
    }

    /**
     * Saves named properties instead of fixed line positions, which keeps the
     * save file readable and safer to extend as the game grows.
     */
    public void saveGame(Player player, int day, Rival titus, Rival cassius, Rival redWolf, Rival viper) {
        saveGameToFile(saveFile, player, day, titus, cassius, redWolf, viper, false, 0, false);
    }

    public void saveGame(Player player, int day, Rival titus, Rival cassius,
                         Rival redWolf, Rival viper, boolean championDefeated) {
        saveGameToFile(saveFile, player, day, titus, cassius, redWolf, viper,
                championDefeated, 0, false);
    }

    public void saveGame(Player player, int day, Rival titus, Rival cassius,
                         Rival redWolf, Rival viper, boolean championDefeated,
                         int titleDefenses, boolean freedomPurchased) {
        saveGameToFile(saveFile, player, day, titus, cassius, redWolf, viper,
                championDefeated, titleDefenses, freedomPurchased);
    }

    public void saveGame(int slot, Player player, int day, Rival titus, Rival cassius, Rival redWolf, Rival viper) {
        saveGameToFile(slotFile(slot), player, day, titus, cassius, redWolf, viper, false, 0, false);
    }

    public void saveGame(int slot, Player player, int day, Rival titus, Rival cassius,
                         Rival redWolf, Rival viper, boolean championDefeated) {
        saveGameToFile(slotFile(slot), player, day, titus, cassius, redWolf, viper,
                championDefeated, 0, false);
    }

    public void saveGame(int slot, Player player, int day, Rival titus, Rival cassius,
                         Rival redWolf, Rival viper, boolean championDefeated,
                         int titleDefenses, boolean freedomPurchased) {
        saveGameToFile(slotFile(slot), player, day, titus, cassius, redWolf, viper,
                championDefeated, titleDefenses, freedomPurchased);
    }

    private void saveGameToFile(String targetFile,
                                Player player,
                                int day,
                                Rival titus,
                                Rival cassius,
                                Rival redWolf,
                                Rival viper,
                                boolean championDefeated,
                                int titleDefenses,
                                boolean freedomPurchased) {
        Properties save = new Properties();

        save.setProperty("save.version", String.valueOf(SAVE_VERSION));
        save.setProperty("player.name", player.getName());
        save.setProperty("player.class", player.getGladiatorClass().name());
        save.setProperty("career.difficulty", player.getDifficulty().name());
        save.setProperty("player.hp", String.valueOf(player.getHp()));
        save.setProperty("player.maxHp", String.valueOf(player.getMaxHp()));
        save.setProperty("player.stamina", String.valueOf(player.getStamina()));
        save.setProperty("player.maxStamina", String.valueOf(player.getMaxStamina()));
        save.setProperty("player.strength", String.valueOf(player.getBaseStrength()));
        save.setProperty("player.defense", String.valueOf(player.getBaseDefense()));
        save.setProperty("player.gold", String.valueOf(player.getGold()));
        save.setProperty("player.fame", String.valueOf(player.getFame()));
        save.setProperty("player.level", String.valueOf(player.getLevel()));
        save.setProperty("player.experience", String.valueOf(player.getExperience()));
        save.setProperty("player.wins", String.valueOf(player.getWins()));
        save.setProperty("player.losses", String.valueOf(player.getLosses()));
        save.setProperty("player.consecutiveLosses", String.valueOf(player.getConsecutiveLosses()));
        save.setProperty("player.crowdFavor", String.valueOf(player.getCrowdFavor()));
        save.setProperty("player.fatigue", String.valueOf(player.getFatigue()));
        save.setProperty("player.daysSinceFight", String.valueOf(player.getDaysSinceFight()));
        save.setProperty("player.injury", player.getInjuryType().name());
        save.setProperty("player.healingSalves", String.valueOf(player.getHealingSalves()));
        save.setProperty("player.staminaDraughts", String.valueOf(player.getStaminaDraughts()));
        save.setProperty("player.antidotes", String.valueOf(player.getAntidotes()));
        save.setProperty("player.whetstones", String.valueOf(player.getWhetstones()));

        save.setProperty("weapon.name", player.getWeapon().getName());
        save.setProperty("weapon.strengthBonus", String.valueOf(player.getWeapon().getStrengthBonus()));
        save.setProperty("armor.name", player.getArmor().getName());
        save.setProperty("armor.defenseBonus", String.valueOf(player.getArmor().getDefenseBonus()));

        save.setProperty("day", String.valueOf(day));
        save.setProperty("career.championDefeated", String.valueOf(championDefeated));
        save.setProperty("career.titleDefenses", String.valueOf(Math.max(0, titleDefenses)));
        save.setProperty("career.freedomPurchased", String.valueOf(freedomPurchased));
        save.setProperty("rival.titus.defeats", String.valueOf(titus.getDefeatsAgainstPlayer()));
        save.setProperty("rival.cassius.defeats", String.valueOf(cassius.getDefeatsAgainstPlayer()));
        save.setProperty("rival.redWolf.defeats", String.valueOf(redWolf.getDefeatsAgainstPlayer()));
        save.setProperty("rival.viper.defeats", String.valueOf(viper.getDefeatsAgainstPlayer()));
        save.setProperty("rival.titus.encounters", String.valueOf(titus.getEncountersAgainstPlayer()));
        save.setProperty("rival.cassius.encounters", String.valueOf(cassius.getEncountersAgainstPlayer()));
        save.setProperty("rival.redWolf.encounters", String.valueOf(redWolf.getEncountersAgainstPlayer()));
        save.setProperty("rival.viper.encounters", String.valueOf(viper.getEncountersAgainstPlayer()));
        save.setProperty("rival.titus.victories", String.valueOf(titus.getVictoriesAgainstPlayer()));
        save.setProperty("rival.cassius.victories", String.valueOf(cassius.getVictoriesAgainstPlayer()));
        save.setProperty("rival.redWolf.victories", String.valueOf(redWolf.getVictoriesAgainstPlayer()));
        save.setProperty("rival.viper.victories", String.valueOf(viper.getVictoriesAgainstPlayer()));

        ensureSaveFolderExists(targetFile);

        try (FileWriter writer = new FileWriter(targetFile)) {
            save.store(writer, "The Unbroken - save file");
            System.out.println("Game saved.");
        } catch (IOException e) {
            System.out.println("Could not save game.");
        }
    }

    public GameSave loadGame() {
        return loadGameFromFile(saveFile, "No save file found.");
    }

    public GameSave loadGame(int slot) {
        return loadGameFromFile(slotFileForRead(slot), "No save file found in that slot.");
    }

    public String describeSlot(int slot) {
        SaveSlotInfo info = getSlotInfo(slot);

        switch (info.getStatus()) {
            case FILLED:
                return "Slot " + slot + " - " + info.getPlayerName()
                        + " | Day " + info.getDayText() + " | " + info.getRank();
            case DAMAGED:
                return "Slot " + slot + " - Damaged save";
            case EMPTY:
            default:
                return "Slot " + slot + " - Empty";
        }
    }

    public SaveSlotInfo getSlotInfo(int slot) {
        String targetFile = slotFileForRead(slot);
        Properties save = new Properties();

        try (FileReader reader = new FileReader(targetFile)) {
            save.load(reader);
            String name = save.getProperty("player.name", "Unknown");
            String dayText = save.getProperty("day", "?");
            int fame = intValue(save, "player.fame", 0);

            return new SaveSlotInfo(slot, SaveSlotInfo.Status.FILLED, name, dayText, rankForFame(fame));
        } catch (IOException e) {
            return new SaveSlotInfo(slot, SaveSlotInfo.Status.EMPTY, "", "", "");
        } catch (IllegalArgumentException e) {
            return new SaveSlotInfo(slot, SaveSlotInfo.Status.DAMAGED, "", "", "");
        }
    }

    /** Deletes only the selected slot. Slot 1 also clears the legacy fallback save. */
    public boolean deleteGame(int slot) {
        String targetFile = slotFile(slot);
        boolean existed = new File(targetFile).exists();

        try {
            boolean deleted = Files.deleteIfExists(Path.of(targetFile));
            if (slot == 1) {
                existed = existed || new File(saveFile).exists();
                deleted = Files.deleteIfExists(Path.of(saveFile)) || deleted;
            }
            if (legacyFallback) {
                String legacySlot = LEGACY_SAVE_FOLDER + File.separator + "slot" + slot + ".txt";
                existed = existed || new File(legacySlot).exists();
                deleted = Files.deleteIfExists(Path.of(legacySlot)) || deleted;
                if (slot == 1) {
                    existed = existed || new File(LEGACY_SAVE_FILE).exists();
                    deleted = Files.deleteIfExists(Path.of(LEGACY_SAVE_FILE)) || deleted;
                }
            }
            return existed && deleted;
        } catch (IOException e) {
            return false;
        }
    }

    private GameSave loadGameFromFile(String targetFile, String missingMessage) {
        Properties save = new Properties();

        try (FileReader reader = new FileReader(targetFile)) {
            save.load(reader);
            int version = intValue(save, "save.version", 1);

            if (version > SAVE_VERSION) {
                throw new IllegalArgumentException("Unsupported save version: " + version);
            }

            Player player = new Player(
                    required(save, "player.name"),
                    GladiatorClass.valueOf(required(save, "player.class"))
            );
            player.setDifficulty(Difficulty.valueOf(
                    save.getProperty("career.difficulty", Difficulty.STANDARD.name())));

            Weapon weapon = new Weapon(
                    required(save, "weapon.name"),
                    intValue(save, "weapon.strengthBonus", 0)
            );

            Armor armor = new Armor(
                    required(save, "armor.name"),
                    intValue(save, "armor.defenseBonus", 0)
            );

            // Base strength and defense are stored separately from equipment bonuses.
            player.restoreSavedState(
                    intValue(save, "player.hp", player.getMaxHp()),
                    intValue(save, "player.maxHp", player.getMaxHp()),
                    intValue(save, "player.stamina", player.getMaxStamina()),
                    intValue(save, "player.maxStamina", player.getMaxStamina()),
                    intValue(save, "player.strength", player.getBaseStrength()),
                    intValue(save, "player.defense", player.getBaseDefense()),
                    intValue(save, "player.gold", player.getGold()),
                    intValue(save, "player.fame", player.getFame()),
                    intValue(save, "player.level", player.getLevel()),
                    intValue(save, "player.experience", player.getExperience()),
                    intValue(save, "player.wins", player.getWins()),
                    intValue(save, "player.losses", player.getLosses()),
                    weapon,
                    armor
            );
            player.restoreExtras(
                    intValue(save, "player.crowdFavor", 0),
                    InjuryType.valueOf(save.getProperty("player.injury", InjuryType.NONE.name())),
                    intValue(save, "player.healingSalves", 0),
                    intValue(save, "player.staminaDraughts", 0),
                    intValue(save, "player.antidotes", 0),
                    intValue(save, "player.whetstones", 0),
                    intValue(save, "player.fatigue", 0),
                    intValue(save, "player.daysSinceFight", 0),
                    intValue(save, "player.consecutiveLosses", 0)
            );

            System.out.println("Game loaded.");
            return new GameSave(
                    player,
                    version,
                    intValue(save, "day", 1),
                    intValue(save, "rival.titus.defeats", 0),
                    intValue(save, "rival.cassius.defeats", 0),
                    intValue(save, "rival.redWolf.defeats", 0),
                    intValue(save, "rival.viper.defeats", 0),
                    intValue(save, "rival.titus.encounters", intValue(save, "rival.titus.defeats", 0)),
                    intValue(save, "rival.cassius.encounters", intValue(save, "rival.cassius.defeats", 0)),
                    intValue(save, "rival.redWolf.encounters", intValue(save, "rival.redWolf.defeats", 0)),
                    intValue(save, "rival.viper.encounters", intValue(save, "rival.viper.defeats", 0)),
                    intValue(save, "rival.titus.victories", 0),
                    intValue(save, "rival.cassius.victories", 0),
                    intValue(save, "rival.redWolf.victories", 0),
                    intValue(save, "rival.viper.victories", 0),
                    Boolean.parseBoolean(save.getProperty("career.championDefeated", "false")),
                    intValue(save, "career.titleDefenses", 0),
                    Boolean.parseBoolean(save.getProperty("career.freedomPurchased", "false"))
            );
        } catch (IOException e) {
            System.out.println(missingMessage);
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println("Save file is damaged.");
            return null;
        }
    }

    private String required(Properties save, String key) {
        String value = save.getProperty(key);

        if (value == null) {
            throw new IllegalArgumentException("Missing save value: " + key);
        }

        return value;
    }

    private void ensureSaveFolderExists(String targetFile) {
        File file = new File(targetFile);
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    private int intValue(Properties save, String key, int defaultValue) {
        String value = save.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        return Integer.parseInt(value);
    }

    private String slotFile(int slot) {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) {
            throw new IllegalArgumentException("Invalid save slot: " + slot);
        }

        return saveFolder + "/slot" + slot + ".txt";
    }

    private String slotFileForRead(int slot) {
        String targetFile = slotFile(slot);

        if (new File(targetFile).exists()) return targetFile;

        if (slot == 1 && new File(saveFile).exists()) {
            return saveFile;
        }

        if (legacyFallback) {
            String legacySlot = LEGACY_SAVE_FOLDER + File.separator + "slot" + slot + ".txt";
            if (new File(legacySlot).exists()) return legacySlot;
            if (slot == 1 && new File(LEGACY_SAVE_FILE).exists()) return LEGACY_SAVE_FILE;
        }

        return targetFile;
    }

    private String rankForFame(int fame) {
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

    /**
     * Bundles the player and world state that Game needs after loading.
     */
    public static class GameSave {
        private final Player player;
        private final int saveVersion;
        private final int day;
        private final int titusDefeats;
        private final int cassiusDefeats;
        private final int redWolfDefeats;
        private final int viperDefeats;
        private final int[] rivalEncounters;
        private final int[] rivalVictories;
        private final boolean championDefeated;
        private final int titleDefenses;
        private final boolean freedomPurchased;

        public GameSave(Player player,
                        int saveVersion,
                        int day,
                        int titusDefeats,
                        int cassiusDefeats,
                        int redWolfDefeats,
                        int viperDefeats) {
            this(player, saveVersion, day, titusDefeats, cassiusDefeats, redWolfDefeats, viperDefeats,
                    titusDefeats, cassiusDefeats, redWolfDefeats, viperDefeats, 0, 0, 0, 0, false);
        }

        public GameSave(Player player,
                        int saveVersion,
                        int day,
                        int titusDefeats,
                        int cassiusDefeats,
                        int redWolfDefeats,
                        int viperDefeats,
                        int titusEncounters,
                        int cassiusEncounters,
                        int redWolfEncounters,
                        int viperEncounters,
                        int titusVictories,
                        int cassiusVictories,
                        int redWolfVictories,
                        int viperVictories) {
            this(player, saveVersion, day, titusDefeats, cassiusDefeats, redWolfDefeats, viperDefeats,
                    titusEncounters, cassiusEncounters, redWolfEncounters, viperEncounters,
                    titusVictories, cassiusVictories, redWolfVictories, viperVictories,
                    false, 0, false);
        }

        public GameSave(Player player, int saveVersion, int day,
                        int titusDefeats, int cassiusDefeats, int redWolfDefeats, int viperDefeats,
                        int titusEncounters, int cassiusEncounters, int redWolfEncounters, int viperEncounters,
                        int titusVictories, int cassiusVictories, int redWolfVictories, int viperVictories,
                        boolean championDefeated) {
            this(player, saveVersion, day, titusDefeats, cassiusDefeats, redWolfDefeats, viperDefeats,
                    titusEncounters, cassiusEncounters, redWolfEncounters, viperEncounters,
                    titusVictories, cassiusVictories, redWolfVictories, viperVictories,
                    championDefeated, 0, false);
        }

        public GameSave(Player player, int saveVersion, int day,
                        int titusDefeats, int cassiusDefeats, int redWolfDefeats, int viperDefeats,
                        int titusEncounters, int cassiusEncounters, int redWolfEncounters, int viperEncounters,
                        int titusVictories, int cassiusVictories, int redWolfVictories, int viperVictories,
                        boolean championDefeated, int titleDefenses, boolean freedomPurchased) {
            this.player = player;
            this.saveVersion = Math.max(1, saveVersion);
            this.day = Math.max(1, day);
            this.titusDefeats = Math.max(0, titusDefeats);
            this.cassiusDefeats = Math.max(0, cassiusDefeats);
            this.redWolfDefeats = Math.max(0, redWolfDefeats);
            this.viperDefeats = Math.max(0, viperDefeats);
            this.rivalEncounters = new int[]{Math.max(0, titusEncounters), Math.max(0, cassiusEncounters),
                    Math.max(0, redWolfEncounters), Math.max(0, viperEncounters)};
            this.rivalVictories = new int[]{Math.max(0, titusVictories), Math.max(0, cassiusVictories),
                    Math.max(0, redWolfVictories), Math.max(0, viperVictories)};
            this.championDefeated = championDefeated;
            this.titleDefenses = Math.max(0, titleDefenses);
            this.freedomPurchased = freedomPurchased;
        }

        public Player getPlayer() {
            return player;
        }

        public int getSaveVersion() {
            return saveVersion;
        }

        public int getDay() {
            return day;
        }

        public int getTitusDefeats() {
            return titusDefeats;
        }

        public int getCassiusDefeats() {
            return cassiusDefeats;
        }

        public int getRedWolfDefeats() {
            return redWolfDefeats;
        }

        public int getViperDefeats() {
            return viperDefeats;
        }

        public int[] getRivalEncounters() {
            return rivalEncounters.clone();
        }

        public int[] getRivalVictories() {
            return rivalVictories.clone();
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
    }
}
