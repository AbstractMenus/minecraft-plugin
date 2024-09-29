package ru.abstractmenus.data.properties;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.ItemProperty;

import java.util.List;

public class PropKnowledgeBook implements ItemProperty {

    private final List<ShapedRecipe> recipes;

    private PropKnowledgeBook(List<ShapedRecipe> recipes){
        this.recipes = recipes;
    }

    @Override
    public boolean canReplaceMaterial() {
        return false;
    }

    @Override
    public boolean isApplyMeta() {
        return true;
    }

    @Override
    public void apply(ItemStack itemStack, ItemMeta itemMeta, Player player, Menu menu) {
        try{
            if (itemMeta instanceof KnowledgeBookMeta){
                KnowledgeBookMeta meta = (KnowledgeBookMeta) itemMeta;
                for (ShapedRecipe recipe : recipes) meta.addRecipe(recipe.getKey());
            }
        } catch (Exception e){
            /* Ignore */
        }
    }

    public static class Serializer implements NodeSerializer<PropKnowledgeBook> {

        @Override
        public PropKnowledgeBook deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            return new PropKnowledgeBook(node.getList(ShapedRecipe.class));
        }

    }
}
