/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.mql.commands;

import io.nosqlbench.nb.mql.format.TableFormatter;
import io.nosqlbench.nb.mql.query.InvalidQueryException;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;
import io.nosqlbench.nb.mql.testdata.TestDatabaseLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("mql")
@Tag("unit")
class RatioCommandTest {

    @Test
    void testRatioCommandExists() throws Exception {
        // Basic smoke test - just verify command can be created and validated
        RatioCommand command = new RatioCommand();
        assertEquals("ratio", command.getName());

        // Valid params should pass validation
        command.validate(Map.of(
            "numerator", "metric1_total",
            "denominator", "metric2_total"
        ));
    }


    @Test
    void testValidationMissingNumerator() {
        RatioCommand command = new RatioCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("denominator", "total")));
    }

    @Test
    void testValidationMissingDenominator() {
        RatioCommand command = new RatioCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("numerator", "errors")));
    }

    @Test
    void testValidationEmptyMetrics() {
        RatioCommand command = new RatioCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("numerator", "", "denominator", "total")));
    }

    @Test
    void testValidationValid() throws Exception {
        RatioCommand command = new RatioCommand();
        command.validate(Map.of("numerator", "errors", "denominator", "total"));
    }
}
