package tank1990.powerup;

import tank1990.core.Globals;
import tank1990.core.TextureFX;

/**
 * @class PowerupShovel
 * @brief Represents a shovel power-up in the game.
 * @details This power-up has a different effect which tank type is collecting.
 * It either reinforces or removes surrounding tiles of the Eagle.
 */
public class PowerupShovel extends AbstractPowerup {
    public PowerupShovel(int x, int y) {
        super(x, y, PowerupType.POWERUP_SHOVEL, Globals.DEFAULT_POWERUP_LIFETIME_MS);
        this.textureFX = new TextureFX(Globals.TEXTURE_POWERUP_SHOVEL);
    }

}
