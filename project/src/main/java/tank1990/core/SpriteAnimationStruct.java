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

import java.io.Serializable;

/**
 * @class SpriteAnimationStruct
 * @brief A structure that holds metadata for sprite-based animation.
 * @details This class contains information about the sprite sheet, including the image path,
 * number of frames, frame delay, number of rows and columns in the sprite sheet,
 * offsets for positioning, rotation offset, and the default delay for animations.
 * It is used to initialize the `SpriteAnimation` class for creating and controlling
 * sprite animations.
 * 
 */
public class SpriteAnimationStruct implements Serializable {
    String imagePath;   /**< The path to the sprite sheet image. */
    int totalFrames;    /**< The total number of frames in the sprite sheet. */
    int frameDelay;     /**< The delay between frames in the animation (in frames). */
    int rows;           /**< The number of rows in the sprite sheet. */
    int columns;        /**< The number of columns in the sprite sheet. */
    int xOffset;        /**< The X offset to apply to the sprite. */
    int yOffset;        /**< The Y offset to apply to the sprite. */
    double rOffset;     /**< The rotation offset (in radians) to apply to the sprite. */
    int defaultDelay;   /**< The default delay for the animation (in frames). */

    /**
     * Constructor to initialize the sprite animation metadata.
     * 
     * @param imagePath The path to the sprite sheet image.
     * @param totalFrames The total number of frames in the sprite sheet.
     * @param frameDelay The delay between frames in the animation.
     * @param rows The number of rows in the sprite sheet.
     * @param columns The number of columns in the sprite sheet.
     * @param xOffset The X offset to apply to the sprite.
     * @param yOffset The Y offset to apply to the sprite.
     * @param defaultDelay The default delay between frames in the animation.
     */
    public SpriteAnimationStruct(
        String imagePath, int totalFrames, int frameDelay, 
        int rows, int columns, int xOffset, int yOffset, int defaultDelay
    ) {
        this.imagePath = imagePath;
        this.totalFrames = totalFrames;
        this.frameDelay = frameDelay;
        this.rows = rows;
        this.columns = columns;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.rOffset = 0;
        this.defaultDelay = defaultDelay;
    }
    
    /**
     * Constructor to initialize the sprite animation metadata without rotation offset
     * and default delay.
     * 
     * @param imagePath The path to the sprite sheet image.
     * @param totalFrames The total number of frames in the sprite sheet.
     * @param frameDelay The delay between frames in the animation.
     * @param rows The number of rows in the sprite sheet.
     * @param columns The number of columns in the sprite sheet.
     * @param xOffset The X offset to apply to the sprite.
     * @param yOffset The Y offset to apply to the sprite.
     */
    public SpriteAnimationStruct(
        String imagePath, int totalFrames, int frameDelay, 
        int rows, int columns, int xOffset, int yOffset
    ) {
        this.imagePath = imagePath;
        this.totalFrames = totalFrames;
        this.frameDelay = frameDelay;
        this.rows = rows;
        this.columns = columns;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.rOffset = 0;
        this.defaultDelay = 0;
    }
    
    /**
     * Constructor to initialize the sprite animation metadata with rotation offset
     * and default delay.
     * 
     * @param imagePath The path to the sprite sheet image.
     * @param totalFrames The total number of frames in the sprite sheet.
     * @param frameDelay The delay between frames in the animation.
     * @param rows The number of rows in the sprite sheet.
     * @param columns The number of columns in the sprite sheet.
     * @param xOffset The X offset to apply to the sprite.
     * @param yOffset The Y offset to apply to the sprite.
     * @param defaultDelay The default delay between frames in the animation.
     * @param rOffset The rotation offset in degrees to apply to the sprite.
     */
    public SpriteAnimationStruct(
        String imagePath, int totalFrames, int frameDelay, 
        int rows, int columns, int xOffset, int yOffset, int defaultDelay, int rOffset
    ) {
        this.imagePath = imagePath;
        this.totalFrames = totalFrames;
        this.frameDelay = frameDelay;
        this.rows = rows;
        this.columns = columns;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.rOffset = Utils.degToRad(rOffset);
        this.defaultDelay = defaultDelay;
    }
}