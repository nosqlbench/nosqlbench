package io.nosqlbench.activitytype.cqld4.core;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.session.Session;
import io.nosqlbench.activitytype.cqld4.codecsupport.UDTCodecInjector;
import com.datastax.driver.core.TokenRangeStmtFilter;
import io.nosqlbench.activitytype.cqld4.api.ErrorResponse;
import io.nosqlbench.activitytype.cqld4.api.D4ResultSetCycleOperator;
import io.nosqlbench.activitytype.cqld4.api.RowCycleOperator;
import io.nosqlbench.activitytype.cqld4.api.StatementFilter;
import io.nosqlbench.activitytype.cqld4.errorhandling.NBCycleErrorHandler;
import io.nosqlbench.activitytype.cqld4.errorhandling.HashedCQLErrorHandler;
import io.nosqlbench.activitytype.cqld4.statements.binders.CqlBinderTypes;
import io.nosqlbench.activitytype.cqld4.statements.rowoperators.RowCycleOperators;
import io.nosqlbench.activitytype.cqld4.statements.rowoperators.Save;
import io.nosqlbench.activitytype.cqld4.statements.rsoperators.ResultSetCycleOperators;
import io.nosqlbench.activitytype.cqld4.statements.rsoperators.TraceLogger;
import io.nosqlbench.activitytype.cqld4.statements.core.*;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtDef;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsBlock;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsDoc;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsDocList;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.ParameterMap;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionCountMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionHistoMetrics;
import io.nosqlbench.engine.api.util.SimpleConfig;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.engine.api.util.TagFilter;
import io.nosqlbench.engine.api.util.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("Duplicates")
public class CqlActivity extends SimpleActivity implements Activity, ActivityDefObserver {

    private final static Logger logger = LoggerFactory.getLogger(CqlActivity.class);
    private final ExceptionCountMetrics exceptionCountMetrics;
    private final ExceptionHistoMetrics exceptionHistoMetrics;
    private final ActivityDef activityDef;
    private final Map<String, Writer> namedWriters = new HashMap<>();
    protected List<OpTemplate> stmts;
    Timer retryDelayTimer;
    Timer bindTimer;
    Timer executeTimer;
    Timer resultTimer;
    Timer resultSuccessTimer;
    Timer pagesTimer;
    Histogram triesHisto;
    Histogram skippedTokensHisto;
    Histogram resultSetSizeHisto;
    int maxpages;
    Meter rowsCounter;
    private HashedCQLErrorHandler errorHandler;
    private OpSequence<ReadyCQLStatement> opsequence;
    private CqlSession session;
    private int maxTries;
    private StatementFilter statementFilter;
    private Boolean showcql;
    private List<RowCycleOperator> rowCycleOperators;
    private List<D4ResultSetCycleOperator> pageInfoCycleOperators;
    private List<StatementModifier> statementModifiers;
    private Long maxTotalOpsInFlight;
    private long retryDelay;
    private long maxRetryDelay;
    private boolean retryReplace;
    private String pooling;
    private String profileName;


    public CqlActivity(ActivityDef activityDef) {
        super(activityDef);
        this.activityDef = activityDef;
        exceptionCountMetrics = new ExceptionCountMetrics(activityDef);
        exceptionHistoMetrics = new ExceptionHistoMetrics(activityDef);
    }

    private void registerCodecs(Session session) {
        UDTCodecInjector injector = new UDTCodecInjector();
        injector.injectUserProvidedCodecs(session, true);
    }


    @Override
    public synchronized void initActivity() {
        logger.debug("initializing activity: " + this.activityDef.getAlias());
        profileName = getParams().getOptionalString("profile").orElse("default");
        session = getSession();

        if (getParams().getOptionalBoolean("usercodecs").orElse(false)) {
            registerCodecs(session);
        }
        initSequencer();
        setDefaultsFromOpSequence(this.opsequence);

        retryDelayTimer = ActivityMetrics.timer(activityDef, "retry-delay");
        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");
        resultTimer = ActivityMetrics.timer(activityDef, "result");
        triesHisto = ActivityMetrics.histogram(activityDef, "tries");
        pagesTimer = ActivityMetrics.timer(activityDef, "pages");
        rowsCounter = ActivityMetrics.meter(activityDef, "rows");
        skippedTokensHisto = ActivityMetrics.histogram(activityDef, "skipped-tokens");
        resultSuccessTimer = ActivityMetrics.timer(activityDef, "result-success");
        resultSetSizeHisto = ActivityMetrics.histogram(activityDef, "resultset-size");
        onActivityDefUpdate(activityDef);
        logger.debug("activity fully initialized: " + this.activityDef.getAlias());
    }

    public synchronized CqlSession getSession() {
        if (session == null) {
            session = CQLSessionCache.get().getSession(this.getActivityDef()).session;
        }
        return session;
    }

    private void initSequencer() {

        Session session = getSession();
        Map<String, Object> fconfig = Map.of("session", session);

        SequencerType sequencerType = SequencerType.valueOf(
                getParams().getOptionalString("seq").orElse("bucket")
        );
        SequencePlanner<ReadyCQLStatement> planner = new SequencePlanner<>(sequencerType);

        StmtsDocList unfiltered = loadStmtsYaml();

        // log tag filtering results
        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("");
        TagFilter tagFilter = new TagFilter(tagfilter);
        unfiltered.getStmts().stream().map(tagFilter::matchesTaggedResult).forEach(r -> logger.info(r.getLog()));

        stmts = unfiltered.getStmts(tagfilter);

        if (stmts.size() == 0) {
            throw new RuntimeException("There were no unfiltered statements found for this activity.");
        }

        for (OpTemplate stmtDef : stmts) {

            ParsedStmt parsed = stmtDef.getParsed().orError();

            boolean prepared = stmtDef.getParamOrDefault("prepared", true);
            boolean parameterized = stmtDef.getParamOrDefault("parameterized", false);
            long ratio = stmtDef.getParamOrDefault("ratio", 1);

            StringBuilder psummary = new StringBuilder();

            boolean instrument = stmtDef.getOptionalStringParam("instrument")
                    .or(() -> getParams().getOptionalString("instrument"))
                    .map(Boolean::parseBoolean)
                    .orElse(false);

            String logresultcsv = stmtDef.getParamOrDefault("logresultcsv","");
            String logresultcsv_act = getParams().getOptionalString("logresultcsv").orElse("");

            if (!logresultcsv_act.isEmpty() && !logresultcsv_act.toLowerCase().equals("true")) {
                throw new RuntimeException("At the activity level, only logresultcsv=true is allowed, no other values.");
            }
            logresultcsv = !logresultcsv.isEmpty() ? logresultcsv : logresultcsv_act;
            logresultcsv = !logresultcsv.toLowerCase().equals("true") ? logresultcsv : stmtDef.getName() + "--results.csv";

            logger.debug("readying statement[" + (prepared ? "" : "un") + "prepared]:" + parsed.getStmt());

            ReadyCQLStatementTemplate template;
            String stmtForDriver = parsed.getPositionalStatement(s -> "?");

            SimpleStatementBuilder stmtBuilder = SimpleStatement.builder(stmtForDriver);
            psummary.append(" statement=>").append(stmtForDriver);


            stmtDef.getOptionalStringParam("cl")
                    .map(DefaultConsistencyLevel::valueOf)
                    .map(conlvl -> {
                        psummary.append(" consistency_level=>").append(conlvl);
                        return conlvl;
                    })
                    .ifPresent(stmtBuilder::setConsistencyLevel);

            stmtDef.getOptionalStringParam("serial_cl")
                    .map(DefaultConsistencyLevel::valueOf)
                    .map(sconlvel -> {
                        psummary.append(" serial_consistency_level=>").append(sconlvel);
                        return sconlvel;
                    })
                    .ifPresent(stmtBuilder::setSerialConsistencyLevel);

            stmtDef.getOptionalStringParam("idempotent")
                    .map(Boolean::valueOf)
                    .map(idempotent -> {
                        psummary.append(" idempotent=").append(idempotent);
                        return idempotent;
                    })
                    .ifPresent(stmtBuilder::setIdempotence);


            if (prepared) {
                PreparedStatement preparedStatement = getSession().prepare(stmtBuilder.build());

                CqlBinderTypes binderType = stmtDef.getOptionalStringParam("binder")
                        .map(CqlBinderTypes::valueOf)
                        .orElse(CqlBinderTypes.DEFAULT);

                template = new ReadyCQLStatementTemplate(
                        fconfig,
                        binderType,
                        getSession(),
                        preparedStatement,
                        ratio,
                        parsed.getName()
                );
            } else {
                SimpleStatement simpleStatement = SimpleStatement.newInstance(stmtForDriver);
                template = new ReadyCQLStatementTemplate(fconfig, getSession(), simpleStatement, ratio,
                    parsed.getName(), parameterized);
            }


            stmtDef.getOptionalStringParam("save")
                    .map(s -> s.split("[,; ]"))
                    .map(Save::new)
                    .ifPresent(save_op -> {
                        psummary.append(" save=>").append(save_op.toString());
                        template.addRowCycleOperators(save_op);
                    });

            stmtDef.getOptionalStringParam("rsoperators")
                    .map(s -> s.split(","))
                    .stream().flatMap(Arrays::stream)
                    .map(ResultSetCycleOperators::newOperator)
                    .forEach(rso -> {
                        psummary.append(" rsop=>").append(rso.toString());
                        template.addResultSetOperators(rso);
                    });

            stmtDef.getOptionalStringParam("rowoperators")
                    .map(s -> s.split(","))
                    .stream().flatMap(Arrays::stream)
                    .map(RowCycleOperators::newOperator)
                    .forEach(ro -> {
                        psummary.append(" rowop=>").append(ro.toString());
                        template.addRowCycleOperators(ro);
                    });

            if (instrument) {
                logger.info("Adding per-statement success and error and resultset-size timers to statement '" + parsed.getName() + "'");
                template.instrument(this);
                psummary.append(" instrument=>").append(instrument);
            }

            if (!logresultcsv.isEmpty()) {
                logger.info("Adding per-statement result CSV logging to statement '" + parsed.getName() + "'");
                template.logResultCsv(this, logresultcsv);
                psummary.append(" logresultcsv=>").append(logresultcsv);
            }

            template.getContextualBindings().getBindingsTemplate().addFieldBindings(stmtDef.getParsed().getBindPoints());

            if (psummary.length() > 0) {
                logger.info("statement named '" + stmtDef.getName() + "' has custom settings:" + psummary.toString());
            }

            planner.addOp(template.resolve(), ratio);
        }

        opsequence = planner.resolve();

    }

    private StmtsDocList loadStmtsYaml() {
        StmtsDocList doclist = null;


        String yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload").orElse("default");

        StrInterpolator interp = new StrInterpolator(activityDef);

        String yamlVersion = "unset";
        if (yaml_loc.endsWith(":1") || yaml_loc.endsWith(":2")) {
            yamlVersion = yaml_loc.substring(yaml_loc.length() - 1);
            yaml_loc = yaml_loc.substring(0, yaml_loc.length() - 2);
        }

        switch (yamlVersion) {
            case "1":
                doclist = getVersion1StmtsDoc(interp, yaml_loc);
                logger.warn("DEPRECATED-FORMAT: Loaded yaml " + yaml_loc + " with compatibility mode. " +
                        "This will be deprecated in a future release.");
                logger.warn("DEPRECATED-FORMAT: Please refer to " +
                        "http://docs.engineblock.io/user-guide/standard_yaml/ for more details.");
                break;
            case "2":
                doclist = StatementsLoader.loadPath(logger, yaml_loc, interp, "activities");
                break;
            case "unset":
                try {
                    logger.debug("You can suffix your yaml filename or url with the " +
                            "format version, such as :1 or :2. Assuming version 2.");
                    doclist = StatementsLoader.loadPath(null, yaml_loc, interp, "activities");
                } catch (Exception ignored) {
                    try {
                        doclist = getVersion1StmtsDoc(interp, yaml_loc);
                        logger.warn("DEPRECATED-FORMAT: Loaded yaml " + yaml_loc +
                                " with compatibility mode. This will be deprecated in a future release.");
                        logger.warn("DEPRECATED-FORMAT: Please refer to " +
                                "http://docs.engineblock.io/user-guide/standard_yaml/ for more details.");
                    } catch (Exception compatError) {
                        logger.warn("Tried to load yaml in compatibility mode, " +
                                "since it failed to load with the standard format, " +
                                "but found an error:" + compatError);
                        logger.warn("The following detailed errors are provided only " +
                                "for the standard format. To force loading version 1 with detailed logging, add" +
                                " a version qualifier to your yaml filename or url like ':1'");
                        // retrigger the error again, this time with logging enabled.
                        doclist = StatementsLoader.loadPath(logger, yaml_loc, interp, "activities");
                    }
                }
                break;
            default:
                throw new RuntimeException("Unrecognized yaml format version, expected :1 or :2 " +
                        "at end of yaml file, but got " + yamlVersion + " instead.");
        }

        return doclist;

    }

    @Deprecated
    private StmtsDocList getVersion1StmtsDoc(StrInterpolator interp, String yaml_loc) {
        StmtsDocList unfiltered;
        List<RawStmtsBlock> blocks = new ArrayList<>();

        YamlCQLStatementLoader deprecatedLoader = new YamlCQLStatementLoader(interp);
        AvailableCQLStatements rawDocs = deprecatedLoader.load(yaml_loc, "activities");

        List<TaggedCQLStatementDefs> rawTagged = rawDocs.getRawTagged();

        for (TaggedCQLStatementDefs rawdef : rawTagged) {
            for (CQLStatementDef rawstmt : rawdef.getStatements()) {
                RawStmtsBlock rawblock = new RawStmtsBlock();

                // tags
                rawblock.setTags(rawdef.getTags());

                // params
                Map<String, Object> params = new HashMap<>(rawdef.getParams());
                if (rawstmt.getConsistencyLevel() != null && !rawstmt.getConsistencyLevel().isEmpty())
                    params.put("cl", rawstmt.getConsistencyLevel());
                if (!rawstmt.isPrepared()) params.put("prepared", "false");
                if (rawstmt.getRatio() != 1L)
                    params.put("ratio", String.valueOf(rawstmt.getRatio()));

                rawblock.setParams(params);


                // stmts
                List<RawStmtDef> stmtslist = new ArrayList<>();
                stmtslist.add(new RawStmtDef(rawstmt.getName(), rawstmt.getStatement()));
                rawblock.setRawStmtDefs(stmtslist);

                // bindings
                rawblock.setBindings(rawstmt.getBindings());

                blocks.add(rawblock);
            }
        }

        RawStmtsDoc rawStmtsDoc = new RawStmtsDoc();
        rawStmtsDoc.setBlocks(blocks);
        List<RawStmtsDoc> rawStmtsDocs = new ArrayList<>();
        rawStmtsDocs.add(rawStmtsDoc);
        RawStmtsDocList rawStmtsDocList = new RawStmtsDocList(rawStmtsDocs);
        unfiltered = new StmtsDocList(rawStmtsDocList);

        return unfiltered;
    }

    public ExceptionCountMetrics getExceptionCountMetrics() {
        return exceptionCountMetrics;
    }

    @Override
    public String toString() {
        return "CQLActivity {" +
                "activityDef=" + activityDef +
                ", session=" + session +
                ", opSequence=" + this.opsequence +
                '}';
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        clearResultSetCycleOperators();
        clearRowCycleOperators();
        clearStatementModifiers();

        ParameterMap params = activityDef.getParams();
        Optional<String> fetchSizeOption = params.getOptionalString("fetchsize");

        if (fetchSizeOption.isPresent()) {
            int fetchSize = fetchSizeOption.flatMap(Unit::bytesFor).map(Double::intValue).orElseThrow(() -> new RuntimeException(
                    "Unable to parse fetch size from " + fetchSizeOption.get()
            ));
            if (fetchSize > 10000000 && fetchSize < 1000000000) {
                logger.warn("Setting the fetchsize to " + fetchSize + " is unlikely to give good performance.");
            } else if (fetchSize > 1000000000) {
                throw new RuntimeException("Setting the fetch size to " + fetchSize + " is likely to cause instability.");
            }
            logger.trace("setting fetchSize to " + fetchSize);

            CQLSessionCache.get().getSession(activityDef).set(DefaultDriverOption.REQUEST_PAGE_SIZE, fetchSize);
        }

        this.retryDelay = params.getOptionalLong("retrydelay").orElse(0L);
        this.maxRetryDelay = params.getOptionalLong("maxretrydelay").orElse(500L);
        this.retryReplace = params.getOptionalBoolean("retryreplace").orElse(false);
        this.maxTries = params.getOptionalInteger("maxtries").orElse(10);
        this.showcql = params.getOptionalBoolean("showcql").orElse(false);
        this.maxpages = params.getOptionalInteger("maxpages").orElse(1);

        this.statementFilter = params.getOptionalString("tokens")
                .map(s -> new TokenRangeStmtFilter(getSession(), s))
                .orElse(null);

        if (statementFilter != null) {
            logger.info("filtering statements" + statementFilter);
        }

        errorHandler = configureErrorHandler();

        params.getOptionalString("trace")
                .map(SimpleConfig::new)
                .map(TraceLogger::new)
                .ifPresent(
                        tl -> {
                            addResultSetCycleOperator(tl);
                            addStatementModifier(tl);
                        });

        this.maxTotalOpsInFlight = params.getOptionalLong("async").orElse(1L);

        // TODO: Support dynamic pooling options
//        Optional<String> dynpooling = params.getOptionalString("pooling");
//        if (dynpooling.isPresent()) {
//            logger.info("dynamically updating pooling");
//            if (!dynpooling.get().equals(this.pooling)) {
//                PoolingOptions opts = CQLOptions.poolingOptionsFor(dynpooling.get());
//                logger.info("pooling=>" + dynpooling.get());
//
//                PoolingOptions cfg = getSession().getCluster().getConfiguration().getPoolingOptions();
//
//                // This looks funny, because we have to set max conns per host
//                // in an order that will appease the driver, as there is no "apply settings"
//                // to do that for us, so we raise max first if it goes higher, and we lower
//                // it last, if it goes lower
//                int prior_mcph_l = cfg.getMaxConnectionsPerHost(HostDistance.LOCAL);
//                int mcph_l = opts.getMaxConnectionsPerHost(HostDistance.LOCAL);
//                int ccph_l = opts.getCoreConnectionsPerHost(HostDistance.LOCAL);
//                if (prior_mcph_l < mcph_l) {
//                    logger.info("setting mcph_l to " + mcph_l);
//                    cfg.setMaxConnectionsPerHost(HostDistance.LOCAL, mcph_l);
//                }
//                logger.info("setting ccph_l to " + ccph_l);
//                cfg.setCoreConnectionsPerHost(HostDistance.LOCAL, ccph_l);
//                if (mcph_l < prior_mcph_l) {
//                    logger.info("setting mcph_l to " + mcph_l);
//                    cfg.setMaxRequestsPerConnection(HostDistance.LOCAL, mcph_l);
//                }
//                cfg.setMaxRequestsPerConnection(HostDistance.LOCAL, opts.getMaxRequestsPerConnection(HostDistance.LOCAL));
//
//                int prior_mcph_r = cfg.getMaxConnectionsPerHost(HostDistance.REMOTE);
//                int mcph_r = opts.getMaxConnectionsPerHost(HostDistance.REMOTE);
//                int ccph_r = opts.getCoreConnectionsPerHost(HostDistance.REMOTE);
//
//                if (mcph_r > 0) {
//                    if (mcph_r > prior_mcph_r) opts.setMaxConnectionsPerHost(HostDistance.REMOTE, mcph_r);
//                    opts.setCoreConnectionsPerHost(HostDistance.REMOTE, ccph_r);
//                    if (prior_mcph_r > mcph_r) opts.setMaxConnectionsPerHost(HostDistance.REMOTE, mcph_r);
//                    if (opts.getMaxConnectionsPerHost(HostDistance.REMOTE) > 0) {
//                        cfg.setMaxRequestsPerConnection(HostDistance.REMOTE, opts.getMaxRequestsPerConnection(HostDistance.REMOTE));
//                    }
//                }
//                this.pooling = dynpooling.get();
//            }
//        }

    }

    // TODO: make error handler updates consistent under concurrent updates

    private HashedCQLErrorHandler configureErrorHandler() {

        HashedCQLErrorHandler newerrorHandler = new HashedCQLErrorHandler(exceptionCountMetrics);

        String errors = activityDef.getParams()
                .getOptionalString("errors")
                .orElse("stop,retryable->retry,unverified->stop");


        String[] handlerSpecs = errors.split(",");
        for (String spec : handlerSpecs) {
            String[] keyval = spec.split("=|->|:", 2);
            if (keyval.length == 1) {
                String verb = keyval[0];
                newerrorHandler.setDefaultHandler(
                        new NBCycleErrorHandler(
                                ErrorResponse.valueOf(verb),
                                exceptionCountMetrics,
                                exceptionHistoMetrics,
                                getParams().getOptionalLong("async").isEmpty()
                        )
                );
            } else {
                String pattern = keyval[0];
                String verb = keyval[1];
                if (newerrorHandler.getGroupNames().contains(pattern)) {
                    NBCycleErrorHandler handler =
                            new NBCycleErrorHandler(
                                    ErrorResponse.valueOf(verb),
                                    exceptionCountMetrics,
                                    exceptionHistoMetrics,
                                    getParams().getOptionalLong("async").isEmpty()
                            );
                    logger.info("Handling error group '" + pattern + "' with handler:" + handler);
                    newerrorHandler.setHandlerForGroup(pattern, handler);
                } else {
                    NBCycleErrorHandler handler = new NBCycleErrorHandler(
                            ErrorResponse.valueOf(keyval[1]),
                            exceptionCountMetrics,
                            exceptionHistoMetrics,
                            getParams().getOptionalLong("async").isEmpty()
                    );
                    logger.info("Handling error pattern '" + pattern + "' with handler:" + handler);
                    newerrorHandler.setHandlerForPattern(keyval[0], handler);
                }
            }
        }

        return newerrorHandler;
    }

    public int getMaxTries() {
        return maxTries;
    }

    public HashedCQLErrorHandler getCqlErrorHandler() {
        return this.errorHandler;
    }

    public StatementFilter getStatementFilter() {
        return statementFilter;
    }

    public void setStatementFilter(StatementFilter statementFilter) {
        this.statementFilter = statementFilter;
    }

    public Boolean isShowCql() {
        return showcql;
    }

    public OpSequence<ReadyCQLStatement> getOpSequencer() {
        return opsequence;
    }

    public List<RowCycleOperator> getRowCycleOperators() {
        return rowCycleOperators;
    }

    protected synchronized void addRowCycleOperator(RowCycleOperator rsOperator) {
        if (rowCycleOperators == null) {
            rowCycleOperators = new ArrayList<>();
        }
        rowCycleOperators.add(rsOperator);
    }

    private void clearRowCycleOperators() {
        this.rowCycleOperators = null;
    }

    public List<D4ResultSetCycleOperator> getPageInfoCycleOperators() {
        return pageInfoCycleOperators;
    }

    protected synchronized void addResultSetCycleOperator(D4ResultSetCycleOperator pageInfoCycleOperator) {
        if (this.pageInfoCycleOperators == null) {
            this.pageInfoCycleOperators = new ArrayList<>();
        }
        this.pageInfoCycleOperators.add(pageInfoCycleOperator);
    }

    private void clearResultSetCycleOperators() {
        this.pageInfoCycleOperators = null;
    }

    public List<StatementModifier> getStatementModifiers() {
        return this.statementModifiers;
    }

    protected synchronized void addStatementModifier(StatementModifier modifier) {
        if (this.statementModifiers == null) {
            this.statementModifiers = new ArrayList<>();
        }
        this.statementModifiers.add(modifier);
    }

    private void clearStatementModifiers() {
        statementModifiers = null;
    }

    public long getMaxOpsInFlight(int slot) {
        int threads = this.getActivityDef().getThreads();
        return maxTotalOpsInFlight / threads + (slot < (maxTotalOpsInFlight % threads) ? 1 : 0);
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    public long getMaxRetryDelay() {
        return maxRetryDelay;
    }

    public void setMaxRetryDelay(long maxRetryDelay) {
        this.maxRetryDelay = maxRetryDelay;
    }

    public boolean isRetryReplace() {
        return retryReplace;
    }

    public void setRetryReplace(boolean retryReplace) {
        this.retryReplace = retryReplace;
    }

    public synchronized Writer getNamedWriter(String name) {
        Writer writer = namedWriters.computeIfAbsent(name, s -> {
            try {
                return new FileWriter(name, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        this.registerAutoCloseable(writer);
        return writer;
    }


}
