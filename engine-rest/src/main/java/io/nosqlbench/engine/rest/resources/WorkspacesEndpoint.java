package io.nosqlbench.engine.rest.resources;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.engine.rest.domain.WorkSpace;
import io.nosqlbench.engine.rest.transfertypes.WorkspaceView;
import io.nosqlbench.engine.rest.transfertypes.WorkspacesInfo;
import io.nosqlbench.nb.annotations.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@Path("/services/workspaces")
@Singleton
@Service(WebServiceObject.class)
public class WorkspacesEndpoint implements WebServiceObject {

    private final static java.nio.file.Path workspacesRoot = Paths.get("workspaces");

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listWorkspaces() {
        WorkspacesInfo info = new WorkspacesInfo(workspacesRoot);
        return Response.ok(info).build();
    }

    @GET
    @Path("{workspace}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listWorkspace(@PathParam("workspace") String workspace) {
        WorkspaceView view = new WorkspaceView(workspace);
        return Response.ok(view).build();
    }

    @POST
    @Path("{workspaceName}/{filepath}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response putFile(
            @PathParam("workspaceName") String workspaceName,
            @PathParam("filepath") String filename
    ) {
        WorkSpace workspace = getWorkspace(workspaceName);
        Optional<Path> puttedFile = workspace.put(workspace);
        if (puttedFile.isPresent()) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("{workspaceName}/{filename}")
    public Response getFile(
            @PathParam("workspaceName") String workspaceName,
            @PathParam("filename") String filename) {

        WorkSpace workSpace = new WorkSpace(workspacesRoot, workspaceName);
        Optional<java.nio.file.Path> optFile = workSpace.get(filename);

        if (optFile.isPresent()) {
            try {
                java.nio.file.Path filepath = optFile.get();
                String contentType = Files.probeContentType(filepath);
                MediaType mediaType = MediaType.valueOf(contentType);

                byte[] bytes = Files.readAllBytes(filepath);
                return Response.ok(bytes, mediaType).build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Response.noContent().status(Response.Status.NOT_FOUND).build();
        }

    }


    private WorkSpace getWorkspace(String workspace) {
        if (!workspace.matches("[a-zA-Z][a-zA-Z0-9]+")) {
            throw new RuntimeException("Workspaces must start with an alphabetic" +
                    " character, and contain only letters and numbers.");
        }
        WorkSpace workSpace = new WorkSpace(workspacesRoot, workspace);
        return workSpace;
    }

}
