package tank1990.tile;

import tank1990.core.Globals;
import tank1990.core.SpriteAnimation;
import tank1990.core.TextureFX;
import tank1990.projectiles.Bullet;

/**
 * @class TileBricks
 * @brief Represents a bricks tile in the game.
 * @details This class extends Tile and initializes the bricks tile with its specific texture and sprite animation.
 */
public class TileBricks extends Tile {
    public TileBricks(int x, int y, BlockConfiguration blockConf) {
        super(x, y, TileType.TILE_BRICKS, blockConf);

        this.spriteAnimationFX = new SpriteAnimation(Globals.TEXTURE_TILE_BRICKS_SPRITE);
    }

    public boolean destroy(Bullet b) {
        hit(b.getDir());

        return true;
    }

}
