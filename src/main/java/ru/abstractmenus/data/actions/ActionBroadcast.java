package ru.abstractmenus.data.actions;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.datatype.TypeDuration;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.util.adventure.AdventureUtil;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class ActionBroadcast implements Action {

    private List<String> chatMessages;
    private String json;
    private String actionbar;
    private String title = "", subtitle = "";
    private Duration fadeIn = Duration.ofSeconds(0);
    private Duration stay = Duration.ofSeconds(0);
    private Duration fadeOut = Duration.ofSeconds(0);

    private ActionBroadcast() {}

    private void setChatMessages(List<String> messages) {
        this.chatMessages = messages;
    }

    private void setJson(String json) {
        this.json = json;
    }

    private void setActionbar(String actionbar) {
        this.actionbar = actionbar;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    private void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    private void setFadeIn(Duration fadeIn) {
        this.fadeIn = fadeIn;
    }

    private void setFadeOut(Duration fadeOut) {
        this.fadeOut = fadeOut;
    }

    private void setStay(Duration stay) {
        this.stay = stay;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if (player == null) return;

        List<String> replacedChat = chatMessages != null ? Handlers.getPlaceholderHandler().replace(player, chatMessages) : null;
        String replacedJson = json != null ? Handlers.getPlaceholderHandler().replace(player, json) : null;
        String replacedActionbar = actionbar != null ? Handlers.getPlaceholderHandler().replace(player, actionbar) : null;
        String replacedTitle = Handlers.getPlaceholderHandler().replace(player, title);
        String replacedSubtitle = Handlers.getPlaceholderHandler().replace(player, subtitle);

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (replacedChat != null) {
                for (String message : replacedChat) {
                    AdventureUtil.sendMessage(target, message);
                }
            }

            if (replacedJson != null) {
                Component component = GsonComponentSerializer.gson().deserialize(replacedJson);
                target.sendMessage(component);
            }

            if (replacedActionbar != null) {
                AdventureUtil.sendActionbar(target, replacedActionbar);
            }

            if (!title.isEmpty() || !subtitle.isEmpty()) {
                AdventureUtil.sendTitle(target, replacedTitle, replacedSubtitle, fadeIn, stay, fadeOut);
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionBroadcast> {

        @Override
        public ActionBroadcast deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            ActionBroadcast message = new ActionBroadcast();

            if (!node.isMap()) {
                message.setChatMessages(Collections.singletonList(node.getString()));
                return message;
            }

            if (node.node("chat").rawValue() != null) {
                message.setChatMessages(node.node("chat").getList(String.class));
            }

            if (node.node("json").rawValue() != null) {
                JsonElement json = node.node("json").getValue(JsonElement.class);
                if (json != null) {
                    message.setJson(json.toString());
                } else {
                    throw new NodeSerializeException(node.node("json"), "Cannot parse HOCON nodes as JSON objects. Check your menu file.");
                }
            }

            if (node.node("actionbar").rawValue() != null) {
                message.setActionbar(node.node("actionbar").getString());
            }

            message.setTitle(node.node("title").getString(""));
            message.setSubtitle(node.node("subtitle").getString(""));

            if (node.node("fadeIn").rawValue() != null) {
                message.setFadeIn(new TypeDuration(node.node("fadeIn").getString()).getDuration());
            }

            if (node.node("stay").rawValue() != null) {
                message.setStay(new TypeDuration(node.node("stay").getString()).getDuration());
            }

            if (node.node("fadeOut").rawValue() != null) {
                message.setFadeOut(new TypeDuration(node.node("fadeOut").getString()).getDuration());
            }

            return message;
        }
    }
}
