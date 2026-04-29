package tankgame;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// bullet logic, but only moves upward for now.
public class Bullet {

    private double x, y;
    private double dx, dy;
    private int width, height;
    private boolean alive;

    public Bullet(double x, double y, double dx, double dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.width = Constants.BULLET_WIDTH;
        this.height = Constants.BULLET_HEIGHT;
        this.alive = true;
    }

    public void update() {
        if (!alive)
            return;
        x += dx;
        y += dy;

        // off-screen
        if (y + height < 0 || y > Constants.WINDOW_HEIGHT ||
                x + width < 0 || x > Constants.WINDOW_WIDTH) {
            alive = false;
        }
    }

    public void destroy() {
        alive = false;
    }

    // rendering, pwede palitan
    public void draw(Graphics2D g) {
        if (!alive)
            return;

        BufferedImage img = AssetLoader.get("bullet");
        if (img != null) {
            g.drawImage(img, (int) x, (int) y, width, height, null);
        } else {
            drawFallback(g);
        }
    }

    private void drawFallback(Graphics2D g) {
        int ix = (int) x;
        int iy = (int) y;

        // glow
        g.setColor(Constants.COLOR_BULLET_GLOW);
        g.fillOval(ix - 3, iy - 3, width + 6, height + 6);

        // core
        g.setColor(Constants.COLOR_BULLET);
        g.fillOval(ix, iy, width, height);
    }

    // getters

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isAlive() {
        return alive;
    }
}
