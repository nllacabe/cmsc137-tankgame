package tank1990.tank;

import tank1990.core.Direction;

/**
 * @class TankFactory
 * @brief Creates tanks — only BasicTank is used as enemy. All other enemy types
 *        map to BasicTank to preserve map-file compatibility.
 */
public class TankFactory {
    public static AbstractTank createTank(TankType type, int x, int y, Direction dir) {
        if (type == null) throw new IllegalArgumentException("Tank type cannot be null!");
        return switch (type) {
            case PLAYER_TANK -> new PlayerTank(x, y, dir);
            // All enemy types collapsed to BasicTank
            default          -> new BasicTank(x, y, dir);
        };
    }

    public static AbstractTank createTank(TankType type, int x, int y) {
        if (type == null) throw new IllegalArgumentException("Tank type cannot be null!");
        return switch (type) {
            case PLAYER_TANK -> new PlayerTank(x, y);
            default          -> new BasicTank(x, y);
        };
    }
}
