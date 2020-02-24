package io.nosqlbench.virtdata.processors;

import java.util.List;
import java.util.Map;

public interface DocCtorData {

    /**
     * @return the {@link Class#getSimpleName()} of the documented ctor.
     */
    String getClassName();

    /**
     * @return javadoc for the documented ctor, or null if it isn't provided
     */
    String getCtorJavaDoc();

    /**
     * @return an ordered map of the arguments of the documented constructor in name,type form.
     */
    Map<String, String> getArgs();

    /**
     * @return a list of examples, where each is list of (example syntax, comment..)
     */
    List<List<String>> getExamples();
}
