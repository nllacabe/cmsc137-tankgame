package tank1990.tile;

import tank1990.core.Globals;
import tank1990.core.SpriteAnimation;
import tank1990.core.TextureFX;
import tank1990.projectiles.Bullet;

/**
 * @class TileSea
 * @brief Represents a sea tile in the game.
 * @details This class extends Tile and initializes the sea tile with its specific sprite animation.
 */
public class TileSea extends Tile {
    public TileSea(int x, int y, BlockConfiguration blockConf) {
        super(x, y, TileType.TILE_SEA, blockConf);

        this.spriteAnimationFX = new SpriteAnimation(Globals.TEXTURE_TILE_SEA_SPRITE);
    }

    @Override
    public void update() {
        if (this.spriteAnimationFX != null) {
            this.spriteAnimationFX.update();
        }
    }

    public boolean destroy(Bullet b) {
        return false;
    }
}
