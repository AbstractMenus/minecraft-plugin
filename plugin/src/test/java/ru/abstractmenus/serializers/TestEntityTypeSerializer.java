package ru.abstractmenus.serializers;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.source.ConfigSources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TestEntityTypeSerializer {

    private final EntityTypeSerializer serializer = new EntityTypeSerializer();

    @Test
    void knownConstantResolves() throws Exception {
        EntityType result = serializer.deserialize(EntityType.class, valueNode("val = ZOMBIE"));
        assertEquals(EntityType.ZOMBIE, result);
    }

    @Test
    void unknownConstantThrowsNodeSerializeException() {
        assertThrows(NodeSerializeException.class, () -> serializer.deserialize(
                EntityType.class, valueNode("val = \"NOT_AN_ENTITY\"")));
    }

    @Test
    void caseSensitiveMatch() {
        // Serializer does not uppercase — lowercase will miss the enum constant.
        assertThrows(NodeSerializeException.class, () -> serializer.deserialize(
                EntityType.class, valueNode("val = zombie")));
    }

    private static ConfigNode valueNode(String hocon) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(hocon.getBytes(StandardCharsets.UTF_8));
        ConfigNode root = ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", in))
                .build()
                .load();
        return root.node("val");
    }
}
