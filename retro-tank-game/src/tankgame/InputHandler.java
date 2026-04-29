package tankgame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

// tracks which keys are currently held down.
// supports simultaneous key presses for smooth movement + shooting.
public class InputHandler implements KeyListener {

    private final Set<Integer> pressedKeys = new HashSet<Integer>();

    // true while the given key is held down.
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    // player keyboard controls
    public boolean up() {
        return isKeyPressed(KeyEvent.VK_W);
    }

    public boolean down() {
        return isKeyPressed(KeyEvent.VK_S);
    }

    public boolean left() {
        return isKeyPressed(KeyEvent.VK_A);
    }

    public boolean right() {
        return isKeyPressed(KeyEvent.VK_D);
    }

    public boolean shoot() {
        return isKeyPressed(KeyEvent.VK_SPACE);
    }

    public boolean enter() {
        return isKeyPressed(KeyEvent.VK_ENTER);
    }

    public boolean esc() {
        return isKeyPressed(KeyEvent.VK_ESCAPE);
    }

    // KeyListener

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
