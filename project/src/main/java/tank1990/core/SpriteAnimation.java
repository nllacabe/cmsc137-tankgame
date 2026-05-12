/*
 * Copyright (c) 2025.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tank1990.core;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * @class SpriteAnimation
 * @brief Class to handle sprite-based animation using a sprite sheet.
 * @details This class loads a sprite sheet, preloads individual frames from it, and
 * manages frame updates, animation speed, repeat count, and drawing the 
 * animation with rotation.
 * The animation frames are drawn sequentially with a specified delay between 
 * each frame.
 *  
 * @see SpriteAnimationStruct
 */
public class SpriteAnimation implements Serializable{
    private SpriteAnimationStruct struct = null;        /**< Structure holding the metadata of the sprite animation. */
    private transient BufferedImage spriteSheet = null; /**<The sprite sheet image containing the frames. */
    private transient BufferedImage[] subFrames = null; /**< Preloaded animation frames. */
    private int frameWidth = 0, frameHeight = 0;        /**< Size of each frame in sprite sheet. */
    private int totalFrames;                            /**< Total number of frames in the sprite sheet. */
    private int currentFrame = 0;                       /**< Current animation frame. */
    private int frameDelay;                             /**< Delay before switching frames. */
    private int frameCounter = 0;                       /**< Counter to control animation speed. */
    private int rows;                                   /**< Number of columns in sprite sheet. */
    private int columns;                                /**< Number of rows in sprite sheet. */
    private int repeatCount = -1;                       /**< Animation repeat count. */
    private int targetWidth = 0, targetHeight = 0;      /**< Size of target frame which will be drawn. */

    private int offsetX;                                /**< The offset for the sprite's X position. */
    private int offsetY;                                /**< The offset for the sprite's Y position. */
    private int defaultDelay = 0;                       /**< The default delay between each frame in game ticks. */
    private int delay = 0;                              /**< The delay between each frame in game ticks. */

    private double rOffset = 0;                         /**< Rotation offset in radians. */

    /**
     * @class Offset
     * @brief Helper class for storing the X and Y offsets for the sprite.
     */
    public class Offset {
        int x;
        int y;

        /**
         * Constructor for Offset.
         * 
         * @param x The X offset.
         * @param y The Y offset.
         */
        public Offset(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * @return The X offset.
         */
        public int getX() {
             return this.x;
        }

        /**
         * @return The Y offset.
         */
        public int getY() {
            return this.y;
       }
    }

    /**
     * Constructor that initializes the sprite animation from a given struct.
     * 
     * @param struct The metadata struct that defines the sprite sheet properties.
     * @throws IOException If there's an error loading the sprite sheet image.
     */
    public SpriteAnimation(SpriteAnimationStruct struct) {
        try {
            this.struct = struct;
            this.spriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(this.struct.imagePath)));
            this.totalFrames = this.struct.totalFrames;
            this.frameDelay = this.struct.frameDelay;
            this.rows = this.struct.rows;
            this.columns = this.struct.columns;
            this.offsetX = this.struct.xOffset;
            this.offsetY = this.struct.yOffset;
            this.defaultDelay = this.struct.defaultDelay;
            this.delay = this.struct.defaultDelay;
            this.rOffset = this.struct.rOffset;

            setSubFrames();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Preloads all frames from the sprite sheet into the subFrames array.
     * 
     * Perform this operation on constructor so that no need to perform this 
     * operation over and over again since it is a bit time-consuming.
     */
    public void setSubFrames() {
        this.frameWidth = this.spriteSheet.getWidth() / this.columns;
        this.frameHeight = this.spriteSheet.getHeight() / this.rows;
        
        // Preload all frames
        this.subFrames = new BufferedImage[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            int row = i / columns;
            int col = i % columns;
            int frameX = col * frameWidth;
            int frameY = row * frameHeight;

            // Extract and store frame
            this.subFrames[i] = spriteSheet.getSubimage(frameX, frameY, frameWidth, frameHeight);
        }
    }

    /**
     * Sets the number of times the animation should repeat.
     * 
     * @param repeatCount The number of repetitions (set to -1 for infinite repeats).
     */
    public void setRepeat(int repeatCount) {
        this.repeatCount = repeatCount;
    }
    
    /**
     * Sets the size of the target frame to be drawn.
     * 
     * @param width The width of the target frame.
     * @param height The height of the target frame.
     */
    public void setTargetSize(int width, int height) {
        this.targetWidth = width;
        this.targetHeight = height;
    }

    /**
     * Sets the rotation offset to be applied to the sprite.
     * 
     * @param rOffset The rotation offset in radians.
     */
    public void setRotationOffset(double rOffset) {
        this.rOffset = rOffset;
    }

    /**
     * Gets the offset values for the sprite.
     * 
     * @return An Offset object containing the X and Y offsets.
     */
    public Offset getOffset() {
        return new Offset(this.offsetX, this.offsetY);
    }

    /**
     * Updates the animation by advancing to the next frame or repeating the animation.
     * 
     * @return True if the animation should continue; false if it has finished.
     */
    public boolean update() {
        if (this.delay>0) this.delay--;

        this.frameCounter++;

        if (this.frameCounter >= this.frameDelay) {
            this.frameCounter = 0;  // Reset counter after update
    
            if (this.repeatCount < 0) {  // Infinite loop
                this.currentFrame = (this.currentFrame + 1) % this.totalFrames;
            } else if (this.repeatCount > 0) {
                if (this.currentFrame < this.totalFrames - 1) {
                    this.currentFrame++;
                } else {
                    this.repeatCount--;  // Decrement after last repeat
                    if (this.repeatCount >= 0) {
                        this.currentFrame = 0; // Reset to first animation
                        this.delay = this.defaultDelay;
                        return false;
                    }
                }
            } else {
                // Do nothing if no repeat left
            }
        }
        return true;
    }

    /**
     * Draws the current frame of the animation to the screen with rotation.
     * 
     * @param g The Graphics object to draw on.
     * @param x The X position of the sprite.
     * @param y The Y position of the sprite.
     * @param rotation The rotation angle (in radians).
     */
    public void draw(Graphics g, int x, int y, double rotation) {
        if (this.repeatCount == 0) return;  // If there is no repeat for the animation do not draw
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        // Apply transformations (rotation + centering)
        g2d.translate(x, y);
        g2d.rotate(rotation+rOffset);

        if (this.subFrames != null) {
            g2d.drawImage(this.subFrames[this.currentFrame], -this.targetWidth / 2, -this.targetHeight / 2, this.targetWidth, this.targetHeight, null);
        }

        g2d.setTransform(oldTransform);
    }

    /**
     * Alternative method for drawing the current frame with a different transformation.
     * 
     * @param g The Graphics object to draw on.
     * @param x The X position of the sprite.
     * @param y The Y position of the sprite.
     * @param rotation The rotation angle (in radians).
     */
    public void draw2(Graphics g, int x, int y, double rotation) {
        if (this.repeatCount == 0) return;  // If there is no repeat for the animation do not draw

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        // Apply transformations (rotation + centering)
        g2d.translate(x, y);
        g2d.rotate(rotation+this.rOffset);

        if (this.subFrames != null) {
            g2d.drawImage(this.subFrames[this.currentFrame], -this.targetWidth / 2, -this.targetHeight / 2, this.targetWidth, this.targetHeight, null);
        }

        g2d.setTransform(oldTransform);
    }

    /**
     * Custom deserialization method to reload the sprite sheet and frames after deserialization.
     * 
     * @param in The ObjectInputStream used for deserialization.
     * @throws IOException If an error occurs during deserialization.
     * @throws ClassNotFoundException If the class of the serialized object is not found.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.spriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(this.struct.imagePath)));
        setSubFrames();
    }
}
