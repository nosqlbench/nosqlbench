package io.nosqlbench.engine.rest.resources;

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


import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;
import io.nosqlbench.engine.api.scenarios.WorkloadDesc;
import io.nosqlbench.engine.rest.services.WorkspaceFinder;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.AutoDocsWebService;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service(value = WebServiceObject.class, selector = "workload-finder")
@Singleton
@Path("/services/workloads")
public class WorkloadFinderEndpoint implements WebServiceObject {
    private final static Logger logger = LogManager.getLogger(AutoDocsWebService.class);

    @Context
    private Configuration config;

    public List<WorkloadDesc> getWorkloads(String search) {
        return getWorkloads(Set.of(search!=null ? search.split(",") : new String[0]));
    }

    public List<WorkloadDesc> getWorkloads(Set<String> search) {
        List<WorkloadDesc> workloads = new ArrayList<>();
        WorkspaceFinder ws = new WorkspaceFinder(config);

        for (String include : search) {
            if (include.equals("builtins")) {
                List<WorkloadDesc> activities = NBCLIScenarioParser.getWorkloadsWithScenarioScripts(true, "activities");
                for (WorkloadDesc desc : activities) {
                    workloads.add(desc);
                }
            } else {
                List<WorkloadDesc> descInWorkspace = ws.getWorkspace(include).getWorkloadsWithScenarioScripts();
                for (WorkloadDesc workload : descInWorkspace) {
                    workloads.add(workload);
                }
            }
        }
        return workloads;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkloadDescriptions(@QueryParam("searchin") String searchin) {
        WorkspaceFinder ws = new WorkspaceFinder(config);
        Set<String> searchIn = Set.of(searchin != null ? searchin.split(",") : new String[]{});

        try {
            List<WorkloadDesc> workloads = getWorkloads(searchIn);
            return Response.ok(workloads).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("parameters")
    public Map<String, String> getParametersByWorkload(
        @QueryParam("workloadName") String workloadName,
        @QueryParam("searchin") String searchin
    ) {
        List<WorkloadDesc> workloads = getWorkloads(searchin);

        Map<String, String> templates = null;

        templates = workloads.stream()
            .filter(workload -> workload.getWorkloadName().equals(workloadName))
            .map(workload -> workload.getTemplates())
            .collect(Collectors.toSet()).iterator().next();

        return templates;
    }

}
