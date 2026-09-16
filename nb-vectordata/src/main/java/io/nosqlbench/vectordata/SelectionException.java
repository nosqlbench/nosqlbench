/*
 * Copyright (c) 2026 The NoSQLBench Authors.
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
package io.nosqlbench.vectordata;

import java.util.List;

/// Why a profile selector produced no usable selection. The message is
/// the reference's, so the same spec fails the same way in either
/// runtime; [#kind] says which way for a caller that branches on it.
public final class SelectionException extends VectorDataException {
    /// The ways a selection fails.
    public enum Kind {
        /// The selector text is malformed; [#position] says where.
        SYNTAX,
        /// No profile matches; [#offered] is what the dataset has.
        NO_MATCH,
        /// A single-profile surface was given a selector matching more
        /// than one profile; [#matches] lists them.
        AMBIGUOUS,
        /// No selector was given and the dataset has no `default`
        /// profile; [#matches] lists the profiles it does have.
        NO_DEFAULT
    }

    private final Kind kind;
    private final int position;
    private final String selector;
    private final List<String> matches;
    private final List<ProfileFacts> offered;

    private SelectionException(Kind kind, String message, int position, String selector, List<String> matches, List<ProfileFacts> offered) {
        super(message);
        this.kind = kind; this.position = position; this.selector = selector;
        this.matches = List.copyOf(matches); this.offered = List.copyOf(offered);
    }

    /// A selector that could not be parsed, with where it went wrong.
    public static SelectionException syntax(int position, String message) {
        return new SelectionException(Kind.SYNTAX, "selector error at " + position + ": " + message, position, null, List.of(), List.of());
    }

    /// A selector that matches no profile, carrying what was on offer.
    public static SelectionException noMatch(String selector, List<ProfileFacts> offered) {
        StringBuilder text = new StringBuilder("selector `" + selector + "` matches no profile; the profiles are:\n");
        for (ProfileFacts facts : offered) text.append("  ").append(facts.summary()).append('\n');
        return new SelectionException(Kind.NO_MATCH, text.toString(), -1, selector, List.of(), offered);
    }

    /// A set handed to a surface that takes one profile.
    public static SelectionException ambiguous(String selector, List<String> matches) {
        return new SelectionException(Kind.AMBIGUOUS, "selector `" + selector + "` matches " + matches.size() + " profiles ("
            + String.join(", ", matches) + ") where one is required; narrow it, or use a surface that takes a set",
            -1, selector, matches, List.of());
    }

    /// No selector, and no `default` to stand for it.
    public static SelectionException noDefault(List<String> profiles) {
        return new SelectionException(Kind.NO_DEFAULT, "no selector given and the dataset has no `default` profile; name one of: "
            + String.join(", ", profiles), -1, null, profiles, List.of());
    }

    /// The same failure, with its message prefixed by the dataset it
    /// concerns — what a catalog surface reports.
    public SelectionException inDataset(String dataset) {
        return new SelectionException(kind, "dataset '" + dataset + "': " + getMessage(), position, selector, matches, offered);
    }

    /// The same syntax failure shifted to a position in a longer text,
    /// for a selector parsed out of a dataset spec.
    public SelectionException shifted(int offset) {
        if (kind != Kind.SYNTAX) return this;
        return syntax(position + offset, getMessage().substring(getMessage().indexOf(": ") + 2));
    }

    public Kind kind() { return kind; }
    /// Character offset into the selector text for [Kind#SYNTAX], `-1` otherwise.
    public int position() { return position; }
    /// The selector as written, or `null` when none was given.
    public String selector() { return selector; }
    /// The matching profiles for [Kind#AMBIGUOUS], the existing ones for
    /// [Kind#NO_DEFAULT], empty otherwise.
    public List<String> matches() { return matches; }
    /// What was on offer for [Kind#NO_MATCH], empty otherwise.
    public List<ProfileFacts> offered() { return offered; }
}
