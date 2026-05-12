package tank1990.powerup;

/**
 * @enum PowerupType
 * @brief Enum representing different types of power-ups available in the game.
 * @details This enum defines various power-up types that can be collected by players,
 * each providing different abilities or enhancements to the tank.
 */
public enum PowerupType {
    POWERUP_GRENADE,
    POWERUP_HELMET,
    POWERUP_SHOVEL,
    POWERUP_STAR,
    POWERUP_TANK,
    POWERUP_TIMER,
    POWERUP_WEAPON;

    public static PowerupType valueOf(int val) {
        return switch (val) {
            case (0) -> POWERUP_GRENADE;
            case (1) -> POWERUP_HELMET;
            case (2) -> POWERUP_SHOVEL;
            case (3) -> POWERUP_STAR;
            case (4) -> POWERUP_TANK;
            case (5) -> POWERUP_TIMER;
            case (6) -> POWERUP_WEAPON;
            default -> throw new RuntimeException("Invalid Value for PowerupType");
        };
    }

}
