package com.datastax.driver.core;

import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.TokenMap;
import com.datastax.oss.driver.api.core.metadata.token.Token;
import com.datastax.oss.driver.api.core.metadata.token.TokenRange;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import com.datastax.oss.driver.internal.core.metadata.token.Murmur3Token;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;

public class M3PTokenFilter {

    private final TokenRange[] ranges;

    public M3PTokenFilter(Set<TokenRange> ranges, Session session) {
        TokenMap tokenMap = session.getMetadata().getTokenMap().orElseThrow();

        List<TokenRange> rangelist = new ArrayList<>();

        for (TokenRange range : ranges) {
            rangelist.add(range);
        }
        this.ranges = rangelist.toArray(new TokenRange[0]);
        if (this.ranges.length<1) {
            throw new RuntimeException("There were no tokens found. Please check your keyspace and cluster settings.");
        }
    }

    public boolean matches(Statement statement) {
        Token token = statement.getRoutingToken();

        for (TokenRange range : ranges) {
            if (range.contains(token)) {
                return true;
            }
        }
        return false;
    }


}
