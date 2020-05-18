package com.datastax.driver.core;

import io.nosqlbench.activitytype.cql.api.StatementFilter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TokenRangeStmtFilter implements StatementFilter {

    private final Metadata clusterMetadata;
    private final ProtocolVersion protocolVersion;
    private final CodecRegistry codecRegistry;
    private final Token.Factory factory;
    private TokenRange[] ranges;

    public TokenRangeStmtFilter(Cluster cluster, String rangesSpec) {
        clusterMetadata = cluster.getMetadata();
        protocolVersion = cluster.getConfiguration().getProtocolOptions().getProtocolVersion();
        codecRegistry = cluster.getConfiguration().getCodecRegistry();
        factory = Token.getFactory(clusterMetadata.partitioner);
        ranges = parseRanges(factory, rangesSpec);
    }

    private TokenRange[] parseRanges(Token.Factory factory, String rangesStr) {
        String[] ranges = rangesStr.split(",");
        List<TokenRange> tr = new ArrayList<>();

        for (String range : ranges) {
            String[] interval = range.split(":");
            Token start = factory.fromString(interval[0]);
            Token end = factory.fromString(interval[1]);
            TokenRange tokenRange = new TokenRange(start, end, factory);
            tr.add(tokenRange);
        }
        return tr.toArray(new TokenRange[tr.size()]);
    }

    @Override
    public boolean matches(Statement statement) {
        ByteBuffer routingKey = statement.getRoutingKey(protocolVersion, codecRegistry);
        Token token = factory.hash(routingKey);
        for (TokenRange range : ranges) {
            if (range.contains(token)) {
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
