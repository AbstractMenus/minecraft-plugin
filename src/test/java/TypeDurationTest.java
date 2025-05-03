import org.junit.jupiter.api.Test;
import ru.abstractmenus.datatype.TypeDuration;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TypeDurationTest {

    private final TypeDuration.Serializer serializer = new TypeDuration.Serializer();

    @Test
    public void testValidDurationDeserializationUsingGetValue() throws NodeSerializeException {
        ConfigNode node = new ConfigNode() {
            @Override
            public boolean isPrimitive() { return true; }

            @Override
            public String getString(String def) { return "1 h 15 min"; }

            @Override
            public String getString() { return "1 h 15 min"; }

            @Override
            public int getInt(int def) { return -1; }

            @Override
            public boolean isList() { return false; }

            @Override
            public boolean isMap() { return false; }

            @Override
            public boolean isNull() { return false; }

            @Override
            public Object rawValue() { return null; }

            @Override
            public <T> T getValue(Class<T> type, T def) { return type.cast("1 h 15 min"); }

            @Override
            public <T> java.util.List<T> getList(Class<T> var1) { return null; }

            @Override
            public java.util.List<ConfigNode> childrenList() { return null; }

            @Override
            public java.util.Map<String, ConfigNode> childrenMap() { return null; }

            @Override
            public boolean getBoolean(boolean def) { return false; }

            @Override
            public long getLong(long def) { return 0; }

            @Override
            public float getFloat(float def) { return 0; }

            @Override
            public double getDouble(double def) { return 0; }

            @Override
            public ru.abstractmenus.hocon.ConfigValue wrapped() { return null; }

            @Override
            public String key() { return null; }

            @Override
            public String[] path() { return new String[0]; }

            @Override
            public ConfigNode parent() { return null; }

            @Override
            public ConfigNode node(String... var1) { return null; }

            @Override
            public ConfigNode child(String var1) { return null; }

            @Override
            public boolean hasChildren() { return false; }
        };

        TypeDuration duration = serializer.deserialize(TypeDuration.class, node);
        assertNotNull(duration);
        assertEquals(Duration.ofHours(1).plusMinutes(15), duration.getDuration());
    }
}
