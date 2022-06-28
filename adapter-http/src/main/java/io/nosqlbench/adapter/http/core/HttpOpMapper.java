/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.adapter.http.core;

import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

import java.util.function.LongFunction;

public class HttpOpMapper implements OpMapper<HttpOp> {

    private final NBConfiguration cfg;
    private final DriverSpaceCache<? extends HttpSpace> spaceCache;

    public HttpOpMapper(NBConfiguration cfg, DriverSpaceCache<? extends HttpSpace> spaceCache) {
        this.cfg = cfg;
        this.spaceCache = spaceCache;
    }

    @Override
    public OpDispenser<? extends HttpOp> apply(ParsedOp op) {
        LongFunction<String> spaceNameF = op.getAsFunctionOr("space", "default");
        LongFunction<HttpSpace> spaceFunc = l -> spaceCache.get(spaceNameF.apply(l));
        return new HttpOpDispenser(spaceFunc, op);
    }
}
