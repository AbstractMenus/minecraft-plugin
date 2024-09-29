package ru.abstractmenus.data.rules;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.Rule;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.api.Handlers;
import ru.abstractmenus.api.Logger;

import javax.script.*;

public class RuleJS implements Rule {

    private static final ScriptEngine ENGINE;

    static {
        ScriptEngineManager manager = new ScriptEngineManager();

        try {
            // Try register Nashorn engine if it loaded
            Class<?> factoryClass = Class.forName("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory");
            ScriptEngineFactory factory = (ScriptEngineFactory) factoryClass.newInstance();
            manager.registerEngineName("Nashorn", factory);
        } catch (Exception e) {
            // Ignore
        }

        ENGINE = manager.getEngineByName("Nashorn");
    }

    private final String js;
    private final Bindings bindings;

    private RuleJS(String js){
        this.js = js;
        this.bindings = ENGINE.createBindings();
    }

    @Override
    public boolean check(Player player, Menu menu, Item clickedItem) {
        try{
            Object result = ENGINE.eval(Handlers.getPlaceholderHandler().replace(player, js), bindings);
            return result.toString().equals("true");
        } catch (ScriptException e){
            Logger.severe("Cannot execute JavaScript code: " + e.getMessage());
        }
        return false;
    }

    public static class Serializer implements NodeSerializer<RuleJS> {

        @Override
        public RuleJS deserialize(Class type, ConfigNode node) {
            return new RuleJS(node.getString());
        }
    }
}
