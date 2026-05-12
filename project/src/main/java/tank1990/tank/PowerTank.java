package tank1990.tank;

import tank1990.core.Direction;
import tank1990.core.GameLevel;
import tank1990.core.Globals;

import java.util.Random;

/**
 * @class PowerTank
 * @brief Represents a powerful enemy tank in the game.
 * @details This tank has higher fire rate and is designed to be more challenging for players.
 */
public class PowerTank extends AbstractTank implements Enemy {

    public PowerTank(int x, int y) {
        super(x, y);

        setDefaultTankTextureFXs();

        setSpeedUnit(Globals.POWER_TANK_MOVEMENT_SPEED);
        setMaxSpeedUnit(Globals.POWER_TANK_MOVEMENT_MAX_SPEED);
    }

    public PowerTank(int x, int y, Direction dir) {
        super(x, y, dir);

        setDefaultTankTextureFXs();

        setSpeedUnit(Globals.POWER_TANK_MOVEMENT_SPEED);
        setMaxSpeedUnit(Globals.POWER_TANK_MOVEMENT_MAX_SPEED);
    }

    @Override
    protected void setDefaultTankTextureFXs() {
        this.tankTextureFxStruct = Globals.TEXTURE_POWER_TANK_STRUCT;
        createTextureFXs();
    }

    @Override
    protected void setRedTankTextureFXs() {
        this.tankTextureFxStruct = Globals.TEXTURE_POWER_TANK_RED_STRUCT;
        createTextureFXs();
    }

}
