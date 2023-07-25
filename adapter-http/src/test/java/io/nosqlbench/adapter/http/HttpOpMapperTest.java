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

package io.nosqlbench.adapter.http;

import io.nosqlbench.adapter.http.core.HttpOpMapper;
import io.nosqlbench.adapter.http.core.HttpSpace;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpOpMapperTest {

    private static final Logger logger = LogManager.getLogger(HttpOpMapperTest.class);
    static NBConfiguration cfg;
    static HttpDriverAdapter adapter;
    static HttpOpMapper mapper;

    @BeforeAll
    public static void initializeTestMapper() {
        HttpOpMapperTest.cfg = HttpSpace.getConfigModel().apply(Map.of());
        HttpOpMapperTest.adapter = new HttpDriverAdapter();
        HttpOpMapperTest.adapter.applyConfig(HttpOpMapperTest.cfg);
        final DriverSpaceCache<? extends HttpSpace> cache = HttpOpMapperTest.adapter.getSpaceCache();
        HttpOpMapperTest.mapper = new HttpOpMapper(HttpOpMapperTest.adapter, HttpOpMapperTest.cfg, cache);
    }

    private static ParsedOp parsedOpFor(final String yaml) {
        final OpsDocList docs = OpsLoader.loadString(yaml, OpTemplateFormat.yaml, Map.of(), null);
        final OpTemplate opTemplate = docs.getOps().get(0);
        final ParsedOp parsedOp = new ParsedOp(opTemplate, HttpOpMapperTest.cfg, List.of(HttpOpMapperTest.adapter.getPreprocessor()), NBLabeledElement.forMap(Map.of()));
        return parsedOp;
    }

    @Test
    public void testOnelineSpec() {
        final ParsedOp pop = HttpOpMapperTest.parsedOpFor("""
            ops:
             - s1: method=get uri=http://localhost/
            """);
        assertThat(pop.getDefinedNames()).containsAll(List.of("method", "uri"));
    }


    @Test
    public void testRFCFormMinimal() {
        final ParsedOp pop = HttpOpMapperTest.parsedOpFor("""
                ops:
                 - s1: get http://localhost/
            """);

        assertThat(pop.getDefinedNames()).containsAll(List.of("method", "uri"));
    }


    @Test
    public void testRFCFormVersioned() {
        final ParsedOp pop = HttpOpMapperTest.parsedOpFor("""
                ops:
                 - s1: get http://localhost/ HTTP/1.1
            """);
        assertThat(pop.getDefinedNames()).containsAll(List.of("method", "uri", "version"));
    }

    @Test
    public void testRFCFormHeaders() {
        final ParsedOp pop = HttpOpMapperTest.parsedOpFor("""
                ops:
                 - s1: |
                    get http://localhost/
                    Content-Type: application/json
                """);
        assertThat(pop.getDefinedNames()).containsAll(List.of("method", "uri", "Content-Type"));
    }

    @Test
    public void testRFCFormBody() {
        final ParsedOp pop = HttpOpMapperTest.parsedOpFor("""
                ops:
                 - s1: |
                    get http://localhost/

                    body1
            """);

        assertThat(pop.getDefinedNames()).containsAll(List.of("method", "uri", "body"));
    }

    @Test
    public void testRFCAllValuesTemplated() {

        // This can not be fully resolved in the unit testing context, but it could be
        // in the integrated testing context. It is sufficient to verify parsing here.
        final ParsedOp pop = HttpOpMapperTest.parsedOpFor("""
                ops:
                 - s1: |
                    {method} {scheme}://{host}/{path}?{query} {version}
                    Header1: {header1val}

                    {body}

                bindings:
                 method: StaticStringMapper('test')
                 scheme: StaticStringMapper('test')
                 host: StaticStringMapper('test')
                 path: StaticStringMapper('test')
                 query: StaticStringMapper('test')
                 version: StaticStringMapper('test')
                 header1val: StaticStringMapper('test')
                 body: StaticStringMapper('test')
            """);

        HttpOpMapperTest.logger.debug(pop);
        assertThat(pop.getDefinedNames()).containsAll(List.of(
            "method","uri","version","Header1","body"
        ));
    }


}
