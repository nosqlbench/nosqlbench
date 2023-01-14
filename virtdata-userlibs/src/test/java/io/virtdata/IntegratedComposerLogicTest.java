/*
 * Copyright (c) 2022 nosqlbench
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

package io.virtdata;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Identity;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.NumberNameToString;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.Template;
import org.apache.commons.lang3.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class IntegratedComposerLogicTest {
    private final static Logger logger = LogManager.getLogger(IntegratedComposerLogicTest.class);

    @Test
    public void testPreferredReturnType() {
        DataMapper<Object> mapper = VirtData.getMapper("Mod(5)");
        Object o = mapper.get(1L);
        assertThat(o).isOfAnyClassIn(Long.class);
    }

    @Test
    public void testSignatureMapping() {
        Optional<DataMapper<Object>> dataMapper = VirtData.getOptionalMapper(
                "HashRange(1000000000,9999999999L); ToString() -> String"
        );
        assertThat(dataMapper).isNotNull();
        assertThat(dataMapper).isPresent();
        Object v = dataMapper.get().get(5L);
        assertThat(v).isNotNull();
    }

    @Test
    public void  testIntegratedComposer() {
        Optional<DataMapper<Object>> dataMapper = VirtData.getOptionalMapper(
                "Binomial(8,0.5); ToDate() -> java.util.Date"
        );
        assertThat(dataMapper).isNotNull();
        assertThat(dataMapper).isPresent();
        assertThat(dataMapper.get().get(1)).isNotNull();
    }

    @Test
    public void testComplexComposition() {
        Optional<DataMapper<Object>> dataMapper = VirtData.getOptionalMapper(
                "Hash(); Normal(50,10.0,'map'); Add(50); ToString(); Suffix('avgdays') -> String"
        );
        assertThat(dataMapper).isNotNull();
        assertThat(dataMapper.isPresent()).isTrue();
        assertThat(dataMapper.get().get(1)).isNotNull();
//        for (int i = 0; i < 1000; i++) {
//            System.out.println(dataMapper.getInverseCumulativeDensity().getInverseCumulativeDensity(i));
//        }
    }

    @Test
    public void testComposerOnly() {
        Optional<DataMapper<Object>> dataMapper = VirtData.getOptionalMapper("Add(5)");
        assertThat(dataMapper.isPresent()).isTrue();
    }

    @Test
    public void testResourceLoader() {
        Optional<DataMapper<Object>> dataMapper = VirtData.getOptionalMapper(" ModuloLineToString('data/variable_words.txt') -> String");
        assertThat(dataMapper).isPresent();
        assertThat(dataMapper.get().get(1)).isEqualTo("completion_count");
        dataMapper = VirtData.getOptionalMapper("ModuloLineToString('variable_words.txt') -> String");
        assertThat(dataMapper).isPresent();
        assertThat(dataMapper.get().get(1)).isEqualTo("completion_count");
    }

    @Test
    public void testNestedFunction() {
        Template t = new Template("_{}_{}_", (Function) String::valueOf, (LongFunction<?>) String::valueOf);
        String r = t.apply(5);
        assertThat(r).isEqualTo("_5_6_");
        Optional<DataMapper<String>> m2 = VirtData.getOptionalMapper("Template('_{}_',long->NumberNameToString()->java.lang.String)");
        assertThat(m2).isPresent();
        DataMapper<String> dm2 = m2.get();
        Object r3 = dm2.get(42L);

    }

    @Test
    public void testBrokenTemplate() {
        Optional<DataMapper<String>> m2 = VirtData.getOptionalMapper("Template('{\"alt1-{}\",\"alt2-{}\"}',ToLongFunction(Identity()),ToLongFunction(Identity()))");
        assertThat(m2).isPresent();
        DataMapper<String> dm2 = m2.get();
        Object r3 = dm2.get(42L);

    }

    @Test
    public void testNegativeLongs() {
        Optional<DataMapper<Long>> mo = VirtData.getOptionalMapper("HashRange(-2147483648L,2147483647L) -> long");
        assertThat(mo).isPresent();
        DataMapper<Long> longDataMapper = mo.get();
        Long result = longDataMapper.get(5L);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(359183748L);
    }

    @Test
    public void testBrokenFlow() {
        Optional<DataMapper<String>> mo = VirtData.getOptionalMapper("HashRange(-2147483648L,2147483647L) -> long");
        assertThat(mo).isPresent();

    }

    @Test
    public void testConversionMatchingManual() {
        Optional<DataMapper<String>> tm = VirtData.getOptionalMapper("ToEpochTimeUUID(); java.lang.Object -> ToString() -> String");
        assertThat(tm).isPresent();
        String s = tm.get().get(55L);
        assertThat(s).isEqualTo("1389a470-1dd2-11b2-8000-000000000000");
    }

    @Test
    public void testConversionMatchingAuto() {
        Optional<DataMapper<String>> tm = VirtData.getOptionalMapper("ToEpochTimeUUID(); ToString()");
        assertThat(tm).isPresent();
        String s = tm.get().get(55L);
        assertThat(s).isEqualTo("1389a470-1dd2-11b2-8000-000000000000");
    }

    @Test
    public void sanityCheckFunctionCasting() {
        Class<?> c1 = NumberNameToString.class;
        Class<?> c2 = LongFunction.class;
        assertThat(ClassUtils.isAssignable(c1, c2)).isTrue();

        Class<?> c3 = Identity.class;
        Class<?> c4 = LongFunction.class;
        assertThat(ClassUtils.isAssignable(c3, c4)).isFalse();
        LongUnaryOperator f;
        f = new Identity();
    }

    @Test
    public void testVirtDataTypeVarianceError() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> VirtData.getMapper("Uniform(0.0D,1.0D) -> java.lang.String", long.class));
    }

    @Test
    public void testAPILevelQualifier() {
        DataMapper mapper1 = VirtData.getMapper("Uniform(0,100)");
        Object o1 = mapper1.get(5L);
        assertThat(o1).isOfAnyClassIn(Long.class);

        DataMapper mapper2 = VirtData.getMapper("Uniform(0,100)", double.class);
        Object o2 = mapper2.get(5L);
        assertThat(o2).isOfAnyClassIn(Double.class);
    }

    @Test
    public void testTypeCoersionWorksForSimpleCases() {
        Optional<DataMapper<Object>> om = VirtData.getOptionalMapper("Add(200000); Div(10); ToEpochTimeUUID()->java.util.UUID; ToString();");
        assertThat(om).isPresent();
        DataMapper<Object> m = om.get();
        Object o = m.get(5L);
        assertThat(o).isOfAnyClassIn(String.class);
    }
}
