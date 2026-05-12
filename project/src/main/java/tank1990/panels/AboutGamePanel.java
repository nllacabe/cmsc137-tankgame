package tank1990.panels;

import tank1990.core.Globals;
import tank1990.core.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @class AboutGamePanel
 * @brief A panel that displays information about the game, including how to play and credits.
 * @details This panel is shown when the user selects the "About" option from the main menu.
 */
public class AboutGamePanel extends SlidingPanel implements KeyListener {

    private static final int TITLE_FONT_SIZE = 32;
    private static final int SECTION_TITLE_FONT_SIZE = 24;
    private static final int SUB_TITLE_FONT_SIZE = 16;
    private static final int FONT_SIZE = 12;

    private Timer blinkTimer = null;
    private JLabel pressButton;
    private boolean colorChanged = false;

    public AboutGamePanel(JFrame frame, AbstractPanel parentPanel) {
        super(frame);
        this.parentPanel = parentPanel;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // If animation is not finished yet, finish it before proceeding
            if (!isAnimationFinished) {
                finishAnimation();
                return;
            }

            // Reset current panel
            resetPanel();

            // Stop the blink timer
            this.blinkTimer.stop();

            // Reset menu panel
            SwingUtilities.invokeLater(() -> {
                if (getParentPanel() != null) {
                    MenuPanel menuPanel = (MenuPanel) getParentPanel();
                    menuPanel.resetPanel();
                    this.frame.revalidate();
                    this.frame.repaint();
                }
            });
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    protected void animationStarted() {

    }

    @Override
    protected void animationFinished() {
        if (this.blinkTimer!=null && this.blinkTimer.isRunning()) {
            this.blinkTimer.stop();
        }

        this.blinkTimer = new Timer(500, e -> {
            if (this.pressButton == null) return;

            colorChanged = !colorChanged;
            if (colorChanged) {
                this.pressButton.setForeground(Color.WHITE);

            } else {
                this.pressButton.setForeground(Color.CYAN);
            }
            this.frame.revalidate();
            this.frame.repaint();
        });

        this.blinkTimer.start();
    }

    @Override
    protected void initPanel() {
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        int width = frame.getWidth();
        int height = frame.getHeight();

        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setBounds(0, 0, width, height);
        backgroundPanel.setBackground(Color.BLACK);
        backgroundPanel.setOpaque(true);

        // Base Panel
        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.setBounds(0, height, width, height);  // Create at the bottom so the sliding animation takes its place
        basePanel.setOpaque(true);
        basePanel.setBackground(Color.BLACK);

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setPreferredSize(new Dimension(width, 40));
        titlePanel.setBackground(Color.BLACK);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        JLabel titleLabel = new JLabel("TANK 1990", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, TITLE_FONT_SIZE));
        titlePanel.add(titleLabel);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        // Top Section - How to Play
        JPanel howToPlaySection = createHowToPlaySection();
        howToPlaySection.setPreferredSize(new Dimension(width, 400));
        contentPanel.add(howToPlaySection, BorderLayout.CENTER);

        // Center Section - Credits
        JPanel creditsSection = createCreditsSection();
        creditsSection.setPreferredSize(new Dimension(width, 240));
        contentPanel.add(creditsSection, BorderLayout.SOUTH);

        // Add all sections
        basePanel.add(titlePanel, BorderLayout.NORTH);
        basePanel.add(contentPanel, BorderLayout.CENTER);

        revalidate();
        repaint();

        frame.add(basePanel);
        frame.add(backgroundPanel);

        // Apply sliding animation for base panel
        startAnimation(basePanel, 5);
    }

    private JPanel createHowToPlaySection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.BLACK);

        // Section title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(Color.BLACK);
        JLabel sectionTitle = new JLabel("HOW TO PLAY");
        sectionTitle.setForeground(Color.CYAN);
        sectionTitle.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, SECTION_TITLE_FONT_SIZE));
        titlePanel.add(sectionTitle);
        panel.add(titlePanel);

        // Controls section
        addSectionTitle(panel, "CONTROLS:", Color.YELLOW);
        panel.add(Box.createVerticalStrut(5));
        addBulletText(panel, "ARROW KEYS", "Move Tank");
        panel.add(Box.createVerticalStrut(5));
        addBulletText(panel, "Z", "Fire Bullet");
        panel.add(Box.createVerticalStrut(5));
        addBulletText(panel, "ESC", "Pause Game");

        // Objective section
        addSectionTitle(panel, "OBJECTIVE:", Color.YELLOW);
        panel.add(Box.createVerticalStrut(5));
        addTextLine(panel, "• Destroy all enemy tanks", Color.WHITE);
        panel.add(Box.createVerticalStrut(5));
        addTextLine(panel, "• Protect your base", Color.WHITE);
        panel.add(Box.createVerticalStrut(5));
        addTextLine(panel, "• Collect power-ups", Color.WHITE);

        panel.add(Box.createVerticalStrut(10));

        // Power-ups section
        addSectionTitle(panel, "POWER-UPS:", Color.YELLOW);
        panel.add(Box.createVerticalStrut(5));
        addTextLine(panel, "Star\t- Upgrade weapon", Color.WHITE);
        panel.add(Box.createVerticalStrut(5));
        addTextLine(panel, "Grenade\t- Destroy all enemies", Color.WHITE);
        panel.add(Box.createVerticalStrut(5));
        addTextLine(panel, "Helmet\t- Temporary shield", Color.WHITE);
        panel.add(Box.createVerticalStrut(5));
        addTextLine(panel, "Timer\t- Freeze enemies", Color.WHITE);
        panel.add(Box.createVerticalStrut(5));
        addTextLine(panel, "Shovel\t- Protect base", Color.WHITE);
        panel.add(Box.createVerticalStrut(5));
        addTextLine(panel, "Tank\t- Extra life", Color.WHITE);

        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    private JPanel createCreditsSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.BLACK);

        // Section title
        JLabel sectionTitle = new JLabel("CREDITS");
        sectionTitle.setForeground(Color.CYAN);
        sectionTitle.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, SECTION_TITLE_FONT_SIZE));
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sectionTitle.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(sectionTitle);
        panel.add(Box.createVerticalStrut(20));

        // Game info - centered
        JLabel gameDesc = new JLabel("Based on the classic Battle City (1985) by Viriliter", SwingConstants.CENTER);
        gameDesc.setForeground(Color.WHITE);
        gameDesc.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, SUB_TITLE_FONT_SIZE));
        gameDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(gameDesc);

        panel.add(Box.createVerticalStrut(5));

        JLabel projectDesc = new JLabel("This project is created for BIL015 Course Project", SwingConstants.CENTER);
        projectDesc.setForeground(Color.WHITE);
        projectDesc.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, SUB_TITLE_FONT_SIZE));
        projectDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(projectDesc);

        panel.add(Box.createVerticalStrut(15));

        JLabel emptyField = new JLabel(" ", SwingConstants.CENTER);
        emptyField.setForeground(Color.WHITE);
        emptyField.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, SUB_TITLE_FONT_SIZE));
        emptyField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(emptyField);

        panel.add(Box.createVerticalStrut(15));

        this.pressButton = new JLabel("PRESS ENTER TO CONTINUE", SwingConstants.CENTER);
        this.pressButton.setForeground(Color.WHITE);
        this.pressButton.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, SUB_TITLE_FONT_SIZE));
        this.pressButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(this.pressButton);

        panel.add(Box.createVerticalStrut(20));

        // Version info
        JLabel versionLabel = new JLabel(String.format("Tank 1990 v%d.%d © 2025", Globals.MAJOR_VERSION, Globals.MINOR_VERSION), SwingConstants.CENTER);
        versionLabel.setForeground(Color.GRAY);
        versionLabel.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, SUB_TITLE_FONT_SIZE));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(versionLabel);

        return panel;
    }

    private void addSectionTitle(JPanel panel, String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, SUB_TITLE_FONT_SIZE));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
    }

    private void addBulletText(JPanel panel, String key, String description) {
        JPanel instructionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        instructionPanel.setBackground(Color.BLACK);

        JLabel keyLabel = new JLabel(String.format("%-12s", key));
        keyLabel.setForeground(Color.ORANGE);
        keyLabel.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.BOLD, SUB_TITLE_FONT_SIZE));

        JLabel actionLabel = new JLabel("- " + description);
        actionLabel.setForeground(Color.WHITE);
        actionLabel.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, SUB_TITLE_FONT_SIZE));

        instructionPanel.add(keyLabel);
        instructionPanel.add(actionLabel);
        instructionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(instructionPanel);
    }

    private void addTextLine(JPanel panel, String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(Utils.loadFont(Globals.FONT_PRESS_START_2P, Font.PLAIN, SUB_TITLE_FONT_SIZE));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
    }
}
