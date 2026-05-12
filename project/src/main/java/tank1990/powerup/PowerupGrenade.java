package tank1990.powerup;

import tank1990.core.Globals;
import tank1990.core.TextureFX;

/**
 * @class PowerupGrenade
 * @brief Represents a grenade power-up in the game.
 * @details This class extends AbstractPowerup and initializes the grenade power-up with its specific texture and lifetime.
 */
public class PowerupGrenade extends AbstractPowerup {
    public PowerupGrenade(int x, int y) {
        super(x, y, PowerupType.POWERUP_GRENADE, Globals.DEFAULT_POWERUP_LIFETIME_MS);
        this.textureFX = new TextureFX(Globals.TEXTURE_POWERUP_GRENADE);
    }

}
