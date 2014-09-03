package org.nnsoft.guice.lifegycle;

/*
 *  Copyright 2012 The 99 Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * A Disposer is a mini-container that releases resources invoking injectees methods annotated by
 * {@code Dispose}.
 */
public final class Disposer {
    /** List of elements have to be disposed. */
    private final List<Disposable> disposables = new LinkedList<>();

    /**
     * Register an injectee and its related method to release resources.
     *
     * @param disposeMethod the method to be invoked to release resources
     * @param injectee the target injectee has to release the resources
     */
    <I> void register(final Method disposeMethod, final I injectee) {
        disposables.add(new Disposable(disposeMethod, injectee));
    }

    /**
     * Releases resources invoking the {@code Dispose} annotated methods, successes/errors will be
     * muted.
     */
    public void dispose() {
        dispose(new NoOpDisposeHandler());
    }

    /**
     * Releases resources invoking the {@code Dispose} annotated methods, successes/errors will be
     * tracked in the input {@link DisposeHandler}.
     *
     * @param disposeHandler the DisposeHandler instance that tracks dispose progresses.
     */
    public void dispose(DisposeHandler disposeHandler) {
        if (null == disposeHandler) {
            disposeHandler = new NoOpDisposeHandler();
        }

        for (final Disposable disposable : disposables) {
            disposable.dispose(disposeHandler);
        }
    }

    /** NOOP {@code DisposeHandler} implementation. */
    private static final class NoOpDisposeHandler
            implements DisposeHandler {
        public <I, E extends Throwable> void onError(@Nonnull final I injectee,
                @Nonnull final E error) {
            // do nothing
        }

        public <I> void onSuccess(@Nonnull final I injectee) {
            // do nothing
        }

    }

    /**
     * A {@code Disposable} is a reference to a disposable injectee and related method to release
     * resources.
     */
    private static final class Disposable {
        /** The method to be invoked to release resources. */
        private final Method disposeMethod;

        /** The target injectee has to release the resources. */
        private final Object injectee;

        /**
         * Creates a new {@code Disposable} reference.
         *
         * @param disposeMethod the method to be invoked to release resources
         * @param injectee the target injectee has to release the resources
         */
        public Disposable(final Method disposeMethod, final Object injectee) {
            this.disposeMethod = disposeMethod;
            this.injectee = injectee;
        }

        /**
         * Disposes allocated resources by invoking the injectee method annotated by {@code
         * Dispose}, tracking progresses in the input {@code DisposeHandler}.
         *
         * @param disposeHandler the handler to track dispose progresses.
         */
        public void dispose(final DisposeHandler disposeHandler) {
            try {
                disposeMethod.invoke(injectee);
            } catch (final IllegalArgumentException | IllegalAccessException e) {
                disposeHandler.onError(injectee, e);
            } catch (final InvocationTargetException e) {
                disposeHandler.onError(injectee, e.getTargetException());
            }
        }

    }

}
