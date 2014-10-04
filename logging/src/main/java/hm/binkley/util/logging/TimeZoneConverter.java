/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>.
 */

package hm.binkley.util.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * {@code TimeZoneConverter} provides the local timezone in {@link TimeZone#SHORT} or {@link
 * TimeZone#LONG} format.  Default is short.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class TimeZoneConverter
        extends ClassicConverter {
    private int format;

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        final List<String> options = getOptionList();
        if (null == options)
            format = TimeZone.SHORT;
        else if (1 == options.size()) {
            final String specifier = options.get(0);
            if ("SHORT".equalsIgnoreCase(specifier))
                format = TimeZone.SHORT;
            else if ("LONG".equalsIgnoreCase(specifier))
                format = TimeZone.LONG;
            else
                addError("Illegal timezone format for %timezone - " + specifier
                        + "; defaulting to SHORT");
        } else
            addError("Illegal timezone format specifier for %timezone - " + options
                    + "; defaulting to SHORT");

        super.start();
    }

    @Override
    public String convert(final ILoggingEvent event) {
        final TimeZone tz = TimeZone.getDefault();
        return tz.getDisplayName(tz.inDaylightTime(new Date()), format);
    }

    public static void main(final String... args) {
        System.setProperty("logback.configurationFile", "osi-logback.xml");
        LoggerFactory.getLogger("main").info("Howdy, Houston!");
    }
}
