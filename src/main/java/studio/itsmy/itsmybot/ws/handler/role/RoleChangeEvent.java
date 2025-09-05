package studio.itsmy.itsmybot.ws.handler.role;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object describing a role change intent for a given player.
 * <p>
 * Used to tag mutations as expected in the {@link LuckPermsSyncManager}.
 */
public class RoleChangeEvent {

    /** Role mutation type. */
    public enum Action { ADD, REMOVE }

    /** Player UUID. */
    public final UUID uuid;

    /** Role (group) name. */
    public final String role;

    /** Mutation action (ADD/REMOVE). */
    public final Action action;

    /**
     * Creates a new role change event.
     *
     * @param uuid   player UUID
     * @param role   role (group) name
     * @param action mutation action
     */
    public RoleChangeEvent(UUID uuid, String role, Action action) {
        this.uuid = uuid;
        this.role = role;
        this.action = action;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RoleChangeEvent)) return false;
        final RoleChangeEvent other = (RoleChangeEvent) o;
        return uuid.equals(other.uuid) && role.equals(other.role) && action == other.action;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(uuid, role, action);
    }
}