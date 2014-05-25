/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.function.Function;

import static java.util.function.Function.identity;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class MoneyTest {
    @Parameters(name = "{index}: {0}: {1}")
    public static Collection<Object[]> parameters()
            throws IOException {
        return parameters(new Ini(MoneyTest.class.getResource("MoneyTest.ini")),
                Key.of("value", identity()), Key.of("currency", Currency::getInstance),
                Key.of("amount", BigDecimal::new));
    }

    public static final class Key {
        public final String name;
        public final Function<String, ?> get;

        public static Key of(final String name, final Function<String, ?> get) {
            return new Key(name, get);
        }

        private Key(final String name, final Function<String, ?> get) {
            this.name = name;
            this.get = get;
        }
    }

    public static List<Object[]> parameters(final Ini ini, final Key... keys) {
        final List<Object[]> parameters = new ArrayList<>();
        for (final Section section : ini.values()) {
            final Object[] array = new Object[1 + keys.length];
            array[0] = section.getName();
            for (int i = 0; i < keys.length; i++) {
                final Key key = keys[i];
                final String value = section.fetch(key.name);
                array[1 + i] = null == value ? null : key.get.apply(value);
            }
            parameters.add(array);
        }
        return parameters;
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
