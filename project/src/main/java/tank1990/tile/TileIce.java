package tank1990.tile;

import tank1990.core.Globals;
import tank1990.core.SpriteAnimation;
import tank1990.core.TextureFX;
import tank1990.projectiles.Bullet;

/**
 * @class TileIce
 * @brief Represents an ice tile in the game.
 * @details This tile is a type of terrain that increases the speed of tanks moving over it.
 */
public class TileIce extends Tile {
    public TileIce(int x, int y, BlockConfiguration blockConf) {
        super(x, y, TileType.TILE_ICE, blockConf);

        this.spriteAnimationFX = new SpriteAnimation(Globals.TEXTURE_TILE_ICE_SPRITE);
    }

    public boolean destroy(Bullet b) {
        return false;
    }
}
