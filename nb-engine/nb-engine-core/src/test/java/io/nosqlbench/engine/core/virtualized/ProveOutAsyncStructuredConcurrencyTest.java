/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.engine.core.virtualized;

import org.junit.jupiter.api.Test;

import java.util.concurrent.StructuredTaskScope;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <H2>Structured Asynchronous Execution</H2>
 * <P>This example shows that it is possible to combine structured concurrency support with
 * asynchronous tasks which run within the context of a StructuredTaskScope.</P>
 *
 * <P>The normative usage of StructuredTaskScope is documented as an ordered pattern of
 * <OL>
 * <LI>initializing the scope</LI>
 * <LI>(and then) forking subtasks</LI>
 * <LI>(and then) joining subtasks</LI>
 * </OL>
 * Asynchronous task structure deviates from this because it is not known what the explicit subtask structure
 * will be, or when subtasks will be forked into the context. In this scenario, the lifetime of the task scope is
 * not strictly bounded to a set of predetermined, subtasks and thus must be closed by some other
 * intentional side-effect.
 * </P>
 *
 * <P>This can be supported within the lifetime of the task scope as long as some pending task keeps the scope open
 * until it is needed to be closed. For this, a <EM>lifeline task</EM> is used as a proxy for the lifetime of the
 * task scope.</P>
 * <p>
 * The distinct error handling flows are tested independently and documented within the examples below.
 */
public class ProveOutAsyncStructuredConcurrencyTest {

    /**
     * <H3>Normal execution</H3>
     * <OL>
     * <LI>(exit on error) task scope init</LI>
     * <LI>lifetime task starts</LI>
     * <LI>multiple subtasks are submitted and run to completion with no exceptions</LI>
     * <LI>lifetime task stops with no exception</LI>
     * <LI>task scope completes with no exception</LI>
     * </OL>
     */
    @Test
    public void testStructuredConcurrencyHappyPath() {
        StructuredTaskScope.ShutdownOnFailure sts = new StructuredTaskScope.ShutdownOnFailure();
        sts.fork(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("lifeline thread at " + i);
//                    if (i==9) {
//                        throw new RuntimeException("failure on 9");
//                    }

                Thread.sleep(i + 1000);
            }
            return 10;
        });
        for (int i = 0; i < 10; i++) {
            int finalI = i;

            StructuredTaskScope.Subtask<Integer> st = sts.fork(() -> {
                    Thread.sleep(finalI * 500);
                    System.out.println("i:" + finalI);
//                    if (finalI==9) {
//                        throw new RuntimeException("failure on 9");
//                    }
                    return finalI;
                }
            );
        }
        try {
            sts.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(sts.exception()).isEmpty();
    }

    /**
     * <H3>Lifecycle Error</H3>
     * <OL>
     * <LI>(exit on error) task scope init</LI>
     * <LI>lifetime task starts</LI>
     * <LI>multiple subtasks are submitted and run to completion with no exceptions</LI>
     * <LI>lifetime task stops <em>WITH</em> exception</LI>
     * <LI>task scope completes with (lifetime task) exception</LI>
     * </OL>
     */
    @Test
    public void testStructuredConcurrencyLifetimeTaskError() {
        StructuredTaskScope.ShutdownOnFailure sts = new StructuredTaskScope.ShutdownOnFailure();
        sts.fork(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("lifeline thread at " + i);
                if (i == 9) {
                    throw new RuntimeException("lifetime failure on 9");
                }

                Thread.sleep(i + 1000);
            }
            return 10;
        });
        for (int i = 0; i < 10; i++) {
            int finalI = i;

            StructuredTaskScope.Subtask<Integer> st = sts.fork(() -> {
                    Thread.sleep(finalI * 500);
                    System.out.println("i:" + finalI);
                    return finalI;
                }
            );
        }
        try {
            sts.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(sts.exception()).isPresent();
        assertThat(sts.exception().toString()).matches(Pattern.compile(".*lifetime failure.*"));

    }


    /**
     * <H3>Subtask Error</H3>
     * <OL>
     * <LI>(exit on error) task scope init</LI>
     * <LI>lifetime task starts</LI>
     * <LI>at least one subtasks is submitted which throws an exception</LI>
     * <LI>lifetime task stops <em>WITH</em> exception</LI>
     * <LI>task scope completes with (subtask) exception</LI>
     * </OL>
     */
    @Test
    public void testStructuredConcurrencySubtaskError() {
        StructuredTaskScope.ShutdownOnFailure sts = new StructuredTaskScope.ShutdownOnFailure();
        sts.fork(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("lifeline thread at " + i);
                Thread.sleep(i + 1000);
            }
            return 10;
        });
        for (int i = 0; i < 10; i++) {
            int finalI = i;

            StructuredTaskScope.Subtask<Integer> st = sts.fork(() -> {
                    Thread.sleep(finalI * 500);
                    System.out.println("i:" + finalI);
                    if (finalI == 9) {
                        throw new RuntimeException("subtask failure on 9");
                    }
                    return finalI;
                }
            );
        }
        try {
            sts.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(sts.exception()).isPresent();
        assertThat(sts.exception().toString()).matches(Pattern.compile(".*subtask failure.*"));
    }

}
