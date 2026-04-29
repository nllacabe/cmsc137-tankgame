package tankgame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Tile {

    public static final int EMPTY = 0;
    public static final int BRICK = 1;
    public static final int STEEL = 2;
    public static final int BUSH = 3;

    private int type;
    private int health;

    public Tile(int type) {
        this.type = type;
        this.health = (type == BRICK) ? Constants.BRICK_MAX_HEALTH : -1;
    }

    public boolean isSolid() {
        return type == BRICK || type == STEEL;
    }

    public boolean allowsBullet() {
        return type == EMPTY || type == BUSH;
    }

    public boolean damage() {
        if (type != BRICK)
            return false;
        health--;
        if (health <= 0) {
            type = EMPTY;
            return true;
        }
        return false;
    }

    public int getType() {
        return type;
    }

    public void draw(Graphics2D g, int x, int y) {
        int s = Constants.TILE_SIZE;

        switch (type) {
            case BRICK:
                drawBrick(g, x, y, s);
                break;
            case STEEL:
                drawSteel(g, x, y, s);
                break;
            case BUSH:
                drawBush(g, x, y, s);
                break;
            default:
                // empty — nothing to draw
                break;
        }
    }

    private void drawBrick(Graphics2D g, int x, int y, int s) {
        BufferedImage img = AssetLoader.get("brick");
        if (img != null) {
            g.drawImage(img, x, y, s, s, null);
        } else {
            // Retro brick pattern
            g.setColor(Constants.COLOR_BRICK);
            g.fillRect(x, y, s, s);
            g.setColor(Constants.COLOR_BRICK_DARK);
            // horizontal mortar lines
            g.drawLine(x, y + s / 2, x + s, y + s / 2);
            // vertical mortar lines (offset per row)
            g.drawLine(x + s / 2, y, x + s / 2, y + s / 2);
            g.drawLine(x + s / 4, y + s / 2, x + s / 4, y + s);
            g.drawLine(x + 3 * s / 4, y + s / 2, x + 3 * s / 4, y + s);
            // show damage with cracks
            if (health < Constants.BRICK_MAX_HEALTH) {
                g.setColor(new Color(0, 0, 0, 100));
                g.drawLine(x + 4, y + 4, x + s - 4, y + s - 4);
                g.drawLine(x + s - 4, y + 4, x + 4, y + s - 4);
            }
        }
    }

    private void drawSteel(Graphics2D g, int x, int y, int s) {
        BufferedImage img = AssetLoader.get("steel");
        if (img != null) {
            g.drawImage(img, x, y, s, s, null);
        } else {
            g.setColor(Constants.COLOR_STEEL);
            g.fillRect(x, y, s, s);
            g.setColor(Constants.COLOR_STEEL_DARK);
            g.drawRect(x + 2, y + 2, s - 4, s - 4);
            // rivet dots
            int r = 3;
            g.fillOval(x + 4, y + 4, r, r);
            g.fillOval(x + s - 7, y + 4, r, r);
            g.fillOval(x + 4, y + s - 7, r, r);
            g.fillOval(x + s - 7, y + s - 7, r, r);
        }
    }

    private void drawBush(Graphics2D g, int x, int y, int s) {
        g.setColor(Constants.COLOR_BUSH);
        g.fillRect(x, y, s, s);
        // small leaf circles
        Color darker = new Color(20, 90, 35, 160);
        g.setColor(darker);
        g.fillOval(x + 2, y + 2, 12, 12);
        g.fillOval(x + 14, y + 6, 14, 14);
        g.fillOval(x + 4, y + 16, 10, 10);
        g.fillOval(x + 18, y + 18, 12, 12);
    }
}
