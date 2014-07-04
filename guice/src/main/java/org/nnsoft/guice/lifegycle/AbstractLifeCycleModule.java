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

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matcher;

import java.lang.annotation.Annotation;

import static com.google.inject.internal.util.$Preconditions.checkArgument;
import static com.google.inject.matcher.Matchers.any;

/**
 * Abstract implementation of a module that requires an annotation type and a type matcher.
 */
abstract class AbstractLifeCycleModule
        extends AbstractModule {

    /**
     * The annotation type the scanner will look for in the types methods.
     */
    private final Class<? extends Annotation> annotationType;

    /**
     * The type matcher to filter classes where looking for lifecycle annotations.
     */
    private final Matcher<Object> typeMatcher;

    /**
     * Creates a new module which looks for the input lifecycle annotation on methods in any type.
     *
     * @param annotationType the lifecycle annotation to be searched.
     */
    public <A extends Annotation> AbstractLifeCycleModule(final Class<A> annotationType) {
        this(annotationType, any());
    }

    /**
     * Creates a new module which looks for the input lifecycle annotation on methods in types
     * filtered by the input matcher.
     *
     * @param annotationType the lifecycle annotation to be searched.
     * @param typeMatcher the filter for injectee types.
     */
    public <A extends Annotation> AbstractLifeCycleModule(final Class<A> annotationType,
            final Matcher<Object> typeMatcher) {
        checkArgument(null != annotationType, "annotationType must be specified");
        checkArgument(null != typeMatcher, "typeMatcher must be specified");
        this.annotationType = annotationType;
        this.typeMatcher = typeMatcher;
    }

    /**
     * Returns the annotation type the scanner will look for in the types methods.
     *
     * @return The annotation type the scanner will look for in the types methods.
     */
    protected final Class<? extends Annotation> getAnnotationType() {
        return annotationType;
    }

    /**
     * Returns the type matcher to filter classes where looking for lifecycle annotations.
     *
     * @return the type matcher to filter classes where looking for lifecycle annotations.
     */
    protected final Matcher<Object> getTypeMatcher() {
        return typeMatcher;
    }

}
