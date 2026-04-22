package ru.abstractmenus.core;

import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.data.activators.OpenButton;
import ru.abstractmenus.data.activators.OpenChat;
import ru.abstractmenus.data.activators.OpenChatContains;
import ru.abstractmenus.data.activators.OpenClickBlock;
import ru.abstractmenus.data.activators.OpenClickBlockType;
import ru.abstractmenus.data.activators.OpenClickEntity;
import ru.abstractmenus.data.activators.OpenClickItem;
import ru.abstractmenus.data.activators.OpenClickNPC;
import ru.abstractmenus.data.activators.OpenCommand;
import ru.abstractmenus.data.activators.OpenJoin;
import ru.abstractmenus.data.activators.OpenLever;
import ru.abstractmenus.data.activators.OpenPlate;
import ru.abstractmenus.data.activators.OpenRegionEnter;
import ru.abstractmenus.data.activators.OpenRegionLeave;
import ru.abstractmenus.data.activators.OpenShiftClickEntity;
import ru.abstractmenus.data.activators.OpenSign;
import ru.abstractmenus.data.activators.OpenSwapItems;

/**
 * Core activator registrations. Mirrors {@code Activators.init()} before
 * the SPI refactor.
 */
final class CoreActivatorsBundle {

    void register(AbstractMenusApi api, MenuExtension owner) {
        api.activators().register("command", OpenCommand.class, new OpenCommand.Serializer(), owner);
        api.activators().register("chat", OpenChat.class, new OpenChat.Serializer(), owner);
        api.activators().register("containsChat", OpenChatContains.class, new OpenChatContains.Serializer(), owner);
        api.activators().register("join", OpenJoin.class, new OpenJoin.Serializer(), owner);
        api.activators().register("clickEntity", OpenClickEntity.class, new OpenClickEntity.Serializer(), owner);
        api.activators().register("shiftClickEntity", OpenShiftClickEntity.class, new OpenShiftClickEntity.Serializer(), owner);
        api.activators().register("clickItem", OpenClickItem.class, new OpenClickItem.Serializer(), owner);
        api.activators().register("button", OpenButton.class, new OpenButton.Serializer(), owner);
        api.activators().register("lever", OpenLever.class, new OpenLever.Serializer(), owner);
        api.activators().register("plate", OpenPlate.class, new OpenPlate.Serializer(), owner);
        api.activators().register("table", OpenSign.class, new OpenSign.Serializer(), owner);
        api.activators().register("clickBlock", OpenClickBlock.class, new OpenClickBlock.Serializer(), owner);
        api.activators().register("clickBlockType", OpenClickBlockType.class, new OpenClickBlockType.Serializer(), owner);
        api.activators().register("swapItems", OpenSwapItems.class, new OpenSwapItems.Serializer(), owner);

        if (AbstractMenus.checkDependency("Citizens")) {
            api.activators().register("clickNPC", OpenClickNPC.class, new OpenClickNPC.Serializer(), owner);
        }

        if (AbstractMenus.checkDependency("WorldGuard")) {
            api.activators().register("regionJoin", OpenRegionEnter.class, new OpenRegionEnter.Serializer(), owner);
            api.activators().register("regionLeave", OpenRegionLeave.class, new OpenRegionLeave.Serializer(), owner);
        }
    }
}
