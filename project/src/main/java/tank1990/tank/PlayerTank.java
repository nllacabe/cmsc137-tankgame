package tank1990.tank;

import java.awt.Dimension;

import tank1990.core.*;
import tank1990.player.PlayerType;

/** * @class PlayerTank
 * @brief Represents a player-controlled tank in the game.
 * @details This class extends AbstractTank and provides functionality specific to player tanks,
 * including movement, texture management, and player type handling.
 */
public class PlayerTank extends AbstractTank {

    public PlayerTank(int x, int y) {
        super(x, y);

        this.tankTextureFxStruct = Globals.TEXTURE_PLAYER1_TANK_STRUCT;
        createTextureFXs();

        setSpeedUnit(Globals.PLAYER_TANK_MOVEMENT_SPEED);
        setMaxSpeedUnit(Globals.PLAYER_TANK_MOVEMENT_MAX_SPEED);

        this.movementTick.setDefaultTick(Utils.Time2GameTick(60));  // This value is more suitable for player tank
    }

    public PlayerTank(int x, int y, Direction dir) {
        super(x, y, dir);

        this.tankTextureFxStruct = Globals.TEXTURE_PLAYER1_TANK_STRUCT;
        createTextureFXs();

        setSpeedUnit(Globals.PLAYER_TANK_MOVEMENT_SPEED);
        setMaxSpeedUnit(Globals.PLAYER_TANK_MOVEMENT_MAX_SPEED);

        this.movementTick.setDefaultTick(Utils.Time2GameTick(60));  // This value is more suitable for player tank
    }

    @Override
    protected void setDefaultTankTextureFXs() { }

    @Override
    protected void setRedTankTextureFXs() { }

    /* * Set the player type and update the tank texture accordingly.
     * This method is used to switch between player 1 and player 2 tanks.
     * @param playerType The type of player (PLAYER_1 or PLAYER_2).
     */
    public void setPlayerType(PlayerType playerType) {
        switch (playerType) {
            case PLAYER_1 -> this.tankTextureFxStruct = Globals.TEXTURE_PLAYER1_TANK_STRUCT;
            case PLAYER_2 -> this.tankTextureFxStruct = Globals.TEXTURE_PLAYER2_TANK_STRUCT;
            case PLAYER_3 -> this.tankTextureFxStruct = Globals.TEXTURE_PLAYER3_TANK_STRUCT;
            case PLAYER_4 -> this.tankTextureFxStruct = Globals.TEXTURE_PLAYER4_TANK_STRUCT;
        }
        createTextureFXs();
    }

    @Override
    public void move(GameLevel level) {
        // Get tank dimensions for boundary calculations
        int tankWidth = (int) getSize().getWidth();
        int tankHeight = (int) getSize().getHeight();

        Dimension gameAreaSize = level.getGameAreaSize();

        // Since tank position is center point, calculate proper boundaries
        int halfWidth = tankWidth / 2;
        int halfHeight = tankHeight / 2;
        
        // Calculate new position with boundary checking (accounting for center position)
        int newX = Math.max(halfWidth, Math.min(getX() + getDx(), (int) gameAreaSize.getWidth() - halfWidth));
        int newY = Math.max(halfHeight, Math.min(getY() + getDy(), (int) gameAreaSize.getHeight() - halfHeight));
        //System.out.println("newX:" + newX + " newY:" + newY);
        RectangleBound newTankBound = new RectangleBound(newX - halfWidth, newY - halfHeight, tankWidth, tankHeight);

        // Check map constraints by checking neighbor tiles of the player tank
        boolean isMovable = level.checkMovable(this, newTankBound);
        if (isMovable) {
            // Update tank position and direction
            setX(newX);
            setY(newY);
        } else {}

        setDir(dir);
    }

}
