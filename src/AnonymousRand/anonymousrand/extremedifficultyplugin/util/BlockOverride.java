package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import net.minecraft.server.v1_16_R1.Block;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/** 
 * Overrides particular values on blocks,
 *
 * @author Kristian
 */
public class BlockOverride { // class from GitHub/Bukkit forums

    // The block we will override
    private final Block block;

    // Old values
    private final Map<String, Object> oldValues = new HashMap<String, Object>();
    private final Map<String, Field> fieldCache = new HashMap<String, Field>();

    public BlockOverride(Block block) {
        this.block = block;
    }

    /** 
     * Update the given field with a new value.
     * @param fieldName - name of field.
     * @param value - new value.
     * @throws IllegalArgumentException If the field name is NULL or the field doesn't exist.
     * @throws RuntimeException If we don't have security clearance.
     */
    public void set(String fieldName, Object value) {
        try {
            // Write the value directly
            FieldUtils.writeField(getField(fieldName), block, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read field.", e);
        }
    }

    /** 
     * Retrieves the current field value.
     * @param fieldName - name of field.
     * @throws IllegalArgumentException If the field name is NULL or the field doesn't exist.
     * @throws RuntimeException If we don't have security clearance.
     */
    public Object get(String fieldName) {
        try {
            // Read the value directly
            return FieldUtils.readField(getField(fieldName), block);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read field.", e);
        }
    }

    /** 
     * Retrieves the old vanilla field value.
     * @param fieldName - name of field.
     * @throws IllegalArgumentException If the field name is NULL or the field doesn't exist.
     * @throws RuntimeException If we don't have security clearance.
     */
    public Object getVanilla(String fieldName) {
        if (fieldName == null)
            throw new NullArgumentException("fieldName");

        if (oldValues.containsKey(fieldName))
            return oldValues.get(fieldName);
        else
            return get(fieldName);
    }

    private Field getField(String fieldName) {
        Field cached = fieldCache.get(fieldName);

        if (cached == null) {
            cached = FieldUtils.getField(block.getClass(), fieldName, true);

            // Remember this particular field
            if (cached != null) {
                fieldCache.put(fieldName, cached);
            } else {
                throw new IllegalArgumentException("Cannot locate field " + fieldName);
            }
        }

        return cached;
    }
}