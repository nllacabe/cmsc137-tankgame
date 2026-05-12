package tank1990.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @class Subject
 * @brief Subject class that maintains a list of observers and notifies them of events.
 * @details This is part of the Observer design pattern, where the Subject is the object being observed.
 * Observers can subscribe to or unsubscribe from the Subject to receive updates.
 */
public abstract class Subject {
    List<Observer> observers;

    public Subject() {
        observers = new ArrayList<>();
    }   

    /**
     * Subscribes an observer to the subject.
     * The observer will receive notifications of events.
     *
     * @param observer The observer to subscribe.
     */
    public void subscribe(Observer observer) {
        observers.add(observer);
    }

    /**
     * Unsubscribes an observer from the subject.
     * The observer will no longer receive notifications of events.
     *
     * @param observer The observer to unsubscribe.
     */
    public void unsubscribe(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all subscribed observers of an event.
     * The observers will receive the event type and associated data.
     *
     * @param event The type of event that occurred.
     * @param data Additional data related to the event.
     */
    protected void notify(EventType event, Object data) {
        for(Observer subscriber: observers) {
            subscriber.eventFilter(event, data);
        }
    }
}
