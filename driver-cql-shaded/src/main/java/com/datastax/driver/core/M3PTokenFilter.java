package com.datastax.driver.core;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
