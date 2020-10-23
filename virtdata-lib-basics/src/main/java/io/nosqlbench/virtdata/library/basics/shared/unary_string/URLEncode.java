package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * URLEncode string data
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class URLEncode implements Function<String, String> {

    private final Charset charset;

    /**
     * UrlEncode any incoming string using the specified charset.
     *
     * @param charset A valid character set name from {@link Charset}
     */
    @Example({"URLEncode('UTF-16')", "URLEncode using the UTF-16 charset."})
    public URLEncode(String charset) {
        this.charset = Charset.forName(charset);
    }

    @Example({"URLEncode()", "URLEncode using the default UTF-8 charset."})
    public URLEncode() {
        this.charset = StandardCharsets.UTF_8;
    }

    @Override
    public String apply(String s) {
        return URLEncoder.encode(s, charset);
    }
}
