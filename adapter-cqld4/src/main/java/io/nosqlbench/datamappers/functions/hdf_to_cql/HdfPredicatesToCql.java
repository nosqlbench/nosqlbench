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
import io.nosqlbench.virtdata.predicates.ast.POperator;
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
    private String lastFieldName;

    /**
     * Create a new binding function that accepts a long input value for the cycle and returns a string
     * @param filename The HDF5 file to read the predicate dataset from
     * @param datasetName The name of the dataset internal to the HDF5 file
     * @param serDesType The type of serialization/deserialization to use for the predicate
     */
    public HdfPredicatesToCql(String filename, String datasetName, String serDesType) {
        hdfFile = new HdfFile(NBIO.all().search(filename).one().asPath());
        dataset = hdfFile.getDatasetByPath(datasetName);
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
        StringBuilder sb = new StringBuilder().append("WHERE ");
        sb.append(switch (model.getConjunction()) {
            case PConjunction.none -> renderTerm((PredicateTerm) model);
            case PConjunction.and -> renderTermsAnd(model);
            case PConjunction.or -> renderTermsOr(model);
        });
        return sb.toString();
    }

    private String renderTerm(PredicateTerm pt) {
        return pt.field.name + " " + pt.operator.symbol() + " " + pt.comparator.formattedValue();
    }

    private String renderTermsAnd(PredicateExpr pae) {
        return pae.getTerms().stream().map(this::renderTerm).collect(Collectors.joining(" and "));
    }

    private String renderTermsOr(PredicateExpr poe) {
        /*
         * There is no OR term in CQL, so we need to convert it to IN. For each record we need to check the following:
         *   1. Does the field name match
         *   2. is the operator eq
         * If either of these are not true this is an invalid expression for CQL
         */
        lastFieldName = poe.getTerms().getFirst().field.name;
        return lastFieldName + " " + "IN(" +
            poe.getTerms().stream().map(this::validateOrConditions).collect(Collectors.joining(",")) + ")";
    }

    private String validateOrConditions(PredicateTerm term) {
        if ((term.field.name.equalsIgnoreCase(lastFieldName)) && term.operator.equals(POperator.eq)) {
            return term.comparator.formattedValue();
        } else {
            throw new RuntimeException("OR term invalid for CQL: " + term);
        }
    }
}
