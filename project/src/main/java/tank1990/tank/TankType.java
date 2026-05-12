package tank1990.tank;

/**
 * @class TankType
 * @brief Tank types — only BASIC_TANK is actively spawned as enemy; others kept for map/save compatibility.
 */
public enum TankType {
    BASIC_TANK,
    FAST_TANK,
    POWER_TANK,
    ARMOR_TANK,
    PLAYER_TANK;

    /** Needed by MapGenerator which reads tank type IDs from binary map files. */
    public static TankType valueOf(int val) {
        return switch (val) {
            case 0 -> BASIC_TANK;
            case 1 -> FAST_TANK;
            case 2 -> POWER_TANK;
            case 3 -> ARMOR_TANK;
            case 4 -> PLAYER_TANK;
            default -> throw new RuntimeException("Invalid TankType value: " + val);
        };
    }
}
