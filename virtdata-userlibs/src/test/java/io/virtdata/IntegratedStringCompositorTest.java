package io.virtdata;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.core.bindings.Bindings;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.StringCompositor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegratedStringCompositorTest {

    private static BindingsTemplate template;
    private static Bindings bindings;

    @BeforeAll
    public static void setupTemplate() {
        BindingsTemplate bindingsTemplate = new BindingsTemplate();
        bindingsTemplate.addFieldBinding("ident","Identity()");
        bindingsTemplate.addFieldBinding("mod5", "Mod(5)");
        bindingsTemplate.addFieldBinding("mod-5", "Mod(5)");
        bindingsTemplate.addFieldBinding("5_mod_5", "Mod(5)");
        bindingsTemplate.addFieldBinding(".mod5", "Mod(5)");
        template = bindingsTemplate;
        bindings = bindingsTemplate.resolveBindings();
    }

    @Test
    public void testBindValues() {
        StringCompositor c = new StringCompositor("A{ident}C");
        String s = c.bindValues(c, bindings, 0L);
        assertThat(s).isEqualTo("A0C");
    }

    @Test
    public void testBindValuesSpecialChars() {
        StringCompositor c = new StringCompositor("A{mod-5}C");
        String s = c.bindValues(c, bindings, 6L);
        assertThat(s).isEqualTo("A1C");

        c = new StringCompositor("A{5_mod_5}C");
        s = c.bindValues(c, bindings, 7L);
        assertThat(s).isEqualTo("A2C");

//        c = new StringCompositor("A{.mod5}C");
//        s = c.bindValues(c, bindings, 8L);
//        assertThat(s).isEqualTo("A3C");
    }

//    @Test
//    public void testBindEscapedAnchor() {
//        StringCompositor c = new StringCompositor("A\\{{mod-5}C");
//        String s = c.bindValues(c, bindings, 6L);
//        assertThat(s).isEqualTo("A{1C");
//    }

    @Test
    public void testBindCustomTransform() {
        Function<Object,String> f = (o) -> "'" + o.toString() + "'";
        StringCompositor c = new StringCompositor("A{mod5}C", f);
        String s = c.bindValues(c, bindings, 13L);
        assertThat(s).isEqualTo("A'3'C");
    }

}
