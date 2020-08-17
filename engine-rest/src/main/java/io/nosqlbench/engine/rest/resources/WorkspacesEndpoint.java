package io.nosqlbench.engine.rest.resources;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.engine.rest.services.WorkspaceService;
import io.nosqlbench.engine.rest.transfertypes.WorkspaceView;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;

@Path("/services/workspaces/")
@Singleton
@Service(WebServiceObject.class)
public class WorkspacesEndpoint implements WebServiceObject {

    private final static Logger logger = LogManager.getLogger(WorkspacesEndpoint.class);

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

    @DELETE
    @Path("{workspace}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteWorkspace(@PathParam("workspace") String workspace,
                                    @QueryParam("deleteCount") String deleteCount) {
        try {
            int dc = deleteCount!=null ? Integer.valueOf(deleteCount):0;
            getSvc().purgeWorkspace(workspace,dc);
            return Response.ok("removed workspace " + workspace).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("{workspace}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkspaceInfo(@PathParam("workspace") String workspace) {
        try {
            WorkspaceView workpaceView = getSvc().getWorkspaceView(workspace);
            return Response.ok(workpaceView).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
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
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.WILDCARD)
    public Response doSomething(@Context HttpServletRequest request, byte[] input) {
        logger.debug("Content-Type: {}", request.getContentType());
        logger.debug("Preferred output: {}", request.getHeader(HttpHeaders.ACCEPT));
        try {
            String pathInfo = request.getPathInfo();
            String[] parts = pathInfo.split("/");
            String workspaceName = parts[parts.length-2];
            String filename = parts[parts.length-1];
            getSvc().putFile(workspaceName, filename, ByteBuffer.wrap(input));
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
                return Response.ok(fileinfo.getPath().toFile(), fileinfo.getMediaType()).build();
//                return Response.ok(fileinfo.getContent(), fileinfo.getMediaType()).build();
            } else {
                return Response.noContent().status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private WorkspaceService getSvc() {
        if (svc == null) {
            svc = new WorkspaceService(config);
        }
        return svc;
    }

}
