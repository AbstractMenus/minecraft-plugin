package ru.abstractmenus.serializers;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.source.ConfigSources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TestColorSerializer {

    private final ColorSerializer serializer = new ColorSerializer();

    @Test
    void hexStringParsedAsRgb() throws Exception {
        Color color = serializer.deserialize(Color.class, valueNode("val = \"#FF8800\""));
        assertEquals(0xFF, color.getRed());
        assertEquals(0x88, color.getGreen());
        assertEquals(0x00, color.getBlue());
    }

    @Test
    void hexStringParsedForLowerBoundBlack() throws Exception {
        Color color = serializer.deserialize(Color.class, valueNode("val = \"#000000\""));
        assertEquals(Color.BLACK, color);
    }

    @Test
    void commaTripleParsedAsRgb() throws Exception {
        Color color = serializer.deserialize(Color.class, valueNode("val = \"10, 20, 30\""));
        assertEquals(10, color.getRed());
        assertEquals(20, color.getGreen());
        assertEquals(30, color.getBlue());
    }

    @Test
    void commaTripleTrimsWhitespace() throws Exception {
        Color color = serializer.deserialize(Color.class, valueNode("val = \"  1  ,2  ,   3\""));
        assertEquals(1, color.getRed());
        assertEquals(2, color.getGreen());
        assertEquals(3, color.getBlue());
    }

    @Test
    void dyeColorConstantResolvesToMatchingColor() throws Exception {
        Color red = serializer.deserialize(Color.class, valueNode("val = RED"));
        assertEquals(DyeColor.RED.getColor(), red);
    }

    @Test
    void unknownDyeConstantThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> serializer.deserialize(Color.class, valueNode("val = \"NOT_A_DYE_COLOR\"")));
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
