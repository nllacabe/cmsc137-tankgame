package tank1990.core;

/**
 * @class EventType
 * @brief Enum representing different types of events in the game.
 * @details This enum is used to categorize events of GameEngine.
 */
public enum EventType {
    NULL,
    UPDATE,
    REPAINT,
    UPDATE_MAP,
    PAUSED,
    STARTED,
    UPDATE_GAME_INFO,
    NEXT_LEVEL,
    GAMEOVER,
    GAME_LOADED,
    GAME_SAVED
}
