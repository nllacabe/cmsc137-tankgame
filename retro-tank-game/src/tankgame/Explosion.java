package tankgame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

// explosion particle effect (e.g. when a wall is destroyed)
public class Explosion {

    private double centerX, centerY;
    private double[] px, py; // particle positions
    private double[] vx, vy; // particle velocities
    private int[] sizes;
    private Color[] colors;
    private int lifetime;
    private int frame;
    private boolean finished;

    private static final Random rand = new Random();

    public Explosion(double centerX, double centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.lifetime = Constants.EXPLOSION_LIFETIME;
        this.frame = 0;
        this.finished = false;

        int count = Constants.EXPLOSION_PARTICLE_COUNT;
        px = new double[count];
        py = new double[count];
        vx = new double[count];
        vy = new double[count];
        sizes = new int[count];
        colors = new Color[count];

        for (int i = 0; i < count; i++) {
            px[i] = centerX;
            py[i] = centerY;
            double angle = rand.nextDouble() * Math.PI * 2;
            double speed = 1.0 + rand.nextDouble() * 4.0;
            vx[i] = Math.cos(angle) * speed;
            vy[i] = Math.sin(angle) * speed;
            sizes[i] = 2 + rand.nextInt(5);
            // Random warm color
            colors[i] = new Color(
                    200 + rand.nextInt(56), // R: 200-255
                    50 + rand.nextInt(150), // G: 50-199 (yellows, oranges, reds)
                    0,
                    200 + rand.nextInt(56) // A: 200-255
            );
        }
    }

    public void update() {
        if (finished)
            return;
        frame++;
        if (frame >= lifetime) {
            finished = true;
            return;
        }

        for (int i = 0; i < px.length; i++) {
            px[i] += vx[i];
            py[i] += vy[i];
            vx[i] *= 0.95; // friction
            vy[i] *= 0.95;
        }
    }

    public void draw(Graphics2D g) {
        if (finished)
            return;

        float alpha = 1.0f - (float) frame / lifetime;

        for (int i = 0; i < px.length; i++) {
            Color base = colors[i];
            int a = (int) (base.getAlpha() * alpha);
            if (a < 0)
                a = 0;
            g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), a));
            int s = (int) (sizes[i] * (1.0 - 0.5 * frame / lifetime));
            if (s < 1)
                s = 1;
            g.fillOval((int) px[i] - s / 2, (int) py[i] - s / 2, s, s);
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
