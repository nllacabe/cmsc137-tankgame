package tank1990.tile;

/**
 * @class TileType
 * @brief Enum representing the different types of tiles in the game.
 * @details Each tile type has specific properties such as whether it is passable and the cost of moving through it.
 */
public enum TileType {
    TILE_NONE,
    TILE_BRICKS,
    TILE_STEEL,
    TILE_TREES,
    TILE_SEA,
    TILE_ICE,
    TILE_EAGLE;

    public static TileType valueOf(int val) {
        return switch (val) {
            case (0) -> TILE_NONE;
            case (1) -> TILE_BRICKS;
            case (2) -> TILE_STEEL;
            case (3) -> TILE_TREES;
            case (4) -> TILE_SEA;
            case (5) -> TILE_ICE;
            case (6) -> TILE_EAGLE;
            default -> throw new RuntimeException("Invalid Value for TileType");
        };
    }

    /**
     * Checks if a tile of the given type is passable.
     * @param type The type of the tile.
     * @return True if the tile is passable, false otherwise.
     */
    public static boolean isPassable(TileType type) {
        return switch (type) {
            case TILE_NONE, TILE_BRICKS, TILE_TREES, TILE_ICE, TILE_EAGLE-> true;
            case TILE_STEEL, TILE_SEA  -> false;
        };
    }

    /**
     * Returns the cost of moving through a tile of the given type.
     * @param type The type of the tile.
     * @return The cost associated with the tile type.
     */
    public static int getCost(TileType type) {
        return switch (type) {
            case TILE_NONE -> 1;
            case TILE_BRICKS -> 10;
            case TILE_TREES -> 1;
            case TILE_ICE -> 2;
            case TILE_EAGLE -> 1;
            case TILE_STEEL, TILE_SEA -> Integer.MAX_VALUE; // Not passable
        };
    }
}
