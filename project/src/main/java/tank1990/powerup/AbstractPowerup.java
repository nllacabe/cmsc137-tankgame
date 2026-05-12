package tank1990.powerup;

import java.awt.Dimension;
import java.awt.Graphics;

import tank1990.core.*;

/**
 * @class AbstractPowerup
 * @brief Represents a powerup in the game.
 * @details This class is an abstract base class for all powerups, providing common
 * functionality such as lifetime management and blinking effects.
 */
public abstract class AbstractPowerup extends DynamicGameObject {
    protected PowerupType powerupType;
    protected int lifeTimeMs;
    protected transient TextureFX textureFX;
    protected boolean isActive = true; // Flag to indicate if the powerup is active

    private TimeTick lifeTimeTick; // Tick for managing lifetime

    private TimeTick blinkTick; // Tick for blinking effect

    private boolean isVisible = true;

    protected int points = 500;

    AbstractPowerup(int x, int y, PowerupType powerupType, int lifeTimeMs) {
        setX(x);
        setY(y);
        setSize(new Dimension(16, 16)); // Assuming a default size for powerups
        this.powerupType = powerupType;
        this.lifeTimeMs = lifeTimeMs;

        lifeTimeTick = new TimeTick(Utils.Time2GameTick(this.lifeTimeMs)); // Tick for managing lifetime
        lifeTimeTick.setRepeats(0);  // Do not repeat
        blinkTick = new TimeTick(Utils.Time2GameTick(Globals.POWERUP_BLINK_INTERVAL_MS));
        blinkTick.setRepeats(-1); // Repeat indefinitely
    }

    public void draw(Graphics g) {
        if (blinkTick.isTimeOut()) {
            isVisible = !isVisible;  // Toggle visibility
            blinkTick.reset();
        }

        setSize(Utils.normalizeDimension(g, Globals.POWERUP_WIDTH, Globals.POWERUP_HEIGHT));

        if (!isVisible) {
            this.textureFX.draw(g, getX(), getY(), 0);
        }
    }

    public void update() {
        // Update lifetime of the powerup
        lifeTimeTick.updateTick();
        blinkTick.updateTick();

        if (lifeTimeTick.isTimeOut()) {
            System.out.println("Powerup expired: " + powerupType);
            setActive(false);
        }


    }

    /**
     * Checks if the powerup is expired based on its lifetime and active status.
     * @return true if the powerup is expired, false otherwise.
     */
    public boolean isExpired() {
        return !isActive || lifeTimeTick.isTimeOut();
    }

    /**
     * Sets the active status of the powerup.
     * @param active true to activate the powerup, false to deactivate it.
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }

    /**
     * Gets the type of the powerup.
     * @return the PowerupType of this powerup.
     */
    public PowerupType getPowerupType() {
        return this.powerupType;
    }

    /**
     * Gets the points to be earned from this powerup.
     * @return the points
     */
    public int getPoints() {
        return this.points;
    }

    /**
     * Sets the points for this powerup.
     * @param points the points to set
     */
    protected void setPoints(int points) {
        this.points = points;
    }
}
