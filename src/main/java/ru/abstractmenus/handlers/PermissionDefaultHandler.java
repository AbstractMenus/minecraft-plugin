package ru.abstractmenus.handlers;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import ru.abstractmenus.api.handler.PermissionsHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionDefaultHandler implements PermissionsHandler {

    private final Plugin plugin;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public PermissionDefaultHandler(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void addPermission(Player player, String permission) {
        if(!attachments.containsKey(player.getUniqueId())){
            attachments.put(player.getUniqueId(), player.addAttachment(plugin));
        }

        attachments.get(player.getUniqueId()).setPermission(permission, true);
    }

    @Override
    public void removePermission(Player player, String permission) {
        PermissionAttachment attachment = attachments.get(player.getUniqueId());

        if(attachment != null){
            attachment.unsetPermission(permission);
        }
    }

    @Override
    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void addGroup(Player player, String group) {

    }

    @Override
    public void removeGroup(Player player, String group) {

    }

    @Override
    public boolean hasGroup(Player player, String group) {
        return false;
    }
}
