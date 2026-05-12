
package tank1990.panels;

import javax.swing.*;
import java.awt.*;

/** * @class SlidingPanel
 * @brief Abstract class for panels that can slide in and out of view.
 * @details This class provides methods to handle sliding animations for panels in a JFrame.
 */
public abstract class SlidingPanel extends AbstractPanel{
    protected boolean isAnimationFinished = false; // Flag to check if animation is finished
    private JPanel panel; // The panel to animate

    public SlidingPanel(JFrame frame) {
        super(frame);
    }

    protected void startAnimation(JPanel panel, int delay) {
        this.panel = panel;
        int finalY = 0; // Target position (top)
        int step = 5; // Pixels to move per tick

        Timer timer = new Timer(delay, null);
        timer.addActionListener(e -> {
            Point current = this.panel.getLocation();
            int newY = current.y - step;

            if (newY <= finalY) {
                this.panel.setLocation(0, finalY);
                timer.stop();
                isAnimationFinished = true;
                animationFinished();
            } else {
                this.panel.setLocation(0, newY);
            }

            this.panel.repaint();
        });

        timer.start();
        animationStarted();
    }

    /**
     * Finishes the animation forcefully by setting the panel's location to the final position.
     */
    protected void finishAnimation() {
        if (this.panel==null) {
            System.err.println("Panel is null, the animation might not be started yet.");
            return;
        }

        int finalY = 0; // Target position (top)
        this.panel.setLocation(0, finalY);
        isAnimationFinished = true;
        animationFinished();
        this.panel.repaint();
    }

    protected abstract void animationStarted();

    protected abstract void animationFinished();

    protected void resetPanel() {
        isAnimationFinished = false;

        super.resetPanel();


    }

}
