package ru.abstractmenus.placeholders.hooks;

import org.bukkit.entity.Player;
import ru.abstractmenus.api.ValueExtractor;
import ru.abstractmenus.api.inventory.Menu;
import ru.abstractmenus.menu.generated.GeneratedMenu;
import ru.abstractmenus.placeholders.PlaceholderHook;
import ru.abstractmenus.services.MenuManager;

public class CatalogPlaceholders implements PlaceholderHook {

    @Override
    public String replace(String placeholder, Player player) {
        Menu menu = MenuManager.instance().getOpenedMenu(player);

        if (menu instanceof GeneratedMenu) {
            GeneratedMenu tmpl = (GeneratedMenu) menu;

            String result = switch (placeholder) {
                case "page" -> String.valueOf(tmpl.getPage());
                case "pages" -> String.valueOf(tmpl.getPages());
                case "page_next" -> String.valueOf(tmpl.getPage() + 1);
                case "page_prev" -> String.valueOf(tmpl.getPage() - 1);
                case "elements" -> String.valueOf(tmpl.getSnapshot().size());
                default -> null;
            };

            if (result != null) {
                return result;
            }

            ValueExtractor extractor = tmpl.getCatalog().extractor();
            Object obj = tmpl.getCurrentObject();
            return extractor.extract(obj, placeholder);
        }

        return null;
    }
}
