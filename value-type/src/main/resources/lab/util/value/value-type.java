package ${package};

@javax.annotation.Generated("lab.util.value.ValueTypeProcessor")
public final class ${class}Value
        extends lab.util.value.Value<${type}, ${class}Value>
        implements ${class} {
    private static final java.util.Map<${type}, ${class}Value> $cache = new java.util.concurrent.ConcurrentHashMap<>();

    @javax.annotation.Nonnull
    public static ${class}Value of(@javax.annotation.Nonnull final ${type} value) {
        if (null == value)
            throw new NullPointerException("value");
        return $cache.computeIfAbsent(value, ${class}Value::new);
    }

    private ${class}Value(final ${type} value) {
        super(value);
    }

    @Override
    public Class<${type}> type() {
        return ${type}.class;
    }
}
