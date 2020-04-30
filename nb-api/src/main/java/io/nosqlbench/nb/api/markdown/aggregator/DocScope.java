package io.nosqlbench.nb.api.markdown.aggregator;

/**
 * DocScope determines which display mode a topic is meant to be displayed in.
 * You should filter for the DocScopes you are interested in when you ask
 * for markdown content.
 *
 * The special values ANY and NONE are provided for two reasons:
 */
public enum DocScope {

    /**
     * The command line doc scope includes any markdown which should be shown to the user
     * when they are searching for or viewing documentation on a command line.
     */
    CommandLine(false),

    /**
     * The static web doc scope includes any markdown which should be shown to the user
     * when they are viewing documentation on an externally hosted site in static form.
     */
    StaticWeb(false),

    /**
     * The dynamic web doc scope includes any markdown which should be made available to
     * users when they are interacting with a web application.
     */
    DynamicWeb(false),

    /**
     * ANY is a descriptive doc scope which is meant to be used as a filter within API calls
     * to find and display docs. It is invalid for any raw markdown content to be tagged
     * with this doc scope.
     */
    ANY(true),

    /**
     * NONE is a descriptive doc scope which is meant to be used as the default value for
     * found raw markdown if it has not been tagged with a direct scope. This scope should only
     * be returned as a place holder when no scopes are defined on content. When reading raw
     * content, finding the NONE scope explicitly on raw source is considered an error.
     */
    NONE(true);

    /**
     * If a doc scope is marked as a query param, then it may only be used as a query param, or returned
     * as a default or qualifier, but should not be assigned in content metadata.
     * Content readers should throw an error when ANY or NONE are found in raw content.
     * Content readers should add ANY to any content which contains any non-query scope.
     *
     * This is added to provide a uniform and simple query interface.
     */
    private final boolean queryParam;

    DocScope(boolean queryParam) {
        this.queryParam = queryParam;
    }
}
