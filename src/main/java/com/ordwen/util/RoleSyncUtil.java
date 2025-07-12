package com.ordwen.util;

import com.google.gson.JsonObject;
import com.ordwen.ItsMyBotPlugin;
import com.ordwen.ws.handler.role.FullRoleSyncWSCommandHandler;
import org.bukkit.entity.Player;

public class RoleSyncUtil {

    private RoleSyncUtil() {}

    public static boolean sendFullRoleSync(ItsMyBotPlugin plugin, Player player) {
        final FullRoleSyncWSCommandHandler handler = new FullRoleSyncWSCommandHandler(plugin);
        final JsonObject request = handler.buildRequest(player, new String[0]);
        if (request == null) return false;

        plugin.getWSClient().sendMessage(request.toString());
        return true;
    }
}
