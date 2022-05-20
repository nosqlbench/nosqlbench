package io.nosqlbench.driver.direct;

/*
 * Copyright (c) 2022 nosqlbench
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


import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.annotations.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * This activity type driver allows you to dynamically map any available
 * Java API which is exposed to the NoSQLBench runtime, executing methods
 * on this API by name, (optionally) storing named results, and re-using
 * these named results as arguments to subsequent calls.
 *
 * It supports static method dispatch, instance methods, and per-thread
 * object scoping.
 */
@Service(value = DriverAdapter.class, selector = "directapi")
public class DirectCallAdapter extends BaseDriverAdapter<DirectCall,Void> {

    @Override
    public List<Function<String, Optional<Map<String, Object>>>> getOpStmtRemappers() {
        return List.of(new DirectCallStmtParser());
    }

    @Override
    public OpMapper<DirectCall> getOpMapper() {
        return new DirectOpMapper();
    }
}
