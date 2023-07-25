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

package io.nosqlbench.engine.api.activityapi;

import io.nosqlbench.api.engine.activityimpl.ParameterMap;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ParameterMapTest {

    @Test
    public void testNullStringYieldsNothing() {
        Optional<ParameterMap> parameterMap = ParameterMap.parseParams(null);
        assertThat(parameterMap.isPresent()).isFalse();
    }

    @Test
    public void testEmptyStringYieldsEmptyMap() {
        Optional<ParameterMap> parameterMap = ParameterMap.parseParams("");
        assertThat(parameterMap.isPresent()).isTrue();
    }

    @Test
    public void testUnparsableYieldsNothing() {
        Optional<ParameterMap> unparseable = ParameterMap.parseParams("woejfslkdjf");
        assertThat(unparseable.isPresent()).isFalse();

    }

    @Test
    public void testGetLongParam() {
        Optional<ParameterMap> longOnly = ParameterMap.parseParams("longval=234433;");
        assertThat(longOnly.isPresent()).isTrue();
        assertThat(longOnly.get().getOptionalLong("longval").orElse(12345L)).isEqualTo(234433L);
        assertThat(longOnly.get().getOptionalLong("missing").orElse(12345L)).isEqualTo(12345L);
    }

    @Test
    public void testGetDoubleParam() {
        Optional<ParameterMap> doubleOnly = ParameterMap.parseParams("doubleval=2.34433;");
        assertThat(doubleOnly.isPresent()).isTrue();
        assertThat(doubleOnly.get().getOptionalDouble("doubleval").orElse(3.4567d)).isEqualTo(2.34433d);
        assertThat(doubleOnly.get().getOptionalDouble("missing").orElse(3.4567d)).isEqualTo(3.4567d);
    }

    @Test
    public void testGetStringParam() {
        Optional<ParameterMap> stringOnly = ParameterMap.parseParams("stringval=avalue;");
        assertThat(stringOnly.isPresent()).isTrue();
        assertThat(stringOnly.get().getOptionalString("stringval").orElse("othervalue")).isEqualTo("avalue");
        assertThat(stringOnly.get().getOptionalString("missing").orElse("othervalue")).isEqualTo("othervalue");
    }

    @Test
    public void testGetStringStringParam() {
        Optional<ParameterMap> stringOnly = ParameterMap.parseParams("stringval=avalue;stringval2=avalue2;");
        assertThat(stringOnly.isPresent()).isTrue();
        assertThat(stringOnly.get().getOptionalString("stringval").orElse("othervalue")).isEqualTo("avalue");
        assertThat(stringOnly.get().getOptionalString("stringval2").orElse("othervalue1")).isEqualTo("avalue2");
    }

    @Test
    public void testMultiNameStringYieldsValue() {
        Optional<ParameterMap> multiNames = ParameterMap.parseParams("alpha=blue;beta=red;delta=blue");
        assertThat(multiNames).isPresent();
        assertThat(multiNames.get().getOptionalString("delta","gamma").orElse("missing")).isEqualTo("blue");
    }

    @Test
    public void testAmbiguousMultiValueThrowsException() {
        Optional<ParameterMap> multiNames = ParameterMap.parseParams("alpha=blue;beta=red;delta=blue");
        assertThat(multiNames).isPresent();
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> multiNames.get().getOptionalString("alpha","delta"));
    }

    @Test
    public void testMissingNamedParameter() {
        Optional<ParameterMap> multiNames = ParameterMap.parseParams("alpha=blue;beta=red;delta=blue");
        assertThat(multiNames).isPresent();
        Optional<ParameterMap.NamedParameter> optional = multiNames.get().getOptionalNamedParameter("alpha", "zeta");
        assertThat(optional).isPresent();
        ParameterMap.NamedParameter paramtuple = optional.get();
        assertThat(paramtuple.getName()).isEqualTo("alpha");
        assertThat(paramtuple.getValue()).isEqualTo("blue");
    }

    @Test
    public void testGetOptional() {
        ParameterMap abc = ParameterMap.parseOrException("a=1;b=2;c=3;");
        Optional<Long> d = abc.getOptionalLong("d");
        assertThat(d).isEmpty();
        Optional<String> a = abc.getOptionalString("a");
        assertThat(a).isEqualTo(Optional.of("1"));
        Optional<Long> aLong = abc.getOptionalLong("a");
        assertThat(aLong).isEqualTo(Optional.of(1L));
    }

    @Test
    public void testQuotedSemis() {
        ParameterMap abc = ParameterMap.parseOrException("a=1;b='two;three';");
    }
}
