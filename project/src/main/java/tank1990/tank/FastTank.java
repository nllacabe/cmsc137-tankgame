package tank1990.tank;

import tank1990.core.Direction;
import tank1990.core.GameLevel;
import tank1990.core.Globals;

import java.util.Random;

/**
 * @class FastTank
 * @brief Represents a fast-moving enemy tank in the game.
 * @details This type of tank moves quickly and is designed to challenge the player with its speed.
 */
public class FastTank extends AbstractTank implements Enemy {

    public FastTank(int x, int y) {
        super(x, y);

        setDefaultTankTextureFXs();

        setSpeedUnit(Globals.FAST_TANK_MOVEMENT_SPEED);
        setMaxSpeedUnit(Globals.FAST_TANK_MOVEMENT_MAX_SPEED);
    }

    public FastTank(int x, int y, Direction dir) {
        super(x, y, dir);

        setDefaultTankTextureFXs();

        setSpeedUnit(Globals.FAST_TANK_MOVEMENT_SPEED);
        setMaxSpeedUnit(Globals.FAST_TANK_MOVEMENT_MAX_SPEED);
    }

    @Override
    protected void setDefaultTankTextureFXs() {
        this.tankTextureFxStruct = Globals.TEXTURE_FAST_TANK_STRUCT;
        createTextureFXs();
    }

    @Override
    protected void setRedTankTextureFXs() {
        this.tankTextureFxStruct = Globals.TEXTURE_FAST_TANK_RED_STRUCT;
        createTextureFXs();
    }

}
