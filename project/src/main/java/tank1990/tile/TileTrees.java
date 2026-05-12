package tank1990.tile;

import tank1990.core.Globals;
import tank1990.core.SpriteAnimation;
import tank1990.core.TextureFX;
import tank1990.projectiles.Bullet;
import tank1990.projectiles.BulletType;

/**
 * @class TileTrees
 * @brief Represents a tile containing trees in the game.
 * @details This tile is part of the game map and can be destroyed by upgraded bullets.
 */
public class TileTrees extends Tile {
    public TileTrees(int x, int y, BlockConfiguration blockConf) {
        super(x, y, TileType.TILE_TREES, blockConf);
        setDepth(1);

        this.spriteAnimationFX = new SpriteAnimation(Globals.TEXTURE_TILE_TREES_SPRITE);
    }

    public boolean destroy(Bullet b) {
        if (b.getType() == BulletType.UPGRADED) setAsDestroyed();
        return false;
    }
}
