package io.nosqlbench.activitytype.cockroachdb;

import io.nosqlbench.activitytype.jdbc.api.JDBCActivity;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;

public class CockroachActivity extends JDBCActivity {
    private static final Logger LOGGER = LogManager.getLogger(CockroachActivity.class);

    public CockroachActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    protected DataSource newDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();

        // serverName is required
        String serverName = getParams().
            getOptionalString("serverName").
            orElseThrow(() -> new RuntimeException("serverName parameter required"));

        // portNumber, user, password are optional
        Integer portNumber = getParams().getOptionalInteger("portNumber").orElse(26257);
        String user = getParams().getOptionalString("user").orElse(null);
        String password = getParams().getOptionalString("password").orElse(null);

        ds.setServerNames(new String[]{serverName});
        ds.setPortNumbers(new int[]{portNumber});
        if (user != null) {
            ds.setUser(user);
        }
        if (password != null) {
            ds.setPassword(password);
        }

        LOGGER.debug("Final DataSource fields"
            + " serverNames=" + Arrays.toString(ds.getServerNames())
            + " portNumbers=" + Arrays.toString(ds.getPortNumbers())
            + " user=" + ds.getUser()
            + " password=" + ds.getPassword());

        return ds;
    }

    @Override
    public boolean isRetryable(SQLException sqlException) {
        return sqlException.getSQLState().equals("40001"); // sql state code for transaction conflict
    }
}
