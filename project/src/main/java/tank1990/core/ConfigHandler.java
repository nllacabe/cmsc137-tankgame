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

import org.ini4j.Ini;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * @class ConfigHandler
 * @brief Handles the loading, parsing, and retrieval of configuration values from a file.
 */
public class ConfigHandler implements Serializable{

    public record WindowProperties (
            int windowWidth,
            int windowHeight,
            int x,
            int y
    ) {}

    public record BattleCityProperties (
            int hiScore
    ) {}

    public record LevelProperties (
    ) {}

    private static Ini ini;

    private static ConfigHandler instance = null;

    private ConfigHandler() {}

    public static ConfigHandler getInstance() {
        if (instance == null) {
            instance = new ConfigHandler();
        }
        return instance;
    }

    /**
     * Loads and parses the configuration file.
     * @param filePath The path to the configuration file.
     */
    public void parse(String filePath) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (input == null) {
                throw new IOException(filePath + " is not found in resources");
            }
            ConfigHandler.ini = new Ini(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets value for the key of the configuration parameter in provided section.
     * @param section The section where the key locates.
     * @param key The key of the configuration parameter.
     * @param value The value to set.
     */
    public <T> void setProperty(String section, String key, T value) {
        ConfigHandler.ini.put(section, key, value);
    }

    /**
     * Returns window properties of the game.
     * @return The weapon properties.
     */
    public WindowProperties getWindowProperties() {
        int windowWidth = Integer.parseInt(ConfigHandler.ini.get("Window").get("Width"));
        int windowHeight = Integer.parseInt(ConfigHandler.ini.get("Window").get("Height"));
        int x = Integer.parseInt(ConfigHandler.ini.get("Window").get("X"));
        int y = Integer.parseInt(ConfigHandler.ini.get("Window").get("Y"));
        return new WindowProperties(windowWidth, windowHeight, x ,y);
    }

    /**
     * Returns window properties of the game.
     * @return The weapon properties.
     */
    public BattleCityProperties getBattleCityProperties() {
        int hiScore = Integer.parseInt(ConfigHandler.ini.get("BattleCity").get("HiScore"));
        return new BattleCityProperties(hiScore);
    }

    /**
     * Returns level properties of the level.
     * @return The level properties.
     */
    public LevelProperties getLevel1Properties() {
        return new LevelProperties();
    }

}
