package tank1990.core;

/**
 * @class Observer
 * @brief Observer interface to implement Observer pattern.
 * @details Classes implementing this interface can filter and respond to specific events.
 */
public interface Observer {
    /**
     * Event filter method to handle events.
     * @param event
     * @param data
     */
    void eventFilter(EventType event, Object data);

}
