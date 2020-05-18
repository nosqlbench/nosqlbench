package io.nosqlbench.activitytype.cql.statements.core;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import io.nosqlbench.activitytype.cql.api.ResultSetCycleOperator;
import io.nosqlbench.activitytype.cql.api.RowCycleOperator;
import io.nosqlbench.activitytype.cql.core.CqlActivity;
import io.nosqlbench.activitytype.cql.statements.binders.CqlBinderTypes;
import io.nosqlbench.activitytype.cql.statements.binders.SimpleStatementValuesBinder;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.bindings.ContextualBindingsArrayTemplate;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.Map;

public class ReadyCQLStatementTemplate {

    private final static Logger logger = LoggerFactory.getLogger(ReadyCQLStatementTemplate.class);
    private final Session session;
    private ContextualBindingsArrayTemplate<?, Statement> template;
    private long ratio;
    private String name;

    private ResultSetCycleOperator[] resultSetCycleOperators;
    private RowCycleOperator[] rowCycleOperators;

    private Timer successTimer;
    private Timer errorTimer;
    private Histogram rowsFetchedHisto;
    private Writer resultCsvWriter;

    public ReadyCQLStatementTemplate(Map<String,Object> fconfig, CqlBinderTypes binderType, Session session,
                                     PreparedStatement preparedStmt, long ratio, String name) {
        this.session = session;
        this.name = name;
        ValuesArrayBinder<PreparedStatement, Statement> binder = binderType.get(session);
        logger.trace("Using binder_type=>" + binder.toString());

        template = new ContextualBindingsArrayTemplate<>(
                preparedStmt,
                new BindingsTemplate(fconfig),
                binder
        );
        this.ratio = ratio;
    }

    public ReadyCQLStatementTemplate(Map<String,Object> fconfig, Session session, SimpleStatement simpleStatement, long ratio, String name, boolean parametrized)     {
        this.session = session;
        this.name = name;
        template = new ContextualBindingsArrayTemplate<>(
                simpleStatement,
                new BindingsTemplate(fconfig),
                new SimpleStatementValuesBinder(parametrized)
        );
        this.ratio = ratio;
    }

    public ReadyCQLStatement resolve() {
        return new ReadyCQLStatement(template.resolveBindings(), ratio, name)
                .withMetrics(this.successTimer, this.errorTimer, this.rowsFetchedHisto)
                .withResultSetCycleOperators(resultSetCycleOperators)
                .withRowCycleOperators(rowCycleOperators)
                .withResultCsvWriter(resultCsvWriter);
    }

    public ContextualBindingsArrayTemplate<?, Statement> getContextualBindings() {
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

    public void addResultSetOperators(ResultSetCycleOperator... addingOperators) {
        resultSetCycleOperators = (resultSetCycleOperators==null) ? new ResultSetCycleOperator[0]: resultSetCycleOperators;

        ResultSetCycleOperator[] newOperators = new ResultSetCycleOperator[resultSetCycleOperators.length + addingOperators.length];
        System.arraycopy(resultSetCycleOperators,0,newOperators,0,resultSetCycleOperators.length);
        System.arraycopy(addingOperators,0,newOperators,resultSetCycleOperators.length,addingOperators.length);
        this.resultSetCycleOperators=newOperators;
    }

    public void addRowCycleOperators(RowCycleOperator... addingOperators) {
        rowCycleOperators = (rowCycleOperators==null) ? new RowCycleOperator[0]: rowCycleOperators;
        RowCycleOperator[] newOperators = new RowCycleOperator[rowCycleOperators.length + addingOperators.length];
        System.arraycopy(rowCycleOperators,0,newOperators,0,rowCycleOperators.length);
        System.arraycopy(addingOperators, 0, newOperators,rowCycleOperators.length,addingOperators.length);
        this.rowCycleOperators = newOperators;
    }


}
