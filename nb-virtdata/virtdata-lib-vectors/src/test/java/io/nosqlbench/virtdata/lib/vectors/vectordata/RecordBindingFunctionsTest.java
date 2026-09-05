package io.nosqlbench.virtdata.lib.vectors.vectordata;

/*
 * Copyright (c) nosqlbench
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


import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.anode.MNode;
import io.nosqlbench.vectordata.anode.MValue;
import io.nosqlbench.vectordata.anode.PNode;
import io.nosqlbench.vectordata.anode.PNode.Comparand;
import io.nosqlbench.vectordata.anode.PNode.ConjugateType;
import io.nosqlbench.vectordata.anode.PNode.FieldRef;
import io.nosqlbench.vectordata.anode.PNode.OpType;
import io.nosqlbench.vectordata.binding.BindException;
import io.nosqlbench.vectordata.binding.BindType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/// The record-binding functions: one field projected by its wire type,
/// a record as named values in bind order, a record under a declared
/// form, and a predicate's comparands as parameters typed from the
/// metadata fields they constrain. The fixture is a local dataset with
/// a metadata slab, its forms namespace, and a predicate slab.
@Tag("unit")
public class RecordBindingFunctionsTest {

    @TempDir static Path temporary;
    static VectorDataSettings settings;
    static String priorCatalog;
    static final byte[] OWNER = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    static final long EPOCH_MILLIS = 1_700_000_000_000L;

    static byte[] row(int i) {
        return new MNode()
            .insert("id", new MValue.Int(i))
            .insert("tag", new MValue.Text("t" + i % 3))
            .insert("score", new MValue.Float(i * 0.5))
            .insert("created", new MValue.Millis(EPOCH_MILLIS + i))
            .insert("owner", new MValue.UuidV7(OWNER))
            .insert("flags", new MValue.ListValue(List.of(new MValue.Int(i), new MValue.Text("x"))))
            .insert("h", new MValue.Half(0x3C00))
            .insert("n", new MValue.Nanos(1_700_000_000L + i, 500))
            .insert("count", new MValue.Int32(i * 2))
            .toBytes();
    }

    /// Two shapes: a conjunction for most queries, and every seventh
    /// query a lone equality on the id — so a facet's predicates differ
    /// in shape the way a published one's do.
    static byte[] predicate(int i) {
        if (i % 7 == 6) return new PNode.Predicate(new FieldRef.Named("id"), OpType.IN, List.of(new Comparand.Int(i), new Comparand.Int(i + 1))).toBytesNamed();
        return new PNode.Conjugate(ConjugateType.AND, List.of(
            new PNode.Predicate(new FieldRef.Named("created"), OpType.GE, List.of(new Comparand.Int(EPOCH_MILLIS + i))),
            new PNode.Predicate(new FieldRef.Named("tag"), OpType.EQ, List.of(new Comparand.Text("t" + i % 3))))).toBytesNamed();
    }

    @BeforeAll
    static void publishFixture() throws IOException {
        Path dataset = Files.createDirectories(temporary.resolve("records"));
        fvec(dataset.resolve("base.fvec"), new float[][] {{0f, 1f}});
        List<byte[]> rows = new ArrayList<>(), predicates = new ArrayList<>();
        for (int i = 0; i < 20; i++) { rows.add(row(i)); predicates.add(predicate(i)); }
        List<byte[]> forms = List.of(
            "{\"name\":\"row\",\"operation\":\"insert\",\"fields\":[\"id\",\"tag\"]}".getBytes(StandardCharsets.UTF_8),
            "{\"name\":\"key\",\"operation\":\"get\",\"fields\":[\"id\"],\"parameters\":{\"id\":\"pk\"}}".getBytes(StandardCharsets.UTF_8));
        slab(dataset.resolve("metadata_content.slab"), rows, Map.of("forms", forms), 4);
        slab(dataset.resolve("metadata_predicates.slab"), predicates, Map.of(), 4);
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: records
            profiles:
              default:
                base_vectors: base.fvec
                metadata_content: metadata_content.slab
                metadata_predicates: metadata_predicates.slab
            """);
        Files.writeString(temporary.resolve("catalog.yaml"), """
            datasets:
              - name: records
                path: records/dataset.yaml
            """);
        settings = VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
        priorCatalog = System.getProperty("vectordata.catalog");
        System.setProperty("vectordata.catalog", temporary.resolve("catalog.yaml").toString());
    }

    @AfterAll
    static void restoreCatalogProperty() {
        if (priorCatalog == null) System.clearProperty("vectordata.catalog");
        else System.setProperty("vectordata.catalog", priorCatalog);
    }

    @Test
    void recordFieldProjectsByWireType() {
        assertEquals(5L, new RecordField("records:default", "metadata_content", "id", settings).apply(5));
        assertEquals("t2", new RecordField("records:default", "metadata_content", "tag", settings).apply(5));
        assertEquals(2.5, new RecordField("records:default", "metadata_content", "score", settings).apply(5));
        RecordField created = new RecordField("records:default", "metadata_content", "created", settings);
        assertEquals(Instant.ofEpochMilli(EPOCH_MILLIS + 5), created.apply(5), "a Millis is an instant, not a long");
        assertEquals(BindType.TIMESTAMP_MILLIS, created.bindType());
        assertEquals(new UUID(0x0001020304050607L, 0x08090a0b0c0d0e0fL), new RecordField("records:default", "metadata_content", "owner", settings).apply(0));
        assertEquals(List.of(5L, "x"), new RecordField("records:default", "metadata_content", "flags", settings).apply(5), "a container projects its elements");
        assertEquals(1.0f, new RecordField("records:default", "metadata_content", "h", settings).apply(0), "a Half is a float, not its bit pattern");
        assertEquals(Instant.ofEpochSecond(1_700_000_000L + 5, 500), new RecordField("records:default", "metadata_content", "n", settings).apply(5));
        assertEquals(10, new RecordField("records:default", "metadata_content", "count", settings).apply(5), "an Int32 is an Integer");
    }

    @Test
    void recordFieldsBindsEveryFieldOrASelection() {
        RecordFields all = new RecordFields("records:default", "metadata_content", "", settings);
        assertEquals(List.of("id", "tag", "score", "created", "owner", "flags", "h", "n", "count"), all.parameters());
        Map<String, Object> bound = all.apply(3);
        assertEquals(all.parameters(), new ArrayList<>(bound.keySet()), "keys in bind order");
        assertEquals(3L, bound.get("id"));
        assertEquals(20, all.count());
        // A selection, in template order, through a facet alias and with
        // whitespace around the names.
        RecordFields some = new RecordFields("records:default", "content", "tag, id", settings);
        assertEquals(List.of("tag", "id"), some.parameters());
        assertEquals(List.of(BindType.TEXT, BindType.INT64), some.types());
        assertEquals(Map.of("tag", "t1", "id", 7L), some.apply(7));
    }

    @Test
    void recordFormBindsAsTheFacetDeclares() {
        RecordForm key = new RecordForm("records:default", "metadata_content", "key", settings);
        assertEquals(List.of("pk"), key.parameters(), "the form's parameter name, not the field's");
        assertEquals(Map.of("pk", 7L), key.apply(7));
        assertEquals("get", key.form().operation());
        RecordForm row = new RecordForm("records:default", "metadata_content", "row", settings);
        assertEquals(List.of("id", "tag"), row.parameters());
        assertEquals(Map.of("id", 2L, "tag", "t2"), row.apply(2));
        BindException e = assertThrows(BindException.class, () -> new RecordForm("records:default", "metadata_content", "document", settings));
        assertEquals(BindException.Kind.NO_SUCH_FORM, e.kind());
        assertEquals(List.of("row", "key"), e.available(), "refused naming what is offered");
    }

    @Test
    void predicateParamsBindInTheShapeOfTheFirstPredicate() {
        PredicateParams params = new PredicateParams("records:default", "metadata_predicates", "metadata_content", "", settings);
        assertEquals(List.of("created", "tag"), params.parameters());
        assertEquals(OpType.GE, params.conditions().get(0).op());
        assertEquals(BindType.TIMESTAMP_MILLIS, params.conditions().get(0).bindType(), "typed from the field, not the Int comparand");
        Map<String, Object> bound = params.apply(4);
        assertEquals(List.of("created", "tag"), new ArrayList<>(bound.keySet()));
        assertEquals(Instant.ofEpochMilli(EPOCH_MILLIS + 4), bound.get("created"), "the comparand is coerced to the field's type");
        assertEquals("t1", bound.get("tag"));
    }

    @Test
    void predicateParamsAcceptATemplateAndRefuseAnotherShape() {
        PredicateParams params = new PredicateParams("records:default", "metadata_predicates", "metadata_content", "(created >= 0 AND tag = '')", settings);
        assertEquals(List.of("created", "tag"), params.parameters());
        assertEquals("t2", params.apply(2).get("tag"));
        PredicateParams other = new PredicateParams("records:default", "metadata_predicates", "metadata_content", "id = 0", settings);
        BindException e = assertThrows(BindException.class, () -> other.apply(0));
        assertTrue(e.getMessage().contains("shape differs"), e.getMessage());
    }

    @Test
    void anUnknownFieldIsRefusedWhenTheFunctionIsBuilt() {
        BindException e = assertThrows(BindException.class, () -> new RecordField("records:default", "metadata_content", "nope", settings));
        assertEquals(BindException.Kind.NO_SUCH_FIELD, e.kind());
        assertEquals("nope", e.name());
        assertTrue(e.available().contains("id"), "naming what the facet has");
    }

    @Test
    void recordTextRendersAVernacularPerRecord() {
        RecordText cql = new RecordText("records:default", "metadata_predicates", "cql", settings);
        assertEquals("created >= " + (EPOCH_MILLIS + 3) + " AND tag = 't0'", cql.apply(3), "a predicate as a bare WHERE clause, comparands inlined");
        assertEquals(20, cql.count());
        RecordText json = new RecordText("records:default", "metadata_content", "jsonl", settings);
        assertTrue(json.apply(1).startsWith("{\"id\":1,\"tag\":\"t1\""), json.apply(1));
        RuntimeException unknown = assertThrows(RuntimeException.class, () -> new RecordText("records:default", "metadata_content", "klingon", settings));
        assertTrue(unknown.getMessage().contains("cql") && unknown.getMessage().contains("json"), unknown.getMessage());
    }

    @Test
    void cqlColumnsFollowTheLayoutAndTheSample() {
        byte[] record = row(1);
        String columns = CqlColumns.columns(io.nosqlbench.vectordata.binding.Layout.discover(record), MNode.fromBytes(record));
        assertEquals("id bigint, tag text, score double, created timestamp, owner uuid, flags list<bigint>, h float, n timestamp, count int", columns);
        assertEquals("list<text>", CqlColumns.cqlType(BindType.list(null), null), "an element type nothing determines is text");
        assertEquals("map<text, text>", CqlColumns.cqlType(BindType.map(null, null), null));
        assertEquals("smallint", CqlColumns.cqlType(BindType.INT16, null));
        assertEquals("text", CqlColumns.cqlType(BindType.TIMESTAMP_TEXT, null), "text on the wire, text in the column");
    }

    @Test
    void predicateClauseInitializesOneFormPerShapeAndBindsThroughIt() {
        PredicateClause clause = new PredicateClause("records:default", "metadata_predicates", "metadata_content", settings);
        // Surveyed when built: every shape known and initialized before
        // the first cycle, most common first.
        PredicateClause.Survey survey = clause.survey();
        assertEquals(2, survey.forms());
        assertEquals(20, survey.predicates());
        assertEquals(List.of("(created >= 0 AND tag = '')", "id IN (0, 0)"), new ArrayList<>(survey.countsByForm().keySet()));
        assertEquals(List.of(18L, 2L), new ArrayList<>(survey.countsByForm().values()));
        assertTrue(survey.toString().startsWith("2 predicate forms across 20 predicates"), survey.toString());
        assertTrue(survey.report().contains("created >= ? AND tag = ?"), survey.report());
        io.nosqlbench.virtdata.core.templates.PreparedFragment third = clause.apply(3);
        assertEquals("created >= ? AND tag = ?", third.text());
        assertEquals("(created >= 0 AND tag = '')", third.formKey(), "keyed by the predicate's fingerprint");
        assertArrayEquals(new Object[] {Instant.ofEpochMilli(EPOCH_MILLIS + 3), "t0"}, third.values(), "values typed from the metadata fields");
        io.nosqlbench.virtdata.core.templates.PreparedFragment sixth = clause.apply(6);
        assertEquals("id IN ?", sixth.text());
        assertEquals(List.of(List.of(6L, 7L)), List.of(sixth.values()), "a membership condition binds a list");
        for (long o = 0; o < 20; o++) clause.apply(o);
        assertEquals(Map.of("(created >= 0 AND tag = '')", "created >= ? AND tag = ?", "id IN (0, 0)", "id IN ?"), clause.forms(), "two shapes, two forms, initialized once each");
        assertEquals(2, clause.survey().forms(), "applying cycles adds no forms the survey did not see");
        assertSame(third.formKey(), clause.apply(10).formKey(), "the form key is the same instance every cycle, so an adapter's lookup allocates nothing");
    }

    // ── fixture writers ────────────────────────────────────────────

    static void fvec(Path path, float[][] vectors) throws IOException {
        ByteBuffer out = ByteBuffer.allocate(vectors.length * (4 + 4 * vectors[0].length)).order(ByteOrder.LITTLE_ENDIAN);
        for (float[] v : vectors) { out.putInt(v.length); for (float f : v) out.putFloat(f); }
        Files.write(path, out.array());
    }

    /// A slab in the reference layout: data pages of `perPage` records,
    /// a pages page per namespace, and — when named namespaces exist —
    /// a namespaces page locating each pages page.
    static void slab(Path path, List<byte[]> records, Map<String, List<byte[]>> namespaces, int perPage) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<byte[]> index = new ArrayList<>();
        dataPages(out, index, records, perPage, 1);
        if (namespaces.isEmpty()) out.write(page(index, 0, 1, 1));
        else {
            List<byte[]> entries = new ArrayList<>();
            long at = out.size(); out.write(page(index, 0, 1, 1)); entries.add(namespaceEntry(1, "", at));
            int namespaceIndex = 2;
            for (Map.Entry<String, List<byte[]>> namespace : namespaces.entrySet()) {
                List<byte[]> pages = new ArrayList<>();
                dataPages(out, pages, namespace.getValue(), perPage, namespaceIndex);
                long pagesAt = out.size(); out.write(page(pages, 0, 1, namespaceIndex));
                entries.add(namespaceEntry(namespaceIndex, namespace.getKey(), pagesAt));
                namespaceIndex++;
            }
            out.write(page(entries, 0, 3, 1));
        }
        Files.write(path, out.toByteArray());
    }

    private static void dataPages(ByteArrayOutputStream out, List<byte[]> index, List<byte[]> records, int perPage, int namespaceIndex) throws IOException {
        for (int start = 0; start < records.size(); start += perPage) {
            ByteBuffer entry = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN); entry.putLong(start).putLong(out.size()); index.add(entry.array());
            out.write(page(records.subList(start, Math.min(records.size(), start + perPage)), start, 2, namespaceIndex));
        }
    }

    private static byte[] namespaceEntry(int index, String name, long pagesPageOffset) {
        byte[] utf8 = name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer entry = ByteBuffer.allocate(2 + utf8.length + 8).order(ByteOrder.LITTLE_ENDIAN);
        entry.put((byte) index).put((byte) utf8.length).put(utf8).putLong(pagesPageOffset);
        return entry.array();
    }

    private static byte[] page(List<byte[]> records, long startOrdinal, int pageType, int namespaceIndex) {
        int data = 0; for (byte[] record : records) data += record.length;
        int size = 8 + data + 4 * (records.size() + 1) + 16;
        ByteBuffer page = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        page.put(new byte[] {'S', 'L', 'A', 'B'}).putInt(size);
        for (byte[] record : records) page.put(record);
        int offset = 8; for (byte[] record : records) { page.putInt(offset); offset += record.length; } page.putInt(offset);
        for (int i = 0; i < 5; i++) page.put((byte) (startOrdinal >>> (8 * i)));
        int count = records.size(); page.put((byte) count).put((byte) (count >>> 8)).put((byte) (count >>> 16));
        page.putInt(size).put((byte) pageType).put((byte) namespaceIndex).putShort((short) 16);
        return page.array();
    }
}
