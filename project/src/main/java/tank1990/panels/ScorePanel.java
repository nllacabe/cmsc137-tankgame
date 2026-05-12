package tank1990.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import tank1990.core.GameScoreStruct;
import tank1990.core.Globals;
import tank1990.core.Utils;

/**
 * @class ScorePanel
 * @brief Displays the score panel with detailed scores for different tank types.
 * @details This panel shows the scores for basic, fast, power, and armor tanks, along with the total score.
 * It also handles key events to navigate back to the main menu.
 */
public class ScorePanel extends AbstractPanel implements KeyListener {

    private boolean keyPressDisabled = true;

    private boolean isAnimationFinished = false;

    private JLabel labelScoreBasicTank;
    private JLabel labelScoreFastTank;
    private JLabel labelScorePowerTank;
    private JLabel labelScoreArmorTank;

    private JLabel labelCountBasicTank;
    private JLabel labelCountFastTank;
    private JLabel labelCountPowerTank;
    private JLabel labelCountArmorTank;
    private JLabel labelTotalCount;

    GameScoreStruct gameScore;

    private boolean basicTankUpdated = false;
    private boolean fastTankUpdated = false;
    private boolean powerTankUpdated = false;
    private boolean armorTankUpdated = false;

    private Timer animationTimer;

    public ScorePanel(JFrame frame, JPanel parentPanel, GameScoreStruct gameScore) {
        super(frame);

        setParentPanel(parentPanel);

        this.gameScore = gameScore;

        this.animationTimer = new Timer(100, e -> { animateScore(); });
        this.animationTimer.setRepeats(true);

        postInitPanel();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        int key = e.getKeyCode();

        if (keyPressDisabled) return;

        switch (key) {
            case KeyEvent.VK_ESCAPE: break;
            case KeyEvent.VK_ENTER: break;
            default: return;
        }

        // TODO: Notifying the parent panel to go back to start new menu

    }

    @Override
    public void keyPressed(KeyEvent e) { }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    protected void initPanel() { }

    protected void postInitPanel() {
        this.setBackground(Globals.COLOR_GRAY);
        this.setOpaque(true);
        this.setBounds(0, 0, Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);
        this.setVisible(false);

        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        int width = frame.getWidth();
        int height = frame.getHeight();

        // Set layout to null for absolute positioning
        setLayout(null);
        setBackground(Color.BLACK);

        // Create main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 0, width, height);
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setOpaque(true);

        // Font for all text
        Font gameFont = tank1990.core.Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, 16);
        Font titleFont = tank1990.core.Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, 18);

        // Calculate center positions
        int centerX = width / 2;
        int startY = 40;

        // HI-SCORE section
        JLabel hiScoreLabel = new JLabel("HI-SCORE");
        hiScoreLabel.setFont(titleFont);
        hiScoreLabel.setForeground(Globals.COLOR_RED);
        hiScoreLabel.setBounds(centerX - 100, startY, 200, 25);
        contentPanel.add(hiScoreLabel);

        JLabel hiScoreValue = new JLabel("20000");
        hiScoreValue.setFont(titleFont);
        hiScoreValue.setForeground(Globals.COLOR_ORANGE);
        hiScoreValue.setBounds(centerX + 100, startY, 200, 25);
        contentPanel.add(hiScoreValue);

        // STAGE section
        JLabel stageLabel = new JLabel("STAGE");
        stageLabel.setFont(gameFont);
        stageLabel.setForeground(Color.WHITE);
        stageLabel.setBounds(centerX - 50, startY + 30, 150, 25);
        contentPanel.add(stageLabel);

        JLabel stageValue = new JLabel(Integer.toString(this.gameScore.getReachedLevel()));
        stageValue.setFont(gameFont);
        stageValue.setForeground(Color.WHITE);
        stageValue.setBounds(centerX + 70, startY + 30, 50, 25);
        contentPanel.add(stageValue);

        // I-PLAYER section
        JLabel playerLabel = new JLabel("I-PLAYER");
        playerLabel.setFont(gameFont);
        playerLabel.setForeground(Globals.COLOR_RED);
        playerLabel.setBounds(centerX - 150, startY + 80, 200, 25);
        contentPanel.add(playerLabel);

        // Player score
        JLabel playerScore = new JLabel(Integer.toString(this.gameScore.getTotalScore()));
        playerScore.setFont(gameFont);
        playerScore.setForeground(Globals.COLOR_ORANGE);
        playerScore.setBounds(centerX - 80, startY + 110, 150, 25);
        contentPanel.add(playerScore);

        // Score entries with tank icons
        int yPos = startY + 150;
        int lineHeight = 64;

        // Basic Tank Row
        labelScoreBasicTank = new JLabel("     PTS");
        labelScoreBasicTank.setFont(gameFont);
        labelScoreBasicTank.setForeground(Color.WHITE);
        labelScoreBasicTank.setBounds(centerX - 150, yPos, 150, 25);
        contentPanel.add(labelScoreBasicTank);

        labelCountBasicTank = new JLabel( " ");
        labelCountBasicTank.setFont(gameFont);
        labelCountBasicTank.setForeground(Color.WHITE);
        labelCountBasicTank.setBounds(centerX, yPos, 50, 25);
        contentPanel.add(labelCountBasicTank);

        // Tank icon for 200 PTS (using text representation)
        JLabel basicTankIcon = new JLabel(Utils.loadPNGIcon(Globals.ICON_BASIC_TANK_PATH, 32, 32));
        basicTankIcon.setFont(gameFont);
        basicTankIcon.setForeground(Color.WHITE);
        basicTankIcon.setBounds(centerX + 50, yPos, 30, 25);
        contentPanel.add(basicTankIcon);

        yPos += lineHeight;

        // Fast Tank Row
        labelScoreFastTank = new JLabel("     PTS");
        labelScoreFastTank.setFont(gameFont);
        labelScoreFastTank.setForeground(Color.WHITE);
        labelScoreFastTank.setBounds(centerX - 150, yPos, 150, 25);
        contentPanel.add(labelScoreFastTank);

        labelCountFastTank = new JLabel(" ");
        labelCountFastTank.setFont(gameFont);
        labelCountFastTank.setForeground(Color.WHITE);
        labelCountFastTank.setBounds(centerX, yPos, 50, 25);
        contentPanel.add(labelCountFastTank);

        JLabel fastTankIcon = new JLabel(Utils.loadPNGIcon(Globals.ICON_FAST_TANK_PATH, 32, 32));
        fastTankIcon.setFont(gameFont);
        fastTankIcon.setForeground(Color.WHITE);
        fastTankIcon.setBounds(centerX + 50, yPos, 30, 25);
        contentPanel.add(fastTankIcon);

        yPos += lineHeight;

        // Power Tank Row
        labelScorePowerTank = new JLabel("     PTS");
        labelScorePowerTank.setFont(gameFont);
        labelScorePowerTank.setForeground(Color.WHITE);
        labelScorePowerTank.setBounds(centerX - 150, yPos, 150, 25);
        contentPanel.add(labelScorePowerTank);

        labelCountPowerTank = new JLabel(" ");
        labelCountPowerTank.setFont(gameFont);
        labelCountPowerTank.setForeground(Color.WHITE);
        labelCountPowerTank.setBounds(centerX, yPos, 50, 25);
        contentPanel.add(labelCountPowerTank);

        JLabel powerTankIcon = new JLabel(Utils.loadPNGIcon(Globals.ICON_POWER_TANK_PATH, 32, 32));
        powerTankIcon.setFont(gameFont);
        powerTankIcon.setForeground(Color.WHITE);
        powerTankIcon.setBounds(centerX + 50, yPos, 30, 25);
        contentPanel.add(powerTankIcon);

        yPos += lineHeight;

        // Armor Tank Row
        labelScoreArmorTank = new JLabel("     PTS");
        labelScoreArmorTank.setFont(gameFont);
        labelScoreArmorTank.setForeground(Color.WHITE);
        labelScoreArmorTank.setBounds(centerX - 150, yPos, 150, 25);
        contentPanel.add(labelScoreArmorTank);

        labelCountArmorTank = new JLabel(" ");
        labelCountArmorTank.setFont(gameFont);
        labelCountArmorTank.setForeground(Color.WHITE);
        labelCountArmorTank.setBounds(centerX, yPos, 50, 25);
        contentPanel.add(labelCountArmorTank);

        JLabel armorTankIcon = new JLabel(Utils.loadPNGIcon(Globals.ICON_ARMOR_TANK_PATH, 32, 32));
        armorTankIcon.setFont(gameFont);
        armorTankIcon.setForeground(Color.WHITE);
        armorTankIcon.setBounds(centerX + 50, yPos, 30, 25);
        contentPanel.add(armorTankIcon);

        yPos += lineHeight + 10;

        // Separator line
        JLabel separatorLine = new JLabel("--------------");
        separatorLine.setFont(gameFont);
        separatorLine.setForeground(Color.WHITE);
        separatorLine.setBounds(centerX - 150, yPos, 250, 25);
        contentPanel.add(separatorLine);

        yPos += lineHeight;

        // TOTAL section
        JLabel totalLabel = new JLabel("TOTAL");
        totalLabel.setFont(gameFont);
        totalLabel.setForeground(Color.WHITE);
        totalLabel.setBounds(centerX - 100, yPos, 100, 25);
        contentPanel.add(totalLabel);

        labelTotalCount = new JLabel("  ");
        labelTotalCount.setFont(gameFont);
        labelTotalCount.setForeground(Color.WHITE);
        labelTotalCount.setBounds(centerX, yPos, 50, 25);
        contentPanel.add(labelTotalCount);

        // Add content panel to this panel
        add(contentPanel);

        revalidate();
        repaint();
    }

    private void animateScore() {

        int basicTankCount = Integer.parseInt(labelCountBasicTank.getText().isBlank()? "0" : labelCountBasicTank.getText());
        int fastTankCount = Integer.parseInt(labelCountFastTank.getText().isBlank()? "0" : labelCountFastTank.getText());
        int powerTankCount = Integer.parseInt(labelCountPowerTank.getText().isBlank()? "0" : labelCountPowerTank.getText());
        int armorTankCount = Integer.parseInt(labelCountArmorTank.getText().isBlank()? "0" : labelCountArmorTank.getText());

        if (!basicTankUpdated) {
            if (this.gameScore.getBasicTankCount() == 0) {
                labelCountBasicTank.setText(String.format("%d", 0));
                labelScoreBasicTank.setText(String.format("%4d PTS", 0));

                basicTankUpdated = true;
            } else if (basicTankCount < this.gameScore.getBasicTankCount()) {
                int scorePerTank = this.gameScore.getBasicTankScore() / this.gameScore.getBasicTankCount();
                String scoreText = labelScoreBasicTank.getText().trim();
                String numericPart = scoreText.replace(" PTS", "").trim();
                int score = Integer.parseInt(numericPart.isBlank() ? "0" : numericPart);

                labelCountBasicTank.setText(String.format("%d", ++basicTankCount));
                labelScoreBasicTank.setText(String.format("%4d PTS", score + scorePerTank));
            } else if (basicTankCount == this.gameScore.getBasicTankCount()){
                labelCountBasicTank.setText(String.format("%d", this.gameScore.getBasicTankCount()));
                labelScoreBasicTank.setText(String.format("%4d PTS", this.gameScore.getBasicTankScore()));

                basicTankUpdated = true;
            }
        }

        if (!fastTankUpdated && basicTankUpdated) {
            if (this.gameScore.getFastTankCount() == 0) {
                labelCountFastTank.setText(String.format("%d", 0));
                labelScoreFastTank.setText(String.format("%4d PTS", 0));

                fastTankUpdated = true;
            } else if (fastTankCount < this.gameScore.getFastTankCount()) {
                int scorePerTank = this.gameScore.getFastTankScore() / this.gameScore.getFastTankCount();
                String scoreText = labelScoreFastTank.getText().trim();
                String numericPart = scoreText.replace(" PTS", "").trim();
                int score = Integer.parseInt(numericPart.isBlank() ? "0" : numericPart);

                labelCountFastTank.setText(String.format("%d", ++fastTankCount));
                labelScoreFastTank.setText(String.format("%4d PTS", score + scorePerTank));
            } else if (fastTankCount == this.gameScore.getFastTankCount()){
                labelCountFastTank.setText(String.format("%d", this.gameScore.getFastTankCount()));
                labelScoreFastTank.setText(String.format("%4d PTS", this.gameScore.getFastTankScore()));

                fastTankUpdated = true;
            }
        }

        if (!powerTankUpdated && fastTankUpdated) {
            if (this.gameScore.getPowerTankCount() == 0) {
                labelCountPowerTank.setText(String.format("%d", 0));
                labelScorePowerTank.setText(String.format("%3d PTS", 0));

                powerTankUpdated = true;
            } else if (powerTankCount < this.gameScore.getPowerTankCount()) {
                int scorePerTank = this.gameScore.getPowerTankScore() / this.gameScore.getPowerTankCount();
                String scoreText = labelScorePowerTank.getText().trim();
                String numericPart = scoreText.replace(" PTS", "").trim();
                int score = Integer.parseInt(numericPart.isBlank() ? "0" : numericPart);

                labelCountPowerTank.setText(String.format("%d", ++powerTankCount));
                labelScorePowerTank.setText(String.format("%4d PTS", score + scorePerTank));
            } else if (powerTankCount == this.gameScore.getPowerTankCount()){
                labelCountPowerTank.setText(String.format("%d", this.gameScore.getPowerTankCount()));
                labelScorePowerTank.setText(String.format("%4d PTS", this.gameScore.getPowerTankScore()));

                powerTankUpdated = true;
            }
        }

        if (!armorTankUpdated && powerTankUpdated) {
            if (this.gameScore.getArmorTankCount() == 0) {
                labelCountArmorTank.setText(String.format("%d", 0));
                labelScoreArmorTank.setText(String.format("%4d PTS", 0));

                armorTankUpdated = true;

                // Animation finished, update total count and stop animation
                updateTotalCount();
                stopAnimation();

            } else if (armorTankCount < this.gameScore.getArmorTankCount()) {
                int scorePerTank = this.gameScore.getArmorTankScore() / this.gameScore.getArmorTankCount();
                String scoreText = labelScoreArmorTank.getText().trim();
                String numericPart = scoreText.replace(" PTS", "").trim();
                int score = Integer.parseInt(numericPart.isBlank() ? "0" : numericPart);

                labelCountArmorTank.setText(String.format("%d", ++armorTankCount));
                labelScoreArmorTank.setText(String.format("%4d PTS", score + scorePerTank));
            } else if (armorTankCount == this.gameScore.getArmorTankCount()){
                labelCountArmorTank.setText(String.format("%d", this.gameScore.getArmorTankCount()));
                labelScoreArmorTank.setText(String.format("%4d PTS", this.gameScore.getArmorTankScore()));

                armorTankUpdated = true;

                // Animation finished, update total count and stop animation
                updateTotalCount();
                stopAnimation();
            }
        }

        revalidate();
        repaint();
    }

    private void stopAnimation() {
        if (this.animationTimer.isRunning()) {
            this.animationTimer.stop();
            isAnimationFinished = true;
        }
        onAnimationFinished();
    }

    private void updateTotalCount() {
        int totalCount = this.gameScore.getBasicTankCount() +
                         this.gameScore.getFastTankCount() +
                         this.gameScore.getPowerTankCount() +
                         this.gameScore.getArmorTankCount();
        labelTotalCount.setText(String.format("%d", totalCount));
    }

    private void onAnimationFinished() {
        // Add a delay before calling onAnimationFinished
        Timer delayTimer = new Timer(3000, e -> {
            GamePanel gamePanel = (GamePanel) getParentPanel();
            gamePanel.onScorePanelAnimationFinished();
        });
        delayTimer.setRepeats(false); // Only execute once
        delayTimer.start();
    }

    public void showPanel() {
        if (this.animationTimer.isRunning()) {
            this.animationTimer.stop();
        }

        this.basicTankUpdated = false;
        this.fastTankUpdated = false;
        this.powerTankUpdated = false;
        this.armorTankUpdated = false;

        this.labelCountBasicTank.setText("0");
        this.labelCountFastTank.setText("0");
        this.labelCountPowerTank.setText("0");
        this.labelCountArmorTank.setText("0");

        this.labelScoreBasicTank.setText(String.format("%4d PTS", 0));
        this.labelScoreFastTank.setText(String.format("%4d PTS", 0));
        this.labelScorePowerTank.setText(String.format("%4d PTS", 0));
        this.labelScoreArmorTank.setText(String.format("%4d PTS", 0));

        this.setVisible(true);

        this.animationTimer.start();
    }
}
