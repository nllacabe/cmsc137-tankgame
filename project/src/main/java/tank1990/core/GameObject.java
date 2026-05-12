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
import java.io.Serializable;

/**
 * @class GameObject
 * @brief Represents a game object in the game world.
 * @details This class is abstract and should be extended by specific game objects.
 * This class serves as a base class for all game objects, providing common properties and methods.
 * It implements Serializable to allow saving and loading game states.
 */
public abstract class GameObject implements Serializable {
    protected int x;
    protected int y;
    protected Direction dir;
    protected int depth = 0;
    protected int width;
    protected int height;

    // Marked as transient since it can be reconstructed from other fields
    protected transient RectangleBound boundingBox = null;

    /**
     * Gets x-coordinate of the object.
     * @return x-coordinate
     */
    public int getX() { return this.x; }

    /**
     * Gets y-coordinate of the object.
     * @return y-coordinate
     */
    public int getY() { return this.y; }

    /**
     * Gets direction of the object.
     * @return direction
     */
    public Direction getDir() { return this.dir; }

    /**
     * Gets depth of the object.
     * Depth is used to determine the rendering order of objects.
     * Objects with lower depth values are rendered first.
     * @return depth
     */
    public int getDepth() { return this.depth; }

    /**
     * Sets x-coordinate of the object.
     * @param x x-coordinate to set
     */
    public void setX(int x) { this.x = x; }

    /**
     * Sets y-coordinate of the object.
     * @param y y-coordinate to set
     */
    public void setY(int y) { this.y = y; }

    /**
     * Sets direction of the object.
     * @param dir direction to set
     */
    public void setDir(Direction dir) { this.dir = dir; }

    /**
     * Sets depth of the object.
     * @param depth depth to set
     */
    public void setDepth(int depth) { this.depth = depth; }

    /**
     * Gets the size of the object.
     * @return Dimension representing the size of the object
     */
    public Dimension getSize() {return new Dimension(this.width, this.height);}

    /**
     * Sets the size of the object.
     * @param dimension Dimension representing the new size of the object
     */
    public void setSize(Dimension dimension) {this.width = (int) dimension.getWidth(); this.height = (int) dimension.getHeight();}

    /**
     * Draws the object on the given graphics context.
     * @param g Graphics context to draw on
     */
    public abstract void draw(Graphics g);

    /**
     * Gets the bounding box of the object.
     * @return RectangleBound representing the bounding box of the object
     */
    public RectangleBound getBoundingBox() {
        return new RectangleBound(this.x - (int) (this.width*0.5), this.y - (int) (this.height*0.5), this.width, this.height);
    }

    /**
     * Reads the object from the input stream.
     * @param in ObjectInputStream to read the object from
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Reset transient fields after deserialization
        this.boundingBox = null;
    }

    /**
     * Writes the object to the output stream.
     * @param out ObjectOutputStream to write the object to
     * @throws java.io.IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }
}
