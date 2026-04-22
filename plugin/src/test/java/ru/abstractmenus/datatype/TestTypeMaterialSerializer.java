package ru.abstractmenus.datatype;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.source.ConfigSources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: loads HOCON config from string, deserializes TypeMaterial via Serializer.
 */
public class TestTypeMaterialSerializer {

    @Test
    public void testDeserializeFromHocon() throws Exception {
        ConfigNode root = loadHocon("material = DIAMOND");
        ConfigNode materialNode = root.node("material");

        TypeMaterial.Serializer serializer = new TypeMaterial.Serializer();
        TypeMaterial result = serializer.deserialize(TypeMaterial.class, materialNode);

        assertEquals(Material.DIAMOND, result.getNative());
    }

    @Test
    public void testDeserializeFromHoconLowerCase() throws Exception {
        ConfigNode root = loadHocon("material = oak_log");
        ConfigNode materialNode = root.node("material");

        TypeMaterial.Serializer serializer = new TypeMaterial.Serializer();
        TypeMaterial result = serializer.deserialize(TypeMaterial.class, materialNode);

        assertEquals(Material.OAK_LOG, result.getNative());
    }

    @Test
    public void testDeserializeInvalidFromHocon() throws Exception {
        ConfigNode root = loadHocon("material = FAKE_BLOCK_999");
        ConfigNode materialNode = root.node("material");

        TypeMaterial.Serializer serializer = new TypeMaterial.Serializer();

        assertThrows(NodeSerializeException.class, () ->
                serializer.deserialize(TypeMaterial.class, materialNode));
    }

    @Test
    public void testDeserializePlaceholderFromHocon() throws Exception {
        ConfigNode root = loadHocon("material = \"%some_placeholder%\"");
        ConfigNode materialNode = root.node("material");

        TypeMaterial.Serializer serializer = new TypeMaterial.Serializer();
        TypeMaterial result = serializer.deserialize(TypeMaterial.class, materialNode);

        // Placeholder value — no native Material, will be resolved at runtime
        assertNull(result.getNative());
    }

    private ConfigNode loadHocon(String content) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", in))
                .build()
                .load();
    }
}
