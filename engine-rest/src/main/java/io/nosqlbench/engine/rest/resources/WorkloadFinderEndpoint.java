package io.nosqlbench.engine.rest.resources;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;
import io.nosqlbench.engine.api.scenarios.WorkloadDesc;
import io.nosqlbench.engine.rest.services.WorkspaceService;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.AutoDocsWebService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service(WebServiceObject.class)
@Singleton
@Path("/services/workloads")
public class WorkloadFinderEndpoint implements WebServiceObject {
    private final static Logger logger = LogManager.getLogger(AutoDocsWebService.class);

    @Context
    private Configuration config;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkloadDescriptions(@QueryParam("searchin") String searchin) {
//        WorkloadsView workloads = new WorkloadsView();
        List<WorkloadDesc> workloads = new ArrayList<>();
        WorkspaceService ws = new WorkspaceService(config);

        try {
            String[] includes = (searchin != null ? searchin.split(",") : new String[]{});
            for (String include : includes) {
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


            return Response.ok(workloads).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("parameters")
    public Map<String, String> getParametersByWorkload(@QueryParam("workloadName") String workloadName) {
        List<WorkloadDesc> workloads = NBCLIScenarioParser.getWorkloadsWithScenarioScripts(true);

        Map<String, String> templates = null;

        templates = workloads.stream()
            .filter(workload -> workload.getWorkloadName().equals(workloadName))
            .map(workload -> workload.getTemplates())
            .collect(Collectors.toSet()).iterator().next();

        return templates;
    }

}
