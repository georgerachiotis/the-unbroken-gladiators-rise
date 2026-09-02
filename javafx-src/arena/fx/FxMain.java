package arena.fx;

import arena.characters.Player;
import arena.characters.Rival;
import arena.engine.GameSession;
import arena.engine.BattleSummary;
import arena.enums.GladiatorClass;
import arena.enums.Difficulty;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.function.Supplier;
import java.util.prefs.Preferences;

/**
 * First JavaFX version of the game shell. It reuses GameSession so the same
 * gameplay state can later drive a proper RPG battle scene.
 */
public class FxMain extends Application {

    private static final Preferences SETTINGS = Preferences.userNodeForPackage(FxMain.class);

    private final GameSession session = new GameSession();

    private Label headerLabel;
    private Button statsToggleButton;
    private Button viewToggleButton;
    private BattleSceneView battleSceneView;
    private TextArea logArea;
    private Label historyPreviewLabel;
    private Button logToggleButton;
    private StatsPanel statsPanelView;
    private TextField nameField;
    private ComboBox<GladiatorClass> classBox;
    private ComboBox<Difficulty> difficultyBox;
    private Label difficultyPreviewLabel;
    private Label classPreviewLabel;
    private Button newGameButton;
    private VBox newGamePanel;
    private CommandPanelView commandPanelView;
    private VBox centerPanel;
    private SplitPane historyCommandPane;
    private VBox historyPanel;
    private ScrollPane commandScroll;
    private ScreenState screenState = ScreenState.START;
    private boolean statsVisible = false;
    private boolean fullLogVisible = false;
    private boolean expandedView = false;
    private AudioManager audioManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        audioManager = new AudioManager();
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        root.setTop(createHeaderPanel());

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.getStyleClass().add("battle-log");
        root.setCenter(createCenterPanel());

        statsPanelView = new StatsPanel();
        root.setRight(statsPanelView.getRoot());
        statsPanelView.getRoot().setVisible(false);
        statsPanelView.getRoot().setManaged(false);

        newGamePanel = createNewGamePanel();
        root.setLeft(newGamePanel);

        Scene scene = new Scene(root, 1120, 700);
        scene.getStylesheets().add(getClass().getResource("fx-style.css").toExternalForm());

        stage.setTitle("The Unbroken: Gladiator's Rise");
        stage.getIcons().add(new Image(
                getClass().getResource("assets/icons/app-icon.png").toExternalForm()));
        stage.setScene(scene);
        stage.setMinWidth(980);
        stage.setMinHeight(620);
        stage.show();

        audioManager.playTheme(AudioManager.Theme.LUDUS);

        appendLog("JavaFX build started.");
        appendLog("Create a gladiator, then use the buttons to advance the campaign.");
        battleSceneView.setBattleMessage("Create a gladiator, then choose your path.");
        setScreen(ScreenState.START);
        refresh();
    }

    private VBox createNewGamePanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("side-panel");
        panel.setPrefWidth(270);

        Label title = new Label("Gladiator");
        title.getStyleClass().add("section-title");

        nameField = new TextField("Marcus");
        nameField.setPromptText("Name");

        classBox = new ComboBox<GladiatorClass>();
        classBox.getItems().addAll(GladiatorClass.values());
        classBox.setConverter(new StringConverter<GladiatorClass>() {
            @Override
            public String toString(GladiatorClass gladiatorClass) {
                return classDisplayName(gladiatorClass);
            }

            @Override
            public GladiatorClass fromString(String text) {
                return GladiatorClass.valueOf(text.toUpperCase());
            }
        });
        classBox.getSelectionModel().select(GladiatorClass.MURMILLO);
        classBox.setMaxWidth(Double.MAX_VALUE);
        classPreviewLabel = new Label();
        classPreviewLabel.getStyleClass().add("class-preview-card");
        classPreviewLabel.setWrapText(true);
        classPreviewLabel.setMaxWidth(Double.MAX_VALUE);
        updateClassPreview(GladiatorClass.MURMILLO);
        classBox.valueProperty().addListener((observable, oldClass, newClass) -> {
            updateClassPreview(newClass);
            if (battleSceneView != null && !session.hasPlayer()) {
                battleSceneView.setPreviewClass(newClass);
                refresh();
            }
        });

        difficultyBox = new ComboBox<>();
        difficultyBox.getItems().addAll(Difficulty.values());
        difficultyBox.getSelectionModel().select(loadPreferredDifficulty());
        difficultyBox.setMaxWidth(Double.MAX_VALUE);
        difficultyPreviewLabel = new Label(difficultyBox.getValue().getDescription());
        difficultyPreviewLabel.getStyleClass().add("class-preview-card");
        difficultyPreviewLabel.setWrapText(true);
        difficultyPreviewLabel.setMaxWidth(Double.MAX_VALUE);
        difficultyBox.valueProperty().addListener((observable, oldDifficulty, newDifficulty) -> {
            if (newDifficulty != null) {
                difficultyPreviewLabel.setText(newDifficulty.getDescription());
                SETTINGS.put("lastDifficulty", newDifficulty.name());
            }
        });

        newGameButton = new Button("New Game");
        newGameButton.setMaxWidth(Double.MAX_VALUE);
        newGameButton.setOnAction(event -> {
            session.startNewGame(nameField.getText(), classBox.getValue(), difficultyBox.getValue());
            lockCharacterCreation();
            appendLog("A new gladiator enters the ludus.");
            battleSceneView.setBattleMessage("New gladiator ready.");
            setScreen(ScreenState.CAMPAIGN);
            refresh();
        });

        Button load1 = new Button("Load Slot 1");
        load1.setMaxWidth(Double.MAX_VALUE);
        load1.setOnAction(event -> {
            loadSlot(1);
        });

        Button load2 = new Button("Load Slot 2");
        load2.setMaxWidth(Double.MAX_VALUE);
        load2.setOnAction(event -> {
            loadSlot(2);
        });

        Button load3 = new Button("Load Slot 3");
        load3.setMaxWidth(Double.MAX_VALUE);
        load3.setOnAction(event -> {
            loadSlot(3);
        });

        panel.getChildren().addAll(title, new Label("Name"), nameField,
                new Label("Class"), classBox, classPreviewLabel,
                new Label("Difficulty"), difficultyBox, difficultyPreviewLabel, newGameButton);
        return panel;
    }

    private void updateClassPreview(GladiatorClass gladiatorClass) {
        if (classPreviewLabel == null || gladiatorClass == null) return;
        Player preview = new Player("Preview", gladiatorClass);
        classPreviewLabel.setText(
                classDisplayName(gladiatorClass) + " — " + classPlaystyle(gladiatorClass)
                        + "\n\nHP " + preview.getMaxHp() + "  |  STA " + preview.getMaxStamina()
                        + "\nSTR " + preview.getStrength() + "  |  DEF " + preview.getDefense()
                        + "\n\nSpecial: " + classSpecial(gladiatorClass)
                        + "\nPassive: " + classPassive(gladiatorClass));
    }

    private Difficulty loadPreferredDifficulty() {
        try {
            return Difficulty.valueOf(SETTINGS.get("lastDifficulty", Difficulty.STANDARD.name()));
        } catch (IllegalArgumentException exception) {
            return Difficulty.STANDARD;
        }
    }

    private String classDisplayName(GladiatorClass gladiatorClass) {
        if (gladiatorClass == null) return "Choose Class";
        String name = gladiatorClass.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String classPlaystyle(GladiatorClass gladiatorClass) {
        switch (gladiatorClass) {
            case MURMILLO: return "Defensive";
            case RETIARIUS: return "Tactical Control";
            case DIMACHAERUS: return "Glass Cannon";
            case THRAEX: return "Armor Breaker";
            default: return "Balanced";
        }
    }

    private String classSpecial(GladiatorClass gladiatorClass) {
        switch (gladiatorClass) {
            case MURMILLO: return "Shield Bash — attack and brace";
            case RETIARIUS: return "Net Cast — trap and drain stamina";
            case DIMACHAERUS: return "Twin Blades — two fast strikes";
            case THRAEX: return "Hook Slash — cuts through defense";
            default: return "None";
        }
    }

    private String classPassive(GladiatorClass gladiatorClass) {
        switch (gladiatorClass) {
            case MURMILLO: return "Stronger guard and counterattack";
            case RETIARIUS: return "Recovers more stamina with Catch Breath";
            case DIMACHAERUS: return "Chance to follow Attack with an extra blade";
            case THRAEX: return "Bonus damage against high Defense";
            default: return "None";
        }
    }

    private HBox createHeaderPanel() {
        HBox headerPanel = new HBox(10);
        headerPanel.getStyleClass().add("header-panel");

        headerLabel = new Label("The Unbroken: Gladiator's Rise");
        headerLabel.getStyleClass().add("header-label");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(headerLabel, Priority.ALWAYS);

        statsToggleButton = new Button("Stats");
        statsToggleButton.getStyleClass().add("utility-button");
        statsToggleButton.setOnAction(event -> toggleStatsPanel());

        Button audioButton = new Button("Audio");
        audioButton.getStyleClass().add("utility-button");
        audioButton.setOnAction(event -> showAudioSettings());

        viewToggleButton = new Button("⛶ Expand View");
        viewToggleButton.getStyleClass().addAll("utility-button", "view-toggle-button");
        viewToggleButton.setVisible(false);
        viewToggleButton.setManaged(false);
        viewToggleButton.setOnAction(event -> toggleExpandedView());

        headerPanel.getChildren().addAll(headerLabel, viewToggleButton, audioButton, statsToggleButton);
        return headerPanel;
    }

    private void showAudioSettings() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Audio Settings");
        dialog.setHeaderText("Music and effects");

        Slider musicSlider = volumeSlider(audioManager.getMusicVolume());
        musicSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                audioManager.setMusicVolume(newValue.doubleValue() / 100.0));

        Slider effectsSlider = volumeSlider(audioManager.getEffectsVolume());
        effectsSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                audioManager.setEffectsVolume(newValue.doubleValue() / 100.0));

        CheckBox mute = new CheckBox("Mute all audio");
        mute.setSelected(audioManager.isMuted());
        mute.selectedProperty().addListener((observable, oldValue, newValue) ->
                audioManager.setMuted(newValue));

        VBox content = new VBox(10,
                new Label("Music volume"), musicSlider,
                new Label("Effects volume"), effectsSlider,
                mute);
        content.setPrefWidth(320);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private Slider volumeSlider(double value) {
        Slider slider = new Slider(0, 100, value * 100.0);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(25);
        slider.setBlockIncrement(5);
        return slider;
    }

    private VBox createCenterPanel() {
        centerPanel = new VBox(10);
        centerPanel.getStyleClass().add("center-panel");

        battleSceneView = new BattleSceneView();
        VBox.setVgrow(battleSceneView.getRoot(), Priority.ALWAYS);

        commandPanelView = new CommandPanelView(session,
                this::setScreen,
                this::perform,
                this::performBattleAction,
                this::loadSlot,
                () -> logArea.clear(),
                this::startNewCareerSetup);
        commandScroll = new ScrollPane(commandPanelView.getRoot());
        commandScroll.getStyleClass().add("command-scroll");
        commandScroll.setFitToWidth(true);
        commandScroll.setMinWidth(280);
        commandScroll.setMaxWidth(Double.MAX_VALUE);

        historyCommandPane = new SplitPane();
        historyCommandPane.getStyleClass().add("bottom-panel");
        logArea.setPrefRowCount(5);
        logArea.setVisible(false);
        logArea.setManaged(false);
        historyPanel = createLogPanel();
        historyPanel.setMinWidth(180);
        historyPanel.setMaxWidth(Double.MAX_VALUE);
        prepareHistoryCommandSplit();

        applyScreenLayout();
        return centerPanel;
    }

    private VBox createLogPanel() {
        VBox logPanel = new VBox(6);
        logPanel.getStyleClass().add("log-panel");
        logPanel.setMinHeight(170);
        logPanel.setPrefHeight(230);
        logPanel.setMaxHeight(260);

        Label historyTitle = new Label("History");
        historyTitle.getStyleClass().add("panel-title");
        logToggleButton = new Button("Open Log");
        logToggleButton.getStyleClass().add("utility-button");
        logToggleButton.setOnAction(event -> toggleFullLog());

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(historyTitle, Priority.ALWAYS);
        titleRow.getChildren().addAll(historyTitle, logToggleButton);

        historyPreviewLabel = new Label("No events yet.");
        historyPreviewLabel.getStyleClass().add("history-preview");
        historyPreviewLabel.setWrapText(true);
        historyPreviewLabel.setMaxWidth(Double.MAX_VALUE);

        VBox.setVgrow(logArea, Priority.ALWAYS);

        logPanel.getChildren().addAll(titleRow, historyPreviewLabel, logArea);
        return logPanel;
    }

    private void toggleStatsPanel() {
        statsVisible = !statsVisible;
        statsPanelView.getRoot().setVisible(statsVisible);
        statsPanelView.getRoot().setManaged(statsVisible);
        statsToggleButton.setText(statsVisible ? "Hide Stats" : "Stats");
        refresh();
    }

    private void toggleFullLog() {
        fullLogVisible = !fullLogVisible;
        logArea.setVisible(fullLogVisible);
        logArea.setManaged(fullLogVisible);
        historyPreviewLabel.setVisible(!fullLogVisible);
        historyPreviewLabel.setManaged(!fullLogVisible);
        logToggleButton.setText(fullLogVisible ? "Close Log" : "Open Log");
    }

    private void perform(String message) {
        if (message == null || message.isEmpty()) {
            refresh();
            syncScreenWithGameState();
            return;
        }

        appendLog(message);
        battleSceneView.setBattleMessage(conciseBattleMessage(message));
        refresh();
        syncScreenWithGameState();
    }

    private void performBattleAction(Supplier<String> action) {
        int playerHpBefore = session.hasPlayer() ? session.getPlayer().getHp() : 0;
        int playerStaBefore = session.hasPlayer() ? session.getPlayer().getStamina() : 0;
        int enemyHpBefore = session.getCurrentEnemy() == null ? 0 : session.getCurrentEnemy().getHp();
        int enemyStaBefore = session.getCurrentEnemy() == null ? 0 : session.getCurrentEnemy().getStamina();
        String enemyName = session.getCurrentEnemy() == null ? "" : session.getCurrentEnemy().getName();

        String message = action.get();

        int playerHpAfter = session.hasPlayer() ? session.getPlayer().getHp() : 0;
        int playerStaAfter = session.hasPlayer() ? session.getPlayer().getStamina() : 0;
        int enemyHpAfter = session.getCurrentEnemy() == null ? 0 : session.getCurrentEnemy().getHp();
        int enemyStaAfter = session.getCurrentEnemy() == null
                ? enemyStaBefore : session.getCurrentEnemy().getStamina();
        BattleLogParts logParts = splitBattleLog(message, enemyName);
        playBattleEffects(message, enemyHpBefore, enemyHpAfter, playerHpBefore, playerHpAfter);
        String roundSummary = "Round summary — You: " + signedChange(playerHpAfter - playerHpBefore)
                + " HP, " + signedChange(playerStaAfter - playerStaBefore) + " STA | Enemy: "
                + signedChange(enemyHpAfter - enemyHpBefore) + " HP, "
                + signedChange(enemyStaAfter - enemyStaBefore) + " STA";

        appendLog(logParts.playerText);
        battleSceneView.setBattleMessage(conciseBattleMessage(logParts.playerText));
        refresh();
        battleSceneView.playActionMotion(logParts.playerText);

        if (enemyHpAfter < enemyHpBefore) {
            battleSceneView.playEnemyHitFeedback();
        }

        if (logParts.enemyText.isEmpty()) {
            if (session.isInBattle()) {
                appendLog(roundSummary);
                battleSceneView.setBattleMessage(roundSummary);
            }
            if (playerHpAfter < playerHpBefore) {
                audioManager.playEffect(AudioManager.Effect.WEAPON_IMPACT);
                battleSceneView.playPlayerHitFeedback();
            }
            syncScreenWithGameState();
            return;
        }

        commandPanelView.getRoot().setDisable(true);
        PauseTransition enemyDelay = new PauseTransition(Duration.millis(650));
        enemyDelay.setOnFinished(event -> {
            String enemyReport = logParts.enemyText + "\n" + roundSummary;
            appendLog(enemyReport);
            battleSceneView.setEnemyBattleMessage(roundSummary);
            refresh();
            battleSceneView.playEnemyActionMotion(logParts.enemyText);

            if (playerHpAfter < playerHpBefore) {
                audioManager.playEffect(AudioManager.Effect.WEAPON_IMPACT);
                battleSceneView.playPlayerHitFeedback();
            }

            commandPanelView.getRoot().setDisable(false);
            syncScreenWithGameState();
        });
        enemyDelay.play();
    }

    private void playBattleEffects(String message, int enemyHpBefore, int enemyHpAfter,
                                   int playerHpBefore, int playerHpAfter) {
        if (enemyHpAfter < enemyHpBefore) {
            audioManager.playEffect(AudioManager.Effect.WEAPON_IMPACT);
        }

        String lowerMessage = message == null ? "" : message.toLowerCase();
        if (lowerMessage.contains("is poisoned") || lowerMessage.contains("poison strike")) {
            audioManager.playEffect(AudioManager.Effect.POISON);
        }

        if (!session.hasBattleSummary()) return;
        BattleSummary summary = session.getLastBattleSummaryInfo();
        if (summary.getOutcome() == BattleSummary.Outcome.VICTORY) {
            audioManager.playEffect(AudioManager.Effect.CROWD_CHEER);
            audioManager.playEffect(AudioManager.Effect.VICTORY);
        } else {
            audioManager.playEffect(AudioManager.Effect.DEFEAT);
        }
    }

    private String signedChange(int change) {
        return change > 0 ? "+" + change : Integer.toString(change);
    }

    private void lockCharacterCreation() {
        nameField.setDisable(true);
        classBox.setDisable(true);
        difficultyBox.setDisable(true);
        newGameButton.setDisable(true);
    }

    private void setScreen(ScreenState nextState) {
        if (nextState != screenState) expandedView = false;
        screenState = nextState;
        applyScreenLayout();
        updateMusicTheme();
        refreshScreenState();
        refresh();
    }

    private void applyScreenLayout() {
        if (centerPanel == null || historyCommandPane == null) return;

        boolean canExpand = canExpandCurrentView();
        viewToggleButton.setVisible(canExpand);
        viewToggleButton.setManaged(canExpand);
        viewToggleButton.setText(expandedView ? "↙ Restore View" : "⛶ Expand View");
        centerPanel.getChildren().clear();

        if (expandedView && canExpand) {
            historyCommandPane.getItems().clear();
            commandScroll.setMinHeight(0);
            commandScroll.setPrefHeight(0);
            commandScroll.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(commandScroll, Priority.ALWAYS);
            centerPanel.getChildren().add(commandScroll);
            return;
        }

        expandedView = false;
        prepareHistoryCommandSplit();
        historyCommandPane.setMinHeight(170);
        historyCommandPane.setPrefHeight(230);
        historyCommandPane.setMaxHeight(260);
        commandScroll.setMinHeight(170);
        commandScroll.setPrefHeight(230);
        commandScroll.setMaxHeight(260);
        VBox.setVgrow(battleSceneView.getRoot(), Priority.ALWAYS);
        VBox.setVgrow(historyCommandPane, Priority.NEVER);
        centerPanel.getChildren().addAll(battleSceneView.getRoot(), historyCommandPane);
    }

    private void toggleExpandedView() {
        if (!canExpandCurrentView()) return;
        expandedView = !expandedView;
        applyScreenLayout();
    }

    private boolean canExpandCurrentView() {
        return screenState == ScreenState.SHOP
                || screenState == ScreenState.TRAINING
                || screenState == ScreenState.CAREER
                || screenState == ScreenState.ITEMS
                || screenState == ScreenState.SAVES;
    }

    private void prepareHistoryCommandSplit() {
        if (historyCommandPane.getItems().size() == 2
                && historyCommandPane.getItems().get(0) == historyPanel
                && historyCommandPane.getItems().get(1) == commandScroll) return;

        historyCommandPane.getItems().setAll(historyPanel, commandScroll);
        double savedDividerPosition = SETTINGS.getDouble("bottomDividerPosition", 0.42);
        historyCommandPane.setDividerPositions(Math.max(0.18, Math.min(0.75, savedDividerPosition)));
        historyCommandPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) ->
                SETTINGS.putDouble("bottomDividerPosition", newValue.doubleValue()));
    }

    private void updateMusicTheme() {
        if (audioManager == null) return;
        if (nextFightUsesChampionTheme()) {
            audioManager.playTheme(AudioManager.Theme.CHAMPION);
        } else if (screenState == ScreenState.BATTLE || screenState == ScreenState.PRE_FIGHT
                || screenState == ScreenState.ARENA || screenState == ScreenState.BATTLE_RESULT) {
            audioManager.playTheme(AudioManager.Theme.ARENA);
        } else {
            audioManager.playTheme(AudioManager.Theme.LUDUS);
        }
    }

    private boolean nextFightUsesChampionTheme() {
        if (screenState != ScreenState.BATTLE && screenState != ScreenState.PRE_FIGHT) return false;
        if (session.getCurrentEnemy() == null) return false;
        return session.getCurrentEnemy() instanceof Rival
                || "Aurelius the Unbroken".equals(session.getCurrentEnemy().getName());
    }

    @Override
    public void stop() {
        if (audioManager != null) audioManager.dispose();
    }

    private void syncScreenWithGameState() {
        if (!session.hasPlayer()) {
            setScreen(ScreenState.START);
        } else if (session.isGameOver()) {
            setScreen(ScreenState.GAME_OVER);
        } else if (session.isInBattle()) {
            setScreen(ScreenState.BATTLE);
        } else if (session.shouldShowFreedomEnding()) {
            setScreen(ScreenState.FREEDOM_ENDING);
        } else if (session.hasChampionEnding()) {
            setScreen(ScreenState.CHAMPION_ENDING);
        } else if (session.hasBattleSummary()) {
            setScreen(ScreenState.BATTLE_RESULT);
        } else if (screenState == ScreenState.BATTLE || screenState == ScreenState.START) {
            setScreen(ScreenState.CAMPAIGN);
        } else {
            refreshScreenState();
        }
    }

    private void startNewCareerSetup() {
        session.returnToNewCareer();
        nameField.setDisable(false);
        classBox.setDisable(false);
        difficultyBox.setDisable(false);
        difficultyBox.getSelectionModel().select(loadPreferredDifficulty());
        newGameButton.setDisable(false);
        nameField.setText("Marcus");
        logArea.clear();
        historyPreviewLabel.setText("Choose a name and class for a new gladiator.");
        battleSceneView.setBattleMessage("Create a new gladiator.");
        setScreen(ScreenState.START);
    }

    private void refreshScreenState() {
        if (commandPanelView == null) {
            return;
        }

        boolean hasPlayer = session.hasPlayer();
        newGamePanel.setVisible(!hasPlayer);
        newGamePanel.setManaged(!hasPlayer);
        commandPanelView.show(screenState);
    }

    private void loadSlot(int slot) {
        appendLog(session.load(slot));
        battleSceneView.setBattleMessage(session.hasPlayer() ? "Gladiator loaded." : "Could not load that slot.");

        if (session.hasPlayer()) {
            lockCharacterCreation();
            setScreen(ScreenState.CAMPAIGN);
        } else {
            setScreen(ScreenState.START);
        }

        refresh();
    }

    private void refresh() {
        headerLabel.setText(session.getHeaderText());
        statsPanelView.refresh(session);
        if (!session.hasPlayer() && classBox != null) {
            battleSceneView.setPreviewClass(classBox.getValue());
        }
        battleSceneView.refresh(session, getIdleSceneTitle());
    }

    private String getIdleSceneTitle() {
        switch (screenState) {
            case TRAINING:
                return "Training Grounds";
            case CAREER:
                return "Road to the Champion";
            case SHOP:
                return "Arena Market";
            case SAVES:
                return "Records Room";
            case ARENA:
                return "Arena Contracts";
            case PRE_FIGHT:
                return "Before the Gates Open";
            case BATTLE_RESULT:
                return "After the Fight";
            case CHAMPION_ENDING:
                return "The Arena Crown";
            case FREEDOM_ENDING:
                return "Freedom";
            case GAME_OVER:
                return "Game Over";
            case CAMPAIGN:
            case START:
            default:
                return "The Ludus";
        }
    }

    private void appendLog(String message) {
        historyPreviewLabel.setText(conciseBattleMessage(message));
        logArea.appendText(message + "\n\n");
        logArea.positionCaret(logArea.getLength());
    }

    private String conciseBattleMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }

        String[] lines = message.trim().split("\\R");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (!line.isEmpty() && !line.startsWith("Round ")) {
                return line;
            }
        }

        return lines[lines.length - 1].trim();
    }

    private BattleLogParts splitBattleLog(String message, String enemyName) {
        if (message == null || message.trim().isEmpty() || enemyName == null || enemyName.isEmpty()) {
            return new BattleLogParts(message == null ? "" : message, "");
        }

        String[] lines = message.trim().split("\\R");
        int enemyStart = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith(enemyName)) {
                enemyStart = i;
                break;
            }
        }

        if (enemyStart <= 0) {
            return new BattleLogParts(message, "");
        }

        StringBuilder playerText = new StringBuilder();
        StringBuilder enemyText = new StringBuilder();

        for (int i = 0; i < enemyStart; i++) {
            appendLine(playerText, lines[i]);
        }

        for (int i = enemyStart; i < lines.length; i++) {
            appendLine(enemyText, lines[i]);
        }

        return new BattleLogParts(playerText.toString(), enemyText.toString());
    }

    private void appendLine(StringBuilder builder, String line) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(line);
    }

    private static class BattleLogParts {
        private final String playerText;
        private final String enemyText;

        BattleLogParts(String playerText, String enemyText) {
            this.playerText = playerText == null ? "" : playerText;
            this.enemyText = enemyText == null ? "" : enemyText;
        }
    }

}
