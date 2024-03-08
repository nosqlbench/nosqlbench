/*
 * Copyright (c) 2024 nosqlbench
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
 *
 */

package io.nosqlbench.datamappers.functions.hdf_to_cql;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.nosqlbench.nb.annotations.ServiceSelector;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.predicates.ast.PConjunction;
import io.nosqlbench.virtdata.predicates.ast.PredicateExpr;
import io.nosqlbench.virtdata.predicates.ast.PredicateTerm;
import io.nosqlbench.virtdata.predicates.types.PredicateAdapter;
import io.nosqlbench.virtdata.predicates.types.PredicateSerDes;

import java.util.ServiceLoader;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

/**
 * Binding function that accepts a long input value for the cycle and returns a string consisting of the
 * CQL predicate parsed from a single record in an HDF5 dataset
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class HdfPredicatesToCql implements LongFunction<String>, PredicateAdapter {
    private final HdfFile hdfFile;
    private final Dataset dataset;
    private final int recordCount;
    private final PredicateSerDes serDes;

    /**
     * Create a new binding function that accepts a long input value for the cycle and returns a string
     * @param filename The HDF5 file to read the predicate dataset from
     * @param datasetname The name of the dataset internal to the HDF5 file
     */
    public HdfPredicatesToCql(String filename, String datasetname, String serDesType) {
        hdfFile = new HdfFile(NBIO.all().search(filename).one().asPath());
        dataset = hdfFile.getDatasetByPath(datasetname);
        recordCount = dataset.getDimensions()[0];
        serDes = ServiceSelector.of(serDesType, ServiceLoader.load(PredicateSerDes.class)).getOne();
    }

    @Override
    public String apply(long l) {
        long[] sliceOffset = {(l % recordCount)};
        int[] sliceDimensions = {1};
        return getPredicate(serDes.unserialize(((String[])dataset.getData(sliceOffset, sliceDimensions))[0]));
    }

    @Override
    public String getPredicate(PredicateExpr model) {
        return switch (model.getConjunction()) {
            case PConjunction.none -> renderTerm(model);
            case PConjunction.and -> renderTermsAnd(model);
            case PConjunction.or -> renderTermsOr(model);
        };
    }

    private String renderTerm(PredicateExpr pe) {
        PredicateTerm pt = pe.getTerms().getFirst();
        return pt.field.name + " " + pt.operator.name() + " " + pt.comparator.value;
    }

    private String renderTermsAnd(PredicateExpr pae) {
        return pae.getTerms().stream().map(this::renderTerm).collect(Collectors.joining(" and "));
    }

    private String renderTermsOr(PredicateExpr poe) {
        return poe.getTerms().stream().map(this::renderTerm).collect(Collectors.joining(" or "));
    }
}
