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

import com.google.inject.matcher.Matcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Guice module to register methods to be invoked when {@link Disposer#dispose()} is invoked.
 */
public final class DisposeModule
        extends AbstractLifeCycleModule {

    /**
     * Creates a new module which register methods annotated with {@link Dispose} on methods in any
     * type.
     */
    public DisposeModule() {
        super(Dispose.class);
    }

    /**
     * Creates a new module which register methods annotated with input annotation on methods in
     * types filtered by the input matcher.
     *
     * @param disposeAnnotationType the <i>Dispose</i> annotation to be searched.
     * @param typeMatcher the filter for injectee types.
     */
    public <A extends Annotation> DisposeModule(final Class<A> disposeAnnotationType,
            final Matcher<Object> typeMatcher) {
        super(disposeAnnotationType, typeMatcher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        final Disposer disposer = new Disposer();

        bind(Disposer.class).toInstance(disposer);

        bindListener(getTypeMatcher(), new AbstractMethodTypeListener(getAnnotationType()) {

            @Override
            protected <I> void hear(final Method disposeMethod, final TypeEncounter<I> encounter) {
                encounter.register((InjectionListener<I>) injectee -> disposer
                        .register(disposeMethod, injectee));
            }

        });
    }

}
