package io.nosqlbench.activitytype.cqld4.errorhandling.exceptions;

import com.datastax.oss.driver.api.core.cql.ResultSet;

/**
 * <p>This is not a core exception. It was added to the CQL activity type
 * driver for nosqlbench specifically to catch the following unexpected
 * condition:
 * Paging would be needed to read all the results from a read query, but the user
 * is not expecting to intentionally check and iterate the result sets for paging.
 * <p>
 * This should only be thrown if a result set would need paging, but configuration
 * options specific that it should not expect to. Rather than assume paging is completely
 * expected or unexpected, we simply assume that only 1 page is allowed, being the
 * first page, or what is thought of as "not paging".
 * <p>If this error is thrown, and paging is expected, then the user can adjust
 * fetchsize or maxpages in order to open up paging to the degree that is allowable or
 * expected.
 */
public class UnexpectedPagingException extends CqlGenericCycleException {

    private final ResultSet resultSet;
    private final String queryString;
    private final int fetchSize;
    private int fetchedPages;
    private int maxpages;

    public UnexpectedPagingException(
            long cycle,
            ResultSet resultSet,
            String queryString,
            int fetchedPages,
            int maxpages,
            int fetchSize) {
        super(cycle);
        this.resultSet = resultSet;
        this.queryString = queryString;
        this.fetchedPages = fetchedPages;
        this.maxpages = maxpages;
        this.fetchSize = fetchSize;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Additional paging would be required to read the results from this query fully" +
                ", but the user has not explicitly indicated that paging was expected.")
                .append(" fetched/allowed: ").append(fetchedPages).append("/").append(maxpages)
                .append(" fetchSize(").append(fetchSize).append("): ").append(queryString);
        return sb.toString();
    }
}
