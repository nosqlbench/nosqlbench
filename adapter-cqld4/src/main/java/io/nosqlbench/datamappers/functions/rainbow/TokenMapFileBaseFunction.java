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

package io.nosqlbench.datamappers.functions.rainbow;

import java.util.function.IntToLongFunction;

public abstract class TokenMapFileBaseFunction implements IntToLongFunction {
    protected transient static ThreadLocal<TokenMapFileAPIService> tl_DataSvc;

    public TokenMapFileBaseFunction(String filename, boolean loopdata, boolean instanced, boolean ascending) {
        tl_DataSvc = ThreadLocal.withInitial(() -> new TokenMapFileAPIService(filename, loopdata, instanced, ascending));
    }

    public TokenMapFileBaseFunction(String filename) {
        this(filename, false, true, true);
    }

//    @Override
//    public long applyAsLong(long operand) {
//        BinaryCursorForTokenCycle bc;
//        bc.next(operand);
//        return 0;
//    }
}
