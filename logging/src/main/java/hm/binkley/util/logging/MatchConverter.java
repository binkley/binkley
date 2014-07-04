/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.status.ErrorStatus;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ch.qos.logback.core.CoreConstants.EVALUATOR_MAP;
import static java.util.Collections.emptyMap;

/**
 * {@code MarkedConverter} provides alternate conversions based on conditions.  Enable with:
 * <pre>
 * &lt;conversionRule
 *     conversionWord="match"
 *     converterClass="hm.binkley.util.logging.MatchConverter"/&gt;</pre> Use as:
 * <pre>
 * &lt;pattern&gt;%match{cond1,patt1,...,fallback}&lt;/pattern&gt;</pre> Example:
 * <pre>
 * &lt;evaluator name="WITH_MARKER"&gt;
 *     &lt;expression&gt;null != marker &amp;mp;&amp;mp; "ALERT".equals(marker.getName())&lt;/expression&gt;
 * &lt;/evaluator&gt;
 * &lt;pattern&gt;%match(WITH_MARKER,%marker/%level,%level)&lt;/pattern&gt;</pre> will log
 * "ALERT/ERROR" when marker is "ALERT" and level is "ERROR", otherwise just "ERROR".
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Fix error reporting - logback swallows
 */
public final class MatchConverter
        extends ClassicConverter {
    private static final int MAX_ERROR_COUNT = 4;
    private Map<String, String> conditions;
    private String unmatched;
    private Map<String, EventEvaluator<ILoggingEvent>> evaluators;
    private int errors;

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        final List<String> options = getOptionList();
        if (null == options || 2 > options.size()) {
            addError("Missing options for %match - " + Objects.toString(options));
            conditions = emptyMap();
            unmatched = "";
            return;
        }

        conditions = new LinkedHashMap<>();
        for (int i = 0; i < options.size() - 1; i += 2)
            conditions.put(options.get(i), options.get(i + 1));
        unmatched = 0 == options.size() % 2 ? "" : options.get(options.size() - 1);

        evaluators = (Map<String, EventEvaluator<ILoggingEvent>>) getContext()
                .getObject(EVALUATOR_MAP);

        super.start();
    }

    @Nonnull
    @Override
    public String convert(@Nonnull final ILoggingEvent event) {
        for (final Map.Entry<String, String> entry : conditions.entrySet())
            if (evaluate(entry.getKey(), event))
                return relayout(entry.getValue(), event);
        return relayout(unmatched, event);
    }

    private boolean evaluate(final String name, final ILoggingEvent event) {
        final EventEvaluator<ILoggingEvent> evaluator = evaluators.get(name);
        try {
            return null != evaluator && evaluator.evaluate(event);
        } catch (final EvaluationException e) {
            errors++;
            if (MAX_ERROR_COUNT > errors) {
                addError("Exception thrown for evaluator named [" + evaluator.getName() + "]", e);
            } else if (MAX_ERROR_COUNT == errors) {
                final ErrorStatus errorStatus = new ErrorStatus(
                        "Exception thrown for evaluator named [" + evaluator.getName() + "].", this,
                        e);
                errorStatus.add(new ErrorStatus(
                        "This was the last warning about this evaluator's errors."
                                + "We don't want the StatusManager to get flooded.", this));
                addStatus(errorStatus);
            }
            return false;
        }
    }

    private String relayout(final String pattern, final ILoggingEvent event) {
        final PatternLayout layout = new PatternLayout();
        layout.setContext(getContext());
        layout.setPattern(pattern);
        layout.start();
        return layout.doLayout(event);
    }
}
