package ru.abstractmenus.datatype;

import lombok.Getter;
import org.bukkit.entity.Player;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.api.Handlers;

public abstract class DataType implements Cloneable {

    @Getter
    private final String value;

    DataType(String value) {
        this.value = value;
    }

    public String replaceFor(Player player, Menu menu) {
        return Handlers.getPlaceholderHandler().replace(player, value);
    }

    public static boolean hasPlaceholder(String string) {
        return string.contains("%") || (string.contains("${") && string.contains("}"));
    }
}
