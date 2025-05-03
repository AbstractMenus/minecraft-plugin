package ru.abstractmenus.datatype;

import io.github.blackbaroness.durationserializer.DurationSerializer;
import io.github.blackbaroness.durationserializer.DurationFormats;
import io.github.blackbaroness.durationserializer.format.InvalidFormatException;
import lombok.Getter;
import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;

import java.time.Duration;

@Getter
public class TypeDuration extends DataType {

    private final Duration duration;

    public TypeDuration(Duration duration) {
        super(null);
        this.duration = duration;
    }

    public TypeDuration(String value) {
        super(value);
        this.duration = parseDuration(value);
    }

    private Duration parseDuration(String value) {
        try {
            return DurationSerializer.deserialize(value, DurationFormats.allBundled());
        } catch (IllegalArgumentException | InvalidFormatException e) {
            throw new InvalidDurationFormatException(value, e);
        }
    }

    public static class InvalidDurationFormatException extends RuntimeException {
        public InvalidDurationFormatException(String value, Throwable cause) {
            super("Invalid duration format: " + value, cause);
        }
    }

    public static class Serializer implements NodeSerializer<TypeDuration> {

        @Override
        public TypeDuration deserialize(Class<TypeDuration> type, ConfigNode node) throws NodeSerializeException {
            if (!node.isPrimitive()) {
                throw new NodeSerializeException(node, "Expected primitive value for duration");
            }

            String str = node.getString();
            Duration duration = parseDurationFromString(str);
            return new TypeDuration(duration);
        }

        private Duration parseDurationFromString(String value) throws NodeSerializeException {
            try {
                return DurationSerializer.deserialize(value, DurationFormats.allBundled());
            } catch (IllegalArgumentException | InvalidFormatException e) {
                throw new NodeSerializeException("Invalid duration format: " + value, e);
            }
        }
    }
}
