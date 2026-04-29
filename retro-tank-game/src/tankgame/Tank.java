package tankgame;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// as of now, single player tank only.
public class Tank {

    private double x, y;
    private int width, height;
    private int health, maxHealth;
    private double speed;
    private boolean alive;
    private long lastShotTime;

    public Tank(double x, double y) {
        this.x = x;
        this.y = y;
        this.width = Constants.TANK_WIDTH;
        this.height = Constants.TANK_HEIGHT;
        this.maxHealth = Constants.TANK_MAX_HEALTH;
        this.health = maxHealth;
        this.speed = Constants.TANK_SPEED;
        this.alive = true;
        this.lastShotTime = 0;
    }

    // update

    public void update(InputHandler input, TileMap map) {
        if (!alive)
            return;

        double nx = x;
        double ny = y;

        if (input.up())
            ny -= speed;
        if (input.down())
            ny += speed;
        if (input.left())
            nx -= speed;
        if (input.right())
            nx += speed;

        nx = Math.max(0, Math.min(nx, Constants.WINDOW_WIDTH - width));
        ny = Math.max(0, Math.min(ny, Constants.WINDOW_HEIGHT - height));

        // tile collisions for X movement
        if (!map.isBlocked(nx, y, width, height)) {
            x = nx;
        }
        // tile collisions for Y movement
        if (!map.isBlocked(x, ny, width, height)) {
            y = ny;
        }
    }

    // attempt to fire a bullet.
    public Bullet tryShoot() {
        if (!alive)
            return null;

        long now = System.currentTimeMillis();
        if (now - lastShotTime < Constants.SHOOT_COOLDOWN)
            return null;

        lastShotTime = now;

        // bullet spawns at top-center of tank, moving upward
        double bx = x + width / 2.0 - Constants.BULLET_WIDTH / 2.0;
        double by = y - Constants.BULLET_HEIGHT;
        return new Bullet(bx, by, 0, -Constants.BULLET_SPEED);
    }

    // dmg logic

    public void takeDamage() {
        if (!alive)
            return;
        health--;
        if (health <= 0) {
            alive = false;
        }
    }

    // rendering

    public void draw(Graphics2D g) {
        if (!alive)
            return;

        BufferedImage img = AssetLoader.get("tank");
        if (img != null) {
            g.drawImage(img, (int) x, (int) y, width, height, null);
        } else {
            drawFallback(g);
        }
    }

    private void drawFallback(Graphics2D g) {
        int ix = (int) x;
        int iy = (int) y;

        // tank body
        g.setColor(Constants.COLOR_PLAYER_TANK);
        g.fillRoundRect(ix + 2, iy + 6, width - 4, height - 6, 4, 4);

        g.setColor(Constants.COLOR_PLAYER_TANK.darker());
        g.fillRect(ix, iy + 6, 3, height - 6);
        g.fillRect(ix + width - 3, iy + 6, 3, height - 6);

        g.setColor(Constants.COLOR_PLAYER_BARREL);
        int barrelW = 4;
        int barrelH = 10;
        g.fillRect(ix + width / 2 - barrelW / 2, iy - barrelH + 8, barrelW, barrelH);

        g.setColor(Constants.COLOR_PLAYER_TANK.brighter());
        int turretSize = 10;
        g.fillOval(ix + width / 2 - turretSize / 2, iy + height / 2 - turretSize / 2 + 2,
                turretSize, turretSize);
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

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isAlive() {
        return alive;
    }

    public double getCenterX() {
        return x + width / 2.0;
    }

    public double getCenterY() {
        return y + height / 2.0;
    }
}
