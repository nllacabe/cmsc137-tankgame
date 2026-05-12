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
import java.util.HashMap;

import tank1990.tank.TankType;
import tank1990.tile.Tile;

/**
 * @class LevelInfo
 * @brief Represents the information of a game level, including the grid structure of game map and enemy tank counts.
 * @details This class is used to store the configuration of a level, including the arrangement of tiles and the number of enemy tanks.
 */
public class LevelInfo implements Serializable {
    public Tile[][] levelGrid;
    public HashMap<TankType, Integer> enemyTankCount;

    public LevelInfo(Tile[][] levelGrid, HashMap<TankType, Integer> enemyTankCount) {
        this.levelGrid = levelGrid;
        this.enemyTankCount = enemyTankCount;
    }
}
