package io.nosqlbench.activitytype.cqlverify;

import io.nosqlbench.activitytype.cql.core.CqlActivity;
import io.nosqlbench.activitytype.cql.statements.rsoperators.AssertSingleRowResultSet;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.virtdata.core.bindings.Bindings;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CqlVerifyActivity extends CqlActivity {

    private final static Logger logger = LogManager.getLogger(CqlVerifyActivity.class);
    private BindingsTemplate expectedValuesTemplate;
    private VerificationMetrics verificationMetrics;

    public CqlVerifyActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public synchronized void initActivity() {
        this.verificationMetrics = new VerificationMetrics(getActivityDef());

        super.initActivity();

        if (this.stmts.size() > 1) {
            throw new RuntimeException("More than one statement was configured as active. "
                    + this.getActivityDef().getActivityType() + " requires exactly one.");
        }

        Optional<String> randomMapper = stmts.stream()
                .flatMap(s -> s.getBindings().values().stream())
                .filter(t -> t.matches(".*Random.*") || t.matches(".*random.*"))
                .findAny();


        if (randomMapper.isPresent()) {
            throw new RuntimeException(
                    "You should not try to verify data generated with random mapping " +
                            "functions, like " + randomMapper.get() + " as it does not " +
                            "produce stable results in different invocation order.");
        }

    }

    public synchronized BindingsTemplate getExpectedValuesTemplate() {
        if (expectedValuesTemplate==null) {
            expectedValuesTemplate = new BindingsTemplate();
            Map<String, String> bindings = stmts.get(0).getBindings();
            if (stmts.get(0).getParams().containsKey("verify-fields")) {
                List<String> fields = new ArrayList<>();
                String fieldSpec= stmts.get(0).getParamOrDefault("verify-fields","");
                String[] vfields = fieldSpec.split("\\s*,\\s*");
                for (String vfield : vfields) {
                    if (vfield.equals("*")) {
                        bindings.forEach((k,v)->fields.add(k));
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
                    String bindingName = fieldNameAndBindingName.length==1 ? fieldName : fieldNameAndBindingName[1];
                    if (!bindings.containsKey(bindingName)) {
                        throw new RuntimeException("binding name '" + bindingName +
                                "' referenced in verify-fields, but it is not present in available bindings.");
                    }
                    expectedValuesTemplate.addFieldBinding(fieldName,bindings.get(bindingName));
                }
            } else {
                bindings.forEach((k,v)->expectedValuesTemplate.addFieldBinding(k,v));
            }
        }
        return expectedValuesTemplate;
    }

    public synchronized VerificationMetrics getVerificationMetrics() {
        return verificationMetrics;
    }

    @Override
    public void shutdownActivity() {
        super.shutdownActivity();
        VerificationMetrics metrics = getVerificationMetrics();
        long unverifiedValues = metrics.unverifiedValuesCounter.getCount();
        long unverifiedRows = metrics.unverifiedRowsCounter.getCount();

        if (unverifiedRows > 0 || unverifiedValues > 0) {
            throw new RuntimeException(
                    "There were " + unverifiedValues + " unverified values across " + unverifiedRows + " unverified rows."
            );
        }
        logger.info("verified " + metrics.verifiedValuesCounter.getCount() + " values across " + metrics.verifiedRowsCounter.getCount() + " verified rows");
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
        addResultSetCycleOperator(new AssertSingleRowResultSet());

        String verify = activityDef.getParams()
                .getOptionalString("compare").orElse("all");
        DiffType diffType = DiffType.valueOf(verify);
        Bindings verifyBindings = getExpectedValuesTemplate().resolveBindings();
        var differ = new RowDifferencer.ThreadLocalWrapper(
                getVerificationMetrics(),
                verifyBindings,
                diffType);
        addRowCycleOperator(differ);
    }
}
