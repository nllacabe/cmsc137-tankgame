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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import tank1990.player.Player;
import tank1990.player.PlayerType;
import tank1990.tank.*;

/**
 * @class GameLevelManager
 * @brief Manages the game levels, player scores, and game state.
 * @details This class is responsible for loading, updating, and transitioning between game levels.
 * It also keeps track of the player's score and lives. Since it is a singleton, it ensures that there is only one
 * instance of GameLevelManager throughout the game.
 */
public class GameLevelManager implements Serializable {
    private ArrayList<GameLevel> gameLevels; // List of game levels
    private int currentLevelIndex; // Index of the current game level
    private int totalPlayerScore; // Total score of the player

    private TimeTick spawnTick;

    private static GameLevelManager instance;

    private GameScoreStruct gameScore = null;

    private HashMap<PlayerType, Integer> playersRemainingLives;

    private transient GameEngine gameEngine = null;

    private GameLevelManager() {
        this.gameLevels = new ArrayList<>();
        this.currentLevelIndex = -1;
        this.totalPlayerScore = 0;

        this.playersRemainingLives = new HashMap<>();
    }

    /**
     * Returns the singleton instance of GameLevelManager.
     * If the instance is null, it creates a new instance.
     *
     * @return The singleton instance of GameLevelManager.
     */
    public static GameLevelManager getInstance() {
        if (instance == null) {
            instance = new GameLevelManager();
        }
        return instance;
    }

    /**
     * Sets the instance of GameLevelManager.
     * This method is used to set a particular instance. It is useful to initialize upon saved data.
     *
     * @param sourceInstance The GameLevelManager instance to set.
     */
    public static void setInstance(GameLevelManager sourceInstance) {
        instance = sourceInstance;
    }

    /**
     * Sets the game engine for the GameLevelManager.
     * This method is used to set the game engine instance that manages the game levels.
     *
     * @param engine The GameEngine instance to set.
     */
    public void setGameEngine(GameEngine engine) {
        getInstance().gameEngine = engine;
    }

    /**
     * Returns the game engine associated with the GameLevelManager.
     * This method retrieves the game engine instance that manages the game levels.
     *
     * @return The GameEngine instance associated with this GameLevelManager.
     */
    public GameEngine getGameEngine() {
        return getInstance().gameEngine;
    }

    /**
     * Initializes the game level manager by adding predefined levels.
     */
    public void addPredefinedGameLevels() {
        this.gameLevels.clear();
        // Add predefined game levels to the manager.
        // For now only stage 1 is predefined one which is second stage in original Tank 1990 game.
        GameLevel level = new GameLevel(Globals.MAP_PATH + String.format("stage-%02d.bin", 1));
        this.gameLevels.add(level);
    }

    /**
     * Returns the current game level index.
     *
     * @return The index of the current game level.
     */
    public int getCurrentIndex() { return this.currentLevelIndex; }

    /**
     * Returns the current game level.
     *
     * @return The current GameLevel instance.
     */
    public GameLevel getCurrentLevel() {
        if (this.currentLevelIndex < 0 || this.currentLevelIndex >= this.gameLevels.size()) {
            return null;
        }

        return this.gameLevels.get(this.currentLevelIndex);
    }

    /**
     * Returns the total player score.
     *
     * @return The total player score.
     */
    public int getPlayerScore() {
        return (this.gameScore==null)? 0: this.gameScore.getTotalScore();
    }

    /**
     * Returns the total player score.
     *
     * @return The total player score.
     */
    public GameScoreStruct getGameScore() {
        return this.gameScore;
    }

    /**
     * Resets the game level manager to its initial state.
     * This method clears the current levels, resets the score, and re-adds the levels.
     */
    public void reset() {
        System.out.println("Resetting game level manager...");
        this.currentLevelIndex = -1;
        this.totalPlayerScore = 0;
        this.gameScore = new GameScoreStruct();
        this.spawnTick = null;
        this.playersRemainingLives.clear();

        // Reset all game levels
        this.gameLevels.clear();

        this.gameEngine = null;

        // Add predefined game levels again
        addPredefinedGameLevels();
    }

    /**
     * Returns the next game level in the sequence.
     * If the current level is the last one, it wraps around to the first level.
     *
     * @return The next GameLevel instance.
     */
    public GameLevel nextLevel() {
        currentLevelIndex = Math.max(0, (this.currentLevelIndex+1));
        return loadLevel(currentLevelIndex);
    }

    /**
     * Loads a game level by its index.
     *
     * @param levelIndex The index of the level to load.
     * @return The loaded GameLevel instance.
     * @throws IndexOutOfBoundsException if the levelIndex is invalid.
     */
    private GameLevel loadLevel(int levelIndex) {
        System.out.println("Loading level: " + levelIndex);

        // Generate a new level info with random game map and enemy types if level index exceeds the size of gameLevels
        if (levelIndex >= this.gameLevels.size()) {
            this.gameLevels.add(new GameLevel());
        }

        if (levelIndex < 0 || levelIndex >= this.gameLevels.size()) {
            throw new IndexOutOfBoundsException("Invalid level index: " + levelIndex);
        }
        // Create a new game score structure for the new level
        this.gameScore = new GameScoreStruct();
        this.gameScore.setReachedLevel(levelIndex+1); // Level index is 0-based, so we add 1 to match the level number
        this.gameScore.setHiScore(ConfigHandler.getInstance().getBattleCityProperties().hiScore());
        this.gameScore.setRemainingTankCount(0);
        this.gameScore.setTotalScore(this.totalPlayerScore);  // Update total score to the current score for new level

        if (this.spawnTick!=null) this.spawnTick.reset();

        return this.gameLevels.get(levelIndex);
    }

    /**
     * Updates the game level and spawns enemy tanks if conditions are met.
     *
     * @return An instance of AbstractTank if an enemy tank is spawned, null otherwise.
     */
    public AbstractTank update() {
        GameLevel gameLevel = getCurrentLevel();
        gameLevel.update();

        if (this.spawnTick!=null) this.spawnTick.updateTick();

        this.gameScore.setRemainingTankCount(gameLevel.getEnemyTankCounts());

        if (gameLevel.getActiveEnemyTankCount()<4 && gameLevel.getEnemyTankCounts()>0) {
            if (this.spawnTick==null) {
                this.spawnTick = new TimeTick(Utils.Time2GameTick(Globals.ENEMY_TANK_SPAWN_DELAY_MS));
                this.spawnTick.setRepeats(0);  // Do not repeat
                return null;
            }

            if (this.spawnTick.isTimeOut()) {
                this.spawnTick.reset();
                return gameLevel.spawnEnemyTank();
            }
        }

        return null;
    }

    /**
     * Adds points to player's score.
     *
     * @param point The points to be added to the player's score.
     */
    public void addPlayerScore(int point) {
        if (this.gameScore != null) {
            this.gameScore.setTotalScore(this.gameScore.getTotalScore() + point);
        }
    }

    /**
     * Adds the score of a tank to the game score.
     * This method updates the score based on the type of tank and increments the count of that tank type.
     *
     * @param tank The tank whose score is to be added.
     */
    public void addTankScore(AbstractTank tank) {
        if (this.gameScore==null) return;

        if (tank instanceof BasicTank) {
            this.gameScore.setBasicTankScore(this.gameScore.getBasicTankScore()+tank.getPoints());
            this.gameScore.setBasicTankCount(this.gameScore.getBasicTankCount()+1);
        }
        else if (tank instanceof FastTank) {
            this.gameScore.setFastTankScore(this.gameScore.getFastTankScore()+tank.getPoints());
            this.gameScore.setFastTankCount(this.gameScore.getFastTankCount()+1);
        }
        else if (tank instanceof PowerTank) {
            this.gameScore.setPowerTankScore(this.gameScore.getPowerTankScore()+tank.getPoints());
            this.gameScore.setPowerTankCount(this.gameScore.getPowerTankCount()+1);
        }
        else if (tank instanceof ArmorTank) {
            this.gameScore.setArmorTankScore(this.gameScore.getArmorTankScore()+tank.getPoints());
            this.gameScore.setArmorTankCount(this.gameScore.getArmorTankCount()+1);
        } else { }

        addPlayerScore(tank.getPoints());
        this.totalPlayerScore = this.gameScore.getTotalScore();

    }

    /**
     * Sets the player's remaining lives.
     *
     * @param playerType The player whose lives are to be set.
     * @param lives The number of lives to set for the player.
     */
    public void setPlayerLives(PlayerType playerType, int lives) {
        if (this.gameScore != null) {
            this.gameScore.setPlayerRemainingLives(lives);
        }

        this.playersRemainingLives.putIfAbsent(playerType, lives);
    }

    /**
     * Gets the number of lives remaining for a player.
     *
     * @param playerType The player whose lives are to be retrieved.
     * @return The number of lives remaining for the player.
     */
    public int getPlayerLives(PlayerType playerType) {
        return this.playersRemainingLives.getOrDefault(playerType, Globals.INITAL_PLAYER_HEALTH-1);  // Player tanks should be already spawned so decrease by 1 for the first time
    }
}
