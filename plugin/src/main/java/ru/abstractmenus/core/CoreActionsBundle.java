package ru.abstractmenus.core;

import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.data.actions.var.*;
import ru.abstractmenus.data.actions.varp.*;
import ru.abstractmenus.data.actions.wrappers.ActionBulk;
import ru.abstractmenus.data.actions.wrappers.ActionDelay;
import ru.abstractmenus.data.actions.wrappers.ActionPlayerScope;
import ru.abstractmenus.data.actions.wrappers.ActionRandomActions;
import ru.abstractmenus.data.actions.ActionMenuClose;
import ru.abstractmenus.data.actions.ActionMenuOpen;
import ru.abstractmenus.data.actions.ActionMenuOpenCtx;
import ru.abstractmenus.data.actions.ActionGroupAdd;
import ru.abstractmenus.data.actions.ActionBungeeConnect;
import ru.abstractmenus.data.actions.ActionMenuRefresh;
import ru.abstractmenus.data.actions.ActionCommand;
import ru.abstractmenus.data.actions.ActionLevelGive;
import ru.abstractmenus.data.actions.ActionMoneyGive;
import ru.abstractmenus.data.actions.ActionPermissionGive;
import ru.abstractmenus.data.actions.ActionLuckPermsMetaSet;
import ru.abstractmenus.data.actions.ActionLuckPermsMetaRemove;
import ru.abstractmenus.data.actions.ActionXpGive;
import ru.abstractmenus.data.actions.ActionItemAdd;
import ru.abstractmenus.data.actions.ActionItemRemove;
import ru.abstractmenus.data.actions.ActionItemClear;
import ru.abstractmenus.data.actions.ActionInventoryClear;
import ru.abstractmenus.data.actions.ActionMessage;
import ru.abstractmenus.data.actions.ActionBroadcast;
import ru.abstractmenus.data.actions.ActionMiniMessage;
import ru.abstractmenus.data.actions.ActionPotionEffect;
import ru.abstractmenus.data.actions.ActionPotionEffectRemove;
import ru.abstractmenus.data.actions.ActionGroupRemove;
import ru.abstractmenus.data.actions.ActionPermissionRemove;
import ru.abstractmenus.data.actions.ActionFoodLevelSet;
import ru.abstractmenus.data.actions.ActionHealthSet;
import ru.abstractmenus.data.actions.ActionSound;
import ru.abstractmenus.data.actions.ActionSoundCustom;
import ru.abstractmenus.data.actions.ActionLevelTake;
import ru.abstractmenus.data.actions.ActionMoneyTake;
import ru.abstractmenus.data.actions.ActionXpTake;
import ru.abstractmenus.data.actions.ActionTeleport;
import ru.abstractmenus.data.actions.ActionBookOpen;
import ru.abstractmenus.data.actions.ActionSkinSet;
import ru.abstractmenus.data.actions.ActionSkinReset;
import ru.abstractmenus.data.actions.ActionRecipeAdd;
import ru.abstractmenus.data.actions.ActionPageNext;
import ru.abstractmenus.data.actions.ActionPagePrev;
import ru.abstractmenus.data.actions.ActionPropertySet;
import ru.abstractmenus.data.actions.ActionPropertyRemove;
import ru.abstractmenus.data.actions.ActionItemRefresh;
import ru.abstractmenus.data.actions.ActionPlayerChat;
import ru.abstractmenus.data.actions.ActionGameModeSet;
import ru.abstractmenus.data.actions.ActionInputChat;
import ru.abstractmenus.data.actions.ActionButtonSet;
import ru.abstractmenus.data.actions.ActionButtonRemove;
import ru.abstractmenus.data.actions.ActionPlacedItemRemove;
import ru.abstractmenus.data.actions.ActionPlaceItem;
import ru.abstractmenus.data.actions.ActionLog;

/**
 * Core action registrations. Mirrors {@code MenuActions.init()} before the
 * SPI refactor.
 */
final class CoreActionsBundle {

    void register(AbstractMenusApi api, MenuExtension owner) {
        api.actions().register("closeMenu", ActionMenuClose.class, new ActionMenuClose.Serializer(), owner);
        api.actions().register("openMenu", ActionMenuOpen.class, new ActionMenuOpen.Serializer(), owner);
        api.actions().register("openMenuCtx", ActionMenuOpenCtx.class, new ActionMenuOpenCtx.Serializer(), owner);
        api.actions().register("playerScope", ActionPlayerScope.class, new ActionPlayerScope.Serializer(), owner);

        api.actions().register("addGroup", ActionGroupAdd.class, new ActionGroupAdd.Serializer(), owner);
        api.actions().register("bungeeConnect", ActionBungeeConnect.class, new ActionBungeeConnect.Serializer(), owner);
        api.actions().register("refreshMenu", ActionMenuRefresh.class, new ActionMenuRefresh.Serializer(), owner);
        api.actions().register("command", ActionCommand.class, new ActionCommand.Serializer(), owner);
        api.actions().register("giveLevel", ActionLevelGive.class, new ActionLevelGive.Serializer(), owner);
        api.actions().register("giveMoney", ActionMoneyGive.class, new ActionMoneyGive.Serializer(), owner);
        api.actions().register("givePermission", ActionPermissionGive.class, new ActionPermissionGive.Serializer(), owner);
        api.actions().register("lpMetaSet", ActionLuckPermsMetaSet.class, new ActionLuckPermsMetaSet.Serializer(), owner);
        api.actions().register("lpMetaRemove", ActionLuckPermsMetaRemove.class, new ActionLuckPermsMetaRemove.Serializer(), owner);
        api.actions().register("giveXp", ActionXpGive.class, new ActionXpGive.Serializer(), owner);
        api.actions().register("itemAdd", ActionItemAdd.class, new ActionItemAdd.Serializer(), owner);
        api.actions().register("itemRemove", ActionItemRemove.class, new ActionItemRemove.Serializer(), owner);
        api.actions().register("itemClear", ActionItemClear.class, new ActionItemClear.Serializer(), owner);
        api.actions().register("inventoryClear", ActionInventoryClear.class, new ActionInventoryClear.Serializer(), owner);
        api.actions().register("message", ActionMessage.class, new ActionMessage.Serializer(), owner);
        api.actions().register("broadcast", ActionBroadcast.class, new ActionBroadcast.Serializer(), owner);
        api.actions().register("miniMessage", ActionMiniMessage.class, new ActionMiniMessage.Serializer(), owner);
        api.actions().register("potionEffect", ActionPotionEffect.class, new ActionPotionEffect.Serializer(), owner);
        api.actions().register("removePotionEffect", ActionPotionEffectRemove.class, new ActionPotionEffectRemove.Serializer(), owner);
        api.actions().register("removeGroup", ActionGroupRemove.class, new ActionGroupRemove.Serializer(), owner);
        api.actions().register("removePermission", ActionPermissionRemove.class, new ActionPermissionRemove.Serializer(), owner);
        api.actions().register("setFoodLevel", ActionFoodLevelSet.class, new ActionFoodLevelSet.Serializer(), owner);
        api.actions().register("setHealth", ActionHealthSet.class, new ActionHealthSet.Serializer(), owner);
        api.actions().register("sound", ActionSound.class, new ActionSound.Serializer(), owner);

        try {
            // SoundCategory missing on legacy Bukkit
            api.actions().register("customSound", ActionSoundCustom.class, new ActionSoundCustom.Serializer(), owner);
        } catch (Throwable ignore) {
        }

        api.actions().register("takeLevel", ActionLevelTake.class, new ActionLevelTake.Serializer(), owner);
        api.actions().register("takeMoney", ActionMoneyTake.class, new ActionMoneyTake.Serializer(), owner);
        api.actions().register("takeXp", ActionXpTake.class, new ActionXpTake.Serializer(), owner);
        api.actions().register("teleport", ActionTeleport.class, new ActionTeleport.Serializer(), owner);
        api.actions().register("openBook", ActionBookOpen.class, new ActionBookOpen.Serializer(), owner);
        api.actions().register("delay", ActionDelay.class, new ActionDelay.Serializer(), owner);
        api.actions().register("setSkin", ActionSkinSet.class, new ActionSkinSet.Serializer(), owner);
        api.actions().register("resetSkin", ActionSkinReset.class, new ActionSkinReset.Serializer(), owner);
        api.actions().register("addRecipe", ActionRecipeAdd.class, new ActionRecipeAdd.Serializer(), owner);
        api.actions().register("pageNext", ActionPageNext.class, new ActionPageNext.Serializer(), owner);
        api.actions().register("pagePrev", ActionPagePrev.class, new ActionPagePrev.Serializer(), owner);
        api.actions().register("bulk", ActionBulk.class, new ActionBulk.Serializer(), owner);
        api.actions().register("setProperty", ActionPropertySet.class, new ActionPropertySet.Serializer(), owner);
        api.actions().register("remProperty", ActionPropertyRemove.class, new ActionPropertyRemove.Serializer(), owner);
        api.actions().register("refreshItem", ActionItemRefresh.class, new ActionItemRefresh.Serializer(), owner);
        api.actions().register("randActions", ActionRandomActions.class, new ActionRandomActions.Serializer(), owner);
        api.actions().register("playerChat", ActionPlayerChat.class, new ActionPlayerChat.Serializer(), owner);
        api.actions().register("setGamemode", ActionGameModeSet.class, new ActionGameModeSet.Serializer(), owner);
        api.actions().register("inputChat", ActionInputChat.class, new ActionInputChat.Serializer(), owner);
        api.actions().register("setButton", ActionButtonSet.class, new ActionButtonSet.Serializer(), owner);
        api.actions().register("removeButton", ActionButtonRemove.class, new ActionButtonRemove.Serializer(), owner);
        api.actions().register("removePlaced", ActionPlacedItemRemove.class, new ActionPlacedItemRemove.Serializer(), owner);
        api.actions().register("placeItem", ActionPlaceItem.class, new ActionPlaceItem.Serializer(), owner);

        api.actions().register("setVar", ActionVarSet.class, new ActionVarSet.Serializer(), owner);
        api.actions().register("removeVar", ActionVarRem.class, new ActionVarRem.Serializer(), owner);
        api.actions().register("incVar", ActionVarInc.class, new ActionVarInc.Serializer(), owner);
        api.actions().register("decVar", ActionVarDec.class, new ActionVarDec.Serializer(), owner);
        api.actions().register("mulVar", ActionVarMul.class, new ActionVarMul.Serializer(), owner);
        api.actions().register("divVar", ActionVarDiv.class, new ActionVarDiv.Serializer(), owner);

        api.actions().register("setVarp", ActionVarpSet.class, new ActionVarpSet.Serializer(), owner);
        api.actions().register("removeVarp", ActionVarpRem.class, new ActionVarpRem.Serializer(), owner);
        api.actions().register("incVarp", ActionVarpInc.class, new ActionVarpInc.Serializer(), owner);
        api.actions().register("decVarp", ActionVarpDec.class, new ActionVarpDec.Serializer(), owner);
        api.actions().register("mulVarp", ActionVarpMul.class, new ActionVarpMul.Serializer(), owner);
        api.actions().register("divVarp", ActionVarpDiv.class, new ActionVarpDiv.Serializer(), owner);

        api.actions().register("print", ActionLog.class, new ActionLog.Serializer(), owner);
    }
}
