package ru.abstractmenus.data.actions;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;

import java.util.ArrayList;
import java.util.List;

public class ActionPotionEffectRemove implements Action {

    private List<PotionEffectType> types;

    private void setTypes(List<PotionEffectType> types){
        this.types = types;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if(types != null){
            for(PotionEffectType type : types){
                if(type != null){
                    player.removePotionEffect(type);
                }
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionPotionEffectRemove> {

        @Override
        public ActionPotionEffectRemove deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            ActionPotionEffectRemove action = new ActionPotionEffectRemove();
            List<String> list = node.getList(String.class);
            List<PotionEffectType> types = new ArrayList<>();

            for(String name : list){
                types.add(PotionEffectType.getByName(name));
            }

            action.setTypes(types);

            return action;
        }
    }
}
