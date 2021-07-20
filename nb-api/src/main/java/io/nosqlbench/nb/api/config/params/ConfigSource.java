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
    List<ElementData> getAll(String injectedName, Object source);

    /**
     * If an element was created with a name, this name must be returned as the
     * canonical name. If it was not, then the name field can provide the name.
     * @return A name, or null if it is not given nor present in the name field
     */
    String getName();
}
