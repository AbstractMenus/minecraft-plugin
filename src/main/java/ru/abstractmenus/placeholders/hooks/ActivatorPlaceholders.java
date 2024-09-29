package ru.abstractmenus.placeholders.hooks;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.Activator;
import ru.abstractmenus.api.Types;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.placeholders.PlaceholderHook;
import ru.abstractmenus.services.MenuManager;

public class ActivatorPlaceholders implements PlaceholderHook {

    @Override
    public String replace(String placeholder, Player player) {
        Menu menu = MenuManager.instance().getOpenedMenu(player);

        if (menu != null && menu.getActivatedBy().isPresent()) {
            Activator activator = menu.getActivatedBy().get();

            if (placeholder.equalsIgnoreCase("name"))
                return Types.getActivatorName(activator.getClass());

            if (menu.getContext().isPresent()) {
                ValueExtractor extractor = activator.getValueExtractor();

                if (extractor != null)
                    return extractor.extract(menu.getContext().get(), placeholder);
            }
        }

        return null;
    }

}
