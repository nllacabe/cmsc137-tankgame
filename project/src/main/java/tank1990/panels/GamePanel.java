package tank1990.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import tank1990.Game;
import tank1990.core.*;
import tank1990.network.NetworkManager;
import tank1990.network.NetworkManager.InputPacket;
import tank1990.player.Player;
import tank1990.player.PlayerType;

/**
 * @class GamePanel
 * @brief Represents the main game panel that contains the game area, info
 *        panel, and overlays.
 * @details This class handles the gameplay area, player controls, game state
 *          management, and rendering of game objects.
 */
public class GamePanel extends AbstractPanel implements ActionListener, KeyListener, Observer {

    private static Dimension gameAreaDimension = null;
    private static GameAreaPanel gameplayAreaRef = null; // live reference for dynamic sizing

    GameEngine gameEngine = null;

    JLayeredPane rootPanel = null; // Store reference to layered root panel
    JPanel getReadyPanel = null; // Store reference to get ready panel
    JPanel gamePanel = null; // Store reference to game content panel
    GameAreaPanel gameplayArea = null; // Store reference to gameplay area
    GameInfoPanel gameInfoPanel = null; // Store reference to game info panel
    JPanel pausePanel = null; // Store reference to pause overlay panel
    JPanel gameOverPanel = null; // Store reference to game over overlay panel
    ScorePanel gameScorePanel = null; // Store reference to game over overlay panel

    private boolean isGameOver = false;

    public GamePanel(JFrame frame, JPanel parentPanel, GameMode gameMode) {
        super(frame);

        setParentPanel(parentPanel);

        postInitPanel();

        this.gameEngine = new GameEngine(gameMode);
        this.gameEngine.subscribe((Observer) this);

        gameplayArea.setGameEngine(this.gameEngine);
    }

    /**
     * Network constructor — used by MenuPanel for NETWORK_MASTER and NETWORK_SLAVE
     * modes.
     * Shows a "Waiting for players" overlay until all slaves connect, then starts
     * the level.
     */
    public GamePanel(JFrame frame, JPanel parentPanel, GameMode networkMode, int masterPort) {
        super(frame);

        setParentPanel(parentPanel);

        postInitPanel();

        this.gameEngine = new GameEngine(networkMode);
        this.gameEngine.subscribe((Observer) this);

        gameplayArea.setGameEngine(this.gameEngine);

        // Show waiting overlay until network is ready
        JPanel waitOverlay = createOverlayLabel("WAITING FOR PLAYERS...", Color.YELLOW, 0.6f);
        rootPanel.add(waitOverlay, JLayeredPane.POPUP_LAYER);
        rootPanel.repaint();

        this.gameEngine.initNetwork(masterPort, () -> {
            rootPanel.remove(waitOverlay);
            rootPanel.repaint();
            show();
        });
    }

    @Override
    public void eventFilter(EventType event, Object data) {
        switch (event) {
            case EventType.REPAINT:
                repaint();
                break;
            case EventType.UPDATE_MAP:
                // Handle map update
                break;
            case EventType.PAUSED:
                showPauseOverlay();
                break;
            case EventType.UPDATE_GAME_INFO:
                this.gameInfoPanel.update((GameScoreStruct) data);
                break;
            case EventType.STARTED:
                hidePauseOverlay();
                break;
            case EventType.NEXT_LEVEL: {
                System.out.println("Game Panel: Next Level Event Triggered");
                this.isGameOver = false;
                this.gameEngine.reset();

                // Add timer to show game score overlay after GAMEOVER_OVERLAY_DURATION
                // milliseconds
                Timer gameScoreTimer = new Timer(Globals.GAMEOVER_OVERLAY_DURATION, e -> {
                    showGameScoreOverlay((GameScoreStruct) data);
                });
                gameScoreTimer.setRepeats(false); // Only execute once
                gameScoreTimer.start();

                break;
            }
            case EventType.GAMEOVER: {
                System.out.println("Game Panel: Game Over Event Triggered");
                this.isGameOver = true;
                // Update Player score if current game score is higher
                int playerCurrentScore = ((GameScoreStruct) data).getTotalScore();
                Game.iPlayerScore = Math.max(Game.iPlayerScore, playerCurrentScore);

                // Update high score if current score is higher than the saved high score
                if (playerCurrentScore > ConfigHandler.getInstance().getBattleCityProperties().hiScore()) {
                    ConfigHandler.getInstance().setProperty("BattleCity", "HiScore",
                            Integer.toString(playerCurrentScore));
                }

                showGameOverOverlay();
                // Add timer to show game score overlay after GAMEOVER_OVERLAY_DURATION
                // milliseconds
                Timer gameScoreTimer = new Timer(Globals.GAMEOVER_OVERLAY_DURATION, e -> {
                    showGameScoreOverlay((GameScoreStruct) data);
                });
                gameScoreTimer.setRepeats(false); // Only execute once
                gameScoreTimer.start();
                break;
            }
            case EventType.GAME_LOADED: {
                // No implementation
            }
            case EventType.GAME_SAVED: {
                String path = (String) data;
                System.out.printf("Game Saved: %s\n", path);

                showGameSavedStatusOverlay(path);
                Timer popupTimer = new Timer(Globals.POPUP_OVERLAY_DURATION_MS, e -> {
                    hideGameSavedStatusOverlay();
                });
                popupTimer.setRepeats(false);
                popupTimer.start();
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void initPanel() {
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.BLACK);
        addKeyListener(this);

        // Keep gameAreaDimension in sync whenever the window is resized,
        // so all coordinate conversions and hitboxes stay accurate.
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateLayoutForCurrentSize();
            }
        });
    }

    /**
     * Recalculates all panel dimensions based on the current frame size.
     * Called on startup and whenever the window is resized.
     */
    private void updateLayoutForCurrentSize() {
        if (gameplayArea == null || rootPanel == null)
            return;

        int w = frame.getContentPane().getWidth();
        int h = frame.getContentPane().getHeight();
        if (w <= 0 || h <= 0)
            return;

        int gameplaySize = Math.min(w * 3 / 4, h);

        gameplayArea.setPreferredSize(new Dimension(gameplaySize, gameplaySize));
        gameAreaDimension = new Dimension(gameplaySize, gameplaySize);

        if (gameInfoPanel != null) {
            gameInfoPanel.setPreferredSize(new Dimension(w - gameplaySize, gameplaySize));
        }
        if (gamePanel != null) {
            gamePanel.setBounds(0, 0, w, h);
            gamePanel.revalidate();
        }

        rootPanel.setPreferredSize(new Dimension(w, h));
        rootPanel.revalidate();
        revalidate();
        repaint();
    }

    @Override
    protected void postInitPanel() {
        // Set this panel's layout to BorderLayout to contain the root panel
        this.setLayout(new BorderLayout());

        // Root Panel as JLayeredPane
        this.rootPanel = new JLayeredPane();
        this.rootPanel.setPreferredSize(new Dimension(Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT));

        // Game Content Panel (contains gameplay area and info panel)
        this.gamePanel = new JPanel(new BorderLayout());
        this.gamePanel.setBounds(0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);

        // Gameplay Area (3/4 of the width)
        this.gameplayArea = new GameAreaPanel();
        this.gameplayArea.setBackground(Color.BLACK);
        this.gameplayArea.setOpaque(true);
        gameplayAreaRef = this.gameplayArea; // keep static ref for live sizing

        // Calculate dimensions for square gameplay area
        int gameplaySize = Math.min(Globals.WINDOW_WIDTH * 3 / 4, Globals.WINDOW_HEIGHT);
        this.gameplayArea.setPreferredSize(new Dimension(gameplaySize, gameplaySize));
        gameAreaDimension = this.gameplayArea.getPreferredSize();

        // Game Info Panel (1/4 of the width)
        this.gameInfoPanel = new GameInfoPanel(this.frame, this);
        this.gameInfoPanel.setPreferredSize(new Dimension(Globals.WINDOW_WIDTH / 4, gameplaySize));

        // Add components to game content panel
        this.gamePanel.add(this.gameplayArea, BorderLayout.CENTER);
        this.gamePanel.add(this.gameInfoPanel, BorderLayout.EAST);

        // Add game content panel to layered pane (background layer)
        this.rootPanel.add(this.gamePanel, JLayeredPane.DEFAULT_LAYER);

        // Add the root panel to this GamePanel component
        this.add(this.rootPanel, BorderLayout.CENTER);

        // Apply correct dimensions immediately based on current frame size
        SwingUtilities.invokeLater(this::updateLayoutForCurrentSize);
    }

    /**
     * Called when the panel is added to the container.
     *
     * This method is overridden to set this panel for focus which is required for
     * keyboard events.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    public static Dimension getGameAreaDimension() {
        // Always return the actual rendered size of the gameplay area.
        // This keeps Utils.gridLoc2Loc() and checkMovable() in sync with
        // the current window size without any caching lag.
        if (gameplayAreaRef != null) {
            int w = gameplayAreaRef.getWidth();
            int h = gameplayAreaRef.getHeight();
            if (w > 0 && h > 0) {
                gameAreaDimension = new Dimension(w, h);
            }
        }
        return gameAreaDimension;
    }

    /**
     * Custom paint method for rendering the game area.
     *
     * This method is responsible for drawing all game objects (player, zombies,
     * projectiles, etc.) on the screen.
     *
     * @param g The Graphics object used to render the game area.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    /**
     * Serializes the current game objects to an output stream.
     *
     * @param os The output stream to write the serialized game objects to.
     * @throws IOException If an I/O error occurs during serialization.
     */
    private void serializeGameObjects(ObjectOutputStream os) throws IOException {
        // Serialize game engine
        os.writeObject(this.gameEngine);
    }

    /**
     * Creates game objects from a serialized input stream.
     *
     * @param os The input stream to read the serialized game objects from.
     * @throws IOException            If an I/O error occurs during deserialization.
     * @throws ClassNotFoundException If the class definition of a game object is
     *                                not found.
     */
    private void createGameObjects(ObjectInputStream os) throws IOException, ClassNotFoundException {
        try {
            this.gameEngine = (GameEngine) os.readObject();
        } catch (EOFException e) {
            // Reached to end of file
        }
    }

    /**
     * Opens a dialog screen to save the current game state to a file.
     */
    public void saveGame() {
        SwingUtilities.invokeLater(() -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save File"); // Set dialog title

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
            String formattedDatetime = LocalDateTime.now().format(formatter);
            String defaultFilename = formattedDatetime + ".dat";

            fileChooser.setSelectedFile(new File(defaultFilename));

            int userSelection = fileChooser.showSaveDialog(null); // Open "Save File" dialog

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile(); // Get selected file

                // Save content to the file
                try {
                    FileOutputStream savedFile = new FileOutputStream(fileToSave);
                    try {
                        ObjectOutputStream os = new ObjectOutputStream(savedFile);

                        serializeGameObjects(os);

                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {

                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Stops game timer, and clears resources.
     */
    public void exit() {
        if (this.gameEngine.isStopped())
            this.gameEngine.stop();

        // if (this.backgroundSoundFX!=null) {
        // this.backgroundSoundFX.stop();
        // this.backgroundSoundFX = null;
        // }
    }

    /**
     * Ends the game and transitions to the game over state.
     */
    public void endGame() {
        exit();

        // showGameOverDialog();
    }

    /**
     * Handles key press events.
     * This method is called when a key is pressed.
     *
     * @param e The KeyEvent that contains the details of the key press.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        int location = e.getKeyLocation();

        // ── Network slave: route all key presses to master via InputPacket ──
        if (gameEngine.isSlaveNode()) {
            NetworkManager nm = gameEngine.getNetworkManager();
            if (nm != null) {
                PlayerType localType = nm.getLocalPlayerType();
                switch (key) {
                    case (Globals.KEY_PLAYER_1_MOVE_UP) -> nm.sendInput(new InputPacket(localType, "MOVE_UP"));
                    case (Globals.KEY_PLAYER_1_MOVE_RIGHT) -> nm.sendInput(new InputPacket(localType, "MOVE_RIGHT"));
                    case (Globals.KEY_PLAYER_1_MOVE_DOWN) -> nm.sendInput(new InputPacket(localType, "MOVE_DOWN"));
                    case (Globals.KEY_PLAYER_1_MOVE_LEFT) -> nm.sendInput(new InputPacket(localType, "MOVE_LEFT"));
                    case (Globals.KEY_PLAYER_1_MOVE_SHOOT) -> nm.sendInput(new InputPacket(localType, "SHOOT"));
                    default -> {
                    }
                }
            }
            return;
        }

        // ── Local control (single player, local multi, or network master) ──
        Player player = null;
        switch (key) {
            case (Globals.KEY_PLAYER_1_MOVE_UP):
                player = gameEngine.getPlayer1();
                if (player != null)
                    player.decrementDy();
                break;
            case (Globals.KEY_PLAYER_1_MOVE_RIGHT):
                player = gameEngine.getPlayer1();
                if (player != null)
                    player.incrementDx();
                break;
            case (Globals.KEY_PLAYER_1_MOVE_DOWN):
                player = gameEngine.getPlayer1();
                if (player != null)
                    player.incrementDy();
                break;
            case (Globals.KEY_PLAYER_1_MOVE_LEFT):
                player = gameEngine.getPlayer1();
                if (player != null)
                    player.decrementDx();
                break;
            case (Globals.KEY_PLAYER_2_MOVE_UP):
                player = gameEngine.getPlayer2();
                if (player != null)
                    player.decrementDy();
                break;
            case (Globals.KEY_PLAYER_2_MOVE_RIGHT):
                player = gameEngine.getPlayer2();
                if (player != null)
                    player.incrementDx();
                break;
            case (Globals.KEY_PLAYER_2_MOVE_DOWN):
                player = gameEngine.getPlayer2();
                if (player != null)
                    player.incrementDy();
                break;
            case (Globals.KEY_PLAYER_2_MOVE_LEFT):
                player = gameEngine.getPlayer2();
                if (player != null)
                    player.decrementDx();
                break;
            case (Globals.KEY_PLAYER_1_MOVE_SHOOT):
                player = gameEngine.getPlayer1();
                if (player != null)
                    gameEngine.triggerPlayerShooting(player);
                break;
            case (Globals.KEY_PLAYER_2_MOVE_SHOOT):
                // Left Ctrl fires P2 — check location to distinguish from right Ctrl
                if (location == KeyEvent.KEY_LOCATION_LEFT) {
                    player = gameEngine.getPlayer2();
                    if (player != null)
                        gameEngine.triggerPlayerShooting(player);
                }
                break;
            case KeyEvent.VK_ESCAPE:
                // Only master or single/local player can pause
                if (!gameEngine.isSlaveNode()) {
                    if (gameEngine.isPaused()) {
                        gameEngine.start();
                    } else {
                        gameEngine.pause();
                        gameEngine.saveGame();
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Handles key release events.
     * This method is called when a key is released.
     *
     * @param e The KeyEvent that contains the details of the key release.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        int location = e.getKeyLocation();

        // ── Network slave: route key releases to master ──
        if (gameEngine.isSlaveNode()) {
            NetworkManager nm = gameEngine.getNetworkManager();
            if (nm != null) {
                PlayerType localType = nm.getLocalPlayerType();
                switch (key) {
                    case (Globals.KEY_PLAYER_1_MOVE_UP),
                            (Globals.KEY_PLAYER_1_MOVE_DOWN) ->
                        nm.sendInput(new InputPacket(localType, "STOP_Y"));
                    case (Globals.KEY_PLAYER_1_MOVE_LEFT),
                            (Globals.KEY_PLAYER_1_MOVE_RIGHT) ->
                        nm.sendInput(new InputPacket(localType, "STOP_X"));
                    default -> {
                    }
                }
            }
            return;
        }

        // ── Local control ──
        Player player = null;
        switch (key) {
            case (Globals.KEY_PLAYER_1_MOVE_UP):
                player = gameEngine.getPlayer1();
                if (player != null)
                    player.resetDy();
                break;
            case (Globals.KEY_PLAYER_1_MOVE_RIGHT):
                player = gameEngine.getPlayer1();
                if (player != null)
                    player.resetDx();
                break;
            case (Globals.KEY_PLAYER_1_MOVE_DOWN):
                player = gameEngine.getPlayer1();
                if (player != null)
                    player.resetDy();
                break;
            case (Globals.KEY_PLAYER_1_MOVE_LEFT):
                player = gameEngine.getPlayer1();
                if (player != null)
                    player.resetDx();
                break;
            case (Globals.KEY_PLAYER_2_MOVE_UP):
                player = gameEngine.getPlayer2();
                if (player != null)
                    player.resetDy();
                break;
            case (Globals.KEY_PLAYER_2_MOVE_RIGHT):
                player = gameEngine.getPlayer2();
                if (player != null)
                    player.resetDx();
                break;
            case (Globals.KEY_PLAYER_2_MOVE_DOWN):
                player = gameEngine.getPlayer2();
                if (player != null)
                    player.resetDy();
                break;
            case (Globals.KEY_PLAYER_2_MOVE_LEFT):
                player = gameEngine.getPlayer2();
                if (player != null)
                    player.resetDx();
                break;
            case (Globals.KEY_PLAYER_1_MOVE_SHOOT):
                break;
            case (KeyEvent.VK_ENTER):
                GameLevel currentLevel = gameEngine.getCurrentLevel();
                if (currentLevel != null && currentLevel.getCurrentState() == LevelState.GET_READY) {
                    SwingUtilities.invokeLater(() -> showGamePanel());
                }
                break;
            default:
                break;
        }
    }

    /**
     * Handles key typing events.
     *
     * Not implemented
     *
     * @param e The KeyEvent that contains the details of the key typed.
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    /**
     * Shows the game panel and starts the game level.
     * This method is called when the game is started or resumed.
     */
    public void show() {
        this.gameEngine.loadGameLevel();
        showGetReadyPanel();
    }

    /**
     * Shows the loaded game state by displaying the "Get Ready" panel.
     * This method is called when a game is loaded from a file.
     */
    public void showLoadedGame() {
        showGetReadyPanel();
    }

    /**
     * Shows the "Get Ready" panel before starting the game level.
     * This panel displays the current level index and prompts the player to get
     * ready.
     */
    private JPanel createOverlayLabel(String text, Color color, float alpha) {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBounds(0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(color);
        label.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, 32));
        panel.add(label);
        return panel;
    }

    private void showGetReadyPanel() {
        int levelIndex = this.gameEngine.getCurrentLevelIndex();
        levelIndex = (levelIndex == 0) ? 1 : levelIndex + 1;

        // Hide the main game panel
        if (this.gamePanel != null) {
            this.gamePanel.setVisible(false);
        }

        // Create and configure the get ready panel
        this.getReadyPanel = new JPanel();
        this.getReadyPanel.setLayout(new BorderLayout());

        JLabel getReadyLabel = new JLabel(String.format("STAGE\t%2d", levelIndex), SwingConstants.CENTER);
        getReadyLabel.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, 24));
        getReadyLabel.setForeground(Color.BLACK);

        this.getReadyPanel.add(getReadyLabel, BorderLayout.CENTER);
        this.getReadyPanel.setBackground(Globals.COLOR_GRAY);
        this.getReadyPanel.setBounds(0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
        this.getReadyPanel.setVisible(true);

        // Add get ready panel to layered pane (foreground layer)
        this.rootPanel.add(this.getReadyPanel, JLayeredPane.POPUP_LAYER);

        this.rootPanel.revalidate();
        this.rootPanel.repaint();

        this.gameEngine.getCurrentLevel().setCurrentState(LevelState.GET_READY);
    }

    /**
     * Shows the main game panel and starts the game level.
     * This method is called when the player is ready to start the game.
     */
    private void showGamePanel() {
        // Clean up the get ready panel
        if (this.getReadyPanel != null) {
            this.getReadyPanel.setVisible(false);
            this.rootPanel.remove(getReadyPanel);
            this.getReadyPanel = null;
        }

        // Clean up the score panel if it exists
        if (this.gameScorePanel != null) {
            this.gameScorePanel.setVisible(false);
            this.rootPanel.remove(this.gameScorePanel);
            this.gameScorePanel = null;
        }

        // Show the main game panel
        if (this.gamePanel != null) {
            this.gamePanel.setVisible(true);
        }

        // Start the game level
        this.gameEngine.startGameLevel();

        requestFocus(); // Ensure game panel has focus for key events
        this.rootPanel.revalidate();
        this.rootPanel.repaint();
    }

    /**
     * Shows the pause overlay on top of the game area.
     * Displays "PAUSE" text centered over the gameplay area.
     */
    private void showPauseOverlay() {
        // Don't create multiple pause panels
        if (this.pausePanel != null) {
            return;
        }

        // Create pause overlay panel that covers only the gameplay area
        this.pausePanel = new JPanel();
        this.pausePanel.setLayout(new BorderLayout());

        // Create the PAUSE label
        JLabel pauseLabel = new JLabel("PAUSE", SwingConstants.CENTER);
        pauseLabel.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, 48));
        pauseLabel.setForeground(Color.YELLOW);

        // Add label to panel
        this.pausePanel.add(pauseLabel, BorderLayout.CENTER);

        // Set semi-transparent background
        this.pausePanel.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black
        this.pausePanel.setOpaque(true);

        // Position the pause panel to cover only the gameplay area
        // Calculate the gameplay area position within the game panel
        int gameplaySize = Math.min(Globals.WINDOW_WIDTH * 3 / 4, Globals.WINDOW_HEIGHT);
        this.pausePanel.setBounds(0, 0, gameplaySize, gameplaySize);
        this.pausePanel.setVisible(true);

        // Add pause panel to the highest layer
        this.rootPanel.add(this.pausePanel, JLayeredPane.MODAL_LAYER);

        this.rootPanel.revalidate();
        this.rootPanel.repaint();
    }

    /**
     * Hides the pause overlay from the game area.
     */
    private void hidePauseOverlay() {
        if (this.pausePanel != null) {
            this.pausePanel.setVisible(false);
            this.rootPanel.remove(this.pausePanel);
            this.pausePanel = null;

            this.rootPanel.revalidate();
            this.rootPanel.repaint();
        }
    }

    /**
     * Shows the game over overlay on top of the game area.
     * Displays "GAME OVER" text centered over the gameplay area.
     */
    private void showGameOverOverlay() {
        // Don't create multiple pause panels
        if (this.gameOverPanel != null) {
            return;
        }

        // Create pause overlay panel that covers only the gameplay area
        this.gameOverPanel = new JPanel();
        this.gameOverPanel.setLayout(new BorderLayout());

        // Create the GAME OVER label
        JLabel gameOverLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        gameOverLabel.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, 48));
        gameOverLabel.setForeground(Color.RED);

        // Add label to panel
        this.gameOverPanel.add(gameOverLabel, BorderLayout.CENTER);

        // Set semi-transparent background
        this.gameOverPanel.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black
        this.gameOverPanel.setOpaque(true);

        // Position the game over panel to cover only the gameplay area
        // Calculate the gameplay area position within the game panel
        int gameplaySize = Math.min(Globals.WINDOW_WIDTH * 3 / 4, Globals.WINDOW_HEIGHT);
        this.gameOverPanel.setBounds(0, 0, gameplaySize, gameplaySize);
        this.gameOverPanel.setVisible(true);

        // Add pause panel to the highest layer
        this.rootPanel.add(this.gameOverPanel, JLayeredPane.MODAL_LAYER);

        this.rootPanel.revalidate();
        this.rootPanel.repaint();

    }

    /**
     * Shows the game score overlay with the provided game score.
     *
     * @param gameScore The GameScoreStruct containing the game score to display.
     */
    private void showGameScoreOverlay(GameScoreStruct gameScore) {
        System.out.println("Game Panel: Showing game score overlay...");
        // Don't create multiple pause panels
        if (this.gameScorePanel != null) {
            return;
        }

        // Create pause overlay panel that covers only the gameplay area
        this.gameScorePanel = new ScorePanel(this.frame, this, gameScore);
        this.gameScorePanel.setLayout(new BorderLayout());

        this.rootPanel.add(this.gameScorePanel, JLayeredPane.POPUP_LAYER);

        this.gameScorePanel.showPanel();

        this.rootPanel.revalidate();
        this.rootPanel.repaint();
    }

    /**
     * Shows a temporary overlay indicating that the game has been saved.
     *
     * @param filePath The path where the game was saved.
     */
    private void showGameSavedStatusOverlay(String filePath) {
        // Don't create if pause panel is not exist
        if (this.pausePanel == null)
            return;

        // Create the PAUSE label
        JLabel labelSavedStatus = new JLabel(String.format("Game Saved: %s", filePath), SwingConstants.CENTER);
        labelSavedStatus.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, 10));
        labelSavedStatus.setForeground(Color.WHITE);

        // Add label to panel
        this.pausePanel.add(labelSavedStatus, BorderLayout.SOUTH);

        this.rootPanel.revalidate();
        this.rootPanel.repaint();
    }

    /**
     * Hides the game saved status overlay.
     * This method removes the saved status label from the pause panel.
     */
    private void hideGameSavedStatusOverlay() {
        if (this.pausePanel == null)
            return;

        Component southComponent = ((BorderLayout) this.pausePanel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        if (southComponent != null) {
            this.pausePanel.remove(southComponent);
            this.rootPanel.revalidate();
            this.rootPanel.repaint();
        }
    }

    /**
     * Loads a game from the provided input stream.
     *
     * @param inputStream The input stream containing the serialized game state.
     * @throws ClassNotFoundException If a class definition cannot be found during
     *                                deserialization.
     * @throws IOException            If an I/O error occurs while reading from the
     *                                input stream.
     */
    public void loadGame(FileInputStream inputStream) throws ClassNotFoundException, IOException {
        this.gameEngine.loadGame(inputStream);
    }

    /**
     * Resets the game panel to its initial state.
     * This method is called when the score panel animation finishes.
     * It clears the game engine and resets the UI components.
     */
    public void onScorePanelAnimationFinished() {
        // Clean up the score panel first
        if (this.gameScorePanel != null) {
            this.gameScorePanel.setVisible(false);
            this.rootPanel.remove(this.gameScorePanel);
            this.gameScorePanel = null;
        }

        if (this.isGameOver) {
            // Shut down network connections if in network mode
            if (this.gameEngine != null && this.gameEngine.getNetworkManager() != null) {
                this.gameEngine.getNetworkManager().shutdown();
            }

            // Reset the game engine
            this.gameEngine = null;
            GameLevelManager.getInstance().reset();

            // Reset current panel
            resetPanel();

            // Reset menu panel
            SwingUtilities.invokeLater(() -> {
                if (getParentPanel() != null) {
                    MenuPanel menuPanel = (MenuPanel) getParentPanel();
                    menuPanel.resetPanel();
                    this.frame.revalidate();
                    this.frame.repaint();
                }
            });
        } else {
            System.out.println("Game Panel: Starting next level...");

            // Ensure proper cleanup and revalidation before showing ready panel
            this.rootPanel.revalidate();
            this.rootPanel.repaint();

            // Use SwingUtilities.invokeLater to ensure UI updates are processed
            SwingUtilities.invokeLater(() -> {
                this.gameEngine.loadGameLevel();
                showGetReadyPanel();
                requestFocus(); // Ensure the panel has focus for key events
            });
        }
    }
}