package io.nosqlbench.virtdata.userlibs.apps.docsapp;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.core.bindings.VirtDataDocs;
import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.virtdata.api.processors.DocFuncData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Service(WebServiceObject.class)
@Singleton
@Path("/services/virtdata/functions/")
public class AutoDocsWebService implements WebServiceObject {

    private final static Logger logger = LogManager.getLogger(AutoDocsWebService.class);
    private List<DocFuncData> _docs = VirtDataDocs.getAllDocs();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("names")
    public List<String> getAutoDocsNames() {
        return VirtDataDocs.getAllNames();
    }

    @GET
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public List<DocFuncData> getAutoDocsDetails(@QueryParam("function") String fname) {
        if (fname == null || fname.isEmpty()) {
            return _docs;
        }

        return _docs.stream()
                .filter(d -> {
                    String fullname = d.getPackageName() + "." + d.getClassName();
                    return fullname.contains(fname);
                }).collect(Collectors.toList());

    }

}
