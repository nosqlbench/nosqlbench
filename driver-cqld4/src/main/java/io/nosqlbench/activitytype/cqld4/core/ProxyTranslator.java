package io.nosqlbench.activitytype.cqld4.core;

import com.datastax.driver.core.policies.AddressTranslator;
import com.datastax.driver.core.Cluster;

import java.net.InetSocketAddress;


public class ProxyTranslator implements AddressTranslator {

    private int hostsIndex = 0;

    private InetSocketAddress address;

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
