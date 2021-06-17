package io.nosqlbench.virtdata.core.templates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedTemplateTest {

    @Test
    public void testParsedTemplate() {
        ParsedTemplate pt = new ParsedTemplate("test template", Map.of());
        assertThat(pt.getAnchors()).isEmpty();
        assertThat(pt.getCheckedBindPoints()).isEmpty();
        assertThat(pt.getSpans()).contains("test template");
        assertThat(pt.getMissing()).isEmpty();
    }

    @Test
    public void testBindPoints() {
        ParsedTemplate pt = new ParsedTemplate("test template {missing1}", Map.of("b1","v1"));
        assertThat(pt.getSpans()).contains("test template ");
        assertThat(pt.getAnchors()).containsExactly("missing1");
        assertThat(pt.getUncheckedBindPoints()).containsExactly(new BindPoint("missing1",null));
    }

    @Test
    public void testSingleBinding() {
        ParsedTemplate pt = new ParsedTemplate("{single}", Map.of());
        Optional<BindPoint> sb = pt.asBinding();
        assertThat(sb).isPresent();
        assertThat(sb).contains(new BindPoint("single",null));
    }

    @Test
    public void testJsonFormat() {
        ParsedTemplate pt = new ParsedTemplate("test template {missing1}", Map.of("b1","v1"));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String format = gson.toJson(pt);
        System.out.println(format);


    }


}
