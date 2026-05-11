package com.knoxhack.echocore.api.config;

public record EchoConfigApplyResult(
        boolean success,
        String moduleId,
        String entryId,
        String value,
        String message) {
    public EchoConfigApplyResult {
        moduleId = clean(moduleId);
        entryId = clean(entryId);
        value = value == null ? "" : value.strip();
        message = message == null ? "" : message.strip();
    }

    public static EchoConfigApplyResult success(String moduleId, String entryId, String value, String message) {
        return new EchoConfigApplyResult(true, moduleId, entryId, value, message);
    }

    public static EchoConfigApplyResult failure(String moduleId, String entryId, String message) {
        return new EchoConfigApplyResult(false, moduleId, entryId, "", message);
    }

    private static String clean(String value) {
        return value == null ? "" : value.strip().toLowerCase(java.util.Locale.ROOT);
    }
}
