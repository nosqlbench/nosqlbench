package io.nosqlbench.activitytype.cqld4.statements.core;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.session.Session;
import io.nosqlbench.activitytype.cqld4.api.D4ResultSetCycleOperator;
import io.nosqlbench.activitytype.cqld4.api.RowCycleOperator;
import io.nosqlbench.activitytype.cqld4.core.CqlActivity;
import io.nosqlbench.activitytype.cqld4.statements.binders.CqlBinderTypes;
import io.nosqlbench.activitytype.cqld4.statements.binders.SimpleStatementValuesBinder;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.bindings.ContextualBindingsArrayTemplate;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.Writer;
import java.util.Map;

public class ReadyCQLStatementTemplate {

    private final static Logger logger = LogManager.getLogger(ReadyCQLStatementTemplate.class);
    private final Session session;
    private final ContextualBindingsArrayTemplate<?, Statement<?>> template;
    private final long ratio;
    private final String name;

    private D4ResultSetCycleOperator[] pageInfoCycleOperators;
    private RowCycleOperator[] rowCycleOperators;

    private Timer successTimer;
    private Timer errorTimer;
    private Histogram rowsFetchedHisto;
    private Writer resultCsvWriter;

    public ReadyCQLStatementTemplate(
            Map<String,Object> fconfig,
            CqlBinderTypes binderType,
            CqlSession session,
            PreparedStatement preparedStmt,
            long ratio,
            String name
    ) {
        this.session = session;
        this.name = name;
        ValuesArrayBinder<PreparedStatement, Statement<?>> binder = binderType.get(session);
        logger.trace("Using binder_type=>" + binder.toString());

        template = new ContextualBindingsArrayTemplate<>(
                preparedStmt,
                new BindingsTemplate(fconfig),
                binder
        );
        this.ratio = ratio;
    }

    public ReadyCQLStatementTemplate(
        Map<String,Object> fconfig,
        Session session,
        SimpleStatement simpleStatement,
        long ratio,
        String name,
        boolean parameterized
    ) {
        this.session = session;
        this.name = name;
        template = new ContextualBindingsArrayTemplate(
                simpleStatement,
                new BindingsTemplate(fconfig),
            new SimpleStatementValuesBinder(parameterized)
        );
        this.ratio = ratio;
    }

    public ReadyCQLStatement resolve() {
        return new ReadyCQLStatement(template.resolveBindings(), ratio, name)
                .withMetrics(this.successTimer, this.errorTimer, this.rowsFetchedHisto)
                .withResultSetCycleOperators(pageInfoCycleOperators)
                .withRowCycleOperators(rowCycleOperators)
                .withResultCsvWriter(resultCsvWriter);
    }

    public ContextualBindingsArrayTemplate<?, Statement<?>> getContextualBindings() {
        return template;
    }


    public String getName() {
        return name;
    }

    public void instrument(CqlActivity activity) {
        this.successTimer = ActivityMetrics.timer(activity.getActivityDef(), name + "--success");
        this.errorTimer = ActivityMetrics.timer(activity.getActivityDef(), name + "--error");
        this.rowsFetchedHisto = ActivityMetrics.histogram(activity.getActivityDef(), name + "--resultset-size");
    }

    public void logResultCsv(CqlActivity activity, String name) {
        this.resultCsvWriter = activity.getNamedWriter(name);
    }

    public void addResultSetOperators(D4ResultSetCycleOperator... addingOperators) {
        pageInfoCycleOperators = (pageInfoCycleOperators ==null) ? new D4ResultSetCycleOperator[0]: pageInfoCycleOperators;

        D4ResultSetCycleOperator[] newOperators = new D4ResultSetCycleOperator[pageInfoCycleOperators.length + addingOperators.length];
        System.arraycopy(pageInfoCycleOperators,0,newOperators,0, pageInfoCycleOperators.length);
        System.arraycopy(addingOperators,0,newOperators, pageInfoCycleOperators.length,addingOperators.length);
        this.pageInfoCycleOperators =newOperators;
    }

    public void addRowCycleOperators(RowCycleOperator... addingOperators) {
        rowCycleOperators = (rowCycleOperators==null) ? new RowCycleOperator[0]: rowCycleOperators;
        RowCycleOperator[] newOperators = new RowCycleOperator[rowCycleOperators.length + addingOperators.length];
        System.arraycopy(rowCycleOperators,0,newOperators,0,rowCycleOperators.length);
        System.arraycopy(addingOperators, 0, newOperators,rowCycleOperators.length,addingOperators.length);
        this.rowCycleOperators = newOperators;
    }


}
