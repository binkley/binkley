/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionSet;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static hm.binkley.inject.JOptSimpleModule.bindArgs;
import static hm.binkley.inject.JOptSimpleModule.bootstrapInjector;
import static java.io.File.listRoots;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code JOptSimpleModuleTest} tests {@link JOptSimpleModule}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class JOptSimpleModuleTest {
    @Test
    public void shouldParse() {
        final Injector bootstrap = createInjector(bootstrapInjector());
        final JOptSimpleModule jOptSimpleModule = bindArgs(bootstrap);
        final ArgumentAcceptingOptionSpec<File> rootsOption = jOptSimpleModule.accepts("r").
                withRequiredArg().
                ofType(File.class).
                defaultsTo(listRoots());
        final Injector guice = bootstrap.createChildInjector(jOptSimpleModule);

        assertThat(guice.getInstance(OptionSet.class).valuesOf(rootsOption),
                is(equalTo(asList(listRoots()))));
    }

    @Test
    public void shouldBindCommandLine() {
        final Injector bootstrap = createInjector(bootstrapInjector());
        final JOptSimpleModule jOptSimpleModule = bindArgs(bootstrap, "a", "b");

        assertThat(bootstrap.createChildInjector(jOptSimpleModule)
                .getInstance(Key.get(new TypeLiteral<List<String>>() {}, named("main.args"))),
                is(equalTo(asList("a", "b"))));
    }
}
