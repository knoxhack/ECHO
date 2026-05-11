package com.knoxhack.echocore.api;

import com.knoxhack.echocore.EchoCore;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight runtime bus for shared data changes.
 */
public final class EchoDataBus {
    private static final List<DataChangeListener> LISTENERS = new CopyOnWriteArrayList<>();

    private EchoDataBus() {
    }

    public static AutoCloseable subscribe(DataChangeListener listener) {
        if (listener != null) {
            LISTENERS.add(listener);
        }
        return () -> unsubscribe(listener);
    }

    public static void unsubscribe(DataChangeListener listener) {
        if (listener != null) {
            LISTENERS.remove(listener);
        }
    }

    public static void publish(DataChangeMessage message) {
        if (message == null) {
            return;
        }
        for (DataChangeListener listener : LISTENERS) {
            try {
                listener.onDataChanged(message);
            } catch (RuntimeException exception) {
                EchoCore.LOGGER.warn("ECHO data bus listener {} failed; ignoring listener output.",
                        listener.getClass().getName(), exception);
            }
        }
    }

    public static int listenerCount() {
        return LISTENERS.size();
    }

    public static void clearForTests() {
        LISTENERS.clear();
    }
}
