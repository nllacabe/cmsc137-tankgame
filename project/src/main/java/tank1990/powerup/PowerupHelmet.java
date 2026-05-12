package tank1990.powerup;

import tank1990.core.Globals;
import tank1990.core.TextureFX;

/**
 * @class PowerupHelmet
 * @brief Represents a helmet power-up in the game.
 * @details This power-up provides tanks with a helmet, which can offer protection.
 */
public class PowerupHelmet extends AbstractPowerup {
    public PowerupHelmet(int x, int y) {
        super(x, y, PowerupType.POWERUP_HELMET, Globals.DEFAULT_POWERUP_LIFETIME_MS);
        this.textureFX = new TextureFX(Globals.TEXTURE_POWERUP_HELMET);
    }

}
