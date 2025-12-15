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

package io.nosqlbench.nb.mql.format;

/**
 * Enum representing all supported output formats for query results.
 * Provides type safety and validation for format selection in CLI.
 */
public enum OutputFormat {
    /**
     * ASCII tables for console output (default).
     */
    TABLE("table", "txt", "ASCII tables for console output") {
        @Override
        public ResultFormatter createFormatter() {
            return new TableFormatter();
        }
    },

    /**
     * Comma-separated values for spreadsheet import.
     */
    CSV("csv", "csv", "Comma-separated values for Excel/Sheets") {
        @Override
        public ResultFormatter createFormatter() {
            return new CsvFormatter();
        }
    },

    /**
     * Tab-separated values for Unix text processing.
     */
    TSV("tsv", "tsv", "Tab-separated values for awk/cut/paste") {
        @Override
        public ResultFormatter createFormatter() {
            return new TsvFormatter();
        }
    },

    /**
     * Pretty-printed JSON for programmatic use.
     */
    JSON("json", "json", "Pretty-printed JSON with metadata") {
        @Override
        public ResultFormatter createFormatter() {
            return new JsonFormatter(true);
        }
    },

    /**
     * JSON Lines (one JSON object per line) for streaming.
     */
    JSONL("jsonl", "jsonl", "JSON Lines for streaming processors") {
        @Override
        public ResultFormatter createFormatter() {
            return new JsonLinesFormatter();
        }
    },

    /**
     * GitHub-flavored Markdown tables for documentation.
     */
    MARKDOWN("markdown", "md", "Markdown tables for documentation") {
        @Override
        public ResultFormatter createFormatter() {
            return new MarkdownFormatter();
        }
    };

    private final String name;
    private final String extension;
    private final String description;

    OutputFormat(String name, String extension, String description) {
        this.name = name;
        this.extension = extension;
        this.description = description;
    }

    /**
     * Create a formatter instance for this format.
     * Each enum value knows how to create its own formatter.
     *
     * @return ResultFormatter instance
     */
    public abstract ResultFormatter createFormatter();

    /**
     * Get the format name as it appears in CLI arguments.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the file extension for this format.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Get a human-readable description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Parse a format name string to an enum value.
     *
     * @param name Format name (case-insensitive)
     * @return The OutputFormat enum, or null if not found
     */
    public static OutputFormat fromString(String name) {
        if (name == null || name.trim().isEmpty()) {
            return TABLE; // Default
        }

        String normalized = name.toLowerCase().trim();
        for (OutputFormat format : values()) {
            if (format.name.equals(normalized)) {
                return format;
            }
        }
        return null; // Not found
    }

    /**
     * Get a comma-separated list of all format names.
     * Useful for CLI help text.
     */
    public static String getAllFormatNames() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values().length; i++) {
            sb.append(values()[i].name);
            if (i < values().length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
