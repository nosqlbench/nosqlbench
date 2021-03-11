package io.nosqlbench.virtdata.userlibs.apps.docsapp;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.api.processors.DocFuncData;
import io.nosqlbench.virtdata.core.bindings.VirtDataDocs;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Service(value = WebServiceObject.class, selector = "autodocs")
@Singleton
@Path("/services/virtdata/functions/")
public class AutoDocsWebService implements WebServiceObject {

    private final static Logger logger = LogManager.getLogger(AutoDocsWebService.class);
    private final List<DocFuncData> _docs = VirtDataDocs.getAllDocs();

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
    public List<DocFuncDataView> getAutoDocsDetails(@QueryParam("function") String fname) {
        return _docs.stream()
            .filter(d -> {
                if (fname == null || fname.isEmpty()) return true;
                String fullname = d.getPackageName() + "." + d.getClassName();
                return fullname.contains(fname);
            })
            .map(DocFuncDataView::new)
            .collect(Collectors.toList());

    }

}
