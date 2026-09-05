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


import io.nosqlbench.vectordata.Catalog;
import io.nosqlbench.vectordata.CatalogSources;
import io.nosqlbench.vectordata.FacetNames;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.anode.Vernacular;
import io.nosqlbench.vectordata.records.Codecs;
import io.nosqlbench.vectordata.records.RecordCodec;
import io.nosqlbench.vectordata.records.Records;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.Arrays;
import java.util.function.LongFunction;

/*
 * Renders each record of a facet of opaque records as text in a
 * vernacular — 'cql', 'sql', 'json', 'jsonl', 'yaml', 'cddl',
 * 'readout', 'display', and their '-schema' and '-value' forms. A
 * metadata record renders as a values tuple and a predicate as a WHERE
 * expression with its comparands inlined. Rendering is the other
 * contract from binding: it is the right form for a statement whose
 * shape changes per cycle, such as a predicate facet whose filters
 * differ from query to query, and the wrong one for a prepared
 * statement, which binds through RecordFields instead. The cycle value
 * is the record ordinal. */
@ThreadSafeMapper
@Categories(Category.vectors)
public class RecordText implements LongFunction<String> {

    private final Records<String> records;

    @Example({"RecordText('exampledataset:exampleprofile','metadata_predicates','cql')",
        "Render each predicate as a CQL WHERE expression, ready to inline into a statement"})
    @Example({"RecordText('exampledataset:exampleprofile','metadata_content','json')",
        "Render each metadata record as a JSON object"})
    public RecordText(String datasetAndProfile, String facetName, String vernacular) {
        this(datasetAndProfile, facetName, vernacular, VectorDataSettings.defaults());
    }

    /** Construct with explicit vectordata settings, including an isolated cache location. */
    public RecordText(String datasetAndProfile, String facetName, String vernacular, VectorDataSettings settings) {
        RecordCodec<String> codec = Codecs.byName(vernacular);
        if (codec == null)
            throw new RuntimeException("Unknown vernacular '" + vernacular + "': expected one of " + Arrays.toString(Vernacular.values()));
        records = Catalog.of(CatalogSources.defaults(), settings).openProfile(datasetAndProfile)
            .openFacetRecords(FacetNames.canonical(facetName)).decode(codec);
    }

    /// The number of records the facet holds.
    public long count() { return records.count(); }

    @Override
    public String apply(long ordinal) { return records.get(ordinal); }
}
