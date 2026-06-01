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

package io.nosqlbench.engine.core.lifecycle.commands;

import io.nosqlbench.nb.api.errors.BasicError;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Translates NoSQLBench command-stream `name=value` parameters into a POSIX argv suitable for
/// handing to a {@link io.nosqlbench.nb.api.apps.BundledApp}. The translation is driven by the
/// app's own picocli command model (see
/// {@link io.nosqlbench.nb.api.apps.BundledApp#getCommandModel()}) so that it is correct for that
/// specific app rather than a blind textual rewrite:
///
/// - A `command=<name>` parameter selects a picocli subcommand; its name is emitted as the
///   leading argv token and subsequent parameters are validated against that subcommand's options.
/// - Each remaining parameter is matched (by its un-prefixed name) against a declared
///   `@Option` and emitted as a single `--name=value` token, which picocli parses unambiguously
///   for string, boolean, and split (multi-value) options alike.
/// - A parameter that matches no option raises a {@link BasicError} naming the valid parameters,
///   rather than passing an argument the app would silently reject.
///
/// When the app exposes no model (an empty {@link Optional}), the adapter falls back to a literal
/// `--name=value` mapping with no validation. Such apps cannot express positionals or subcommands
/// through `runapp` and are better invoked at the top level with their native argv.
public final class RunappArgvAdapter {

    private RunappArgvAdapter() {
    }

    /// Build the argv for a bundled app from its command-stream parameters.
    ///
    /// @param params the command parameters, already stripped of the reserved `appname` key
    /// @param model  the app's picocli model, or empty if it has none
    /// @return the argv to pass to {@link io.nosqlbench.nb.api.apps.BundledApp#applyAsInt(String[])}
    public static String[] toArgv(Map<String, String> params, Optional<CommandLine> model) {
        Map<String, String> remaining = new LinkedHashMap<>(params);
        List<String> argv = new ArrayList<>();

        if (model.isEmpty()) {
            remaining.forEach((k, v) -> argv.add("--" + k + "=" + v));
            return argv.toArray(new String[0]);
        }

        CommandSpec spec = model.get().getCommandSpec();

        if (!spec.subcommands().isEmpty() && remaining.containsKey("command")) {
            String sub = remaining.remove("command");
            CommandLine subCmd = spec.subcommands().get(sub);
            if (subCmd == null) {
                throw new BasicError("unknown subcommand '" + sub + "' for app '" + spec.name()
                    + "'. Available subcommands: " + String.join(", ", spec.subcommands().keySet()));
            }
            argv.add(sub);
            spec = subCmd.getCommandSpec();
        }

        Map<String, OptionSpec> optionsByName = optionsByName(spec);
        for (Map.Entry<String, String> entry : remaining.entrySet()) {
            String name = entry.getKey();
            OptionSpec option = optionsByName.get(name);
            if (option == null) {
                throw new BasicError("unknown parameter '" + name + "' for app '" + spec.name()
                    + "'. Valid parameters: " + String.join(", ", optionsByName.keySet())
                    + (spec.subcommands().isEmpty() ? ""
                        : ". Subcommands (select with command=<name>): " + String.join(", ", spec.subcommands().keySet())));
            }
            argv.add(option.longestName() + "=" + entry.getValue());
        }

        return argv.toArray(new String[0]);
    }

    /// Index a command spec's options by each of their declared names with leading dashes
    /// stripped, so that a `name=value` parameter resolves to the option it addresses.
    private static Map<String, OptionSpec> optionsByName(CommandSpec spec) {
        Map<String, OptionSpec> byName = new LinkedHashMap<>();
        for (OptionSpec option : spec.options()) {
            for (String optionName : option.names()) {
                byName.put(optionName.replaceFirst("^-+", ""), option);
            }
        }
        return byName;
    }
}
