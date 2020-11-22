package io.nosqlbench.nb.api.spi;

public interface Named {
    /**
     * <p>Return the name for this function library implementation.</p>
     *
     * @return Simple lower-case canonical library name
     */
    String getName();
}
