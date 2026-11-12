package ru.abstractmenus.data.properties;

import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.source.ConfigSources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestPropMaxStackSize {

    @Test
    void rejectsOutOfRangeAtMenuLoad() throws Exception {
        for (int size : new int[]{0, -1, 100, Integer.MAX_VALUE}) {
            ConfigNode node = loadValue(size);
            assertThrows(NodeSerializeException.class, () -> new PropMaxStackSize.Serializer().deserialize(PropMaxStackSize.class, node));
        }
    }

    @Test
    void acceptsBoundsAndNormalStackSize() throws Exception {
        for (int size : new int[]{1, 64, 99}) {
            PropMaxStackSize property = new PropMaxStackSize.Serializer().deserialize(PropMaxStackSize.class, loadValue(size));
            ItemMeta meta = mock(ItemMeta.class);
            property.apply(null, meta, null, null);
            verify(meta).setMaxStackSize(size);
        }
    }

    private static ConfigNode loadValue(int size) throws Exception {
        return ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", new ByteArrayInputStream(("value: " + size).getBytes(StandardCharsets.UTF_8))))
                .build().load().node("value");
    }
}
