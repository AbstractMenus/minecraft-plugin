package ru.abstractmenus.handlers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.node.types.PermissionNode;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.handler.PermissionsHandler;

import java.util.Optional;

public class LuckPermsHandler implements PermissionsHandler {

    private final LuckPerms api;

    public LuckPermsHandler(LuckPerms api) {
        this.api = api;
    }

    private User getUser(Player player) {
        return player == null || !player.isOnline() ? null : api.getPlayerAdapter(Player.class).getUser(player);
    }

    @Override
    public void addPermission(Player player, String permission) {
        User user = getUser(player);

        if (user != null) {
            Node node = PermissionNode.builder(permission).build();
            user.data().add(node);
            api.getUserManager().saveUser(user);
        }
    }

    @Override
    public void removePermission(Player player, String permission) {
        User user = getUser(player);

        if (user != null) {
            Node node = PermissionNode.builder(permission).build();
            user.data().remove(node);
            api.getUserManager().saveUser(user);
        }
    }

    @Override
    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void addGroup(Player player, String group) {
        User user = getUser(player);

        if (user != null) {
            Node node = InheritanceNode.builder(group).build();
            user.data().add(node);
            api.getUserManager().saveUser(user);
        }
    }

    @Override
    public void removeGroup(Player player, String group) {
        User user = getUser(player);

        if (user != null) {
            Node node = InheritanceNode.builder(group).build();
            user.data().remove(node);
            api.getUserManager().saveUser(user);
        }
    }

    @Override
    public boolean hasGroup(Player player, String groupName) {
        return player.hasPermission("group." + groupName);
    }

    public void addMeta(Player player, String key, String value) {
        User user = getUser(player);

        if (user != null) {
            Node node = MetaNode.builder(key, value).build();
            user.data().add(node);
            api.getUserManager().saveUser(user);
        }
    }

    public void removeMeta(Player player, String key) {
        User user = getUser(player);

        if (user != null) {
            user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(key)));
            api.getUserManager().saveUser(user);
        }
    }

    public Optional<String> getMeta(Player player, String key) {
        User user = getUser(player);

        if (user != null) {
            return Optional.ofNullable(user.getCachedData().getMetaData().getMetaValue(key));
        }

        return Optional.empty();

    }

    public boolean hasMeta(Player player, String key) {
        User user = getUser(player);

        if (user != null) {
            String metaValue = user.getCachedData().getMetaData().getMetaValue(key);
            return StringUtils.isNotBlank(metaValue);
        }

        return false;

    }
}
