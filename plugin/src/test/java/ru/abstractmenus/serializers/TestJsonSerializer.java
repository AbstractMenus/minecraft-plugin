package ru.abstractmenus.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.ConfigurationLoader;
import ru.abstractmenus.hocon.api.source.ConfigSources;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TestJsonSerializer {

    private final JsonSerializer serializer = new JsonSerializer();

    @Test
    void primitiveStringPassedThroughAsJson() throws Exception {
        JsonElement result = serializer.deserialize(JsonElement.class,
                valueNode("val = \"{\\\"a\\\":1}\""));
        assertTrue(result.isJsonObject());
        assertEquals(1, result.getAsJsonObject().get("a").getAsInt());
    }

    @Test
    void integerNodeBecomesJsonPrimitive() throws Exception {
        JsonElement result = serializer.deserialize(JsonElement.class,
                valueNode("val { k = 42 }").node("k"));
        assertInstanceOf(JsonPrimitive.class, result);
        assertEquals(42, result.getAsInt());
    }

    @Test
    void booleanNodeBecomesJsonPrimitive() throws Exception {
        JsonElement result = serializer.deserialize(JsonElement.class,
                valueNode("val { k = true }").node("k"));
        assertTrue(result.getAsBoolean());
    }

    @Test
    void doubleNodeBecomesJsonPrimitive() throws Exception {
        JsonElement result = serializer.deserialize(JsonElement.class,
                valueNode("val { k = 3.14 }").node("k"));
        assertEquals(3.14, result.getAsDouble(), 0.0);
    }

    @Test
    void mapNodeBecomesJsonObject() throws Exception {
        JsonElement result = serializer.deserialize(JsonElement.class,
                valueNode("val { a = 1, b = \"text\" }"));
        assertInstanceOf(JsonObject.class, result);
        JsonObject obj = result.getAsJsonObject();
        assertEquals(1, obj.get("a").getAsInt());
        assertEquals("text", obj.get("b").getAsString());
    }

    @Test
    void listNodeBecomesJsonArray() throws Exception {
        JsonElement result = serializer.deserialize(JsonElement.class,
                valueNode("val = [1, 2, 3]"));
        assertInstanceOf(JsonArray.class, result);
        JsonArray arr = result.getAsJsonArray();
        assertEquals(3, arr.size());
        assertEquals(1, arr.get(0).getAsInt());
        assertEquals(3, arr.get(2).getAsInt());
    }

    @Test
    void nestedObjectAndArrayRoundtrip() throws Exception {
        JsonElement result = serializer.deserialize(JsonElement.class,
                valueNode("val { nested { items = [\"x\", \"y\"] } }"));
        JsonObject nested = result.getAsJsonObject()
                .get("nested").getAsJsonObject();
        JsonArray items = nested.get("items").getAsJsonArray();
        assertEquals(2, items.size());
        assertEquals("x", items.get(0).getAsString());
    }

    @Test
    void heterogeneousListKeepsTypes() throws Exception {
        JsonElement result = serializer.deserialize(JsonElement.class,
                valueNode("val = [1, \"str\", true]"));
        JsonArray arr = result.getAsJsonArray();
        assertEquals(1, arr.get(0).getAsInt());
        assertEquals("str", arr.get(1).getAsString());
        assertTrue(arr.get(2).getAsBoolean());
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
