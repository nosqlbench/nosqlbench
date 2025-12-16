/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.templates;

import io.nosqlbench.virtdata.core.templates.BindPoint;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;
import io.nosqlbench.virtdata.core.templates.StringCompositor;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Demonstrates the (intentional) "dedup by bindspec" behavior in {@link StringCompositor}.
 *
 * <p>In practical terms: for concat templates like {@code "L{a}M{b}R"}, the compositor builds a set of unique
 * bindspecs (the right-hand-side of the bindings map) and evaluates each unique spec once per cycle. Each bindpoint
 * then indexes into that computed value list via a LUT.</p>
 *
 * <p>This is subtle but important: it means duplicate bindspecs within a single template instance are "coalesced"
 * into a single mapper evaluation per cycle. This is mostly an optimization for pure functions, but it also defines
 * semantics if any mapper is stateful or non-deterministic.</p>
 *
 * <p>Additionally, bindpoints tagged as {@link BindPoint.Type#localref} or {@link BindPoint.Type#globalref} are not
 * executable VirtData bindspecs and must not be treated as such during spec enumeration (they are references to
 * captured values, not mapper recipes). The last tests below guard that these ref-types do not get passed into
 * {@code VirtData.getOptionalMapper(...)}.</p>
 */
public class StringCompositorDedupBehaviorTest {

    @Test
    public void shouldDeduplicateIdenticalBindspecsWithinAConcatTemplate() throws Exception {
        ParsedTemplateString pt = new ParsedTemplateString(
            "L{a}M{b}R",
            Map.of(
                "a", "FixedValue(42)",
                "b", "FixedValue(42)"
            )
        );
        StringCompositor compositor = new StringCompositor(pt, Map.of());

        DataMapper<?>[] mappers = (DataMapper<?>[]) getPrivateField(compositor, "mappers");
        int[] lut = (int[]) getPrivateField(compositor, "LUT");

        assertThat(mappers).hasSize(1);
        assertThat(lut).containsExactly(0, 0);
        assertThat(compositor.apply(123L)).isEqualTo("L42M42R");
    }

    @Test
    public void shouldNotDeduplicateDistinctBindspecsWithinAConcatTemplate() throws Exception {
        ParsedTemplateString pt = new ParsedTemplateString(
            "L{a}M{b}R",
            Map.of(
                "a", "FixedValue(1)",
                "b", "FixedValue(2)"
            )
        );
        StringCompositor compositor = new StringCompositor(pt, Map.of());

        DataMapper<?>[] mappers = (DataMapper<?>[]) getPrivateField(compositor, "mappers");
        int[] lut = (int[]) getPrivateField(compositor, "LUT");

        assertThat(mappers).hasSize(2);
        assertThat(lut).containsExactly(0, 1);
        assertThat(compositor.apply(123L)).isEqualTo("L1M2R");
    }

    @Test
    public void shouldNotTreatLocalrefBindpointsAsVirtDataBindspecs() {
        assertThatCode(() -> {
            ParsedTemplateString pt = new SwitchingRefTypeTemplate(BindPoint.Type.localref);
            StringCompositor compositor = new StringCompositor(pt, Map.of());
            assertThat(compositor.apply(7L)).isEqualTo("L42M42R");
        }).doesNotThrowAnyException();
    }

    @Test
    public void shouldNotTreatGlobalrefBindpointsAsVirtDataBindspecs() {
        assertThatCode(() -> {
            ParsedTemplateString pt = new SwitchingRefTypeTemplate(BindPoint.Type.globalref);
            StringCompositor compositor = new StringCompositor(pt, Map.of());
            assertThat(compositor.apply(7L)).isEqualTo("L42M42R");
        }).doesNotThrowAnyException();
    }

    /**
     * A small harness to isolate the "ref types must not be treated as bindspecs" behavior.
     *
     * <p>{@link BindPointParser} does not currently emit {@code localref}/{@code globalref} bindpoints, so this uses a
     * controlled override: the first {@link #getBindPoints()} call returns a ref-type bindpoint with an intentionally
     * unresolvable bindspec. If {@link StringCompositor} tried to resolve it as a VirtData mapper, the constructor would
     * fail. Subsequent {@link #getBindPoints()} calls return a normal concat template with resolvable bindspecs so the
     * compositor can finish LUT construction and produce output.</p>
     */
    private static final class SwitchingRefTypeTemplate extends ParsedTemplateString {
        private final AtomicInteger calls = new AtomicInteger();
        private final List<BindPoint> first;
        private final List<BindPoint> rest;

        private SwitchingRefTypeTemplate(BindPoint.Type refType) {
            super("L{a}M{b}R", Map.of("a", "FixedValue(42)", "b", "FixedValue(42)"));
            if (refType != BindPoint.Type.localref && refType != BindPoint.Type.globalref) {
                throw new IllegalArgumentException("Expected localref/globalref, got " + refType);
            }
            first = List.of(
                BindPoint.of("a", "NotARealMapper(1)", refType),
                BindPoint.of("b", "FixedValue(42)", BindPoint.Type.reference)
            );
            rest = List.of(
                BindPoint.of("a", "FixedValue(42)", BindPoint.Type.reference),
                BindPoint.of("b", "FixedValue(42)", BindPoint.Type.reference)
            );
        }

        @Override
        public List<BindPoint> getBindPoints() {
            return calls.getAndIncrement() == 0 ? first : rest;
        }

        @Override
        public String[] getSpans() {
            return new String[]{"L", "a", "M", "b", "R"};
        }
    }

    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}

