package ru.abstractmenus.data.properties;

import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import ru.abstractmenus.api.AbstractMenusApi;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.menu.item.SimpleItem;
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

    @Test
    void invalidPlaceholderValueWarnsAndKeepsTheItemAndExistingLimit() throws Exception {
        MockBukkit.mock();
        AbstractMenusApi api = mock(AbstractMenusApi.class, RETURNS_DEEP_STUBS);
        try (var apiLookup = mockStatic(AbstractMenusApi.class); var logging = mockStatic(Logger.class)) {
            apiLookup.when(AbstractMenusApi::get).thenReturn(api);
            for (int size : new int[]{0, 100}) {
                when(api.providers().placeholders().resolve().replace(null, "%stack_size%")).thenReturn(String.valueOf(size));
                SimpleItem template = new SimpleItem();
                template.addProperty("initialStackSize", new PropMaxStackSize.Serializer().deserialize(PropMaxStackSize.class, loadValue(32)));
                template.addProperty("maxStackSize", new PropMaxStackSize.Serializer().deserialize(PropMaxStackSize.class, loadNode("\"%stack_size%\"")));

                var item = template.build(null, null);

                assertNotNull(item);
                assertEquals(32, item.getMaxStackSize());
                logging.verify(() -> Logger.warning("Skipping maxStackSize value outside 1..99: " + size));
            }
        } finally {
            MockBukkit.unmock();
        }
    }

    private static ConfigNode loadValue(int size) throws Exception {
        return loadNode(String.valueOf(size));
    }

    private static ConfigNode loadNode(String value) throws Exception {
        return ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", new ByteArrayInputStream(("value: " + value).getBytes(StandardCharsets.UTF_8))))
                .build().load().node("value");
    }
}
