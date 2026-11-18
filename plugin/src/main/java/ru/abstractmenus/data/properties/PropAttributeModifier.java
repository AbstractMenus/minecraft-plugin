package ru.abstractmenus.data.properties;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.abstractmenus.api.inventory.ItemProperty;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.List;
import java.util.Map;

public class PropAttributeModifier implements ItemProperty {

    private final List<Map.Entry<Attribute, AttributeModifier>> modifiers;

    private PropAttributeModifier(List<Map.Entry<Attribute, AttributeModifier>> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public boolean canReplaceMaterial() {
        return false;
    }

    @Override
    public boolean isApplyMeta() {
        return true;
    }

    @Override
    public void apply(ItemStack itemStack, ItemMeta meta, Player player, Menu menu) {
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers) {
            Collection<AttributeModifier> existing = meta.getAttributeModifiers(entry.getKey());
            if (existing != null && existing.stream().anyMatch(modifier -> modifier.getKey().equals(entry.getValue().getKey()))) continue;
            meta.addAttributeModifier(entry.getKey(), entry.getValue());
        }
    }


    public static class Serializer implements NodeSerializer<PropAttributeModifier> {

        @Override
        @SuppressWarnings("UnstableApiUsage")
        public PropAttributeModifier deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            List<ConfigNode> nodes = node.childrenList();
            List<Map.Entry<Attribute, AttributeModifier>> modifiers = new ArrayList<>();

            for (ConfigNode entry : nodes) {
                try {
                    String modifierType = entry.node("type").getString();
                    NamespacedKey attributeKey = NamespacedKey.fromString(modifierType.toLowerCase(Locale.ROOT));
                    if (attributeKey != null) {
                        String name = attributeKey.getKey().replaceFirst("^(generic|player|zombie)\\.", "");
                        attributeKey = new NamespacedKey(attributeKey.getNamespace(), name);
                    }
                    Attribute attribute = attributeKey == null ? null : Registry.ATTRIBUTE.get(attributeKey);
                    if (attribute == null) {
                        throw new NodeSerializeException(entry, "Unknown attribute in attributeModifier: " + modifierType);
                    }
                    double amount = entry.node("amount").getDouble(0);
                    AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(entry.node("operation").getString("add_number").toUpperCase());

                    EquipmentSlot slot = null;
                    String slotName = entry.node("slot").getString(null);
                    if (slotName != null) {
                        try {
                            slot = EquipmentSlot.valueOf(slotName.toUpperCase());
                        } catch (IllegalArgumentException ignored) {
                        }
                    }

                    String content = attributeKey + "|" + Double.toHexString(amount) + "|" + operation.name()
                            + "|" + (slot == null ? EquipmentSlotGroup.ANY : slot.getGroup());
                    UUID entryKey = UUID.nameUUIDFromBytes(content.getBytes(StandardCharsets.UTF_8));
                    NamespacedKey key = NamespacedKey.minecraft("custom_modifier_" + modifiers.size() + "_" + entryKey);
                    AttributeModifier modifier;

                    if (slot != null) {
                        modifier = new AttributeModifier(key, amount, operation, slot.getGroup());
                    } else {
                        modifier = new AttributeModifier(key, amount, operation);
                    }

                    modifiers.add(Map.entry(attribute, modifier));
                } catch (IllegalArgumentException e) {
                    throw new NodeSerializeException(entry, "Invalid attribute or operation in attributeModifier: " + e.getMessage());
                }
            }

            return new PropAttributeModifier(modifiers);
        }
    }
}