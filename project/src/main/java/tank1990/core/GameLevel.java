/*
 * Copyright (c) 2025.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tank1990.core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.*;

import tank1990.powerup.AbstractPowerup;
import tank1990.powerup.PowerupType;
import tank1990.powerup.PowerupFactory;
import tank1990.tank.AbstractTank;
import tank1990.tank.TankFactory;
import tank1990.tank.TankType;
import tank1990.tile.*;

/**
 * @class GameLevel
 * @brief Represents a game level in the Tank 1990 game.
 * @details This class manages the state of the game level, including enemy
 *          tanks, eagle location,
 *          player location, and various game mechanics such as spawning tanks
 *          and power-ups.
 *          GameLevels are handled by the GameLevelManager.
 */
public class GameLevel implements Serializable {
    private static final long serialVersionUID = 1L;
    // Create a min-heap for spawn location which spawn location and timestamp of
    // last enemy tank created.
    private static final PriorityQueue<Map.Entry<GridLocation, Long>> SPAWN_LOCATIONS = new PriorityQueue<>(
            Comparator.comparingLong(Map.Entry::getValue));

    private HashMap<TankType, Integer> enemyTankCounts; // Map of tank types to their counts in the level

    private LevelState currentState;

    private LevelInfo levelInfo;

    private Dimension gameAreaSize;

    private int activeEnemyTankCount;

    private GridLocation eagleLocation = null;
    private GridLocation playerLocation = null;

    private int totalEnemyTankCount; // Total number of enemy tanks in the level

    private static final List<Integer> MAGIC_NUMBERS = List.of(4, 11, 18); // Magic numbers used to determine which
                                                                           // enemy tanks are spawned as red tanks in
                                                                           // the level

    private HashMap<GridLocation, BlockConfiguration> originalTilesAroundEagle;
    private HashMap<GridLocation, BlockConfiguration> currentTilesAroundEagle;

    private boolean isShovelActive = false;
    private boolean isAntiShovelActive = false;
    private boolean shovelActivationTriggered = false;
    private boolean antiShovelActivationTriggered = false;

    /** When true, eagle and enemies are removed (network 4-player mode). */
    private boolean isNetworkMode = false;

    private TimeTick shovelTick;
    private TimeTick antiShovelTick;

    public GameLevel(String levelPath) {
        this.currentState = LevelState.NOT_LOADED;
        this.levelInfo = MapGenerator.readLevelInfo(levelPath);
        this.enemyTankCounts = new HashMap<TankType, Integer>();
        this.activeEnemyTankCount = 0;

        // Set default size values for now, it will be updated on draw method
        this.gameAreaSize = new Dimension(Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);

        HashMap<TankType, Integer> enemyTankCount = this.levelInfo.enemyTankCount;
        this.setEnemyTankCount(enemyTankCount);
        this.totalEnemyTankCount = enemyTankCount.values().stream().mapToInt(Integer::intValue).sum();

        // Default timestamp for spawn locations is -1 which means invalid
        SPAWN_LOCATIONS.clear();
        SPAWN_LOCATIONS.add(new AbstractMap.SimpleEntry<>(Globals.ENEMY_SPAWN_LOCATION_1, -1L));
        SPAWN_LOCATIONS.add(new AbstractMap.SimpleEntry<>(Globals.ENEMY_SPAWN_LOCATION_2, -1L));
        SPAWN_LOCATIONS.add(new AbstractMap.SimpleEntry<>(Globals.ENEMY_SPAWN_LOCATION_3, -1L));

        this.eagleLocation = findEagleLocation();

        this.originalTilesAroundEagle = new HashMap<>();
        this.currentTilesAroundEagle = new HashMap<>();

        // Store the original state of tiles around the eagle for later use
        setSurroundingEagleTiles(this.originalTilesAroundEagle);

        // Initialize the shovel and anti-shovel ticks
        this.shovelTick = new TimeTick(Utils.Time2GameTick(Globals.SHOVEL_COOLDOWN_MS));
        this.shovelTick.setRepeats(-1); // Repeat indefinitely
        this.antiShovelTick = new TimeTick(Utils.Time2GameTick(Globals.ANTI_SHOVEL_COOLDOWN_MS));
        this.antiShovelTick.setRepeats(-1); // Repeat indefinitely
    }

    public GameLevel() {
        this.currentState = LevelState.NOT_LOADED;
        this.levelInfo = MapGenerator.generateRandomLevelInfo();
        this.enemyTankCounts = new HashMap<TankType, Integer>();
        this.activeEnemyTankCount = 0;

        // Set default size values for now, it will be updated on draw method
        this.gameAreaSize = new Dimension(Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);

        HashMap<TankType, Integer> enemyTankCount = this.levelInfo.enemyTankCount;
        this.setEnemyTankCount(enemyTankCount);
        this.totalEnemyTankCount = enemyTankCount.values().stream().mapToInt(Integer::intValue).sum();

        // Default timestamp for spawn locations is -1 which means invalid
        SPAWN_LOCATIONS.clear();
        SPAWN_LOCATIONS.add(new AbstractMap.SimpleEntry<>(Globals.ENEMY_SPAWN_LOCATION_1, -1L));
        SPAWN_LOCATIONS.add(new AbstractMap.SimpleEntry<>(Globals.ENEMY_SPAWN_LOCATION_2, -1L));
        SPAWN_LOCATIONS.add(new AbstractMap.SimpleEntry<>(Globals.ENEMY_SPAWN_LOCATION_3, -1L));

        this.eagleLocation = findEagleLocation();

        this.originalTilesAroundEagle = new HashMap<>();
        this.currentTilesAroundEagle = new HashMap<>();

        // Store the original state of tiles around the eagle for later use
        setSurroundingEagleTiles(this.originalTilesAroundEagle);

        // Initialize the shovel and anti-shovel ticks
        this.shovelTick = new TimeTick(Utils.Time2GameTick(Globals.SHOVEL_COOLDOWN_MS));
        this.shovelTick.setRepeats(-1); // Repeat indefinitely
        this.antiShovelTick = new TimeTick(Utils.Time2GameTick(Globals.ANTI_SHOVEL_COOLDOWN_MS));
        this.antiShovelTick.setRepeats(-1); // Repeat indefinitely
    }

    /**
     * Enables network mode — strips the eagle tile, zeros enemy counts,
     * and prevents any enemy spawning. Call this before startGameLevel().
     */
    public void setNetworkMode(boolean networkMode) {
        this.isNetworkMode = networkMode;
        if (networkMode && this.levelInfo != null) {
            MapGenerator.stripEagleAndEnemies(this.levelInfo);
            this.eagleLocation = null; // No eagle in network mode
            this.enemyTankCounts.replaceAll((type, count) -> 0);
            this.totalEnemyTankCount = 0;
            System.out.println("[GameLevel] Network mode — eagle and enemies removed.");
        }
    }

    public boolean isNetworkMode() {
        return this.isNetworkMode;
    }

    /**
     * Gets the current state of the game level.
     * This method returns the current state of the level, which can be used
     * to determine if the level is loaded, in progress, or completed.
     *
     * @return The current LevelState of the game level.
     */
    public LevelState getCurrentState() {
        return this.currentState;
    }

    /**
     * Gets the size of the game area.
     * This method returns the current size of the game area, which is typically
     * updated during the drawing process.
     *
     * @return A Dimension object representing the width and height of the game
     *         area.
     */
    public Dimension getGameAreaSize() {
        return this.gameAreaSize;
    }

    /**
     * Gets the count of active enemy tanks in the game level.
     * This method returns the number of enemy tanks that are currently active.
     *
     * @return The count of active enemy tanks.
     */
    public int getActiveEnemyTankCount() {
        return this.activeEnemyTankCount;
    }

    /**
     * Decreases the count of active enemy tanks.
     */
    public void decreaseActiveEnemyTank() {
        this.activeEnemyTankCount = this.activeEnemyTankCount > 0 ? this.activeEnemyTankCount - 1 : 0;
    }

    /**
     * Sets the current state of the game level.
     * This method allows the game level to change its state, which can be used
     * to manage transitions between different phases of the game (e.g., loading,
     * playing, completed).
     *
     * @param state The new LevelState to set for the game level.
     */
    public void setCurrentState(LevelState state) {
        this.currentState = state;
    }

    public void update() {
        // Check timestamp of spawn time. Set the timestamp to invalid if it is too old
        Map.Entry<GridLocation, Long> oldestSpawnLocationEntry = SPAWN_LOCATIONS.poll();

        if (oldestSpawnLocationEntry != null) {
            if (System.currentTimeMillis() - oldestSpawnLocationEntry.getValue() > Globals.ENEMY_TANK_SPAWN_DELAY_MS) {
                // Set the timestamp to invalid
                oldestSpawnLocationEntry.setValue(-1L);
            }
            // Update the list
            SPAWN_LOCATIONS.add(oldestSpawnLocationEntry);
        }

        for (int row = 0; row < this.levelInfo.levelGrid.length; row++) {
            for (int col = 0; col < this.levelInfo.levelGrid[row].length; col++) {
                Tile tile = this.levelInfo.levelGrid[row][col];
                if (tile == null)
                    continue;

                if (tile.isDestroyed()) {
                    this.levelInfo.levelGrid[row][col] = null; // Set the array element to null
                } else {
                    tile.update();
                }
            }
        }

        if (this.isShovelActive) {
            if (this.shovelActivationTriggered) {
                this.shovelTick.reset();
                this.shovelActivationTriggered = false;
            }

            this.shovelTick.updateTick();

            if (this.shovelTick.isTimeOut()) {
                deactivateShovelPowerup();
                this.shovelTick.reset();
            }
        }

        if (this.isAntiShovelActive) {
            if (this.antiShovelActivationTriggered) {
                this.antiShovelTick.reset();
                this.antiShovelActivationTriggered = false;
            }

            this.antiShovelTick.updateTick();

            if (this.antiShovelTick.isTimeOut()) {
                deactivateAntiShovelPowerup();
                this.antiShovelTick.reset();
            }
        }
    }

    /**
     * Draws the game level on the provided graphics context.
     * This method iterates through all tiles in the map and draws them
     * if their depth is greater than or equal to the specified minimum depth.
     *
     * @param g        The graphics context to draw on.
     * @param minDepth The minimum depth of tiles to draw.
     */
    public void draw(Graphics g, int minDepth) {
        for (Tile[] tileRows : this.levelInfo.levelGrid) {
            for (Tile tile : tileRows) {
                if (tile != null && tile.depth >= minDepth)
                    tile.draw(g);
            }
        }

        this.gameAreaSize.width = g.getClipBounds().width;
        this.gameAreaSize.height = g.getClipBounds().height;
    }

    /**
     * Gets the map of tiles for the game level.
     * This method returns a 2D array representing the tiles in the game level.
     *
     * @return A 2D array of Tile objects representing the game level map.
     */
    public Tile[][] getMap() {
        return this.levelInfo.levelGrid;
    }

    /*
     * Gets the neighbors of a tile at the specified grid location.
     * 
     * @param gloc The grid location of the tile.
     * 
     * @return An array of Tile objects representing the neighbors of the tile,
     */
    public Tile[] getNeighbors(GridLocation gloc) {
        if (gloc == null)
            return null;

        Tile[] neighbors = new Tile[9];
        int row = gloc.rowIndex();
        int col = gloc.colIndex();

        // Add the center tile
        neighbors[0] = this.levelInfo.levelGrid[row][col];

        // Check bounds and get all 8 neighbors (including corners)
        if (row > 0)
            neighbors[1] = this.levelInfo.levelGrid[row - 1][col]; // Up
        if (col < Globals.COL_TILE_COUNT - 1)
            neighbors[2] = this.levelInfo.levelGrid[row][col + 1]; // Right
        if (row < Globals.ROW_TILE_COUNT - 1)
            neighbors[3] = this.levelInfo.levelGrid[row + 1][col]; // Down
        if (col > 0)
            neighbors[4] = this.levelInfo.levelGrid[row][col - 1]; // Left
        if (row > 0 && col > 0)
            neighbors[5] = this.levelInfo.levelGrid[row - 1][col - 1]; // Top-Left
        if (row > 0 && col < Globals.COL_TILE_COUNT - 1)
            neighbors[6] = this.levelInfo.levelGrid[row - 1][col + 1]; // Top-Right
        if (row < Globals.ROW_TILE_COUNT - 1 && col < Globals.COL_TILE_COUNT - 1)
            neighbors[7] = this.levelInfo.levelGrid[row + 1][col + 1]; // Bottom-Right
        if (row < Globals.ROW_TILE_COUNT - 1 && col > 0)
            neighbors[8] = this.levelInfo.levelGrid[row + 1][col - 1]; // Bottom-Left

        return neighbors;
    }

    /**
     * Gets the count of enemy tanks remaining in the game level.
     * This method sums up the counts of all enemy tank types to determine
     * how many enemy tanks are still available to spawn.
     *
     * @return The total number of remaining enemy tanks across all types.
     */
    public int getEnemyTankCounts() {
        int remainingTanks = 0;
        for (Integer count : enemyTankCounts.values()) {
            remainingTanks += count;
        }
        return remainingTanks;
    }

    /**
     * Sets the counts of enemy tanks for the game level.
     * This method allows the game level to define how many of each type of enemy
     * tank
     * should be available for spawning.
     *
     * @param enemyTankCounts A map where keys are TankType and values are the
     *                        counts of each type.
     */
    public void setEnemyTankCount(HashMap<TankType, Integer> enemyTankCounts) {
        this.enemyTankCounts = enemyTankCounts;
    }

    /**
     * Spawns a powerup in the game level.
     * This method randomly selects a powerup type and spawns it at a random
     * location
     * within the game area.
     *
     * @return An instance of AbstractPowerup representing the spawned powerup, or
     *         null if no powerup is created.
     */
    public AbstractPowerup spawnPowerup() {
        Random random = new Random();
        PowerupType powerupType = PowerupType.valueOf(random.nextInt(PowerupType.values().length));

        Dimension gameArea = getGameAreaSize();

        // Ensure the powerup spawns within the game area, leaving space for its
        // dimensions
        int x = random.nextInt(gameArea.width - Globals.POWERUP_WIDTH);
        int y = random.nextInt(gameArea.height - Globals.POWERUP_HEIGHT);

        AbstractPowerup powerup = PowerupFactory.createPowerup(powerupType, x, y);
        if (powerup != null) {
            System.out.println("Spawning powerup: " + powerupType + " at (" + x + ", " + y + ")");
        }
        return powerup;
    }

    /**
     * Spawns an enemy tank in the game level.
     * This method randomly selects a tank type from the available types
     * and spawns it at a random spawn location with a random direction.
     * It also updates the count of active enemy tanks and the remaining counts of
     * each tank type.
     *
     * @return An instance of AbstractTank representing the spawned enemy tank, or
     *         null if no tanks are available to spawn.
     */
    public AbstractTank spawnEnemyTank() {
        if (this.isNetworkMode)
            return null;
        if (getRemainingEnemyTanks() <= 0) {
            return null;
        }

        Random random = new Random();
        TankType[] tankTypes = new TankType[] { TankType.BASIC_TANK, TankType.FAST_TANK, TankType.POWER_TANK,
                TankType.ARMOR_TANK };

        TankType currentTankType = null;
        for (int i = 0; i < tankTypes.length; i++) {
            TankType candidate = tankTypes[random.nextInt(tankTypes.length)];
            if (enemyTankCounts.getOrDefault(candidate, 0) > 0) {
                currentTankType = candidate;
                break;
            }
        }
        if (currentTankType == null) {
            return null;
        }

        Map.Entry<GridLocation, Long> spawnLocationEntry = SPAWN_LOCATIONS.poll();

        if (spawnLocationEntry == null)
            return null;

        // If spawn time is not old enough, do not spawn any tank
        if (spawnLocationEntry.getValue() > 0) {
            // Update the list
            SPAWN_LOCATIONS.add(spawnLocationEntry);
            return null;
        }

        // If tile is occupied, do not spawn any tank
        if (isTileOccupied(spawnLocationEntry.getKey(), null)) {
            // Update the list
            SPAWN_LOCATIONS.add(spawnLocationEntry);
            return null;
        }

        // Set timestamp of spawn time
        spawnLocationEntry.setValue(System.currentTimeMillis());

        // Update the list
        SPAWN_LOCATIONS.add(spawnLocationEntry);

        Direction[] directions = Direction.values();
        Direction spawnDir = directions[random.nextInt(1, directions.length)];

        this.activeEnemyTankCount++;
        enemyTankCounts.put(currentTankType, enemyTankCounts.get(currentTankType) - 1);
        System.out.println(
                "Spawning enemy tank: " + currentTankType + " at " + spawnLocationEntry.getKey() + " facing " + spawnDir
                        + ". Remaining: " + enemyTankCounts.get(currentTankType) + " Active: " + activeEnemyTankCount);
        Location loc = Utils.gridLoc2Loc(spawnLocationEntry.getKey());

        AbstractTank enemyTank = TankFactory.createTank(currentTankType, loc.x(), loc.y());

        int nThTank = this.totalEnemyTankCount - getRemainingEnemyTanks() + 1;
        assert enemyTank != null;
        if (MAGIC_NUMBERS.contains(nThTank)) {
            enemyTank.setAsRed();
        } else {
            // There is a 30% chance to spawn a red tank
            if (Utils.getRandomProbability(30))
                enemyTank.setAsRed();
        }

        return enemyTank;
    }

    /**
     * Gets the count of enemy tanks remaining in the game level.
     * This method sums up the counts of all enemy tank types.
     *
     * @return The total number of remaining enemy tanks.
     */
    public int getRemainingEnemyTanks() {
        return enemyTankCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Finds the eagle location in the game level.
     * This method searches through the map for a tile of type TILE_EAGLE
     * and sets the eagleLocation field accordingly.
     *
     * @return The location of the eagle in grid coordinates, or null if not found.
     */
    public GridLocation findEagleLocation() {
        if (this.eagleLocation != null) {
            return this.eagleLocation;
        }

        for (int i = 0; i < Globals.ROW_TILE_COUNT; i++) {
            for (int j = 0; j < Globals.COL_TILE_COUNT; j++) {
                Tile tile = this.levelInfo.levelGrid[i][j];
                if (tile == null)
                    continue;

                if (tile.getType() == TileType.TILE_EAGLE) {
                    this.eagleLocation = new GridLocation(i, j);
                    return this.eagleLocation;
                }
            }
        }
        return null; // No eagle found
    }

    /**
     * Gets the eagle location in the game level.
     * This is typically used to determine where the eagle is positioned for
     * gameplay purposes.
     *
     * @return The current location of the eagle in grid coordinates, or null if not
     *         set.
     */
    public GridLocation getEagleLocation() {
        return this.eagleLocation;
    }

    /**
     * Gets the player location in the game level.
     * This is typically used to determine where the player is currently positioned.
     *
     * @return The current location of the player in grid coordinates.
     */
    public GridLocation getPlayerLocation() {
        return this.playerLocation;
    }

    /**
     * Sets the player location in the game level.
     * This is typically used to update the player's position after movement.
     *
     * @param playerLocation The new location of the player in grid coordinates.
     */
    public void setPlayerLocation(GridLocation playerLocation) {
        this.playerLocation = playerLocation;
    }

    /**
     * Calculates the distance from the origin to the eagle location.
     * The distance is defined as the sum of the differences in row and column
     * indices.
     * If the eagle location is not set, returns -1.
     *
     * @param origin The origin location from which to calculate the distance.
     * @return The distance to the eagle location, or -1 if the eagle location is
     *         not set.
     */
    public int getEagleDistance(GridLocation origin) {
        if (this.eagleLocation == null) {
            return -1;
        }

        int dx = Math.abs(this.eagleLocation.colIndex() - origin.colIndex());
        int dy = Math.abs(this.eagleLocation.rowIndex() - origin.rowIndex());

        return dx + dy;
    }

    /**
     * Calculates the distance from the origin to the player location.
     * The distance is defined as the sum of the differences in row and column
     * indices.
     * If the player location is not set, returns -1.
     *
     * @param origin The origin location from which to calculate the distance.
     * @return The distance to the player location, or -1 if the player location is
     *         not set.
     */
    public int getPlayerDistance(GridLocation origin) {
        if (this.playerLocation == null) {
            return -1;
        }

        int dx = Math.abs(this.playerLocation.colIndex() - origin.colIndex());
        int dy = Math.abs(this.playerLocation.rowIndex() - origin.rowIndex());

        return dx + dy;
    }

    /**
     * Checks if the eagle is alive in the game level.
     * The eagle is considered alive if its location is set and the corresponding
     * tile in the map is not null.
     *
     * @return true if the eagle is alive, false otherwise.
     */
    public boolean isEagleAlive() {
        return this.eagleLocation != null
                && this.levelInfo.levelGrid[this.eagleLocation.rowIndex()][this.eagleLocation.colIndex()] != null;
    }

    /**
     * Checks if a tank can see the eagle with a specified bounding rectangle and
     * direction.
     * A tank can see the eagle if it is alive and the tank is in the same row or
     * column as the eagle,
     * and the direction of the tank matches the direction towards the eagle.
     *
     * @param tankBound The bounding rectangle of the tank.
     * @param tankDir   The direction of the tank.
     * @return true if the tank can see the eagle, false otherwise.
     */
    public boolean seeTarget(RectangleBound tankBound, Direction tankDir) {
        // Check if the eagle is alive
        if (!isEagleAlive())
            return false;

        // Calculate the bound that sees directly towards its direction
        GridLocation tankGloc = Utils.loc2GridLoc(new Location(tankBound.getOriginX(), tankBound.getOriginY()));

        int rowDiff = this.eagleLocation.rowIndex() - tankGloc.rowIndex();
        int colDiff = this.eagleLocation.colIndex() - tankGloc.colIndex();

        if (rowDiff == 0 || colDiff == 0) {
            if (rowDiff > 0 && tankDir == Direction.DIRECTION_DOWNWARDS)
                return true;
            if (rowDiff < 0 && tankDir == Direction.DIRECTION_UPWARDS)
                return true;
            if (colDiff > 0 && tankDir == Direction.DIRECTION_RIGHT)
                return true;
            if (colDiff < 0 && tankDir == Direction.DIRECTION_LEFT)
                return true;
        }

        return rowDiff == 0 && colDiff == 0;
    }

    /**
     * Checks if a tank with a specified bounding rectangle is aligned with the
     * eagle.
     * A tank is considered aligned with the eagle if it is in the same row or
     * column as the eagle.
     *
     * @param tankBound The bounding rectangle of the tank.
     * @return true if the tank is aligned with the eagle, false otherwise.
     */
    public boolean isTargetAligned(RectangleBound tankBound) {
        // Check if the eagle is alive
        if (!isEagleAlive())
            return false;

        // Calculate the bound that aligns with the eagle
        GridLocation tankGloc = Utils.loc2GridLoc(new Location(tankBound.getOriginX(), tankBound.getOriginY()));

        int rowDiff = this.eagleLocation.rowIndex() - tankGloc.rowIndex();
        int colDiff = this.eagleLocation.colIndex() - tankGloc.colIndex();

        return rowDiff == 0 || colDiff == 0;
    }

    /**
     * Gets the target direction towards the eagle based on the tank's bounding
     * rectangle.
     * This method calculates the direction from the tank's position to the eagle's
     * position
     * and returns the corresponding Direction enum value.
     *
     * @param tankBound The bounding rectangle of the tank.
     * @return The Direction towards the eagle, or DIRECTION_INVALID if the eagle is
     *         not alive.
     */
    public Direction getTargetDirection(RectangleBound tankBound) {
        // Check if the eagle is alive
        if (!isEagleAlive())
            return Direction.DIRECTION_INVALID;

        // Calculate the bound that sees directly towards its direction
        GridLocation tankGloc = Utils.loc2GridLoc(new Location(tankBound.getOriginX(), tankBound.getOriginY()));

        int rowDiff = this.eagleLocation.rowIndex() - tankGloc.rowIndex();
        int colDiff = this.eagleLocation.colIndex() - tankGloc.colIndex();

        if (rowDiff == 0 || colDiff == 0) {
            if (rowDiff > 0)
                return Direction.DIRECTION_DOWNWARDS;
            if (rowDiff < 0)
                return Direction.DIRECTION_UPWARDS;
            if (colDiff > 0)
                return Direction.DIRECTION_RIGHT;
            if (colDiff < 0)
                return Direction.DIRECTION_LEFT;
        }

        return Direction.DIRECTION_INVALID;
    }

    /**
     * Checks if a tank with a specified bounding rectangle can eligible to move.
     * This method checks the neighboring tiles of the tank's current location
     * to determine if the tank can move without colliding with other tanks or
     * obstacles.
     *
     * @param tankBound The bounding rectangle of the tank.
     * @return true if the tank can move, false otherwise.
     */
    public boolean checkMovable(AbstractTank tank, RectangleBound tankBound) {
        // 1 — Check tank-bound is within game area
        Dimension gameArea = GameLevelManager.getInstance().getCurrentLevel().getGameAreaSize();
        if (tankBound.getMaxX() > gameArea.width ||
                tankBound.getMaxY() > gameArea.height ||
                tankBound.getMinX() < 0 ||
                tankBound.getMinY() < 0)
            return false;

        // 2 — Compute cell pixel size directly from gameAreaSize (same value used by
        // Tile.draw), so tile collision bounds always match what is rendered.
        int cellW = gameArea.width / Globals.COL_TILE_COUNT;
        int cellH = gameArea.height / Globals.ROW_TILE_COUNT;

        // 3 — Determine which grid cells the tentative tank bound overlaps.
        // Add one cell of margin on each side to catch edge cases.
        int minCol = Math.max(0, (int) (tankBound.getMinX() / cellW) - 1);
        int maxCol = Math.min(Globals.COL_TILE_COUNT - 1, (int) (tankBound.getMaxX() / cellW) + 1);
        int minRow = Math.max(0, (int) (tankBound.getMinY() / cellH) - 1);
        int maxRow = Math.min(Globals.ROW_TILE_COUNT - 1, (int) (tankBound.getMaxY() / cellH) + 1);

        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                Tile tile = this.levelInfo.levelGrid[row][col];
                if (tile == null)
                    continue;

                // Passable tile types — tank is allowed to overlap
                TileType ttype = tile.getType();
                if (ttype == TileType.TILE_ICE)
                    continue;
                if (ttype == TileType.TILE_TREES)
                    continue;
                if (ttype == TileType.TILE_NONE)
                    continue;

                // Build the tile's pixel bounding box from grid indices and gameAreaSize,
                // matching exactly the pixel positions used in Tile.draw().
                int tileCenterX = col * cellW + cellW / 2;
                int tileCenterY = row * cellH + cellH / 2;
                RectangleBound tileBound = new RectangleBound(
                        tileCenterX - cellW / 2,
                        tileCenterY - cellH / 2,
                        cellW, cellH);

                if (RectangleBound.isCollided(tileBound, tankBound))
                    return false;
            }
        }

        // 4 — Check tank-vs-tank collisions
        return !(GameLevelManager.getInstance().getGameEngine().checkTankCollisions(tank, tankBound));
    }

    /**
     * Checks if a tile at the specified grid location is occupied by a tank.
     *
     * @param gloc         The grid location to check for occupancy.
     * @param exceptionLoc An optional grid location to exclude from the check.
     * @return true if the tile is occupied by a tank, false otherwise.
     */
    public boolean isTileOccupied(GridLocation gloc, GridLocation exceptionLoc) {
        if (gloc == null)
            return false;

        // Check if the tile is within bounds
        if (gloc.rowIndex() < 0 || gloc.rowIndex() >= Globals.ROW_TILE_COUNT ||
                gloc.colIndex() < 0 || gloc.colIndex() >= Globals.COL_TILE_COUNT) {
            return false;
        }

        ArrayList<GridLocation> tankLocations = GameLevelManager.getInstance().getGameEngine().getTankLocations();
        if (exceptionLoc != null)
            tankLocations.remove(exceptionLoc); // Remove the exception location if it exists

        return tankLocations.contains(gloc);
    }

    /**
     * Sets the map with the tiles surrounding the eagle location.
     * excluding the eagle tile itself. It checks bounds to ensure it does not go
     * out of the grid.
     *
     * @param surroundingTiles The map to store the surrounding tiles.
     */
    private void setSurroundingEagleTiles(HashMap<GridLocation, BlockConfiguration> surroundingTiles) {
        if (this.eagleLocation == null)
            return;

        int row = this.eagleLocation.rowIndex();
        int col = this.eagleLocation.colIndex();

        // Clear previous tiles
        surroundingTiles.clear();

        // Check bounds and add tiles around the eagle to the list
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;

                // Do not include the eagle tile itself
                if (newRow == row && newCol == col)
                    continue;

                if (newRow >= 0 && newRow < Globals.ROW_TILE_COUNT && newCol >= 0 && newCol < Globals.COL_TILE_COUNT) {
                    Tile tile = this.levelInfo.levelGrid[newRow][newCol];
                    if (tile != null) {
                        surroundingTiles.put(tile.getGridLocation(), tile.getBlockConf());
                    }
                }
            }
        }
    }

    /**
     * Activates the shovel powerup, which converts surrounding tiles around the
     * eagle to steel.
     * It stores the original state of the tiles before conversion for later
     * restoration.
     */
    public void activateShovelPowerup() {
        if (this.originalTilesAroundEagle.isEmpty())
            return;

        if (isAntiShovelActive) {
            deactivateAntiShovelPowerup();
        }

        setSurroundingEagleTiles(this.currentTilesAroundEagle); // Store the current state which will be needed to
                                                                // restore later

        for (Map.Entry<GridLocation, BlockConfiguration> entry : this.originalTilesAroundEagle.entrySet()) {
            GridLocation gloc = entry.getKey();
            BlockConfiguration blockConf = entry.getValue();

            // Convert the tile to steel
            this.levelInfo.levelGrid[gloc.rowIndex()][gloc.colIndex()] = null;
            this.levelInfo.levelGrid[gloc.rowIndex()][gloc.colIndex()] = TileFactory.createTile(TileType.TILE_STEEL,
                    gloc.colIndex(), gloc.rowIndex(), blockConf);
        }

        isShovelActive = true;
        isAntiShovelActive = false; // Activating shovel powerup deactivates the anti-shovel powerup
        shovelActivationTriggered = true; // Set the flag to indicate that the shovel powerup was activated. It is
                                          // useful in successive activations.
    }

    /**
     * Deactivates the shovel powerup, restoring the original tiles around the
     * eagle.
     * It uses the stored state from when the powerup was activated to restore the
     * tiles.
     */
    public void deactivateShovelPowerup() {
        if (this.currentTilesAroundEagle.isEmpty())
            return;

        for (Map.Entry<GridLocation, BlockConfiguration> entry : this.currentTilesAroundEagle.entrySet()) {
            GridLocation gloc = entry.getKey();
            BlockConfiguration blockConf = entry.getValue();

            // Restore according to last tile configuration
            this.levelInfo.levelGrid[gloc.rowIndex()][gloc.colIndex()] = TileFactory.createTile(TileType.TILE_BRICKS,
                    gloc.colIndex(), gloc.rowIndex(), blockConf);
        }

        isShovelActive = false;
    }

    /**
     * Activates the anti-shovel powerup, which removes surrounding tiles around the
     * eagle.
     * Normally there is no such a powerup type in the game, but it is the effect
     * when enemy tanks use the shovel powerup.
     * It stores the original state of the tiles before removal for later
     * restoration.
     */
    public void activateAntiShovelPowerup() {
        if (this.originalTilesAroundEagle.isEmpty())
            return;

        if (isShovelActive) {
            deactivateShovelPowerup();
        }

        setSurroundingEagleTiles(this.currentTilesAroundEagle); // Store the current state which will be needed to
                                                                // restore later

        for (Map.Entry<GridLocation, BlockConfiguration> entry : this.originalTilesAroundEagle.entrySet()) {
            GridLocation gloc = entry.getKey();
            BlockConfiguration blockConf = entry.getValue();

            this.levelInfo.levelGrid[gloc.rowIndex()][gloc.colIndex()] = null; // Remove the tile around the eagle
        }

        isShovelActive = false; // Activating anti-shovel powerup deactivates the shovel powerup
        isAntiShovelActive = true;
        antiShovelActivationTriggered = true; // Set the flag to indicate that the anti-shovel powerup was activated. It
                                              // is useful in successive activations.
    }

    /**
     * Deactivates the anti-shovel powerup, restoring the original tiles around the
     * eagle.
     * It uses the stored state from when the powerup was activated to restore the
     * tiles.
     */
    public void deactivateAntiShovelPowerup() {
        if (this.currentTilesAroundEagle.isEmpty())
            return;

        for (Map.Entry<GridLocation, BlockConfiguration> entry : this.currentTilesAroundEagle.entrySet()) {
            GridLocation gloc = entry.getKey();
            BlockConfiguration blockConf = entry.getValue();

            // Restore according to last tile configuration
            this.levelInfo.levelGrid[gloc.rowIndex()][gloc.colIndex()] = TileFactory.createTile(TileType.TILE_BRICKS,
                    gloc.colIndex(), gloc.rowIndex(), blockConf);
        }

        isAntiShovelActive = false;
    }
}