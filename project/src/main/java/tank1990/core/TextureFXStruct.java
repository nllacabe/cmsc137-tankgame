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
 * @class TextureFXStruct
 * @brief A structure that holds metadata and configuration for a texture effect.
 * @details This class is used to define the properties of a texture effect, including the path to the texture
 * file, offset values for positioning, and the default delay for rendering the texture.
 */
public class TextureFXStruct implements Serializable {
    String path;         /**< The path to the texture image file. */
    int offsetX;         /**< The X offset to apply to the texture's position. */
    int offsetY;         /**< The Y offset to apply to the texture's position. */
    int defaultDelay;    /**< The default delay (in frames) for the texture effect before it is rendered again ( 0 means no delay). */

    /**
     * Constructs a TextureFXStruct object with specified path, offsets, and delay.
     *
     * @param path The path to the texture image file.
     * @param offsetX The X offset to apply to the texture's position.
     * @param offsetY The Y offset to apply to the texture's position.
     * @param defaultDelay The default delay (in frames) before the texture effect is rendered again.
     */
    public TextureFXStruct(String path, int offsetX, int offsetY, int defaultDelay) {
        this.path = path;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.defaultDelay = defaultDelay;
    }

    /**
     * Constructs a TextureFXStruct object with specified path and offsets, using a default delay of 0.
     *
     * @param path The path to the texture image file.
     * @param offsetX The X offset to apply to the texture's position.
     * @param offsetY The Y offset to apply to the texture's position.
     */
    public TextureFXStruct(String path, int offsetX, int offsetY) {
        this.path = path;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.defaultDelay = 0;
    }
}