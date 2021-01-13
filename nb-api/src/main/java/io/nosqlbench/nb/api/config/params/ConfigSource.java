package io.nosqlbench.nb.api.config.params;

import java.util.List;

/**
 * A Config Source knows how to read a block of data and convert it
 * into a stream of zero or more configuration elements.
 */
public interface ConfigSource {

    /**
     * Test the input data format to see if it appears valid for reading
     * with this config source.
     *
     * @param source An object of any kind
     * @return true if the text is parsable by this config source
     */
    boolean canRead(Object source);

    /**
     * Read the source of data into a collection of config elements
     *
     * @param source An object of any kind
     * @return a collection of {@link Element}s
     */
    List<ElementData> getAll(Object source);

//    ElementData getOneElementData(Object src);
}
