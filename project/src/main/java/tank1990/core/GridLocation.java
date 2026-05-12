package tank1990.core;

import java.io.Serializable;

/**
 * @class GridLocation
 * @brief Represents a location in a grid with row and column indices.
 * @details This class is used to identify specific positions in a grid structure of game world.
 */
public class GridLocation implements Serializable {
    private final int rowIndex;
    private final int colIndex;

    public GridLocation(int rowIndex, int colIndex) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
    }

    /**
     * Returns the row index of this grid location.
     * @return the row index
     */
    public int rowIndex() {
        return rowIndex;
    }

    /**
     * Returns the column index of this grid location.
     * @return the column index
     */
    public int colIndex() {
        return colIndex;
    }

    @Override
    public String toString() {
        return String.format("GL(r:%d, c:%d)", rowIndex, colIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof GridLocation other)) return false;

        return this.rowIndex == other.rowIndex && this.colIndex == other.colIndex;
    }
}
