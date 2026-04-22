package ru.abstractmenus.data.rules;


import ru.abstractmenus.api.Types;
import ru.abstractmenus.data.rules.logical.RuleOneOf;
import ru.abstractmenus.data.rules.logical.RuleOr;
import ru.abstractmenus.data.rules.logical.RuleAnd;
import ru.abstractmenus.data.rules.logical.RulePlayerScope;

public final class MenuRules {

    private MenuRules(){}

    public static void init(){
        Types.registerRule("chance", RuleChance.class, new RuleChance.Serializer());
        Types.registerRule("if", RuleIf.class, new RuleIf.Serializer());
        Types.registerRule("foodLevel", RuleFoodLevel.class, new RuleFoodLevel.Serializer());
        Types.registerRule("gamemode", RuleGameMode.class, new RuleGameMode.Serializer());
        Types.registerRule("group", RuleGroup.class, new RuleGroup.Serializer());
        Types.registerRule("health", RuleHealth.class, new RuleHealth.Serializer());
        Types.registerRule("inventoryItems", RuleInventoryItem.class, new RuleInventoryItem.Serializer());
        Types.registerRule("heldItem", RuleHeldItem.class, new RuleHeldItem.Serializer());
        Types.registerRule("level", RuleLevel.class, new RuleLevel.Serializer());
        Types.registerRule("money", RuleMoney.class, new RuleMoney.Serializer());
        Types.registerRule("online", RuleOnline.class, new RuleOnline.Serializer());
        Types.registerRule("bungeeOnline", RuleBungeeOnline.class, new RuleBungeeOnline.Serializer());
        Types.registerRule("permission", RulePermission.class, new RulePermission.Serializer());
        Types.registerRule("world", RuleWorld.class, new RuleWorld.Serializer());
        Types.registerRule("xp", RuleXp.class, new RuleXp.Serializer());
        Types.registerRule("existVar", RuleExistVar.class, new RuleExistVar.Serializer());
        Types.registerRule("existVarp", RuleExistVarp.class, new RuleExistVarp.Serializer());
        Types.registerRule("freeSlot", RuleFreeSlot.class, new RuleFreeSlot.Serializer());
        Types.registerRule("freeSlotCount", RuleFreeSlotCount.class, new RuleFreeSlotCount.Serializer());
        Types.registerRule("region", RuleRegion.class, new RuleRegion.Serializer());
        Types.registerRule("js", RuleJS.class, new RuleJS.Serializer());
        Types.registerRule("bungeeIsOnline", RuleBungeeIsOnline.class, new RuleBungeeIsOnline.Serializer());
        Types.registerRule("playerIsOnline", RulePlayerIsOnline.class, new RulePlayerIsOnline.Serializer());
        Types.registerRule("placedItem", RulePlacedItem.class, new RulePlacedItem.Serializer());

        Types.registerRule("and", RuleAnd.class, new RuleAnd.Serializer());
        Types.registerRule("or", RuleOr.class, new RuleOr.Serializer());
        Types.registerRule("oneof", RuleOneOf.class, new RuleOneOf.Serializer());
        Types.registerRule("playerScope", RulePlayerScope.class, new RulePlayerScope.Serializer());
    }

}
