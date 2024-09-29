package ru.abstractmenus.serializers.menu;

import org.bukkit.event.inventory.InventoryType;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.data.Actions;
import ru.abstractmenus.data.rules.logical.RuleAnd;
import ru.abstractmenus.datatype.TypeSlot;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.Types;
import ru.abstractmenus.menu.AbstractMenu;
import ru.abstractmenus.menu.SimpleMenu;
import ru.abstractmenus.menu.animated.AnimatedMenu;
import ru.abstractmenus.menu.generated.GeneratedMenu;
import ru.abstractmenus.services.MenuManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseMenuSerializer implements NodeSerializer<Menu> {

    private static final int MENU_ROW_SIZE = 9;

    private static final MenuSerializer<SimpleMenu> simpleMenuSerializer;
    private static final MenuSerializer<AnimatedMenu> animatedMenuSerializer;
    private static final MenuSerializer<GeneratedMenu> generatedMenuSerializer;

    static {
        simpleMenuSerializer = new SimpleMenuSerializer();
        animatedMenuSerializer = new AnimatedMenuSerializer();
        generatedMenuSerializer = new GeneratedMenuSerializer();
    }

    @Override
    public Menu deserialize(Class type, ConfigNode node) throws NodeSerializeException {
        String title = Colors.of(node.node("title").getString());
        int size = node.node("size").getInt(1) * MENU_ROW_SIZE;

        if (size < 9 || size > 54)
            throw new NodeSerializeException("Menu size cannot be more 6 and less that 1");

        AbstractMenu menu;

        if (node.node("frames").rawValue() != null) {
            // Frames defined. This is an animated menu
            menu = animatedMenuSerializer.deserialize(node, title, size);
            MenuManager.instance().startUpdateTask();
        } else if(node.node("catalog").rawValue() != null){
            // Catalog defined. This is a template menu
            menu = generatedMenuSerializer.deserialize(node, title, size);
        } else {
            // No special parameters. This is simple menu
            menu = simpleMenuSerializer.deserialize(node, title, size);
        }

        deserializeBase(menu, node);

        return menu;
    }

    private void deserializeBase(AbstractMenu menu, ConfigNode node) throws NodeSerializeException {
        String type = node.node("type").getString();

        if (type != null) {
            menu.setType(InventoryType.valueOf(type.toUpperCase()));
        }

        if (node.node("updateInterval").rawValue() != null) {
            long interval = node.node("updateInterval").getLong(-1L);
            menu.setUpdateInterval(interval);
            if (interval > -1) MenuManager.instance().startUpdateTask();
        }

        if (node.node("rules").rawValue() != null) {
            menu.setOpenRules(node.node("rules").getValue(RuleAnd.class));
        }

        if (node.node("preOpenActions").rawValue() != null) {
            menu.setPreOpenActions(node.node("preOpenActions").getValue(Actions.class));
        }

        if (node.node("openActions").rawValue() != null) {
            menu.setOpenActions(node.node("openActions").getValue(Actions.class));
        }

        if (node.node("postOpenActions").rawValue() != null) {
            menu.setPostOpenActions(node.node("postOpenActions").getValue(Actions.class));
        }

        if (node.node("denyActions").rawValue() != null) {
            menu.setDenyActions(node.node("denyActions").getValue(Actions.class));
        }

        if (node.node("closeActions").rawValue() != null) {
            menu.setCloseActions(node.node("closeActions").getValue(Actions.class));
        }

        if (node.node("updateActions").rawValue() != null) {
            menu.setUpdateActions(node.node("updateActions").getValue(Actions.class));
        }

        if (node.node("draggable").rawValue() != null) {
            menu.setDraggableSlots(node.node("draggable").getValue(TypeSlot.class));
        }

        if (node.node("onPlaceItem").rawValue() != null) {
            menu.setOnPlaceItem(node.node("onPlaceItem").getValue(Actions.class));
        }

        if (node.node("onTakeItem").rawValue() != null) {
            menu.setOnTakeItem(node.node("onTakeItem").getValue(Actions.class));
        }

        if (node.node("onDragItem").rawValue() != null) {
            menu.setOnDragItem(node.node("onDragItem").getValue(Actions.class));
        }

        if (node.node("activators").rawValue() != null) {
            menu.setActivators(getActivators(node.node("activators")));
        }
    }

    private List<Activator> getActivators(ConfigNode node) throws NodeSerializeException {
        List<Activator> list = new ArrayList<>();
        Map<String, ConfigNode> activators = node.childrenMap();

        for(Map.Entry<String, ConfigNode> entry : activators.entrySet()) {
            String key = entry.getKey();
            Class<? extends Activator> type = Types.getActivator(key);

            if(type != null) {
                Activator activator = entry.getValue().getValue(type);
                list.add(activator);
            } else{
                throw new NodeSerializeException(entry.getValue(), "Error while serializing activator " + key);
            }
        }

        return list;
    }
}
