package ru.abstractmenus.data.actions;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
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
import java.util.List;

public class ActionMessage implements Action {

    private List<String> chatMessages;
    private String json;
    private String actionbar;
    private String title = "", subtitle = "";
    private Duration fadeIn = Duration.ofSeconds(0);
    private Duration stay = Duration.ofSeconds(0);
    private Duration fadeOut = Duration.ofSeconds(0);

    private ActionMessage() {
    }

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
        if (player != null) {
            TagResolver[] tagResolvers = new TagResolver[] {
                    AdventureUtil.papiTagResolver(player, true),
            };

            if (chatMessages != null) {
                for (String message : chatMessages) {
                    AdventureUtil.sendMessage(player, message, tagResolvers);
                }
            }

            if (json != null) {
                String replaced = Handlers.getPlaceholderHandler().replace(player, json);
                Component component = GsonComponentSerializer.gson().deserialize(replaced);
                player.sendMessage(component);
            }

            if (actionbar != null) {
                AdventureUtil.sendActionbar(player, actionbar, tagResolvers);
            }

            if (!this.title.isEmpty() || !this.subtitle.isEmpty()) {
                AdventureUtil.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut, tagResolvers);
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionMessage> {

        @Override
        public ActionMessage deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            ActionMessage message = new ActionMessage();

            if (!node.isMap()) {
                message.setChatMessages(List.of(node.getString()));
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
                    throw new NodeSerializeException(node, "Cannot parse HOCON nodes as JSON objects. Check your menu file.");
                }
            }

            if (node.node("actionbar").rawValue() != null) {
                message.setActionbar(node.node("actionbar").getString());
            }

            message.setTitle(node.node("title").getString(""));
            message.setSubtitle(node.node("subtitle").getString(""));

            if (node.node("fadeIn").rawValue() != null) {
                String fadeInStr = node.node("fadeIn").getString();
                message.setFadeIn(new TypeDuration(fadeInStr).getDuration());
            }

            if (node.node("stay").rawValue() != null) {
                String stayStr = node.node("stay").getString();
                message.setStay(new TypeDuration(stayStr).getDuration());
            }

            if (node.node("fadeOut").rawValue() != null) {
                String fadeOutStr = node.node("fadeOut").getString();
                message.setFadeOut(new TypeDuration(fadeOutStr).getDuration());
            }

            return message;
        }
    }
}
