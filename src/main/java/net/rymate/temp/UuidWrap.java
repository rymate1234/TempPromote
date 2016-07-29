package net.rymate.temp;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Because UUIDs don't serialize very well
 *
 * Created by Ryan on 27/07/2016.
 */
public class UuidWrap implements ConfigurationSerializable {
    private UUID uuid;

    public UuidWrap(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID get() {
        return uuid;
    }

    public Map<String, Object> serialize() {
        final Map<String, Object> serialized = new LinkedHashMap<String, Object>();
        serialized.put("uuid", uuid.toString());
        return serialized;
    }

    public static UuidWrap deserialize(final Map<String, Object> args) {
        UuidWrap uuid;
        if (!args.containsKey("uuid")) {
            return null;
        }

        return new UuidWrap(UUID.fromString((String) args.get("uuid")));
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UUID) {
            return uuid.equals(obj);
        } else if (obj instanceof UuidWrap){
            return ((UuidWrap) obj).get().equals(uuid);
        } else {
            return super.equals(obj);
        }
    }
}
