package io.nosqlbench.nb.api.markdown.types;

public interface BasicFrontMatterInfo {

    String WEIGHT = "weight";
    String TITLE = "title";

    /**
     * @return A title for the given markdown source file.
     */
    String getTitle();

    /**
     * @return A weight for the given markdown source file.
     */
    int getWeight();
}
