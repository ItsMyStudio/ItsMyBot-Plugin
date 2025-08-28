package studio.itsmy.itsmybot.ws.handler.role;

import java.util.Objects;
import java.util.UUID;

public class RoleChangeEvent {
    public enum Action { ADD, REMOVE }

    public final UUID uuid;
    public final String role;
    public final Action action;

    public RoleChangeEvent(UUID uuid, String role, Action action) {
        this.uuid = uuid;
        this.role = role;
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RoleChangeEvent)) return false;
        final RoleChangeEvent other = (RoleChangeEvent) o;
        return uuid.equals(other.uuid) && role.equals(other.role) && action == other.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, role, action);
    }
}