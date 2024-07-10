package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import net.minecraft.server.v1_16_R1.Block;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Overrides particular values on blocks.
 *
 * @author Kristian
 */
// Code from GitHub/Bukkit forums; I unfortunately can't find the link anymore
public class BlockOverride {

    private final Block block;
    private final Map<String, Field> fieldCache = new HashMap<String, Field>();

    public BlockOverride(Block block) {
        this.block = block;
    }

    /**
     * Update the given field with a new value.
     *
     * @param  fieldName                name of field
     * @param  value                    new value
     * @throws IllegalArgumentException if the field name is <code>null</code> or the field doesn't exist
     * @throws RuntimeException         if we don't have security clearance
     */
    public void set(String fieldName, Object value) {
        try {
            FieldUtils.writeField(getField(fieldName), block, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read field for setting: " + fieldName, e);
        }
    }

    /**
     * Retrieves the current field value.
     *
     * @param  fieldName                name of field
     * @throws IllegalArgumentException if the field name is <code>null</code> or the field doesn't exist
     * @throws RuntimeException         if we don't have security clearance
     */
    public Object get(String fieldName) {
        try {
            return FieldUtils.readField(getField(fieldName), block);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read field for getting: " + fieldName, e);
        }
    }

    private Field getField(String fieldName) {
        Field cached = fieldCache.get(fieldName);

        if (cached == null) {
            cached = FieldUtils.getField(block.getClass(), fieldName, true);

            if (cached != null) {
                fieldCache.put(fieldName, cached);
            } else {
                throw new IllegalArgumentException("Cannot locate field: " + fieldName);
            }
        }

        return cached;
    }
}
