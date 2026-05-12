package tank1990.panels;

import tank1990.core.GameScoreStruct;
import tank1990.core.Globals;
import tank1990.core.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @class GameInfoPanel
 * @brief Displays game information such as current level, player lives, and remaining enemies.
 * @details This panel is used to show the current game state, including the number of remaining enemies,
 * the player's remaining lives, and the current level.
 */
public class GameInfoPanel extends AbstractPanel {
    AbstractPanel parentPanel;

    private JLabel labelCurrentLevel;
    private JLabel labelPlayerRemainingLives;
    private JPanel panelRemainingEnemy;

    private ImageIcon enemyIcon;
    private ImageIcon lifeIcon;
    private ImageIcon stageIcon;

    private final ArrayList<JLabel> labelEnemyTanks = new ArrayList<>();

    private static final int FONT_SIZE = 24;

    public GameInfoPanel(JFrame frame, AbstractPanel parentPanel) {
        super(frame);
        this.parentPanel = parentPanel;

        this.enemyIcon = Utils.loadPNGIcon(Globals.ICON_ENEMY_TANK_PATH, 24, 24);
        this.lifeIcon = Utils.loadPNGIcon(Globals.ICON_PLAYER_LIFE_PATH, 32, 32);
        this.stageIcon = Utils.loadPNGIcon(Globals.ICON_STAGE_PATH, 64, 64);

        postInitPanel();
    }

    @Override
    protected void initPanel() {
        setBackground(Globals.COLOR_GRAY);
        setLayout(null); // Use absolute positioning
    }

    protected void postInitPanel() {
        int panelWidth = Globals.WINDOW_WIDTH / 4;
        int panelHeight = Globals.WINDOW_HEIGHT; // Total available height

        int enemySectionHeight = panelHeight / 2;      // 1/2 of total height
        int playerSectionHeight = panelHeight / 4;     // 1/4 of total height
        int stageSectionHeight = panelHeight / 4;      // 1/4 of total height

        // Enemy Count Section
        JPanel enemySection = createEnemyCountSection();
        enemySection.setBounds(10, 10, panelWidth - 20, enemySectionHeight - 20);
        add(enemySection);

        // Player Section
        JPanel playerSection = createPlayerSection();
        playerSection.setBounds(10, enemySectionHeight, panelWidth - 20, playerSectionHeight - 10);
        add(playerSection);

        // Stage Section
        JPanel stageSection = createStageSection();
        stageSection.setBounds(10, enemySectionHeight + playerSectionHeight, panelWidth - 20, stageSectionHeight - 10);
        add(stageSection);
    }

    private JPanel createEnemyCountSection() {
        this.panelRemainingEnemy = new JPanel();
        this.panelRemainingEnemy.setLayout(new GridBagLayout());
        this.panelRemainingEnemy.setBackground(Globals.COLOR_GRAY);
        updateRemainingEnemies(0); // Initialize with no enemies

        return panelRemainingEnemy;
    }

    private JPanel createPlayerSection() {
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        playerPanel.setBackground(Globals.COLOR_GRAY);
        playerPanel.setOpaque(true);

        // Player label
        JLabel label = new JLabel("IP");
        label.setForeground(Color.BLACK);
        label.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, FONT_SIZE));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel iconAndLivesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        iconAndLivesPanel.setOpaque(false);

        // Player icon
        JLabel labelPlayerIcon = new JLabel(lifeIcon);

        // Lives count
        this.labelPlayerRemainingLives = new JLabel(String.valueOf(0));
        this.labelPlayerRemainingLives.setForeground(Color.BLACK);
        this.labelPlayerRemainingLives.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, FONT_SIZE));

        // Add icon and lives to horizontal panel
        iconAndLivesPanel.add(labelPlayerIcon);
        iconAndLivesPanel.add(this.labelPlayerRemainingLives);

        // Add components
        playerPanel.add(label);
        playerPanel.add(Box.createVerticalStrut(5));
        playerPanel.add(iconAndLivesPanel);

        return playerPanel;
    }

    private JPanel createStageSection() {
        JPanel stagePanel = new JPanel();
        stagePanel.setLayout(new BoxLayout(stagePanel, BoxLayout.Y_AXIS));
        stagePanel.setBackground(Globals.COLOR_GRAY);
        stagePanel.setOpaque(true);

        // Stage icon
        JLabel labelStageIcon = new JLabel(stageIcon);
        labelStageIcon.setPreferredSize(new Dimension(64, 64));
        labelStageIcon.setMaximumSize(new Dimension(64, 64));
        labelStageIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stage label
        this.labelCurrentLevel = new JLabel(String.valueOf(0));  // Set current level to 0 initially
        this.labelCurrentLevel.setForeground(Color.BLACK);
        this.labelCurrentLevel.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, FONT_SIZE));
        this.labelCurrentLevel.setAlignmentX(Component.CENTER_ALIGNMENT);

        stagePanel.add(labelStageIcon);
        stagePanel.add(Box.createVerticalStrut(5));
        stagePanel.add(labelCurrentLevel);

        return stagePanel;
    }

    public void updateRemainingEnemies(int remainingEnemies) {
        // Clear the panel
        this.panelRemainingEnemy.removeAll();
        this.labelEnemyTanks.clear();

        // Add enemy icons based on remaining count
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        for (int i = 0; i < remainingEnemies; i++) {
            JLabel label = new JLabel(enemyIcon, SwingConstants.CENTER);

            gbc.gridx = i % 2; // There should be 2 columns
            gbc.gridy = i / 2; // Assign row based on index

            this.labelEnemyTanks.add(label);
            this.panelRemainingEnemy.add(label, gbc);
        }

        this.panelRemainingEnemy.revalidate();
        this.panelRemainingEnemy.repaint();
    }

    public void update(GameScoreStruct gameScore) {

        int remainingEnemies = gameScore.getRemainingTankCount();
        this.labelCurrentLevel.setText(Integer.toString(gameScore.getReachedLevel()));
        this.labelPlayerRemainingLives.setText(Integer.toString(Math.max(gameScore.getPlayerRemainingLives(), 0)));

        updateRemainingEnemies(remainingEnemies);

        this.revalidate();
        this.repaint();
    }
}
