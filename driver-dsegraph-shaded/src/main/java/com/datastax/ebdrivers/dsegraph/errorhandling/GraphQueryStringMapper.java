package com.datastax.ebdrivers.dsegraph.errorhandling;

import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.driver.dse.graph.RegularGraphStatement;
import com.datastax.driver.dse.graph.SimpleGraphStatement;

public class GraphQueryStringMapper {
    public static String getQueryString(GraphStatement statement) {
        String queryString;
        if (statement instanceof RegularGraphStatement) {
            queryString = ((SimpleGraphStatement) statement).getQueryString();
        } else {
            queryString = "(ERROR) Unknown statement type: " + statement.getClass().getCanonicalName();
        }
        return queryString;
    }

}
