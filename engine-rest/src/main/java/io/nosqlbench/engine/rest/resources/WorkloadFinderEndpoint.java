package io.nosqlbench.engine.rest.resources;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;
import io.nosqlbench.engine.api.scenarios.WorkloadDesc;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.AutoDocsWebService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service(WebServiceObject.class)
@Singleton
@Path("/services/workloads")
public class WorkloadFinderEndpoint implements WebServiceObject {
    private final static Logger logger = LogManager.getLogger(AutoDocsWebService.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getWorkloadNames(@QueryParam("workspace") String workspace) {
        String[] includes = (workspace!=null ? new String[]{workspace} : new String[]{});
        List<WorkloadDesc> workloads = NBCLIScenarioParser.getWorkloadsWithScenarioScripts(includes);
        return workloads.stream().map(WorkloadDesc::getWorkloadName).collect(Collectors.toList());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("parameters")
    public Map<String, String> getParametersByWorkload(@QueryParam("workloadName") String workloadName) {
        List<WorkloadDesc> workloads = NBCLIScenarioParser.getWorkloadsWithScenarioScripts();

        Map<String, String> templates = null;

        templates = workloads.stream()
            .filter(workload -> workload.getWorkloadName().equals(workloadName))
            .map(workload -> workload.getTemplates())
            .collect(Collectors.toSet()).iterator().next();

        return templates;
    }

}
