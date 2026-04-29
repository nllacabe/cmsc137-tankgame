package tankgame;

import java.awt.Graphics2D;

/**
 * Tile-based arena map. Holds a 2D grid of tiles and provides
 * collision queries and spawn positions.
 */
public class TileMap {

    private Tile[][] grid;
    private int cols, rows;

    public TileMap() {
        this.cols = Constants.MAP_COLS;
        this.rows = Constants.MAP_ROWS;
        this.grid = new Tile[rows][cols];
        loadDefaultMap();
    }

    /**
     * Hardcoded single arena layout.
     *
     * Legend:
     *   . = empty
     *   B = brick (destructible)
     *   S = steel (indestructible)
     *   G = bush (cosmetic)
     */
    private void loadDefaultMap() {
        String[] layout = {
            ".........................",  // row 0  (top)
            ".........................",  // row 1
            "..B..B..S..B.S..B..B....",  // row 2
            ".........................",  // row 3
            "....B......B......B.....",  // row 4
            "..S....BB.....BB....S...",  // row 5
            ".........................",  // row 6
            "....S..........S........",  // row 7
            "........BB.BB...........",  // row 8
            "..B...............B.....",  // row 9
            "..B...............B.....",  // row 10
            "........BB.BB...........",  // row 11
            "....S..........S........",  // row 12
            ".........................",  // row 13
            "..S....BB.....BB....S...",  // row 14
            "....B......B......B.....",  // row 15
            ".........................",  // row 16
            "..G..G.....G.....G..G...",  // row 17
            ".........................",  // row 18
            ".........................",  // row 19 (bottom)
        };

        for (int r = 0; r < rows; r++) {
            String row = (r < layout.length) ? layout[r] : "";
            for (int c = 0; c < cols; c++) {
                char ch = (c < row.length()) ? row.charAt(c) : '.';
                grid[r][c] = new Tile(charToType(ch));
            }
        }
    }

    private int charToType(char ch) {
        switch (ch) {
            case 'B': return Tile.BRICK;
            case 'S': return Tile.STEEL;
            case 'G': return Tile.BUSH;
            default:  return Tile.EMPTY;
        }
    }

    // ── Collision ───────────────────────────────────────────

    /**
     * Check if a bounding box overlaps any solid tile.
     */
    public boolean isBlocked(double x, double y, int w, int h) {
        int startCol = Math.max(0, (int)(x) / Constants.TILE_SIZE);
        int endCol   = Math.min(cols - 1, (int)(x + w - 1) / Constants.TILE_SIZE);
        int startRow = Math.max(0, (int)(y) / Constants.TILE_SIZE);
        int endRow   = Math.min(rows - 1, (int)(y + h - 1) / Constants.TILE_SIZE);

        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                if (grid[r][c].isSolid()) return true;
            }
        }
        return false;
    }

    /**
     * Check if a bullet hits a solid tile. If it's a brick, damage it.
     * @return true if the bullet should be destroyed
     */
    public boolean bulletHitsTile(double x, double y, int w, int h) {
        int startCol = Math.max(0, (int)(x) / Constants.TILE_SIZE);
        int endCol   = Math.min(cols - 1, (int)(x + w - 1) / Constants.TILE_SIZE);
        int startRow = Math.max(0, (int)(y) / Constants.TILE_SIZE);
        int endRow   = Math.min(rows - 1, (int)(y + h - 1) / Constants.TILE_SIZE);

        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                Tile t = grid[r][c];
                if (t.isSolid()) {
                    t.damage(); // does nothing to steel
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the tile at a given row/col. Returns null if out of bounds.
     */
    public Tile getTileAt(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return null;
        return grid[row][col];
    }

    // ── Rendering ───────────────────────────────────────────

    public void draw(Graphics2D g) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].draw(g, c * Constants.TILE_SIZE, r * Constants.TILE_SIZE);
            }
        }
    }

    // ── Spawn helpers ───────────────────────────────────────

    /** Player spawn: bottom center */
    public double getPlayerSpawnX() {
        return (cols * Constants.TILE_SIZE) / 2.0 - Constants.TANK_WIDTH / 2.0;
    }

    public double getPlayerSpawnY() {
        return (rows - 2) * Constants.TILE_SIZE;
    }


    public int getCols() { return cols; }
    public int getRows() { return rows; }
}
