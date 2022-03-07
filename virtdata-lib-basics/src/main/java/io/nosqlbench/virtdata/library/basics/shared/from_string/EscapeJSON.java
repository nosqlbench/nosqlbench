package io.nosqlbench.virtdata.library.basics.shared.from_string;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.text.StringEscapeUtils;

import java.util.function.Function;

/**
 * Escape all special characters which are required to be escaped when found within
 * JSON content according to the JSON spec
 * <pre>{@code
 * \b  Backspace (ascii code 08)
 * \f  Form feed (ascii code 0C)
 * \n  New line
 * \r  Carriage return
 * \t  Tab
 * \"  Double quote
 * \\  Backslash character
 * \/  Forward slash
 * }</pre>
 */
@ThreadSafeMapper
public class EscapeJSON implements Function<String,String> {
    Gson gson = new GsonBuilder().create();

    @Override
    public String apply(String s) {
        return StringEscapeUtils.escapeJson(s);
    }
}
