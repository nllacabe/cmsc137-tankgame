package tank1990;

import javax.swing.*;
import java.awt.*;

import tank1990.core.ConfigHandler;
import tank1990.core.Globals;
import tank1990.panels.MenuPanel;

/**
 * @class Game
 * @brief Entry point — simplified: single player only, no GameMode selection.
 */
public class Game {
    public static int iPlayerScore = 0;

    public static void main(String[] args) {
        ConfigHandler.getInstance().parse(Globals.CONFIGURATION_FILE);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(Globals.GAME_TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            if (Globals.WINDOW_WIDTH == 0 || Globals.WINDOW_HEIGHT == 0) {
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                frame.setSize(screen.width, screen.height);
            } else {
                frame.getContentPane().setPreferredSize(new Dimension(Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT));
                frame.pack();
            }

            // Lock window size — prevent any resize after initial pack()
            Dimension lockedSize = frame.getSize();
            frame.setMinimumSize(lockedSize);
            frame.setMaximumSize(lockedSize);
            frame.setPreferredSize(lockedSize);

            MenuPanel menuPanel = new MenuPanel(frame);
            frame.add(menuPanel);
            frame.setVisible(true);
        });
    }
}