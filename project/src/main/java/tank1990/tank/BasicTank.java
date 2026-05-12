package tank1990.tank;

import tank1990.core.*;

import java.util.Random;

/**
 * @class BasicTank
 * @brief Represents a basic enemy tank in the game.
 * @details This class extends AbstractTank and offers mediocre speed and firepower.
 */
public class BasicTank extends AbstractTank implements Enemy {

    public BasicTank(int x, int y) {
        super(x, y);

        setDefaultTankTextureFXs();

        setSpeedUnit(Globals.BASIC_TANK_MOVEMENT_SPEED);
        setMaxSpeedUnit(Globals.BASIC_TANK_MOVEMENT_MAX_SPEED);
    }

    public BasicTank(int x, int y, Direction dir) {
        super(x, y, dir);

        setDefaultTankTextureFXs();

        setSpeedUnit(Globals.BASIC_TANK_MOVEMENT_SPEED);
        setMaxSpeedUnit(Globals.BASIC_TANK_MOVEMENT_MAX_SPEED);
    }

    @Override
    protected void setDefaultTankTextureFXs() {
        this.tankTextureFxStruct = Globals.TEXTURE_BASIC_TANK_STRUCT;
        createTextureFXs();
    }

    @Override
    protected void setRedTankTextureFXs() {
        this.tankTextureFxStruct = Globals.TEXTURE_BASIC_TANK_RED_STRUCT;
        createTextureFXs();
    }

}
