package tank1990.panels;

import java.awt.Graphics;

import javax.swing.JLayeredPane;

import tank1990.core.GameEngine;

/**
 * @class GameAreaPanel
 * @brief Represents the main game area where the game is rendered.
 * @details This panel is responsible for displaying the game engine's graphics and handling the game state.
 */
public class GameAreaPanel extends JLayeredPane {
    private GameEngine gameEngine = null;

    GameAreaPanel() { }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        this.gameEngine.setParentPanel(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (this.gameEngine!=null) this.gameEngine.paintComponent(g);
    }
}

