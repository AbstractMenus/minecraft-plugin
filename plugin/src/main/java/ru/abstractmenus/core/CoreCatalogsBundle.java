package ru.abstractmenus.core;

import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.MenuExtension;
import ru.abstractmenus.data.catalogs.IteratorCatalog;
import ru.abstractmenus.data.catalogs.PlayerCatalog;
import ru.abstractmenus.data.catalogs.EntityCatalog;
import ru.abstractmenus.data.catalogs.WorldCatalog;
import ru.abstractmenus.data.catalogs.ServerCatalog;
import ru.abstractmenus.data.catalogs.SliceCatalog;

/**
 * Core catalog registrations. Mirrors {@code Catalogs.init()} before the
 * SPI refactor.
 */
final class CoreCatalogsBundle {

    void register(AbstractMenusApi api, MenuExtension owner) {
        api.catalogs().register("iterator", IteratorCatalog.class, new IteratorCatalog.Serializer(), owner);
        api.catalogs().register("players", PlayerCatalog.class, new PlayerCatalog.Serializer(), owner);
        api.catalogs().register("entities", EntityCatalog.class, new EntityCatalog.Serializer(), owner);
        api.catalogs().register("worlds", WorldCatalog.class, new WorldCatalog.Serializer(), owner);
        api.catalogs().register("bungee_servers", ServerCatalog.class, new ServerCatalog.Serializer(), owner);
        api.catalogs().register("slice", SliceCatalog.class, new SliceCatalog.Serializer(), owner);
    }
}
