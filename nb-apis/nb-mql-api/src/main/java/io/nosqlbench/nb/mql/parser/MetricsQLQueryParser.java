/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.mql.parser;

import io.nosqlbench.nb.mql.generated.MetricsQLLexer;
import io.nosqlbench.nb.mql.generated.MetricsQLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for parsing MetricsQL queries with proper error handling.
 *
 * <p>This class encapsulates the setup of ANTLR lexer, parser, and custom error listener
 * to provide user-friendly error messages when parsing MetricsQL queries.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * MetricsQLQueryParser queryParser = new MetricsQLQueryParser();
 * SQLFragment sql = queryParser.parse("rate(http_requests[5m])");
 * }</pre>
 */
public class MetricsQLQueryParser {
    private static final Logger logger = LoggerFactory.getLogger(MetricsQLQueryParser.class);

    private final MetricsQLTransformer transformer;

    /**
     * Creates a new query parser with default transformer.
     */
    public MetricsQLQueryParser() {
        this.transformer = new MetricsQLTransformer();
    }

    /**
     * Creates a new query parser with a custom transformer.
     *
     * @param transformer The transformer to use for converting parse trees to SQL
     */
    public MetricsQLQueryParser(MetricsQLTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Parses a MetricsQL query and transforms it to SQL.
     *
     * @param query The MetricsQL query string
     * @return SQLFragment containing the generated SQL and parameters
     * @throws MetricsQLParseException if the query has syntax errors
     */
    public SQLFragment parse(String query) {
        return parse(query, true);
    }

    /**
     * Parses a MetricsQL query with optional strict error checking.
     *
     * @param query The MetricsQL query string
     * @param strictErrors If true, throw exception on parse errors; if false, attempt partial parsing
     * @return SQLFragment containing the generated SQL and parameters
     * @throws MetricsQLParseException if strictErrors is true and the query has syntax errors
     */
    public SQLFragment parse(String query, boolean strictErrors) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        logger.debug("Parsing MetricsQL query: {}", query);

        // Create lexer and parser
        MetricsQLLexer lexer = new MetricsQLLexer(CharStreams.fromString(query));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MetricsQLParser parser = new MetricsQLParser(tokens);

        // Remove default console error listener
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

        // Add custom error listener
        MetricsQLErrorListener errorListener = new MetricsQLErrorListener();
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);

        // Parse the query
        MetricsQLParser.QueryContext parseTree = parser.query();

        // Check for errors
        if (strictErrors && errorListener.hasErrors()) {
            String errorMessage = "Failed to parse MetricsQL query:\n" + errorListener.getErrorsAsString();
            logger.error("Parse errors: {}", errorMessage);
            throw new MetricsQLParseException(errorMessage, errorListener.getErrors());
        }

        // Transform to SQL
        try {
            SQLFragment result = transformer.visit(parseTree);
            logger.debug("Successfully transformed query to SQL");
            return result;
        } catch (Exception e) {
            logger.error("Error transforming parse tree to SQL", e);
            throw new MetricsQLParseException("Error transforming query to SQL: " + e.getMessage(), e);
        }
    }

    /**
     * Validates a MetricsQL query without transforming it to SQL.
     *
     * @param query The MetricsQL query string
     * @return true if the query is valid, false otherwise
     */
    public boolean validate(String query) {
        try {
            parse(query, true);
            return true;
        } catch (MetricsQLParseException e) {
            return false;
        }
    }

    /**
     * Validates a MetricsQL query and returns any errors found.
     *
     * @param query The MetricsQL query string
     * @return List of error messages, or empty list if query is valid
     */
    public java.util.List<String> validateWithErrors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return java.util.List.of("Query cannot be null or empty");
        }

        MetricsQLLexer lexer = new MetricsQLLexer(CharStreams.fromString(query));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MetricsQLParser parser = new MetricsQLParser(tokens);

        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

        MetricsQLErrorListener errorListener = new MetricsQLErrorListener();
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);

        parser.query();

        return errorListener.getErrors();
    }
}
