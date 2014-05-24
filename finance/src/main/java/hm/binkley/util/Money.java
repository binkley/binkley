/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * {@code Money} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Help with formatting, perhaps format()?
 */
public final class Money
        implements Comparable<Money> {
    /** @todo Does JDK have a pattern for floating point? */
    private static final Pattern ISO = Pattern.compile("^([A-Z]{3})\\s*(\\S+)$");
    private final Currency currency;
    private final BigDecimal amount;

    /**
     * Creates a new {@code Money} instance for the given text <var>value</var> with a leading
     * currency followed by an amount.
     *
     * @param value the text representation of money, never missing
     *
     * @return the money instance, never missing
     *
     * @throws MoneyFormatException if <var>value</var> is not well-formed
     * @todo Implement by formatted symbol
     * @todo Amount needs to have correct decimal places for the currency
     * @todo Are Special Drawing Rights, et al, handled correctly?
     * @todo Negative amounts: accept surrounding parens or only minus sign?
     * @todo Some kind of caching?
     */
    public static Money parse(@Nonnull final String value)
            throws MoneyFormatException {
        try {
            final Matcher matcher = ISO.matcher(value);
            if (matcher.matches()) {
                final Currency currency = Currency.getInstance(matcher.group(1));
                BigDecimal amount = new BigDecimal(matcher.group(2));
                final int digits = currency.getDefaultFractionDigits();
                if (0 <= digits) // SDR returns -1
                    amount = amount.setScale(digits);
                return new Money(currency, amount);
            }
        } catch (final IllegalArgumentException | ArithmeticException ignored) {
        }

        throw new MoneyFormatException(value);
    }

    private Money(@Nonnull final Currency currency, @Nonnull final BigDecimal amount) {
        this.currency = currency;
        this.amount = amount;
    }

    /**
     * Returns the ISO currency code followed by the amount with full decimal places.
     *
     * @return the text representation of this money, never missing
     */
    @Override
    @Nonnull
    public String toString() {
        return currency.getCurrencyCode() + amount;
    }

    @Override
    public int compareTo(@Nonnull final Money that) {
        if (!currency.equals(that.currency))
            throw new IllegalArgumentException(
                    format("Different currencies: %s vs %s", currency, that.currency));
        return amount.compareTo(that.amount);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final Money that = (Money) o;

        return currency.equals(that.currency) && amount.equals(that.amount);
    }

    @Override
    public int hashCode() {
        int result = currency.hashCode();
        result = 31 * result + amount.hashCode();
        return result;
    }
}
