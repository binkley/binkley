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

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.inject.ProvisionException;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;

/**
 * Guice module to register methods to be invoked after injection is complete.
 */
public final class AfterInjectionModule
    extends AbstractLifeCycleModule
{

    /**
     * Creates a new module which register methods annotated with {@link AfterInjection} on methods in any type.
     */
    public AfterInjectionModule()
    {
        super( AfterInjection.class );
    }

    /**
     * Creates a new module which register methods annotated with input annotation on methods
     * in types filtered by the input matcher.
     *
     * @param afterInjectionAnnotationType the <i>AfterInjection</i> annotation to be searched.
     * @param typeMatcher the filter for injectee types.
     */
    public <A extends Annotation> AfterInjectionModule( Class<A> afterInjectionAnnotationType,
                                                        Matcher<Object> typeMatcher )
    {
        super( afterInjectionAnnotationType, typeMatcher );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure()
    {
        bindListener( getTypeMatcher(), new AbstractMethodTypeListener( getAnnotationType() )
        {

            @Override
            protected <I> void hear( final Method method, TypeEncounter<I> encounter )
            {
                encounter.register( new InjectionListener<I>()
                {

                    public void afterInjection( I injectee )
                    {
                        try
                        {
                            method.invoke( injectee );
                        }
                        catch ( IllegalArgumentException e )
                        {
                            // should not happen, anyway...
                            throw new ProvisionException( format( "Method @%s %s requires arguments",
                                                                  getAnnotationType().getName(),
                                                                  method ), e );
                        }
                        catch ( IllegalAccessException e )
                        {
                            throw new ProvisionException( format( "Impossible to access to @%s %s on %s",
                                                                  getAnnotationType().getName(),
                                                                  method,
                                                                  injectee ), e );
                        }
                        catch ( InvocationTargetException e )
                        {
                            throw new ProvisionException( format( "An error occurred while invoking @%s %s on %s",
                                                                  getAnnotationType().getName(),
                                                                  method,
                                                                  injectee ), e.getTargetException() );
                        }
                    }

                } );
            }

        } );
    }

}
