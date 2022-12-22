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

import io.nosqlbench.virtdata.core.bindings.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class IntegratedComposerLibraryTest {
    private final static Logger logger = LogManager.getLogger(IntegratedComposerLibraryTest.class);

    // The deprecated functions are not being included in the next release, so this test's purpose has been
    // reversed.
    @Test
    public void testArgumentMatchingViaMainLib() {
        BindingsTemplate bt = new BindingsTemplate();
        bt.addFieldBinding("param","RandomLineToString('data/variable_words.txt')");
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> bt.resolveBindings());
    }

    @Test
    public void testChainedTypeResolutionForLong() {
        BindingsTemplate bt = new BindingsTemplate();
        bt.addFieldBinding("longchain", "compose CycleRange(123456789) ; Div(3); Mod(7) -> long");
        Bindings bindings = bt.resolveBindings();
    }

    @Test
    public void testChainedTypeResolutionForWithInternalLong() {
        BindingsTemplate bt = new BindingsTemplate();
        bt.addFieldBinding("longchain", "compose HashRange(1234,6789) -> long; Mod(3) -> int;");
        Bindings bindings = bt.resolveBindings();
        Object n1 = bindings.getAll(123)[0];
        assertThat(n1).isOfAnyClassIn(Integer.class);
    }

    @Test
    public void testChainedTypeResolutionForInt() {
        BindingsTemplate bt = new BindingsTemplate();
        bt.addFieldBinding("intchain", "compose ToInt() ; CycleRange(123456789) ; Div(3) ; Mod(7) -> int");
        Bindings bindings = bt.resolveBindings();
    }

    @Test
    public void testStringConversion() {
        BindingsTemplate bt = new BindingsTemplate();
        bt.addFieldBinding("phone","compose HashRange(1000000000,9999999999L); ToString() -> String");
        Bindings bindings = bt.resolveBindings();
    }

    @Test
    public void testPrefixSuffix() {
        BindingsTemplate bt = new BindingsTemplate();
        bt.addFieldBinding("solr_query","compose HashRange(1000000000,9999999999L); ToString(); Prefix('before'); Suffix('after') -> String");
        Bindings bindings = bt.resolveBindings();
    }

    // TODO: Fix this test
    @Test
    @Disabled
    public void testTypeCoercionWhenNeeded() {
        BindingsTemplate bt = new BindingsTemplate();
        bt.addFieldBinding("mod_to_string", "compose Mod(3) ; Suffix('0000000000') -> String");
        Bindings bindings = bt.resolveBindings();
        Object[] all = bindings.getAll(5);
        assertThat(all).isNotNull();
        assertThat(all.length).isEqualTo(1);
        Object o = all[0];
        assertThat(o.getClass()).isEqualTo(String.class);
        assertThat((String) o).isEqualTo("20000000000");
    }

    // TODO: Fix this test
    @Test
    @Disabled
    public void testBasicRange() {
        BindingsTemplate bt = new BindingsTemplate();
        bt.addFieldBinding("phone","HashRange(1000000000, 9999999999)");
        Bindings bindings = bt.resolveBindings();
    }

    @Test
    public void testUUIDChain() {
        Optional<DataMapper<Object>> dm =
                VirtData.getOptionalMapper("compose Mod(1000); ToHashedUUID() -> java.util.UUID");
        assertThat(dm).isPresent();
        Object o = dm.get().get(5L);
        assertThat(o).isEqualTo(UUID.fromString("3df498b1-9568-4584-96fd-76f6081da01a"));
    }

    @Test
    public void testNormalDoubleAdd() {
        Optional<DataMapper<String>> dm =
                VirtData.getOptionalMapper("compose Normal(0.0,5.0); Add(5.0) -> double");
        assertThat(dm).isPresent();
    }

    @Test
    public void testDistInCompose() {
        Optional<DataMapper<String>> dm =
                VirtData.getOptionalMapper("compose Hash(); Uniform(0,100); ToString() -> String");
        assertThat(dm).isPresent();
        String s = dm.get().get(5L);
        assertThat(s).isNotEmpty();
        assertThat(s).isEqualTo("78");
    }

    @Test
    public void testComposeSingleFuncTypeCoercion() {
        Optional<DataMapper<Object>> longMapper =
                VirtData.getOptionalMapper("compose Uniform(1,10) -> long");
        assertThat(longMapper).isPresent();
        Object l = longMapper.get().get(23L);
        assertThat(l).isNotNull();
        assertThat(l.getClass()).isEqualTo(Long.class);

        Optional<DataMapper<Object>> intMapper =
                VirtData.getOptionalMapper("compose Uniform(1,123) -> int");
        assertThat(intMapper).isPresent();
        Object i = intMapper.get().get(23L);
        assertThat(i).isNotNull();
        assertThat(i.getClass()).isEqualTo(Integer.class);
    }

    private static Object assertMapper(String def, long cycle)
    {
        Optional<DataMapper<Object>> mapper = VirtData.getOptionalMapper(def);
        assertThat(mapper).isPresent();
        Object o = mapper.get().get(cycle);
        assertThat(o).isNotNull();
        return o;
    }

    private static void assertInteger(Object o, int expected)
    {
        assertThat(o.getClass()).isEqualTo(Integer.class);
        assertThat(o).isEqualTo(expected);
    }

    @Test
    public void testChainedHashRanges() {
        final int initialCycle = 0;
        final int intermediateCycle = 52;
        final int finalCycle = 81;

        Object intermediateValue = assertMapper("compose HashRange(0,100) -> int", 0);
        assertThat(intermediateValue).isEqualTo(52);

        Object finalValue = assertMapper("compose HashRange(0,100) -> int", intermediateCycle);
        assertThat(finalValue).isEqualTo(16);

        Object finalChainedValue = assertMapper("compose HashRange(0,100); HashRange(0,100) -> int", initialCycle);
        assertThat(finalChainedValue).isEqualTo(16);
    }

    @Test
    public void testLeadingIdentityDoesNotImpactTypes()
    {
        final int initialCycle = 0;
        final int finalCycle = 160;

        Object o1 = assertMapper("compose HashRange(0,1000); HashRange(0,1000) -> int", initialCycle);
        assertInteger(o1, finalCycle);

        Object o2 = assertMapper("compose Identity(); HashRange(0,1000); HashRange(0,1000) -> int", initialCycle);
        assertInteger(o2, finalCycle);
    }

    @Test
    public void testTemplateBindingConversion() {
        ResolverDiagnostics diag;
        diag = VirtData.getMapperDiagnostics("Uniform(0.0,1.0)");
        logger.debug(diag.toString());

        diag = VirtData.getMapperDiagnostics("Template('{}', long->Uniform(0.0D,1.0D))->double");
        logger.debug(diag.toString());
    }

}
