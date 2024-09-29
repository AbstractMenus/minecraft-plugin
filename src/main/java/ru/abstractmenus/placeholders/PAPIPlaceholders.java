package ru.abstractmenus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import ru.abstractmenus.placeholders.hooks.ActivatorPlaceholders;
import ru.abstractmenus.placeholders.hooks.HeadAnimPlaceholders;
import ru.abstractmenus.placeholders.hooks.CatalogPlaceholders;
import ru.abstractmenus.placeholders.hooks.VarPlaceholders;
import ru.abstractmenus.placeholders.hooks.dnd.ChangedItemPlaceholders;
import ru.abstractmenus.placeholders.hooks.dnd.PlacedItemPlaceholders;
import ru.abstractmenus.placeholders.hooks.dnd.TakenItemPlaceholders;

public final class PAPIPlaceholders {

    public static class ActivatorHook extends PlaceholderWrapper {
        public ActivatorHook() {
            super(new ActivatorPlaceholders(), "activator");
        }
    }

    public static class CatalogHook extends PlaceholderWrapper {
        public CatalogHook() {
            super(new CatalogPlaceholders(), "ctg");
        }
    }

    public static class HeadAnimHook extends PlaceholderWrapper {
        public HeadAnimHook() {
            super(new HeadAnimPlaceholders(), "hanim");
        }
    }

    public static class VarHook extends PlaceholderWrapper {
        public VarHook() {
            super(new VarPlaceholders.VarHook(), "var");
        }
    }

    public static class VarPlayerHook extends PlaceholderWrapper {
        public VarPlayerHook() {
            super(new VarPlaceholders.VarPlayerHook(), "varp");
        }
    }

    public static class VarTempHook extends PlaceholderWrapper {
        public VarTempHook() {
            super(new VarPlaceholders.VarTempHook(), "vart");
        }
    }

    public static class VarTempPlayerHook extends PlaceholderWrapper {
        public VarTempPlayerHook() {
            super(new VarPlaceholders.VarTempPlayerHook(), "varpt");
        }
    }

    public static class PlacedItemHook extends PlaceholderWrapper {
        public PlacedItemHook() {
            super(new PlacedItemPlaceholders(), "placed");
        }
    }

    public static class TakenItemHook extends PlaceholderWrapper {
        public TakenItemHook() {
            super(new TakenItemPlaceholders(), "taken");
        }
    }

    public static class MovedItemHook extends PlaceholderWrapper {
        public MovedItemHook() {
            super(new ChangedItemPlaceholders(), "changed");
        }
    }

    private static abstract class PlaceholderWrapper extends PlaceholderExpansion {

        private final PlaceholderHook wrapped;
        private final String id;

        public PlaceholderWrapper(PlaceholderHook wrapped, String id) {
            this.wrapped = wrapped;
            this.id = id;
        }

        @Override
        public String getIdentifier() {
            return id;
        }

        @Override
        public String getAuthor() {
            return "BrainRTP, Nanit";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onPlaceholderRequest(Player player, String placeholder) {
            return wrapped.replace(placeholder, player);
        }
    }
}
