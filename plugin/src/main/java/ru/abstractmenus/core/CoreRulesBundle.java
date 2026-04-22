package ru.abstractmenus.core;

import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.data.rules.RuleChance;
import ru.abstractmenus.data.rules.RuleIf;
import ru.abstractmenus.data.rules.RuleFoodLevel;
import ru.abstractmenus.data.rules.RuleGameMode;
import ru.abstractmenus.data.rules.RuleGroup;
import ru.abstractmenus.data.rules.RuleHealth;
import ru.abstractmenus.data.rules.RuleInventoryItem;
import ru.abstractmenus.data.rules.RuleHeldItem;
import ru.abstractmenus.data.rules.RuleLevel;
import ru.abstractmenus.data.rules.RuleMoney;
import ru.abstractmenus.data.rules.RuleOnline;
import ru.abstractmenus.data.rules.RuleBungeeOnline;
import ru.abstractmenus.data.rules.RulePermission;
import ru.abstractmenus.data.rules.RuleWorld;
import ru.abstractmenus.data.rules.RuleXp;
import ru.abstractmenus.data.rules.RuleExistVar;
import ru.abstractmenus.data.rules.RuleExistVarp;
import ru.abstractmenus.data.rules.RuleFreeSlot;
import ru.abstractmenus.data.rules.RuleFreeSlotCount;
import ru.abstractmenus.data.rules.RuleRegion;
import ru.abstractmenus.data.rules.RuleJS;
import ru.abstractmenus.data.rules.RuleBungeeIsOnline;
import ru.abstractmenus.data.rules.RulePlayerIsOnline;
import ru.abstractmenus.data.rules.RulePlacedItem;
import ru.abstractmenus.data.rules.logical.RuleOneOf;
import ru.abstractmenus.data.rules.logical.RuleOr;
import ru.abstractmenus.data.rules.logical.RuleAnd;
import ru.abstractmenus.data.rules.logical.RulePlayerScope;

/**
 * Core rule registrations. Mirrors {@code MenuRules.init()} before the
 * SPI refactor.
 */
final class CoreRulesBundle {

    void register(AbstractMenusApi api, MenuExtension owner) {
        api.rules().register("chance", RuleChance.class, new RuleChance.Serializer(), owner);
        api.rules().register("if", RuleIf.class, new RuleIf.Serializer(), owner);
        api.rules().register("foodLevel", RuleFoodLevel.class, new RuleFoodLevel.Serializer(), owner);
        api.rules().register("gamemode", RuleGameMode.class, new RuleGameMode.Serializer(), owner);
        api.rules().register("group", RuleGroup.class, new RuleGroup.Serializer(), owner);
        api.rules().register("health", RuleHealth.class, new RuleHealth.Serializer(), owner);
        api.rules().register("inventoryItems", RuleInventoryItem.class, new RuleInventoryItem.Serializer(), owner);
        api.rules().register("heldItem", RuleHeldItem.class, new RuleHeldItem.Serializer(), owner);
        api.rules().register("level", RuleLevel.class, new RuleLevel.Serializer(), owner);
        api.rules().register("money", RuleMoney.class, new RuleMoney.Serializer(), owner);
        api.rules().register("online", RuleOnline.class, new RuleOnline.Serializer(), owner);
        api.rules().register("bungeeOnline", RuleBungeeOnline.class, new RuleBungeeOnline.Serializer(), owner);
        api.rules().register("permission", RulePermission.class, new RulePermission.Serializer(), owner);
        api.rules().register("world", RuleWorld.class, new RuleWorld.Serializer(), owner);
        api.rules().register("xp", RuleXp.class, new RuleXp.Serializer(), owner);
        api.rules().register("existVar", RuleExistVar.class, new RuleExistVar.Serializer(), owner);
        api.rules().register("existVarp", RuleExistVarp.class, new RuleExistVarp.Serializer(), owner);
        api.rules().register("freeSlot", RuleFreeSlot.class, new RuleFreeSlot.Serializer(), owner);
        api.rules().register("freeSlotCount", RuleFreeSlotCount.class, new RuleFreeSlotCount.Serializer(), owner);
        api.rules().register("region", RuleRegion.class, new RuleRegion.Serializer(), owner);
        api.rules().register("js", RuleJS.class, new RuleJS.Serializer(), owner);
        api.rules().register("bungeeIsOnline", RuleBungeeIsOnline.class, new RuleBungeeIsOnline.Serializer(), owner);
        api.rules().register("playerIsOnline", RulePlayerIsOnline.class, new RulePlayerIsOnline.Serializer(), owner);
        api.rules().register("placedItem", RulePlacedItem.class, new RulePlacedItem.Serializer(), owner);

        api.rules().register("and", RuleAnd.class, new RuleAnd.Serializer(), owner);
        api.rules().register("or", RuleOr.class, new RuleOr.Serializer(), owner);
        api.rules().register("oneof", RuleOneOf.class, new RuleOneOf.Serializer(), owner);
        api.rules().register("playerScope", RulePlayerScope.class, new RulePlayerScope.Serializer(), owner);
    }
}
