package tank1990.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import tank1990.Game;
import tank1990.core.ConfigHandler;
import tank1990.core.GameMode;
import tank1990.core.Globals;
import tank1990.core.Utils;
import tank1990.network.NetworkManager;

/**
 * @class MenuPanel
 * @brief Main menu.
 *
 *        Menu items:
 *        0 - START GAME (single player)
 *        1 - LOCAL 2P (local multiplayer, same machine)
 *        2 - NETWORK (MASTER)(start network game as master)
 *        3 - NETWORK (SLAVE) (join network game as slave)
 *        4 - LOAD GAME
 */
public class MenuPanel extends SlidingPanel implements KeyListener {

    private int selectedIndex = 0;

    private static final String[] MENU_ITEMS = {
            "START GAME",
            "LOCAL 2P",
            "NETWORK (MASTER)",
            "NETWORK (SLAVE)",
            "LOAD GAME",
    };

    private static final List<JLabel> selectorItems = new ArrayList<>();

    // ---- network port used by master (slaves read theirs from config.txt) ----
    private static final int DEFAULT_MASTER_PORT = 5000;

    public MenuPanel(JFrame frame) {
        super(frame);
    }

    // ------------------------------------------------------------------ init

    @Override
    protected void initPanel() {
        for (KeyListener l : getKeyListeners())
            removeKeyListener(l);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        int width = frame.getWidth();
        int height = frame.getHeight();

        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setBounds(0, 0, width, height);
        backgroundPanel.setBackground(Color.BLACK);

        JPanel basePanel = new JPanel(new GridBagLayout());
        basePanel.setBounds(0, height, width, height);
        basePanel.setBackground(Color.BLACK);
        revalidate();
        repaint();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        // ---- Score row (15%) ----
        gbc.gridy = 0;
        gbc.weighty = 0.12;
        JPanel panelScore = new JPanel(new BorderLayout());
        panelScore.setBackground(Color.BLACK);
        int playerCount = NetworkManager.peekPlayerCount(Globals.NETWORK_CONFIG_FILE);
        String textScore = String.format("I-  %02d HI- %05d   CFG: %dP",
                Game.iPlayerScore,
                ConfigHandler.getInstance().getBattleCityProperties().hiScore(),
                playerCount);
        JLabel labelScore = new JLabel(textScore, SwingConstants.CENTER);
        labelScore.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, 18));
        labelScore.setForeground(Color.WHITE);
        panelScore.add(labelScore, BorderLayout.CENTER);
        basePanel.add(panelScore, gbc);

        // ---- Game title (30%) ----
        gbc.gridy = 1;
        gbc.weighty = 0.30;
        JPanel panelTitle = new JPanel();
        panelTitle.setLayout(new BoxLayout(panelTitle, BoxLayout.Y_AXIS));
        panelTitle.setBackground(Color.BLACK);

        TexturedFontPanel t1 = new TexturedFontPanel(Utils.loadPNGIcon(Globals.TEXTURE_TILE_BRICKS_PATH, 64, 64));
        t1.setText(Globals.GAME_RULE_TEXT);
        t1.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, 68));
        t1.setBackground(Color.BLACK);
        t1.setOpaque(true);
        t1.setAlignmentX(Component.CENTER_ALIGNMENT);
        t1.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        TexturedFontPanel t2 = new TexturedFontPanel(Utils.loadPNGIcon(Globals.TEXTURE_TILE_BRICKS_PATH, 64, 64));
        t2.setText("1990");
        t2.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, 68));
        t2.setBackground(Color.BLACK);
        t2.setOpaque(true);
        t2.setAlignmentX(Component.CENTER_ALIGNMENT);
        t2.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        panelTitle.add(t1);
        panelTitle.add(t2);
        basePanel.add(panelTitle, gbc);

        // ---- Selection list (40%) ----
        gbc.gridy = 2;
        gbc.weighty = 0.40;
        JPanel panelSelection = new JPanel(new GridBagLayout());
        panelSelection.setBackground(Color.BLACK);
        selectorItems.clear();

        for (int i = 0; i < MENU_ITEMS.length; i++) {
            // Selector icon
            GridBagConstraints gIcon = new GridBagConstraints();
            gIcon.gridx = 0;
            gIcon.gridy = i;
            gIcon.insets = new Insets(3, 5, 3, 5);
            gIcon.anchor = GridBagConstraints.WEST;
            JLabel icon = new JLabel(Utils.loadPNGIcon(Globals.ICON_PLAYER1_TANK_PATH, 28, 28));
            icon.setPreferredSize(new Dimension(28, 28));
            icon.setVisible(false);
            panelSelection.add(icon, gIcon);
            selectorItems.add(icon);

            // Menu text
            GridBagConstraints gText = new GridBagConstraints();
            gText.gridx = 1;
            gText.gridy = i;
            gText.insets = new Insets(3, 5, 3, 5);
            gText.anchor = GridBagConstraints.WEST;
            JLabel item = new JLabel(MENU_ITEMS[i]);
            item.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, 16));
            // Colour-code network items for clarity
            if (i == 2)
                item.setForeground(Color.CYAN);
            else if (i == 3)
                item.setForeground(Color.GREEN);
            else
                item.setForeground(Color.WHITE);
            panelSelection.add(item, gText);
        }
        basePanel.add(panelSelection, gbc);

        // ---- Copyright (18%) ----
        gbc.gridy = 3;
        gbc.weighty = 0.18;
        JPanel panelCopy = new JPanel(new BorderLayout());
        panelCopy.setBackground(Color.BLACK);
        JLabel labelCopy = new JLabel(Globals.COPYRIGHT_TEXT, SwingConstants.CENTER);
        labelCopy.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, 14));
        labelCopy.setForeground(Color.WHITE);
        panelCopy.add(labelCopy, BorderLayout.CENTER);
        basePanel.add(panelCopy, gbc);

        frame.add(basePanel);
        frame.add(backgroundPanel);
        startAnimation(basePanel, 5);
    }

    // ------------------------------------------------------------------ helpers

    private void updateSelectorVisibility() {
        if (selectorItems.isEmpty())
            return;
        for (int i = 0; i < selectorItems.size(); i++)
            selectorItems.get(i).setVisible(i == selectedIndex);
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    private void launchGame(GameMode mode) {
        frame.getContentPane().removeAll();
        GamePanel gp = new GamePanel(frame, this, mode);
        frame.add(gp);
        SwingUtilities.invokeLater(() -> {
            gp.requestFocusInWindow();
            gp.show();
            revalidate();
            repaint();
        });
    }

    /**
     * Launches the game in network master mode.
     * Asks the user which port to listen on (defaults to 5000),
     * then shows the "Waiting for players" overlay inside GamePanel.
     */
    private void launchNetworkMaster() {
        String input = JOptionPane.showInputDialog(
                frame,
                "Master listen port (default " + DEFAULT_MASTER_PORT + "):",
                "Network Game — Master",
                JOptionPane.QUESTION_MESSAGE);

        int port = DEFAULT_MASTER_PORT;
        if (input != null && !input.trim().isEmpty()) {
            try {
                port = Integer.parseInt(input.trim());
            } catch (NumberFormatException ignored) {
            }
        } else if (input == null) {
            return; // user cancelled
        }

        final int masterPort = port;
        frame.getContentPane().removeAll();
        GamePanel gp = new GamePanel(frame, this, GameMode.MODE_NETWORK_MASTER, masterPort);
        frame.add(gp);
        SwingUtilities.invokeLater(() -> {
            gp.requestFocusInWindow();
            revalidate();
            repaint();
        });
    }

    /**
     * Launches the game in network slave mode.
     * Slave connects to master address/port from config.txt automatically.
     */
    private void launchNetworkSlave() {
        // The slave's master address comes from config.txt — no extra dialog needed.
        // We still need a local port for the slave process (not used for listening,
        // just shown in logs). Pass 0 so GameEngine/NetworkManager ignores it.
        frame.getContentPane().removeAll();
        GamePanel gp = new GamePanel(frame, this, GameMode.MODE_NETWORK_SLAVE, 0);
        frame.add(gp);
        SwingUtilities.invokeLater(() -> {
            gp.requestFocusInWindow();
            revalidate();
            repaint();
        });
    }

    private void loadGame() {
        JFileChooser fc = new JFileChooser(Globals.DEFAULT_SAVE_LOCATION);
        if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return;

        File file = fc.getSelectedFile();
        GamePanel gp = new GamePanel(frame, this, GameMode.MODE_SINGLE_PLAYER);
        boolean error = true;
        try {
            frame.getContentPane().removeAll();
            gp.loadGame(new FileInputStream(file));
            frame.add(gp);
            SwingUtilities.invokeLater(() -> {
                gp.requestFocusInWindow();
                gp.showLoadedGame();
                revalidate();
                repaint();
            });
            error = false;
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "File not found!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Corrupted file!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Cannot read file!", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (error)
                gp.onScorePanelAnimationFinished();
        }
    }

    // ------------------------------------------------------------------ key events

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP) {
            selectedIndex = Math.max(0, selectedIndex - 1);
            updateSelectorVisibility();
        } else if (code == KeyEvent.VK_DOWN) {
            selectedIndex = Math.min(MENU_ITEMS.length - 1, selectedIndex + 1);
            updateSelectorVisibility();
        } else if (code == KeyEvent.VK_ENTER) {
            if (!isAnimationFinished) {
                finishAnimation();
                return;
            }
            switch (selectedIndex) {
                case 0 -> launchGame(GameMode.MODE_SINGLE_PLAYER);
                case 1 -> launchGame(GameMode.MODE_MULTI_PLAYER);
                case 2 -> launchNetworkMaster();
                case 3 -> launchNetworkSlave();
                case 4 -> loadGame();
                case 5 -> {
                    frame.getContentPane().removeAll();
                    AboutGamePanel about = new AboutGamePanel(frame, this);
                    frame.add(about);
                    SwingUtilities.invokeLater(() -> {
                        about.requestFocusInWindow();
                        about.setVisible(true);
                        frame.revalidate();
                        frame.repaint();
                    });
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    protected void animationStarted() {
    }

    @Override
    protected void animationFinished() {
        updateSelectorVisibility();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        frame.repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    @Override
    protected void resetPanel() {
        selectedIndex = 0;
        super.resetPanel();
    }
}
