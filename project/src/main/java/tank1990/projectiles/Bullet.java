package tank1990.projectiles;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import tank1990.core.Utils;
import tank1990.core.Direction;
import tank1990.core.DynamicGameObject;
import tank1990.core.GameLevel;
import tank1990.core.Globals;
import tank1990.core.GridLocation;
import tank1990.core.Location;
import tank1990.tank.AbstractTank;
import tank1990.tank.Enemy;
import tank1990.tile.Tile;
import tank1990.tile.TileBricks;
import tank1990.tile.TileEagle;
import tank1990.tile.TileIce;
import tank1990.tile.TileSea;
import tank1990.tile.TileSteel;
import tank1990.tile.TileTrees;

/**
 * @class Bullet
 * @brief Represents a bullet fired by a tank in the game.
 * @details This class handles the bullet's position, direction, speed, and
 *          rendering.
 */
public class Bullet extends DynamicGameObject {

    private AbstractTank tankInst = null;
    private int speed;
    private int baseSpeed;
    private BulletType type;

    public Bullet(AbstractTank tankInst, int x, int y, Direction dir, int speed) {
        this.tankInst = tankInst;
        setX(x);
        setY(y);
        setDir(dir);
        setSize(new Dimension(Globals.BULLET_WIDTH, Globals.BULLET_HEIGHT));
        this.baseSpeed = speed;
        this.speed = baseSpeed;
        this.type = BulletType.NORMAL;
    }

    /**
     * Display-only constructor used by slave nodes to render bullets
     * received in a FullGameSnapshot. No tank reference needed.
     */
    public Bullet(int x, int y, Direction dir) {
        this.tankInst = null;
        setX(x);
        setY(y);
        setDir(dir);
        setSize(new Dimension(Globals.BULLET_WIDTH, Globals.BULLET_HEIGHT));
        this.baseSpeed = 0;
        this.speed = 0;
        this.type = BulletType.NORMAL;
    }

    public Bullet(AbstractTank tankInst, int x, int y, Direction dir, int speed, BulletType type) {
        this.tankInst = tankInst;
        setX(x);
        setY(y);
        setDir(dir);
        setSize(new Dimension(Globals.BULLET_WIDTH, Globals.BULLET_HEIGHT));
        this.baseSpeed = speed;
        this.speed = baseSpeed;
        this.type = type;
    }

    @Override
    public void draw(Graphics g) {
        Dimension dim = Utils.normalizeDimension(g, Globals.BULLET_WIDTH, Globals.BULLET_HEIGHT);
        this.speed = Utils.normalize(g, this.baseSpeed);
        setSize(dim);

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();
        g2d.setColor(Globals.COLOR_GRAY);
        g2d.translate(x, y);

        if (this.dir == Direction.DIRECTION_DOWNWARDS || this.dir == Direction.DIRECTION_UPWARDS) {
            g2d.fillRect((int) -dim.getWidth(), (int) -dim.getHeight(),
                    (int) dim.getWidth(), (int) dim.getHeight());
        } else {
            g2d.fillRect((int) -dim.getHeight(), (int) -dim.getWidth(),
                    (int) dim.getHeight(), (int) dim.getWidth());
        }

        g2d.setTransform(oldTransform);
    }

    public void update(GameLevel level) {
        // System.out.printf("Bullet update: x=%d, y=%d, dir=%s, speed=%d%n", x, y, dir,
        // speed);
        switch (this.dir) {
            case DIRECTION_UPWARDS:
                y -= (int) this.speed;
                break;
            case DIRECTION_RIGHT:
                x += (int) this.speed;
                break;
            case DIRECTION_DOWNWARDS:
                y += (int) this.speed;
                break;
            case DIRECTION_LEFT:
                x -= (int) this.speed;
                break;
            default:
                break;
        }
    }

    /**
     * Gets the current speed of the bullet.
     *
     * @return The current speed of the bullet.
     */
    public BulletType getType() {
        return this.type;
    }

    /**
     * Sets the type of the bullet.
     *
     * @param type The new type to set for the bullet.
     */
    public void setType(BulletType type) {
        this.type = type;
    }

    /**
     * Sets the speed of the bullet.
     *
     * @param speed The new speed to set for the bullet.
     */
    public void setMoveSpeed(int speed) {
        this.baseSpeed = speed;
    }

    /**
     * Destroys the bullet and returns a new Blast object at the current bullet
     * position.
     * This method sets the bullet status in the tank instance to true,
     * indicating that the bullet has been destroyed.
     * 
     * @return A new Blast object representing the explosion
     */
    public Blast destroy() {
        this.tankInst.setBulletStatus(true);
        // Create a blast at the current bullet position
        return new Blast(x, y);

    }

    /**
     * Checks if the projectile is out of bounds, meaning it has gone beyond the
     * specified area (width and height).
     * 
     * @param width  The width of the game area.
     * @param height The height of the game area.
     * @return true if the projectile is out of bounds, false otherwise.
     */
    public boolean isOutOfBounds(int width, int height) {
        return this.x < 0 || this.x > width || this.y < 0 || this.y > height;
    }

    /**
     * Checks if the bullet belongs to an enemy tank.
     *
     * @return true if the bullet is fired by an enemy tank, false otherwise.
     */
    public boolean isEnemyBullet() {
        return this.tankInst instanceof Enemy;
    }

    /**
     * Returns true if this bullet was fired by the given player's tank.
     * Used for friendly-fire detection in network multiplayer.
     */
    public boolean isOwnedBy(tank1990.player.Player player) {
        return player.getTank() == this.tankInst;
    }

    /**
     * Checks for collision with the game level's tile map. (DEPRECATED)
     * 
     * @param gameLevel The current game level to check for collisions.
     * @return true if the bullet collides with a tile that should destroy it (like
     *         bricks or eagle), false otherwise.
     */
    public boolean checkCollision(GameLevel gameLevel) {
        if (gameLevel == null || gameLevel.getMap() == null) {
            return false;
        }

        // Get the tile map
        Tile[][] map = gameLevel.getMap();

        // Get game area dimensions
        Dimension gameAreaSize = gameLevel.getGameAreaSize();
        int maxWidth = (int) gameAreaSize.getWidth();
        int maxHeight = (int) gameAreaSize.getHeight();

        // Convert bullet position to grid location
        GridLocation gLoc = Utils.loc2GridLoc(new Location(this.x, this.y));

        int r = gLoc.rowIndex();
        int c = gLoc.colIndex();

        // Check bounds
        if (r < 0 || r >= Globals.ROW_TILE_COUNT || c < 0 || c >= Globals.COL_TILE_COUNT) {
            return false;
        }

        Tile currentTile = map[r][c];
        if (currentTile == null) {
            return false;
        }

        // Handle different tile types
        if (currentTile instanceof TileBricks) {
            // Destroy both bullet and brick tile
            map[r][c] = null; // Remove the brick tile
            return true; // Bullet should be destroyed
        } else if (currentTile instanceof TileSteel) {
            return true;
        } else if (currentTile instanceof TileEagle) {
            // Destroy both bullet and eagle (game over condition could be handled here)
            map[r][c] = null; // Remove the eagle tile
            return true; // Bullet should be destroyed
        } else if (currentTile instanceof TileTrees) {
            return false;
        } else if (currentTile instanceof TileSea) {
            return false;
        } else if (currentTile instanceof TileIce) {
            return false;
        }

        // For any other tile types, bullet passes through
        return false;
    }

    public String toString() {
        return String.format("Bullet [x=%d, y=%d, dir=%s, speed=%d]", x, y, dir, speed);
    }

}