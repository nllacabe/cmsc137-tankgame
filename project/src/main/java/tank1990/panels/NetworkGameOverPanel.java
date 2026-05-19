package tank1990.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import tank1990.core.GameScoreStruct;
import tank1990.core.Globals;
import tank1990.core.Utils;
import tank1990.player.PlayerType;

/**
 * @class NetworkGameOverPanel
 * @brief Dedicated game over screen for network (master/slave) mode.
 *
 *        Shows a winner/loser message and automatically returns to the main
 *        menu
 *        after AUTO_RETURN_MS milliseconds. The player can also press ENTER to
 *        return immediately.
 *
 *        Usage:
 *        NetworkGameOverPanel panel = new NetworkGameOverPanel(
 *        frame, parentPanel, survivingPlayerType, localPlayerType, gameScore);
 *        rootPanel.add(panel, JLayeredPane.POPUP_LAYER);
 *        panel.start();
 */
public class NetworkGameOverPanel extends AbstractPanel implements KeyListener {

    private static final int AUTO_RETURN_MS = 6000; // auto-return to menu after 6 s
    private static final int COUNTDOWN_INTERVAL_MS = 1000;

    private final PlayerType survivingPlayer; // null if everyone lost (e.g. eagle destroyed)
    private final PlayerType localPlayer; // which player this screen belongs to
    private final GameScoreStruct gameScore;

    private JLabel countdownLabel;
    private int countdown = AUTO_RETURN_MS / COUNTDOWN_INTERVAL_MS;
    private Timer countdownTimer;
    private boolean returned = false;

    // ------------------------------------------------------------------
    // constructor

    /**
     * @param frame           The application frame.
     * @param parentPanel     The panel to return to (MenuPanel).
     * @param survivingPlayer The PlayerType that survived, or null if no one won.
     * @param localPlayer     The PlayerType running on this machine.
     * @param gameScore       Final game score.
     */
    public NetworkGameOverPanel(JFrame frame,
            JPanel parentPanel,
            PlayerType survivingPlayer,
            PlayerType localPlayer,
            GameScoreStruct gameScore) {
        super(frame);
        this.survivingPlayer = survivingPlayer;
        this.localPlayer = localPlayer;
        this.gameScore = gameScore;
        setParentPanel(parentPanel);
    }

    // ------------------------------------------------------------------ init

    @Override
    protected void initPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setOpaque(true);
        setFocusable(true);
        addKeyListener(this);
    }

    /**
     * Builds the UI and starts the countdown timer.
     * Call this after adding the panel to the layered pane.
     */
    public void start() {
        buildUI();
        requestFocusInWindow();
        startCountdown();
    }

    // ------------------------------------------------------------------ UI

    private void buildUI() {
        removeAll();

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.BLACK);
        container.setOpaque(true);
        container.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));

        Font bigFont = Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, 28);
        Font midFont = Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, 18);
        Font smallFont = Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, 14);

        // ── Result headline ──────────────────────────────────────────────────
        String headline;
        Color headlineColor;

        if (survivingPlayer == null) {
            headline = "DRAW!";
            headlineColor = Color.YELLOW;
        } else if (survivingPlayer == localPlayer) {
            headline = "YOU WIN!";
            headlineColor = Color.GREEN;
        } else {
            headline = "YOU LOSE!";
            headlineColor = Color.RED;
        }

        JLabel headlineLabel = new JLabel(headline, SwingConstants.CENTER);
        headlineLabel.setFont(bigFont);
        headlineLabel.setForeground(headlineColor);
        headlineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(headlineLabel);
        container.add(Box.createVerticalStrut(30));

        // ── Winner line ──────────────────────────────────────────────────────
        String winnerText = (survivingPlayer == null)
                ? "ALL PLAYERS ELIMINATED"
                : "WINNER: " + survivingPlayer.name().replace("_", " ");

        JLabel winnerLabel = new JLabel(winnerText, SwingConstants.CENTER);
        winnerLabel.setFont(midFont);
        winnerLabel.setForeground(Color.WHITE);
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(winnerLabel);
        container.add(Box.createVerticalStrut(20));

        container.add(Box.createVerticalStrut(40));

        // ── Countdown ────────────────────────────────────────────────────────
        countdownLabel = new JLabel(
                "RETURNING TO MENU IN " + countdown + "...", SwingConstants.CENTER);
        countdownLabel.setFont(smallFont);
        countdownLabel.setForeground(Color.GRAY);
        countdownLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(countdownLabel);
        container.add(Box.createVerticalStrut(15));

        // ── Press ENTER hint ─────────────────────────────────────────────────
        JLabel enterLabel = new JLabel("PRESS ENTER TO RETURN NOW", SwingConstants.CENTER);
        enterLabel.setFont(smallFont);
        enterLabel.setForeground(Color.DARK_GRAY);
        enterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(enterLabel);

        add(container, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // ------------------------------------------------------------------ countdown

    private void startCountdown() {
        countdownTimer = new Timer(COUNTDOWN_INTERVAL_MS, e -> {
            countdown--;
            if (countdownLabel != null) {
                countdownLabel.setText("RETURNING TO MENU IN " + Math.max(countdown, 0) + "...");
            }
            if (countdown <= 0) {
                countdownTimer.stop();
                returnToMenu();
            }
        });
        countdownTimer.setRepeats(true);
        countdownTimer.start();
    }

    // ------------------------------------------------------------------ navigation

    private void returnToMenu() {
        if (returned)
            return;
        returned = true;

        if (countdownTimer != null)
            countdownTimer.stop();

        tank1990.core.GameLevelManager.getInstance().reset();

        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            MenuPanel menuPanel = new MenuPanel(frame);
            frame.add(menuPanel);
            frame.revalidate();
            frame.repaint();
        });
    }

    // ------------------------------------------------------------------
    // KeyListener

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            returnToMenu();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}