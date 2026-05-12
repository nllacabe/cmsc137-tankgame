package tank1990.powerup;

import tank1990.core.Globals;
import tank1990.core.TextureFX;

/**
 * @class PowerupWeapon
 * @brief Represents a weapon power-up in the game.
 * @details This class grands a weapon that shoots upgraded bullets when collected which can destroy steel blocks.
 */
public class PowerupWeapon extends AbstractPowerup {
    public PowerupWeapon(int x, int y) {
        super(x, y, PowerupType.POWERUP_WEAPON, Globals.DEFAULT_POWERUP_LIFETIME_MS);
        this.textureFX = new TextureFX(Globals.TEXTURE_POWERUP_WEAPON);
    }
}
