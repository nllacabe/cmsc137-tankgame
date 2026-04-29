package tankgame;

import javax.swing.*;

// main
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame(Constants.GAME_TITLE);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false);

                GamePanel gamePanel = new GamePanel();
                frame.add(gamePanel);
                frame.pack();
                frame.setLocationRelativeTo(null); // center on screen
                frame.setVisible(true);

                gamePanel.requestFocusInWindow();
            }
        });
    }
}
