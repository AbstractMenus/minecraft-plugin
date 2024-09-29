package ru.abstractmenus.data.activators;

import ru.abstractmenus.AbstractMenus;
import ru.abstractmenus.api.Types;

public final class Activators {

    private Activators() { }

    public static void init() {
        Types.registerActivator("command", OpenCommand.class, new OpenCommand.Serializer());
        Types.registerActivator("chat", OpenChat.class, new OpenChat.Serializer());
        Types.registerActivator("containsChat", OpenChatContains.class, new OpenChatContains.Serializer());
        Types.registerActivator("join", OpenJoin.class, new OpenJoin.Serializer());
        Types.registerActivator("clickEntity", OpenClickEntity.class, new OpenClickEntity.Serializer());
        Types.registerActivator("shiftClickEntity", OpenShiftClickEntity.class, new OpenShiftClickEntity.Serializer());
        Types.registerActivator("clickItem", OpenClickItem.class, new OpenClickItem.Serializer());
        Types.registerActivator("button", OpenButton.class, new OpenButton.Serializer());
        Types.registerActivator("lever", OpenLever.class, new OpenLever.Serializer());
        Types.registerActivator("plate", OpenPlate.class, new OpenPlate.Serializer());
        Types.registerActivator("table", OpenSign.class, new OpenSign.Serializer());
        Types.registerActivator("clickBlock", OpenClickBlock.class, new OpenClickBlock.Serializer());
        Types.registerActivator("clickBlockType", OpenClickBlockType.class, new OpenClickBlockType.Serializer());
        Types.registerActivator("swapItems", OpenSwapItems.class, new OpenSwapItems.Serializer());

        if(AbstractMenus.checkDependency("Citizens")){
            Types.registerActivator("clickNPC", OpenClickNPC.class, new OpenClickNPC.Serializer());
        }

        if(AbstractMenus.checkDependency("WorldGuard")){
            Types.registerActivator("regionJoin", OpenRegionEnter.class, new OpenRegionEnter.Serializer());
            Types.registerActivator("regionLeave", OpenRegionLeave.class, new OpenRegionLeave.Serializer());
        }
    }

}
