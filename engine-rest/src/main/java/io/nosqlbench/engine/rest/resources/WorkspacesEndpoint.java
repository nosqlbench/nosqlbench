package io.nosqlbench.engine.rest.resources;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.engine.rest.services.WorkspaceService;
import io.nosqlbench.engine.rest.transfertypes.WorkspaceView;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;

@Path("/services/workspaces/")
@Singleton
@Service(WebServiceObject.class)
public class WorkspacesEndpoint implements WebServiceObject {

    private final static Logger logger =
        LogManager.getLogger(WorkspacesEndpoint.class);

    public static final String WORKSPACE_ROOT = "workspace_root";

    @Context
    private Configuration config;

    private final static java.nio.file.Path workspacesRoot = Paths.get("workspaces");
    private WorkspaceService svc;

    /**
     * @return A list of workspaces as a
     * {@link List} of {@link WorkspaceView}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkspacesInfo() {
        List<WorkspaceView> wsviews = getSvc().getWorkspaceViews();
        return Response.ok(wsviews).build();
    }

    @GET
    @Path("{workspace}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkspaceInfo(@PathParam("workspace") String workspace) {
        WorkspaceView workpaceView = getSvc().getWorkspaceView(workspace);
        return Response.ok(workpaceView).build();
    }

    @POST
    @Path("{workspaceName}/upload/{filepath}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFileIntoWorkspace(
    ) {
        return Response.ok().build();
    }

    @POST
    @Path("{workspaceName}/{filepath}")
    public Response putFileInWorkspace(
        @PathParam("workspaceName") String workspaceName,
        @PathParam("filepath") String filename,
        ByteBuffer content
    ) {
        try {
            getSvc().putFile(workspaceName, filename, content);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("{workspaceName}/{filename}")
    public Response getFileInWorkspace(
        @PathParam("workspaceName") String workspaceName,
        @PathParam("filename") String filename) {

        try {
            WorkspaceService.FileInfo fileinfo = getSvc().readFile(workspaceName, filename);
            if (fileinfo != null) {
                return Response.ok(fileinfo.getContent(), fileinfo.getMediaType()).build();
            } else {
                return Response.noContent().status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private WorkspaceService getSvc() {
        if (svc == null) {
            svc = new WorkspaceService(config.getProperties().get(WORKSPACE_ROOT));
        }
        return svc;
    }

}
