package arena.fx;

import arena.engine.GameSession;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

class StatsPanel {

    private final VBox statsPanel;
    private final ScrollPane root;
    private final Label identityValue = new Label();
    private final Label vitalityValue = new Label();
    private final Label combatValue = new Label();
    private final Label progressionValue = new Label();
    private final Label equipmentValue = new Label();
    private final Label conditionValue = new Label();
    private final Label recordValue = new Label();

    StatsPanel() {
        statsPanel = new VBox(10);
        statsPanel.getStyleClass().add("stats-panel");
        statsPanel.setPrefWidth(290);

        statsPanel.getChildren().addAll(
                statSection("Identity", identityValue),
                statSection("Vitality", vitalityValue),
                statSection("Combat", combatValue),
                statSection("Progress", progressionValue),
                statSection("Equipment", equipmentValue),
                statSection("Condition", conditionValue),
                statSection("Record", recordValue)
        );

        root = new ScrollPane(statsPanel);
        root.getStyleClass().add("stats-scroll");
        root.setFitToWidth(true);
        root.setPrefWidth(300);
    }

    ScrollPane getRoot() {
        return root;
    }

    void refresh(GameSession session) {
        if (!session.hasPlayer()) {
            identityValue.setText("Start or load a game.");
            vitalityValue.setText("-");
            combatValue.setText("-");
            progressionValue.setText("-");
            equipmentValue.setText("-");
            conditionValue.setText("-");
            recordValue.setText("-");
            return;
        }

        identityValue.setText(session.getPlayer().getName()
                + "\n" + session.getPlayer().getGladiatorClass()
                + "\n" + session.getPlayer().getRank());
        vitalityValue.setText("HP " + session.getPlayer().getHp() + "/" + session.getPlayer().getMaxHp()
                + "\nSTA " + session.getPlayer().getStamina() + "/" + session.getPlayer().getMaxStamina());
        combatValue.setText("STR " + session.getPlayer().getStrength()
                + "\nDEF " + session.getPlayer().getDefense());
        progressionValue.setText("Level " + session.getPlayer().getLevel()
                + "\nXP " + session.getPlayer().getExperience()
                + "/" + session.getPlayer().getExperienceToNextLevel()
                + "\nGold " + session.getPlayer().getGold()
                + "\nFame " + session.getPlayer().getFame()
                + "/300"
                + "\nNext: " + session.getNextRankName()
                + (session.getFameToNextMilestone() > 0
                ? " (" + session.getFameToNextMilestone() + " Fame)" : "")
                + "\nFavor " + session.getPlayer().getCrowdFavor());
        equipmentValue.setText(session.getPlayer().getWeapon().getName()
                + "\n" + session.getPlayer().getArmor().getName()
                + "\nSalves " + session.getPlayer().getHealingSalves()
                + "\nDraughts " + session.getPlayer().getStaminaDraughts()
                + "\nAntidotes " + session.getPlayer().getAntidotes()
                + "\nWhetstones " + session.getPlayer().getWhetstones());
        conditionValue.setText("Injury: " + session.getPlayer().getInjuryType().getDisplayName()
                + "\nFatigue " + session.getPlayer().getFatigue() + "/100"
                + "\nLoss Streak " + session.getPlayer().getConsecutiveLosses() + "/3");
        recordValue.setText(session.getPlayer().getWins() + " wins"
                + "\n" + session.getPlayer().getLosses() + " losses");
    }

    private VBox statSection(String title, Label valueLabel) {
        VBox section = new VBox(5);
        section.getStyleClass().add("stat-section");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        valueLabel.getStyleClass().add("stat-value");
        valueLabel.setWrapText(true);

        section.getChildren().addAll(titleLabel, valueLabel);
        return section;
    }
}
