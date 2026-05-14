package com.knoxhack.echolens.api;

/**
 * Marker for Lens providers whose output is safe to compute on the server
 * during a server-assisted Deep Scan response.
 */
public interface ServerLensProvider extends LensInfoProvider {
}
