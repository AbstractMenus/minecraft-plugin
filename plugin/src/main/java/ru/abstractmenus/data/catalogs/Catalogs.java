package ru.abstractmenus.data.catalogs;

import ru.abstractmenus.api.Types;

public final class Catalogs {

    private Catalogs(){}

    public static void init() {
        Types.registerCatalog("iterator", IteratorCatalog.class, new IteratorCatalog.Serializer());
        Types.registerCatalog("players", PlayerCatalog.class, new PlayerCatalog.Serializer());
        Types.registerCatalog("entities", EntityCatalog.class, new EntityCatalog.Serializer());
        Types.registerCatalog("worlds", WorldCatalog.class, new WorldCatalog.Serializer());
        Types.registerCatalog("bungee_servers", ServerCatalog.class, new ServerCatalog.Serializer());
        Types.registerCatalog("slice", SliceCatalog.class, new SliceCatalog.Serializer());
    }

}
