package arena.fx;

import arena.engine.GameSession;
import arena.engine.BattleActionInfo;
import arena.engine.FightContractInfo;
import arena.engine.ShopOffer;
import arena.engine.TrainingOptionInfo;
import arena.saves.SaveSlotInfo;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.ColumnConstraints;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

class CommandPanelView {

    private final GameSession session;
    private final Consumer<ScreenState> screenSetter;
    private final Consumer<String> performer;
    private final Consumer<Supplier<String>> battlePerformer;
    private final IntConsumer slotLoader;
    private final Runnable logClearer;
    private final Runnable newCareerStarter;
    private final VBox root = new VBox(8);

    CommandPanelView(GameSession session,
                     Consumer<ScreenState> screenSetter,
                     Consumer<String> performer,
                     Consumer<Supplier<String>> battlePerformer,
                     IntConsumer slotLoader,
                     Runnable logClearer,
                     Runnable newCareerStarter) {
        this.session = session;
        this.screenSetter = screenSetter;
        this.performer = performer;
        this.battlePerformer = battlePerformer;
        this.slotLoader = slotLoader;
        this.logClearer = logClearer;
        this.newCareerStarter = newCareerStarter;

        root.getStyleClass().add("command-panel");
        root.setMinWidth(0);
        root.setPrefWidth(600);
    }

    VBox getRoot() {
        return root;
    }

    void show(ScreenState screenState) {
        switch (screenState) {
            case START:
                showStartCommands();
                break;
            case TRAINING:
                showTrainingCommands();
                break;
            case CAREER:
                showCareerCommands();
                break;
            case SHOP:
                showShopCommands();
                break;
            case ARENA:
                showArenaCommands();
                break;
            case PRE_FIGHT:
                showPreFightCommands();
                break;
            case ITEMS:
                showItemsCommands();
                break;
            case SAVES:
                showSaveCommands();
                break;
            case BATTLE:
                showBattleCommands();
                break;
            case BATTLE_RESULT:
                showBattleResultCommands();
                break;
            case CHAMPION_ENDING:
                showChampionEndingCommands();
                break;
            case FREEDOM_ENDING:
                showFreedomEndingCommands();
                break;
            case GAME_OVER:
                showGameOverCommands();
                break;
            case CAMPAIGN:
            default:
                showMainCommands();
                break;
        }
    }

    private void showMainCommands() {
        root.getChildren().clear();
        root.getChildren().addAll(
                commandTitle("Commands"),
                tutorialLabel(session.consumeMainTutorialTip()),
                guidanceLabel(session.getDayGuidanceText()),
                commandGrid(
                menuButton("Arena", () -> screenSetter.accept(ScreenState.ARENA)),
                menuButton("Training", () -> screenSetter.accept(ScreenState.TRAINING)),
                menuButton("Shop", () -> screenSetter.accept(ScreenState.SHOP)),
                menuButton("Career", () -> screenSetter.accept(ScreenState.CAREER)),
                commandButton("Rest One Day\nAdvances day", () -> session.restOneDay()),
                commandButton("Rest Until Healed\nMay advance several days", () -> session.restUntilHealed()),
                menuButton("Saves", () -> screenSetter.accept(ScreenState.SAVES)),
                commandButton("Stats", () -> session.getStatsText()),
                clearLogButton()
                )
        );
    }

    private void showCareerCommands() {
        root.getChildren().clear();

        ProgressBar progress = new ProgressBar(session.isChampionDefeated()
                ? session.getFreedomProgress() : session.getChampionProgress());
        progress.getStyleClass().add("career-progress-bar");
        progress.setMaxWidth(Double.MAX_VALUE);

        Label progressText = resultLabel(session.getCareerProgressText());
        progressText.getStyleClass().add("career-progress-text");

        Button freedomButton = commandButton("Buy Your Freedom\n" + GameSession.FREEDOM_PRICE + " Gold",
                () -> session.buyFreedom(), session.canBuyFreedom());
        freedomButton.setVisible(session.isChampionDefeated() && !session.isFreedomPurchased());
        freedomButton.setManaged(session.isChampionDefeated() && !session.isFreedomPurchased());

        root.getChildren().addAll(
                commandTitle(session.getCareerScreenTitle()),
                progress,
                progressText,
                freedomButton,
                backButton()
        );
    }

    private void showTrainingCommands() {
        root.getChildren().clear();
        root.getChildren().addAll(
                commandTitle("Training"),
                tutorialLabel(session.consumeTrainingTutorialTip()),
                guidanceLabel("Training advances the day. High fatigue increases injury risk."),
                commandGrid(
                trainingButton(session.getSafeDrillsInfo(), () -> session.trainSafeDrills()),
                trainingButton(session.getBrutalConditioningInfo(), () -> session.trainBrutalConditioning()),
                trainingButton(session.getEnduranceInfo(), () -> session.trainEndurance()),
                trainingButton(session.getPublicSparringInfo(), () -> session.trainPublicSparring()),
                backButton()
                )
        );
    }

    private void showArenaCommands() {
        root.getChildren().clear();
        root.getChildren().addAll(
                commandTitle("Contracts"),
                tutorialLabel(session.consumeArenaTutorialTip()),
                guidanceLabel("An arena fight advances the day and resets lanista pressure."),
                commandGrid(
                contractPreviewButton(session.getMeasuredBoutInfo(), () -> session.prepareMeasuredBout()),
                contractPreviewButton(session.getBloodPriceInfo(), () -> session.prepareBloodPrice()),
                contractPreviewButton(session.getRivalChallengeInfo(), () -> session.prepareRivalChallenge()),
                championMatchButton(),
                backButton()
                )
        );
    }

    private void showPreFightCommands() {
        root.getChildren().clear();
        Label preview = resultLabel(session.getPreFightInfo().getText());
        preview.getStyleClass().add("pre-fight-preview");
        root.getChildren().addAll(
                commandTitle("Before the Gates Open"),
                preview,
                commandGrid(
                        commandButton("Enter Arena", () -> session.confirmFightPreview()),
                        menuButton("Choose Another Contract", () -> {
                            session.clearFightPreview();
                            screenSetter.accept(ScreenState.ARENA);
                        })
                )
        );
    }

    private void showShopCommands() {
        root.getChildren().clear();
        root.getChildren().addAll(
                commandTitle(session.getShopHeaderText()),
                guidanceLabel("Gear and stamina meals advance the day. Consumables do not."),
                shopRow(session.getWeaponOffer(), () -> session.buyWeapon()),
                shopRow(session.getArmorOffer(), () -> session.buyArmor()),
                shopRow(session.getStaminaMealOffer(), () -> session.buyStaminaMeal()),
                shopRow(session.getHealingSalveOffer(), () -> session.buyHealingSalve()),
                shopRow(session.getStaminaDraughtOffer(), () -> session.buyStaminaDraught()),
                shopRow(session.getAntidoteOffer(), () -> session.buyAntidote()),
                shopRow(session.getWhetstoneOffer(), () -> session.buyWhetstone()),
                backButton()
        );
    }

    private void showBattleCommands() {
        root.getChildren().clear();
        root.getChildren().addAll(commandTitle("Battle"),
                tutorialLabel(session.consumeBattleTutorialTip()), battleGrid(
                battleCommandButton(session.getAttackInfo(), () -> session.battleAttack()),
                battleCommandButton(session.getHeavyAttackInfo(), () -> session.battleHeavyAttack()),
                battleCommandButton(session.getSpecialInfo(), () -> session.battleSpecial()),
                battleCommandButton(session.getDefendInfo(), () -> session.battleDefend()),
                battleCommandButton(session.getCatchBreathInfo(), () -> session.battleRest()),
                menuButton("Items", () -> screenSetter.accept(ScreenState.ITEMS)),
                battleCommandButton(session.getForfeitInfo(), () -> session.battleForfeit())
        ));
    }

    private void showItemsCommands() {
        root.getChildren().clear();
        root.getChildren().addAll(commandTitle("Items"), battleGrid(
                battleCommandButton(session.getHealingSalveInfo(), () -> session.battleUseHealingSalve()),
                battleCommandButton(session.getStaminaDraughtInfo(), () -> session.battleUseStaminaDraught()),
                battleCommandButton(session.getAntidoteInfo(), () -> session.battleUseAntidote()),
                battleCommandButton(session.getWhetstoneInfo(), () -> session.battleUseWhetstone()),
                menuButton("Back", () -> screenSetter.accept(ScreenState.BATTLE))
        ));
    }

    private void showSaveCommands() {
        root.getChildren().clear();
        SaveSlotInfo slot1 = session.getSaveSlotInfo(1);
        SaveSlotInfo slot2 = session.getSaveSlotInfo(2);
        SaveSlotInfo slot3 = session.getSaveSlotInfo(3);
        root.getChildren().addAll(
                commandTitle("Saves"),
                saveSlotRow(slot1),
                saveSlotRow(slot2),
                saveSlotRow(slot3),
                commandGrid(
                loadButton(slot1, () -> {
                    slotLoader.accept(1);
                    return "";
                }),
                loadButton(slot2, () -> {
                    slotLoader.accept(2);
                    return "";
                }),
                loadButton(slot3, () -> {
                    slotLoader.accept(3);
                    return "";
                }),
                backButton())
        );
    }

    private HBox saveSlotRow(SaveSlotInfo info) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Button saveButton = new Button("Save " + info.getSlot() + "\n" + formatSaveSlot(info));
        saveButton.getStyleClass().add("command-button");
        configureCommandButton(saveButton);
        saveButton.setOnAction(event -> saveWithConfirmation(info));
        HBox.setHgrow(saveButton, Priority.ALWAYS);

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("command-button");
        configureCommandButton(deleteButton);
        deleteButton.setDisable(info.getStatus() == SaveSlotInfo.Status.EMPTY);
        deleteButton.setOnAction(event -> confirmDeleteSave(info));
        row.getChildren().addAll(saveButton, deleteButton);
        return row;
    }

    private void saveWithConfirmation(SaveSlotInfo info) {
        if (info.getStatus() == SaveSlotInfo.Status.EMPTY) {
            performer.accept(session.save(info.getSlot()));
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Overwrite Save");
        confirmation.setHeaderText("Overwrite save slot " + info.getSlot() + "?");
        confirmation.setContentText(formatSaveSlot(info)
                + "\n\nThe previous save in this slot will be replaced.");
        confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);
        confirmation.showAndWait().ifPresent(choice -> {
            if (choice == ButtonType.YES) performer.accept(session.save(info.getSlot()));
        });
    }

    private void confirmDeleteSave(SaveSlotInfo info) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Save");
        confirmation.setHeaderText("Delete save slot " + info.getSlot() + "?");
        confirmation.setContentText(formatSaveSlot(info)
                + "\n\nThis career cannot be recovered after deletion.");
        confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);

        confirmation.showAndWait().ifPresent(choice -> {
            if (choice == ButtonType.YES) {
                performer.accept(session.deleteSave(info.getSlot()));
                showSaveCommands();
            }
        });
    }

    private void showBattleResultCommands() {
        root.getChildren().clear();
        root.getChildren().addAll(
                commandTitle("Result"),
                menuButton("Continue", () -> {
                    session.clearBattleSummary();
                    screenSetter.accept(ScreenState.CAMPAIGN);
                })
        );
    }

    private void showChampionEndingCommands() {
        root.getChildren().clear();
        Label ending = resultLabel(session.getChampionEndingText());
        ending.getStyleClass().add("champion-ending-card");
        root.getChildren().addAll(
                commandTitle("Arena Champion"),
                ending,
                commandGrid(
                        menuButton("Continue as Champion", () -> {
                            session.continueAsChampion();
                            screenSetter.accept(ScreenState.CAMPAIGN);
                        }),
                        menuButton("New Career", newCareerStarter)
                )
        );
    }

    private void showFreedomEndingCommands() {
        root.getChildren().clear();
        Label ending = resultLabel(session.getFreedomEndingText());
        ending.getStyleClass().add("freedom-ending-card");
        root.getChildren().addAll(
                commandTitle("Freedom"),
                ending,
                commandGrid(
                        menuButton("Continue by Choice", () -> {
                            session.acknowledgeFreedomEnding();
                            screenSetter.accept(ScreenState.CAMPAIGN);
                        }),
                        menuButton("New Career", newCareerStarter)
                )
        );
    }

    private void showStartCommands() {
        root.getChildren().clear();
        root.getChildren().addAll(
                commandTitle("Start"),
                commandGrid(
                menuButton("Load Slot 1", () -> slotLoader.accept(1)),
                menuButton("Load Slot 2", () -> slotLoader.accept(2)),
                menuButton("Load Slot 3", () -> slotLoader.accept(3)),
                clearLogButton()
                )
        );
    }

    private void showGameOverCommands() {
        root.getChildren().clear();
        root.getChildren().addAll(
                commandTitle("Game Over"),
                commandGrid(
                menuButton("Load Slot 1", () -> slotLoader.accept(1)),
                menuButton("Load Slot 2", () -> slotLoader.accept(2)),
                menuButton("Load Slot 3", () -> slotLoader.accept(3)),
                clearLogButton()
                )
        );
    }

    private Label commandTitle(String text) {
        Label title = new Label(text);
        title.getStyleClass().add("command-title");
        return title;
    }

    private Button commandButton(String text, Supplier<String> action) {
        return commandButton(text, action, true);
    }

    private Button commandButton(String text, Supplier<String> action, boolean enabled) {
        Button button = actionButton(text, action);
        button.getStyleClass().add("command-button");
        button.setDisable(!enabled);
        return button;
    }

    private Button trainingButton(TrainingOptionInfo info, Supplier<String> action) {
        return commandButton(info.getName() + "\n" + info.getReward() + " | " + info.getRisk(), action);
    }

    private Button contractButton(FightContractInfo info, Supplier<String> action) {
        String detail = info.getGoldPercent() + "% gold | " + info.getFamePercent() + "% fame";
        if (info.isRivalChallenge()) {
            detail += " | rival";
        }

        return commandButton(info.getName() + "\n" + info.getDescription() + "\n" + detail, action);
    }

    private Button contractPreviewButton(FightContractInfo info, Supplier<String> action) {
        Button button = contractButton(info, action);
        button.setOnAction(event -> {
            performer.accept(action.get());
            if (session.hasFightPreview()) screenSetter.accept(ScreenState.PRE_FIGHT);
        });
        return button;
    }

    private Button championMatchButton() {
        String text = session.isChampionDefeated()
                ? "Champion Defeated\nArena crown claimed"
                : "Champion Match\nRequires 300 Fame";
        Button button = commandButton(text, () -> session.prepareChampionMatch(),
                session.isChampionMatchUnlocked());
        button.setOnAction(event -> {
            performer.accept(session.prepareChampionMatch());
            if (session.hasFightPreview()) screenSetter.accept(ScreenState.PRE_FIGHT);
        });
        return button;
    }

    private Button loadButton(SaveSlotInfo info, Supplier<String> action) {
        return commandButton("Load " + info.getSlot() + "\n" + formatSaveSlot(info), action, info.isLoadable());
    }

    private String formatSaveSlot(SaveSlotInfo info) {
        switch (info.getStatus()) {
            case FILLED:
                return info.getPlayerName() + " | Day " + info.getDayText() + " | " + info.getRank();
            case DAMAGED:
                return "Damaged save";
            case EMPTY:
            default:
                return "Empty";
        }
    }

    private HBox shopRow(ShopOffer offer, Supplier<String> action) {
        HBox row = new HBox(8);
        row.getStyleClass().add("shop-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Label details = resultLabel(formatOffer(offer));
        HBox.setHgrow(details, Priority.ALWAYS);

        Button buyButton = new Button("Buy");
        buyButton.getStyleClass().add("buy-button");
        configureCommandButton(buyButton);
        buyButton.setDisable(!offer.isBuyable());
        buyButton.setOnAction(event -> performer.accept(action.get()));

        row.getChildren().addAll(details, buyButton);
        return row;
    }

    private String formatOffer(ShopOffer offer) {
        String text = offer.getName();

        if (!offer.getDescription().isEmpty()) {
            text += "\n" + offer.getDescription() + " | " + offer.getPrice() + " Gold";
        }

        if (offer.getOwned() >= 0) {
            text += "\nOwned: " + offer.getOwned();
        }

        switch (offer.getStatus()) {
            case AVAILABLE:
                return text + "\nAffordable";
            case NOT_ENOUGH_GOLD:
                return text + "\nNot enough gold";
            case LOCKED:
                return text + "\nLocked: Requires " + offer.getFameRequirement() + " Fame";
            case OWNED_BEST:
            default:
                return text;
        }
    }

    private Label resultLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("result-label");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private Label guidanceLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("day-guidance");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private Label tutorialLabel(String text) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().add("tutorial-tip");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        boolean visible = text != null && !text.isEmpty();
        label.setVisible(visible);
        label.setManaged(visible);
        return label;
    }

    private Button battleCommandButton(BattleActionInfo info, Supplier<String> action) {
        Button button = new Button(formatBattleAction(info));
        button.getStyleClass().addAll("command-button", "battle-command-button");
        configureCommandButton(button);
        button.setDisable(!info.isEnabled());
        button.setOnAction(event -> battlePerformer.accept(action));
        return button;
    }

    private GridPane battleGrid(Button... buttons) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("battle-command-grid");
        grid.setHgap(8);
        grid.setVgap(8);
        addEqualColumns(grid, 4);

        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setHgrow(button, Priority.ALWAYS);
            GridPane.setVgrow(button, Priority.ALWAYS);
            grid.add(button, i % 4, i / 4);
        }

        return grid;
    }

    private GridPane commandGrid(Button... buttons) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("battle-command-grid");
        grid.setHgap(8);
        grid.setVgap(8);
        addEqualColumns(grid, 4);

        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            button.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(button, Priority.ALWAYS);
            grid.add(button, i % 4, i / 4);
        }

        return grid;
    }

    private void addEqualColumns(GridPane grid, int count) {
        for (int i = 0; i < count; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / count);
            grid.getColumnConstraints().add(column);
        }
    }

    private String formatBattleAction(BattleActionInfo info) {
        String text = info.getName();

        if (!info.getDetail().isEmpty()) {
            text += "\n" + info.getDetail();
        }

        if (!info.isEnabled() && !info.getDisabledReason().isEmpty()) {
            text += "\n" + info.getDisabledReason();
        }

        return text;
    }

    private Button menuButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("command-button");
        configureCommandButton(button);
        button.setOnAction(event -> action.run());
        return button;
    }

    private Button backButton() {
        return menuButton("Back", () -> screenSetter.accept(ScreenState.CAMPAIGN));
    }

    private Button actionButton(String text, Supplier<String> action) {
        Button button = new Button(text);
        configureCommandButton(button);
        button.setOnAction(event -> performer.accept(action.get()));
        HBox.setHgrow(button, Priority.ALWAYS);
        return button;
    }

    private Button clearLogButton() {
        Button button = new Button("Clear Log");
        configureCommandButton(button);
        button.setOnAction(event -> logClearer.run());
        return button;
    }

    private void configureCommandButton(Button button) {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setWrapText(true);
        button.setMinHeight(38);
    }
}
