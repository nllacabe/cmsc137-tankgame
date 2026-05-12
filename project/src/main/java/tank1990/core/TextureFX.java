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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;

import javax.imageio.ImageIO;

/**
 * @class TextureFX
 * @brief A class representing a texture effect for drawing images.
 * @details This class handles the loading, updating, and drawing of a texture (image)
 * The texture is loaded from a path specified in a `TextureFXStruct` object passed during
 * initialization.
 */
public class TextureFX implements Serializable{
    private transient BufferedImage texture = null; /**< The BufferedImage representing the texture to be rendered. */
    private TextureFXStruct struct = null;          /**< The structure holding metadata and configuration for the texture. */
    private int targetWidth;                        /**< The target width for the texture when drawn. */
    private int targetHeight;                       /**< The target height for the texture when drawn. */
    private int offsetX = 0;                        /**< The X offset to apply to the texture's position. */
    private int offsetY = 0;                        /**< The Y offset to apply to the texture's position. */
    private int defaultDelay = 0;                   /**< The default delay (in frames) for the texture effect before it is drawn again. */
    private int delay = 0;                          /**< The current delay (in frames) for the texture effect before it is drawn again. */

    /**
     * Constructs a TextureFX object using the given texture metadata structure.
     * The texture is loaded from the path specified in the struct object.
     *
     * @param struct The structure containing metadata about the texture.
     */
    public TextureFX (TextureFXStruct struct) {
        try {
            this.struct = struct;
            this.texture = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(struct.path)));
            if (this.texture != null) {
                this.targetWidth = this.texture.getWidth();
                this.targetHeight = this.texture.getHeight();
                this.offsetX = struct.offsetX;
                this.offsetY = struct.offsetY;
                this.defaultDelay = struct.defaultDelay;
                this.delay = this.defaultDelay;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the target width and height for the texture when drawn.
     *
     * @param width The new width for the texture.
     * @param height The new height for the texture.
     */
    public void setTargetSize(int width, int height) {
        this.targetWidth = width;
        this.targetHeight = height;
    }

    public Dimension getOffsets() {
        return new Dimension(this.offsetX, this.offsetY);
    }

    /**
     * Updates the delay for the texture effect, decrementing it if it is greater than 0.
     * This is typically called every frame to manage the animation delay.
     */
    public void update() {
        this.delay = this.delay> 0 ? this.delay-1 : 0;
    }

    /**
     * Draws the texture on the provided Graphics context at the specified position
     * and applies the specified rotation.
     *
     * @param g The Graphics context on which to draw the texture.
     * @param x The X coordinate for the texture's position.
     * @param y The Y coordinate for the texture's position.
     * @param rotation The rotation angle (in radians) to apply to the texture.
     */
    public void draw(Graphics g, int x, int y, double rotation) {
        if (this.delay>0) return;

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        // Apply transformations (rotation + centering)
        g2d.translate(x, y);
        g2d.rotate(rotation);
        //g2d.translate(this.offsetX, this.offsetY);

        if (this.texture!=null) g2d.drawImage(this.texture, -this.targetWidth / 2, -this.targetHeight / 2, this.targetWidth, this.targetHeight, null);

        g2d.setTransform(oldTransform);

    }

    /**
     * Custom deserialization logic to ensure the texture is reloaded after deserialization.
     *
     * @param in The input stream to read from during deserialization.
     * @throws IOException If an I/O error occurs while reading the texture.
     * @throws ClassNotFoundException If a class cannot be found during deserialization.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.texture = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(this.struct.path)));
    }
}
