package com.datastax.driver.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;

public class M3PTokenFilter {
    private final TokenRange[] ranges;
    private final ProtocolVersion protocolVersion;
    private final CodecRegistry codecRegistry;
    private final Metadata clusterMetadata;
    private final Token.Factory factory;

    public M3PTokenFilter(Set<TokenRange> ranges, Cluster cluster) {
        protocolVersion = cluster.getConfiguration().getProtocolOptions().getProtocolVersion();
        codecRegistry = cluster.getConfiguration().getCodecRegistry();
        clusterMetadata = cluster.getMetadata();
        factory = Token.getFactory(clusterMetadata.partitioner);
        List<TokenRange> rangeList = new ArrayList<>();
        for (TokenRange range : ranges) {
            if (!range.getStart().getType().equals(DataType.bigint())) {
                throw new RuntimeException("This filter only works with bigint valued token types");
            }
            rangeList.add(range);
        }
        this.ranges=rangeList.toArray(new TokenRange[0]);
        if (this.ranges.length<1) {
            throw new RuntimeException("There were no tokens found. Please check your keyspace and cluster settings.");
        }
    }

    public OptionalLong matches(Statement statement) {
        ByteBuffer routingKey = statement.getRoutingKey(protocolVersion, codecRegistry);
        Token token = factory.hash(routingKey);

        for (TokenRange range : ranges) {
            if (range.contains(token)) {
                return OptionalLong.of((long)token.getValue());
            }
        }
        return OptionalLong.empty();
    }


}
