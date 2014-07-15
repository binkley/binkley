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

/** A {@link DisposeHandler} instance is used to track dispose progress. */
public interface DisposeHandler {
    /**
     * Tracks the input injectee successfully released the resources.
     *
     * @param injectee the injectee to be released
     */
    <I> void onSuccess(@Nonnull final I injectee);

    /**
     * Tracks an error occurred while the input injectee released the resources.
     *
     * @param injectee the injectee to be released
     * @param error the exception occurred
     */
    <I, E extends Throwable> void onError(@Nonnull final I injectee, @Nonnull final E error);
}
