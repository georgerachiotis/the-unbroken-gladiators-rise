# The Unbroken: Gladiator's Rise

A single-player JavaFX gladiator RPG where you train a fighter, choose arena
contracts, fight recurring rivals, buy equipment, save progress, challenge the
champion, and decide whether to continue fighting or purchase your freedom.
The game is fully offline and the original console version remains in the
repository as a reference implementation.

## How to Run

To run the JavaFX build from the project root, install a JavaFX SDK and either
set `JAVAFX_HOME` or use the default local SDK path if it exists:

```powershell
.\scripts\run-javafx.ps1
```

If PowerShell blocks local scripts, use the batch launcher instead:

```bat
scripts\run-javafx.bat
```

The JavaFX source lives in `javafx-src/`, while shared gameplay code lives in
`src/`.

The JavaFX edition uses cinematic background music for the Ludus, normal arena
fights, and rival/champion encounters. The `Audio` button provides separate
music/effects volume controls and a master mute option. Music credits and source
links are recorded in `javafx-src/arena/fx/assets/audio/README.md`.

The original console version can still be run from `arena.Main`:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
java -cp out arena.Main
```

New JavaFX careers offer three difficulty settings. `Standard` keeps the
original balance, `Story` lowers incoming combat damage and raises rewards,
and `Merciless` increases incoming combat damage while lowering rewards. The
chosen setting is stored with the career save.

If you are using IntelliJ IDEA, configure the JavaFX SDK and run
`arena.fx.FxMain` with the `javafx.controls` and `javafx.media` modules.

## Windows Build

Create a self-contained Windows application image from PowerShell:

```powershell
.\scripts\build-windows.ps1
```

The finished executable is written to:

```text
dist\TheUnbrokenGladiatorsRise\TheUnbrokenGladiatorsRise.exe
```

The generated folder includes its own trimmed Java runtime. It can run without
an IDE, a separate Java installation, or a manually configured `JAVAFX_HOME`.

## Gameplay Loop

The game opens on a main menu where you can start a new game, load an existing
save slot, or exit. Starting a new game creates a gladiator; loading restores
the saved player, day, and rival progress from the chosen slot.

New games begin with a guided prologue fight in the training pit against another
slave. The tutorial teaches one action per round: basic attack, heavy attack,
defend, rest, and a class special finisher. It uses real stamina costs, but
restores the player afterward before the normal daily loop starts.

Each day you choose one main action:

- View your gladiator's stats.
- Train strength, defense, or stamina.
- Choose a training style with different rewards and risks.
- Repeat your last training drill when you want to keep a routine moving.
- Pick a fight contract before entering the arena, unless the lanista has ordered
  you into a compulsory bout.
- Buy equipment, stamina meals, and battle consumables from the market.
- Buy the last market item again when stocking supplies.
- Rest once or rest until healed to recover HP, stamina, injuries, and fatigue.
- Save or load progress.
- Change fight text between detailed and compact mode.

Winning fights grants fame, gold, and experience. Early wins grant a little
extra fame so the opening rank does not drag, and rival victories grant bonus
fame, especially the first time you beat a named rival. Experience raises your
level, restores you, and improves core stats over time.

Losing a fight is punishing but no longer ends the game immediately. The guards
drag you out injured, with low HP, extra fatigue, and a loss streak. A win clears
the streak, but three consecutive losses end the gladiator's story.
After a loss, you can use the infirmary, bribe a physician, or train through the
pain for a small XP gain.

Gladiator classes now have different starting identities:

- `Murmillo`: tougher and better defended.
- `Retiarius`: lighter, with the most stamina.
- `Dimachaerus`: highest starting strength.
- `Thraex`: balanced with a defensive lean.

Most arena days are against common opponents, while named rivals appear as
notable fights. Rivals become more common once your fame rises, and contracts
can force rival challenges for higher fame. Arena events can affect rewards or
crowd favor before the fight. Market gear improves as your fame and rank rise.

Consumables can be used in battle from the item menu:

- Healing Salve restores HP.
- Stamina Draught restores stamina.
- Antidote cures poison.
- Whetstone gives a small strength boost.

Losing or risky training can cause temporary injuries. Resting clears injuries.

Training is limited by fatigue and arena rank. Repeated training builds fatigue,
which reduces gains and raises injury risk. Stats also have rank-based training
caps, and cap messages tell the player which stat stopped improving and what
rank cap they reached. Higher fame is needed before training can keep improving
a gladiator.

The player is owned and trained in a gladiator school by a lanista. Avoiding
fights for several days lowers standing, and if the player stays idle too long
the lanista orders a compulsory arena bout instead of letting the player keep
choosing day-to-day activities.

Once fame reaches 300, the champion match becomes available.

## Project Map

- `arena.fx.FxMain` starts the JavaFX GUI from `javafx-src/`.
- `arena.fx.BattleSceneView` owns the central arena scene, sprites, meters, and battle animations.
- `arena.fx.AvatarRegistry` maps player classes, common enemies, and rivals to avatar keys and future PNG paths.
- `arena.fx.CommandPanelView` owns the changing command menus and action buttons.
- `arena.fx.StatsPanel` owns the right-side player character sheet.
- `arena.fx.ScreenState` names the JavaFX screen modes.
- `arena.engine.GameSession` is the GUI-facing gameplay session.
- `arena.Main` starts the original console application.
- `arena.Game` controls the original console menus, day progression, arena entry, and saving.
- `arena.combat.Battle` runs turn-based fights.
- `arena.combat.PlayerAction` names the battle menu actions.
- `arena.enemies.ArenaRoster` creates common enemies, rivals, and the champion.
- `arena.characters.Combatant` stores shared fighting stats and health logic.
- `arena.characters.Player` stores player progression, fame, record, and gear.
- `arena.characters.Enemy` stores arena opponents and victory rewards.
- `arena.characters.Rival` adds rematch history and quotes to named enemies.
- `arena.shop.Shop` creates tiered market gear from class and fame.
- `arena.items` contains simple weapon and armor bonus models.
- `arena.saves.SaveManager` writes and reads saved game state.
- `arena.io.ConsoleInput` keeps console menu input safe and reusable.

See `DESIGN.md` for the gameplay and architecture notes.

## Avatar Assets

The JavaFX battle screen supports optional PNG avatars. If a PNG exists at the
path registered by `arena.fx.AvatarRegistry`, the UI shows it. If it is missing,
the procedural JavaFX placeholder remains visible.

Avatar files belong under:

- `javafx-src/arena/fx/assets/avatars/players/`
- `javafx-src/arena/fx/assets/avatars/enemies/`
- `javafx-src/arena/fx/assets/avatars/rivals/`
- `javafx-src/arena/fx/assets/avatars/champions/`

Example rival filename:

```text
javafx-src/arena/fx/assets/avatars/rivals/rival-titus-the-butcher.png
```

The JavaFX run scripts include `javafx-src` on the classpath, so PNG files in
these folders are available immediately when running the GUI.

## Automated Checks

There is a small no-framework test runner for save/load, input, leveling, and
core stat behavior:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
java -cp out arena.tests.TestRunner
```

The test runner creates `saves/test-save.txt` and files under
`saves/test-slots/` when it runs.

## Save Files

The normal game uses three save slots inside the user's local application-data
folder (`TheUnbrokenGladiatorsRise/saves`). Existing project-local saves are
still detected for backwards compatibility. Saving asks which slot to write, and loading shows the
gladiator name, day, and rank for each occupied slot. The save files use named
properties. The JavaFX Records Room also allows an occupied or damaged slot to
be deleted after a confirmation prompt, and asks before overwriting an existing
career. Audio volume, mute, the last selected difficulty, and adjustable UI
positions are remembered between launches. Save files use readable named
properties such as `player.hp`, `weapon.name`, and `rival.titus.defeats`, which
makes them easier to inspect and extend safely.

## Credits and third-party assets

Music and sound effects are used under the Pixabay Content License. Full titles,
creators, source links, and licensing information are collected in
[`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).
