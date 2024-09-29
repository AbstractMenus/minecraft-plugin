package ru.abstractmenus.serializers;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import ru.abstractmenus.util.bukkit.ItemUtil;
import ru.abstractmenus.api.inventory.Item;

import java.util.List;
import java.util.Map;

public class ShapedRecipeSerializer implements NodeSerializer<ShapedRecipe> {

    private final Plugin plugin;

    public ShapedRecipeSerializer(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public ShapedRecipe deserialize(Class<ShapedRecipe> type, ConfigNode node) throws NodeSerializeException {
        NamespacedKey key = new NamespacedKey(plugin, node.node("key").getString());
        ItemStack result = node.node("result").getValue(Item.class).build(null, null);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        List<String> shape = node.node("shape").getList(String.class);
        Map<String, ConfigNode> ingredients = node.node("ingredients").childrenMap();

        recipe.shape(shape.toArray(new String[0]));

        for (Map.Entry<String, ConfigNode> entry : ingredients.entrySet()) {
            try {
                recipe.setIngredient(entry.getKey().charAt(0), ItemUtil.get(entry.getValue().rawValue()));
            } catch (Throwable t) {
                throw new NodeSerializeException(entry.getValue(), "Cannot set ingredient. Maybe material not found");
            }
        }

        try {
            Bukkit.addRecipe(recipe);
        } catch (Exception e){
            /* Recipe already registered. Ignore */
        }

        return recipe;
    }
}
