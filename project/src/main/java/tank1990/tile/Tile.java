package tank1990.tile;

import java.awt.*;

import tank1990.core.*;
import tank1990.projectiles.Bullet;
import tank1990.tank.AbstractTank;

/**
 * * @class Tile
 * 
 * @brief Represents a tile in the game map.
 * @details This class extends StaticGameObject and provides functionality for
 *          different types of tiles,
 *          including their type, block configuration, sprite animation, and
 *          interaction with tanks and bullets.
 */
public abstract class Tile extends StaticGameObject {
    private static final long serialVersionUID = -8644477400178611263L;
    protected TileType type = null;
    protected BlockConfiguration blockConf = BlockConfiguration.BLOCK_CONF_FULL;
    protected SpriteAnimation spriteAnimationFX = null;
    protected boolean isDestroyed = false;

    protected AbstractTank includedTankInst = null;

    private GridLocation gloc = null;

    private boolean isCoordinatesUpdated = false;

    protected Boolean[][] subpieces = null;

    /**
     * Constructor for Tile.
     * Initializes the tile with the specified indices, type, and default block
     * configuration.
     *
     * @param x    The column index of the tile in the map.
     * @param y    The row index of the tile in the map.
     * @param type The type of the tile.
     */
    public Tile(int x, int y, TileType type) {
        setX(x);
        setY(y);
        setDir(Direction.DIRECTION_INVALID);

        this.type = type;
        this.blockConf = BlockConfiguration.BLOCK_CONF_FULL;
        this.subpieces = new Boolean[Globals.TILE_SUBDIVISION][Globals.TILE_SUBDIVISION];
        setSubPieceVisibility(this.blockConf);
    }

    /**
     * Constructor for Tile.
     * Initializes the tile with the specified indices, type, and block
     * configuration.
     *
     * @param x         The column index of the tile in the map.
     * @param y         The row index of the tile in the map.
     * @param type      The type of the tile.
     * @param blockConf The block configuration of the tile.
     */
    public Tile(int x, int y, TileType type, BlockConfiguration blockConf) {
        setX(x); // By default, set as index, then it will be updated in draw method for actual
                 // X-Y coordinate
        setY(y); // By default, set as index, then it will be updated in draw method for actual
                 // X-Y coordinate
        setDir(Direction.DIRECTION_INVALID);

        this.type = type;
        this.blockConf = blockConf;
        this.gloc = new GridLocation(y, x); // Initialize grid location based on row (y) and column (x) indices
        this.subpieces = new Boolean[Globals.TILE_SUBDIVISION][Globals.TILE_SUBDIVISION];
        setSubPieceVisibility(this.blockConf);
    }

    /**
     * Returns the type of the tile.
     * 
     * @return the type of the tile
     */
    public TileType getType() {
        return this.type;
    }

    /**
     * Returns the block configuration of the tile.
     * 
     * @return the block configuration of the tile
     */
    public BlockConfiguration getBlockConf() {
        return this.blockConf;
    }

    /**
     * Returns the grid location of the tile.
     * 
     * @return the grid location of the tile
     */
    public GridLocation getGridLocation() {
        return this.gloc;
    }

    public void update() {
    }

    public void draw(Graphics g) {
        if (this.spriteAnimationFX == null) {
            System.err.println("Tile textureFX is null for tile: " + this.type);
            return;
        }

        // Always recompute size and position from clip bounds every frame.
        // Never cache pixel coordinates — window can be any size at any time.
        java.awt.Rectangle clipBounds = g.getClipBounds();
        int cellW = clipBounds.width / Globals.COL_TILE_COUNT;
        int cellH = clipBounds.height / Globals.ROW_TILE_COUNT;

        setSize(new java.awt.Dimension(cellW, cellH));

        if (this.gloc != null) {
            setX(this.gloc.colIndex() * cellW + cellW / 2);
            setY(this.gloc.rowIndex() * cellH + cellH / 2);
        } else {
            // Fallback: raw index stored at construction time
            setX(this.x * cellW + cellW / 2);
            setY(this.y * cellH + cellH / 2);
        }

        this.spriteAnimationFX.setTargetSize(cellW, cellH);
        this.spriteAnimationFX.draw(g, x, y, 0.0);

        // Draw black over any missing subpieces (damaged bricks etc.)
        // Top-left of this tile in pixel space:
        int tileLeft = x - cellW / 2;
        int tileTop = y - cellH / 2;
        int subW = cellW / Globals.TILE_SUBDIVISION;
        int subH = cellH / Globals.TILE_SUBDIVISION;

        boolean allSubpiecesDestroyed = true;
        for (int r = 0; r < Globals.TILE_SUBDIVISION; r++) {
            for (int c = 0; c < Globals.TILE_SUBDIVISION; c++) {
                if (!this.subpieces[r][c]) {
                    g.setColor(Color.BLACK);
                    g.fillRect(tileLeft + c * subW, tileTop + r * subH, subW, subH);
                } else {
                    allSubpiecesDestroyed = false;
                }
            }
        }

        // If all sub-pieces are not visible, set the tile as destroyed
        if (allSubpiecesDestroyed) {
            setAsDestroyed();
        }

        if (Globals.SHOW_BOUNDING_BOX) {
            g.setColor(Color.pink);
            g.drawRect((int) getBoundingBox().getX(), (int) getBoundingBox().getY(), (int) getBoundingBox().getWidth(),
                    (int) getBoundingBox().getHeight());
        }
    }

    public boolean includesTank() {
        return this.includedTankInst != null;
    }

    /**
     * Returns true if any subpiece is missing (tile has been partially damaged).
     */
    public boolean hasDamage() {
        if (this.subpieces == null)
            return false;
        for (int r = 0; r < Globals.TILE_SUBDIVISION; r++)
            for (int c = 0; c < Globals.TILE_SUBDIVISION; c++)
                if (!this.subpieces[r][c])
                    return true;
        return false;
    }

    /**
     * Returns subpieces as primitive boolean[][] for network serialization.
     */
    public boolean[][] getSubpiecesAsBoolean() {
        int sz = Globals.TILE_SUBDIVISION;
        boolean[][] result = new boolean[sz][sz];
        for (int r = 0; r < sz; r++)
            for (int c = 0; c < sz; c++)
                result[r][c] = this.subpieces[r][c];
        return result;
    }

    /**
     * Applies subpiece visibility from a network snapshot (slave sync).
     */
    public void applySubpieces(boolean[][] incoming) {
        int sz = Globals.TILE_SUBDIVISION;
        for (int r = 0; r < sz; r++)
            for (int c = 0; c < sz; c++)
                this.subpieces[r][c] = incoming[r][c];
    }

    /**
     * Destroys the tile and returns true if it was successfully destroyed.
     * If the tile cannot be destroyed, it should return false.
     * This method should be overridden by subclasses to implement specific
     * destruction logic.
     * 
     * @param b the bullet that is attempting to destroy the tile
     *
     * @return true if the tile was successfully destroyed, false otherwise
     */
    public abstract boolean destroy(Bullet b);

    /**
     * Checks if the tile is destroyed.
     * 
     * @return true if the tile is destroyed, false otherwise
     */
    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    /**
     * Sets the tile as destroyed.
     * This method should be called when the tile is destroyed. Actual destruction
     * logic is controlled by Game Engine.
     */
    public void setAsDestroyed() {
        this.isDestroyed = true;
    }

    /**
     * Sets the visibility of sub-pieces based on the block configuration.
     * 
     * @param blockConfiguration the block configuration to set visibility for
     *                           sub-pieces
     */
    protected void setSubPieceVisibility(BlockConfiguration blockConfiguration) {
        for (int r = 0; r < Globals.TILE_SUBDIVISION; r++) {
            for (int c = 0; c < Globals.TILE_SUBDIVISION; c++) {
                this.subpieces[r][c] = false;
            }
        }

        if (blockConfiguration == BlockConfiguration.BLOCK_CONF_FULL) {
            for (int r = 0; r < Globals.TILE_SUBDIVISION; r++) {
                for (int c = 0; c < Globals.TILE_SUBDIVISION; c++) {
                    this.subpieces[r][c] = true;
                }
            }
        } else if (blockConfiguration == BlockConfiguration.BLOCK_CONF_1) {
            this.subpieces[0][0] = true;
            this.subpieces[0][1] = true;
            this.subpieces[1][0] = true;
            this.subpieces[1][1] = true;
        } else if (blockConfiguration == BlockConfiguration.BLOCK_CONF_2) {
            this.subpieces[0][2] = true;
            this.subpieces[0][3] = true;
            this.subpieces[1][2] = true;
            this.subpieces[1][3] = true;
        } else if (blockConfiguration == BlockConfiguration.BLOCK_CONF_3) {
            this.subpieces[2][2] = true;
            this.subpieces[2][3] = true;
            this.subpieces[3][2] = true;
            this.subpieces[3][3] = true;
        } else if (blockConfiguration == BlockConfiguration.BLOCK_CONF_4) {
            this.subpieces[2][0] = true;
            this.subpieces[2][1] = true;
            this.subpieces[3][0] = true;
            this.subpieces[3][1] = true;
        } else if (blockConfiguration == BlockConfiguration.BLOCK_CONF_5) {
            this.subpieces[0][0] = true;
            this.subpieces[0][1] = true;
            this.subpieces[0][2] = true;
            this.subpieces[0][3] = true;
            this.subpieces[1][0] = true;
            this.subpieces[1][1] = true;
            this.subpieces[1][2] = true;
            this.subpieces[1][3] = true;
        } else if (blockConfiguration == BlockConfiguration.BLOCK_CONF_6) {
            this.subpieces[0][2] = true;
            this.subpieces[0][3] = true;
            this.subpieces[1][2] = true;
            this.subpieces[1][3] = true;
            this.subpieces[2][2] = true;
            this.subpieces[2][3] = true;
            this.subpieces[3][2] = true;
            this.subpieces[3][3] = true;
        } else if (blockConfiguration == BlockConfiguration.BLOCK_CONF_7) {
            this.subpieces[2][0] = true;
            this.subpieces[2][1] = true;
            this.subpieces[2][2] = true;
            this.subpieces[2][3] = true;
            this.subpieces[3][0] = true;
            this.subpieces[3][1] = true;
            this.subpieces[3][2] = true;
            this.subpieces[3][3] = true;
        } else if (blockConfiguration == BlockConfiguration.BLOCK_CONF_8) {
            this.subpieces[0][0] = true;
            this.subpieces[0][1] = true;
            this.subpieces[1][0] = true;
            this.subpieces[1][1] = true;
            this.subpieces[2][0] = true;
            this.subpieces[2][1] = true;
            this.subpieces[3][0] = true;
            this.subpieces[3][1] = true;
        } else {
        }
    }

    /**
     * Checks if a row or column of sub-pieces is hit based on the direction.
     * 
     * @param hitDir the direction of the hit
     * @return true if a row or column was hit, false otherwise
     */
    protected boolean hitRow(Direction hitDir) {
        boolean isHit = false;
        if (hitDir == Direction.DIRECTION_DOWNWARDS) {
            for (int r = 0; r < Globals.TILE_SUBDIVISION; r++) {
                if (this.subpieces[r][0] || this.subpieces[r][1] |
                        this.subpieces[r][2] || this.subpieces[r][3]) {
                    this.subpieces[r][0] = false;
                    this.subpieces[r][1] = false;
                    this.subpieces[r][2] = false;
                    this.subpieces[r][3] = false;
                    isHit = true;
                    break;
                }
            }
        } else { // Direction.DIRECTION_UPWARDS
            for (int r = Globals.TILE_SUBDIVISION - 1; r >= 0; r--) {
                if (this.subpieces[r][0] || this.subpieces[r][1] |
                        this.subpieces[r][2] || this.subpieces[r][3]) {
                    this.subpieces[r][0] = false;
                    this.subpieces[r][1] = false;
                    this.subpieces[r][2] = false;
                    this.subpieces[r][3] = false;
                    isHit = true;
                    break;
                }
            }
        }
        return isHit;
    }

    /**
     * Checks if a column of sub-pieces is hit based on the direction.
     * 
     * @param hitDir the direction of the hit
     * @return true if a column was hit, false otherwise
     */
    protected boolean hitColumn(Direction hitDir) {
        boolean isHit = false;
        if (hitDir == Direction.DIRECTION_LEFT) {
            for (int c = Globals.TILE_SUBDIVISION - 1; c >= 0; c--) {
                if (this.subpieces[0][c] || this.subpieces[1][c] |
                        this.subpieces[2][c] || this.subpieces[3][c]) {
                    this.subpieces[0][c] = false;
                    this.subpieces[1][c] = false;
                    this.subpieces[2][c] = false;
                    this.subpieces[3][c] = false;
                    isHit = true;
                    break;
                }
            }
        } else { // Direction.DIRECTION_RIGHT
            for (int c = 0; c < Globals.TILE_SUBDIVISION; c++) {
                if (this.subpieces[0][c] || this.subpieces[1][c] |
                        this.subpieces[2][c] || this.subpieces[3][c]) {
                    this.subpieces[0][c] = false;
                    this.subpieces[1][c] = false;
                    this.subpieces[2][c] = false;
                    this.subpieces[3][c] = false;
                    isHit = true;
                    break;
                }
            }
        }
        return isHit;
    }

    /**
     * Handles a hit on the tile based on the direction.
     * If the hit is vertical (upwards or downwards), it checks the rows.
     * If the hit is horizontal (left or right), it checks the columns.
     * If no sub-pieces were hit, it sets the tile as destroyed.
     *
     * @param hitDir the direction of the hit
     */
    protected void hit(Direction hitDir) {
        boolean isHit;

        if (hitDir == Direction.DIRECTION_DOWNWARDS ||
                hitDir == Direction.DIRECTION_UPWARDS) {
            isHit = hitRow(hitDir);
        } else {
            isHit = hitColumn(hitDir);
        }

        // If no sub-pieces were hit, set the tile as destroyed
        if (!isHit)
            setAsDestroyed();
    }

    public RectangleBound getBoundingBox() {
        int minRow = Globals.TILE_SUBDIVISION;
        int maxRow = -1;
        int minCol = Globals.TILE_SUBDIVISION;
        int maxCol = -1;

        // Find the bounds of visible sub-pieces
        boolean hasVisibleSubpieces = false;
        for (int r = 0; r < Globals.TILE_SUBDIVISION; r++) {
            for (int c = 0; c < Globals.TILE_SUBDIVISION; c++) {
                if (this.subpieces[r][c]) {
                    hasVisibleSubpieces = true;
                    minRow = Math.min(minRow, r);
                    maxRow = Math.max(maxRow, r);
                    minCol = Math.min(minCol, c);
                    maxCol = Math.max(maxCol, c);
                }
            }
        }

        // If no visible sub-pieces, return empty bounding box
        if (!hasVisibleSubpieces) {
            return new RectangleBound(getX(), getY(), 0, 0);
        }

        // Calculate sub-piece dimensions
        int subpieceWidth = getSize().width / Globals.TILE_SUBDIVISION;
        int subpieceHeight = getSize().height / Globals.TILE_SUBDIVISION;

        // Calculate the actual bounding box dimensions
        int boundingWidth = (maxCol - minCol + 1) * subpieceWidth;
        int boundingHeight = (maxRow - minRow + 1) * subpieceHeight;

        // Calculate the top-left position of the visible area (not center)
        int topLeftX = (getX() - getSize().width / 2) + (minCol * subpieceWidth);
        int topLeftY = (getY() - getSize().height / 2) + (minRow * subpieceHeight);

        // Return bounding box with top-left position
        return new RectangleBound(topLeftX, topLeftY, boundingWidth, boundingHeight);
    }

    public String toString() {
        return "Tile{" +
                "type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", width=" + getSize().width +
                ", height=" + getSize().height +
                ", LocX=" + Utils.gridLoc2Loc(this.gloc).x() +
                ", LocY=" + Utils.gridLoc2Loc(this.gloc).y() +
                '}';
    }
}