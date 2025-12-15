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

package io.nosqlbench.nb.mql.testdata;

import java.nio.file.Path;

/**
 * Generates the reference example database used in EXAMPLES.md documentation.
 * This database has specific, well-known timestamps and values that are referenced
 * in documentation and validated by ExampleDocumentationTest.
 *
 * The data represents a simplified e-commerce API with:
 * - Request counters (GET/POST operations on different endpoints)
 * - API latency timers
 * - HTTP status codes (200, 404, 500)
 *
 * Timeline:
 * - 2025-10-23 10:00:00Z - T+0: Initial state (zeros)
 * - 2025-10-23 10:01:00Z - T+1min: Normal traffic
 * - 2025-10-23 10:02:00Z - T+2min: Increased load
 * - 2025-10-23 10:05:00Z - T+5min: High load period
 * - 2025-10-23 10:10:00Z - T+10min: Back to normal
 */
public class ExampleDataGenerator {

    /**
     * Generate examples.db - the reference database for documentation examples.
     */
    public static void generateExamples(Path outputPath) throws Exception {
        MetricsDataBuilder.create(outputPath)
            // T+0: Initial state (10:00:00)
            .atTime("2025-10-23T10:00:00Z")
                .counter("api_requests", 0, "method", "GET", "endpoint", "/users", "status", "200")
                .counter("api_requests", 0, "method", "GET", "endpoint", "/products", "status", "200")
                .counter("api_requests", 0, "method", "POST", "endpoint", "/users", "status", "200")
                .counter("api_requests", 0, "method", "GET", "endpoint", "/users", "status", "404")
                .counter("api_requests", 0, "method", "GET", "endpoint", "/users", "status", "500")
                .timer("api_latency", new long[]{10, 12, 15, 20, 25}, "method", "GET", "endpoint", "/users")
                .timer("api_latency", new long[]{8, 10, 12, 14, 16}, "method", "GET", "endpoint", "/products")

            // T+1min: Normal traffic (10:01:00)
            .atTime("2025-10-23T10:01:00Z")
                .counter("api_requests", 1000, "method", "GET", "endpoint", "/users", "status", "200")
                .counter("api_requests", 800, "method", "GET", "endpoint", "/products", "status", "200")
                .counter("api_requests", 150, "method", "POST", "endpoint", "/users", "status", "200")
                .counter("api_requests", 5, "method", "GET", "endpoint", "/users", "status", "404")
                .counter("api_requests", 2, "method", "GET", "endpoint", "/users", "status", "500")
                .timer("api_latency", new long[]{10, 12, 15, 18, 22, 25, 30}, "method", "GET", "endpoint", "/users")
                .timer("api_latency", new long[]{8, 10, 11, 13, 15, 18}, "method", "GET", "endpoint", "/products")

            // T+2min: Increased load (10:02:00)
            .atTime("2025-10-23T10:02:00Z")
                .counter("api_requests", 2200, "method", "GET", "endpoint", "/users", "status", "200")
                .counter("api_requests", 1650, "method", "GET", "endpoint", "/products", "status", "200")
                .counter("api_requests", 320, "method", "POST", "endpoint", "/users", "status", "200")
                .counter("api_requests", 12, "method", "GET", "endpoint", "/users", "status", "404")
                .counter("api_requests", 4, "method", "GET", "endpoint", "/users", "status", "500")
                .timer("api_latency", new long[]{12, 15, 18, 22, 28, 35, 42}, "method", "GET", "endpoint", "/users")
                .timer("api_latency", new long[]{9, 11, 13, 15, 18, 22}, "method", "GET", "endpoint", "/products")

            // T+5min: High load period (10:05:00)
            .atTime("2025-10-23T10:05:00Z")
                .counter("api_requests", 5500, "method", "GET", "endpoint", "/users", "status", "200")
                .counter("api_requests", 4200, "method", "GET", "endpoint", "/products", "status", "200")
                .counter("api_requests", 750, "method", "POST", "endpoint", "/users", "status", "200")
                .counter("api_requests", 35, "method", "GET", "endpoint", "/users", "status", "404")
                .counter("api_requests", 8, "method", "GET", "endpoint", "/users", "status", "500")
                .timer("api_latency", new long[]{15, 20, 25, 32, 45, 58, 75}, "method", "GET", "endpoint", "/users")
                .timer("api_latency", new long[]{10, 13, 16, 20, 25, 30}, "method", "GET", "endpoint", "/products")

            // T+10min: Back to normal (10:10:00)
            .atTime("2025-10-23T10:10:00Z")
                .counter("api_requests", 11000, "method", "GET", "endpoint", "/users", "status", "200")
                .counter("api_requests", 8500, "method", "GET", "endpoint", "/products", "status", "200")
                .counter("api_requests", 1500, "method", "POST", "endpoint", "/users", "status", "200")
                .counter("api_requests", 75, "method", "GET", "endpoint", "/users", "status", "404")
                .counter("api_requests", 15, "method", "GET", "endpoint", "/users", "status", "500")
                .timer("api_latency", new long[]{11, 14, 17, 22, 28, 35, 45}, "method", "GET", "endpoint", "/users")
                .timer("api_latency", new long[]{9, 11, 13, 16, 19, 24}, "method", "GET", "endpoint", "/products")

            .build();
    }

    /**
     * Main method to generate the examples database.
     */
    public static void main(String[] args) throws Exception {
        Path examplesDb = Path.of("nb-apis/nb-mql-api/src/test/resources/testdata/examples.db");

        System.out.println("Generating examples.db for documentation...");
        generateExamples(examplesDb);
        System.out.println("✓ Generated examples.db");
        System.out.println("Location: " + examplesDb.toAbsolutePath());
    }
}
