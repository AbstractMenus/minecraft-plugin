package ru.abstractmenus.data.properties;

import ru.abstractmenus.data.Actions;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.Types;
import ru.abstractmenus.menu.item.SimpleItem;
import ru.abstractmenus.data.rules.logical.RuleAnd;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PropBindings implements ItemProperty {

    private final List<BindGroup> groups;

    private PropBindings(List<BindGroup> groups) {
        this.groups = groups;
    }

    @Override
    public boolean canReplaceMaterial() {
        return false;
    }

    @Override
    public boolean isApplyMeta() {
        return false;
    }

    @Override
    public void apply(ItemStack itemStack, ItemMeta meta, Player player, Menu menu) {
        for (BindGroup group : groups) {
            if (group.getRules().check(player, menu, null)) {
                SimpleItem.applyProperties(group.getMatProps(), itemStack, player, menu);
                SimpleItem.applyProperties(group.getProps(), itemStack, player, menu);
            }
        }
    }

    public static class BindGroup {

        private final List<ItemProperty> matProps;
        private final List<ItemProperty> props;
        private final Rule rules;

        public BindGroup(List<ItemProperty> props, Rule rules) {
            this.rules = rules;
            this.matProps = new LinkedList<>();
            this.props = new LinkedList<>();

            for (ItemProperty prop : props) {
                if (prop.canReplaceMaterial()) {
                    this.matProps.add(prop);
                } else {
                    this.props.add(prop);
                }
            }
        }

        public List<ItemProperty> getMatProps() {
            return matProps;
        }

        public List<ItemProperty> getProps() {
            return props;
        }

        public Rule getRules() {
            return rules;
        }
    }

    public static class BindGroupSerializer implements NodeSerializer<BindGroup> {

        @Override
        public BindGroup deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            if (node.node("rules").rawValue() == null) {
                throw new NodeSerializeException(node, "Bind rules always should be specified");
            }

            Rule rules = node.node("rules").getValue(RuleAnd.class);
            List<ItemProperty> properties = new LinkedList<>();
            Map<String, ConfigNode> propsMap = node.node("props").childrenMap();

            for (Map.Entry<String, ConfigNode> entry : propsMap.entrySet()){
                String key = entry.getKey();

                if (Actions.isReserved(key)) continue;

                Class<? extends ItemProperty> token = Types.getItemPropertyType(key);

                if (token != null) {
                    properties.add(entry.getValue().getValue(token));
                }
            }

            return new BindGroup(properties, rules);
        }

    }

    public static class Serializer implements NodeSerializer<PropBindings> {

        @Override
        public PropBindings deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropBindings(node.getList(BindGroup.class));
        }

    }
}
