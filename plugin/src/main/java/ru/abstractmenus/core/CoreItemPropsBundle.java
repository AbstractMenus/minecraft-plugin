package ru.abstractmenus.core;

import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.data.properties.PropMaterial;
import ru.abstractmenus.data.properties.PropTexture;
import ru.abstractmenus.data.properties.PropSkullOwner;
import ru.abstractmenus.data.properties.PropHDB;
import ru.abstractmenus.data.properties.PropMmoItem;
import ru.abstractmenus.data.properties.PropItemsAdder;
import ru.abstractmenus.data.properties.PropOraxen;
import ru.abstractmenus.data.properties.PropEquipItem;
import ru.abstractmenus.data.properties.PropSerialized;
import ru.abstractmenus.data.properties.PropName;
import ru.abstractmenus.data.properties.PropData;
import ru.abstractmenus.data.properties.PropCount;
import ru.abstractmenus.data.properties.PropLore;
import ru.abstractmenus.data.properties.PropGlow;
import ru.abstractmenus.data.properties.PropEnchantments;
import ru.abstractmenus.data.properties.PropColor;
import ru.abstractmenus.data.properties.PropFlags;
import ru.abstractmenus.data.properties.PropUnbreakable;
import ru.abstractmenus.data.properties.PropPotionData;
import ru.abstractmenus.data.properties.PropFireworkData;
import ru.abstractmenus.data.properties.PropAttributeModifier;
import ru.abstractmenus.data.properties.PropBookData;
import ru.abstractmenus.data.properties.PropBannerData;
import ru.abstractmenus.data.properties.PropShieldData;
import ru.abstractmenus.data.properties.PropModel;
import ru.abstractmenus.data.properties.PropEnchantStore;
import ru.abstractmenus.data.properties.PropKnowledgeBook;
import ru.abstractmenus.data.properties.PropDamage;
import ru.abstractmenus.data.properties.PropNbt;
import ru.abstractmenus.data.properties.PropBindings;
import ru.abstractmenus.data.properties.PropNameLight;
import ru.abstractmenus.data.properties.PropLoreLight;

/**
 * Core item property registrations. Mirrors {@code ItemProps.init()} before
 * the SPI refactor.
 */
final class CoreItemPropsBundle {

    void register(AbstractMenusApi api, MenuExtension owner) {
        api.itemProperties().register("material", PropMaterial.class, new PropMaterial.Serializer(), owner);
        api.itemProperties().register("texture", PropTexture.class, new PropTexture.Serializer(), owner);
        api.itemProperties().register("skullOwner", PropSkullOwner.class, new PropSkullOwner.Serializer(), owner);
        api.itemProperties().register("hdb", PropHDB.class, new PropHDB.Serializer(), owner);
        api.itemProperties().register("mmoitem", PropMmoItem.class, new PropMmoItem.Serializer(), owner);
        api.itemProperties().register("itemsAdder", PropItemsAdder.class, new PropItemsAdder.Serializer(), owner);
        api.itemProperties().register("oraxen", PropOraxen.class, new PropOraxen.Serializer(), owner);
        api.itemProperties().register("equipItem", PropEquipItem.class, new PropEquipItem.Serializer(), owner);
        api.itemProperties().register("serialized", PropSerialized.class, new PropSerialized.Serializer(), owner);

        api.itemProperties().register("name", PropName.class, new PropName.Serializer(), owner);
        api.itemProperties().register("data", PropData.class, new PropData.Serializer(), owner);
        api.itemProperties().register("count", PropCount.class, new PropCount.Serializer(), owner);
        api.itemProperties().register("lore", PropLore.class, new PropLore.Serializer(), owner);
        api.itemProperties().register("glow", PropGlow.class, new PropGlow.Serializer(), owner);
        api.itemProperties().register("enchantments", PropEnchantments.class, new PropEnchantments.Serializer(), owner);
        api.itemProperties().register("color", PropColor.class, new PropColor.Serializer(), owner);
        api.itemProperties().register("flags", PropFlags.class, new PropFlags.Serializer(), owner);
        api.itemProperties().register("unbreakable", PropUnbreakable.class, new PropUnbreakable.Serializer(), owner);
        api.itemProperties().register("potionData", PropPotionData.class, new PropPotionData.Serializer(), owner);
        api.itemProperties().register("fireworkData", PropFireworkData.class, new PropFireworkData.Serializer(), owner);
        api.itemProperties().register("attributeModifier", PropAttributeModifier.class, new PropAttributeModifier.Serializer(), owner);
        api.itemProperties().register("bookData", PropBookData.class, new PropBookData.Serializer(), owner);
        api.itemProperties().register("bannerData", PropBannerData.class, new PropBannerData.Serializer(), owner);
        api.itemProperties().register("shieldData", PropShieldData.class, new PropShieldData.Serializer(), owner);
        api.itemProperties().register("model", PropModel.class, new PropModel.Serializer(), owner);
        api.itemProperties().register("enchantStore", PropEnchantStore.class, new PropEnchantStore.Serializer(), owner);
        api.itemProperties().register("recipes", PropKnowledgeBook.class, new PropKnowledgeBook.Serializer(), owner);
        api.itemProperties().register("damage", PropDamage.class, new PropDamage.Serializer(), owner);
        api.itemProperties().register("nbt", PropNbt.class, new PropNbt.Serializer(), owner);
        api.itemProperties().register(CoreItemPropertyKeys.BINDINGS, PropBindings.class, new PropBindings.Serializer(), owner);

        api.itemProperties().register("nameLight", PropNameLight.class, new PropNameLight.Serializer(), owner);
        api.itemProperties().register("loreLight", PropLoreLight.class, new PropLoreLight.Serializer(), owner);
    }
}
