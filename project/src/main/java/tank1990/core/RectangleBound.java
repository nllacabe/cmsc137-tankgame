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

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * @class RectangleBound
 * @brief Represents a rectangular boundary with optional rotation.
 * @details This class extends the Rectangle class to include rotation support.
 * It provides methods for collision detection, retrieving rotated coordinates,
 * and rendering the rotated rectangle.
 */
public class RectangleBound extends Rectangle {
    private int x, y;           /**< X and Y coordinates of the rectangle */
    private int width, height;  /**< Width and height of the rectangle */
    private double rRad = 0.0;  /**< Rotation angle in radians */

    /**
     * Constructs a RectangleBound with given position and size.
     * @param x X-coordinate of the rectangle.
     * @param y Y-coordinate of the rectangle.
     * @param size Size of the rectangle.
     */
    public RectangleBound(int x, int y, Dimension size) {
        this(x, y, size.width, size.height);
    }

    /**
     * Constructs a RectangleBound with given position and size.
     * @param x X-coordinate of the rectangle.
     * @param y Y-coordinate of the rectangle.
     * @param width Width of the rectangle.
     * @param height Height of the rectangle.
     */
    public RectangleBound(int x, int y, int width, int height) {
        this(x, y, width, height, 0.0);
    }

    /**
     * Constructs a RectangleBound with position, size, and rotation.
     * @param x X-coordinate of the rectangle.
     * @param y Y-coordinate of the rectangle.
     * @param width Width of the rectangle.
     * @param height Height of the rectangle.
     * @param rRad Rotation angle in radians.
     */
    public RectangleBound(int x, int y, int width, int height, double rRad) {
        super(x, y, width, height);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rRad = rRad;
    }

    /**
     * @brief Gets the rotation angle in radians.
     * @return Rotation angle in radians.
     */
    public double getR() {
        return this.rRad;
    }

    /**
     * @brief Computes the rotated X-coordinate of the rectangle's origin.
     * @return The rotated X-coordinate.
     */
    public int getOriginX() {
        double centerX = this.x + this.width / 2.0;
        double centerY = this.y + this.height / 2.0;

        double rotatedX = (this.x - centerX) * Math.cos(this.rRad) - (this.y - centerY) * Math.sin(this.rRad) + centerX;
        return (int) rotatedX;
    }

    /**
     * @brief Computes the rotated Y-coordinate of the rectangle's origin.
     * @return The rotated Y-coordinate.
     */
    public int getOriginY() {
        double centerX = this.x + this.width / 2.0;
        double centerY = this.y + this.height / 2.0;

        double rotatedY = (this.x - centerX) * Math.sin(this.rRad) + (this.y - centerY) * Math.cos(this.rRad) + centerY;
        return (int) rotatedY;
    }

    /**
     * @brief Checks if two rotated rectangles collide.
     * @param rb1 First RectangleBound object.
     * @param rb2 Second RectangleBound object.
     * @return True if the rectangles collide, false otherwise.
     */
    public static boolean isCollided(RectangleBound rb1, RectangleBound rb2) {
        Polygon p1 = rb1.getPolygon();
        Polygon p2 = rb2.getPolygon();

        return p1.getBounds2D().intersects(p2.getBounds2D()) ||
                p2.getBounds2D().contains(p1.getBounds2D()) ||
                p1.getBounds2D().contains(p2.getBounds2D());
    }

    /**
     * @brief Checks if a rectangle and a rotated rectangle collide.
     * @param r1 Regular Rectangle object.
     * @param rb2 RectangleBound object.
     * @return True if the rectangles collide, false otherwise.
     */
    public static boolean isCollided(Rectangle r1, RectangleBound rb2) {
        Polygon p2 = rb2.getPolygon();

        return r1.getBounds2D().intersects(p2.getBounds2D()) ||
                p2.getBounds2D().contains(r1.getBounds2D()) ||
                r1.getBounds2D().contains(p2.getBounds2D());
    }

    /**
     * @brief Generates a polygon representing the rotated rectangle.
     * @return Polygon object of the rotated rectangle.
     */
    public Polygon getPolygon() {
        int[] xPoints = new int[4];
        int[] yPoints = new int[4];

        // Get the 4 corners of the rectangle (without rotation)
        Point[] points = new Point[4];
        points[0] = new Point(this.x, this.y); // top-left
        points[1] = new Point(this.x + this.width, this.y); // top-right
        points[2] = new Point(this.x + this.width, this.y + this.height); // bottom-right
        points[3] = new Point(this.x, this.y + this.height); // bottom-left

        // Calculate the center of the rectangle
        double centerX = this.x + this.width / 2.0;
        double centerY = this.y + this.height / 2.0;

        // Rotate each point around the center of the rectangle
        for (int i = 0; i < 4; i++) {
            Point p = points[i];
            // Translate the point to the origin (subtract the center coordinates)
            double x = p.x - centerX;
            double y = p.y - centerY;

            // Apply rotation using the rotation matrix
            double newX = centerX + x * Math.cos(this.rRad) - y * Math.sin(this.rRad);
            double newY = centerY + x * Math.sin(this.rRad) + y * Math.cos(this.rRad);

            // Store the rotated coordinates
            xPoints[i] = (int) Math.round(newX);
            yPoints[i] = (int) Math.round(newY);
        }

        // Create and return the rotated polygon
        return new Polygon(xPoints, yPoints, 4);
    }

    /**
     * @brief Draws the rotated rectangle.
     *
     * This method used for debug purposes.
     * @param g Graphics object for drawing.
     */
    public void draw(Graphics g) {
        // Save current transformation
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        g2d.setColor(Color.RED);
        g2d.fill(getPolygon());
        g2d.draw(getPolygon());

        g2d.translate(getCenterX(), getCenterY());
        g2d.setColor(Color.WHITE);
        g2d.fillRect(-5, -5, 5, 5);

        // Restore previous transformation
        g2d.setTransform(oldTransform);
    }

    /**
     * @brief Draws the rotated rectangle with a specified color.
     *
     * This method used for debug purposes
     * @param g Graphics object for drawing.
     * @param c Color of the rectangle.
     */
    public void draw(Graphics g, Color c) {
        // Save current transformation
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        g2d.setColor(c);
        g2d.fill(getPolygon());
        g2d.draw(getPolygon());

        // Restore previous transformation
        g2d.setTransform(oldTransform);
    }

    /**
     * @brief Draws the center of the rotated rectangle.
     *
     * This method used for debug purposes
     * @param g Graphics object for drawing.
     */
    public void drawCenter(Graphics g) {
        // Save current transformation
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        g2d.translate(getCenterX(), getCenterY());
        g2d.setColor(Color.WHITE);
        g2d.fillRect(-5, -5, 5, 5);

        // Restore previous transformation
        g2d.setTransform(oldTransform);
    }

    public String toString() {
        return String.format("RectangleBound [x=%d, y=%d, width=%d, height=%d, rRad=%.2f]",
                             this.x, this.y, this.width, this.height, this.rRad);
    }
}
