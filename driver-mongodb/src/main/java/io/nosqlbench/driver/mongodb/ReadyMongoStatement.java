package io.nosqlbench.driver.mongodb;

import com.mongodb.ReadPreference;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import io.nosqlbench.virtdata.core.templates.StringBindingsTemplate;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ReadyMongoStatement {

    private final StringBindings bindings;
    private final ReadPreference readPreference;

    public ReadyMongoStatement(OpTemplate<?> stmtDef) {
        ParsedTemplate paramTemplate = new ParsedTemplate(stmtDef.getStmt(), stmtDef.getBindings());
        BindingsTemplate paramBindings = new BindingsTemplate(paramTemplate.getCheckedBindPoints());
        StringBindingsTemplate template = new StringBindingsTemplate(stmtDef.getStmt(), paramBindings);

        this.bindings = template.resolve();
        this.readPreference = stmtDef.getOptionalStringParam("readPreference")
                .map(ReadPreference::valueOf)
                .orElse(ReadPreference.primary());
    }

    public ReadPreference getReadPreference() {
        return readPreference;
    }

    public Bson bind(long value) {
        return Document.parse(bindings.bind(value));
    }
}
