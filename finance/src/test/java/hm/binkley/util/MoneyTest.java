/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import org.ini4j.Profile.Section;
import org.ini4j.Wini;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOError;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class MoneyTest {
    private static final Currency USD = Currency.getInstance("USD");

    @Parameters(name = "{index}: {0}: {1}")
    public static Collection<Object[]> parameters() {
        final List<Object[]> parameters = new ArrayList<>();
        try {
            final String path = "/" + MoneyTest.class.getName().replace(".", "/") + ".ini";
            final Wini ini = new Wini(MoneyTest.class.getResourceAsStream(path));
            for (final Section section : ini.values()) {
                final String currency = section.get("currency");
                final String amount = section.get("amount");
                parameters.add(array(section.getName(), section.get("value"),
                        null == currency ? null : Currency.getInstance(currency),
                        null == amount ? null : new BigDecimal(amount)));
            }

            return parameters;
        } catch (final IOException e) {
            final IOError x = new IOError(e);
            x.setStackTrace(e.getStackTrace());
            throw x;
        }
    }

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final String description;
    private final String value;
    private final Currency currency;
    private final BigDecimal amount;

    public MoneyTest(@Nonnull final String description, @Nonnull final String value, @Nullable final Currency currency,
            @Nullable final BigDecimal amount) {
        this.description = description;
        this.value = value;
        this.currency = currency;
        this.amount = amount;
    }

    @Test
    public void shouldParse() {
        if (null == currency) {
            thrown.expect(MoneyFormatException.class);
            thrown.expectMessage(value);
        }

        final Money money = Money.parse(value);

        assertThat(money.getCurrency(), is(equalTo(currency)));
        assertThat(money.getAmount(), is(equalTo(amount)));
    }

    private static Object[] array(@Nonnull final String description, @Nonnull final String value,
            @Nullable final Currency currency, @Nullable final BigDecimal amount) {
        if ((null == currency && null == amount) || (null != currency && null != amount))
            return new Object[]{description, value, currency, amount};

        throw new IllegalArgumentException("Both currency and amount cannot be null");
    }
}
