package fi.helsinki.cs.tmc.langs.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ValueObject {

    private static final Logger log = LoggerFactory.getLogger(ValueObject.class);

    private Object value;

    public ValueObject(Object object) {
        this.value = object;
    }

    public Object get() {
        return this.value;
    }

    /**
     * Returns the value of this object as a String.
     *
     * @return Value as a String. Null if value isn't a String.
     */
    public String asString() {
        if (!(value instanceof String)) {
            log.error("Couldn't convert configuration {} to String.", value.toString());
            return null;
        }
        return (String) this.value;
    }

    /**
     * Returns the value of this object as a Boolean.
     *
     * @return Value as a Boolean. Null if value isn't a Boolean.
     */
    public Boolean asBoolean() {
        if (!(value instanceof Boolean)) {
            log.error("Couldn't convert configuration {} to Boolean.", value.toString());
            return null;
        }
        return (Boolean) this.value;
    }
}