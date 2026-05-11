package com.knoxhack.echocore.api;

@FunctionalInterface
public interface DataChangeListener {
    void onDataChanged(DataChangeMessage message);
}
