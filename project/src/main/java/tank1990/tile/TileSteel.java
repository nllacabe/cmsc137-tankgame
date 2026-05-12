package tank1990.tile;

import tank1990.core.Globals;
import tank1990.core.SpriteAnimation;
import tank1990.core.TextureFX;
import tank1990.projectiles.Bullet;
import tank1990.projectiles.BulletType;

/**
 * @class TileSteel
 * @brief Represents a steel tile in the game.
 * @details This tile is indestructible by normal bullets and can only be damaged by upgraded bullets.
 */
public class TileSteel extends Tile {
    public TileSteel(int x, int y, BlockConfiguration blockConf) {
        super(x, y, TileType.TILE_STEEL, blockConf);

        this.spriteAnimationFX = new SpriteAnimation(Globals.TEXTURE_TILE_STEEL_SPRITE);
    }

    public boolean destroy(Bullet b) {
        if (b.getType() == BulletType.UPGRADED) hit(b.getDir());
        return true;
    }
}
