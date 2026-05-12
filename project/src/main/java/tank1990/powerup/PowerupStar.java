package tank1990.powerup;

import tank1990.core.Globals;
import tank1990.core.TextureFX;

/**
 * @class PowerupStar
 * @brief Represents a star power-up in the game.
 * @details This power-up grants the tank to increase its tier. In each tier, the tank can gain special abilities.
 */
public class PowerupStar extends AbstractPowerup {
    public PowerupStar(int x, int y) {
        super(x, y, PowerupType.POWERUP_STAR, Globals.DEFAULT_POWERUP_LIFETIME_MS);
        this.textureFX = new TextureFX(Globals.TEXTURE_POWERUP_STAR);
    }

}
