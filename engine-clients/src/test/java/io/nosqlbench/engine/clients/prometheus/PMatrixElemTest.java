package io.nosqlbench.engine.clients.prometheus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

public class PMatrixElemTest {

    @Test
    public void testMatrixElem() {
        Gson gson = new GsonBuilder().create();
        String json = """
                {
                   "status" : "success",
                   "data" : {
                      "resultType" : "matrix",
                      "result" : [
                         {
                            "metric" : {
                               "__name__" : "up",
                               "job" : "prometheus",
                               "instance" : "localhost:9090"
                            },
                            "values" : [
                               [ 1435781430.781, "1" ],
                               [ 1435781445.781, "1" ],
                               [ 1435781460.781, "1" ]
                            ]
                         },
                         {
                            "metric" : {
                               "__name__" : "up",
                               "job" : "node",
                               "instance" : "localhost:9091"
                            },
                            "values" : [
                               [ 1435781430.781, "0" ],
                               [ 1435781445.781, "0" ],
                               [ 1435781460.781, "1" ]
                            ]
                         }
                      ]
                   }
                }""";
        Type type = new TypeToken<PromQueryResult<PMatrixData>>() {
        }.getType();
        Object result = gson.fromJson(json, type);
        assertThat(result).isOfAnyClassIn(PromQueryResult.class);

    }
}