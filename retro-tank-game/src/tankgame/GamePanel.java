package tankgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Core game panel: owns the game loop, manages state, handles rendering and collisions.
// states: menu - playing
public class GamePanel extends JPanel implements ActionListener {

    // states
    private static final int STATE_MENU = 0;
    private static final int STATE_PLAYING = 1;

    private int gameState;

    // core objects
    private Timer timer;
    private InputHandler input;
    private TileMap map;
    private Tank player;
    private List<Bullet> bullets;
    private List<Explosion> explosions;
    private HUD hud;

    // menu animation
    private int menuBlink;

    private BufferedImage scanlineOverlay;

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setBackground(Constants.COLOR_BACKGROUND);
        setFocusable(true);
        setDoubleBuffered(true);

        input = new InputHandler();
        addKeyListener(input);

        bullets = new ArrayList<Bullet>();
        explosions = new ArrayList<Explosion>();
        hud = new HUD();

        buildScanlineOverlay();
        gameState = STATE_MENU;
        menuBlink = 0;

        timer = new Timer(Constants.FRAME_DELAY, this);
        timer.start();
    }

    // game loop

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    private void update() {
        menuBlink++;

        switch (gameState) {
            case STATE_MENU:
                if (input.enter()) {
                    startGame();
                }
                if (input.esc()) {
                    System.exit(0);
                }
                break;

            case STATE_PLAYING:
                updatePlaying();
                break;
        }
    }

    private void startGame() {
        map = new TileMap();
        player = new Tank(map.getPlayerSpawnX(), map.getPlayerSpawnY());
        bullets.clear();
        explosions.clear();
        hud = new HUD();

        gameState = STATE_PLAYING;
    }

    private void updatePlaying() {
        // player
        player.update(input, map);

        // shooting
        if (input.shoot()) {
            Bullet b = player.tryShoot();
            if (b != null)
                bullets.add(b);
        }

        // bullets
        Iterator<Bullet> bi = bullets.iterator();
        while (bi.hasNext()) {
            Bullet b = bi.next();
            b.update();

            if (!b.isAlive()) {
                bi.remove();
                continue;
            }

            // bullet vs tile
            if (map.bulletHitsTile(b.getX(), b.getY(), b.getWidth(), b.getHeight())) {
                b.destroy();
                bi.remove();
                continue;
            }
        }

        // explosions
        Iterator<Explosion> ei = explosions.iterator();
        while (ei.hasNext()) {
            Explosion ex = ei.next();
            ex.update();
            if (ex.isFinished())
                ei.remove();
        }

        // ESC returns to menu
        if (input.esc()) {
            gameState = STATE_MENU;
        }
    }

    // collision helper (baka makatulong sa multiplayer tank-vs-bullet collisions)

    boolean intersects(double ax, double ay, int aw, int ah,
            double bx, double by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    // rendering

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;

        // Antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (gameState) {
            case STATE_MENU:
                drawMenu(g);
                break;
            case STATE_PLAYING:
                drawGame(g);
                break;
        }

        if (scanlineOverlay != null) {
            g.drawImage(scanlineOverlay, 0, 0, null);
        }
    }

    private void drawMenu(Graphics2D g) {
        // bg
        g.setColor(Constants.COLOR_BACKGROUND);
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        // grid lines
        g.setColor(Constants.COLOR_GRID_LINE);
        for (int x = 0; x < Constants.WINDOW_WIDTH; x += Constants.TILE_SIZE) {
            g.drawLine(x, 0, x, Constants.WINDOW_HEIGHT);
        }
        for (int y = 0; y < Constants.WINDOW_HEIGHT; y += Constants.TILE_SIZE) {
            g.drawLine(0, y, Constants.WINDOW_WIDTH, y);
        }

        // title
        g.setFont(Constants.FONT_TITLE);
        FontMetrics fm = g.getFontMetrics();
        String title1 = "RETRO CLASSIC";
        String title2 = "TANK GAME";
        int t1w = fm.stringWidth(title1);
        int t2w = fm.stringWidth(title2);

        g.setColor(new Color(0, 255, 200, 40));
        g.drawString(title1, Constants.WINDOW_WIDTH / 2 - t1w / 2 + 2, 202);
        g.drawString(title2, Constants.WINDOW_WIDTH / 2 - t2w / 2 + 2, 252);

        g.setColor(Constants.COLOR_TITLE);
        g.drawString(title1, Constants.WINDOW_WIDTH / 2 - t1w / 2, 200);
        g.drawString(title2, Constants.WINDOW_WIDTH / 2 - t2w / 2, 250);

        // authors
        g.setFont(Constants.FONT_HUD);
        FontMetrics fmNames = g.getFontMetrics();
        String names = "Aquino | Lacabe";
        int nw = fmNames.stringWidth(names);
        g.setColor(Constants.COLOR_TEXT);
        g.drawString(names, Constants.WINDOW_WIDTH / 2 - nw / 2, 280);

        // controls info
        g.setFont(Constants.FONT_MENU);
        fm = g.getFontMetrics();
        g.setColor(Constants.COLOR_TEXT);
        String[] controls = {
                "CONTROLS:",
                "W A S D - Move Tank",
                "SPACE -  Shoot",
                "ESC (Menu) - Exit Game"
        };
        int cy = 345;
        for (String line : controls) {
            int lw = fm.stringWidth(line);
            g.drawString(line, Constants.WINDOW_WIDTH / 2 - lw / 2, cy);
            cy += 22;
        }

        // blink "Press ENTER to Start"
        if ((menuBlink / 30) % 2 == 0) {
            g.setFont(Constants.FONT_LARGE);
            fm = g.getFontMetrics();
            String prompt = "PRESS ENTER TO START";
            int pw = fm.stringWidth(prompt);
            g.setColor(Constants.COLOR_BULLET);
            g.drawString(prompt, Constants.WINDOW_WIDTH / 2 - pw / 2, 520);
        }
    }

    private void drawGame(Graphics2D g) {
        // bg
        g.setColor(Constants.COLOR_BACKGROUND);
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        // grid
        g.setColor(Constants.COLOR_GRID_LINE);
        for (int x = 0; x < Constants.WINDOW_WIDTH; x += Constants.TILE_SIZE) {
            g.drawLine(x, 0, x, Constants.WINDOW_HEIGHT);
        }
        for (int y = 0; y < Constants.WINDOW_HEIGHT; y += Constants.TILE_SIZE) {
            g.drawLine(0, y, Constants.WINDOW_WIDTH, y);
        }

        // tiles
        map.draw(g);

        // bullets
        for (Bullet b : bullets) {
            b.draw(g);
        }

        // player
        player.draw(g);

        // explosions (on top)
        for (Explosion ex : explosions) {
            ex.draw(g);
        }

        // heads up display
        hud.draw(g, player);
    }

    // overlay for end-game screens (baka makatulong sa multiplayer win/lose)
    void drawOverlay(Graphics2D g, String line1, String line2, Color color) {
        // dim bg
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        // main text
        g.setFont(Constants.FONT_TITLE);
        FontMetrics fm = g.getFontMetrics();
        int w1 = fm.stringWidth(line1);
        g.setColor(color);
        g.drawString(line1, Constants.WINDOW_WIDTH / 2 - w1 / 2, 260);

        // score
        g.setFont(Constants.FONT_LARGE);
        fm = g.getFontMetrics();
        int w2 = fm.stringWidth(line2);
        g.setColor(Constants.COLOR_BULLET);
        g.drawString(line2, Constants.WINDOW_WIDTH / 2 - w2 / 2, 310);

        // prompt
        if ((menuBlink / 30) % 2 == 0) {
            g.setFont(Constants.FONT_MENU);
            fm = g.getFontMetrics();
            String prompt = "Press ENTER for Menu";
            int pw = fm.stringWidth(prompt);
            g.setColor(Constants.COLOR_TEXT);
            g.drawString(prompt, Constants.WINDOW_WIDTH / 2 - pw / 2, 400);
        }
    }

    private void buildScanlineOverlay() {
        scanlineOverlay = new BufferedImage(
                Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scanlineOverlay.createGraphics();
        g.setColor(Constants.COLOR_SCANLINE);
        for (int y = 0; y < Constants.WINDOW_HEIGHT; y += 2) {
            g.drawLine(0, y, Constants.WINDOW_WIDTH, y);
        }
        g.dispose();
    }
}
