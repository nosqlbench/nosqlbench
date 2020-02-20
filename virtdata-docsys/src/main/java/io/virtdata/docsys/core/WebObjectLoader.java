package io.virtdata.docsys.core;

import io.virtdata.docsys.api.WebServiceObject;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class WebObjectLoader {
    public static List<WebServiceObject> loadWebServiceObjects() {
        List<WebServiceObject> endpoints = new ArrayList<>();
        ServiceLoader<WebServiceObject> loader = ServiceLoader.load(WebServiceObject.class);
        loader.forEach(endpoints::add);
        return endpoints;
    }
}
