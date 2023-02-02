package io.nosqlbench.adapter.jdbc.opdispensers;

import io.nosqlbench.adapter.jdbc.JDBCSpace;
import io.nosqlbench.adapter.jdbc.optypes.JDBCOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Statement;
import java.util.function.LongFunction;

public class JDBCQueryOpDispenser extends BaseOpDispenser<JDBCOp, JDBCSpace> {
    private final static Logger logger = LogManager.getLogger(JDBCQueryOpDispenser.class);
    private final DataSource dataSource;
    private final LongFunction<JDBCOp> jdbcOpLongFunction;
//    private final LongFunction<String> tableNameFunc;
    //private final LongFunction<String> targetFunction;

    public JDBCQueryOpDispenser(DriverAdapter adapter, LongFunction<JDBCSpace> jdbcSpaceLongFunction, ParsedOp op/*, LongFunction<String> targetFunction*/) {
        super(adapter, op);
        this.jdbcOpLongFunction = getOpFunc(jdbcSpaceLongFunction, op);
        //this.targetFunction = targetFunction;
        //TODO -- implement this
        dataSource = null;
    }

    public JDBCQueryOpDispenser(DriverAdapter<JDBCOp, JDBCSpace> adapter, ParsedOp op) {
        super(adapter, op);
        //TODO -- implement this
        this.jdbcOpLongFunction = null;
        this.dataSource = null;
        //this.targetFunction = null;
    }

    protected LongFunction<Statement> createStmtFunc(ParsedOp cmd) {
        LongFunction<Statement> basefunc = l -> null;//targetFunction.apply(l));
        return null;
    }

    private LongFunction<JDBCOp> getOpFunc(LongFunction<JDBCSpace> jdbcSpaceLongFunction, ParsedOp op) {
/*
        LongFunction<HttpRequest.Builder> builderF = l -> HttpRequest.newBuilder();
        LongFunction<String> bodyF = op.getAsFunctionOr("body", null);
        LongFunction<HttpRequest.BodyPublisher> bodyPublisherF =
            l -> Optional.ofNullable(bodyF.apply(l)).map(HttpRequest.BodyPublishers::ofString).orElse(
                HttpRequest.BodyPublishers.noBody()
            );

        LongFunction<String> methodF = op.getAsFunctionOr("method", "GET");
        LongFunction<HttpRequest.Builder> initBuilderF =
            l -> builderF.apply(l).method(methodF.apply(l), bodyPublisherF.apply(l));

        initBuilderF = op.enhanceFuncOptionally(
            initBuilderF, "version", String.class,
            (b, v) -> b.version(HttpClient.Version.valueOf(
                    v.replaceAll("/1.1", "_1_1")
                        .replaceAll("/2.0", "_2")
                )
            )
        );

        Optional<LongFunction<String>> optionalUriFunc = op.getAsOptionalFunction("uri", String.class);
        LongFunction<String> urifunc;
        // Add support for URLENCODE on the uri field if either it statically or dynamically contains the E or URLENCODE pattern,
        // OR the enable_urlencode op field is set to true.
        if (optionalUriFunc.isPresent()) {
            String testUriValue = optionalUriFunc.get().apply(0L);
            if (HttpFormatParser.URLENCODER_PATTERN.matcher(testUriValue).find()
                || op.getStaticConfigOr("enable_urlencode", true)) {
                initBuilderF =
                    op.enhanceFuncOptionally(
                        initBuilderF,
                        "uri",
                        String.class,
                        (b, v) -> b.uri(URI.create(HttpFormatParser.rewriteExplicitSections(v)))
                    );
            }
        } else {
            initBuilderF = op.enhanceFuncOptionally(initBuilderF, "uri", String.class, (b, v) -> b.uri(URI.create(v)));
        }

        op.getOptionalStaticValue("follow_redirects", boolean.class);


        List<String> headerNames = op.getDefinedNames().stream()
            .filter(n -> n.charAt(0) >= 'A')
            .filter(n -> n.charAt(0) <= 'Z')
            .toList();
        if (headerNames.size() > 0) {
            for (String headerName : headerNames) {
                initBuilderF = op.enhanceFunc(initBuilderF, headerName, String.class, (b, h) -> b.header(headerName, h));
            }
        }

        initBuilderF = op.enhanceFuncOptionally(initBuilderF, "timeout", long.class, (b, v) -> b.timeout(Duration.ofMillis(v)));

        LongFunction<HttpRequest.Builder> finalInitBuilderF = initBuilderF;
        LongFunction<HttpRequest> reqF = l -> finalInitBuilderF.apply(l).build();


        Pattern ok_status = op.getOptionalStaticValue("ok-status", String.class)
            .map(Pattern::compile)
            .orElse(Pattern.compile(DEFAULT_OK_STATUS));

        Pattern ok_body = op.getOptionalStaticValue("ok-body", String.class)
            .map(Pattern::compile)
            .orElse(null);

        LongFunction<HttpOp> opFunc = cycle -> new HttpOp(
            jdbcSpaceLongFunction.apply(cycle).getClient(),
            reqF.apply(cycle),
            ok_status,
            ok_body,
            jdbcSpaceLongFunction.apply(cycle), cycle
        );
    */
        //return null;
        LongFunction<JDBCOp> jdbcOpLongFunction = cycle -> new JDBCOp(jdbcSpaceLongFunction.apply(cycle), "DUMMY_STRINGcycle");
        return jdbcOpLongFunction;
    }

    @Override
    public JDBCOp apply(long value) {
        JDBCOp op = this.jdbcOpLongFunction.apply(value);
        return op;
    }
}
