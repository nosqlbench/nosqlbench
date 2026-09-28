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
import io.nosqlbench.vectordata.TestDataView;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.binding.Layout;
import io.nosqlbench.vectordata.records.RecordFacet;

import java.util.function.LongFunction;

/// Base plumbing for record-binding functions: opens the named dataset
/// profile from the default catalogs, opens a facet of opaque records
/// by ordinal, and learns its field layout **once**, before the first
/// cycle. Everything a subclass compiles — a binder, a form, a
/// predicate template — is compiled against that layout, so the
/// per-cycle path resolves no field names.
///
/// A cycle value is the record ordinal, as it is for every other
/// function in this package; wrapping, hashing, or striding cycles
/// onto ordinals is the workload's policy. Reading a record costs the
/// page that holds it, so a workload touching a scattered fraction of
/// a remote facet fetches a fraction of it.
public abstract class CoreRecords<T> implements LongFunction<T> {

    protected final TestDataView tdv;
    protected final String facetName;
    protected final RecordFacet facet;
    protected final Layout layout;

    protected CoreRecords(String datasetAndProfile, String facetName, VectorDataSettings settings) {
        tdv = Catalog.of(CatalogSources.defaults(), settings).openProfile(datasetAndProfile);
        this.facetName = FacetNames.canonical(facetName);
        facet = tdv.openFacetRecords(this.facetName);
        layout = Layout.discover(facet);
    }

    /// The facet's field layout: names in wire order, with what each
    /// binds as.
    public Layout layout() { return layout; }

    /// The number of records the facet holds — exact and cheap, from
    /// the container's index.
    public long count() { return facet.count(); }
}
