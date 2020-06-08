package io.nosqlbench.activitytype.cqld4.core;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.virtdata.api.bindings.VALUE;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CQLBindHelper {

    private final ProtocolVersion protocolVersion;
    private final CodecRegistry codecRegistry;
//    private final ColumnDefinitions definitions;

    // refrence ProtocolConstants.DataType

    public CQLBindHelper(CqlSession session) {
        this.protocolVersion = session.getContext().getProtocolVersion();
        this.codecRegistry = session.getContext().getCodecRegistry();

    }

    private final static Pattern stmtToken = Pattern.compile("\\?(\\w+[-_\\d\\w]*)|\\{(\\w+[-_\\d\\w.]*)}");

    public Statement<?> rebindUnappliedStatement(
        Statement<?> statement,
        ColumnDefinitions defs,
        Row row) {

        if (!(statement instanceof BoundStatement)) {
            throw new RuntimeException("Unable to rebind a non-bound statement: " + statement.toString());
        }

        BoundStatement bound = (BoundStatement) statement;

        for (ColumnDefinition def : defs) {
            ByteBuffer byteBuffer = row.getByteBuffer(def.getName());
            bound=bound.setBytesUnsafe(def.getName(), byteBuffer);
        }
        return bound;
    }

    public BoundStatement bindStatement(Statement<?> statement, String name, Object value, DataType dataType) {

        if (!(statement instanceof BoundStatement)) {
            throw new RuntimeException("only BoundStatement is supported here");
        }
        BoundStatement bound = (BoundStatement) statement;

        if (value == VALUE.unset) {
            return bound.unset(name);
        } else {
            TypeCodec<Object> codec = codecRegistry.codecFor(dataType);
            return bound.set(name, value, codec);
        }
    }

    public static Map<String, String> parseAndGetSpecificBindings(StmtDef stmtDef, ParsedStmt parsed) {
        List<String> spans = new ArrayList<>();

        String statement = stmtDef.getStmt();

        Set<String> extraBindings = new HashSet<>();
        extraBindings.addAll(stmtDef.getBindings().keySet());
        Map<String, String> specificBindings = new LinkedHashMap<>();

        Matcher m = stmtToken.matcher(statement);
        int lastMatch = 0;
        String remainder = "";
        while (m.find(lastMatch)) {
            String pre = statement.substring(lastMatch, m.start());

            String form1 = m.group(1);
            String form2 = m.group(2);
            String tokenName = (form1 != null && !form1.isEmpty()) ? form1 : form2;
            lastMatch = m.end();
            spans.add(pre);

            if (extraBindings.contains(tokenName)) {
                if (specificBindings.get(tokenName) != null) {
                    String postfix = UUID.randomUUID().toString();
                    specificBindings.put(tokenName + postfix, stmtDef.getBindings().get(tokenName));
                } else {
                    specificBindings.put(tokenName, stmtDef.getBindings().get(tokenName));
                }
            }
        }
        return specificBindings;
    }
}
