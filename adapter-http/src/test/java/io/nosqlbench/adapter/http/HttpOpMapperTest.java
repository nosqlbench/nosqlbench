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
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.engine.api.activityconfig.OpsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.engine.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpOpMapperTest {

    private final static Logger logger = LogManager.getLogger(HttpOpMapperTest.class);
    static NBConfiguration cfg;
    static HttpDriverAdapter adapter;
    static HttpOpMapper mapper;

    @BeforeAll
    public static void initializeTestMapper() {
        cfg = HttpSpace.getConfigModel().apply(Map.of());
        adapter = new HttpDriverAdapter();
        adapter.applyConfig(cfg);
        DriverSpaceCache<? extends HttpSpace> cache = adapter.getSpaceCache();
        mapper = new HttpOpMapper(adapter,cfg, cache);
    }

    private static ParsedOp parsedOpFor(String yaml) {
        OpsDocList docs = OpsLoader.loadString(yaml, OpTemplateFormat.yaml, Map.of(), null);
        OpTemplate stmtDef = docs.getStmts().get(0);
        ParsedOp parsedOp = new ParsedOp(stmtDef, cfg, List.of(adapter.getPreprocessor()));
        return parsedOp;
    }

    @Test
    public void testOnelineSpec() {
        ParsedOp pop = parsedOpFor("""
            ops:
             - s1: method=get uri=http://localhost/
            """);
        assertThat(pop.getDefinedNames()).containsAll(List.of("method", "uri"));
    }


    @Test
    public void testRFCFormMinimal() {
        ParsedOp pop = parsedOpFor("""
                ops:
                 - s1: get http://localhost/
            """);

        assertThat(pop.getDefinedNames()).containsAll(List.of("method", "uri"));
    }


    @Test
    public void testRFCFormVersioned() {
        ParsedOp pop = parsedOpFor("""
                ops:
                 - s1: get http://localhost/ HTTP/1.1
            """);
        assertThat(pop.getDefinedNames()).containsAll(List.of("method", "uri", "version"));
    }

    @Test
    public void testRFCFormHeaders() {
        ParsedOp pop = parsedOpFor("""
                ops:
                 - s1: |
                    get http://localhost/
                    Content-Type: application/json
                """);
        assertThat(pop.getDefinedNames()).containsAll(List.of("method", "uri", "Content-Type"));
    }

    @Test
    public void testRFCFormBody() {
        ParsedOp pop = parsedOpFor("""
                statements:
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
        ParsedOp pop = parsedOpFor("""
                statements:
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

        logger.debug(pop);
        assertThat(pop.getDefinedNames()).containsAll(List.of(
            "method","uri","version","Header1","body"
        ));
    }


}
