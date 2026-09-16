/*
 * Copyright (c) 2026 The NoSQLBench Authors.
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
package io.nosqlbench.vectordata;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// The selector grammar, value readings, matching rules, and the way a
/// spec splits into a dataset head and a selector — the reference's
/// cases, so the two runtimes agree on every spec.
@Tag("unit")
class ProfileSelectorTest {

    private static ProfileFacts facts(String name, Object... attributes) {
        return profile(name, null, null, false, null, attributes);
    }

    private static ProfileFacts profile(String name, Long baseCount, Integer maxk, boolean partition, String inherits, Object... attributes) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < attributes.length; i += 2) map.put((String) attributes[i], attributes[i + 1]);
        return new ProfileFacts(name, baseCount, maxk, partition, inherits, map);
    }

    private static List<ProfileFacts> tessera() {
        List<ProfileFacts> all = new ArrayList<>();
        all.add(profile("default", 495_930_736L, null, false, null, "size", "495m", "family", "stratified", "predicates", "mixed"));
        all.add(profile("10m", 10_000_000L, null, false, "default", "size", "10m", "family", "stratified", "predicates", "mixed",
            "selectivity_ladder", List.of(1e-1, 1e-2, 1e-3)));
        all.add(profile("10m-uniform-2-1e-2", 10_000_000L, null, false, "10m-unfiltered", "size", "10m", "family", "uniform", "predicates", "uniform-2", "selectivity", 1e-2));
        all.add(profile("10m-uniform-2-1e-3", 10_000_000L, null, false, null, "size", "10m", "family", "uniform", "predicates", "uniform-2", "selectivity", 1e-3));
        all.add(profile("100m", 100_000_000L, null, false, null, "size", "100m", "family", "stratified", "predicates", "mixed"));
        all.add(profile("label-3", null, null, true, null, "nested", Map.of("kind", "oracle")));
        return all;
    }

    private static List<String> names(String selector, List<ProfileFacts> all) { return ProfileSelector.parse(selector).select(all); }

    /// A bare name is the literal profile, never a set.
    @Test void aBareNameIsOneProfile() {
        List<ProfileFacts> all = tessera();
        assertEquals(List.of("10m"), names("10m", all));
        assertEquals(List.of("10m"), names("10M", all), "case folds");
        assertTrue(names("10", all).isEmpty(), "a literal is not a prefix");
        assertEquals("10m", ProfileSelector.parse("10m").bareName().orElseThrow());
        assertTrue(ProfileSelector.parse("profile=10m").bareName().isEmpty());
    }

    /// Each spelling has one reading.
    @Test void valuesAreReadByTheirSpelling() {
        List<ProfileFacts> all = tessera();
        assertEquals(List.of("10m", "10m-uniform-2-1e-2", "10m-uniform-2-1e-3"), names("profile=10m*", all));
        assertEquals(List.of("10m-uniform-2-1e-2", "10m-uniform-2-1e-3"), names("profile=^10m-.*$", all));
        assertEquals(List.of("10m"), names("profile=1?m", all));
        assertEquals(List.of("10m"), names("profile=[1]0m", all));
        assertEquals(List.of("10m-uniform-2-1e-3"), names("selectivity=1e-3..1e-2", all));
        assertEquals(List.of("label-3"), names("partition=true", all));
        assertEquals(List.of("10m-uniform-2-1e-2", "10m-uniform-2-1e-3"), names("family=uniform", all));
        // Quoted: a literal that would otherwise be a glob.
        ProfileFacts quoted = facts("odd", "form", "a*b");
        assertTrue(ProfileSelector.parse("form='a*b'").matches(quoted));
        assertFalse(ProfileSelector.parse("form='a?b'").matches(quoted));
    }

    /// Numbers compare numerically under the count rule, and case folds
    /// on keys, values, and suffixes.
    @Test void numbersCompareUnderTheCountRule() {
        List<ProfileFacts> all = tessera();
        assertEquals(List.of("10m-uniform-2-1e-3"), names("selectivity=0.001", all));
        assertEquals(List.of("default", "100m"), names("size>=100m", all));
        assertEquals(List.of("default", "100m"), names("SIZE>=100M", all));
        assertEquals(List.of("10m", "10m-uniform-2-1e-2", "10m-uniform-2-1e-3"), names("size=10M", all));
        assertEquals(List.of("10m", "10m-uniform-2-1e-2", "10m-uniform-2-1e-3"), names("base_count<20m", all));
        assertEquals(List.of("10m-uniform-2-1e-2", "10m-uniform-2-1e-3"), names("Family=UNIFORM", all));
        assertEquals(134_217_728.0, ProfileSelector.parseNumber("128mi"));
        assertEquals(134_217_728.0, ProfileSelector.parseNumber("128Mi"));
        assertEquals(100_000.0, ProfileSelector.parseNumber("100k"));
        assertEquals(0.001, ProfileSelector.parseNumber("1e-3"));
        assertNull(ProfileSelector.parseNumber("default"));
        assertNull(ProfileSelector.parseNumber("10f"), "a Java float suffix is not a number here");
    }

    /// The RE2 subset is normative; a lookaround is refused.
    @Test void aLookaroundRegexIsRefused() {
        SelectionException err = assertThrows(SelectionException.class, () -> ProfileSelector.parse("profile=^(?=1)0m$"));
        assertEquals(SelectionException.Kind.SYNTAX, err.kind());
        assertTrue(err.getMessage().contains("regular expression"), err.getMessage());
        assertThrows(SelectionException.class, () -> ProfileSelector.parse("profile=^(a)\\1$"), "a backreference is outside RE2");
    }

    /// A comparison against a string is false, not an error; an absent
    /// attribute matches nothing, even under `!=`.
    @Test void stringsAndAbsenceAreFalseNotErrors() {
        List<ProfileFacts> all = tessera();
        assertTrue(names("family<1", all).isEmpty());
        assertEquals(2, names("selectivity!=1", all).size(), "only profiles that have the key");
        assertEquals(all.size() - 1, names("not(selectivity=1e-2)", all).size(), "not() is how to say anything but, undescribed included");
    }

    /// A list matches on any element, each atom on its own; a map is
    /// reached with a dotted key.
    @Test void aListMatchesOnAnyElement() {
        List<ProfileFacts> all = tessera();
        assertEquals(List.of("10m"), names("selectivity_ladder=1e-2", all));
        assertEquals(List.of("10m"), names("selectivity_ladder>=1e-3,selectivity_ladder<1e-2", all));
        assertEquals(List.of("10m"), names("selectivity_ladder=1e-3..1e-2", all));
        assertTrue(names("nested=oracle", all).isEmpty(), "a map as a whole matches nothing");
        assertEquals(List.of("label-3"), names("nested.kind=oracle", all));
    }

    /// Junctions compose, keys repeat, bare names are terms.
    @Test void junctionsComposeAndKeysRepeat() {
        List<ProfileFacts> all = tessera();
        assertEquals(List.of("10m", "10m-uniform-2-1e-2", "10m-uniform-2-1e-3"), names("size=10m,or(family=uniform,predicates=mixed)", all));
        assertEquals(List.of("default", "10m", "100m", "label-3"), names("not(size=10m,family=uniform)", all));
        assertEquals(List.of("10m-uniform-2-1e-2", "10m-uniform-2-1e-3"), names("and(size=10m,family=uniform)", all));
        assertEquals(List.of("10m-uniform-2-1e-3"), names("selectivity>=1e-3,selectivity<1e-2", all));
        assertEquals(List.of("10m", "100m"), names("or(size=10m,size=100m),family=stratified", all));
        assertTrue(names("size=10m,size=100m", all).isEmpty(), "two equalities on a scalar under AND");
        assertEquals(List.of("10m", "100m"), names("or(10m,100m)", all));
        assertEquals(4, names("or(profile=10m*,profile=^100.*$)", all).size());
        assertEquals(2, names("size=10m, family = uniform", all).size(), "whitespace is ignored");
    }

    /// Structural keys select, and shadow attributes.
    @Test void structuralKeysSelectBeforeAttributes() {
        List<ProfileFacts> all = tessera();
        assertEquals(List.of("10m"), names("inherits=default", all));
        assertEquals(3, names("base_count=10000000", all).size());
        ProfileFacts shadowed = profile("x", 5L, null, false, null, "base_count", 99.0);
        assertTrue(ProfileSelector.parse("base_count=5").matches(shadowed));
        assertFalse(ProfileSelector.parse("base_count=99").matches(shadowed));
    }

    /// The head is found by shape; a bad selector is a selector error,
    /// never a lookup.
    @Test void theHeadIsFoundByShape() {
        DatasetSpec spec = DatasetSpec.parse("https://host:8080/ds");
        assertEquals("https://host:8080/ds", spec.head());
        assertFalse(spec.hasSelector());
        spec = DatasetSpec.parse("https://host/ds:10m");
        assertEquals("https://host/ds", spec.head());
        assertEquals("10m", spec.selector().bareName().orElseThrow());
        spec = DatasetSpec.parse("tessera:profile=^a:b$");
        assertEquals("tessera", spec.head());
        assertTrue(spec.selector().matches(facts("a:b")));
        assertEquals("C:\\data\\ds", DatasetSpec.parse("C:\\data\\ds:10m").head());
        assertFalse(DatasetSpec.parse("./ds").hasSelector());
        assertFalse(DatasetSpec.parse("tessera:").hasSelector(), "an empty selector is no selector");
        assertArrayEquals(new String[] {"tessera", "size="}, DatasetSpec.splitHead("tessera:size="));
        SelectionException err = assertThrows(SelectionException.class, () -> DatasetSpec.parse("tessera:size=10m,"));
        assertTrue(err.position() >= "tessera:".length(), err.getMessage());
        err = assertThrows(SelectionException.class, () -> DatasetSpec.parse("tessera:size>abc"));
        assertTrue(err.getMessage().contains("compares numbers"), err.getMessage());
        err = assertThrows(SelectionException.class, () -> DatasetSpec.parse("tessera:size 10m"));
        assertTrue(err.getMessage().contains("operator"), err.getMessage());
        err = assertThrows(SelectionException.class, () -> ProfileSelector.parse("size==10m"));
        assertTrue(err.getMessage().contains("quote it"), err.getMessage());
        assertThrows(SelectionException.class, () -> ProfileSelector.parse("or(10m"));
        assertThrows(SelectionException.class, () -> ProfileSelector.parse("form='unterminated"));
        assertThrows(SelectionException.class, () -> ProfileSelector.parse("   "));
    }

    @Test void globsMatchWholeText() {
        assertTrue(ProfileSelector.globMatch("10m*", "10m-uniform"));
        assertFalse(ProfileSelector.globMatch("10m", "10m-uniform"));
        assertTrue(ProfileSelector.globMatch("*form*", "10m-uniform"));
        assertTrue(ProfileSelector.globMatch("1?m", "10m"));
        assertTrue(ProfileSelector.globMatch("[a-c]x", "bx"));
        assertFalse(ProfileSelector.globMatch("[!a-c]x", "bx"));
    }

    /// No selector means `default`, `profile=*` means all, and a single
    /// surface refuses a set.
    @Test void resolutionDefaultsAndRefusesAmbiguity() {
        List<ProfileFacts> offer = List.of(facts("default"), facts("10m", "size", "10m"), facts("20m", "size", "20m"));
        assertEquals(List.of("default"), ProfileSelector.resolve(null, offer));
        assertEquals("default", ProfileSelector.resolveOne(null, offer));
        assertEquals(List.of("default", "10m", "20m"), ProfileSelector.resolve("profile=*", offer));
        assertEquals("10m", ProfileSelector.resolveOne("10M", offer));
        SelectionException ambiguous = assertThrows(SelectionException.class, () -> ProfileSelector.resolveOne("or(10m, 20m)", offer));
        assertEquals(SelectionException.Kind.AMBIGUOUS, ambiguous.kind());
        assertEquals(List.of("10m", "20m"), ambiguous.matches());
        SelectionException none = assertThrows(SelectionException.class, () -> ProfileSelector.resolve("size=99m", offer));
        assertEquals(SelectionException.Kind.NO_MATCH, none.kind());
        assertTrue(none.getMessage().contains("matches no profile"), none.getMessage());
        assertTrue(none.getMessage().contains("10m (size=10m)"), "the offer lists attributes: " + none.getMessage());
        assertEquals(SelectionException.Kind.SYNTAX, assertThrows(SelectionException.class, () -> ProfileSelector.resolve("size==", offer)).kind());
        assertEquals(SelectionException.Kind.SYNTAX, assertThrows(SelectionException.class, () -> ProfileSelector.resolve("", offer)).kind(),
            "an empty selector is a syntax error; no selector is spelled by giving none");
        SelectionException noDefault = assertThrows(SelectionException.class, () -> ProfileSelector.resolve(null, List.of(facts("10m"))));
        assertEquals(SelectionException.Kind.NO_DEFAULT, noDefault.kind());
        assertTrue(noDefault.getMessage().contains("name one of: 10m"), noDefault.getMessage());
    }

    @Test void aSummaryNamesEverythingASelectorReads() {
        ProfileFacts full = profile("10m", 10L, 100, false, "default", "size", "10m", "ladder", List.of(1, 2), "flag", true, "ratio", 0.5, "note", null);
        assertEquals("10m (base_count=10, maxk=100, inherits=default, size=10m, ladder=[1, 2], flag=true, ratio=0.5, note=~)", full.summary());
        assertEquals("p (partition=true)", profile("p", null, null, true, null).summary());
        assertEquals("bare", facts("bare").summary());
        assertEquals("1", ProfileFacts.canonicalText(1.0), "an integral float renders without its point, as YAML would");
        assertEquals("10000000", ProfileFacts.canonicalText(1e7), "and never with an exponent");
    }
}
