package ru.abstractmenus.serializers;

import ru.abstractmenus.Constants;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.datatype.TypeSlot;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import org.bukkit.event.inventory.ClickType;

import ru.abstractmenus.menu.item.MenuItem;
import ru.abstractmenus.menu.item.SimpleItem;
import ru.abstractmenus.data.rules.logical.RuleAnd;
import ru.abstractmenus.menu.item.InventoryItem;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.Types;

import java.util.*;

public class ItemSerializer implements NodeSerializer<Item> {

    @Override
    public Item deserialize(Class<Item> type, ConfigNode node) throws NodeSerializeException {
        Item item = new SimpleItem();

        // Parse menu item data

        if(node.node("slot").rawValue() != null){
            item = new InventoryItem();
            ((InventoryItem)item).setSlot(node.node("slot").getValue(TypeSlot.class));
        }

        if(node.node("rules").rawValue() != null
                || node.node("mrules").rawValue() != null
                || node.node("click").rawValue() != null){

            MenuItem menuItem = new MenuItem();

            if(node.node("slot").rawValue() != null){
                menuItem.setSlot(node.node("slot").getValue(TypeSlot.class));
            }

            if(node.node("rules").rawValue() != null){
                menuItem.setShowRules(node.node("rules").getValue(RuleAnd.class));
            }

            if(node.node("mrules").rawValue() != null){
                menuItem.setMinorRules(node.node("mrules").getValue(RuleAnd.class));
            }

            if(node.node("click").rawValue() != null){
                menuItem.setClicks(getClicks(menuItem, node.node("click")));
            }

            if (node.node("clickCooldown") != null) {
                menuItem.setClickCooldown(node.node("clickCooldown").getInt());
            }

            item = menuItem;
        }

        // Parse item data

        Map<String, ConfigNode> children = node.childrenMap();

        for (Map.Entry<String, ConfigNode> entry : children.entrySet()){
            String key = entry.getKey();
            Class<? extends ItemProperty> token = Types.getItemPropertyType(key);

            if (token != null) {
                item.addProperty(key, entry.getValue().getValue(token));
            }
        }

        return item;
    }

    private Map<ClickType, Actions> getClicks(MenuItem item, ConfigNode node) throws NodeSerializeException {
        Map<ClickType, Actions> map = new HashMap<>();
        Actions anyActions = new Actions();
        Map<String, ConfigNode> children = node.childrenMap();

        for (Map.Entry<String, ConfigNode> entry : children.entrySet()) {
            String key = entry.getKey();
            ClickType clickType = getClickType(key);

            if(clickType != null) {
                Actions clickActions = entry.getValue().getValue(Actions.class);
                map.put(clickType, clickActions);
                continue;
            }

            Actions.Serializer.deserializeAction(key, entry.getValue(), anyActions);
        }

        if (node.node(Constants.FIELD_RULES).rawValue() != null) {
            anyActions.setRules(node.node(Constants.FIELD_RULES).getValue(RuleAnd.class));
        }

        if (node.node(Constants.FIELD_ACTIONS).rawValue() != null) {
            anyActions.setActions(node.node(Constants.FIELD_ACTIONS).getValue(Actions.class));
        }

        if (node.node(Constants.FIELD_DENY_ACTIONS).rawValue() != null) {
            anyActions.setDenyActions(node.node(Constants.FIELD_DENY_ACTIONS).getValue(Actions.class));
        }

        item.setAnyClickActions(anyActions);

        return map;
    }

    private ClickType getClickType(String str){
        try{
            return ClickType.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e){
            return null;
        }
    }

}
