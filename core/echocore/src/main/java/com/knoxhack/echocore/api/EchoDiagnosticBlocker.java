package com.knoxhack.echocore.api;

import net.minecraft.resources.Identifier;

public record EchoDiagnosticBlocker(
        Identifier id,
        String chapterId,
        Severity severity,
        String title,
        String detail,
        String nextAction) {
    public EchoDiagnosticBlocker {
        if (id == null) {
            throw new IllegalArgumentException("ECHO diagnostic id is required.");
        }
        chapterId = chapterId == null || chapterId.isBlank() ? id.getNamespace() : chapterId;
        severity = severity == null ? Severity.INFO : severity;
        title = title == null || title.isBlank() ? id.getPath() : title;
        detail = detail == null ? "" : detail;
        nextAction = nextAction == null ? "" : nextAction;
    }

    public enum Severity {
        INFO,
        WARNING,
        BLOCKED,
        CRITICAL
    }
}
