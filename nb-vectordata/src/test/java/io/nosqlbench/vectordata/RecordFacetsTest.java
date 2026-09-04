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
import io.nosqlbench.vectordata.anode.MNode;
import io.nosqlbench.vectordata.anode.MValue;
import io.nosqlbench.vectordata.anode.Vernacular;
import io.nosqlbench.vectordata.records.Codecs;
import io.nosqlbench.vectordata.records.RecordException;
import io.nosqlbench.vectordata.records.RecordFacet;
import io.nosqlbench.vectordata.records.Records;
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

/// Reading a slab facet by ordinal and typing it with a codec, mirrored
/// from the reference's `record_facets` suite: the container, the
/// currying that types it, and the property that makes both work for a
/// series — a shard is an ordinary slab based at zero, and the facet
/// ordinal is resolved before the container ever sees it.
@Tag("unit")
class RecordFacetsTest {
    @TempDir Path temporary;

    private VectorDataSettings settings() {
        return VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
    }

    /// `count` MNode records `{id, bucket}` with ids from `first`.
    static List<byte[]> metadataRecords(int first, int count) {
        List<byte[]> records = new ArrayList<>();
        for (int i = first; i < first + count; i++)
            records.add(new MNode().insert("id", new MValue.Int32(i)).insert("bucket", new MValue.Int32(i % 4)).toBytes());
        return records;
    }

    private Path singleFacetDataset(String name, int records) throws IOException {
        Path dir = Files.createDirectories(temporary.resolve(name));
        FixtureSupport.slabOf(dir, "metadata_content.slab", metadataRecords(0, records), 4);
        FixtureSupport.fvec(dir, "base.fvec", new float[][] {{0f, 1f, 2f, 3f}});
        Files.writeString(dir.resolve("dataset.yaml"), "name: meta\nprofiles:\n  default:\n    base_vectors: base.fvec\n    metadata_content: metadata_content.slab\n");
        return dir;
    }

    /// Three shards of 10, 10, and 5 records, each an ordinary slab
    /// whose own ordinals start at zero; the ids distinguish them.
    private Path shardedFacetDataset() throws IOException {
        Path dir = Files.createDirectories(temporary.resolve("sharded"));
        FixtureSupport.slabOf(dir, "meta__0000.slab", metadataRecords(0, 10), 4);
        FixtureSupport.slabOf(dir, "meta__0001.slab", metadataRecords(10, 10), 4);
        FixtureSupport.slabOf(dir, "meta__0002.slab", metadataRecords(20, 5), 4);
        FixtureSupport.fvec(dir, "base.fvec", new float[][] {{0f, 1f, 2f, 3f}});
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: sharded-meta
            profiles:
              default:
                base_vectors: base.fvec
                metadata_content:
                  source: meta__NNNN.slab
                  shard_stride: 10
                  shard_count: 3
                  record_count: 25
            """);
        return dir;
    }

    private TestDataView view(Path dir) { return TestDataGroup.load(dir.toUri(), settings()).profile("default"); }

    @SuppressWarnings("unchecked")
    private static long id(Object tree) { return (Long) ((Map<String, Object>) tree).get("id"); }

    @Test void aSlabFacetReadsRecordsByOrdinal() throws Exception {
        RecordFacet facet = view(singleFacetDataset("by-ordinal", 12)).openFacetRecords("metadata_content");
        assertEquals(12, facet.count());
        // The raw bytes are the escape hatch beneath every codec, and
        // they carry the dialect leader that makes stage 1 self-describing.
        assertEquals(ANode.DIALECT_MNODE, facet.recordBytes(0)[0]);
        RecordException past = assertThrows(RecordException.class, () -> facet.recordBytes(12), "one past the end");
        assertEquals(RecordException.Kind.OUT_OF_BOUNDS, past.kind()); assertEquals(12, past.ordinal());
    }

    /// The dialect comes from the record, not from the facet.
    @Test void theRecordSaysWhichDialectItIs() throws Exception {
        Records<ANode> nodes = view(singleFacetDataset("dialect", 4)).openFacetRecords("metadata_content").decode(Codecs.ANODE);
        for (int o = 0; o < 4; o++) {
            ANode node = nodes.get(o);
            assertInstanceOf(ANode.M.class, node, "ordinal " + o);
            assertEquals(new MValue.Int32(o), ((ANode.M) node).node().get("id"));
        }
    }

    /// A codec applied to a facet is a typed reader, and the untyped
    /// level is not a separate path — just the codec that stops after
    /// stage one.
    @Test void applyingACodecTypesTheSameFacet() throws Exception {
        RecordFacet facet = view(singleFacetDataset("typed", 6)).openFacetRecords("metadata_content");
        Records<ANode> nodes = facet.decode(Codecs.ANODE);
        assertInstanceOf(ANode.M.class, nodes.get(2));
        Records<String> cql = facet.decode(Codecs.text(Vernacular.CQL));
        assertEquals("(2, 2)", cql.get(2), "CQL values for id=2, bucket=2");
        Records<Object> rows = facet.decode(Codecs.TREE);
        assertEquals(Map.of("id", 2L, "bucket", 2L), rows.get(2));
        assertEquals(Map.of("id", 5L, "bucket", 1L), rows.get(5));
        assertEquals(6, nodes.count()); assertEquals(6, cql.count()); assertEquals(6, rows.count());
    }

    @Test void aCodecNamedAtRuntimeDecodesIdentically() throws Exception {
        RecordFacet facet = view(singleFacetDataset("named", 3)).openFacetRecords("metadata_content");
        String byType = facet.decode(Codecs.text(Vernacular.CQL)).get(1);
        assertEquals(byType, Codecs.byName("cql").decode(facet.recordBytes(1)));
        assertNull(Codecs.byName("not-a-codec"));
        assertNull(Codecs.byName("anode"), "anode produces no text, so it names no text codec");
    }

    @Test void recordsIterateInOrdinalOrder() throws Exception {
        Records<Object> rows = view(singleFacetDataset("iter", 5)).openFacetRecords("metadata_content").decode(Codecs.TREE);
        List<Long> ids = new ArrayList<>();
        for (Object row : rows) ids.add(id(row));
        assertEquals(List.of(0L, 1L, 2L, 3L, 4L), ids);
        assertEquals(List.of(0L, 1L, 2L, 3L, 4L), rows.stream().map(RecordFacetsTest::id).toList());
    }

    @Test void aSlabSeriesReadsAsOneFacet() throws Exception {
        RecordFacet facet = view(shardedFacetDataset()).openFacetRecords("metadata_content");
        assertEquals(25, facet.count(), "the series total, not a shard's");
        Records<Object> rows = facet.decode(Codecs.TREE);
        for (long o = 0; o < 25; o++) assertEquals(o, id(rows.get(o)), "facet ordinal " + o + " must reach the record that belongs to it");
        for (long o : new long[] {9, 10, 19, 20, 24, 10, 9, 0}) assertEquals(o, id(rows.get(o)));
        assertThrows(RecordException.class, () -> rows.get(25), "one past the end of the series");
    }

    /// Shards carry relative ordinals: shard 2's own ordinal 0 holds
    /// the record the facet calls ordinal 20.
    @Test void eachShardIsASlabBasedAtZero() throws Exception {
        Path dir = shardedFacetDataset();
        ANode direct = ANode.decode(RecordFacet.over("shard", null, List.of(new io.nosqlbench.vectordata.internal.MappedStorage(dir.resolve("meta__0002.slab")))).recordBytes(0));
        ANode throughSeries = view(dir).openFacetRecords("metadata_content").decode(Codecs.ANODE).get(20);
        assertEquals(direct, throughSeries, "local ordinal 0 is facet ordinal 20");
        assertEquals(new MValue.Int32(20), ((ANode.M) direct).node().get("id"));
    }

    @Test void aSiblingNamespaceReadsThroughTheSameContainers() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("ns"));
        byte[] content = new MNode().insert("id", new MValue.Int32(7)).insert("bucket", new MValue.Int32(3)).toBytes();
        byte[] schema = "{\"kind\":\"metadata\"}".getBytes(StandardCharsets.UTF_8);
        FixtureSupport.slabWithNamespaces(dir, "metadata_content.slab", List.of(content), Map.of("schema", List.of(schema)), 4);
        FixtureSupport.fvec(dir, "base.fvec", new float[][] {{0f, 1f, 2f, 3f}});
        Files.writeString(dir.resolve("dataset.yaml"), "name: ns\nprofiles:\n  default:\n    base_vectors: base.fvec\n    metadata_content: metadata_content.slab\n");
        RecordFacet facet = view(dir).openFacetRecords("metadata_content");
        assertEquals(1, facet.count());
        assertEquals(7L, id(facet.decode(Codecs.TREE).get(0)));
        RecordFacet sibling = facet.namespace("schema");
        assertEquals(1, sibling.count());
        assertArrayEquals(schema, sibling.recordBytes(0));
        assertTrue(sibling.name().endsWith(":schema"));
    }

    @Test void anAbsentNamespaceReportsEmptyRatherThanFailing() throws Exception {
        RecordFacet facet = view(singleFacetDataset("absent", 3)).openFacetRecords("metadata_content");
        RecordFacet missing = facet.namespace("layout");
        assertEquals(0, missing.count(), "no layout namespace was written");
        RecordException e = assertThrows(RecordException.class, () -> missing.recordBytes(0));
        assertEquals(RecordException.Kind.OUT_OF_BOUNDS, e.kind());
        assertEquals(3, facet.count(), "the content namespace is unaffected");
    }

    @Test void aShardedContentFacetCarriesNoEmbeddedLayout() throws Exception {
        RecordFacet facet = view(shardedFacetDataset()).openFacetRecords("metadata_content");
        assertEquals(25, facet.count());
        assertEquals(0, facet.namespace("layout").count(), "no shard carries the embedded copy");
    }

    @Test void aFacetReportsWhichShapeItHolds() throws Exception {
        TestDataView view = view(singleFacetDataset("shape", 4));
        assertEquals(FacetShape.ELEMENTS, view.facetShape("base_vectors"));
        assertEquals(FacetShape.RECORDS, view.facetShape("metadata_content"));
        for (String name : List.of("base_vectors", "metadata_content")) {
            switch (view.facetShape(name)) {
                case ELEMENTS -> assertDoesNotThrow(() -> view.openFacet(name), name + " should open as vectors");
                case RECORDS -> assertDoesNotThrow(() -> view.openFacetRecords(name), name + " should open as records");
            }
        }
        assertEquals(FacetShape.RECORDS, view(shardedFacetDataset()).facetShape("metadata_content"), "a series has one format to report");
    }

    /// Opening a record facet as vectors names the reader that works,
    /// structurally, so a caller can branch rather than parse prose.
    @Test void openingARecordFacetAsVectorsPointsAtTheRightReader() throws Exception {
        TestDataView view = view(singleFacetDataset("wrong-door", 4));
        WrongFacetShapeException e = assertThrows(WrongFacetShapeException.class, () -> view.openFacet("metadata_content"));
        assertEquals("metadata_content", e.facet());
        assertEquals(FacetShape.RECORDS, e.shape());
        assertEquals(FacetShape.ELEMENTS, e.attempted());
        assertTrue(e.reader().contains("openFacetRecords"), e.reader());
        assertFalse(e.getMessage().contains("Unsupported vector extension"), e.getMessage());
        assertThrows(WrongFacetShapeException.class, () -> view.openVariableFacet("metadata_content"));
    }

    @Test void openingAnElementFacetAsRecordsPointsBack() throws Exception {
        RecordException e = assertThrows(RecordException.class, () -> view(singleFacetDataset("mirror", 4)).openFacetRecords("base_vectors"));
        assertEquals(RecordException.Kind.WRONG_SHAPE, e.kind());
        assertEquals("base_vectors", e.facet());
        assertTrue(e.reader().contains("openFacet"), e.reader());
    }

    @Test void aFormatTheSpecDoesNotNameHasNoShape() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("unnamed"));
        Files.write(dir.resolve("m.parquet"), new byte[64]);
        FixtureSupport.fvec(dir, "base.fvec", new float[][] {{0f, 1f, 2f, 3f}});
        Files.writeString(dir.resolve("dataset.yaml"), "name: p\nprofiles:\n  default:\n    base_vectors: base.fvec\n    metadata_predicates: m.parquet\n");
        VectorDataException e = assertThrows(VectorDataException.class, () -> view(dir).facetShape("metadata_predicates"));
        assertTrue(e.getMessage().contains("names no format for 'parquet'"), e.getMessage());
    }
}
