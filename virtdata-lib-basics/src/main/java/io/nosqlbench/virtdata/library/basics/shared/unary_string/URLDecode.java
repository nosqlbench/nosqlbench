package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * URLDecode string data
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class URLDecode implements Function<String, String> {

    private final Charset charset;

    /**
     * URLDecode any incoming string using the specified charset.
     *
     * @param charset A valid character set name from {@link Charset}
     */
    @Example({"URLDecode('UTF-16')", "URLDecode using the UTF-16 charset."})
    public URLDecode(String charset) {
        this.charset = Charset.forName(charset);
    }

    @Example({"URLDecode()", "URLDecode using the default UTF-8 charset."})
    public URLDecode() {
        this.charset = StandardCharsets.UTF_8;
    }

    @Override
    public String apply(String s) {
        return URLDecoder.decode(s, charset);
    }
}
