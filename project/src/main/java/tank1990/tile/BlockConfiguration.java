package tank1990.tile;

/**
 * @class Block Configurations
 * @brief Enum representing the different configurations of blocks in the game.
 * @details This enum defines various configurations of blocks based on their states.
 * Following is the mapping of configurations:
 * X => FULL, 0 => EMPTY
 *
 * 00 BLOCK_CONF_EMPTY
 * 00
 *
 * XX BLOCK_CONF_FULL
 * XX
 *
 * XO BLOCK_CONF_1  OX BLOCK_CONF_2  OO BLOCK_CONF_3  0O BLOCK_CONF_4
 * OO               OO               OX               XO
 *
 * XX BLOCK_CONF_5  OX BLOCK_CONF_6  OO BLOCK_CONF_7  XO BLOCK_CONF_8
 * OO               OX               XX               XO
 *
 */
public enum BlockConfiguration {
    BLOCK_CONF_EMPTY,
    BLOCK_CONF_FULL,
    BLOCK_CONF_1,
    BLOCK_CONF_2,
    BLOCK_CONF_3,
    BLOCK_CONF_4,
    BLOCK_CONF_5,
    BLOCK_CONF_6,
    BLOCK_CONF_7,
    BLOCK_CONF_8;

    public static BlockConfiguration valueOf(int val) {
        return switch (val) {
            case (-1) -> BLOCK_CONF_EMPTY;
            case (0) -> BLOCK_CONF_FULL;
            case (1) -> BLOCK_CONF_1;
            case (2) -> BLOCK_CONF_2;
            case (3) -> BLOCK_CONF_3;
            case (4) -> BLOCK_CONF_4;
            case (5) -> BLOCK_CONF_5;
            case (6) -> BLOCK_CONF_6;
            case (7) -> BLOCK_CONF_7;
            case (8) -> BLOCK_CONF_8;
            default -> throw new RuntimeException("Invalid Value for BlockConfiguration");
        };
    }
}
