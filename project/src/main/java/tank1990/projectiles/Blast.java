package tank1990.projectiles;

import java.awt.Dimension;
import java.awt.Graphics;

import tank1990.core.DynamicGameObject;
import tank1990.core.Globals;
import tank1990.core.SpriteAnimation;
import tank1990.core.Utils;

/**
 * @class Blast
 * @brief Represents a blast effect in the game.
 * @details This class handles the visual representation of a blast, including its animation and drawing on the screen.
 */
public class Blast extends DynamicGameObject {
    protected SpriteAnimation spriteAnimation;

    public Blast(int x, int y) {
        setX(x);
        setY(y);
        setSize(new Dimension(Globals.BLAST_WIDTH, Globals.BLAST_HEIGHT));
        this.spriteAnimation = new SpriteAnimation(Globals.BLAST_ANIMATION);
        this.spriteAnimation.setRepeat(1);  // Do not repeat the blast animation
    }

    public boolean update() {
        return this.spriteAnimation.update();
    }

    @Override
    public void draw(Graphics g) {
        Dimension nDim = Utils.normalizeDimension(g, Globals.BLAST_WIDTH, Globals.BLAST_HEIGHT);
        
        this.spriteAnimation.setTargetSize(nDim.width, nDim.height);
        this.spriteAnimation.draw(g, x - nDim.width/2, y - nDim.height/2, 0);
    }

}
