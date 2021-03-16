package io.nosqlbench.docsys.endpoints;

import io.nosqlbench.docsys.api.Docs;
import io.nosqlbench.docsys.api.DocsBinder;
import io.nosqlbench.docsys.api.DocsNameSpace;
import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.docsys.core.DocsysPathLoader;
import io.nosqlbench.docsys.core.PathWalker;
import io.nosqlbench.nb.annotations.Service;
import jakarta.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service(value = WebServiceObject.class, selector = "docsys-markdown")
@Singleton
@Path("/services/docs/")
public class DocsysMarkdownEndpoint implements WebServiceObject {
    private final static Logger logger  = LogManager.getLogger(DocsysMarkdownEndpoint.class);
    private DocsBinder docsinfo;
    private DocsBinder enabled;
    private DocsBinder disabled;

    private final AtomicLong version = new AtomicLong(System.nanoTime());
    private final Set<String> enables = new HashSet<>();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("version")
    public long getVersion() {
        return version.get();
    }

    /**
     * If no enable= parameter is provided, then this call simply provides a map of
     * namespaces which are enabled and disabled.
     *
     * @param enable A set of namespaces to enable, or no provided value to enable all namespaces
     * @return A view of the namespaces known to this service
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("namespaces")
    public Map<String, Map<String, Set<java.nio.file.Path>>> getNamespaces(
            @QueryParam("enable") String enable,
            @QueryParam("reload") boolean reload
    ) {

        if (enable!=null && !enable.isEmpty()) {
            enables.clear();
            enables.addAll(List.of(enable.split("[, ;]")));
        }

        init(reload);
        enable(enables);

        return Map.of(
                "enabled",enabled.getPathMap(),
                "disabled",disabled.getPathMap()
        );
    }


    /**
     * @return Provide a list of all files from all enabled namespaces.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("allfiles.csv")
    public String getAllfilesList(@QueryParam("reload") boolean reload) {

        init(reload);

        StringBuilder sb = new StringBuilder();
        for (java.nio.file.Path path : enabled.getPaths()) {
            PathWalker.findAll(path).forEach(f -> {
                sb.append(path.relativize(f).toString()).append("\n");
            });
        }
        return sb.toString();
    }

    /**
     * @return Provide a lit of all files from all enabled namespaces
     * where the file path ends with '.md'
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("markdown.csv")
    public String getMarkdownList(@QueryParam("reload") boolean reload) {

        init(reload);

        StringBuilder sb = new StringBuilder();
        for (java.nio.file.Path path : enabled.getPaths()) {
            PathWalker.findAll(path).forEach(f -> {
                if (f.toString().endsWith(".md")) {
                    sb.append(path.relativize(f).toString()).append("\n");
                }
            });
        }
        return sb.toString();
    }

    /**
     * @return Provides a list of all files from all enabled namespaces as a JSON list.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list")
    public List<String> listFiles(@QueryParam("reload") boolean reload) {
        init(reload);
        List<String> list = new ArrayList<>();
        for (java.nio.file.Path path : enabled.getPaths()) {
            PathWalker.findAll(path).forEach(f -> {
                java.nio.file.Path relative = path.relativize(f);
                list.add(relative.toString());
            });
        }
        return list;
    }

    /**
     * @param pathspec the path as known to the manifest
     * @return The contents of a file
     */
    @GET
    @Path(value = "{pathspec:.*}")
    public Response getFileInPath(@PathParam("pathspec") String pathspec) {
        try {
            java.nio.file.Path path = findPath(pathspec);
            String contentType = Files.probeContentType(path);
            MediaType mediaType = MediaType.valueOf(contentType);

            return Response.ok(path.toFile(),mediaType).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    public java.nio.file.Path findPath(String pathspec) {
        pathspec = URLDecoder.decode(pathspec, StandardCharsets.UTF_8);
        for (java.nio.file.Path path : enabled.getPaths()) {
            java.nio.file.Path resolved = path.resolve(pathspec);
            if (Files.isDirectory(resolved)) {
                throw new RuntimeException("Path is a directory: '" + pathspec + "'");
            }
            if (Files.exists(resolved)) {
                return resolved;
            }
        }
        throw new RuntimeException("Unable to find any valid file at '" + pathspec + "'");

    }

    private void init(boolean reload) {
        if (reload) {
            this.enabled = null;
            this.disabled = null;
            this.docsinfo = null;
        }
        if (this.docsinfo == null) {
            this.docsinfo = DocsysPathLoader.loadDynamicPaths();
            version.set(System.nanoTime());
        }
        if (enabled==null || disabled==null) {
            enable(enables);
        }
    }

    private void enable(Set<String> enabled) {

        Set<String> toEnable = new HashSet<>();
        if (this.enables !=null) {
            toEnable.addAll(this.enables);
        }

        for (DocsNameSpace nsinfo : docsinfo) {
            // add namespaces which are neither enabled nor disabled to the default group
            if (nsinfo.isEnabledByDefault()) {
                if (disabled!=null && disabled.getPathMap().containsKey(nsinfo.getName())) {
                    continue;
                }
                enables.add(nsinfo.getName());
            }
        }

        if (enabled.isEmpty()) { // Nothing is enabled or enabled by default, so enable everything
            this.enabled = new Docs().merge(docsinfo);
            this.disabled = new Docs().asDocsBinder();
        } else { // At least one thing was enabled by default, or previously enabled specifically
            this.disabled = new Docs().merge(docsinfo);
            this.enabled = disabled.remove(enabled);
        }
    }


}
