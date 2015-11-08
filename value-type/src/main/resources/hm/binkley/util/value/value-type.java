package ${package};

@javax.annotation.Generated(
        value = "hm.binkley.util.value.ValueTypeProcessor",
        date = "${timestamp}")
public final class ${class}Value
        extends hm.binkley.util.value.${base}<${type}, ${class}Value>
        implements ${class} {
    private static final java.util.Map<${type}, ${class}Value> $cache = new java.util.concurrent.ConcurrentHashMap<>();

    @javax.annotation.Nonnull
    public static ${class}Value of(@javax.annotation.Nonnull final ${type} value) {
        java.util.Objects.requireNonNull(value, "value");
        return $cache.computeIfAbsent(value, ${class}Value::new);
    }

    private ${class}Value(final ${type} value) {
        super(value${modify});
    }

    @Override
    @javax.annotation.Nonnull
    public Class<${type}> type() {
        return ${type}.class;
    }
}
