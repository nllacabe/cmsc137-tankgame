package tank1990.powerup;

/**
 * @class PowerupFactory
 * @brief Factory class for creating powerup objects based on their type.
 * @details This class provides a static method to create instances of different powerup types.
 */
public class PowerupFactory {
    /**
     * Creates a new powerup based on the given type.
     *
     * This method generates an enemy using the provided configuration
     * and assigns it the given x and y coordinates.
     *
     * @param type The type of powerup to create.
     * @param x The x-coordinate of spawn location of the powerup.
     * @param y The y-coordinate of the spawn location of the powerup.
     * @return A newly created AbstractPowerup object of the specified type.
     * @throws IllegalStateException If the config parameter is null.
     * @throws IllegalArgumentException If the type parameter is null.
     */
    public static AbstractPowerup createPowerup(PowerupType type, int x, int y) {
        if (type == null) {
            throw new IllegalArgumentException("Powerup type cannot be null!");
        }

        switch (type) {
            case POWERUP_GRENADE:
                return new PowerupGrenade(x, y);
            case POWERUP_HELMET:
                return new PowerupHelmet(x, y);
            case POWERUP_SHOVEL:
                return new PowerupShovel(x, y);
            case POWERUP_STAR:
                return new PowerupStar(x, y);
            case POWERUP_TANK:
                return new PowerupTank(x, y);
            case POWERUP_TIMER:
                return new PowerupTimer(x, y);
            case POWERUP_WEAPON:
                return new PowerupWeapon(x, y);
            default:
                return null;
        }
    }
}
