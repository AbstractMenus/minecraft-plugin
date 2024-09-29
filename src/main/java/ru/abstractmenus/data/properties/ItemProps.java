package ru.abstractmenus.data.properties;

import ru.abstractmenus.api.Types;

public final class ItemProps {

    public static final String BINDINGS = "bindings";

    public static void init() {
        Types.registerItemProperty("material", PropMaterial.class, new PropMaterial.Serializer());
        Types.registerItemProperty("texture", PropTexture.class, new PropTexture.Serializer());
        Types.registerItemProperty("skullOwner", PropSkullOwner.class, new PropSkullOwner.Serializer());
        Types.registerItemProperty("hdb", PropHDB.class, new PropHDB.Serializer());
        Types.registerItemProperty("mmoitem", PropMmoItem.class, new PropMmoItem.Serializer());
        Types.registerItemProperty("itemsAdder", PropItemsAdder.class, new PropItemsAdder.Serializer());
        Types.registerItemProperty("oraxen", PropOraxen.class, new PropOraxen.Serializer());
        Types.registerItemProperty("equipItem", PropEquipItem.class, new PropEquipItem.Serializer());
        Types.registerItemProperty("serialized", PropSerialized.class, new PropSerialized.Serializer());

        Types.registerItemProperty("name", PropName.class, new PropName.Serializer());
        Types.registerItemProperty("data", PropData.class, new PropData.Serializer());
        Types.registerItemProperty("count", PropCount.class, new PropCount.Serializer());
        Types.registerItemProperty("lore", PropLore.class, new PropLore.Serializer());
        Types.registerItemProperty("glow", PropGlow.class, new PropGlow.Serializer());
        Types.registerItemProperty("enchantments", PropEnchantments.class, new PropEnchantments.Serializer());
        Types.registerItemProperty("color", PropColor.class, new PropColor.Serializer());
        Types.registerItemProperty("flags", PropFlags.class, new PropFlags.Serializer());
        Types.registerItemProperty("unbreakable", PropUnbreakable.class, new PropUnbreakable.Serializer());
        Types.registerItemProperty("potionData", PropPotionData.class, new PropPotionData.Serializer());
        Types.registerItemProperty("fireworkData", PropFireworkData.class, new PropFireworkData.Serializer());
        Types.registerItemProperty("bookData", PropBookData.class, new PropBookData.Serializer());
        Types.registerItemProperty("bannerData", PropBannerData.class, new PropBannerData.Serializer());
        Types.registerItemProperty("shieldData", PropShieldData.class, new PropShieldData.Serializer());
        Types.registerItemProperty("model", PropModel.class, new PropModel.Serializer());
        Types.registerItemProperty("enchantStore", PropEnchantStore.class, new PropEnchantStore.Serializer());
        Types.registerItemProperty("recipes", PropKnowledgeBook.class, new PropKnowledgeBook.Serializer());
        Types.registerItemProperty("damage", PropDamage.class, new PropDamage.Serializer());
        Types.registerItemProperty("nbt", PropNbt.class, new PropNbt.Serializer());
        Types.registerItemProperty(BINDINGS, PropBindings.class, new PropBindings.Serializer());

        Types.registerItemProperty("nameLight", PropNameLight.class, new PropNameLight.Serializer());
        Types.registerItemProperty("loreLight", PropLoreLight.class, new PropLoreLight.Serializer());
    }

    private ItemProps(){}
}
