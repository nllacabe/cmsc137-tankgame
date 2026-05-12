package tank1990.powerup;

import tank1990.core.Globals;
import tank1990.core.TextureFX;

/**
 * @class PowerupTank
 * @brief Represents a tank power-up in the game.
 * @details This class increases remaining lives of the player tank. There is no effect on enemy tanks.
 */
public class PowerupTank extends AbstractPowerup {
    public PowerupTank(int x, int y) {
        super(x, y, PowerupType.POWERUP_TANK, Globals.DEFAULT_POWERUP_LIFETIME_MS);
        this.textureFX = new TextureFX(Globals.TEXTURE_POWERUP_TANK);
    }

}
