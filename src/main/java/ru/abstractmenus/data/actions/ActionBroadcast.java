package ru.abstractmenus.data.actions;

import com.google.gson.JsonElement;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.nms.actionbar.ActionBar;
import ru.abstractmenus.nms.title.Title;
import ru.abstractmenus.datatype.TypeInt;
import ru.abstractmenus.util.MiniMessageUtil;

import java.util.Collections;
import java.util.List;

public class ActionBroadcast implements Action {

    private List<String> chatMessages;
    private String json;
    private String actionbar;
    private String title = "", subtitle = "";
    private TypeInt fadeIn = new TypeInt(0);
    private TypeInt stay = new TypeInt(0);
    private TypeInt fadeOut = new TypeInt(0);

    private ActionBroadcast(){}

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

    private void setFadeIn(TypeInt fadeIn) {
        this.fadeIn = fadeIn;
    }

    private void setFadeOut(TypeInt fadeOut) {
        this.fadeOut = fadeOut;
    }

    private void setStay(TypeInt stay) {
        this.stay = stay;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if(player != null) {
            if (chatMessages != null) {
                List<String> replaced = Handlers.getPlaceholderHandler().replace(player, chatMessages);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    MiniMessageUtil.sendParsed(replaced, p);
                }
            }

            if(json != null) {
                BaseComponent[] component = ComponentSerializer.parse(
                        Handlers.getPlaceholderHandler().replace(player, json));

                if(component != null) {
                    for (Player p : Bukkit.getOnlinePlayers())
                        p.spigot().sendMessage(component);
                }
            }

            if(actionbar != null) {
                String replaced = Handlers.getPlaceholderHandler().replace(player, actionbar);
                ActionBar bar = ActionBar.create();

                for (Player p : Bukkit.getOnlinePlayers())
                    bar.send(p, MiniMessageUtil.parseToLegacy(replaced));
            }

            if (!this.title.isEmpty() || !this.subtitle.isEmpty()) {
                String title = MiniMessageUtil.parseToLegacy(
                        Handlers.getPlaceholderHandler().replace(player, this.title)
                );
                String subtitle = MiniMessageUtil.parseToLegacy(
                        Handlers.getPlaceholderHandler().replace(player, this.subtitle)
                );
                Title t = new Title(
                        title, subtitle,
                        fadeIn.getInt(player, menu),
                        stay.getInt(player, menu),
                        fadeOut.getInt(player, menu)
                );

                for (Player p : Bukkit.getOnlinePlayers())
                    t.send(p);
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionBroadcast> {

        @Override
        public ActionBroadcast deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            ActionBroadcast message = new ActionBroadcast();

            if(!node.isMap()) {
                message.setChatMessages(Collections.singletonList(Colors.of(node.getString())));
                return message;
            }

            if(node.node("chat").rawValue() != null) {
                message.setChatMessages(Colors.ofList(node.node("chat").getList(String.class)));
            }

            if(node.node("json").rawValue() != null) {
                ConfigNode jsonNode = node.node("json");
                JsonElement json = jsonNode.getValue(JsonElement.class);

                if(json != null) {
                    message.setJson(Colors.of(json.toString()));
                } else {
                    throw new NodeSerializeException(jsonNode, "Cannot parse HOCON nodes as JSON objects. Check your menu file.");
                }
            }

            if(node.node("actionbar").rawValue() != null){
                message.setActionbar(Colors.of(node.node("actionbar").getString()));
            }

            message.setTitle(Colors.of(node.node("title").getString("")));
            message.setSubtitle(Colors.of(node.node("subtitle").getString("")));
            message.setFadeIn(node.node("fadeIn").getValue(TypeInt.class, new TypeInt(10)));
            message.setStay(node.node("stay").getValue(TypeInt.class, new TypeInt(20)));
            message.setFadeOut(node.node("fadeOut").getValue(TypeInt.class, new TypeInt(10)));

            return message;
        }
    }
}
