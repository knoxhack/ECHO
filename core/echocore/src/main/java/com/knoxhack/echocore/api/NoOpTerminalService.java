package com.knoxhack.echocore.api;

/**
 * Safe fallback used when ECHO: Terminal is not installed.
 */
public final class NoOpTerminalService implements ITerminalService {
    public static final NoOpTerminalService INSTANCE = new NoOpTerminalService();

    private NoOpTerminalService() {
    }
}
