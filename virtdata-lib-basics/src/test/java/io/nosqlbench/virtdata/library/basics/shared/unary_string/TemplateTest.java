package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.Template;
import org.junit.Test;

import java.util.function.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateTest {


    @Test
    public void testTemplate() {
        Template t = new Template("{}-->{}{}", new F("={}="), new F("_{}_"), new F("<{}>"));
        assertThat(t.apply(6L)).isEqualTo("=6=-->_7_<8>");
    }

    @Test
    public void testExtraCurlyBraces() {
        Template t = new Template("{{}-->{}{}}", new F("={}="), new F("_{}_"), new F("<{}>"));
        assertThat(t.apply(6L)).isEqualTo("{=6=-->_7_<8>}");
    }

    @Test
    public void testVariousFuncTypes() {
        IntFunction f1 = value -> value+3;
        IntUnaryOperator f4 = value -> value +7;
        LongFunction f2 = value -> value+4L;
        LongUnaryOperator f3 = value -> value+5L;
        DoubleUnaryOperator f5 = value -> value +23d;
        DoubleFunction f6 = value -> value+ 234d;
        Function<Object,String> f7 = value -> value + "__";

        Template t = new Template("{} {} {} {} {} {} {}",
                f1, f2, f3, f4, f5, f6, f7);
        String result = t.apply(23L);
        assertThat(result).isEqualTo("26 28 30 33 50.0 262.0 29__");
    }

    private static class F implements LongFunction<String> {

        private final String template;

        public F(String template) {
            this.template = template;
        }

        @Override
        public String apply(long value) {
            return template.replaceAll("\\{}", String.valueOf(value));
        }
    }

//    @Test
//    public void testUnescaping() {
//        String escaped="front \\' back";
//        LongFunction<String> func = String::valueOf;
//        Template template = new Template("{} extra", func);
//        String unescaped = template.unescape(escaped);
//        assertThat(unescaped).isEqualTo("front ' back");
//
//        String unescaped2= template.unescape("\\' one \\' two \\'");
//        assertThat(unescaped2).isEqualTo("' one ' two '");
//    }
//
//    @Test
//    public void testBackslashUnescaping() {
//        String escaped="front \\\\\" back";
//        LongFunction<String> func = String::valueOf;
//        Template template = new Template("{} extra", func);
//        String unescaped = template.unescape(escaped);
//        assertThat(unescaped).isEqualTo("front \\\" back");
//
//    }
}
