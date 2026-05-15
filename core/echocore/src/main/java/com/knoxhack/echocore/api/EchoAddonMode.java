package com.knoxhack.echocore.api;

/**
 * Describes how an ECHO addon should behave based on which other ECHO modules are present.
 */
public enum EchoAddonMode {
    /**
     * Only the addon and its required foundations are present.
     * Generic content and local fallback UI only.
     */
    STANDALONE,
    /**
     * One or more optional ECHO modules are present.
     * Integrations are active through Core services, provider contracts, or optional APIs.
     */
    ECHO_CONNECTED,
    /**
     * Ashfall is present.
     * Ashfall-specific content and showcase integrations may register after generic content.
     */
    ASHFALL_CONNECTED
}
