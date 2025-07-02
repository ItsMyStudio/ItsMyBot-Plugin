package com.ordwen.enums;

public enum Permissions {

    LINK("itsmybot.link"),
    UNLINK("itsmybot.unlink"),
    CLAIM("itsmybot.claim"),

    RELOAD("itsmybot.reload");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String get() {
        return permission;
    }
}
