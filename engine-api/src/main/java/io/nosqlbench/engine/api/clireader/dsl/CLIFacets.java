package io.nosqlbench.engine.api.clireader.dsl;

public interface CLIFacets {

    interface WantsAnyOption
        extends WantsGlobalOption {
    }

    interface WantsParameterizedCommand {
        WantsAnyOption namedParams();
    }

    interface WantsGlobalOption {
        WantsOptionType global(String optionName);
    }

    interface WantsOptionType {
        WantsAnyOption toggle();
        WantsAnyOption string();
        WantsAnyOption number();
    }

}
