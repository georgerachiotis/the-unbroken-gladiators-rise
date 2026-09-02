# Design Notes

This project is a small gladiator RPG with a JavaFX GUI and an original console
version kept as a reference while the conversion continues. The goal of the code
structure is to keep gameplay rules readable without introducing a large
framework.

## Main Flow

`arena.engine.GameSession` coordinates the JavaFX-facing session. It owns the
player, current day, active enemy, save access, and calls into focused systems
for shop, roster, contracts, events, and combat actions.

`arena.Game` still coordinates the original console session. It remains useful
as a reference for mechanics that have not been fully reworked into the GUI.

The session starts at a title menu. New Game resets the day and rival state
before character creation, then runs a non-lethal guided tutorial match in the
training pit before the daily loop. The tutorial teaches basic attack, heavy
attack, defend, rest, and a class special finisher one round at a time. Load
Game asks for a save slot, then restores the saved player, day, and rival defeat
counts before entering the daily loop.

Daily actions are:

- View the gladiator.
- Choose a training style.
- Repeat the last chosen training style.
- Pick a fight contract and fight in the arena.
- Visit the shop for gear or consumables.
- Repeat the last shop purchase when stocking supplies.
- Rest once or rest until healed.
- Save or load.
- Toggle detailed or compact fight text.
- Challenge the champion after 300 fame.

## Progression

Fame controls rank and unlocks the champion match. Gold buys market upgrades.
Experience comes from fight rewards and raises level. Level-ups restore HP and
stamina, increase max HP and max stamina, and alternate between strength and
defense improvements.

Early fight victories grant a small bonus fame payout before Arena Rookie, which
keeps the opening from lingering too long. Rival wins grant extra fame, with a
larger bonus for first-time rival defeats, so named fights naturally accelerate
the midgame without lowering the 300-fame champion requirement.

Training is useful but intentionally limited. Training adds fatigue, high fatigue
reduces gains and can cause injury, and each rank has stat caps. When a cap is
reached, the lanista stops the drill and the message names the capped stat, the
current cap, and the rank causing it. This prevents the optimal strategy from
becoming "train forever before fighting."

The player is a slave in a gladiator school, so arena pressure comes from the
lanista instead of an abstract schedule. Avoiding the sand for several days costs
standing; after that, the lanista sends the player into a compulsory bout.

Crowd favor is a secondary momentum track earned from events, sparring, close
victories, and normal wins. At high enough favor, the crowd grants bonus gold
and fame after a victory. Injuries are temporary penalties from losses or risky
training:

- `BRUISED_RIBS`: lowers effective stamina.
- `WOUNDED_ARM`: lowers effective strength.
- `SHAKEN`: lowers effective defense.

Resting clears injuries.

Defeat uses a three-strike pressure model instead of instant game over. A lost
match increments the player's consecutive loss streak, applies an injury, lowers
standing, adds fatigue, and returns the player to the ludus with low HP and
stamina. Any victory clears the streak. Three consecutive losses means the
lanista gives up on the fighter and the run ends.
After a loss, the player chooses between a free infirmary cot, a paid physician,
or training through pain for a small XP reward.

Class identity is set in `Player`:

- `MURMILLO`: highest HP and defense, lower stamina.
- `RETIARIUS`: highest stamina, lighter defenses.
- `DIMACHAERUS`: highest starting strength.
- `THRAEX`: balanced with a defensive lean.

## Shop

`arena.shop.Shop` builds market offerings from the player's fame and class.
Weapon names follow the gladiator class, while material, price, and bonus follow
rank tiers. `ShopItem` only describes what the menu needs to show and charge.
The shop also sells consumables for battle: salves, stamina draughts, antidotes,
and whetstones.

## Contracts And Events

`FightContract` controls fight risk and reward multipliers. The player can take
a safer bout, a richer dangerous bout, or a rival challenge. If the lanista
orders a compulsory fight, the game uses a fixed contract and skips the contract
choice. `ArenaEvent` adds pre-fight flavor and small reward or crowd-favor
changes.

## Arena Roster

`arena.enemies.ArenaRoster` owns enemy creation. It creates:

- Recurring named rivals and their rematch history.
- Scaled versions of rivals for arena fights.
- Common opponents for normal arena days.
- The final champion.

Rivals appear on every third day, become more likely after high fame, or appear
by random chance. Common enemies stay in the rotation so rivals remain notable
instead of becoming the entire arena schedule.

## Battle

`arena.combat.Battle` runs one fight. Each exchange is shown as a numbered
round with compact player and enemy status lines, a single action prompt with
stamina costs, then separate "Your move" and "Enemy move" results. Special
abilities are option 3 so they sit near the attack choices. If the player lacks
stamina for an action, the turn is not spent; the battle asks for another
choice. Option 6 opens consumables. `PlayerAction` turns menu numbers into named
actions, which makes the battle loop easier to read. Enemy specials use
`EnemyAbility`, so special behavior depends on explicit data rather than enemy
name text.

Class passives add small identity:

- `MURMILLO`: blocks slightly more damage while defending.
- `RETIARIUS`: regains stamina after a successful net throw.
- `DIMACHAERUS`: can follow a basic attack with an extra blade hit.
- `THRAEX`: deals bonus damage against high-defense enemies.

## Saves

`SaveManager` writes named properties to slot files under `saves/`. The normal
game offers three slots, while tests can still provide a direct save path. The
current save format includes `save.version=5`, player state, equipment,
consumables, injury, fatigue, crowd favor, day, fight-avoidance count, and rival
defeat counts. Missing older optional fields still fall back to safe defaults
where possible.

Generated save files are ignored by `.gitignore`.

## Tests

`arena.tests.TestRunner` is a no-framework test runner. It checks save/load,
stat clamping, leveling, class identities, input handling, enemy abilities,
shop tiers, roster creation, save versioning, consumables, injuries, contracts,
and arena events.
