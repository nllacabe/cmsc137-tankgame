package tank1990.player;

import java.awt.Graphics;
import java.io.Serializable;

import tank1990.core.*;
import tank1990.core.GameMode;
import tank1990.projectiles.Blast;
import tank1990.projectiles.Bullet;
import tank1990.tank.AbstractTank;
import tank1990.tank.PlayerTank;
import tank1990.tank.TankFactory;
import tank1990.tank.TankType;

/**
 * @class Player
 * @brief Represents a player (local or remote) managing their tank and
 *        remaining lives.
 *        Supports up to 4 player types for network play.
 */
public class Player implements Serializable {
    private int remainingLives;
    private PlayerTank myTank = null;
    private final PlayerType playerType;
    private GameMode gameMode;

    /** Constructor for local/single-player modes. */
    public Player(PlayerType playerType) {
        this.playerType = playerType;
        this.gameMode = GameMode.MODE_SINGLE_PLAYER;
        this.remainingLives = Globals.INITAL_PLAYER_HEALTH;
        spawnTank();
    }

    /** Constructor that carries game mode so spawn location is chosen correctly. */
    public Player(PlayerType playerType, GameMode gameMode) {
        this.playerType = playerType;
        this.gameMode = gameMode;
        this.remainingLives = Globals.INITAL_PLAYER_HEALTH;
        spawnTank();
    }

    public void draw(Graphics g) {
        if (myTank != null)
            myTank.draw(g);
    }

    public void update(GameLevel level) {
        if (myTank == null)
            return;
        myTank.update(level);
        GridLocation gLoc = Utils.loc2GridLoc(new Location(myTank.getX(), myTank.getY()));
        level.setPlayerLocation(gLoc);
    }

    public void decrementDx() {
        if (myTank != null)
            myTank.decrementDx();
    }

    public void incrementDx() {
        if (myTank != null)
            myTank.incrementDx();
    }

    public void decrementDy() {
        if (myTank != null)
            myTank.decrementDy();
    }

    public void incrementDy() {
        if (myTank != null)
            myTank.incrementDy();
    }

    public void resetDx() {
        if (myTank != null)
            myTank.resetDx();
    }

    public void resetDy() {
        if (myTank != null)
            myTank.resetDy();
    }

    public Bullet shoot() {
        return myTank != null ? myTank.shoot() : null;
    }

    public boolean getDamage() {
        if (myTank == null || myTank.isSpawnProtectionEnabled())
            return false;
        return myTank.getDamage();
    }

    public Blast destroy() {
        return myTank != null ? myTank.destroy() : null;
    }

    public int getRemainingLives() {
        return remainingLives;
    }

    public boolean isTankDestroyed() {
        return myTank == null || myTank.isDestroyed();
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public AbstractTank getTank() {
        return myTank;
    }

    public boolean isFrozen() {
        return myTank != null && myTank.isFrozen();
    }

    public void setFrozen(boolean f) {
        if (myTank != null)
            myTank.setFrozen(f);
    }

    public void setRemainingLives(int v) {
        this.remainingLives = v;
    }

    public RectangleBound getBoundingBox() {
        return myTank != null ? myTank.getBoundingBox() : new RectangleBound(0, 0, 0, 0);
    }

    /**
     * Spawns the player's tank at the correct location for their PlayerType.
     * Supports PLAYER_1 through PLAYER_4.
     */
    public void spawnTank() {
        --remainingLives;
        if (remainingLives < 0)
            return;

        boolean isNetwork = (gameMode == GameMode.MODE_NETWORK_MASTER
                || gameMode == GameMode.MODE_NETWORK_SLAVE);

        GridLocation spawnLoc;
        Direction spawnDir;

        if (isNetwork) {
            // Network mode: spawn at the 4 corners
            switch (playerType) {
                case PLAYER_1 -> {
                    spawnLoc = Globals.NETWORK_PLAYER_1_LOC;
                    spawnDir = Globals.NETWORK_PLAYER_1_DIR;
                }
                case PLAYER_2 -> {
                    spawnLoc = Globals.NETWORK_PLAYER_2_LOC;
                    spawnDir = Globals.NETWORK_PLAYER_2_DIR;
                }
                case PLAYER_3 -> {
                    spawnLoc = Globals.NETWORK_PLAYER_3_LOC;
                    spawnDir = Globals.NETWORK_PLAYER_3_DIR;
                }
                case PLAYER_4 -> {
                    spawnLoc = Globals.NETWORK_PLAYER_4_LOC;
                    spawnDir = Globals.NETWORK_PLAYER_4_DIR;
                }
                default -> {
                    spawnLoc = Globals.NETWORK_PLAYER_1_LOC;
                    spawnDir = Globals.NETWORK_PLAYER_1_DIR;
                }
            }
        } else {
            // Local/single player mode: spawn at the bottom center
            switch (playerType) {
                case PLAYER_1 -> {
                    spawnLoc = Globals.INITIAL_PLAYER_1_LOC;
                    spawnDir = Globals.INITIAL_PLAYER_1_DIR;
                }
                case PLAYER_2 -> {
                    spawnLoc = Globals.INITIAL_PLAYER_2_LOC;
                    spawnDir = Globals.INITIAL_PLAYER_2_DIR;
                }
                default -> {
                    spawnLoc = Globals.INITIAL_PLAYER_1_LOC;
                    spawnDir = Globals.INITIAL_PLAYER_1_DIR;
                }
            }
        }

        myTank = (PlayerTank) TankFactory.createTank(
                TankType.PLAYER_TANK,
                Utils.gridLoc2Loc(spawnLoc).x(),
                Utils.gridLoc2Loc(spawnLoc).y(),
                spawnDir);
        myTank.setDir(spawnDir);
        myTank.setPlayerType(playerType);
    }
}