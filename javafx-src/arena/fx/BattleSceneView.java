package arena.fx;

import arena.engine.GameSession;
import arena.engine.BattleSummary;
import arena.enums.GladiatorClass;
import arena.events.ArenaEvent;
import javafx.animation.PauseTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;

class BattleSceneView {

    private static final String CLASSIC_ARENA_BACKGROUND =
            "/arena/fx/assets/backgrounds/arena-classic-day.png";
    private static final String LUDUS_BACKGROUND =
            "/arena/fx/assets/backgrounds/ludus-training-yard.png";

    private final VBox root;
    private final AvatarRegistry avatarRegistry = new AvatarRegistry();
    private final Label sceneTitleLabel;
    private final StackPane arenaScene;
    private final ImageView arenaBackgroundImage;
    private final ImageView ludusBackgroundImage;
    private final StackPane playerSpriteNode;
    private final StackPane enemySpriteNode;
    private final ImageView playerAvatarImage;
    private final ImageView enemyAvatarImage;
    private final VBox playerShapeFigure;
    private final VBox enemyShapeFigure;
    private final Label playerSpriteLabel;
    private final Label enemySpriteLabel;
    private final Label playerBattleLabel;
    private final Label enemyBattleLabel;
    private final Label playerStatusLabel;
    private final Label enemyStatusLabel;
    private final Label battleMessageLabel;
    private final Label roundLabel;
    private final Label rivalBannerLabel;
    private final VBox resultOverlay;
    private final Label resultTitleLabel;
    private final Label resultDetailLabel;
    private final ProgressBar playerHpBar;
    private final ProgressBar playerStaminaBar;
    private final ProgressBar enemyHpBar;
    private final ProgressBar enemyStaminaBar;
    private final Label playerHpText;
    private final Label playerStaminaText;
    private final Label enemyHpText;
    private final Label enemyStaminaText;
    private final VBox enemyBox;
    private GladiatorClass previewClass = GladiatorClass.MURMILLO;

    BattleSceneView() {
        root = new VBox(10);

        sceneTitleLabel = new Label("The Ludus");
        sceneTitleLabel.getStyleClass().add("scene-title");
        sceneTitleLabel.setMaxWidth(Double.MAX_VALUE);

        arenaScene = new StackPane();
        arenaScene.getStyleClass().add("arena-scene");
        Rectangle arenaClip = new Rectangle();
        arenaClip.widthProperty().bind(arenaScene.widthProperty());
        arenaClip.heightProperty().bind(arenaScene.heightProperty());
        arenaScene.setClip(arenaClip);
        StackPane backdrop = createArenaBackdrop();
        arenaBackgroundImage = createBackgroundImage(CLASSIC_ARENA_BACKGROUND);
        ludusBackgroundImage = createBackgroundImage(LUDUS_BACKGROUND);
        if (arenaBackgroundImage != null) {
            backdrop.getChildren().add(arenaBackgroundImage);
        }
        if (ludusBackgroundImage != null) {
            backdrop.getChildren().add(ludusBackgroundImage);
        }
        arenaScene.getChildren().add(backdrop);

        HBox battlePanel = new HBox(14);
        battlePanel.getStyleClass().add("battle-panel");
        battlePanel.setAlignment(Pos.BOTTOM_CENTER);

        VBox playerBox = new VBox(-8);
        playerBox.getStyleClass().addAll("actor-panel", "player-actor-panel");
        playerSpriteNode = createSprite("player-sprite");
        playerAvatarImage = createAvatarImageView(220, 240);
        playerShapeFigure = findShapeFigure(playerSpriteNode);
        playerSpriteNode.getChildren().add(playerAvatarImage);
        playerSpriteLabel = new Label("GLADIATOR");
        playerSpriteLabel.getStyleClass().add("sprite-label");
        playerSpriteLabel.setWrapText(true);
        playerSpriteLabel.setMaxWidth(118);
        playerSpriteLabel.setVisible(false);
        playerSpriteLabel.setManaged(false);
        playerSpriteNode.getChildren().add(playerSpriteLabel);
        playerBattleLabel = new Label("Player");
        playerBattleLabel.getStyleClass().add("section-title");
        StackPane playerHpMeter = createMeter("hp-bar");
        playerHpBar = (ProgressBar) playerHpMeter.getChildren().get(0);
        playerHpText = (Label) playerHpMeter.getChildren().get(1);
        StackPane playerStaminaMeter = createMeter("stamina-bar");
        playerStaminaBar = (ProgressBar) playerStaminaMeter.getChildren().get(0);
        playerStaminaText = (Label) playerStaminaMeter.getChildren().get(1);
        playerStatusLabel = new Label("Ready");
        playerStatusLabel.getStyleClass().add("combat-status");
        VBox playerStatus = createStatusBox();
        playerStatus.getChildren().addAll(playerBattleLabel, playerHpMeter, playerStaminaMeter,
                playerStatusLabel);
        playerBox.getChildren().addAll(playerSpriteNode, playerStatus);

        enemyBox = new VBox(-8);
        enemyBox.getStyleClass().addAll("actor-panel", "enemy-actor-panel");
        enemySpriteNode = createSprite("enemy-sprite");
        enemyAvatarImage = createAvatarImageView(220, 240);
        enemyAvatarImage.setScaleX(-1);
        enemyShapeFigure = findShapeFigure(enemySpriteNode);
        enemySpriteNode.getChildren().add(enemyAvatarImage);
        enemySpriteLabel = new Label("OPPONENT");
        enemySpriteLabel.getStyleClass().add("sprite-label");
        enemySpriteLabel.setWrapText(true);
        enemySpriteLabel.setMaxWidth(118);
        enemySpriteLabel.setVisible(false);
        enemySpriteLabel.setManaged(false);
        enemySpriteNode.getChildren().add(enemySpriteLabel);
        enemyBattleLabel = new Label("Enemy");
        enemyBattleLabel.getStyleClass().add("section-title");
        StackPane enemyHpMeter = createMeter("hp-bar");
        enemyHpBar = (ProgressBar) enemyHpMeter.getChildren().get(0);
        enemyHpText = (Label) enemyHpMeter.getChildren().get(1);
        StackPane enemyStaminaMeter = createMeter("stamina-bar");
        enemyStaminaBar = (ProgressBar) enemyStaminaMeter.getChildren().get(0);
        enemyStaminaText = (Label) enemyStaminaMeter.getChildren().get(1);
        enemyStatusLabel = new Label("Waiting");
        enemyStatusLabel.getStyleClass().add("combat-status");
        VBox enemyStatus = createStatusBox();
        enemyStatus.getChildren().addAll(enemyBattleLabel, enemyHpMeter, enemyStaminaMeter,
                enemyStatusLabel);
        enemyBox.getChildren().addAll(enemySpriteNode, enemyStatus);

        HBox.setHgrow(playerBox, Priority.ALWAYS);
        HBox.setHgrow(enemyBox, Priority.ALWAYS);
        battlePanel.getChildren().addAll(playerBox, enemyBox);

        battleMessageLabel = new Label("Create or load a gladiator.");
        battleMessageLabel.getStyleClass().add("battle-message");
        battleMessageLabel.setWrapText(true);
        battleMessageLabel.setMaxWidth(Double.MAX_VALUE);
        battleMessageLabel.setVisible(false);
        battleMessageLabel.setManaged(false);
        StackPane.setAlignment(battleMessageLabel, Pos.TOP_CENTER);
        battleMessageLabel.setTranslateY(48);

        roundLabel = new Label("ROUND 1");
        roundLabel.getStyleClass().add("round-badge");
        roundLabel.setVisible(false);
        roundLabel.setManaged(false);
        StackPane.setAlignment(roundLabel, Pos.TOP_CENTER);
        roundLabel.setTranslateY(10);

        rivalBannerLabel = new Label("RIVAL");
        rivalBannerLabel.getStyleClass().add("rival-banner");
        rivalBannerLabel.setVisible(false);
        rivalBannerLabel.setManaged(false);
        StackPane.setAlignment(rivalBannerLabel, Pos.TOP_RIGHT);
        rivalBannerLabel.setTranslateX(-14);
        rivalBannerLabel.setTranslateY(10);

        resultTitleLabel = new Label("Victory");
        resultTitleLabel.getStyleClass().add("result-title");
        resultDetailLabel = new Label();
        resultDetailLabel.getStyleClass().add("result-detail");
        resultDetailLabel.setWrapText(true);
        resultDetailLabel.setMaxWidth(Double.MAX_VALUE);
        resultOverlay = new VBox(10);
        resultOverlay.getStyleClass().add("result-overlay");
        resultOverlay.setMaxWidth(520);
        resultOverlay.setVisible(false);
        resultOverlay.setManaged(false);
        resultOverlay.getChildren().addAll(resultTitleLabel, resultDetailLabel);
        StackPane.setAlignment(resultOverlay, Pos.CENTER);

        arenaScene.getChildren().add(battlePanel);
        arenaScene.getChildren().addAll(roundLabel, rivalBannerLabel, battleMessageLabel);
        arenaScene.getChildren().add(resultOverlay);
        VBox.setVgrow(arenaScene, Priority.ALWAYS);

        root.getChildren().addAll(sceneTitleLabel, arenaScene);
    }

    VBox getRoot() {
        return root;
    }

    void setPreviewClass(GladiatorClass previewClass) {
        this.previewClass = previewClass == null ? GladiatorClass.MURMILLO : previewClass;
    }

    void refresh(GameSession session, String idleSceneTitle) {
        refreshArenaStyle(session);
        refreshResultOverlay(session);
        boolean activeBattle = session.isInBattle();
        roundLabel.setVisible(activeBattle);
        roundLabel.setManaged(activeBattle);
        roundLabel.setText("ROUND " + Math.max(1, session.getBattleRound()));
        rivalBannerLabel.setVisible(activeBattle && session.isRivalBattle());
        rivalBannerLabel.setManaged(activeBattle && session.isRivalBattle());
        rivalBannerLabel.setText(session.getRivalBannerText());
        battleMessageLabel.setVisible(activeBattle && !battleMessageLabel.getText().isEmpty());
        battleMessageLabel.setManaged(activeBattle && !battleMessageLabel.getText().isEmpty());

        if (session.hasPlayer()) {
            AvatarInfo avatar = avatarRegistry.playerAvatar(session.getPlayer().getGladiatorClass());
            updateSpriteClass(playerSpriteNode, "class-", session.getPlayer().getGladiatorClass().name());
            updateAvatarClass(playerSpriteNode, "player-sprite", avatar);
            updateAvatarImage(playerAvatarImage, playerShapeFigure, playerSpriteNode, avatar);
            playerSpriteLabel.setText(avatar.getLabel());
            playerBattleLabel.setText(session.getPlayer().getName());
            playerHpBar.setProgress(clamp(session.getPlayerHpPercent()));
            playerStaminaBar.setProgress(clamp(session.getPlayerStaminaPercent()));
            playerHpText.setText("HP " + session.getPlayer().getHp() + "/" + session.getPlayer().getMaxHp());
            playerStaminaText.setText("STA " + session.getPlayer().getStamina()
                    + "/" + session.getPlayer().getMaxStamina());
            playerStatusLabel.setText(playerConditionText(session));
            refreshStatusClass(playerStatusLabel,
                    session.isPlayerPoisoned() || session.isPlayerStaggered(),
                    session.isPlayerDefending() || session.isCounterattackReady());
        } else {
            AvatarInfo avatar = avatarRegistry.playerAvatar(previewClass);
            updateSpriteClass(playerSpriteNode, "class-", previewClass.name());
            updateAvatarClass(playerSpriteNode, "player-sprite", avatar);
            updateAvatarImage(playerAvatarImage, playerShapeFigure, playerSpriteNode, avatar);
            playerSpriteLabel.setText(avatar.getLabel());
            playerBattleLabel.setText(avatar.getLabel());
            playerHpBar.setProgress(0);
            playerStaminaBar.setProgress(0);
            playerHpText.setText("HP");
            playerStaminaText.setText("STA");
            playerStatusLabel.setText("No gladiator");
        }

        if (session.getCurrentEnemy() != null) {
            sceneTitleLabel.setText((session.isRivalBattle() ? "Rival Challenge" : "Arena")
                    + " | " + session.getCurrentArenaEvent().getName());
            enemyBox.setVisible(true);
            enemyBox.setManaged(true);
            updateSpriteClass(enemySpriteNode, "ability-", session.getCurrentEnemy().getAbility().name());
            AvatarInfo avatar = avatarRegistry.enemyAvatar(session.getCurrentEnemy());
            updateAvatarClass(enemySpriteNode, "enemy-sprite", avatar);
            updateAvatarImage(enemyAvatarImage, enemyShapeFigure, enemySpriteNode, avatar);
            enemySpriteLabel.setText(avatar.getLabel());
            enemyBattleLabel.setText(session.getCurrentEnemy().getName());
            enemyHpBar.setProgress(clamp(session.getEnemyHpPercent()));
            enemyHpText.setText("HP " + session.getCurrentEnemy().getHp()
                    + "/" + session.getCurrentEnemy().getMaxHp());
            enemyStaminaBar.setProgress(clamp(session.getCurrentEnemy().getMaxStamina() == 0
                    ? 0.0
                    : session.getCurrentEnemy().getStamina() * 1.0
                    / session.getCurrentEnemy().getMaxStamina()));
            enemyStaminaText.setText("STA " + session.getCurrentEnemy().getStamina()
                    + "/" + session.getCurrentEnemy().getMaxStamina());
            enemyStatusLabel.setText(enemyConditionText(session));
            refreshStatusClass(enemyStatusLabel, session.isEnemyStaggered(), session.isEnemyTrapped());
        } else if (session.hasBattleSummary()) {
            sceneTitleLabel.setText("After the Fight");
            hideEnemy("RESULT");
        } else if (session.isGameOver()) {
            sceneTitleLabel.setText("Game Over");
            hideEnemy("ENDED");
        } else {
            sceneTitleLabel.setText(idleSceneTitle);
            hideEnemy("WAITING");
        }
    }

    void setBattleMessage(String message) {
        battleMessageLabel.setText(message == null ? "" : message.trim());
        updateBattleMessageStyle(message, false);
    }

    void setEnemyBattleMessage(String message) {
        battleMessageLabel.setText(message == null ? "" : message.trim());
        updateBattleMessageStyle(message, true);
    }

    private void updateBattleMessageStyle(String message, boolean enemyAction) {
        battleMessageLabel.getStyleClass().removeIf(style -> style.startsWith("message-"));
        String text = message == null ? "" : message.toLowerCase();

        if (text.contains("poison")) {
            battleMessageLabel.getStyleClass().add("message-poison");
        } else if (text.contains("drains") || text.contains("weighted net")) {
            battleMessageLabel.getStyleClass().add("message-control");
        } else if (text.contains("misses")) {
            battleMessageLabel.getStyleClass().add("message-miss");
        } else if (enemyAction && isEnemySpecialText(text)) {
            battleMessageLabel.getStyleClass().add("message-special");
        } else {
            battleMessageLabel.getStyleClass().add(enemyAction ? "message-enemy" : "message-player");
        }
    }

    void playActionMotion(String actionText) {
        String text = actionText == null ? "" : actionText.toLowerCase();

        if (text.contains("raises a guard") || text.contains("shield bash")) {
            playGuardMotion();
            return;
        }

        if (text.contains("heavy attack") || text.contains("brutal critical")) {
            playHeavyMotion();
            return;
        }

        if (text.contains("uses an antidote") || text.contains("healing salve")
                || text.contains("draught") || text.contains("catches his breath")
                || text.contains("sharpens the weapon")) {
            playRecoveryMotion();
            return;
        }

        if (text.contains("shield bash") || text.contains("casts the net")
                || text.contains("strikes with both blades") || text.contains("hooks past")) {
            playSpecialMotion();
            return;
        }

        TranslateTransition lunge = new TranslateTransition(Duration.millis(90), playerSpriteNode);
        lunge.setFromX(0);
        lunge.setToX(14);
        lunge.setCycleCount(2);
        lunge.setAutoReverse(true);
        lunge.setOnFinished(event -> playerSpriteNode.setTranslateX(0));
        lunge.play();
    }

    void playEnemyActionMotion(String actionText) {
        String text = actionText == null ? "" : actionText.toLowerCase();
        boolean special = isEnemySpecialText(text);

        TranslateTransition lunge = new TranslateTransition(
                Duration.millis(special ? 170 : 110), enemySpriteNode);
        lunge.setFromX(0);
        lunge.setToX(special ? -30 : -15);
        lunge.setCycleCount(2);
        lunge.setAutoReverse(true);

        RotateTransition turn = new RotateTransition(Duration.millis(170), enemySpriteNode);
        turn.setFromAngle(0);
        turn.setToAngle(special ? -7 : 0);
        turn.setCycleCount(2);
        turn.setAutoReverse(true);

        ScaleTransition weight = new ScaleTransition(Duration.millis(170), enemySpriteNode);
        weight.setFromX(1.0);
        weight.setFromY(1.0);
        weight.setToX(special ? 1.07 : 1.0);
        weight.setToY(special ? 1.07 : 1.0);
        weight.setCycleCount(2);
        weight.setAutoReverse(true);

        ParallelTransition motion = new ParallelTransition(lunge, turn, weight);
        motion.setOnFinished(event -> resetEnemyMotion());
        motion.play();
    }

    private boolean isEnemySpecialText(String text) {
        return text.contains("cleave") || text.contains("crushing blow")
                || text.contains("attacks twice") || text.contains("poison")
                || text.contains("shield") || text.contains("dust flurry")
                || text.contains("wild swing") || text.contains("club wildly")
                || text.contains("hooks past") || text.contains("weighted net")
                || text.contains("behind the shield");
    }

    private void playHeavyMotion() {
        TranslateTransition lunge = new TranslateTransition(Duration.millis(150), playerSpriteNode);
        lunge.setFromX(0);
        lunge.setToX(34);
        lunge.setCycleCount(2);
        lunge.setAutoReverse(true);

        ScaleTransition weight = new ScaleTransition(Duration.millis(150), playerSpriteNode);
        weight.setFromX(1.0);
        weight.setFromY(1.0);
        weight.setToX(1.08);
        weight.setToY(1.08);
        weight.setCycleCount(2);
        weight.setAutoReverse(true);

        ParallelTransition motion = new ParallelTransition(lunge, weight);
        motion.setOnFinished(event -> resetPlayerMotion());
        motion.play();
    }

    private void playGuardMotion() {
        TranslateTransition brace = new TranslateTransition(Duration.millis(130), playerSpriteNode);
        brace.setFromX(0);
        brace.setToX(-10);
        brace.setCycleCount(2);
        brace.setAutoReverse(true);

        ScaleTransition guard = new ScaleTransition(Duration.millis(130), playerSpriteNode);
        guard.setFromX(1.0);
        guard.setFromY(1.0);
        guard.setToX(1.04);
        guard.setToY(0.96);
        guard.setCycleCount(2);
        guard.setAutoReverse(true);

        ParallelTransition motion = new ParallelTransition(brace, guard);
        motion.setOnFinished(event -> resetPlayerMotion());
        motion.play();
    }

    private void playSpecialMotion() {
        RotateTransition turn = new RotateTransition(Duration.millis(170), playerSpriteNode);
        turn.setFromAngle(0);
        turn.setToAngle(8);
        turn.setCycleCount(2);
        turn.setAutoReverse(true);

        TranslateTransition lunge = new TranslateTransition(Duration.millis(170), playerSpriteNode);
        lunge.setFromX(0);
        lunge.setToX(25);
        lunge.setCycleCount(2);
        lunge.setAutoReverse(true);

        ParallelTransition motion = new ParallelTransition(turn, lunge);
        motion.setOnFinished(event -> resetPlayerMotion());
        motion.play();
    }

    private void playRecoveryMotion() {
        ScaleTransition recovery = new ScaleTransition(Duration.millis(180), playerSpriteNode);
        recovery.setFromX(1.0);
        recovery.setFromY(1.0);
        recovery.setToX(1.06);
        recovery.setToY(1.06);
        recovery.setCycleCount(2);
        recovery.setAutoReverse(true);
        recovery.setOnFinished(event -> resetPlayerMotion());
        recovery.play();
    }

    private void resetPlayerMotion() {
        playerSpriteNode.setTranslateX(0);
        playerSpriteNode.setScaleX(1);
        playerSpriteNode.setScaleY(1);
        playerSpriteNode.setRotate(0);
    }

    private void resetEnemyMotion() {
        enemySpriteNode.setTranslateX(0);
        enemySpriteNode.setScaleX(1);
        enemySpriteNode.setScaleY(1);
        enemySpriteNode.setRotate(0);
    }

    void playEnemyHitFeedback() {
        playHitFeedback(enemySpriteNode);
    }

    void playPlayerHitFeedback() {
        playHitFeedback(playerSpriteNode);
    }

    private void hideEnemy(String label) {
        enemyBox.setVisible(false);
        enemyBox.setManaged(false);
        enemySpriteLabel.setText(label);
        enemyBattleLabel.setText("No active enemy");
        enemyHpBar.setProgress(0);
        enemyHpText.setText("HP");
        enemyStaminaBar.setProgress(0);
        enemyStaminaText.setText("STA");
        enemyStatusLabel.setText(label);
        updateSpriteClass(enemySpriteNode, "ability-", "");
        enemySpriteNode.getStyleClass().removeIf(style -> style.startsWith("rival-")
                || style.startsWith("enemy-"));
        clearAvatarImage(enemyAvatarImage, enemyShapeFigure, enemySpriteNode);
    }

    private StackPane createSprite(String styleClass) {
        StackPane sprite = new StackPane();
        sprite.getStyleClass().addAll("sprite", styleClass);

        Circle head = new Circle(24);
        head.getStyleClass().add("sprite-head");

        Rectangle body = new Rectangle(72, 92);
        body.setArcWidth(22);
        body.setArcHeight(22);
        body.getStyleClass().add("sprite-body");

        Rectangle weapon = new Rectangle(10, 86);
        weapon.getStyleClass().add("sprite-weapon");
        weapon.setRotate(-28);
        weapon.setTranslateX(54);
        weapon.setTranslateY(28);

        Rectangle guard = new Rectangle(30, 48);
        guard.setArcWidth(16);
        guard.setArcHeight(16);
        guard.getStyleClass().add("sprite-guard");
        guard.setTranslateX(-50);
        guard.setTranslateY(42);

        VBox figure = new VBox(-4);
        figure.setAlignment(Pos.CENTER);
        figure.getStyleClass().add("sprite-figure");
        figure.getChildren().addAll(head, body);

        sprite.getChildren().addAll(weapon, guard, figure);
        return sprite;
    }

    private ImageView createAvatarImageView(double fitWidth, double fitHeight) {
        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("avatar-image");
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setVisible(false);
        imageView.setManaged(false);
        return imageView;
    }

    private VBox findShapeFigure(StackPane sprite) {
        for (Node child : sprite.getChildren()) {
            if (child instanceof VBox && child.getStyleClass().contains("sprite-figure")) {
                return (VBox) child;
            }
        }

        throw new IllegalStateException("Sprite figure was not created.");
    }

    private StackPane createArenaBackdrop() {
        StackPane backdrop = new StackPane();
        backdrop.getStyleClass().add("arena-backdrop");
        backdrop.setMouseTransparent(true);
        backdrop.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Rectangle upperWall = new Rectangle(760, 118);
        upperWall.getStyleClass().add("arena-upper-wall");
        StackPane.setAlignment(upperWall, Pos.TOP_CENTER);
        upperWall.setTranslateY(12);

        Rectangle crowdBand = new Rectangle(700, 58);
        crowdBand.getStyleClass().add("arena-crowd-band");
        StackPane.setAlignment(crowdBand, Pos.TOP_CENTER);
        crowdBand.setTranslateY(38);

        Rectangle leftPillar = new Rectangle(30, 230);
        leftPillar.getStyleClass().add("arena-pillar");
        StackPane.setAlignment(leftPillar, Pos.CENTER_LEFT);
        leftPillar.setTranslateX(44);
        leftPillar.setTranslateY(16);

        Rectangle rightPillar = new Rectangle(30, 230);
        rightPillar.getStyleClass().add("arena-pillar");
        StackPane.setAlignment(rightPillar, Pos.CENTER_RIGHT);
        rightPillar.setTranslateX(-44);
        rightPillar.setTranslateY(16);

        Ellipse sandShadow = new Ellipse(250, 42);
        sandShadow.getStyleClass().add("sand-shadow");
        StackPane.setAlignment(sandShadow, Pos.BOTTOM_CENTER);
        sandShadow.setTranslateY(-36);

        Rectangle arenaLine = new Rectangle(560, 3);
        arenaLine.getStyleClass().add("arena-floor-line");
        StackPane.setAlignment(arenaLine, Pos.BOTTOM_CENTER);
        arenaLine.setTranslateY(-112);

        backdrop.getChildren().addAll(upperWall, crowdBand, leftPillar, rightPillar, arenaLine, sandShadow);
        return backdrop;
    }

    private ImageView createBackgroundImage(String imagePath) {
        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl == null) {
            return null;
        }

        Image image = new Image(imageUrl.toExternalForm(), false);
        if (image.isError()) {
            return null;
        }

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.fitWidthProperty().bind(arenaScene.widthProperty());
        imageView.fitHeightProperty().bind(arenaScene.heightProperty());
        return imageView;
    }

    private VBox createStatusBox() {
        VBox status = new VBox(6);
        status.getStyleClass().add("combatant-panel");
        return status;
    }

    private String playerConditionText(GameSession session) {
        String text = "Fatigue " + session.getPlayer().getFatigue() + "/100";
        if (!"None".equals(session.getPlayer().getInjuryType().getDisplayName())) {
            text += " | Injured: " + session.getPlayer().getInjuryType().getDisplayName();
        }
        if (session.isPlayerPoisoned()) {
            text += " | POISONED";
        }
        if (session.isPlayerDefending()) {
            text += " | DEFENDING";
        }
        if (session.isPlayerStaggered()) {
            text += " | WINDED / STAGGERED";
        }
        if (session.isCounterattackReady()) {
            text += " | COUNTER READY";
        }

        return text;
    }

    private String enemyConditionText(GameSession session) {
        String text = "Ability: " + displayAbility(session.getCurrentEnemy().getAbility().name());
        if (session.isEnemyTrapped()) {
            text += " | TRAPPED";
        }
        if (session.isEnemyStaggered()) {
            text += " | WINDED / STAGGERED";
        } else if (session.getCurrentEnemy().getHp() * 4 <= session.getCurrentEnemy().getMaxHp()) {
            text += " | DESPERATE";
        } else if (session.getCurrentEnemy().hasStamina(12)) {
            text += " | POISED";
        }

        return text;
    }

    private String displayAbility(String abilityName) {
        String[] parts = abilityName.toLowerCase().split("_");
        StringBuilder text = new StringBuilder();
        for (String part : parts) {
            if (text.length() > 0) {
                text.append(' ');
            }
            text.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return text.toString();
    }

    private void refreshStatusClass(Label label, boolean danger, boolean active) {
        label.getStyleClass().removeAll("status-danger", "status-active");
        if (danger) {
            label.getStyleClass().add("status-danger");
        } else if (active) {
            label.getStyleClass().add("status-active");
        }
    }

    private void refreshArenaStyle(GameSession session) {
        arenaScene.getStyleClass().removeAll("arena-clear", "arena-crowd", "arena-sandstorm",
                "arena-sponsor", "arena-idle");
        boolean showingArena = session.isInBattle();
        setBackgroundVisible(arenaBackgroundImage, showingArena);
        setBackgroundVisible(ludusBackgroundImage, !showingArena);

        if (!showingArena) {
            arenaScene.getStyleClass().add("arena-idle");
            return;
        }

        ArenaEvent event = session.getCurrentArenaEvent();
        switch (event) {
            case CROWD_FAVOR:
                arenaScene.getStyleClass().add("arena-crowd");
                break;
            case SANDSTORM:
                arenaScene.getStyleClass().add("arena-sandstorm");
                break;
            case NOBLE_SPONSOR:
                arenaScene.getStyleClass().add("arena-sponsor");
                break;
            case NONE:
            default:
                arenaScene.getStyleClass().add("arena-clear");
                break;
        }
    }

    private void setBackgroundVisible(ImageView imageView, boolean visible) {
        if (imageView == null) {
            return;
        }

        imageView.setVisible(visible);
        imageView.setManaged(visible);
    }

    private void refreshResultOverlay(GameSession session) {
        BattleSummary summary = session.getLastBattleSummaryInfo();
        boolean showingResult = summary != null && !session.isInBattle();

        resultOverlay.setVisible(showingResult);
        resultOverlay.setManaged(showingResult);

        resultOverlay.getStyleClass().removeAll("victory-result", "defeat-result", "champion-result");
        if (!showingResult) {
            return;
        }

        boolean victory = summary.getOutcome() == BattleSummary.Outcome.VICTORY;
        boolean championVictory = victory && session.hasChampionEnding();
        resultOverlay.getStyleClass().add(championVictory ? "champion-result"
                : victory ? "victory-result" : "defeat-result");
        resultTitleLabel.setText(championVictory ? "Arena Champion"
                : (victory ? "Victory" : "Defeat") + " — " + summary.getOpponentName());
        resultDetailLabel.setText(formatResultDetails(summary, session));
    }

    private String formatResultDetails(BattleSummary summary, GameSession session) {
        if (summary.getOutcome() == BattleSummary.Outcome.VICTORY) {
            return "Rewards"
                    + "\nGold: +" + summary.getGoldReward()
                    + "\nFame: +" + summary.getFameReward()
                    + "\nCareer: " + session.getPlayer().getFame() + "/300 Fame"
                    + "\nRecord: " + summary.getWins() + "-" + summary.getLosses()
                    + "\nNext: " + summary.getNextStep();
        }

        String text = summary.isRunEnded()
                ? summary.getNextStep()
                : "Injury: " + summary.getInjuryName()
                + "\nFame: " + summary.getFameReward()
                + "\nLoss Streak: " + summary.getLossStreak() + "/" + summary.getMaxLossStreak()
                + "\nNext: " + summary.getNextStep();

        return text + "\nCareer: " + session.getPlayer().getFame() + "/300 Fame"
                + "\nRecord: " + summary.getWins() + "-" + summary.getLosses();
    }

    private void updateSpriteClass(StackPane sprite, String prefix, String value) {
        sprite.getStyleClass().removeIf(style -> style.startsWith(prefix));
        if (value != null && !value.isEmpty()) {
            sprite.getStyleClass().add(prefix + value.toLowerCase().replace('_', '-'));
        }
    }

    private void updateAvatarClass(StackPane sprite, String baseStyleToKeep, AvatarInfo avatar) {
        sprite.getStyleClass().removeIf(style -> isAvatarStyle(style, baseStyleToKeep));
        sprite.getStyleClass().add(avatar.getStyleKey());
    }

    private void updateAvatarImage(ImageView imageView,
                                   VBox shapeFigure,
                                   StackPane sprite,
                                   AvatarInfo avatar) {
        Image image = loadAvatarImage(avatar);

        if (image == null || image.isError()) {
            clearAvatarImage(imageView, shapeFigure, sprite);
            return;
        }

        imageView.setImage(image);
        imageView.setVisible(true);
        imageView.setManaged(true);
        shapeFigure.setVisible(false);
        setShapeDecorationsVisible(sprite, false);
        sprite.getStyleClass().add("image-avatar");
    }

    private void clearAvatarImage(ImageView imageView, VBox shapeFigure, StackPane sprite) {
        imageView.setImage(null);
        imageView.setVisible(false);
        imageView.setManaged(false);
        shapeFigure.setVisible(true);
        setShapeDecorationsVisible(sprite, true);
        sprite.getStyleClass().remove("image-avatar");
    }

    private Image loadAvatarImage(AvatarInfo avatar) {
        URL imageUrl = getClass().getResource(avatar.getImagePath());
        if (imageUrl == null) {
            return null;
        }

        return new Image(imageUrl.toExternalForm(), false);
    }

    private void setShapeDecorationsVisible(StackPane sprite, boolean visible) {
        for (Node node : sprite.getChildren()) {
            if (node.getStyleClass().contains("sprite-weapon")
                    || node.getStyleClass().contains("sprite-guard")) {
                node.setVisible(visible);
                node.setManaged(visible);
            }
        }
    }

    private boolean isAvatarStyle(String style, String baseStyleToKeep) {
        if (style.equals(baseStyleToKeep)) {
            return false;
        }

        return style.startsWith("player-")
                || style.startsWith("enemy-")
                || style.startsWith("rival-");
    }

    private StackPane createMeter(String styleClass) {
        ProgressBar bar = new ProgressBar(0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add(styleClass);

        Label text = new Label();
        text.getStyleClass().add("meter-text");

        StackPane meter = new StackPane();
        meter.getStyleClass().add("meter");
        meter.getChildren().addAll(bar, text);
        return meter;
    }

    private void playHitFeedback(StackPane sprite) {
        sprite.getStyleClass().remove("hit-flash");
        sprite.getStyleClass().add("hit-flash");

        TranslateTransition shake = new TranslateTransition(Duration.millis(55), sprite);
        shake.setFromX(-8);
        shake.setToX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(event -> sprite.setTranslateX(0));
        shake.play();

        PauseTransition flash = new PauseTransition(Duration.millis(240));
        flash.setOnFinished(event -> sprite.getStyleClass().remove("hit-flash"));
        flash.play();

        ScaleTransition pulse = new ScaleTransition(Duration.millis(120), sprite);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.04);
        pulse.setToY(0.96);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private double clamp(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}
