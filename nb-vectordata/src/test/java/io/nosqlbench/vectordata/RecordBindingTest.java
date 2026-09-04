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

import io.nosqlbench.vectordata.anode.ANode;
import io.nosqlbench.vectordata.anode.ANodeException;
import io.nosqlbench.vectordata.anode.Field;
import io.nosqlbench.vectordata.anode.MNode;
import io.nosqlbench.vectordata.anode.MNodeVernacular;
import io.nosqlbench.vectordata.anode.MValue;
import io.nosqlbench.vectordata.anode.PNode;
import io.nosqlbench.vectordata.anode.PNode.Comparand;
import io.nosqlbench.vectordata.anode.PNode.ConjugateType;
import io.nosqlbench.vectordata.anode.PNode.FieldRef;
import io.nosqlbench.vectordata.anode.PNode.OpType;
import io.nosqlbench.vectordata.anode.TypeTag;
import io.nosqlbench.vectordata.anode.Vernacular;
import io.nosqlbench.vectordata.anode.Vernaculars;
import io.nosqlbench.vectordata.binding.BindException;
import io.nosqlbench.vectordata.binding.BindType;
import io.nosqlbench.vectordata.binding.Binder;
import io.nosqlbench.vectordata.binding.Condition;
import io.nosqlbench.vectordata.binding.Form;
import io.nosqlbench.vectordata.binding.Forms;
import io.nosqlbench.vectordata.binding.Layout;
import io.nosqlbench.vectordata.binding.PredicateBinder;
import io.nosqlbench.vectordata.records.RecordFacet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Binding records to operation parameters, numbered as the acceptance
/// cases in the reference's record-binding design: a layout learned
/// once, binders compiled against it, forms declared or implicit,
/// predicates bound as parameters, and the type asymmetries that make
/// binding a contract of its own rather than a rendering.
@Tag("unit")
class RecordBindingTest {
    @TempDir Path temporary;

    private VectorDataSettings settings() { return VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build(); }

    static byte[] row(long id, String tag, double score) {
        return new MNode().insert("id", new MValue.Int(id)).insert("tag", new MValue.Text(tag)).insert("score", new MValue.Float(score)).toBytes();
    }

    static List<byte[]> utf8(String... texts) { List<byte[]> out = new ArrayList<>(); for (String t : texts) out.add(t.getBytes(StandardCharsets.UTF_8)); return out; }

    /// A dataset whose metadata facet holds `records`, with a `forms`
    /// namespace when `forms` is given.
    private Path dataset(String name, List<byte[]> records, List<byte[]> forms) throws IOException {
        Path dir = Files.createDirectories(temporary.resolve(name));
        if (forms == null) FixtureSupport.slabOf(dir, "metadata_content.slab", records, 4);
        else FixtureSupport.slabWithNamespaces(dir, "metadata_content.slab", records, Map.of(Forms.NAMESPACE, forms), 4);
        FixtureSupport.fvec(dir, "base.fvec", new float[][] {{0f, 1f, 2f, 3f}});
        Files.writeString(dir.resolve("dataset.yaml"), "name: b\nprofiles:\n  default:\n    base_vectors: base.fvec\n    metadata_content: metadata_content.slab\n");
        return dir;
    }

    private RecordFacet open(Path dir) { return TestDataGroup.load(dir.toUri(), settings()).profile("default").openFacetRecords("metadata_content"); }

    // ── cases 5, 6: the binding contract ───────────────────────────

    /// **Case 5** — a bound record is names and typed values, in
    /// declared order. **Case 6** — the layout is obtained once, before
    /// any record.
    @Test void aBoundRecordIsNamedTypedValuesInDeclaredOrder() throws IOException {
        RecordFacet facet = open(dataset("c5", List.of(row(1, "a", 1.5), row(2, "b", 2.5)), null));
        Layout layout = Layout.discover(facet);
        assertEquals(List.of("id", "tag", "score"), layout.names());
        assertEquals(List.of(BindType.INT64, BindType.TEXT, BindType.FLOAT64), layout.types());
        Binder binder = Binder.all(layout);
        assertEquals(List.of("id", "tag", "score"), binder.parameters());
        List<Field> out = binder.bind(facet.recordBytes(1));
        assertEquals(3, out.size());
        assertEquals(2L, out.get(0).longValue());
        assertEquals("b", out.get(1).stringValue());
        assertEquals(2.5, out.get(2).doubleValue());
    }

    /// A template may name fields in any order, and bind order follows
    /// the template rather than the record.
    @Test void bindOrderFollowsTheTemplateNotTheRecord() throws IOException {
        RecordFacet facet = open(dataset("order", List.of(row(7, "z", 9.0)), null));
        Binder binder = Binder.select(Layout.discover(facet), "score", "id");
        assertEquals(List.of("score", "id"), binder.parameters());
        List<Field> out = binder.bind(facet.recordBytes(0));
        assertEquals(9.0, out.get(0).doubleValue());
        assertEquals(7L, out.get(1).longValue());
        List<String> seen = new ArrayList<>();
        binder.bindEach(facet.recordBytes(0), (slot, f) -> seen.add(slot + ":" + f.name()));
        assertEquals(List.of("1:id", "0:score"), seen, "the loop form meets fields in wire order, tagged with their slot");
    }

    /// A field the facet does not have is refused when the binder is
    /// built, naming what the facet does have — not on the first cycle.
    @Test void anUnknownFieldIsRefusedAtCompileTime() throws IOException {
        Layout layout = Layout.discover(open(dataset("unknown", List.of(row(1, "a", 1.0)), null)));
        BindException e = assertThrows(BindException.class, () -> Binder.select(layout, "id", "nope"));
        assertEquals(BindException.Kind.NO_SUCH_FIELD, e.kind());
        assertEquals("nope", e.name());
        assertEquals(List.of("id", "tag", "score"), e.available());
        assertTrue(e.getMessage().contains("nope") && e.getMessage().contains("id, tag, score"), e.getMessage());
    }

    /// Names are the metadata names, and a runtime may override one for
    /// substitution — applied at compile time, leaving the facet's own
    /// name untouched.
    @Test void aParameterMayBeRenamedWithoutTouchingTheField() throws IOException {
        RecordFacet facet = open(dataset("rename", List.of(row(3, "c", 3.5)), null));
        Layout layout = Layout.discover(facet);
        Binder binder = Binder.all(layout).withOverrides(Map.of("id", "pk"));
        assertEquals(List.of("pk", "tag", "score"), binder.parameters());
        assertEquals("id", layout.names().get(0), "the facet still calls it id");
        assertEquals(3L, binder.bind(facet.recordBytes(0)).get(0).longValue(), "the rename moved no data");
    }

    /// A record whose layout differs from the one the binder was
    /// compiled against is refused, not bound short.
    @Test void aRecordOfAnotherLayoutIsRefused() throws IOException {
        Layout layout = Layout.discover(open(dataset("layout", List.of(row(1, "a", 1.0)), null)));
        Binder binder = Binder.all(layout);
        byte[] narrower = new MNode().insert("id", new MValue.Int(1)).toBytes();
        BindException e = assertThrows(BindException.class, () -> binder.bindEach(narrower, (s, f) -> { }));
        assertEquals(BindException.Kind.RECORD, e.kind());
        assertTrue(e.getMessage().contains("layout differs"), e.getMessage());
        assertEquals(BindException.Kind.NO_SUCH_FIELD, assertThrows(BindException.class, () -> binder.bind(narrower)).kind());
    }

    // ── cases 1–4: forms ───────────────────────────────────────────

    /// **Case 1 / case 10 — the gate.** A facet with no `forms`
    /// namespace offers exactly one form and binds unchanged. That is
    /// every dataset in existence; absence is not an empty set.
    @Test void aFacetWithoutFormsOffersOneImplicitForm() throws IOException {
        RecordFacet facet = open(dataset("implicit", List.of(row(1, "a", 1.0)), null));
        List<Form> forms = Forms.of(facet);
        assertEquals(1, forms.size(), "one implicit form, not none");
        assertEquals(Form.IMPLICIT, forms.get(0).name());
        assertEquals(List.of("id", "tag", "score"), forms.get(0).binder(Layout.discover(facet)).parameters());
    }

    /// **Case 2** — declared forms are enumerable by name, and each
    /// compiles to its own binder.
    @Test void declaredFormsAreEnumerableAndCompileIndependently() throws IOException {
        RecordFacet facet = open(dataset("forms", List.of(row(1, "a", 1.0)), utf8(
            "{\"name\":\"row\",\"operation\":\"insert\",\"fields\":[\"id\",\"tag\",\"score\"]}",
            "{\"name\":\"key\",\"operation\":\"get\",\"fields\":[\"id\"],\"parameters\":{\"id\":\"pk\"}}")));
        Layout layout = Layout.discover(facet);
        assertEquals(List.of("row", "key"), Forms.of(facet).stream().map(Form::name).toList());
        Binder rowBinder = Forms.byName(facet, "row").binder(layout);
        assertEquals(List.of("id", "tag", "score"), rowBinder.parameters());
        assertEquals("insert", Forms.byName(facet, "row").operation());
        Binder keyBinder = Forms.byName(facet, "key").binder(layout);
        assertEquals(List.of("pk"), keyBinder.parameters());
        assertEquals(List.of(BindType.INT64), keyBinder.types());
        List<Field> out = keyBinder.bind(facet.recordBytes(0));
        assertEquals(1, out.size());
        assertEquals(1L, out.get(0).longValue());
    }

    /// **Case 3** — a form the facet does not offer is refused, naming
    /// the ones it does.
    @Test void anUnknownFormIsRefusedNamingWhatIsOffered() throws IOException {
        RecordFacet facet = open(dataset("noform", List.of(row(1, "a", 1.0)), utf8("{\"name\":\"row\"}")));
        BindException e = assertThrows(BindException.class, () -> Forms.byName(facet, "document"));
        assertEquals(BindException.Kind.NO_SUCH_FORM, e.kind());
        assertEquals("document", e.name());
        assertEquals(List.of("row"), e.available());
        assertTrue(e.getMessage().contains("document") && e.getMessage().contains("row"), e.getMessage());
    }

    /// **Case 4** — a form carrying keys this build does not know is
    /// preserved, not rejected; and a form record it cannot read at all
    /// is skipped, leaving the ones it can.
    @Test void anUnrecognisedFormKeyIsPreserved() throws IOException {
        RecordFacet facet = open(dataset("extra", List.of(row(1, "a", 1.0)), utf8(
            "{\"name\":\"row\",\"consistency\":\"quorum\",\"ttl_seconds\":600}", "not a form at all", "{\"kind\":\"metadata\"}")));
        Form form = Forms.byName(facet, "row");
        assertEquals(new MValue.Text("quorum"), form.extra().get("consistency"));
        assertEquals(new MValue.Int(600), form.extra().get("ttl_seconds"));
        assertEquals(List.of("id", "tag", "score"), form.binder(Layout.discover(facet)).parameters(), "an unknown key is not a broken form");
        assertEquals(1, Forms.of(facet).size(), "the two unreadable records were skipped, not fatal");
        assertThrows(ANodeException.class, () -> Form.fromJson("{\"kind\":\"metadata\"}"), "a form without a name is not a form");
    }

    // ── cases 13, 14, 16: the type asymmetry ───────────────────────

    /// **Case 13** — a `Half` binds as a float, not as a smallint. The
    /// rendering mapping still says smallint, and is still right about
    /// DDL — the two answers coexist rather than one being wrong.
    @Test void aHalfBindsAsAFloatNotAnInteger() {
        assertEquals(BindType.FLOAT16, BindType.ofTag(TypeTag.HALF));
        assertNotEquals(BindType.INT16, BindType.ofTag(TypeTag.HALF));
        assertTrue(MNodeVernacular.toCqlSchema(new MNode().insert("h", new MValue.Half(0x3C00))).contains("smallint"));
    }

    /// **Case 14** — a container's element type is not collapsed to
    /// text; the tag alone does not determine it, and the honest answer
    /// is to say so.
    @Test void containerElementTypesAreUndeterminedRatherThanGuessed() {
        for (TypeTag tag : List.of(TypeTag.LIST, TypeTag.ARRAY)) {
            assertEquals(BindType.list(null), BindType.ofTag(tag));
            assertTrue(BindType.ofTag(tag).isUnderdetermined());
        }
        assertEquals(BindType.set(null), BindType.ofTag(TypeTag.SET));
        assertEquals(BindType.map(null, null), BindType.ofTag(TypeTag.MAP));
        assertTrue(BindType.ofTag(TypeTag.TYPED_MAP).isUnderdetermined());
        assertFalse(BindType.ofTag(TypeTag.INT).isUnderdetermined());
        assertFalse(BindType.ofTag(TypeTag.UUID_V7).isUnderdetermined());
        assertFalse(BindType.list(BindType.TEXT).isUnderdetermined());
        assertEquals("list<text>", BindType.list(BindType.TEXT).toString());
        assertEquals("map<?, ?>", BindType.ofTag(TypeTag.MAP).toString());
    }

    /// **Case 16** — every tag has a bind type; the exhaustive switch is
    /// the real guarantee, and this pins what it currently answers.
    @Test void everyTagHasABindType() {
        assertEquals(29, TypeTag.values().length, "the tag set is a cross-language contract");
        for (TypeTag tag : TypeTag.values()) assertNotNull(BindType.ofTag(tag));
        assertEquals(BindType.NULL, BindType.ofTag(TypeTag.NULL), "null carries no type of its own");
        assertNotEquals(BindType.ofTag(TypeTag.UUID_V1), BindType.ofTag(TypeTag.UUID_V7));
        assertNotEquals(BindType.ofTag(TypeTag.MILLIS), BindType.ofTag(TypeTag.NANOS));
        assertEquals(BindType.TIMESTAMP_TEXT, BindType.ofTag(TypeTag.DATETIME), "text on the wire");
        assertNotEquals(BindType.ofTag(TypeTag.DATETIME), BindType.ofTag(TypeTag.NANOS));
    }

    // ── cases 11, 12: series and scattered access ──────────────────

    /// **Case 11** — forms and layout are read from the series, not
    /// from shard 0. A sharded facet's layout is the facet's.
    @Test void aShardedFacetBindsAcrossItsShards() throws IOException {
        Path dir = Files.createDirectories(temporary.resolve("sharded"));
        for (int shard = 0; shard < 2; shard++) {
            List<byte[]> rows = new ArrayList<>();
            for (int i = 0; i < 5; i++) rows.add(row(shard * 5L + i, "x", shard * 5 + i));
            FixtureSupport.slabOf(dir, String.format("meta__%04d.slab", shard), rows, 4);
        }
        FixtureSupport.fvec(dir, "base.fvec", new float[][] {{0f, 1f, 2f, 3f}});
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: sh
            profiles:
              default:
                base_vectors: base.fvec
                metadata_content:
                  source: meta__NNNN.slab
                  shard_stride: 5
                  shard_count: 2
                  record_count: 10
            """);
        RecordFacet facet = open(dir);
        assertEquals(10, facet.count());
        assertEquals(1, Forms.of(facet).size(), "one implicit form for the whole series, not one per shard");
        Binder binder = Binder.all(Layout.discover(facet));
        for (long o : new long[] {0, 4, 5, 9}) {
            long[] id = {-1};
            binder.bindEach(facet.recordBytes(o), (slot, f) -> { if (slot == 0) id[0] = f.longValue(); });
            assertEquals(o, id[0], "facet ordinal " + o);
        }
    }

    /// **Case 12** — binding stays incremental, addressing scattered
    /// records by ordinal; the remote half is pinned against a served
    /// facet in the HTTP integration suite.
    @Test void bindingAddressesScatteredRecords() throws IOException {
        List<byte[]> rows = new ArrayList<>();
        for (int i = 0; i < 2000; i++) rows.add(row(i, "x", i));
        RecordFacet facet = open(dataset("scattered", rows, null));
        Binder binder = Binder.select(Layout.discover(facet), "id");
        for (long o : new long[] {0, 900, 1999}) {
            long[] id = {-1};
            binder.bindEach(facet.recordBytes(o), (slot, f) -> id[0] = f.longValue());
            assertEquals(o, id[0]);
        }
    }

    // ── cases 7, 8, 15: predicate binding ──────────────────────────

    static PNode pred(String field, OpType op, Comparand c) { return new PNode.Predicate(new FieldRef.Named(field), op, List.of(c)); }
    static PNode and(PNode... children) { return new PNode.Conjugate(ConjugateType.AND, List.of(children)); }

    /// A metadata layout with a temporal and a uuid field, so the typing
    /// question in case 15 is answerable.
    private Layout typedLayout(String name) throws IOException {
        byte[] record = new MNode().insert("id", new MValue.Int(1)).insert("created", new MValue.Millis(1_700_000_000_000L))
            .insert("owner", new MValue.UuidV7(new byte[16])).insert("tag", new MValue.Text("a")).toBytes();
        return Layout.discover(open(dataset(name, List.of(record), null)));
    }

    /// **Case 7** — a predicate binds parameters, not an inlined
    /// fragment: the statement is fixed and only the values move.
    @Test void aPredicateBindsParametersRatherThanInliningThem() throws IOException {
        Layout layout = typedLayout("c7");
        PNode template = and(pred("created", OpType.GE, new Comparand.Int(0)), pred("tag", OpType.EQ, new Comparand.Text("")));
        PredicateBinder binder = PredicateBinder.compile(template, layout);
        List<Condition> c = binder.conditions();
        assertEquals(2, c.size());
        assertEquals("created", c.get(0).field()); assertEquals(OpType.GE, c.get(0).op());
        assertEquals("tag", c.get(1).field()); assertEquals(OpType.EQ, c.get(1).op());
        assertEquals(1, c.get(0).arity());
        PNode actual = and(pred("created", OpType.GE, new Comparand.Int(1_700_000_000_000L)), pred("tag", OpType.EQ, new Comparand.Text("blue")));
        List<String> params = new ArrayList<>();
        List<Comparand> values = new ArrayList<>();
        binder.bindEach(actual.toBytesNamed(), (cond, cs) -> { params.add(cond.parameter()); values.add(cs.get(0)); });
        assertEquals(List.of("created", "tag"), params);
        assertEquals(new Comparand.Int(1_700_000_000_000L), values.get(0));
        assertEquals(new Comparand.Text("blue"), values.get(1));
        assertTrue(Vernaculars.render(ANode.of(actual), Vernacular.CQL).contains("blue"), "rendering inlines");
        assertEquals(List.of("since", "tag"), binder.withOverrides(Map.of("created", "since")).conditions().stream().map(Condition::parameter).toList());
    }

    /// **Case 15** — a parameter's type comes from the **field's** tag,
    /// not the comparand's variant.
    @Test void aParameterIsTypedFromTheFieldNotTheComparand() throws IOException {
        Layout layout = typedLayout("c15");
        PNode template = and(pred("created", OpType.GE, new Comparand.Int(0)), pred("owner", OpType.EQ, new Comparand.Bytes(new byte[16])), pred("id", OpType.EQ, new Comparand.Int(0)));
        List<Condition> c = PredicateBinder.compile(template, layout).conditions();
        assertEquals(BindType.TIMESTAMP_MILLIS, c.get(0).bindType(), "not INT64");
        assertEquals(BindType.UUID, c.get(1).bindType(), "not BLOB");
        assertEquals(BindType.INT64, c.get(2).bindType());
        assertNotEquals(BindType.ofTag(TypeTag.INT), c.get(0).bindType());
        BindException e = assertThrows(BindException.class, () -> PredicateBinder.compile(pred("absent", OpType.EQ, new Comparand.Int(0)), layout));
        assertEquals(BindException.Kind.NO_SUCH_FIELD, e.kind());
    }

    /// **Case 8** — the dialect leader byte is the authority. A metadata
    /// record handed to a predicate binder is refused by what it says
    /// it is, not accepted because a template expected otherwise.
    @Test void aFormCannotOverrideTheDialectLeaderByte() throws IOException {
        PredicateBinder binder = PredicateBinder.compile(and(pred("id", OpType.EQ, new Comparand.Int(0))), typedLayout("c8"));
        BindException e = assertThrows(BindException.class, () -> binder.bindEach(new MNode().insert("id", new MValue.Int(5)).toBytes(), (c, cs) -> { }));
        assertTrue(e.getMessage().contains("dialect") && e.getMessage().contains("not a predicate"), e.getMessage());
    }

    /// A predicate of a different shape is refused rather than bound
    /// into the wrong conditions — what the congruence check is for.
    @Test void aPredicateOfAnotherShapeIsRefused() throws IOException {
        Layout layout = typedLayout("shape");
        PredicateBinder binder = PredicateBinder.compile(and(pred("created", OpType.GE, new Comparand.Int(0)), pred("tag", OpType.EQ, new Comparand.Text(""))), layout);
        PNode other = and(pred("created", OpType.LT, new Comparand.Int(1)), pred("tag", OpType.EQ, new Comparand.Text("x")));
        BindException e = assertThrows(BindException.class, () -> binder.bindEach(other.toBytesNamed(), (c, cs) -> { }));
        assertTrue(e.getMessage().contains("shape differs"), e.getMessage());
    }

    /// A predicate that is not a flat conjunction has no parameter
    /// list, and is refused saying so rather than partially flattened.
    @Test void aDisjunctivePredicateIsRefusedAtCompileTime() throws IOException {
        Layout layout = typedLayout("or");
        PNode or = new PNode.Conjugate(ConjugateType.OR, List.of(pred("id", OpType.EQ, new Comparand.Int(1)), pred("id", OpType.EQ, new Comparand.Int(2))));
        BindException e = assertThrows(BindException.class, () -> PredicateBinder.compile(or, layout));
        assertTrue(e.getMessage().contains("flat conjunction"), e.getMessage());
    }

    // ── error paths and the small surface ──────────────────────────

    /// A facet with no records has no layout to learn, and says so
    /// rather than producing an empty one that binds nothing.
    @Test void anEmptyFacetHasNoLayout() throws IOException {
        RecordFacet facet = open(dataset("empty", List.of(), null));
        assertEquals(0, facet.count());
        BindException e = assertThrows(BindException.class, () -> Layout.discover(facet));
        assertEquals(BindException.Kind.NO_LAYOUT, e.kind());
        assertTrue(e.name().contains("metadata_content"), e.name());
    }

    /// A facet with no declared forms reports none *available* while
    /// still offering its implicit one — the distinction a caller needs
    /// to tell "undeclared" from "declared and unmatched".
    @Test void anImplicitFormIsOfferedButNotListedAsAChoice() throws IOException {
        RecordFacet facet = open(dataset("choice", List.of(row(1, "a", 1.0)), null));
        assertEquals(1, Forms.of(facet).size());
        BindException e = assertThrows(BindException.class, () -> Forms.byName(facet, "row"));
        assertTrue(e.available().isEmpty(), "the implicit form is not a named choice");
        assertTrue(e.getMessage().contains("only its implicit form"), e.getMessage());
        assertEquals(Form.IMPLICIT, Forms.byName(facet, Form.IMPLICIT).name(), "but reachable by its own name");
    }

    /// The layout answers where a field sits, which is the identity
    /// every compiled binder is built on; and the same walk serves a
    /// caller holding one record rather than a facet.
    @Test void aLayoutReportsFieldPositions() throws IOException {
        Layout layout = Layout.discover(open(dataset("positions", List.of(row(1, "a", 1.0)), null)));
        assertEquals(0, layout.positionOf("id"));
        assertEquals(1, layout.positionOf("tag"));
        assertEquals(2, layout.positionOf("score"));
        assertEquals(-1, layout.positionOf("absent"));
        assertEquals(BindType.FLOAT64, layout.types().get(layout.positionOf("score")));
        assertEquals(layout.names(), Layout.discover(row(9, "q", 0.5)).names());
    }

    /// The namespace is named by a constant, and the fixtures write the
    /// name that constant holds.
    @Test void theFormsNamespaceConstantIsTheNameWritten() throws IOException {
        assertEquals("forms", Forms.NAMESPACE);
        RecordFacet facet = open(dataset("constant", List.of(row(1, "a", 1.0)), utf8("{\"name\":\"row\"}")));
        assertEquals(1, facet.namespace(Forms.NAMESPACE).count());
        assertEquals("row", Forms.of(facet).get(0).name());
    }
}
