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

import tank1990.panels.GamePanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

import static tank1990.core.Globals.GAME_TICK_MS;

/**
 * @interface Utils
 * @brief Utility methods for various game-related operations.
 * @details This interface provides static methods for time conversion, image loading, collision detection, and more.
 */
public interface Utils {
    /**
     * Converts a duration in milliseconds to the number of game ticks.
     * @param durationMs Duration in milliseconds
     * @return Number of game ticks
     */
    static int Time2GameTick(int durationMs) {
        return durationMs / GAME_TICK_MS;
    }

    /**
     * Converts a game tick to the corresponding time in milliseconds.
     * @param tick Game tick
     * @return Time in milliseconds
     */
    static int GameTick2Time(int tick) {
        return tick * GAME_TICK_MS;
    }

    /**
     * Loads a PNG icon from resources and scales it to the specified width and height.
     * @param path Path to the PNG file (e.g., "icons/icon.png")
     * @param targetWidth Desired width of the icon
     * @param targetHeight Desired height of the icon
     * @return ImageIcon scaled to the specified dimensions, or null if loading failed
     * @throws NullPointerException
     */
    static ImageIcon loadPNGIcon(String path, int targetWidth, int targetHeight) throws NullPointerException {
        try {
            InputStream inputStream = Globals.class.getClassLoader().getResourceAsStream(path);
            if (inputStream == null) {
                System.err.println("PNG file not found: " + path);
                return null;
            }

            ImageIcon icon = new ImageIcon(inputStream.readAllBytes());
            Image image = icon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);  // Resize image
            return new ImageIcon(image);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static Image loadTexture(String fileName) {
        try {
            InputStream inputStream = Globals.class.getClassLoader().getResourceAsStream(fileName);
            if (inputStream == null) {
                System.err.println("Texture file not found: " + fileName);
                return null;
            }
            return ImageIO.read(inputStream);  // Load as BufferedImage
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a texture image from resources and scales it to the specified width and height.
     *
     * @param fileName Name of the texture file (e.g., "textures/texture.png")
     * @param width    Desired width of the texture
     * @param height   Desired height of the texture
     * @return Scaled Image or null if loading failed
     */
    static Image loadTexture(String fileName, int width, int height) {
        try {
            InputStream inputStream = Globals.class.getClassLoader().getResourceAsStream(fileName);
            if (inputStream == null) {
                System.err.println("Texture file not found: " + fileName);
                return null;
            }
            Image original = ImageIO.read(inputStream);  // Load as BufferedImage
            return original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a custom TTF font from resources and returns a derived font.
     *
     * @param path  Path to the font file (e.g., "/fonts/arcade.ttf")
     * @param style Font.PLAIN, Font.BOLD, Font.ITALIC, or Font.BOLD | Font.ITALIC
     * @param size  Font size in points
     * @return the custom Font or fallback Font if failed
     */
    static Font loadFont(String path, int style, float size) {
        try {
            InputStream is = Globals.class.getClassLoader().getResourceAsStream(path);
            if (is == null) throw new IllegalArgumentException("Font not found at " + path);

            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            Font styledFont = baseFont.deriveFont(style, size);

            // Optional: register it system-wide (so you can use by name if needed)
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(styledFont);

            return styledFont;
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Monospaced", style, (int) size); // fallback font
        }
    }

    /**
     * Converts radians to degrees.
     * @param radians angle in radians
     * @return angle in degrees
     */
    static double radToDeg(double radians) {
        return Math.toDegrees(radians);
    }

    // Convert degrees to radians
    static double degToRad(double degrees) {
        return Math.toRadians(degrees);
    }

    static boolean isObjectsCollided(Rectangle rect1, Rectangle rect2) {
        return rect1.getBounds().intersects(rect2.getBounds()) ||
                rect2.getBounds().contains(rect1.getBounds()) ||
                rect1.getBounds().contains(rect2.getBounds());
    }

    static boolean isObjectsCollided(RectangleBound rectBound1, RectangleBound rectBound2) {
        return RectangleBound.isCollided(rectBound1, rectBound2);
    }

    static boolean isObjectsCollided(Rectangle rect1, RectangleBound rectBound2) {
        return RectangleBound.isCollided(rect1, rectBound2);
    }

    /**
     * Converts a GridLocation to a Location in the game area.
     * Converted location is the center of the cell.
     * Note: This method assumes that the game area has been initialized and its dimensions are set.
     * @param gloc GridLocation to convert
     * @return Location corresponding to the GridLocation
     */
    static Location gridLoc2Loc(GridLocation gloc) {
        Dimension gameAreaDimension = GamePanel.getGameAreaDimension();

        if (gameAreaDimension == null) {
            throw new IllegalStateException("Size of game area has not been set yet!");
        }

        int cellWidth = gameAreaDimension.width / Globals.COL_TILE_COUNT;
        int cellHeight = gameAreaDimension.height / Globals.ROW_TILE_COUNT;
        return new Location(cellWidth * gloc.colIndex() + cellWidth/2, cellHeight * gloc.rowIndex() + cellHeight/2);
    }

    /**
     * Converts a Location in the game area to a GridLocation.
     * Note: This method assumes that the game area has been initialized and its dimensions are set.
     * @param loc Location to convert
     * @return GridLocation corresponding to the Location
     */
    static GridLocation loc2GridLoc(Location loc) {
        Dimension gameAreaDimension = GamePanel.getGameAreaDimension();

        if (gameAreaDimension == null) {
            throw new IllegalStateException("Size of game area has not been set yet!");
        }

        int cellWidth = gameAreaDimension.width / Globals.COL_TILE_COUNT;
        int cellHeight = gameAreaDimension.height / Globals.ROW_TILE_COUNT;

        return new GridLocation(Math.max(Math.min((int) (loc.y() / cellHeight), Globals.ROW_TILE_COUNT-1), 0),
                Math.max(Math.min((int) (loc.x() / cellWidth), Globals.COL_TILE_COUNT-1), 0));
    }

    /*
     * Normalizes width and height in respect to size of tile size.
     *
     * @param g Reference screen size
     * @param width Width of source object
     * @param height Height of source object
     * @return normalized Dimension
     */
    static Dimension normalizeDimension(Graphics g, int width, int height) {
        width = width * (g.getClipBounds().width / Globals.COL_TILE_COUNT) / Globals.TILE_WIDTH;
        height = height * (g.getClipBounds().height / Globals.ROW_TILE_COUNT) / Globals.TILE_HEIGHT;

        return new Dimension(width, height);
    }

    /*
     * Normalizes length in respect to size of tile size.
     *
     * @param g Reference graphics
     * @param value Value to be normalized
     * @return normalized Dimension
     */
    static int normalize(Graphics g, int value) {
        int gSize = Math.min(g.getClipBounds().width, g.getClipBounds().height);
        int tileCount = Math.min(Globals.COL_TILE_COUNT, Globals.ROW_TILE_COUNT);
        int tileSize = Math.min(Globals.TILE_WIDTH, Globals.TILE_HEIGHT);

        return value * (gSize / tileCount) / tileSize;
    }

    /**
     * Generates a random boolean based on the given probability percentage.
     * @param percent Probability percentage (0-100)
     * @return true with the given probability, false otherwise
     */
    static boolean getRandomProbability(int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Probability must be between 0 and 100");
        }
        if (percent == 0) return false; // 0% chance
        return Math.random() * 100 < percent;
    }
}
