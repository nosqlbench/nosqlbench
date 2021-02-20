package io.nosqlbench.activitytype.cql.statements.rowoperators.verification;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerifierBuilder {
    public static BindingsTemplate getExpectedValuesTemplate(OpTemplate stmtDef) {

        BindingsTemplate expected = new BindingsTemplate();

        if (!stmtDef.getParams().containsKey("verify-fields") && !stmtDef.getParams().containsKey("verify")) {
            throw new RuntimeException("Unable to create expected values template with no 'verify' param");
        }

        Map<String, String> reading = stmtDef.getBindings();

        List<String> fields = new ArrayList<>();
        String fieldSpec = stmtDef.getOptionalStringParam("verify-fields")
            .or(() -> stmtDef.getOptionalStringParam("verify"))
            .orElse("*");

        String[] vfields = fieldSpec.split("\\s*,\\s*");
        for (String vfield : vfields) {
            if (vfield.equals("*")) {
                reading.forEach((k, v) -> fields.add(k));
            } else if (vfield.startsWith("+")) {
                fields.add(vfield.substring(1));
            } else if (vfield.startsWith("-")) {
                fields.remove(vfield.substring(1));
            } else if (vfield.matches("\\w+(\\w+->[\\w-]+)?")) {
                fields.add(vfield);
            } else {
                throw new RuntimeException("unknown verify-fields format: '" + vfield + "'");
            }
        }
        for (String vfield : fields) {
            String[] fieldNameAndBindingName = vfield.split("\\s*->\\s*", 2);
            String fieldName = fieldNameAndBindingName[0];
            String bindingName = fieldNameAndBindingName.length == 1 ? fieldName : fieldNameAndBindingName[1];
            if (!reading.containsKey(bindingName)) {
                throw new RuntimeException("binding name '" + bindingName +
                    "' referenced in verify-fields, but it is not present in available bindings.");
            }
            expected.addFieldBinding(fieldName, reading.get(bindingName));
        }
        return expected;
    }

}
