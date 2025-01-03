package io.nosqlbench.engine.api.activityimpl;

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


import io.nosqlbench.adapters.api.activityimpl.OpLookup;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.tagging.TagFilter;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class OpLookupService implements OpLookup {
    private List<ParsedOp> pops;
    private final Supplier<List<ParsedOp>> popsF;

    public OpLookupService(Supplier<List<ParsedOp>> popsF) {
        this.popsF = popsF;
    }

    @Override
    public synchronized Optional<ParsedOp> lookup(String tagSpec) {
        if (pops == null) {
            pops = popsF.get();
        }
        TagFilter filter = new TagFilter(tagSpec);
        List<ParsedOp> list = filter.filter(pops);

        if (list.size() > 1) {
            throw new RuntimeException(
                "Too many (" + list.size() + ") ops were found when looking up '" + tagSpec + "'");
        }
        if (list.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}
