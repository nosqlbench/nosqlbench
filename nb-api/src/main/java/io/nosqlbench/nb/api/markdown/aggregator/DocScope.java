package io.nosqlbench.nb.api.markdown.aggregator;

/**
 * DocScope determines which display mode a topic is meant to be displayed in.
 * You should filter for the DocScopes you are interested in when you ask
 * for markdown content.
 *
 * The special values ANY and NONE are provided for two reasons:
 */
public enum DocScope {

    CommandLine(false),
    StaticWeb(false),
    DynamicWeb(false),
    ANY(true),
    NONE(true);

    /**
     * If a doc scope is marked as a query param, then it may only be used as a query param, or returned
     * as a default or qualifier, but should not be assigned in content metadata.
     * Content readers should throw an error when ANY or NONE are found in raw content.
     * Content readers should add ANY to any content which contains any non-query scope.
     * Content readers should add NONE to any content which contains no non-query scope.
     *
     * This is added to provide a uniform and simple query interface.
     */
    private final boolean queryParam;

    DocScope(boolean queryParam) {
        this.queryParam = queryParam;
    }
}
