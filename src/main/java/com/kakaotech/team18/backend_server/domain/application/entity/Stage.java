package com.kakaotech.team18.backend_server.domain.application.entity;

public enum Stage {
    INTERVIEW, FINAL;

    public static Stage fromRaw(String raw) {
        if (raw == null) throw new IllegalArgumentException("stage is required");
        try {
            return Stage.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported stage: " + raw);
        }
    }
}
