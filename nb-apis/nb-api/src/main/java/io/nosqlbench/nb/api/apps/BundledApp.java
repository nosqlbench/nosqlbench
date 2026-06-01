/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.nb.api.apps;

import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.nb.api.docsapi.Docs;
import io.nosqlbench.nb.api.docsapi.DocsBinder;
import io.nosqlbench.nb.annotations.Service;
import picocli.CommandLine;

import java.util.Optional;
import java.util.function.ToIntFunction;

public interface BundledApp extends ToIntFunction<String[]> {

    int applyAsInt(String[] value);

    default String getBundledAppName() {
        return this.getClass().getAnnotation(Service.class).selector();
    }

    /// Exposes this app's picocli command model so that callers in the command stream (the
    /// `runapp` verb) can adapt NoSQLBench `name=value` parameters into a correct argv for
    /// this specific app.
    ///
    /// The returned [CommandLine] is read for metadata only — its option names, types, and
    /// subcommands — and is never executed here; the app's own [#applyAsInt(String[])] remains
    /// the single execution entry point. Apps that parse their arguments with picocli should
    /// override this to return their model, typically `Optional.of(new CommandLine(this))`
    /// when the app itself is the `@Command`-annotated object, or
    /// `Optional.of(new CommandLine(new SomeCli()))` when parsing is delegated.
    ///
    /// Apps that delegate to an external CLI with no picocli model leave this empty. In that
    /// case the caller falls back to a literal `--name=value` mapping, which cannot express
    /// positionals or subcommands; such apps remain fully usable via the top-level
    /// `nb5 <app> ...` invocation with their native argv.
    ///
    /// @return this app's picocli command model, or empty if it has none
    default Optional<CommandLine> getCommandModel() {
        return Optional.empty();
    }

    default DocsBinder getBundledDocs() {
        Docs docs = new Docs().namespace("apps");

        String dev_docspath = "app-" + this.getBundledAppName() + "/src/main/resources/docs/" + this.getBundledAppName();
        String cp_docspath = "docs/" + this.getBundledAppName();
        Optional<Content<?>> bundled_docs = NBIO.local().pathname(dev_docspath, cp_docspath).first();
        bundled_docs.map(Content::asPath).ifPresent(docs::addContentsOf);

        Optional<Content<?>> maindoc = NBIO.local().pathname("/src/main/resources/" + this.getBundledAppName() + ".md", this.getBundledAppName() + ".md").first();

        maindoc.map(Content::asPath).ifPresent(docs::addPath);

        return docs.asDocsBinder();
    }

}
