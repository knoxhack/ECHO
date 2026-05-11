package com.knoxhack.echocore.api.config;

public record EchoConfigValidation(boolean valid, String normalizedValue, String message) {
    public EchoConfigValidation {
        normalizedValue = normalizedValue == null ? "" : normalizedValue.strip();
        message = message == null ? "" : message.strip();
    }

    public static EchoConfigValidation ok(String normalizedValue) {
        return new EchoConfigValidation(true, normalizedValue, "");
    }

    public static EchoConfigValidation error(String message) {
        return new EchoConfigValidation(false, "", message);
    }
}
