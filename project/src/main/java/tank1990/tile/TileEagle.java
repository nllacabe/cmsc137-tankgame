package tank1990.tile;

import tank1990.core.Globals;
import tank1990.core.SpriteAnimation;
import tank1990.core.TextureFX;
import tank1990.projectiles.Bullet;

/**
 * @class TileEagle
 * @brief Represents the Eagle tile in the game.
 * @details This tile is used to represent the Eagle, which can be destroyed by a bullet.
 */
public class TileEagle extends Tile {
    public TileEagle(int x, int y, BlockConfiguration blockConf) {
        super(x, y, TileType.TILE_EAGLE, blockConf);

        this.spriteAnimationFX = new SpriteAnimation(Globals.TEXTURE_TILE_EAGLE_SPRITE);
    }

    public boolean destroy(Bullet b) {
        // Set the texture to Withdraw
        this.spriteAnimationFX = new SpriteAnimation(Globals.TEXTURE_TILE_WITHDRAW_SPRITE);
        return true;
    }

}
