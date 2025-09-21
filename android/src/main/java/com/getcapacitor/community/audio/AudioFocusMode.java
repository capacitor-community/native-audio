package com.getcapacitor.community.audio;

public enum AudioFocusMode {
    NONE("none"),
    EXCLUSIVE("exclusive"),
    DUCK("duck");

    private final String value;

    AudioFocusMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AudioFocusMode fromString(String value) {
        if (value == null) {
            return NONE;
        }

        for (AudioFocusMode mode : AudioFocusMode.values()) {
            if (mode.value.equals(value)) {
                return mode;
            }
        }

        return NONE;
    }
}
