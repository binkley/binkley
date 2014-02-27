/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import com.google.inject.Inject;
import hm.binkley.inject.DemoMain.DemoMainConfig;
import joptsimple.OptionDeclarer;
import org.aeonbits.owner.Config;
import org.kohsuke.MetaInfServices;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.ServiceLoader;

import static java.lang.String.format;
import static java.lang.System.out;

/**
 * {@code DemoMain} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @version $Id: $Id
 * @todo Needs documentation.
 */
@MetaInfServices
public final class DemoMain
        extends Main<DemoMainConfig> {
    private DemoMainConfig config;

    /**
     * Constructs a new {@code DemoMain}.  Note the use of {@link DemoMainConfig} with {@code
     * super}.  This does <em>not</em> use constructor injection so that {@link ServiceLoader} may
     * instantiate freely.
     */
    public DemoMain() {
        super(DemoMainConfig.class, declarer -> {
            declarer.accepts("debug");
        });
    }

    /**
     * Injects {@link #config} with the given <var>config</var>.
     *
     * @param config the config, never missing
     */
    @Inject
    public void setConfig(@Nonnull final DemoMainConfig config) { this.config = config; }

    @Override
    protected void addOptions(@Nonnull final OptionDeclarer optionDeclarer) {
        optionDeclarer.accepts("debug");
    }

    /** For demonstration. */
    @PostConstruct
    public void init() {
        out.println(format("DemoMain.init = %s", config.debug()));
    }

    public interface DemoMainConfig
            extends Config {
        @DefaultValue("false")
        boolean debug();
    }
}
