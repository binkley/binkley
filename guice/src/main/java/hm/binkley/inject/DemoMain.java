package hm.binkley.inject;

import com.google.inject.Inject;
import hm.binkley.inject.DemoMain.DemoMainConfig;
import joptsimple.OptionParser;
import org.aeonbits.owner.Config;
import org.kohsuke.MetaInfServices;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import static java.lang.String.format;
import static java.lang.System.out;

/**
 * {@code DemoMain} needs documentation.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
@MetaInfServices
public final class DemoMain
        extends Main<DemoMainConfig> {
    private DemoMainConfig config;

    @Inject
    public void setConfig(final DemoMainConfig config) {
        this.config = config;
    }

    @Nonnull
    @Override
    protected Class<DemoMainConfig> configType() {
        return DemoMainConfig.class;
    }

    @Override
    protected void addOptions(@Nonnull final OptionParser optionParser) {
        optionParser.accepts("debug");
    }

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
