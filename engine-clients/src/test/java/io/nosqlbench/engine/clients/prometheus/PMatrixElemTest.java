/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.clients.prometheus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.nosqlbench.api.content.NBIO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

public class PMatrixElemTest {

    @Test
    @Disabled
    public void testMatrixElem() {
        Gson gson = new GsonBuilder().create();
        String json = NBIO.classpath().pathname("test.json").one().asString();
        Type type = new TypeToken<PromQueryResult<PMatrixData>>() {
        }.getType();
        Object result = gson.fromJson(json, type);
        assertThat(result).isOfAnyClassIn(PromQueryResult.class);

    }
}
