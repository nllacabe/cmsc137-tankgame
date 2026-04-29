package tankgame;

import java.awt.Color;
import java.awt.Font;

// constants for all parts ng game
public class Constants {

    // window size
    public static final String GAME_TITLE = "Retro Classic 90's Tank Game";
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 640;

    // tile grid
    public static final int TILE_SIZE = 32;
    public static final int MAP_COLS = 25; // 25 * 32 = 800
    public static final int MAP_ROWS = 20; // 20 * 32 = 640

    // timing
    public static final int TARGET_FPS = 60;
    public static final int FRAME_DELAY = 1000 / TARGET_FPS; // ~16 ms

    // player tank
    public static final int TANK_WIDTH = 48;
    public static final int TANK_HEIGHT = 48;
    public static final double TANK_SPEED = 3.0;
    public static final int TANK_MAX_HEALTH = 5;
    public static final long SHOOT_COOLDOWN = 300; // ms between shots

    // tank bullet
    public static final int BULLET_WIDTH = 10;
    public static final int BULLET_HEIGHT = 20;
    public static final double BULLET_SPEED = 6.0;

    // explosion
    public static final int EXPLOSION_PARTICLE_COUNT = 20;
    public static final int EXPLOSION_LIFETIME = 30; // frames

    // colors from retro game color palette
    public static final Color COLOR_BACKGROUND = new Color(10, 10, 18);
    public static final Color COLOR_GRID_LINE = new Color(25, 25, 45);
    public static final Color COLOR_PLAYER_TANK = new Color(0, 230, 118); // neon green
    public static final Color COLOR_PLAYER_BARREL = new Color(0, 200, 100);
    public static final Color COLOR_BULLET = new Color(255, 235, 59); // bright yellow
    public static final Color COLOR_BULLET_GLOW = new Color(255, 235, 59, 80);
    public static final Color COLOR_BRICK = new Color(160, 82, 45);
    public static final Color COLOR_BRICK_DARK = new Color(120, 60, 30);
    public static final Color COLOR_STEEL = new Color(160, 170, 180);
    public static final Color COLOR_STEEL_DARK = new Color(120, 130, 140);
    public static final Color COLOR_BUSH = new Color(34, 120, 50, 150);
    public static final Color COLOR_HUD_BG = new Color(0, 0, 0, 160);
    public static final Color COLOR_HEALTH_BAR = new Color(0, 230, 118);
    public static final Color COLOR_HEALTH_LOST = new Color(60, 60, 60);
    public static final Color COLOR_TEXT = new Color(220, 220, 220);
    public static final Color COLOR_TITLE = new Color(0, 255, 200);
    public static final Color COLOR_SCANLINE = new Color(0, 0, 0, 30);

    // fonts
    public static final Font FONT_TITLE = new Font("Monospaced", Font.BOLD, 36);
    public static final Font FONT_MENU = new Font("Monospaced", Font.PLAIN, 18);
    public static final Font FONT_HUD = new Font("Monospaced", Font.BOLD, 14);
    public static final Font FONT_LARGE = new Font("Monospaced", Font.BOLD, 28);

    // brick wall
    public static final int BRICK_MAX_HEALTH = 2;

    private Constants() {
    } // prevent instantiation
}
