package com.knoxhack.echocore.api;

/**
 * Safe fallback used when ECHO: ThemeCore is not installed.
 */
public final class NoOpThemeService implements IThemeService {
    public static final NoOpThemeService INSTANCE = new NoOpThemeService();

    private NoOpThemeService() {
    }
}
