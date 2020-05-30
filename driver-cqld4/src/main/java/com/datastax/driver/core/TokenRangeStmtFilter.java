package com.datastax.driver.core;

import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.token.Token;
import com.datastax.oss.driver.api.core.metadata.token.TokenRange;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import com.datastax.oss.driver.internal.core.metadata.token.Murmur3Token;
import com.datastax.oss.driver.internal.core.metadata.token.Murmur3TokenFactory;
import com.datastax.oss.driver.internal.core.metadata.token.Murmur3TokenRange;
import io.nosqlbench.activitytype.cqld4.api.StatementFilter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TokenRangeStmtFilter implements StatementFilter {

    private final Metadata clusterMetadata;
    private final ProtocolVersion protocolVersion;
    private final CodecRegistry codecRegistry;
//    private final Token.Factory factory;
    private TokenRange[] ranges;

    public TokenRangeStmtFilter(Session session, String rangesSpec) {
        clusterMetadata = session.getMetadata();
        protocolVersion = session.getContext().getProtocolVersion();
        codecRegistry = session.getContext().getCodecRegistry();
        ranges = parseRanges(session, rangesSpec);
    }

    private TokenRange[] parseRanges(Session session, String rangesStr) {
        String[] ranges = rangesStr.split(",");
        List<TokenRange> tr = new ArrayList<>();

        for (String range : ranges) {
            String[] interval = range.split(":");
            Murmur3TokenFactory m3f = new Murmur3TokenFactory();
            Token start = m3f.parse(interval[0]);
            Token end = m3f.parse(interval[1]);
            TokenRange tokenRange = m3f.range(start,end);
            tr.add(tokenRange);
        }
        return tr.toArray(new TokenRange[0]);
    }

    @Override
    public boolean matches(Statement<?> statement) {
        Token routingToken = statement.getRoutingToken();
        for (TokenRange range : ranges) {
            if (range.contains(routingToken)) {
                return true;
            }
        }
        return false;

    }

    @Override
    public String toString() {
        return "including token ranges: " +
                Arrays.stream(ranges)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
