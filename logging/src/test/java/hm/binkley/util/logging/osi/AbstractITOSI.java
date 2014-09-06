/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging.osi;

import hm.binkley.util.logging.AbstractITLogback;
import org.junit.After;
import org.junit.Before;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static hm.binkley.util.logging.LoggerUtil.refreshLogback;
import static hm.binkley.util.logging.osi.OSI.SystemProperty.LOGBACK_CONFIGURATION_FILE;
import static java.lang.String.format;

/**
 * Base class for OSI integration tests.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Does not do what you would expect
 */
public abstract class AbstractITOSI
        extends AbstractITLogback {
    private final List<OSI.SystemProperty> setProperties = new ArrayList<>(
            OSI.SystemProperty.values().length);

    /**
     * Sets a system property for OSI integration tests.  Use {@code null} as <var>value</var> to
     * clear the system property.
     *
     * @param property the property enum, never missing
     * @param value the value, {@code null} to clear
     */
    protected void setOSISystemProperty(@Nonnull final OSI.SystemProperty property,
            @Nullable final String value) {
        if (!property.set(value, false))
            throw new IllegalStateException(
                    format("Previous test not cleaned up for %s: %s", property, property.get()));
        refreshLogback();
        setProperties.add(property);
    }

    @Before
    public final void setUpAbstractITOSI() {
        setOSISystemProperty(LOGBACK_CONFIGURATION_FILE, "osi-logback.xml");
    }

    @After
    public final void tearDownAbstractITOSI() {
        setProperties.forEach(OSI.SystemProperty::unset);
    }
}
