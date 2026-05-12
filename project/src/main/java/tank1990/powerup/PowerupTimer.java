package tank1990.powerup;

import tank1990.core.Globals;
import tank1990.core.TextureFX;

/** * @class PowerupTimer
 * @brief Represents a timer power-up in the game.
 * @details This power-up freezes all opponents for a specified duration. It can be collected by both players and
 * enemy tanks. If an enemy tank collects it, player tanks will freeze but able to shoot.
 */
public class PowerupTimer extends AbstractPowerup {
    public PowerupTimer(int x, int y) {
        super(x, y, PowerupType.POWERUP_TIMER, Globals.DEFAULT_POWERUP_LIFETIME_MS);
        this.textureFX = new TextureFX(Globals.TEXTURE_POWERUP_TIMER);
    }

}
