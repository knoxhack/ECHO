package com.knoxhack.echocore.api.index;

public interface IIndexTerminalBridge {
    default boolean available() {
        return false;
    }

    default void register() {
    }
}
