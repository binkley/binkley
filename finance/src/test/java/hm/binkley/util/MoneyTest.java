/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import hm.binkley.util.ParameterizedHelper.Key;
import org.ini4j.Ini;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;

import static hm.binkley.util.ParameterizedHelper.parametersFrom;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class MoneyTest {
    @Parameters(name = "{index}: {0}: {1}")
    public static Collection<Object[]> parameters()
            throws IOException {
        return parametersFrom(new Ini(MoneyTest.class.getResource("MoneyTest.ini")),
                Key.of("value"), Key.of("currency", Currency::getInstance),
                Key.of("amount", BigDecimal::new));
    }

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final String name;
    private final String value;
    private final Currency currency;
    private final BigDecimal amount;

    public MoneyTest(@Nonnull final String name, @Nonnull final String value,
            @Nullable final Currency currency, @Nullable final BigDecimal amount) {
        this.name = name;
        this.value = value;
        this.currency = currency;
        this.amount = amount;

        if (!((null == currency && null == amount) || (null != currency && null != amount)))
            throw new IllegalArgumentException(
                    format("%s: currency and amount must both be null or non-null", name));
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
}
