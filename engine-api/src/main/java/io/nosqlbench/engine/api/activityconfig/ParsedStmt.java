/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityconfig;

import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.nb.api.config.params.NBParams;
import io.nosqlbench.nb.api.config.params.Element;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Allow for uniform statement anchor parsing, using the <pre>?anchor</pre>
 * and <pre>{anchor}</pre> anchoring conventions. This type also includes
 * all of the properties from the enclosed StmtDef, in addition to a couple of
 * helpers. It should allow programmers to project this type directly from an
 * existing {@link StmtDef} as a substitute.
 */
public class ParsedStmt {

    private final static Pattern stmtToken = Pattern.compile("\\?(\\w+[-_\\d\\w]*)|\\{(\\w+[-_\\d\\w.]*)}");
    private final static Logger logger = LogManager.getLogger(ParsedStmt.class);
    private ParsedTemplate template;

    private final StmtDef stmtDef;
    private final ParsedTemplate parsed;

    /**
     * Construct a new ParsedStatement from the provided stmtDef and anchor token.
     *
     * @param stmtDef An existing statement def as read from the YAML API.
     */
    public ParsedStmt(StmtDef stmtDef) {
        this.stmtDef = stmtDef;
        parsed = new ParsedTemplate(stmtDef.getStmt(), stmtDef.getBindings());
    }

    public ParsedStmt orError() {
        if (hasError()) {
            throw new RuntimeException("Unable to parse statement: " + this.toString());
        }
        return this;
    }

    public String toString() {
        return parsed.toString();
    }

    /**
     * @return true if the parsed statement is not usable.
     */
    public boolean hasError() {
        return parsed.hasError();
    }

    /**
     * The list of binding names returned by this method does not
     * constitute an error. They may be used for
     * for informational purposes in error handlers, for example.
     *
     * @return a set of bindings names which were provided to
     * this parsed statement, but which were not referenced
     * in either <pre>{anchor}</pre> or <pre>?anchor</pre> form.
     */
    public Set<String> getExtraBindings() {
        return parsed.getExtraBindings();
    }

    /**
     * Returns a list of binding names which were referenced
     * in either <pre>{anchor}</pre> or <pre>?anchor</pre> form,
     * but which were not present in the provided bindings map.
     * If any binding names are present in the returned set, then
     * this binding will not be usable.
     *
     * @return A list of binding names which were referenced but not defined*
     */
    public Set<String> getMissingBindings() {
        return parsed.getMissingBindings();
    }

    /**
     * Return a map of bindings which were referenced in the statement.
     * This is an easy way to get the list of effective bindings for
     * a statement for diagnostic purposes without including a potentially
     * long list of library bindings.
     * @return a bindings map of referenced bindings in the statement
     */
    public Map<String, String> getSpecificBindings() {
        return parsed.getSpecificBindings();
    }

    /**
     * Return the statement that can be used as-is by any driver specific version.
     * This uses the anchor token as provided to yield a version of the statement
     * which contains positional anchors, but no named bindings.
     * @param tokenMapper A function which maps the anchor name to the needed form
     *                    in the callers driver context
     * @return A driver or usage-specific format of the statement, with anchors
     */
    public String getPositionalStatement(Function<String,String> tokenMapper) {
        return parsed.getPositionalStatement(tokenMapper);
    }

    /**
     * @return the statement name from the enclosed {@link StmtDef}
     */
    public String getName() {
        return stmtDef.getName();
    }

    /**
     * @return the raw statement from the enclosed {@link StmtDef}
     */
    public String getStmt() {
        return stmtDef.getStmt();
    }

    /**
     * @return the tags from the enclosed {@link StmtDef}
     */
    public Map<String, String> getTags() {
        return stmtDef.getTags();
    }

    /**
     * @return the bindings from the enclosed {@link StmtDef}
     */
    public Map<String, String> getBindings() {
        return stmtDef.getBindings();
    }

    /**
     * @return the params from the enclosed {@link StmtDef}
     * @deprecated You should use {@link #getParamReader()} instead.
     */
    public Map<String, Object> getParams() {
        return stmtDef.getParams();
    }

    /**
     * @return a params reader from the enclosed {@link StmtDef} params map
     */
    public Element getParamReader() {
        return NBParams.one(stmtDef.getParams());
    }

    public List<BindPoint> getBindPoints() {
        return parsed.getBindPoints();
    }
}
