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

import javax.swing.*;
import java.awt.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import tank1990.network.NetworkManager;
import tank1990.network.NetworkManager.InputPacket;
import tank1990.network.NetworkManager.GameStatePacket;
import tank1990.network.NetworkManager.FullGameSnapshot;
import tank1990.network.NetworkManager.PlayerState;
import tank1990.network.NetworkManager.BulletState;
import tank1990.network.NetworkManager.TileState;
import java.util.function.Consumer;
import tank1990.panels.GameAreaPanel;
import tank1990.player.Player;
import tank1990.player.PlayerType;
import tank1990.powerup.AbstractPowerup;
import tank1990.projectiles.Blast;
import tank1990.projectiles.Bullet;
import tank1990.projectiles.BulletType;
import tank1990.tank.AbstractTank;
import tank1990.tank.Enemy;

import tank1990.tile.Tile;

/**
 * @class GameEngine
 * @brief The main game engine that manages the game state, updates game
 *        objects, and handles game logic.
 * @details This class is responsible for managing players, enemies, powerups,
 *          bullets, and other game objects.
 *          It also handles the game loop and updates the game state
 *          periodically.
 */
public class GameEngine extends Subject {
    private GameAreaPanel parentPanel = null;

    // Game objects
    private ArrayList<Player> players = null; /* < List of zombies in the game. */
    private ArrayList<Enemy> enemies = null; /* < List of enemies in the game. */
    private ArrayList<AbstractPowerup> powerups = null; /* < List of powerups in the game. */
    private ArrayList<Blast> blastFXs = null; /* < List of blast effects in the game. */
    private ArrayList<Bullet> bullets = null; /* < List of bullets in the game. */

    // Game parameters
    private boolean isStopped = false;
    private boolean isPaused = false; /* < Flag indicating whether the game is paused. */

    private Timer gameTimer; /* < Timer for handling periodic updates. */

    private GameMode gameMode = GameMode.MODE_SINGLE_PLAYER;
    private GameLevel currentGameLevel = null;

    public GameEngine(GameMode gameMode) {
        this.gameMode = gameMode;

        this.players = new ArrayList<>();

        if (this.gameMode == GameMode.MODE_SINGLE_PLAYER) {
            players.add(new Player(PlayerType.PLAYER_1, this.gameMode));
        } else if (this.gameMode == GameMode.MODE_MULTI_PLAYER) {
            players.add(new Player(PlayerType.PLAYER_1, this.gameMode));
            players.add(new Player(PlayerType.PLAYER_2, this.gameMode));
        } else if (this.gameMode == GameMode.MODE_NETWORK_MASTER) {
            players.add(new Player(PlayerType.PLAYER_1, this.gameMode));
        } else if (this.gameMode == GameMode.MODE_NETWORK_SLAVE) {
            players.add(new Player(PlayerType.PLAYER_2, this.gameMode));
            localPlayerType = PlayerType.PLAYER_2;
        }

        // Game level manager stores the players lives even if game engine is created
        // again.
        for (Player player : players) {
            player.setRemainingLives(GameLevelManager.getInstance().getPlayerLives(player.getPlayerType()));
        }

        // Initialize other game objects
        this.enemies = new ArrayList<>();
        this.powerups = new ArrayList<>();
        this.blastFXs = new ArrayList<>();
        this.bullets = new ArrayList<>();

        // Sets the game engine instance to the GameLevelManager.
        // I know this is ugly but only solution to ensure game levels can access game
        // objects
        GameLevelManager.getInstance().setGameEngine(this);
        GameLevelManager.getInstance().addPredefinedGameLevels();

        // Initialize game timer
        gameTimer = new Timer(Globals.GAME_TICK_MS, e -> this.update());
        gameTimer.setRepeats(true);
    }

    /**
     * Custom paint method for rendering the game area.
     * This method is responsible for drawing all game objects (player, zombies,
     * projectiles, etc.) on the screen.
     *
     * @param g The Graphics object used to render the game area.
     */
    public void paintComponent(Graphics g) {
        // Draw map [Layer - 0]
        GameLevel gameLevel = GameLevelManager.getInstance().getCurrentLevel();
        if (gameLevel != null)
            gameLevel.draw(g, 0);

        // Draw player(s) [Layer - 1]
        for (Player p : this.players) {
            p.draw(g);
        }

        // Draw enemies [Layer - 2]
        for (Enemy e : this.enemies) {
            AbstractTank t = (AbstractTank) e;
            t.draw(g);
        }

        // Draw bullets [Layer - 3]
        for (Bullet b : this.bullets) {
            if (b == null)
                continue;
            b.draw(g);
        }

        // Draw higher layers of game map (trees etc.)
        if (gameLevel != null)
            gameLevel.draw(g, 1);

        // Draw blast animations [Layer - 5]
        for (Blast blastFX : this.blastFXs) {
            blastFX.draw(g);
        }

        // Draw powerups [Layer - 6]
        for (AbstractPowerup p : this.powerups) {
            if (p == null)
                continue;
            p.draw(g);
        }

    }

    // inside GameEngine class
    private NetworkManager networkManager;
    private PlayerType localPlayerType = PlayerType.PLAYER_1;
    private boolean isGameOver = false;

    // Resize tracking — rescale tank pixel positions when window size changes
    private int lastAreaWidth = 0;
    private int lastAreaHeight = 0;

    /**
     * Initializes the network for multiplayer mode.
     *
     * @param masterPort  The port number for the master server.
     * @param onConnected A callback to run once the network is initialized and
     *                    connected.
     */
    public void initNetwork(int masterPort, Runnable onConnected) {
        this.networkManager = new NetworkManager();
        boolean isMasterNode = (this.gameMode == GameMode.MODE_NETWORK_MASTER);

        Thread netThread = new Thread(() -> {
            try {
                if (isMasterNode) {
                    networkManager.startMaster(
                            masterPort,
                            (InputPacket input) -> SwingUtilities.invokeLater(() -> applyInputPacket(input)),
                            (GameStatePacket state) -> {
                            });

                    int numPlayers = networkManager.getNumPlayers();
                    SwingUtilities.invokeLater(() -> {
                        for (int i = players.size(); i < numPlayers; i++) {
                            players.add(new Player(PlayerType.fromIndex(i), gameMode));
                        }
                    });
                } else {
                    networkManager.startSlave(
                            (GameStatePacket state) -> {
                            },
                            (FullGameSnapshot snap) -> SwingUtilities.invokeLater(() -> applySnapshot(snap)));

                    PlayerType assigned = networkManager.getLocalPlayerType();
                    SwingUtilities.invokeLater(() -> {
                        players.clear();
                        players.add(new Player(assigned, gameMode));
                        localPlayerType = assigned;
                    });
                }

                if (onConnected != null)
                    SwingUtilities.invokeLater(onConnected);
            } catch (Exception e) {
                System.err.println("Failed to initialize network: " + e.getMessage());
                e.printStackTrace();
            }
        }, "NetworkInit");
        netThread.setDaemon(true);
        netThread.start();
    }

    private void applyInputPacket(InputPacket input) {
        Player target = getPlayerByType(input.playerType);
        if (target == null)
            return;
        switch (input.action) {
            case "MOVE_UP" -> target.decrementDy();
            case "MOVE_DOWN" -> target.incrementDy();
            case "MOVE_LEFT" -> target.decrementDx();
            case "MOVE_RIGHT" -> target.incrementDx();
            case "STOP_X" -> target.resetDx();
            case "STOP_Y" -> target.resetDy();
            case "SHOOT" -> triggerPlayerShooting(target);
        }
    }

    private void applySnapshot(FullGameSnapshot snap) {
        if (snap.gameOver) {
            endGame();
            return;
        }

        for (PlayerState ps : snap.players) {
            Player p = getPlayerByType(ps.playerType);
            if (p == null) {
                p = new Player(ps.playerType, gameMode);
                players.add(p);
            }

            if (ps.isDestroyed) {
                if (!p.isTankDestroyed()) {
                    Blast blast = p.destroy();
                    if (blast != null)
                        blastFXs.add(blast);
                }
            } else if (p.isTankDestroyed() && ps.remainingLives >= 0) {
                p.spawnTank();
                if (p.getTank() != null) {
                    p.getTank().setX(ps.x);
                    p.getTank().setY(ps.y);
                    try {
                        p.getTank().setDir(Direction.valueOf(ps.direction));
                    } catch (Exception ignored) {
                    }
                }
            } else if (p.getTank() != null && !p.isTankDestroyed()) {
                p.getTank().setX(ps.x);
                p.getTank().setY(ps.y);
                try {
                    p.getTank().setDir(Direction.valueOf(ps.direction));
                } catch (Exception ignored) {
                }
            }
        }

        players.removeIf(p -> snap.players.stream().noneMatch(ps -> ps.playerType == p.getPlayerType())
                && p.getPlayerType() != localPlayerType);

        boolean allDead = players.stream()
                .allMatch(p -> p.getRemainingLives() < 0 || (p.isTankDestroyed() && p.getRemainingLives() <= 0));
        if (allDead && !players.isEmpty()) {
            endGame();
            return;
        }

        this.bullets.clear();
        for (BulletState bs : snap.bullets) {
            try {
                Direction dir = Direction.valueOf(bs.direction);
                this.bullets.add(new Bullet(bs.x, bs.y, dir));
            } catch (Exception ignored) {
            }
        }

        GameLevel level = GameLevelManager.getInstance().getCurrentLevel();
        if (level != null && level.getMap() != null) {
            Tile[][] map = level.getMap();
            for (TileState ts : snap.tiles) {
                if (ts.row < 0 || ts.row >= Globals.ROW_TILE_COUNT)
                    continue;
                if (ts.col < 0 || ts.col >= Globals.COL_TILE_COUNT)
                    continue;
                if (ts.isDestroyed)
                    map[ts.row][ts.col] = null;
                else if (map[ts.row][ts.col] != null)
                    map[ts.row][ts.col].applySubpieces(ts.subpieces);
            }
        }
    }

    private void applyStatePacket(GameStatePacket state) {
    }

    public Player getPlayerByType(PlayerType type) {
        for (Player p : players)
            if (p.getPlayerType() == type)
                return p;
        return null;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public boolean isMasterNode() {
        return gameMode == GameMode.MODE_NETWORK_MASTER;
    }

    public boolean isSlaveNode() {
        return gameMode == GameMode.MODE_NETWORK_SLAVE;
    }

    public PlayerType getLocalPlayerType() {
        return localPlayerType;
    }

    /**
     * Updates all game objects, and notify the panel to draw them accordingly.
     * This method is called periodically to update all the game objects and check
     * for collisions.
     */
    /**
     * Rescales all tank pixel positions when the game area is resized.
     * Tanks store absolute pixel positions, so when the window changes size
     * their positions must be scaled to match the new dimensions.
     */
    private void rescaleTankPositionsIfNeeded() {
        java.awt.Dimension area = tank1990.panels.GamePanel.getGameAreaDimension();
        if (area == null)
            return;

        int newW = area.width;
        int newH = area.height;

        // First call — just record the current size, no rescaling needed
        if (lastAreaWidth == 0 || lastAreaHeight == 0) {
            lastAreaWidth = newW;
            lastAreaHeight = newH;
            return;
        }

        // No change — nothing to do
        if (newW == lastAreaWidth && newH == lastAreaHeight)
            return;

        // Window was resized — rescale all tank positions proportionally
        double scaleX = (double) newW / lastAreaWidth;
        double scaleY = (double) newH / lastAreaHeight;

        for (Player p : players) {
            if (p.getTank() != null) {
                p.getTank().setX((int) Math.round(p.getTank().getX() * scaleX));
                p.getTank().setY((int) Math.round(p.getTank().getY() * scaleY));
            }
        }
        for (Enemy e : enemies) {
            AbstractTank t = (AbstractTank) e;
            t.setX((int) Math.round(t.getX() * scaleX));
            t.setY((int) Math.round(t.getY() * scaleY));
        }
        for (tank1990.projectiles.Bullet b : bullets) {
            if (b != null) {
                b.setX((int) Math.round(b.getX() * scaleX));
                b.setY((int) Math.round(b.getY() * scaleY));
            }
        }

        lastAreaWidth = newW;
        lastAreaHeight = newH;
    }

    public void update() {
        // Check if window was resized and rescale all tank positions proportionally
        rescaleTankPositionsIfNeeded();

        // Slave nodes: master is authoritative — only animate blasts and repaint
        if (isSlaveNode()) {
            updateBlasts();
            notify(EventType.REPAINT, null);
            return;
        }

        GameLevel gameLevel = GameLevelManager.getInstance().getCurrentLevel();

        updateGameLevel();
        updatePlayers(gameLevel);
        updateEnemies(gameLevel);
        updateProjectiles(gameLevel);
        updatePowerups(gameLevel);
        checkCollisions(gameLevel);
        updateGameInfo();
        updateBlasts();

        // Broadcast full authoritative snapshot to slaves each tick
        if (isMasterNode() && networkManager != null) {
            FullGameSnapshot snap = new FullGameSnapshot();

            for (Player p : this.players) {
                if (p.getTank() == null)
                    continue;
                snap.players.add(new PlayerState(
                        p.getPlayerType(),
                        p.getTank().getX(), p.getTank().getY(),
                        p.getTank().getDir().name(),
                        p.isTankDestroyed(),
                        p.getRemainingLives()));
            }
            for (Bullet b : this.bullets) {
                if (b == null)
                    continue;
                snap.bullets.add(new BulletState(b.getX(), b.getY(), b.getDir().name(), b.isEnemyBullet()));
            }
            GameLevel level = GameLevelManager.getInstance().getCurrentLevel();
            if (level != null && level.getMap() != null) {
                Tile[][] map = level.getMap();
                for (int row = 0; row < Globals.ROW_TILE_COUNT; row++) {
                    for (int col = 0; col < Globals.COL_TILE_COUNT; col++) {
                        Tile tile = map[row][col];
                        if (tile == null) {
                            snap.tiles.add(new TileState(row, col, true,
                                    new boolean[Globals.TILE_SUBDIVISION][Globals.TILE_SUBDIVISION]));
                        } else if (tile.hasDamage()) {
                            snap.tiles.add(new TileState(row, col, false, tile.getSubpiecesAsBoolean()));
                        }
                    }
                }
            }
            snap.gameOver = this.isGameOver;
            networkManager.broadcastSnapshot(snap);
        }

        notify(EventType.REPAINT, null);
    }

    /**
     * Sets the parent panel for this game engine.
     *
     * @param parentPanel The parent GameAreaPanel
     */
    public void setParentPanel(GameAreaPanel parentPanel) {
        this.parentPanel = parentPanel;
    }

    /**
     * Gets current game level.
     *
     * @return Current game level
     */
    public GameLevel getCurrentLevel() {
        return GameLevelManager.getInstance().getCurrentLevel();
    }

    /**
     * Gets the index of the current game level.
     *
     * @return Index of the current game level
     */
    public int getCurrentLevelIndex() {
        return GameLevelManager.getInstance().getCurrentIndex();
    }

    /**
     * Gets the first player in multiplayer mode.
     * If the game mode is single player, this method returns the only player.
     *
     * @return The first player in multiplayer mode or the only player in single
     *         player mode.
     */
    public Player getPlayer1() {
        return !players.isEmpty() ? players.getFirst() : null;
    }

    /**
     * Gets the second player in multiplayer mode.
     * If the game mode is single player, this method returns null.
     *
     * @return The second player if in multiplayer mode, otherwise null.
     */
    public Player getPlayer2() {
        if (this.gameMode == GameMode.MODE_MULTI_PLAYER)
            return players.getLast();
        return null;
    }

    /**
     * Checks if the game is stopped.
     * 
     * @return true if the game is stopped, false otherwise.
     */
    public boolean isStopped() {
        return this.isStopped;
    }

    /**
     * Checks if the game is paused.
     * 
     * @return true if the game is paused, false otherwise.
     */
    public boolean isPaused() {
        return this.isPaused;
    }

    /**
     * Saves game objects and saves to default save location.
     */
    public void saveGame() {
        SwingUtilities.invokeLater(() -> {
            // Create saves directory if it doesn't exist
            File savesDir = new File(Globals.DEFAULT_SAVE_LOCATION);
            if (!savesDir.exists()) {
                savesDir.mkdirs();
            }

            // Generate filename with timestamp
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
            String formattedDatetime = LocalDateTime.now().format(formatter);
            String defaultFilename = formattedDatetime + ".dat";

            File fileToSave = new File(savesDir, defaultFilename);

            // Save content to the file
            try (FileOutputStream savedFile = new FileOutputStream(fileToSave);
                    ObjectOutputStream os = new ObjectOutputStream(savedFile)) {

                serializeGameObjects(os);

                notify(EventType.GAME_SAVED, fileToSave.getAbsolutePath()); // Notify observers to repaint event
                System.out.println("Game saved to: " + fileToSave.getAbsolutePath());

            } catch (IOException e) {
                System.err.println("Failed to save game: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Serializes the game objects to the specified output stream.
     *
     * @param os The output stream to write the serialized game objects to.
     * @throws IOException If an I/O error occurs while writing to the output
     *                     stream.
     */
    private void serializeGameObjects(ObjectOutputStream os) throws IOException {
        // Serialize game level manager
        os.writeObject(GameLevelManager.getInstance());

        // Serialize player object
        os.writeObject(this.players);

        // Serialize enemies
        os.writeObject(this.enemies);

        // Serialize projectiles
        os.writeObject(this.bullets);

        // Serialize powerups
        os.writeObject(this.powerups);

        // Serialize blast effects
        os.writeObject(this.blastFXs);

        os.write(this.gameMode.ordinal());
    }

    /**
     * Creates game objects from the stream of saved file.
     * The order of the objects in the stream must match the order in which they
     * were serialized.
     * 
     * @param inputStream The input stream containing the serialized game state.
     * @throws ClassNotFoundException If a class definition cannot be found during
     *                                deserialization.
     * @throws IOException            If an I/O error occurs while reading from the
     *                                input stream.
     */
    @SuppressWarnings("unchecked")
    private void createGameObjects(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        try {
            GameLevelManager.setInstance((GameLevelManager) inputStream.readObject());
            GameLevelManager.getInstance().setGameEngine(this);

            this.players = (ArrayList<Player>) inputStream.readObject();
            this.enemies = (ArrayList<Enemy>) inputStream.readObject();
            this.bullets = (ArrayList<Bullet>) inputStream.readObject();
            this.powerups = (ArrayList<AbstractPowerup>) inputStream.readObject();
            this.blastFXs = (ArrayList<Blast>) inputStream.readObject();

            this.currentGameLevel = GameLevelManager.getInstance().getCurrentLevel();
        } catch (EOFException e) {
            // Reached to end of file
        }
    }

    /**
     * Loads a saved game from the specified input stream.
     * This method reads the game objects from the input stream and initializes the
     * game objects according to saved data.
     *
     * @param inputStream The input stream to read the saved game data from.
     * @throws IOException            If an I/O error occurs while reading the input
     *                                stream.
     * @throws ClassNotFoundException If a class cannot be found during
     *                                deserialization.
     */
    public void loadGame(FileInputStream inputStream) throws IOException, ClassNotFoundException {
        if (inputStream == null)
            return;

        ObjectInputStream os = new ObjectInputStream(inputStream);

        reset();

        createGameObjects(os);

        os.close();
    }

    /**
     * Starts the game engine.
     * This method initializes the game state and starts the game timer.
     */
    public void start() {
        this.isStopped = false;
        this.isPaused = false;
        this.gameTimer.start();

        notify(EventType.STARTED, null); // Notify observers to repaint event
    }

    /**
     * Resumes the game engine if it was paused.
     * This method resumes the game timer and updates the game state.
     */
    public void pause() {
        this.isStopped = false;
        this.isPaused = true;
        this.gameTimer.stop();

        notify(EventType.PAUSED, null); // Notify observers to repaint event
    }

    /**
     * Stops the game engine.
     * This method stops the game timer and sets the game state to stopped.
     */
    public void stop() {
        this.isStopped = true;
        this.isPaused = false;
        if (this.gameTimer != null)
            this.gameTimer.stop();
    }

    /**
     * Resets the game engine by clearing all game objects and resetting the game
     * state.
     * This method stops the game timer, resets the game objects, and notifies
     * observers that the game has ended.
     */
    public void reset() {
        this.isStopped = false;
        this.isPaused = false;

        // Reset game objects
        this.players.clear();
        this.enemies.clear();
        this.powerups.clear();
        this.blastFXs.clear();
        this.bullets.clear();

        this.isGameOver = false;
        this.lastAreaWidth = 0;
        this.lastAreaHeight = 0;

        if (this.gameMode == GameMode.MODE_SINGLE_PLAYER) {
            players.add(new Player(PlayerType.PLAYER_1, this.gameMode));
        } else if (this.gameMode == GameMode.MODE_MULTI_PLAYER) {
            players.add(new Player(PlayerType.PLAYER_1, this.gameMode));
            players.add(new Player(PlayerType.PLAYER_2, this.gameMode));
        } else if (this.gameMode == GameMode.MODE_NETWORK_MASTER) {
            players.add(new Player(PlayerType.PLAYER_1, this.gameMode));
        } else if (this.gameMode == GameMode.MODE_NETWORK_SLAVE) {
            players.add(new Player(PlayerType.PLAYER_2, this.gameMode));
            localPlayerType = PlayerType.PLAYER_2;
        }
    }

    /**
     * Loads the next game level.
     * This method stops the current game level and loads the next one from the
     * GameLevelManager.
     */
    public void loadGameLevel() {
        this.stop();

        GameLevel currentGameLevel = GameLevelManager.getInstance().nextLevel();
        if (currentGameLevel != null) {
            this.currentGameLevel = currentGameLevel;
            if (isMasterNode() || isSlaveNode()) {
                this.currentGameLevel.setNetworkMode(true);
            }
            this.currentGameLevel.setCurrentState(LevelState.LOADED);
        }
    }

    /**
     * Starts the current game level.
     * This method starts the game timer and sets the game level state to PLAYING.
     */
    public void startGameLevel() {
        start();
        this.currentGameLevel.setCurrentState(LevelState.PLAYING);
    }

    /**
     * Adds a projectile to the projectile list when the player shoots.
     */
    public void triggerPlayerShooting(Player player) {
        Bullet bullet = player.shoot();
        if (bullet != null)
            addBullet(bullet);
    }

    /**
     * Adds a bullet to the game.
     * 
     * @param bullet The bullet to be added
     */
    private void addBullet(Bullet bullet) {
        if (bullet == null)
            return;

        this.bullets.add(bullet);
    }

    /**
     * Updates the players' tanks and checks for player deaths.
     * If a player's tank is destroyed, it spawns a new tank.
     * If a player has no remaining lives, they are removed from the game.
     */
    private void updatePlayers(GameLevel gameLevel) {
        // Remove players' tank
        Iterator<Player> it = this.players.iterator();
        while (it.hasNext()) {
            Player p = it.next();

            if (p.isTankDestroyed() && p.getRemainingLives() >= 0) {
                p.spawnTank();
            }

            if (p.getRemainingLives() < 0) {
                // Player is dead, remove from the game
                it.remove();
            }

            p.update(gameLevel);
            GameLevelManager.getInstance().setPlayerLives(p.getPlayerType(), p.getRemainingLives());
        }

        // If there are no players left, game is over
        if (this.players.isEmpty()) {
            endGame();
            return;
        }

        // Network mode: last one standing wins
        if ((isMasterNode() || isSlaveNode()) && this.players.size() == 1) {
            endGame();
            return;
        }
    }

    /**
     * Updates the game level status, including checking level progression.
     */
    private void updateGameLevel() {
        GameLevel gameLevel = GameLevelManager.getInstance().getCurrentLevel();

        // In network mode there are no enemies — skip zero-enemy win condition
        if (!isMasterNode() && !isSlaveNode()) {
            if (gameLevel.getRemainingEnemyTanks() == 0 && gameLevel.getActiveEnemyTankCount() == 0) {
                System.out.println("All enemy tanks destroyed, going to next level...");
                goToNextLevel();
                return;
            }
        }

        AbstractTank newEnemyTank = GameLevelManager.getInstance().update();
        if (newEnemyTank != null) {
            this.enemies.add((Enemy) newEnemyTank);
        }
    }

    /**
     * Updates the enemies' tanks and checks for enemy deaths.
     * If an enemy's tank is destroyed, it is removed from the game.
     */
    private void updateEnemies(GameLevel gameLevel) {
        // Remove destroyed enemies
        Iterator<Enemy> it = this.enemies.iterator();
        while (it.hasNext()) {
            AbstractTank t = (AbstractTank) it.next();
            if (t.isDestroyed())
                it.remove();
        }

        // Update remaining enemies
        for (Enemy e : this.enemies) {
            AbstractTank t = (AbstractTank) e;
            t.update(gameLevel);
            Bullet bullet = t.shoot();
            if (bullet != null) {
                addBullet(bullet);
            }
        }
    }

    /**
     * Updates the projectiles in the game.
     * This method updates the position of each bullet and checks if they are out of
     * bounds.
     * If a bullet is out of bounds, it is destroyed and removed from the game.
     */
    private void updateProjectiles(GameLevel gameLevel) {
        Iterator<Bullet> it = this.bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();

            // Update bullet position
            b.update(gameLevel);

            if (b.isOutOfBounds(gameLevel.getGameAreaSize().width, gameLevel.getGameAreaSize().height)) {
                Blast blast = b.destroy(); // Notify the tank that bullet is destroyed
                blastFXs.add(blast);
                it.remove(); // Remove bullet
                continue;
            }

        }
    }

    /**
     * Updates the powerups in the game.
     * This method updates the position of each powerup and checks if they are
     * collected by players.
     */
    private void updatePowerups(GameLevel gameLevel) {
        // Remove expired powerup first
        this.powerups.removeIf(AbstractPowerup::isExpired);

        for (AbstractPowerup p : this.powerups) {
            p.update();
        }
    }

    /**
     * Checks for collisions between game objects.
     * 
     * @param gameLevel The current game level to check for collisions
     */
    private void checkCollisions(GameLevel gameLevel) {
        checkProjectileCollisions(gameLevel);

        checkPowerupCollisions(gameLevel);
    }

    /**
     * Checks for collisions between bullets and other game objects (tiles, tanks,
     * etc.).
     * This method iterates through all bullets and checks for collisions with
     * tiles, player tanks, enemy tanks, and other bullets.
     * If a collision occurs, the bullet is destroyed and the appropriate effects
     * are applied.
     * 
     * @param gameLevel The current game level to check for collisions
     */
    private void checkProjectileCollisions(GameLevel gameLevel) {
        // Collect bullets to be removed to avoid ConcurrentModificationException
        ArrayList<Bullet> bulletsToRemove = new ArrayList<>();

        // Check bullet collisions with tiles, tanks, and other bullets
        Iterator<Bullet> bulletIt = this.bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            RectangleBound bulletBounds = bullet.getBoundingBox();
            boolean bulletDestroyed = false;

            // Skip if bullet is already marked for removal
            if (bulletsToRemove.contains(bullet)) {
                continue;
            }

            // 1. Check collision with tiles
            if (!bulletDestroyed && checkBulletTileCollision(bullet, gameLevel)) {
                Blast blast = bullet.destroy();
                blastFXs.add(blast);
                bulletsToRemove.add(bullet);
                bulletDestroyed = true;
                continue;
            }

            // 2. Check collision with player tanks
            if (!bulletDestroyed) {
                for (Player player : this.players) {
                    if (!player.isTankDestroyed()) {
                        RectangleBound tankBounds = player.getBoundingBox();
                        if (tankBounds == null)
                            continue;

                        // Check if bullet intersects with player tank
                        if (RectangleBound.isCollided(bulletBounds, tankBounds)) {
                            // Check if bullet belongs to enemy
                            if (bullet.isEnemyBullet()) {
                                // Player tank hit by enemy bullet
                                player.getDamage();

                                Blast blast = bullet.destroy();
                                blastFXs.add(blast);
                                bulletsToRemove.add(bullet);
                                bulletDestroyed = true;
                                break;
                            }
                        }
                    }
                }
            }

            // 3. Check collision with enemy tanks
            if (!bulletDestroyed) {
                Iterator<Enemy> enemyIt = this.enemies.iterator();
                while (enemyIt.hasNext() && !bulletDestroyed) {
                    Enemy enemy = enemyIt.next();
                    AbstractTank enemyTank = (AbstractTank) enemy;
                    if (!enemyTank.isDestroyed()) {
                        RectangleBound tankBounds = enemyTank.getBoundingBox();
                        if (RectangleBound.isCollided(bulletBounds, tankBounds)) {
                            // Check if bullet belongs to player (don't let enemies shoot themselves)
                            if (!bullet.isEnemyBullet()) {
                                // Enemy tank hit by player bullet
                                boolean isDamaged = enemyTank.getDamage();
                                if (!isDamaged) {
                                    // Just destroy the bullet if enemy tank is not damaged
                                    Blast blast = bullet.destroy();
                                    blastFXs.add(blast);
                                    bulletsToRemove.add(bullet);
                                    bulletDestroyed = true;
                                    break;
                                }

                                // If enemy tank is red, spawn a powerup
                                if (enemyTank.isRedTank()) {
                                    AbstractPowerup powerup = gameLevel.spawnPowerup();
                                    if (powerup != null) {
                                        this.powerups.clear(); // Clear existing powerups to avoid duplicate powerups
                                        this.powerups.add(powerup);
                                    }
                                }

                                // Remove enemy if destroyed
                                if (enemyTank.isDestroyed()) {
                                    gameLevel.decreaseActiveEnemyTank();
                                    updateEnemyTankScore(enemyTank);
                                    enemyIt.remove();
                                }

                                Blast blast = bullet.destroy();
                                blastFXs.add(blast);
                                bulletsToRemove.add(bullet);
                                bulletDestroyed = true;
                                break;
                            }
                        }
                    }
                }
            }

            // 4. Check collision with other bullets
            if (!bulletDestroyed) {
                for (Bullet otherBullet : this.bullets) {
                    // Don't check bullet against itself
                    if (bullet == otherBullet) {
                        continue;
                    }

                    // Skip if other bullet is already marked for removal
                    if (bulletsToRemove.contains(otherBullet)) {
                        continue;
                    }

                    RectangleBound otherBulletBounds = otherBullet.getBoundingBox();
                    if (RectangleBound.isCollided(bulletBounds, otherBulletBounds)) {
                        // Check if bullets belong to different teams
                        if (bullet.isEnemyBullet() != otherBullet.isEnemyBullet()) {
                            // Bullets from different teams (enemy vs player tanks) collided - destroy both
                            Blast blast1 = bullet.destroy();
                            Blast blast2 = otherBullet.destroy();
                            blastFXs.add(blast1);
                            blastFXs.add(blast2);

                            bulletsToRemove.add(bullet);
                            bulletsToRemove.add(otherBullet);
                            bulletDestroyed = true;
                            break;
                        }
                    }
                }
            }
        }

        // Remove all bullets that were marked for removal
        this.bullets.removeAll(bulletsToRemove);
    }

    /**
     * Checks if a bullet collides with any destroyable tiles.
     * 
     * @param bullet    The bullet to check
     * @param gameLevel The current game level
     * @return true if collision occurred and tile was destroyed
     */
    private boolean checkBulletTileCollision(Bullet bullet, GameLevel gameLevel) {
        if (gameLevel == null || gameLevel.getMap() == null) {
            return false;
        }

        RectangleBound bulletBounds = bullet.getBoundingBox();

        // Convert bullet position to grid coordinates
        GridLocation bulletGridLoc = Utils.loc2GridLoc(new Location(bullet.getX(), bullet.getY()));

        // Get the neighboring tiles of the bullet's grid location
        Tile[] neighborTiles = gameLevel.getNeighbors(bulletGridLoc);
        for (Tile tile : neighborTiles) {

            if (tile == null) {
                continue;
            }
            RectangleBound tileBounds = tile.getBoundingBox();

            // Check for intersection
            if (RectangleBound.isCollided(bulletBounds, tileBounds)) {
                // If the bullet hits to any tile, try to destroy the tile
                switch (tile.getType()) {
                    case TILE_BRICKS, TILE_STEEL, TILE_TREES:
                        return tile.destroy(bullet);
                    case TILE_EAGLE:
                        return destroyEagleTile(tile);
                    default:
                        break;
                }
                // Trees, ice, and water don't stop bullets
                break;
            }
        }

        return false; // Do not stop bullet if no collision occurred
    }

    /**
     * Checks powerup collisions with tanks.
     * 
     * @param gameLevel The current game level to check for collisions
     */
    private void checkPowerupCollisions(GameLevel gameLevel) {
        Iterator<AbstractPowerup> it = this.powerups.iterator();

        HashMap<AbstractPowerup, Player> collectedPowerupsByPlayer = new HashMap<>();
        HashMap<AbstractPowerup, AbstractTank> collectedPowerupsByEnemy = new HashMap<>();

        while (it.hasNext()) {
            AbstractPowerup powerup = it.next();
            RectangleBound powerupBounds = powerup.getBoundingBox();

            // Check collision with player tanks
            for (Player player : this.players) {
                if (!player.isTankDestroyed()) {
                    RectangleBound tankBounds = player.getBoundingBox();
                    if (tankBounds == null)
                        continue;

                    // Check if powerup intersects with player tank
                    if (RectangleBound.isCollided(powerupBounds, tankBounds)) {
                        collectedPowerupsByPlayer.put(powerup, player);
                        break; // No need to check other players
                    }
                }
            }

            // Check collision with enemy tanks
            for (Enemy enemy : this.enemies) {
                AbstractTank enemyTank = (AbstractTank) enemy;
                if (!enemyTank.isDestroyed()) {
                    RectangleBound tankBounds = enemyTank.getBoundingBox();
                    if (tankBounds == null)
                        continue;

                    // Check if powerup intersects with enemy tank
                    if (RectangleBound.isCollided(powerupBounds, tankBounds)) {
                        collectedPowerupsByEnemy.put(powerup, enemyTank);
                        break; // No need to check other enemies
                    }
                }
            }
        }

        for (AbstractPowerup powerup : collectedPowerupsByPlayer.keySet()) {
            applyPowerupEffects(gameLevel, powerup, collectedPowerupsByPlayer.get(powerup));
            // Remove collected powerups from the game
            this.powerups.remove(powerup);
        }

        for (AbstractPowerup powerup : collectedPowerupsByEnemy.keySet()) {
            applyPowerupEffects(gameLevel, powerup, collectedPowerupsByEnemy.get(powerup));
            // Remove collected powerups from the game
            this.powerups.remove(powerup);
        }

    }

    /**
     * Checks if a tank collides with any other tanks.
     * 
     * @param tank               The tank to check for collisions
     * @param tentativeTankBound The bounding box of the tank to check against other
     *                           tanks
     * @return true if the tank collides with any other tank, false otherwise
     */
    public boolean checkTankCollisions(AbstractTank tank, RectangleBound tentativeTankBound) {
        for (Player player : this.players) {
            if (player.getTank() == tank)
                continue;

            if (RectangleBound.isCollided(tentativeTankBound, player.getBoundingBox()))
                return true;
        }

        for (Enemy enemy : this.enemies) {
            AbstractTank enemyTank = (AbstractTank) enemy;
            if (enemyTank == tank)
                continue;
            if (RectangleBound.isCollided(tentativeTankBound, enemyTank.getBoundingBox()))
                return true;
        }

        return false; // No collision detected
    }

    /**
     * Gets the locations of all tanks in the game.
     * 
     * @return A list of grid locations representing the positions of all tanks
     *         (both player and enemy).
     */
    public ArrayList<GridLocation> getTankLocations() {
        ArrayList<GridLocation> tankLocations = new ArrayList<>();

        // Add player tanks
        for (Player player : this.players) {
            if (!player.isTankDestroyed()) {
                RectangleBound bound = player.getBoundingBox();
                GridLocation loc = Utils.loc2GridLoc(new Location(bound.getOriginX(), bound.getOriginY()));
                tankLocations.add(loc);
            }
        }

        // Add enemy tanks
        for (Enemy enemy : this.enemies) {
            AbstractTank enemyTank = (AbstractTank) enemy;
            if (!enemyTank.isDestroyed()) {
                RectangleBound bound = enemyTank.getBoundingBox();
                GridLocation loc = Utils.loc2GridLoc(new Location(bound.getOriginX(), bound.getOriginY()));
                tankLocations.add(loc);
            }
        }

        return tankLocations;
    }

    /**
     * Applies the effects of a powerup on the player.
     * This method handles the specific effects of each powerup type.
     *
     * @param gameLevel The current game level
     * @param powerup   The powerup to apply
     * @param player    The player who collected the powerup
     */
    private void applyPowerupEffects(GameLevel gameLevel, AbstractPowerup powerup, Player player) {
        switch (powerup.getPowerupType()) {
            case POWERUP_GRENADE -> {
                // Destroy all enemy tanks
                Iterator<Enemy> enemyIt = this.enemies.iterator();

                while (enemyIt.hasNext()) {
                    Enemy enemy = enemyIt.next();
                    AbstractTank enemyTank = (AbstractTank) enemy;
                    if (!enemyTank.isDestroyed()) {
                        Blast b = enemyTank.destroy(); // Destroy call directly destructs the tank
                        updateEnemyTankScore(enemyTank);
                        gameLevel.decreaseActiveEnemyTank();

                        this.blastFXs.add(b);
                        enemyIt.remove();
                    }
                }
            }
            case POWERUP_HELMET -> {
                // No specific action for tank powerup
            }
            case POWERUP_SHOVEL -> {
                // Shovel around the eagle tile
                GameLevelManager.getInstance().getCurrentLevel().activateShovelPowerup();
            }
            case POWERUP_STAR -> {
                // No specific action for tank powerup
            }
            case POWERUP_TANK -> {
                // No specific action for tank powerup
            }
            case POWERUP_TIMER -> {
                // Freezes all enemy tanks for a short duration
                for (Enemy enemy : this.enemies) {
                    AbstractTank enemyTank = (AbstractTank) enemy;
                    if (!enemyTank.isDestroyed()) {
                        enemyTank.setFrozen(true);
                    }
                }
            }
            case POWERUP_WEAPON -> {
                // No specific action for tank powerup
            }
            default -> {
                System.err.println("Unknown powerup type: " + powerup.getPowerupType());
            }
        }
    }

    /**
     * Applies the effects of a powerup on the enemy tank.
     * This method handles the specific effects of each powerup type.
     *
     * @param gameLevel The current game level
     * @param powerup   The powerup to apply
     * @param enemyTank The enemy tank that collected the powerup
     */
    private void applyPowerupEffects(GameLevel gameLevel, AbstractPowerup powerup, AbstractTank enemyTank) {
        switch (powerup.getPowerupType()) {
            case POWERUP_GRENADE -> {
                // Destroy all enemy tanks
                Iterator<Player> playerIt = this.players.iterator();

                while (playerIt.hasNext()) {
                    Player player = playerIt.next();
                    Blast b = player.destroy(); // Destroy call directly destructs player's tank
                    this.blastFXs.add(b);
                }
            }
            case POWERUP_HELMET -> {
                // No specific action for tank powerup
            }
            case POWERUP_SHOVEL -> {
                // Remove the protection around the eagle tile
                GameLevelManager.getInstance().getCurrentLevel().activateAntiShovelPowerup();
            }
            case POWERUP_STAR -> {
                // No specific action for tank powerup
            }
            case POWERUP_TANK -> {
                // No specific action for tank powerup
            }
            case POWERUP_TIMER -> {
                for (Player player : this.players) {
                    if (!player.isTankDestroyed()) {
                        player.setFrozen(true);
                    }
                }
            }
            case POWERUP_WEAPON -> {
                // No specific action for tank powerup
            }
            default -> {
                System.err.println("Unknown powerup type: " + powerup.getPowerupType());
            }
        }
    }

    /**
     * Updates the game information such as score, lives, etc.
     * This method can be expanded later to include more detailed game statistics.
     */
    private void updateGameInfo() {
        notify(EventType.UPDATE_GAME_INFO, GameLevelManager.getInstance().getGameScore()); // Notify observers that the
                                                                                           // next level is loaded
    }

    /**
     * Updates the score of the enemy tank.
     * This method is called when an enemy tank is destroyed.
     * It updates the game score based on the type of enemy tank destroyed.
     *
     * @param enemyTank The enemy tank that was destroyed
     */
    private void updateEnemyTankScore(AbstractTank enemyTank) {
        if (enemyTank == null)
            return;

        GameLevelManager.getInstance().addTankScore(enemyTank);
    }

    /**
     * Notifies observers about game is ready to switch next level.
     *
     */
    private void goToNextLevel() {
        stop(); // Stop the game engine

        // Add some delay before notifying observers
        Timer delayedTimer = new Timer(2000, e -> {
            notify(EventType.NEXT_LEVEL, GameLevelManager.getInstance().getGameScore()); // Notify observers that the
                                                                                         // next level is loaded
        });
        delayedTimer.start();
        delayedTimer.setRepeats(false);
    }

    /**
     * Ends the game and notifies observers that the game is over.
     * This method stops the game engine and notifies observers about the game over
     * event.
     */
    private void endGame() {
        if (this.isGameOver)
            return;
        this.isGameOver = true;
        stop();
        Timer delayedTimer = new Timer(2000, e -> {
            notify(EventType.GAMEOVER, GameLevelManager.getInstance().getGameScore());
        });
        delayedTimer.setRepeats(false);
        delayedTimer.start();
    }

    /**
     * Destroys the eagle tile and ends the game.
     * This method is called when the eagle tile is destroyed by a bullet.
     *
     * @param tile The eagle tile that was destroyed
     * @return true if the bullet should be stopped, false otherwise
     */
    private boolean destroyEagleTile(Tile tile) {
        if (isMasterNode() || isSlaveNode())
            return true; // No eagle in network mode
        tile.destroy(null);
        endGame();
        return true;
    }

    /**
     * Updates the blast effects in the game.
     * This method iterates through all blast effects and updates their animations.
     * If a blast effect is done, it is removed from the list.
     */
    private void updateBlasts() {
        Iterator<Blast> it = blastFXs.iterator();
        while (it.hasNext()) {
            Blast blast = it.next();

            if (!blast.update()) {
                // If the blast animation is done, remove it
                it.remove();
            }
        }
    }
}