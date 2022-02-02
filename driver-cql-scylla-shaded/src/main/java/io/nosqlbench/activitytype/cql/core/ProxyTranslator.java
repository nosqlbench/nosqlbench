package io.nosqlbench.activitytype.cql.core;

import com.datastax.driver.core.policies.AddressTranslator;
import com.datastax.driver.core.Cluster;

import java.net.InetSocketAddress;


public class ProxyTranslator implements AddressTranslator {

    private final int hostsIndex = 0;

    private final InetSocketAddress address;

    public ProxyTranslator(InetSocketAddress host){
        this.address= host;
    }

    @Override
    public void init(Cluster cluster) {
        // Nothing to do
    }

    @Override
    public InetSocketAddress translate(InetSocketAddress address) {
        return address;
    }

    @Override
    public void close() {
    }
}
