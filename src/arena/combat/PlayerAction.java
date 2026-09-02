package arena.combat;

/**
 * Menu actions available to the player during battle.
 */
public enum PlayerAction {
    ATTACK(1),
    HEAVY_ATTACK(2),
    SPECIAL_ABILITY(3),
    DEFEND(4),
    REST(5),
    USE_ITEM(6),
    INVALID(-1);

    private final int menuChoice;

    PlayerAction(int menuChoice) {
        this.menuChoice = menuChoice;
    }

    public static PlayerAction fromMenuChoice(int menuChoice) {
        for (PlayerAction action : values()) {
            if (action.menuChoice == menuChoice) {
                return action;
            }
        }

        return INVALID;
    }
}
