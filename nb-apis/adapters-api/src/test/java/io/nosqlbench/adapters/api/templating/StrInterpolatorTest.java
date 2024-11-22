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

package io.nosqlbench.adapters.api.templating;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StrInterpolatorTest {

    private static final List<Map<String, String>> abcd = new ArrayList<Map<String, String>>() {{
        add(
                new HashMap<>() {{
                    put("akey", "aval1");
                    put("bkey", "bval1");
                    put("ckey", "cval1");
                }}
        );
        add(
                new HashMap<>() {
                    {
                        put("akey", "aval2");
                        put("bkey", "bval2");
                    }
                }
        );
        add(
                new HashMap<>() {
                    {
                        put("json-a-b", "'a': 'b'");
                    }
                }
        );
    }};



    @Test
    public void shouldReturnIdentity() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("A");
        assertThat(a).isEqualTo("A");
    }

    @Test
    public void shouldMatchSimpleSubst() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("<<akey>>");
        assertThat(a).isEqualTo("aval1");
    }

    @Test
    public void shouldMatchAlternateSubst() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("TEMPLATE(akey)");
        assertThat(a).isEqualTo("aval1");
        String b = interp.apply("TEMPLATE(nokeymatches,value2)");
        assertThat(b).isEqualTo("value2");
    }

    @Test
    public void shouldMatchNewAlternateSubst() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("${akey}");
        assertThat(a).isEqualTo("aval1");
        String b = interp.apply("${nokeymatches:value2}");
        assertThat(b).isEqualTo("value2");
    }

    @Test
    public void shouldMatchNestedParens() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("TEMPLATE(keydist,Uniform(0,1000000000)->int);");
        assertThat(a).isEqualTo("Uniform(0,1000000000)->int;");
    }

    @Test
    public void shouldMatchNestedParens2() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("${keydist:Uniform(0,1000000000)->int};");
        assertThat(a).isEqualTo("Uniform(0,1000000000)->int;");
    }

    @Test
    public void shouldMatchNestedParens3() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("${keydist:${nokeymatchesthis2:Uniform(0,1000000000)->int}};");
        assertThat(a).isEqualTo("Uniform(0,1000000000)->int;");
    }

    @Test
    public void shouldMatchWithComments() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("""
                                TEMPLATE(start,START)
                                  # TEMPLATE(blahblah,blah)
                                TEMPLATE(keydist,Uniform(0,1000000000)->int);
                                # TEMPLATE(blahblah,blah)
                                """);
        assertThat(a).isEqualTo("""
                                START
                                  # TEMPLATE(blahblah,blah)
                                Uniform(0,1000000000)->int;
                                # TEMPLATE(blahblah,blah)
                                """);
    }

    @Test
    public void sustitutionTests() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("-${setkey:=setme}-${setkey}-");
        assertThat(a).isEqualTo("-setme-setme-");
        String b = interp.apply("-${setkey2:-setme}-${setkey2:-setyou}-");
        assertThat(b).isEqualTo("-setme-setyou-");
        String c = interp.apply("-${setkey:=setme}-${setkey3:+setme}-${setkey:+setyou}-");
        assertThat(c).isEqualTo("-setme-setme-setyou-");
        String d = interp.apply("-${setkey:+${setkey3:+setme}}-");
        assertThat(d).isEqualTo("-setme-");
        String e = interp.apply("-${${setkey3:+setkey}}-");
        assertThat(e).isEqualTo("-setme-");
    }

    @Test
    public void shouldThrowException() {
        StrInterpolator interp = new StrInterpolator(abcd);

        // Test for missing variable or invalid syntax
        Exception exception = assertThrows(NullPointerException.class, () -> {
            interp.apply("-${unsetKey:?unset exception}-");
        });

        // Verify the exception message if necessary
        assertThat(exception.getMessage()).contains("unsetKey");
    }

    @Test
    public void shouldReturnWarningWhenUnmatched() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("<<nokeymatchesthis>>");
        assertThat(a).isEqualTo("UNSET:nokeymatchesthis");
    }

    @Test
    public void shouldReturnDefaultWhenNotOverridden() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("<<nokeymatchesthis:butithasadefault>>");
        assertThat(a).isEqualTo("butithasadefault");
    }

    @Test
    public void shouldOverrideDefault() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("<<bkey:bkeydefault>>");
        assertThat(a).isEqualTo("bval1");
    }

    @Test
    public void shouldWorkWithOddCharacters() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("<<unchanged:{'parm1':'val1',parm2:val2, parm3: 'val3'}>>");
        assertThat(a).isEqualTo("{'parm1':'val1',parm2:val2, parm3: 'val3'}");
    }

    @Test
    public void shouldWorkWithAllQuotes() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("<<Token:'Key': 'Value'>>");
        assertThat(a).isEqualTo("'Key': 'Value'");
    }

    @Test
    public void shouldWorkWithAllQuotesOverride() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("<<Token:'Key': 'Value'>>");
        assertThat(a).isEqualTo("'Key': 'Value'");
        StrInterpolator interp2 = new StrInterpolator(abcd);
        String b = interp2.apply("<<json-a-b:'Key': 'Value'>>");
        assertThat(b).isEqualTo("'a': 'b'");
    }

    @Test
    public void shouldWorkWithMultipleGroups() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("<<Token:'Key': 'Value'>>.<<Token2:'Stuff'>>");
        assertThat(a).isEqualTo("'Key': 'Value'.'Stuff'");
    }

    @Test
    public void shouldExpandNestedTemplates() {
        StrInterpolator interp = new StrInterpolator(abcd);
        String a = interp.apply("-TEMPLATE(akey,TEMPLATE(dkey,whee)-");
        assertThat(a).isEqualTo("-aval1-");
        String b = interp.apply("-TEMPLATE(unknown,TEMPLATE(bkey,whee))-");
        assertThat(b).isEqualTo("-bval1-");
    }

//    @Test
//    public void shouldGetBasicDetails() {
//        StrInterpolator interp = new StrInterpolator(abcd);
//        LinkedHashMap<String, String> details = interp.getTemplateDetails("-TEMPLATE(akey,TEMPLATE(dkey,whee)-");
//        assertThat(details).containsOnlyKeys("akey","dkey");
//        assertThat(details).containsValues("test1");
//
//    }

}
