package tank1990.player;

/**
 * @class PlayerType
 * @brief Enum representing up to 4 players (1 master + up to 3 slaves).
 *
 * Assignment:
 *   PLAYER_1 — always the master node
 *   PLAYER_2 — first slave to connect
 *   PLAYER_3 — second slave to connect
 *   PLAYER_4 — third slave to connect
 */
public enum PlayerType {
    PLAYER_1,
    PLAYER_2,
    PLAYER_3,
    PLAYER_4;

    /** Converts a 0-based connection index to the matching PlayerType. */
    public static PlayerType fromIndex(int index) {
        return switch (index) {
            case 0 -> PLAYER_1;
            case 1 -> PLAYER_2;
            case 2 -> PLAYER_3;
            case 3 -> PLAYER_4;
            default -> throw new IllegalArgumentException("Max 4 players supported, got index " + index);
        };
    }

    public int toIndex() {
        return switch (this) {
            case PLAYER_1 -> 0;
            case PLAYER_2 -> 1;
            case PLAYER_3 -> 2;
            case PLAYER_4 -> 3;
        };
    }
}
