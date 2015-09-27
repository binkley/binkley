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

package hm.binkley.util;

import javax.annotation.Nonnull;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * {@code Lists} has methods on {@code java.util.List}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public final class Lists {
    /**
     * Partitions the given <var>list</var> into <var>n</var> buckets as
     * evenly as possible. Buckets are contiguous sublists of
     * <var>list</var>.
     *
     * @param list the list to partition, never missing
     * @param n the bucket count, always positive
     * @param <T> the list item type
     *
     * @return the list of buckets, never missing
     */
    @Nonnull
    public static <T> List<List<T>> partition(@Nonnull final List<T> list,
            final int n) {
        if (1 > n)
            throw new IllegalArgumentException(
                    "Non-positive bucket count: " + n);
        final int size = list.size();
        final int div = size / n;
        final int mod = size % n;

        int start = 0;
        final List<List<T>> buckets = new ArrayList<>(n);
        for (int i = 0; i < mod; ++i) {
            final int end = start + div + 1;
            buckets.add(list.subList(start, end));
            start = end;
        }
        for (int i = mod; i < n; ++i) {
            final int end = start + div;
            buckets.add(list.subList(start, end));
            start = end;
        }
        return buckets;
    }

    @Nonnull
    public static <T> List<T> list(@Nonnull final FromIntFunction<T> get,
            @Nonnull final IntSupplier size) {
        return new ListList<>(get, size);
    }

    private Lists() {
    }

    @FunctionalInterface
    public interface FromIntFunction<T> {
        T apply(final int i);
    }

    private static final class ListList<T>
            extends AbstractList<T> {
        private final FromIntFunction<T> get;
        private final IntSupplier size;

        public ListList(final FromIntFunction<T> get,
                final IntSupplier size) {
            this.get = get;
            this.size = size;
        }

        @Override
        public T get(final int index) {
            return get.apply(index);
        }

        @Override
        public int size() {
            return size.getAsInt();
        }
    }
}
