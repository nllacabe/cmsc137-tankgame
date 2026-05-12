package tank1990.core;

import java.awt.event.KeyEvent;
import java.awt.Color;

public interface Globals {

        int MAJOR_VERSION = 1;
        int MINOR_VERSION = 0;

        String CONFIGURATION_FILE = "config.ini";
        String DEFAULT_SAVE_LOCATION = "saves/";
        String GAME_TITLE = "TANK 1990 - NES";
        String GAME_RULE_TEXT = "TANK N";
        String COPYRIGHT_TEXT = "";

        Boolean SHOW_BOUNDING_BOX = false;

        int WINDOW_WIDTH = 1040;
        int WINDOW_HEIGHT = 780;

        int GAMEOVER_OVERLAY_DURATION = 3000;
        int POPUP_OVERLAY_DURATION_MS = 3000;

        int TILE_WIDTH = 16;
        int TILE_HEIGHT = 16;

        int TANK_WIDTH = 16;
        int TANK_HEIGHT = 16;

        int DEFAULT_SHOOT_PERIOD_MS = 500;

        int BULLET_WIDTH = 2;
        int BULLET_HEIGHT = 3;
        int BULLET_SPEED_PER_TICK = 3;
        int BLAST_WIDTH = 8;
        int BLAST_HEIGHT = 8;

        int COL_TILE_COUNT = 13;
        int ROW_TILE_COUNT = 13;
        int TILE_SUBDIVISION = 4;

        // Powerup constants (kept for compilation — powerup package still exists in
        // project)
        int POWERUP_WIDTH = 16;
        int POWERUP_HEIGHT = 16;
        int DEFAULT_POWERUP_LIFETIME_MS = 15000;
        int POWERUP_BLINK_INTERVAL_MS = 500;
        int SHOVEL_COOLDOWN_MS = 10000;
        int ANTI_SHOVEL_COOLDOWN_MS = 10000;
        int FROZEN_COOLDOWN_MS = 5000;
        int HELMET_COOLDOWN_MS = 10000;

        // Other enemy tank speed constants (kept for compilation — old tank files still
        // exist)
        int FAST_TANK_MOVEMENT_SPEED = 8;
        int FAST_TANK_MOVEMENT_MAX_SPEED = 8;
        int POWER_TANK_MOVEMENT_SPEED = 4;
        int POWER_TANK_MOVEMENT_MAX_SPEED = 4;
        int ARMOR_TANK_MOVEMENT_SPEED = 4;
        int ARMOR_TANK_MOVEMENT_MAX_SPEED = 4;
        int RED_TANK_BLINK_ANIMATION_PERIOD_MS = 500;

        // Powerup textures (kept for compilation)
        TextureFXStruct TEXTURE_POWERUP_GRENADE = new TextureFXStruct("textures/powerups/powerup-grenade.png", 0, 0, 0);
        TextureFXStruct TEXTURE_POWERUP_HELMET = new TextureFXStruct("textures/powerups/powerup-helmet.png", 0, 0, 0);
        TextureFXStruct TEXTURE_POWERUP_SHOVEL = new TextureFXStruct("textures/powerups/powerup-shovel.png", 0, 0, 0);
        TextureFXStruct TEXTURE_POWERUP_STAR = new TextureFXStruct("textures/powerups/powerup-star.png", 0, 0, 0);
        TextureFXStruct TEXTURE_POWERUP_TANK = new TextureFXStruct("textures/powerups/powerup-tank.png", 0, 0, 0);
        TextureFXStruct TEXTURE_POWERUP_TIMER = new TextureFXStruct("textures/powerups/powerup-timer.png", 0, 0, 0);
        TextureFXStruct TEXTURE_POWERUP_WEAPON = new TextureFXStruct("textures/powerups/powerup-weapon.png", 0, 0, 0);

        // Other tank textures (kept for compilation)
        TankTextureStruct TEXTURE_BASIC_TANK_RED_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-basic-red-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-basic-red-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-basic-red-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-basic-red-left.png", 0, 0, 0));
        TankTextureStruct TEXTURE_FAST_TANK_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-fast-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-fast-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-fast-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-fast-left.png", 0, 0, 0));
        TankTextureStruct TEXTURE_FAST_TANK_RED_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-fast-red-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-fast-red-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-fast-red-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-fast-red-left.png", 0, 0, 0));
        TankTextureStruct TEXTURE_POWER_TANK_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-power-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-power-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-power-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-power-left.png", 0, 0, 0));
        TankTextureStruct TEXTURE_POWER_TANK_RED_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-power-red-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-power-red-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-power-red-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-power-red-left.png", 0, 0, 0));
        TankTextureStruct TEXTURE_ARMOR_TANK_L4_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-armor-l4-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l4-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l4-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l4-left.png", 0, 0, 0));
        TankTextureStruct TEXTURE_ARMOR_TANK_L3_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-armor-l3-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l3-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l3-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l3-left.png", 0, 0, 0));
        TankTextureStruct TEXTURE_ARMOR_TANK_L2_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-armor-l2-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l2-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l2-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l2-left.png", 0, 0, 0));
        TankTextureStruct TEXTURE_ARMOR_TANK_L1_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-armor-l1-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l1-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l1-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-l1-left.png", 0, 0, 0));
        TankTextureStruct TEXTURE_ARMOR_TANK_RED_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-armor-red-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-red-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-red-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-armor-red-left.png", 0, 0, 0));

        String FONT_PRESS_START_2P = "fonts/PressStart2PRegular.ttf";

        // Player controls
        int KEY_PLAYER_1_MOVE_UP = KeyEvent.VK_UP;
        int KEY_PLAYER_1_MOVE_RIGHT = KeyEvent.VK_RIGHT;
        int KEY_PLAYER_1_MOVE_DOWN = KeyEvent.VK_DOWN;
        int KEY_PLAYER_1_MOVE_LEFT = KeyEvent.VK_LEFT;
        int KEY_PLAYER_1_MOVE_SHOOT = KeyEvent.VK_Z;
        int KEY_PLAYER_2_MOVE_UP = KeyEvent.VK_W;
        int KEY_PLAYER_2_MOVE_RIGHT = KeyEvent.VK_D;
        int KEY_PLAYER_2_MOVE_DOWN = KeyEvent.VK_S;
        int KEY_PLAYER_2_MOVE_LEFT = KeyEvent.VK_A;
        int KEY_PLAYER_2_MOVE_SHOOT = KeyEvent.VK_CONTROL;

        int INITAL_PLAYER_HEALTH = 3;

        // Local play spawn locations (bottom center — single player and local 2P)
        GridLocation INITIAL_PLAYER_1_LOC = new GridLocation(12, 4);
        Direction INITIAL_PLAYER_1_DIR = Direction.DIRECTION_UPWARDS;
        GridLocation INITIAL_PLAYER_2_LOC = new GridLocation(12, 8);
        Direction INITIAL_PLAYER_2_DIR = Direction.DIRECTION_UPWARDS;

        // Network play spawn locations (4 corners — master/slave mode)
        // P1 = top-left, P2 = top-right, P3 = bottom-left, P4 = bottom-right
        GridLocation NETWORK_PLAYER_1_LOC = new GridLocation(0, 0);
        Direction NETWORK_PLAYER_1_DIR = Direction.DIRECTION_DOWNWARDS;
        GridLocation NETWORK_PLAYER_2_LOC = new GridLocation(0, 12);
        Direction NETWORK_PLAYER_2_DIR = Direction.DIRECTION_DOWNWARDS;
        GridLocation NETWORK_PLAYER_3_LOC = new GridLocation(12, 0);
        Direction NETWORK_PLAYER_3_DIR = Direction.DIRECTION_UPWARDS;
        GridLocation NETWORK_PLAYER_4_LOC = new GridLocation(12, 12);
        Direction NETWORK_PLAYER_4_DIR = Direction.DIRECTION_UPWARDS;

        // Network config file location
        String NETWORK_CONFIG_FILE = "config.txt";

        int PLAYER_TANK_MOVEMENT_SPEED = 4;
        int PLAYER_TANK_MOVEMENT_MAX_SPEED = 4;

        // Enemy (BasicTank only)
        int BASIC_TANK_MOVEMENT_SPEED = 4;
        int BASIC_TANK_MOVEMENT_MAX_SPEED = 4;

        GridLocation ENEMY_SPAWN_LOCATION_1 = new GridLocation(0, 0);
        GridLocation ENEMY_SPAWN_LOCATION_2 = new GridLocation(0, 6);
        GridLocation ENEMY_SPAWN_LOCATION_3 = new GridLocation(0, 12);

        int ENEMY_TANK_SPAWN_DELAY_MS = 1000;

        Color COLOR_GRAY = new Color(114, 116, 114);
        Color COLOR_RED = new Color(177, 68, 31);
        Color COLOR_ORANGE = new Color(222, 156, 71);

        int GAME_TICK_MS = 12;

        int SPAWN_PROTECTION_COOLDOWN_MS = 1500;
        int SPAWN_PROTECTION_BLINK_PERIOD_MS = 100;

        // Tile sprites
        String TEXTURE_TILE_BRICKS_PATH = "textures/tiles/tile-bricks.png";

        SpriteAnimationStruct TEXTURE_TILE_BRICKS_SPRITE = new SpriteAnimationStruct("textures/tiles/tile-bricks.png",
                        1, 1, 1, 1, 0, 0);
        SpriteAnimationStruct TEXTURE_TILE_STEEL_SPRITE = new SpriteAnimationStruct("textures/tiles/tile-steel.png", 1,
                        1, 1, 1, 0, 0);
        SpriteAnimationStruct TEXTURE_TILE_TREES_SPRITE = new SpriteAnimationStruct("textures/tiles/tile-trees.png", 1,
                        1, 1, 1, 0, 0);
        SpriteAnimationStruct TEXTURE_TILE_SEA_SPRITE = new SpriteAnimationStruct("textures/tiles/tile-sea.png", 4, 60,
                        1, 4, 0, 0);
        SpriteAnimationStruct TEXTURE_TILE_ICE_SPRITE = new SpriteAnimationStruct("textures/tiles/tile-ice.png", 1, 1,
                        1, 1, 0, 0);
        SpriteAnimationStruct TEXTURE_TILE_EAGLE_SPRITE = new SpriteAnimationStruct("textures/tiles/tile-eagle.png", 1,
                        1, 1, 1, 0, 0);
        SpriteAnimationStruct TEXTURE_TILE_WITHDRAW_SPRITE = new SpriteAnimationStruct(
                        "textures/tiles/tile-withdraw.png", 1, 1, 1, 1, 0, 0);

        // Tank textures - player and basic enemy only
        String ICON_PLAYER1_TANK_PATH = "textures/tank/tank-player-1-right.png";
        String ICON_BASIC_TANK_PATH = "textures/tank/tank-basic-upwards.png";
        String ICON_FAST_TANK_PATH = "textures/tank/tank-fast-upwards.png";
        String ICON_POWER_TANK_PATH = "textures/tank/tank-power-upwards.png";
        String ICON_ARMOR_TANK_PATH = "textures/tank/tank-armor-l1-upwards.png";
        String ICON_ENEMY_TANK_PATH = "textures/miscs/enemy-icon.png";
        String ICON_PLAYER_LIFE_PATH = "textures/miscs/player-life.png";
        String ICON_STAGE_PATH = "textures/miscs/stage-icon.png";

        TankTextureStruct TEXTURE_PLAYER1_TANK_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-player-1-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-1-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-1-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-1-left.png", 0, 0, 0));

        TankTextureStruct TEXTURE_PLAYER2_TANK_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-player-2-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-2-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-2-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-2-left.png", 0, 0, 0));
        // Players 3 & 4 reuse player-2 textures as fallback; replace paths once art is
        // ready
        TankTextureStruct TEXTURE_PLAYER3_TANK_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-player-2-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-2-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-2-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-2-left.png", 0, 0, 0));
        TankTextureStruct TEXTURE_PLAYER4_TANK_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-player-2-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-2-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-2-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-player-2-left.png", 0, 0, 0));

        TankTextureStruct TEXTURE_BASIC_TANK_STRUCT = new TankTextureStruct(
                        new TextureFXStruct("textures/tank/tank-basic-upwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-basic-right.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-basic-downwards.png", 0, 0, 0),
                        new TextureFXStruct("textures/tank/tank-basic-left.png", 0, 0, 0));

        int FRAME_DELAY = 1;
        SpriteAnimationStruct BLAST_ANIMATION = new SpriteAnimationStruct("textures/miscs/blast.png", 5,
                        FRAME_DELAY * 2, 1, 5, 0, 0);

        String MAP_PATH = "maps/";
}