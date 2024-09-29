package ru.abstractmenus.data.actions;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ShapedRecipe;
import ru.abstractmenus.api.Action;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;

import java.util.List;

public class ActionRecipeAdd implements Action {

    private final List<ShapedRecipe> recipes;

    private ActionRecipeAdd(List<ShapedRecipe> recipes){
        this.recipes = recipes;
    }

    @Override
    public void activate(Player player, Menu menu, Item clickedItem) {
        try{
            for (ShapedRecipe recipe : recipes){
                player.discoverRecipe(recipe.getKey());
            }
        } catch (Exception e){
            /* Ignore */
        }
    }

    public static class Serializer implements NodeSerializer<ActionRecipeAdd> {

        @Override
        public ActionRecipeAdd deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new ActionRecipeAdd(node.getList(ShapedRecipe.class));
        }

    }
}
