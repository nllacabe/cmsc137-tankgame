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
 * @class TimeTick
 * @brief A class that manages a countdown timer with optional repeat functionality and associated actions.
 * @details The TimeTick class is responsible for managing a countdown timer, executing actions when the timer reaches zero.
 * It allows setting a countdown action which triggers when the countdown reaches zero.
 */
public class TimeTick implements Serializable {
    private int tick = 0;                       /**< The current tick value, representing the remaining time. */
    private int defaultTick = 0;                /**< The default tick value used to reset the timer. */
    private int repeatCount = -1;               /**< The number of times the timer will repeat after reaching zero. -1 means infinite repeats. */
    private SerializableRunnable action = null; /**< The action to be executed when the timer reaches zero. */

    /**
     * Constructs a TimeTick object with the specified default tick value.
     *
     * @param defaultTick The default number of ticks before the timer reaches zero.
     */
    public TimeTick(int defaultTick) {
        this.defaultTick = defaultTick;
        this.reset();
    }

    /**
     * Constructs a TimeTick object with the specified default tick value and an action to execute when the timer reaches zero.
     *
     * @param defaultTick The default number of ticks before the timer reaches zero.
     * @param action The action to execute when the timer reaches zero.
     */
    public TimeTick(int defaultTick, SerializableRunnable action) {
        this.defaultTick = defaultTick;
        this.action = action;
        this.reset();
    }

    /**
     * Constructs a TimeTick object with the specified tick, default tick value, and repeat count.
     *
     * @param tick The initial tick value for the timer.
     * @param defaultTick The default number of ticks before the timer reaches zero.
     * @param repeatCount The number of repeats after the timer reaches zero. -1 means infinite repeats.
     */
    public TimeTick(int tick, int defaultTick, int repeatCount) {
        this.tick = tick;
        this.defaultTick = defaultTick;
        this.repeatCount = repeatCount;
    }

    /**
     * Sets the repeat count for the timer.
     *
     * @param repeatCount The number of times to repeat the countdown after reaching zero. -1 means infinite repeats.
     */
    public void setRepeats(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    /**
     * Sets the default tick.
     *
     * @param defaultTick The default number of ticks before the timer reaches zero.
     */
    public void setDefaultTick(int defaultTick) {
        if (defaultTick < 0) {
            throw new IllegalArgumentException("Default tick must be non-negative.");
        }
        this.defaultTick = defaultTick;
        this.reset();
    }

    /**
     * Returns the current tick value of the timer.
     *
     * @return The remaining time in ticks.
     */
    public int getTick() {
        return this.tick;
    }

    /**
     * Sets the action to be executed when the timer reaches zero.
     *
     * @param action The action to execute when the timer reaches zero.
     */
    public void setAction(SerializableRunnable action) {
        this.action = action;
    }

    /**
     * Updates the timer by decrementing the tick value and executing the action if the timer reaches zero.
     *
     */
    public void updateTick() {
        if (this.tick > 0) this.tick--;

        if (this.tick == 0 && action != null) {
            action.run();
        }

        // If repeats are available, reset and decrement the repeat count
        if (this.repeatCount > 0 && this.tick == 0) {
            this.repeatCount--;
        }

        // If no repeats are set (repeatCount is -1), keep ticking
        if (this.repeatCount == -1 && this.tick == 0) {
        }
    }

    /**
     * Resets the timer to its default tick value.
     */
    public void reset() {
        this.tick = this.defaultTick;
    }

    /**
     * Checks if the timer has timed out.
     *
     * @return True if the timer has timed out, otherwise false.
     */
    public boolean isTimeOut() {
        return this.tick == 0;
    }

}