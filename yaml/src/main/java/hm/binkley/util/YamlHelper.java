package hm.binkley.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;
import static org.yaml.snakeyaml.DumperOptions.LineBreak.UNIX;

/**
 * {@code YamlHelper} provides an alternate API to common SnakeYAML
 * configuration based on Java 8 improvements for interfaces and builder
 * pattern.
 * <p>
 * This is a light-weight API wrapper; a given buider is not designed for reuse
 * or thread-safety.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public interface YamlHelper<T> {
    /**
     * Gets the first characters for implicit matching.
     *
     * @see Yaml#addImplicitResolver(Tag, Pattern, String)
     */
    @Nonnull
    String firstChars();

    /**
     * Gets the pattern for implicit matching.
     *
     * @see Yaml#addImplicitResolver(Tag, Pattern, String)
     */
    @Nonnull
    Pattern match();

    /**
     * Converter for YAML constructors map.
     *
     * @see Builder.BuilderConstructor#addImplicit(Class, YamlHelper)
     */
    @Nonnull
    Function<String, T> valueOf();

    /** Creates a new {@code Builder}. */
    @Nonnull
    static Builder builder() { return new Builder(); }

    /** Creates a new {@code YamlHelper} for enums. */
    @SafeVarargs
    static <E extends Enum<E>> YamlHelper<E> from(final Class<E> type,
            final E... values) {
        final List<E> v = asList(values);
        final String firstChars = v.stream().
                map(Object::toString).
                map(s -> s.substring(0, 1)).
                collect(joining());
        final Pattern match = compile(v.stream().
                map(Object::toString).
                collect(joining("|", "^(", ")$")));
        final Function<String, E> valueOf = s -> Enum.valueOf(type, s);
        return from(firstChars, match, valueOf);
    }

    /** Creates a new {@code YamlHelper} for one-arg-constructor classes. */
    static <T, R> YamlHelper<R> from(final String firstChars,
            final String regex, final Function<String, T> valueOfT,
            final Function<T, R> ctor, final String error) {
        final Pattern match = compile(regex);
        final Function<String, R> valueOf = s -> {
            final Matcher matcher = Util.matcher(s, match, error);
            return ctor.apply(valueOfT.apply(matcher.group(1)));
        };
        return from(firstChars, match, valueOf);
    }

    /** Creates a new {@code YamlHelper} for two-arg-constructor classes. */
    static <T, U, R> YamlHelper<R> from(final String firstChars,
            final String regex, final Function<String, T> valueOfT,
            final Function<String, U> valueOfU,
            final BiFunction<T, U, R> ctor, final String error) {
        final Pattern match = compile(regex);
        final Function<String, R> valueOf = s -> {
            final Matcher matcher = Util.matcher(s, match, error);
            return ctor.apply(valueOfT.apply(matcher.group(1)),
                    valueOfU.apply(matcher.group(2)));
        };
        return from(firstChars, match, valueOf);
    }

    /** Creates a new {@code YamlHelper}. */
    static <T> YamlHelper<T> from(final String firstChars, final Pattern match,
            final Function<String, T> valueOf) {
        return new SimpleYamlHelper<>(firstChars, match, valueOf);
    }

    final class Builder {
        private final BuilderConstructor constructor = new BuilderConstructor();
        private final BuilderRepresenter representer = new BuilderRepresenter();
        private final BuilderDumperOptions dumperOptions
                = new BuilderDumperOptions();
        private final Yaml yaml = new BuilderYaml(constructor, representer,
                dumperOptions);

        private Builder() {}

        /** Supports Hollywood principal. */
        public Builder then(final Consumer<Builder> them) {
            them.accept(this);
            return this;
        }

        /**
         * Configures an implicit tag for {@code Yaml} defining the tag to be
         * the <var>type</var> simple name prefixed by an exclamation point.
         */
        public <T> Builder addImplicit(final Class<T> type,
                final YamlHelper<T> helper) {
            final Tag tag = tagFor(type);
            constructor.addImplicit(tag, helper.valueOf());
            representer.addImplicit(type);
            yaml.addImplicitResolver(tag, helper.match(), helper.firstChars());
            return this;
        }

        /**
         * Gets the configured {@code Yaml}.  This does not destroy or disbable
         * the instance in this builder, further builder calls modifying the
         * returned instance.
         */
        public Yaml build() {
            return yaml;
        }

        private static Tag tagFor(final Class<?> type) {
            return new Tag("!" + type.getSimpleName());
        }

        private static class BuilderConstructor
                extends Constructor {
            <T> void addImplicit(final Tag tag,
                    final Function<String, T> valueOf) {
                yamlConstructors.put(tag, new BuilderConstruct<>(valueOf));
            }

            private static class BuilderConstruct<T>
                    extends AbstractConstruct {
                private final Function<String, T> valueOf;

                BuilderConstruct(final Function<String, T> valueOf) {
                    this.valueOf = valueOf;
                }

                @Override
                public T construct(final Node node) {
                    final String val = ((ScalarNode) node).getValue();
                    return valueOf.apply(val);
                }
            }
        }

        private static class BuilderRepresenter
                extends Representer {
            {
                setDefaultFlowStyle(BLOCK);
                setTimeZone(null); // null is UTC
            }

            void addImplicit(final Class<?> type) {
                representers.put(type,
                        data -> representScalar(tagFor(type), data.toString()));
            }
        }

        private static class BuilderDumperOptions
                extends DumperOptions {
            {
                setDefaultFlowStyle(BLOCK);
                setExplicitEnd(true);
                setExplicitStart(true);
                setLineBreak(UNIX);
                setTimeZone(null); // null is UTC
            }
        }

        private static class BuilderYaml
                extends Yaml {
            public BuilderYaml(final BuilderConstructor constructor,
                    final BuilderRepresenter representer,
                    final BuilderDumperOptions dumperOptions) {
                super(constructor, representer, dumperOptions);
            }

            @Override
            public Iterable<Object> loadAll(final Reader yaml) {
                final List<Object> list = new ArrayList<>();
                for (final Object doc : super.loadAll(yaml))
                    list.add(doc);
                return list;
            }

            @Override
            public Iterable<Object> loadAll(final String yaml) {
                final List<Object> list = new ArrayList<>();
                for (final Object doc : super.loadAll(yaml))
                    list.add(doc);
                return list;
            }

            @Override
            public Iterable<Object> loadAll(final InputStream yaml) {
                final List<Object> list = new ArrayList<>();
                for (final Object doc : super.loadAll(yaml))
                    list.add(doc);
                return list;
            }
        }
    }

    class SimpleYamlHelper<T>
            implements YamlHelper<T> {
        private final String firstChars;
        private final Pattern match;
        private final Function<String, T> valueOf;

        public SimpleYamlHelper(final String firstChars, final Pattern match,
                final Function<String, T> valueOf) {
            this.firstChars = firstChars;
            this.match = match;
            this.valueOf = valueOf;
        }

        @Nonnull
        @Override
        public String firstChars() { return firstChars; }

        @Nonnull
        @Override
        public Pattern match() { return match; }

        @Nonnull
        @Override
        public Function<String, T> valueOf() { return valueOf; }
    }

    class Util {
        private static Matcher matcher(final String s, final Pattern match,
                final String error) {
            final Matcher matcher = match.matcher(s);
            if (!matcher.matches())
                throw new IllegalArgumentException(format(error, s));
            return matcher;
        }
    }
}
