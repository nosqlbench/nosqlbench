package io.nosqlbench.activitytype.cql.codecsupport;

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


import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.UserType;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class UDTCodecInjector {
    private final static Logger logger = LogManager.getLogger(UDTCodecInjector.class);

    private final List<UserCodecProvider> codecProviders = new ArrayList<>();
    private final List<UserType> userTypes = new ArrayList<>();

    public void injectUserProvidedCodecs(Session session, boolean allowAcrossKeyspaces) {

        CodecRegistry registry = session.getCluster().getConfiguration().getCodecRegistry();

        ServiceLoader<UserCodecProvider> codecLoader = ServiceLoader.load(UserCodecProvider.class);

        for (UserCodecProvider userCodecProvider : codecLoader) {
            codecProviders.add(userCodecProvider);
        }

        for (UserCodecProvider codecProvider : codecProviders) {
            codecProvider.registerCodecsForCluster(session,true);
        }
    }
}
