/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.inject;

import javax.annotation.Nonnull;

/**
 * {@code UncheckedThrow} throws checked exceptions as unchecked.
 *
 * @author <a href="mailto:roman.stoffel@gamlor.info">Roman Stoffel (Gamlor)</a>
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @see <a href="http://www.gamlor.info/wordpress/2010/02/throwing-checked-excpetions-like-unchecked-exceptions-in-java/">Throwing
 * Checked Exceptions Like Unchecked Exceptions in Java</a>
 */
public final class UncheckedThrow {
    UncheckedThrow() {}

    /**
     * Throws a checked exception as unchecked
     *
     * @param e the exception to rethrow, never missing
     * @param <T> the return type when syntactically required, never used
     *
     * @return ignored
     */
    public static <T> T throwUnchecked(@Nonnull final Exception e) {
        // Now we use the 'generic' method. Normally the type T is inferred
        // from the parameters. However you can specify the type also explicit!
        // Now we du just that! We use the RuntimeException as type!
        // That means the throwsUnchecked throws an unchecked exception!
        // Since the types are erased, no type-information is there to prevent this!
        UncheckedThrow.<RuntimeException>throwsUnchecked(e);

        // This is here is only to satisfy the compiler. It's actually unreachable code!
        throw new AssertionError(
                "This code should be unreachable. Something went terrible wrong here!");
    }

    /**
     * Remember, Generics are erased in Java. So this basically throws an Exception. The real Type
     * of T is lost during the compilation
     */
    private static <T extends Exception> void throwsUnchecked(final Exception toThrow)
            throws T {
        // Since the type is erased, this cast actually does nothing!!!
        // we can throw any exception
        throw (T) toThrow;
    }
}
