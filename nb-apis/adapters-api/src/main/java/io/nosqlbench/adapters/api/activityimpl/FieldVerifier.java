package io.nosqlbench.adapters.api.activityimpl;

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


import io.nosqlbench.adapters.api.activityimpl.uniform.Validator;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.DiffType;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import io.nosqlbench.virtdata.core.templates.CapturePoints;

import java.util.*;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

// TODO: Make op(verifyref) use tags, and require 1
public class FieldVerifier implements Validator {

    private final LongFunction<Map<String, Object>> expectedValuesF;
    private final DiffType diffType;
    private final NBMetricCounter resultsVerifiedError;
    private final NBMetricCounter resultsOkCounter;
    private final NBMetricCounter verifiedFieldsCounter;
    private final String[] fieldNames;
    private final String[] bindingNames;

    public FieldVerifier(NBComponent parent, ParsedOp pop, OpLookup lookup) {
        this.resultsVerifiedError = parent.create().counter(
            "results_verified_error", MetricCategory.Verification,
            "The number of results which have been verified with no error"
        );
        this.resultsOkCounter = parent.create().counter(
            "results_verified_ok",
            MetricCategory.Verification,
            "The number of results which had " + "a verification error"
        );
        this.verifiedFieldsCounter = parent.create().counter(
            "field_verified_ok", MetricCategory.Verification,
            "the number of fields in results which have been verified with no error"
        );

        this.diffType = pop.takeEnumFromFieldOr(DiffType.class, DiffType.all, "compare");

        List<String> fields = new ArrayList<>();
        List<String> bindings = new ArrayList<>();
        CapturePoints captures = pop.getCaptures();

        ParsedOp config = pop.takeAsSubConfig("verify_fields");

        Optional<Object> vspec = config.takeOptionalStaticValue("verify_fields", Object.class);
        if (vspec.isPresent()) {
            Object vspeco = vspec.get();
            if (vspeco instanceof Map verifyers) {
                verifyers.forEach((k, v) -> {
                    if (k instanceof CharSequence keyName && v instanceof CharSequence keyValue) {
                        fields.add(keyName.toString());
                        bindings.add(keyValue.toString());

                    } else {
                        throw new RuntimeException(
                            "Strings must be used in map form of " + "verify_field");
                    }

                });
            } else if (vspeco instanceof String verifyBindingSpec) {
                parseFieldSpec(verifyBindingSpec, lookup, fields, bindings, captures, pop);
            } else {
                throw new OpConfigError("Unrecognized type for verify_fields value:" + vspeco.getClass().getSimpleName());
            }
        } else {
            config.getDefinedNames().forEach(name -> {
                fields.add(name);
                bindings.add(config.getStaticValue(name));
            });
        }


        List<BindPoint> bindPoints = pop.getBindPoints();

//        Optional<String> vb = config.getOptionalStaticValue("verify_bindings", String.class);
//        if (vb.isPresent()) {
//            String verifyBindingSpec = vb.get();
//            if (verifyBindingSpec.startsWith("op(") && verifyBindingSpec.endsWith(")")) {
//                String toLookup = verifyBindingSpec.substring(2, verifyBindingSpec.lastIndexOf(-1));
//                ParsedOp referenced = lookup.lookup(toLookup).orElseThrow();
//            }
//        }

        this.fieldNames = fields.toArray(new String[fields.size()]);
        this.bindingNames = bindings.toArray(new String[bindings.size()]);
        this.expectedValuesF = pop.newOrderedMapBinder(bindingNames);

    }

    private void parseFieldSpec(
        String fieldSpec, OpLookup lookup, List<String> fields,
        List<String> bindings, CapturePoints captures, ParsedOp pop
    ) {
        if (fieldSpec.startsWith("op(") && fieldSpec.endsWith(")")) {
            String toLookup = fieldSpec.substring("op(".length(), fieldSpec.length() - 1);
            Optional<ParsedOp> referenced = lookup.lookup(toLookup);
            if (referenced.isPresent()) {
                List<String> vars = referenced.get().getBindPoints().stream().map(
                    bp -> bp.getAnchor()).toList();
                fields.addAll(vars);
                bindings.addAll(vars);
            } else {
                throw new OpConfigError(
                    "no op found for verify setting '" + fieldSpec + "' " + "for op " + "template" + " '" + pop.getName() + "'");
            }
        } else {
            String[] vfields = fieldSpec.split("\\s*,\\s*");
            for (String vfield : vfields) {
//                if (vfield.equals("*")) {
//                    fields.addAll(captures.getAsNames());
//                    fields.addAll(bindPoints.stream().map(bp -> bp.getAnchor()).toList());
//                } else
                if (vfield.startsWith("+")) {
                    fields.add(vfield.substring(1));
                } else if (vfield.startsWith("-")) {
                    fields.remove(vfield.substring(1));
                } else if (vfield.matches("\\w+(\\w+->[\\w-]+)?")) {
                    String[] parts = vfield.split("->", 2);
                    fields.add(parts[0]);
                    bindings.add(parts[1]);
                } else {
                    throw new RuntimeException("unknown verify_fields format: '" + vfield + "'");
                }
            }
        }

    }

    /// Compare the values of the row with the values generated.
    ///
    /// Specifically,
    /// - Ensure the same number of fields.
    /// - Ensure the same pair-wise field names.
    /// - Ensure that each pair of same-named fields has the same data type.
    /// - Ensure that the value of each pair of fields is equal according to the equals
    /// operator for the respective type.
    /// @return a count of differences between the row and the reference values
    @Override
    public void validate(long cycle, Object data) {
        if (data instanceof Map<?, ?> r) {
            Map<String, ?> result = (Map<String, ?>) r;
            Map<String, Object> referenceMap = this.expectedValuesF.apply(cycle);

            int diff = 0;
            StringBuilder logbuffer = new StringBuilder(); // make this a TL
            logbuffer.setLength(0);

            if (diffType.is(DiffType.reffields)) {

                List<String> missingRowFields = Arrays.stream(this.fieldNames).filter(
                    gk -> !result.containsKey(gk)).collect(Collectors.toList());
                if (missingRowFields.size() > 0) {
                    diff += missingRowFields.size();

                    logbuffer.append("\nexpected fields '");
                    logbuffer.append(String.join("','", missingRowFields));
                    logbuffer.append("' not in row.");
                }
            }

//            if (diffType.is(DiffType.rowfields)) {
//                List<String> missingRefFields = result.keySet().stream().filter(
//                    k -> !referenceMap.containsKey(k)).collect(Collectors.toList());
//                if (missingRefFields.size() > 0) {
//                    diff += missingRefFields.size();
//
//                    logbuffer.append("\nexpected fields '");
//                    logbuffer.append(String.join("','", missingRefFields));
//                    logbuffer.append("' not in reference data: " + referenceMap);
//                }
//            }

            if (diffType.is(DiffType.values)) {
                for (int fidx = 0; fidx < fieldNames.length; fidx++) {
                    String fname = fieldNames[fidx];
                    ;
                    String rname = bindingNames[fidx];
                    if (referenceMap.containsKey(rname)) {
                        if (referenceMap.get(rname).equals(result.get(fname))) {
                            verifiedFieldsCounter.inc();
                        } else {
                            logbuffer.append("\nvalue differs for '").append(fname).append("' ");
                            logbuffer.append("expected:'").append(
                                referenceMap.get(fname).toString()).append("'");
                            logbuffer.append(" actual:'").append(result.get(rname)).append("'");
                            diff++;
                        }
                    }
                }
            }
            if (diff == 0) {
                resultsOkCounter.inc();
            } else {
                resultsVerifiedError.inc();
                throw new RuntimeException("in cycle " + cycle + ", " + logbuffer.toString());
            }

        } else {
            throw new OpConfigError("Can only validate fields of type Map");
        }


    }

    @Override
    public String getName() {
        return "verify_fields";
    }


}
