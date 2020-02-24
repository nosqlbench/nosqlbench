package io.nosqlbench.docsys.handlers;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EndpointsHandler extends ServletHandler {
    @Override
    public void doHandle(
            String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException
    {
        if (request.getServletPath().equals("/status.json")) {
            response.setContentType("text/json");
            response.setStatus(HttpStatus.OK_200);
            response.getWriter().println("" +
                    "{" +
                    " 'status': 'OK'" +
                    "}" +
                    "");
            baseRequest.setHandled(true);
        }
    }
}
