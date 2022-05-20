package io.nosqlbench.activitytype.cql.core;

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
