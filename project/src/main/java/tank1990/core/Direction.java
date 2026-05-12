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

/**
 * @class Direction
 * @brief Represents the four primary directions and provides methods for direction manipulation.
 */
public enum Direction {
    DIRECTION_INVALID,
    DIRECTION_UPWARDS,
    DIRECTION_RIGHT,
    DIRECTION_DOWNWARDS,
    DIRECTION_LEFT;

    /**
     * Returns the opposite direction of the current direction.
     * @return The opposite direction.
     */
    public Direction opposite() {
        switch (this) {
            case DIRECTION_UPWARDS:
                return DIRECTION_DOWNWARDS;
            case DIRECTION_RIGHT:
                return DIRECTION_LEFT;
            case DIRECTION_DOWNWARDS:
                return DIRECTION_UPWARDS;
            case DIRECTION_LEFT:
                return DIRECTION_RIGHT;
            default:
                return DIRECTION_INVALID;
        }
    }

    /**
     * Returns the direction as a string.
     * @return A string representation of the direction.
     */
    public Direction rotateCW() {
        return switch (this) {
            case DIRECTION_UPWARDS -> DIRECTION_RIGHT;
            case DIRECTION_RIGHT -> DIRECTION_DOWNWARDS;
            case DIRECTION_DOWNWARDS -> DIRECTION_LEFT;
            case DIRECTION_LEFT -> DIRECTION_UPWARDS;
            default -> DIRECTION_INVALID;
        };
    }

    /**
     * Returns the direction rotated counter-clockwise.
     * @return The counter-clockwise rotated direction.
     */
    public Direction rotateCCW() {
        return switch (this) {
            case DIRECTION_UPWARDS -> DIRECTION_LEFT;
            case DIRECTION_LEFT -> DIRECTION_DOWNWARDS;
            case DIRECTION_DOWNWARDS -> DIRECTION_RIGHT;
            case DIRECTION_RIGHT -> DIRECTION_UPWARDS;
            default -> DIRECTION_INVALID;
        };
    }

}
