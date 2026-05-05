package com.knoxhack.echocore.api;

/**
 * Result of a faction dialogue, service, or contract action.
 */
public record EchoFactionActionResult(
        boolean success,
        String title,
        String message,
        int reputationDelta,
        boolean refresh) {

    public EchoFactionActionResult {
        title = title == null || title.isBlank() ? (success ? "Accepted" : "Rejected") : title.trim();
        message = message == null ? "" : message.trim();
    }

    public static EchoFactionActionResult success(String title, String message) {
        return new EchoFactionActionResult(true, title, message, 0, true);
    }

    public static EchoFactionActionResult success(String title, String message, int reputationDelta) {
        return new EchoFactionActionResult(true, title, message, reputationDelta, true);
    }

    public static EchoFactionActionResult info(String title, String message) {
        return new EchoFactionActionResult(true, title, message, 0, false);
    }

    public static EchoFactionActionResult failure(String title, String message) {
        return new EchoFactionActionResult(false, title, message, 0, true);
    }
}
