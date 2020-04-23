package io.nosqlbench.virtdata.core.templates;

//import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
//import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.virtdata.core.bindings.Bindings;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

/**
 * Uses a string template and a bindings template to create instances of {@link StringBindings}.
 */
public class StringBindingsTemplate {

    private String stringTemplate;
    private BindingsTemplate bindingsTemplate;

    public StringBindingsTemplate(String stringTemplate, BindingsTemplate bindingsTemplate) {
        this.stringTemplate = stringTemplate;
        this.bindingsTemplate = bindingsTemplate;
    }

//    /**
//     * Build a default string bindings template using the standard representation
//     * for a string template in NoSQLBench, which is a literal string interspersed
//     * with named anchors in {@code {{curlybraces}}} form.
//     * @param stmtDef A stmtDef
//     */
//    public StringBindingsTemplate(StmtDef stmtDef) {
//        this(stmtDef, s->"{{"+s+"}}");
//    }
//
//    /**
//     * Build a string bindings template using a custom representation that maps
//     * the named anchors to a different form than the default {@code {{curlybraces}}} form.
//     * The mapping function provides the textual substitution which is used to composite
//     * the normative representation of the statement.
//     * @param stmtdef The {@link StmtDef} which provides the bindpoints
//     * @param tokenMapper A custom named anchor formatting function
//     */
//    public StringBindingsTemplate(StmtDef stmtdef, Function<String,String> tokenMapper) {
//        ParsedStmt parsedStmt = stmtdef.getParsed().orError();
//        this.stringTemplate = parsedStmt.getPositionalStatement(tokenMapper);
//        this.bindingsTemplate = new BindingsTemplate(parsedStmt.getBindPoints());
//    }

    /**
     * Create a new instance of {@link StringBindings}, preferably in the thread context that will use it.
     * @return a new StringBindings
     */
    public StringBindings resolve() {

        StringCompositor compositor = new StringCompositor(stringTemplate);
        HashSet<String> unqualifiedNames = new HashSet<>(compositor.getBindPointNames());
        unqualifiedNames.removeAll(new HashSet<>(bindingsTemplate.getBindPointNames()));
        if (unqualifiedNames.size()>0) {
            throw new RuntimeException("Named anchors were specified in the template which were not provided in the bindings: " + unqualifiedNames.toString());
        }

        Bindings bindings = bindingsTemplate.resolveBindings();
        return new StringBindings(compositor,bindings);
    }

    public String getDiagnostics() {
        StringCompositor compositor = new StringCompositor(stringTemplate);
        HashSet<String> unqualifiedNames = new HashSet<>(compositor.getBindPointNames());
        unqualifiedNames.removeAll(new HashSet<>(bindingsTemplate.getBindPointNames()));
        if (unqualifiedNames.size()>0) {
            throw new RuntimeException("Named anchors were specified in the template which were not provided in the bindings: " + unqualifiedNames.toString());
        }
        return bindingsTemplate.getDiagnostics();
    }

    @Override
    public String toString() {
        return "TEMPLATE:"+this.stringTemplate+" BINDING:"+bindingsTemplate.toString();
    }
}
