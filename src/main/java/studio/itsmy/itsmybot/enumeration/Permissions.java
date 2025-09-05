package studio.itsmy.itsmybot.enumeration;

/**
 * Defines all plugin permission nodes used for command and feature access control.
 * <p>
 * Each enum value corresponds to a permission string that can be registered
 * in {@code plugin.yml} and assigned to players or groups via a permission manager
 * (e.g., LuckPerms).
 */
public enum Permissions {

    USE("itsmybot.use"),
    LINK("itsmybot.link"),
    UNLINK("itsmybot.unlink"),
    CLAIM("itsmybot.claim"),

    RELOAD("itsmybot.reload");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    /**
     * Returns the permission node as a string.
     *
     * @return the permission string (e.g., {@code "itsmybot.use"})
     */
    public String get() {
        return permission;
    }
}
