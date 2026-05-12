package tank1990.panels;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.*;
import java.awt.*;

/**
 * @class AbstractPanel
 * @brief Base class for all panels in the application.
 * @details This class provides common functionality for panels, such as initialization and resetting.
 */
public abstract class AbstractPanel extends JPanel {
    protected JFrame frame;

    protected JPanel parentPanel;

    public AbstractPanel(JFrame frame) {
        this.frame = frame;

        initPanel();
    }

    public void setParentPanel(JPanel parentPanel) { this.parentPanel = parentPanel; }

    public JPanel getParentPanel() { return this.parentPanel; }

    protected abstract void initPanel();

    protected void resetPanel() {
        this.frame.getContentPane().removeAll();
        this.removeAll();
        this.setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.requestFocusInWindow();

        // Add mouse listeners for dragging
        addMouseListener(new MouseAdapter() {});
        addMouseMotionListener(new MouseMotionAdapter() {});

        initPanel();

        postInitPanel();

        this.frame.add(this);

        // Revalidate and repaint the panel
        revalidate();
        repaint();
    }

    protected void postInitPanel() {}
}
