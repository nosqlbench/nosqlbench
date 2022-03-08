/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.docsys.endpoints;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.docsys.core.NBWebServer;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.annotations.Maturity;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service(value = WebServiceObject.class, selector = "docserver-status", maturity = Maturity.Deprecated)
@Singleton
@Path("_")
public class DocServerStatusEndpoint implements WebServiceObject {

    private final static Logger logger =
            LogManager.getLogger(DocServerStatusEndpoint.class);

    @Context
    private Configuration config;

    private String name;

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public String getStats() {
        NBWebServer s = (NBWebServer) config.getProperty("server");
        return s.toString();
    }

}
