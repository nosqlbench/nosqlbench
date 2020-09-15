package io.nosqlbench.engine.rest.services.openapi;

import org.junit.Test;

public class OpenApiLoaderTest {

    @Test
    public void testYamlGenerator() {
        String openidpath = "stargate.yaml";
        String filterJson = "{\n" +
            "'POST /api/rest/v1/auth' : {}\n" +
            "}\n";

        String result = OpenApiLoader.generateWorkloadFromFilepath(openidpath, filterJson);
        System.out.println(result);

    }

}
