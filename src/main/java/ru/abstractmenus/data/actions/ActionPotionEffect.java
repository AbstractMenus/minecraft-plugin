package ru.abstractmenus.data.actions;


import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;

import java.util.List;

public class ActionPotionEffect implements Action {

    private List<PotionEffect> effects;

    private void setEffects(List<PotionEffect> effects){
        this.effects = effects;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        if(effects != null){
            for(PotionEffect effect : effects){
                player.addPotionEffect(effect);
            }
        }
    }

    public static class Serializer implements NodeSerializer<ActionPotionEffect> {

        @Override
        public ActionPotionEffect deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            ActionPotionEffect action = new ActionPotionEffect();
            action.setEffects(node.getList(PotionEffect.class));
            return action;
        }

    }
}
