package tank1990.tile;

/**
 * @class TileFactory
 * @brief Factory class for creating different types of tiles.
 * @details This class provides a static method to create tiles based on the specified TileType.
 */
public class TileFactory {
    public static Tile createTile(TileType type, int x, int y, BlockConfiguration blockConf) {
        if (type == null) {
            throw new IllegalArgumentException("Tank type cannot be null!");
        }

        return switch (type) {
            case TILE_BRICKS -> new TileBricks(x, y, blockConf);
            case TILE_STEEL -> new TileSteel(x, y, blockConf);
            case TILE_TREES -> new TileTrees(x, y, blockConf);
            case TILE_SEA -> new TileSea(x, y, blockConf);
            case TILE_ICE -> new TileIce(x, y, blockConf);
            case TILE_EAGLE -> new TileEagle(x, y, blockConf);
            default -> null;
        };
    }
}
