package com.knoxhack.echocore.api;

/**
 * Safe fallback used when ECHO: Lens is not installed.
 */
public final class NoOpLensService implements ILensService {
    public static final NoOpLensService INSTANCE = new NoOpLensService();

    private NoOpLensService() {
    }
}
