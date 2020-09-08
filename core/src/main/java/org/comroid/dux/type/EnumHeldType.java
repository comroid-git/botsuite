package org.comroid.dux.type;

import org.comroid.api.Junction;
import org.comroid.api.Named;
import org.comroid.api.HeldType;
import org.comroid.uniform.ValueType;

import java.util.function.Function;

public class EnumHeldType<R extends Enum<R> & Named, USR, MSG> implements HeldType<R> {
    private final Class<R> enumType;
    private final Junction<String, R> converter;

    @Override
    public Junction<String, R> getConverter() {
        return converter;
    }

    @Override
    public String getName() {
        return enumType.getSimpleName();
    }

    public EnumHeldType(Class<R> enumType, Function<String, R> parser) {
        this.enumType = enumType;
        this.converter = Junction.ofString(parser);
    }

    @Override
    public <T> T convert(R value, HeldType<T> toType) {
        if (toType.equals(ValueType.STRING))
            //noinspection unchecked
            return (T) value.getName();
        throw new UnsupportedOperationException();
    }
}
