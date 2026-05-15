package com.knoxhack.echocore.api;

/**
 * Safe fallback used when ECHO: SoundCore is not installed.
 */
public final class NoOpSoundService implements ISoundService {
    public static final NoOpSoundService INSTANCE = new NoOpSoundService();

    private NoOpSoundService() {
    }
}
