package ru.abstractmenus.data.actions;

import ru.abstractmenus.api.Types;
import ru.abstractmenus.data.actions.var.*;
import ru.abstractmenus.data.actions.varp.*;
import ru.abstractmenus.data.actions.wrappers.ActionBulk;
import ru.abstractmenus.data.actions.wrappers.ActionDelay;
import ru.abstractmenus.data.actions.wrappers.ActionPlayerScope;
import ru.abstractmenus.data.actions.wrappers.ActionRandomActions;

public final class MenuActions {

    private MenuActions(){}

    public static void init() {
        Types.registerAction("closeMenu", ActionMenuClose.class, new ActionMenuClose.Serializer());
        Types.registerAction("openMenu", ActionMenuOpen.class, new ActionMenuOpen.Serializer());
        Types.registerAction("openMenuCtx", ActionMenuOpenCtx.class, new ActionMenuOpenCtx.Serializer());
        Types.registerAction("playerScope", ActionPlayerScope.class, new ActionPlayerScope.Serializer());

        Types.registerAction("addGroup", ActionGroupAdd.class, new ActionGroupAdd.Serializer());
        Types.registerAction("bungeeConnect", ActionBungeeConnect.class, new ActionBungeeConnect.Serializer());
        Types.registerAction("refreshMenu", ActionMenuRefresh.class, new ActionMenuRefresh.Serializer());
        Types.registerAction("command", ActionCommand.class, new ActionCommand.Serializer());
        Types.registerAction("giveLevel", ActionLevelGive.class, new ActionLevelGive.Serializer());
        Types.registerAction("giveMoney", ActionMoneyGive.class, new ActionMoneyGive.Serializer());
        Types.registerAction("givePermission", ActionPermissionGive.class, new ActionPermissionGive.Serializer());
        Types.registerAction("lpMetaSet", ActionLuckPermsMetaSet.class, new ActionLuckPermsMetaSet.Serializer());
        Types.registerAction("lpMetaRemove", ActionLuckPermsMetaRemove.class, new ActionLuckPermsMetaRemove.Serializer());
        Types.registerAction("giveXp", ActionXpGive.class, new ActionXpGive.Serializer());
        Types.registerAction("itemAdd", ActionItemAdd.class, new ActionItemAdd.Serializer());
        Types.registerAction("itemRemove", ActionItemRemove.class, new ActionItemRemove.Serializer());
        Types.registerAction("itemClear", ActionItemClear.class, new ActionItemClear.Serializer());
        Types.registerAction("inventoryClear", ActionInventoryClear.class, new ActionInventoryClear.Serializer());
        Types.registerAction("message", ActionMessage.class, new ActionMessage.Serializer());
        Types.registerAction("broadcast", ActionBroadcast.class, new ActionBroadcast.Serializer());
        Types.registerAction("miniMessage", ActionMiniMessage.class, new ActionMiniMessage.Serializer());
        Types.registerAction("potionEffect", ActionPotionEffect.class, new ActionPotionEffect.Serializer());
        Types.registerAction("removePotionEffect", ActionPotionEffectRemove.class, new ActionPotionEffectRemove.Serializer());
        Types.registerAction("removeGroup", ActionGroupRemove.class, new ActionGroupRemove.Serializer());
        Types.registerAction("removePermission", ActionPermissionRemove.class, new ActionPermissionRemove.Serializer());
        Types.registerAction("setFoodLevel", ActionFoodLevelSet.class, new ActionFoodLevelSet.Serializer());
        Types.registerAction("setHealth", ActionHealthSet.class, new ActionHealthSet.Serializer());
        Types.registerAction("sound", ActionSound.class, new ActionSound.Serializer());

        try {
            // SoundCategory missing on legacy Bukkit
            Types.registerAction("customSound", ActionSoundCustom.class, new ActionSoundCustom.Serializer());
        } catch (Throwable ignore) {}

        Types.registerAction("takeLevel", ActionLevelTake.class, new ActionLevelTake.Serializer());
        Types.registerAction("takeMoney", ActionMoneyTake.class, new ActionMoneyTake.Serializer());
        Types.registerAction("takeXp", ActionXpTake.class, new ActionXpTake.Serializer());
        Types.registerAction("teleport", ActionTeleport.class, new ActionTeleport.Serializer());
        Types.registerAction("openBook", ActionBookOpen.class, new ActionBookOpen.Serializer());
        Types.registerAction("delay", ActionDelay.class, new ActionDelay.Serializer());
        Types.registerAction("setSkin", ActionSkinSet.class, new ActionSkinSet.Serializer());
        Types.registerAction("resetSkin", ActionSkinReset.class, new ActionSkinReset.Serializer());
        Types.registerAction("addRecipe", ActionRecipeAdd.class, new ActionRecipeAdd.Serializer());
        Types.registerAction("pageNext", ActionPageNext.class, new ActionPageNext.Serializer());
        Types.registerAction("pagePrev", ActionPagePrev.class, new ActionPagePrev.Serializer());
        Types.registerAction("bulk", ActionBulk.class, new ActionBulk.Serializer());
        Types.registerAction("setProperty", ActionPropertySet.class, new ActionPropertySet.Serializer());
        Types.registerAction("remProperty", ActionPropertyRemove.class, new ActionPropertyRemove.Serializer());
        Types.registerAction("refreshItem", ActionItemRefresh.class, new ActionItemRefresh.Serializer());
        Types.registerAction("randActions", ActionRandomActions.class, new ActionRandomActions.Serializer());
        Types.registerAction("playerChat", ActionPlayerChat.class, new ActionPlayerChat.Serializer());
        Types.registerAction("setGamemode", ActionGameModeSet.class, new ActionGameModeSet.Serializer());
        Types.registerAction("inputChat", ActionInputChat.class, new ActionInputChat.Serializer());
        Types.registerAction("setButton", ActionButtonSet.class, new ActionButtonSet.Serializer());
        Types.registerAction("removeButton", ActionButtonRemove.class, new ActionButtonRemove.Serializer());
        Types.registerAction("removePlaced", ActionPlacedItemRemove.class, new ActionPlacedItemRemove.Serializer());
        Types.registerAction("placeItem", ActionPlaceItem.class, new ActionPlaceItem.Serializer());

        Types.registerAction("setVar", ActionVarSet.class, new ActionVarSet.Serializer());
        Types.registerAction("removeVar", ActionVarRem.class, new ActionVarRem.Serializer());
        Types.registerAction("incVar", ActionVarInc.class, new ActionVarInc.Serializer());
        Types.registerAction("decVar", ActionVarDec.class, new ActionVarDec.Serializer());
        Types.registerAction("mulVar", ActionVarMul.class, new ActionVarMul.Serializer());
        Types.registerAction("divVar", ActionVarDiv.class, new ActionVarDiv.Serializer());

        Types.registerAction("setVarp", ActionVarpSet.class, new ActionVarpSet.Serializer());
        Types.registerAction("removeVarp", ActionVarpRem.class, new ActionVarpRem.Serializer());
        Types.registerAction("incVarp", ActionVarpInc.class, new ActionVarpInc.Serializer());
        Types.registerAction("decVarp", ActionVarpDec.class, new ActionVarpDec.Serializer());
        Types.registerAction("mulVarp", ActionVarpMul.class, new ActionVarpMul.Serializer());
        Types.registerAction("divVarp", ActionVarpDiv.class, new ActionVarpDiv.Serializer());

        Types.registerAction("print", ActionLog.class, new ActionLog.Serializer());
    }
}
