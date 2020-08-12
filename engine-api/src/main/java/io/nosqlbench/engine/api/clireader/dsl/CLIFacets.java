package io.nosqlbench.engine.api.clireader.dsl;

public interface CLIFacets {

    public static interface WantsAnyOption
        extends WantsGlobalOption {
    }

    public static interface WantsParameterizedCommand {
        public WantsAnyOption namedParams();
    }

    public static interface WantsGlobalOption {
        public WantsOptionType global(String optionName);
    }

    public static interface WantsOptionType {
        public WantsAnyOption toggle();
        public WantsAnyOption string();
        public WantsAnyOption number();
    }

}
