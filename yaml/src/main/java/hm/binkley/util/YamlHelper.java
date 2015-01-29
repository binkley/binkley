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
import static org.yaml.snakeyaml.DumperOptions.ScalarStyle.PLAIN;

/**
 * {@code YamlHelper} provides an alternate API to common SnakeYAML
 * configuration based on Java 8 improvements for interfaces and builder
 * pattern.
 * <p>
 * This is a light-weight API wrapper; a given buider is not designed for
 * reuse or thread-safety.
 *
 * @param <T> the type of class receiving help
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public interface YamlHelper<T> {
    interface Implicit<T>
            extends YamlHelper<T> {
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
    }

    interface Explicit<T>
            extends YamlHelper<T> {}

    /**
     * Converter for YAML constructors map.
     *
     * @see Builder.BuilderConstructor#addImplicit(Class, Implicit)
     * @see Builder.BuilderConstructor#addExplicit(Tag, Explicit)
     */
    @Nonnull
    Function<String, T> valueOf();

    /** Creates a new {@code Builder}. */
    @Nonnull
    static Builder builder() { return new Builder(); }

    /** Creates a new {@code YamlHelper.Implicit} for enums. */
    @Nonnull
    @SafeVarargs
    static <E extends Enum<E>> Implicit<E> implicitFrom(
            @Nonnull final Class<E> type, final E... values) {
        final List<E> v = asList(values);
        final String firstChars = v.stream().
                map(Object::toString).
                map(s -> s.substring(0, 1)).
                collect(joining());
        final Pattern match = compile(v.stream().
                map(Object::toString).
                collect(joining("|", "^(", ")$")));
        final Function<String, E> valueOf = s -> Enum.valueOf(type, s);
        return implicitFrom(firstChars, match, valueOf);
    }

    /**
     * Creates a new {@code YamlHelper.Implicit} for one-arg-constructor
     * classes.
     */
    @Nonnull
    static <T, R> Implicit<R> implicitFrom(@Nonnull final String firstChars,
            @Nonnull final String regex,
            @Nonnull final Function<String, T> valueOfT,
            @Nonnull final Function<T, R> ctor, @Nonnull final String error) {
        final Pattern match = compile(regex);
        final Function<String, R> valueOf = s -> {
            final Matcher matcher = Util.matcher(s, match, error);
            return ctor.apply(valueOfT.apply(matcher.group(1)));
        };
        return implicitFrom(firstChars, match, valueOf);
    }

    /**
     * Creates a new {@code YamlHelper.Implicit} for two-arg-constructor
     * classes.
     */
    @Nonnull
    static <T, U, R> Implicit<R> implicitFrom(
            @Nonnull final String firstChars, @Nonnull final String regex,
            @Nonnull final Function<String, T> valueOfT,
            @Nonnull final Function<String, U> valueOfU,
            @Nonnull final BiFunction<T, U, R> ctor,
            @Nonnull final String error) {
        final Pattern match = compile(regex);
        final Function<String, R> valueOf = s -> {
            final Matcher matcher = Util.matcher(s, match, error);
            return ctor.apply(valueOfT.apply(matcher.group(1)),
                    valueOfU.apply(matcher.group(2)));
        };
        return implicitFrom(firstChars, match, valueOf);
    }

    /** Creates a new {@code YamlHelper.Implicit}. */
    @Nonnull
    static <T> Implicit<T> implicitFrom(@Nonnull final String firstChars,
            @Nonnull final Pattern match,
            @Nonnull final Function<String, T> valueOf) {
        return new SimpleImplicit<>(firstChars, match, valueOf);
    }

    @Nonnull
    static <T> Explicit<T> explicitFrom(
            @Nonnull final Function<String, T> valueOf) {
        return () -> valueOf;
    }

    final class Builder {
        private final BuilderConstructor constructor
                = new BuilderConstructor();
        private final BuilderRepresenter representer
                = new BuilderRepresenter();
        private final BuilderDumperOptions dumperOptions
                = new BuilderDumperOptions();
        private final BuilderYaml yaml = new BuilderYaml(constructor,
                representer, dumperOptions);

        private Builder() {}

        /** Supports Hollywood principal. */
        @Nonnull
        public Builder then(final Consumer<Builder> then) {
            then.accept(this);
            return this;
        }

        /** Configures the dumper options. */
        @Nonnull
        public Builder dumper(final Consumer<DumperOptions> dumper) {
            dumper.accept(dumperOptions);
            return this;
        }

        /**
         * Configures an implicit tag for {@code Yaml} defining the tag to be
         * the <var>type</var> simple name prefixed by an exclamation point.
         */
        @Nonnull
        public <T> Builder addImplicit(@Nonnull final Class<T> type,
                @Nonnull final Implicit<T> helper) {
            final Tag tag = tagFor(type);
            constructor.addImplicit(tag, helper);
            representer.addImplicit(type, tag);
            yaml.addImplicitResolver(tag, helper);
            return this;
        }

        @Nonnull
        public <T> Builder addExplicit(@Nonnull final Class<T> type,
                @Nonnull final String tagText,
                @Nonnull final Explicit<T> helper) {
            final Tag tag = new Tag("!" + tagText);
            constructor.addExplicit(tag, helper);
            representer.addExplicit(type, tag);
            return this;
        }

        /**
         * Gets the configured {@code Yaml}.  This does not destroy or
         * disbable the instance in this builder, further builder calls
         * modifying the returned instance.
         */
        public Yaml build() {
            return yaml;
        }

        private static Tag tagFor(final Class<?> type) {
            return new Tag("!" + type.getSimpleName());
        }

        private static class BuilderConstructor
                extends Constructor {
            <T> void addImplicit(final Tag tag, final Implicit<T> implicit) {
                yamlConstructors.put(tag, new BuilderConstruct<>(implicit));
            }

            <T> void addExplicit(final Tag tag, final Explicit<T> explicit) {
                final BuilderConstruct<T> ctor = new BuilderConstruct<>(
                        explicit);
                yamlConstructors.put(tag, ctor);
                yamlMultiConstructors.put(tag.getValue(), ctor);
            }

            private class BuilderConstruct<T>
                    extends AbstractConstruct {
                private final Function<String, T> valueOf;

                BuilderConstruct(final YamlHelper<T> helper) {
                    valueOf = helper.valueOf();
                }

                @Override
                public T construct(final Node node) {
                    return valueOf.apply((String) constructScalar(
                            (ScalarNode) node));
                }
            }
        }

        private static class BuilderRepresenter
                extends Representer {
            {
                setDefaultFlowStyle(BLOCK);
                setDefaultScalarStyle(PLAIN);
                setTimeZone(null); // null is UTC
            }

            void addImplicit(final Class<?> type, final Tag tag) {
                addClassTag(type, tag);
                representers.put(type,
                        data -> representScalar(tag, data.toString()));
            }

            void addExplicit(final Class<?> type, final Tag tag) {
                addClassTag(type, tag);
                representers.put(type,
                        data -> representScalar(tag, data.toString()));
            }
        }

        private static class BuilderDumperOptions
                extends DumperOptions {
            {
                setDefaultFlowStyle(BLOCK);
                setDefaultScalarStyle(PLAIN);
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

            public void addImplicitResolver(final Tag tag,
                    final Implicit<?> helper) {
                addImplicitResolver(tag, helper.match(), helper.firstChars());
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

    class SimpleImplicit<T>
            implements Implicit<T> {
        private final String firstChars;
        private final Pattern match;
        private final Function<String, T> valueOf;

        public SimpleImplicit(final String firstChars, final Pattern match,
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
