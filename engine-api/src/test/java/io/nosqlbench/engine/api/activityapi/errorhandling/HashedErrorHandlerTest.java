/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.api.activityapi.errorhandling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class HashedErrorHandlerTest {

    HashedErrorHandler<Throwable, Boolean> handler;

    @BeforeEach
    public void beforeTest() {
        handler = new HashedErrorHandler<Throwable,Boolean>();
    }

    @Test
    public void testDefaultHandlerThrowsException() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
            handler.handleError(1L, new InvalidParameterException("this is an invalid exception, actually"));
        });
    }

    @Test
    public void testSuperclassErrorHandler() {
        List<CycleErrorHandler.Triple> errorList = new ArrayList<>();
        handler.setHandlerForClasses(
                CycleErrorHandlers.store(errorList, true), IndexOutOfBoundsException.class
        );
        try {
            String[] a = new String[10];
            System.out.println(a[20]);
        } catch (Exception e) {
            handler.handleError(2L, e);
        }
        assertThat(errorList).hasSize(1);
    }

    @Test
    public void testDefaultOverride() {
        List<CycleErrorHandler.Triple> list = new ArrayList<>();
        RuntimeException myError = new RuntimeException("none here");
        handler.setDefaultHandler(CycleErrorHandlers.store(list, true));
        handler.handleError(3L, myError, "an error");
        assertThat(list.get(0).cycle).isEqualTo(3L);
        assertThat(list.get(0).error).isEqualTo(myError);
        assertThat(list.get(0).msg).isEqualTo("an error");
    }

    @Test
    public void testExactClassHandler() {
        List<CycleErrorHandler.Triple> list = new ArrayList<>();
        handler.setHandlerForClasses(
                CycleErrorHandlers.store(list, true),
                StringIndexOutOfBoundsException.class

        );
        handler.setHandlerForClasses(
                CycleErrorHandlers.store(list, false), IndexOutOfBoundsException.class
        );

        try {
            String[] a = new String[10];
            System.out.println(a[20]);

        } catch (Exception e) {
            handler.handleError(2L, e);
        }
        assertThat(list.get(0).result).isOfAnyClassIn(Boolean.class);
        boolean result = (boolean) list.get(0).result;
        assertThat(result).isFalse();
    }

    @Test
    public void testNamedGroup() {
        handler.setGroup("test1",IndexOutOfBoundsException.class,ArrayIndexOutOfBoundsException.class);
        handler.setGroup("types",InvalidParameterException.class);
        handler.setHandlerForGroup("types",CycleErrorHandlers.rethrow("testNamedGroup"));
        assertThat(handler.getGroupNames()).hasSize(2);
        assertThat(handler.getGroupNames()).contains("test1");
        assertThat(handler.getGroupNames()).contains("types");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
            handler.handleError(5L,new InvalidParameterException("this is an error")));
    }

    @Test
    public void testFindVagueSingleSubmatchException() {
        handler.setGroup("index", IndexOutOfBoundsException.class, ArrayIndexOutOfBoundsException.class);
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                handler.setHandlerForPattern("Index", CycleErrorHandlers.rethrow("12345 678910 11 12")));
    }

    @Test
    public void testFindMultipleRegex() {
        handler.setGroup("index", IndexOutOfBoundsException.class, ArrayIndexOutOfBoundsException.class);
        handler.setHandlerForPattern(".*Index.*", CycleErrorHandlers.rethrow("Journey through the klein bottle."));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
            handler.handleError(9L, new IndexOutOfBoundsException("9L was out of bounds")));
    }

    @Test
    public void testNonMatchingSubstringException() {
        handler.setGroup("index", IndexOutOfBoundsException.class, ArrayIndexOutOfBoundsException.class);
        Set<Class<? extends Throwable>> groups = handler.getGroup("index");
        assertThat(groups).isNotNull();
        assertThat(groups).hasSize(2);
        assertThat(groups.contains(IndexOutOfBoundsException.class)).isTrue();
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
            handler.setHandlerForPattern("Dyahwemo", CycleErrorHandlers.rethrow("Journey through the klein bottle.")));
    }

    @Test
    public void testSetHandlerForMissingGroupException() {
        handler.setGroup("index", IndexOutOfBoundsException.class, ArrayIndexOutOfBoundsException.class);
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
            handler.setHandlerForGroup("outdex", CycleErrorHandlers.rethrow("Journey through the klein bottle.")));
    }

    @Test
    public void testResetAllClassHandlers() {
        handler.setGroup("some", IndexOutOfBoundsException.class, ArrayIndexOutOfBoundsException.class);
        handler.setHandlerForGroup("some", CycleErrorHandlers.rethrow("Octothorpe and Umlaut are friends."));
        assertThat(handler.getHandlers()).hasSize(2);
        handler.resetAllClassHandlers();
        assertThat(handler.getHandlers()).hasSize(0);
    }
}
