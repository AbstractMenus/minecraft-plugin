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

public class TestTypeMaterial {

    @Test
    public void testMaterialConstructor() {
        TypeMaterial type = new TypeMaterial(Material.STONE);
        assertEquals(Material.STONE, type.getNative());
    }

    @Test
    public void testStringConstructor() {
        TypeMaterial type = new TypeMaterial("STONE");
        assertNull(type.getNative());
    }

    @Test
    public void testDeserializeValidMaterial() throws Exception {
        ConfigNode node = loadNode("val = STONE");
        TypeMaterial.Serializer serializer = new TypeMaterial.Serializer();

        TypeMaterial result = serializer.deserialize(TypeMaterial.class, node);

        assertEquals(Material.STONE, result.getNative());
    }

    @Test
    public void testDeserializeMaterialCaseInsensitive() throws Exception {
        ConfigNode node = loadNode("val = diamond_sword");
        TypeMaterial.Serializer serializer = new TypeMaterial.Serializer();

        TypeMaterial result = serializer.deserialize(TypeMaterial.class, node);

        assertEquals(Material.DIAMOND_SWORD, result.getNative());
    }

    @Test
    public void testDeserializeInvalidMaterialThrows() throws Exception {
        ConfigNode node = loadNode("val = NOT_A_REAL_MATERIAL");
        TypeMaterial.Serializer serializer = new TypeMaterial.Serializer();

        assertThrows(NodeSerializeException.class, () ->
                serializer.deserialize(TypeMaterial.class, node));
    }

    @Test
    public void testDeserializePlaceholderReturnsStringBased() throws Exception {
        ConfigNode node = loadNode("val = \"%player_material%\"");
        TypeMaterial.Serializer serializer = new TypeMaterial.Serializer();

        TypeMaterial result = serializer.deserialize(TypeMaterial.class, node);

        assertNull(result.getNative());
    }

    private ConfigNode loadNode(String hocon) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(hocon.getBytes(StandardCharsets.UTF_8));
        ConfigNode root = ConfigurationLoader.builder()
                .source(ConfigSources.inputStream("test", in))
                .build()
                .load();
        return root.node("val");
    }
}
