package io.nosqlbench.engine.clients.prometheus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.nosqlbench.nb.api.content.NBIO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

public class PMatrixElemTest {

    @Test
    @Disabled
    public void testMatrixElem() {
        Gson gson = new GsonBuilder().create();
        String json = NBIO.classpath().name("test.json").one().asString();
        Type type = new TypeToken<PromQueryResult<PMatrixData>>() {
        }.getType();
        Object result = gson.fromJson(json, type);
        assertThat(result).isOfAnyClassIn(PromQueryResult.class);

    }
}
